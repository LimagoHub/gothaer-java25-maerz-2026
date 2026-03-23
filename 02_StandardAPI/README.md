# Standard-API Neuerungen (Java 19–24)

Dieses Kapitel behandelt Neuerungen in der Java Standard-API:
- **SequencedCollection API** (Java 21, JEP 431)
- **Stream Gatherers** (Java 24, JEP 485) → [stream_gatherers.md](stream_gatherers.md)
- **Factory-Methoden für Collections** (Java 19)

---

## 1. Motivation: Unified Sequenced API

Obwohl viele Collection-Typen in Java eine definierte Reihenfolge haben,
fehlte bis Java 21 eine **einheitliche Schnittstelle** für Operationen am Anfang und Ende:

| Collection       | getFirst/getLast? | addFirst/addLast? | reversed()?  |
|-----------------|-------------------|-------------------|--------------|
| `List`           | nein (nur Index)  | nein              | nein         |
| `Deque`          | ja                | ja                | nein         |
| `SortedSet`      | first()/last()    | nein              | nein         |
| `LinkedHashSet`  | nein              | nein              | nein         |

Java 21 löst das mit drei neuen Interfaces.

---

## 2. Java 19 – Neue Factory-Methoden

Neue Factory-Methoden erzeugen Collections mit vorberechneter Kapazität,
was Rehashing bei vielen Einträgen reduziert:

```java
// Mit erwarteter Größe (100 Elemente) vorinitialisieren:
Map<String, Integer> map  = HashMap.newHashMap(100);
Map<String, Integer> lmap = LinkedHashMap.newLinkedHashMap(100);
Set<String> set           = HashSet.newHashSet(100);
Set<String> lset          = LinkedHashSet.newLinkedHashSet(100);
Map<String, Integer> wmap = WeakHashMap.newWeakHashMap(100);
```

Der Parameter gibt die **erwartete Anzahl von Einträgen** an (nicht die initiale Kapazität).
Die JVM berechnet die optimale Anfangskapazität intern.

---

## 3. Java 21 – SequencedCollection (JEP 431)

### 3.1 Die drei neuen Interfaces

```java
// Für Collections mit definierter Encounter-Order:
public interface SequencedCollection<E> extends Collection<E> {
    SequencedCollection<E> reversed();
    void addFirst(E e);
    void addLast(E e);
    E getFirst();
    E getLast();
    E removeFirst();
    E removeLast();
}

// Für Sets mit definierter Order:
public interface SequencedSet<E> extends Set<E>, SequencedCollection<E> {
    SequencedSet<E> reversed();
}

// Für Maps mit definierter Key-Order:
public interface SequencedMap<K, V> extends Map<K, V> {
    SequencedMap<K, V> reversed();
    SequencedSet<K>          sequencedKeySet();
    SequencedCollection<V>   sequencedValues();
    SequencedSet<Entry<K,V>> sequencedEntrySet();
    V putFirst(K key, V value);
    V putLast(K key, V value);
    Entry<K,V> firstEntry();
    Entry<K,V> lastEntry();
}
```

### 3.2 Welche Klassen implementieren die neuen Interfaces?

| Klasse           | Interface             |
|------------------|-----------------------|
| `ArrayList`      | `SequencedCollection` |
| `LinkedList`     | `SequencedCollection` |
| `ArrayDeque`     | `SequencedCollection` |
| `LinkedHashSet`  | `SequencedSet`        |
| `TreeSet`        | `SequencedSet`        |
| `LinkedHashMap`  | `SequencedMap`        |
| `TreeMap`        | `SequencedMap`        |

### 3.3 Praxisbeispiele

```java
// SequencedCollection mit List:
List<String> list = new ArrayList<>(List.of("a", "b", "c", "d"));

String first = list.getFirst();   // "a"  (statt list.get(0))
String last  = list.getLast();    // "d"  (statt list.get(list.size()-1))

list.addFirst("z");               // z, a, b, c, d
list.removeLast();                // z, a, b, c

List<String> reversed = list.reversed();  // c, b, a, z (View, keine Kopie!)
```

```java
// SequencedSet mit LinkedHashSet:
SequencedSet<String> set = new LinkedHashSet<>(List.of("X", "Y", "Z"));

System.out.println(set.getFirst()); // "X"
System.out.println(set.getLast());  // "Z"
System.out.println(set.reversed()); // [Z, Y, X]
```

```java
// SequencedMap mit LinkedHashMap:
SequencedMap<Integer, String> map = new LinkedHashMap<>();
map.putLast(2, "zwei");
map.putLast(3, "drei");
map.putFirst(1, "eins");    // 1 wird an den Anfang eingefügt

System.out.println(map.firstEntry()); // 1=eins
System.out.println(map.lastEntry());  // 3=drei
```

### 3.4 Polymorphe Verwendung

```java
// Einheitlicher Code für alle sequenzierten Collections:
void printBounds(SequencedCollection<?> col) {
    System.out.println("Erstes Element: " + col.getFirst());
    System.out.println("Letztes Element: " + col.getLast());
}

// Funktioniert für List, LinkedHashSet, ArrayDeque etc.:
printBounds(new ArrayList<>(List.of(1, 2, 3)));
printBounds(new LinkedHashSet<>(Set.of("a", "b", "c")));
```

---

## 4. Java 24 – Stream Gatherers (JEP 485)

→ Vollständige Dokumentation: [stream_gatherers.md](stream_gatherers.md)

Kurzübersicht der wichtigsten eingebauten Gatherers:

```java
import java.util.stream.Gatherers;

// Fenster fester Größe:
Stream.of(1,2,3,4,5,6).gather(Gatherers.windowFixed(3)).toList();
// → [[1,2,3], [4,5,6]]

// Gleitendes Fenster:
Stream.of(1,2,3,4,5).gather(Gatherers.windowSliding(3)).toList();
// → [[1,2,3], [2,3,4], [3,4,5]]

// Laufende Summe (scan):
Stream.of(1,2,3,4,5).gather(Gatherers.scan(() -> 0, Integer::sum)).toList();
// → [1, 3, 6, 10, 15]

// Paralleles Mapping mit maximalem Parallelitätsgrad:
stream.gather(Gatherers.mapConcurrent(4, item -> fetchRemote(item))).toList();
```

---

## 5. Zusammenfassung

| Version | Neuerung                                           |
|---------|----------------------------------------------------|
| Java 19 | Factory-Methoden: `HashMap.newHashMap(n)` etc.     |
| Java 21 | `SequencedCollection`, `SequencedSet`, `SequencedMap` |
| Java 24 | Stream Gatherers (`gather()`, `Gatherers.*`)        |
