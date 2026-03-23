# Stream Gatherers (Java 24, final)

> **JEP 485**, finalisiert in Java 24.

Stream Gatherers ermöglichen **benutzerdefinierte Zwischenoperationen** in Stream-Pipelines.
Sie schließen die letzte große Lücke der Stream-API: Bisher waren nur die eingebauten
Intermediate Operations (`filter`, `map`, `flatMap`, `distinct`, usw.) verfügbar.

---

## 1. Motivation

Manche Transformationen lassen sich mit den eingebauten Operationen nicht elegant ausdrücken:

- Sliding Window (je N aufeinanderfolgende Elemente)
- Scan (laufende Summe/Akkumulation)
- Elemente in Gruppen fester Größe aufteilen
- Zustandsbehaftete Filterung

Bisher: Umweg über `collect()` + eigene Collector-Logik oder externe Bibliotheken.
Ab Java 24: `stream.gather(Gatherer)` als neue Intermediate Operation.

---

## 2. Die Schnittstelle

```java
// Neue Intermediate Operation:
Stream<R> gather(Gatherer<? super T, ?, R> gatherer)
```

`Gatherer<T, A, R>` hat drei Typ-Parameter:
- `T` – Eingabe-Elementtyp
- `A` – Zustand (State)
- `R` – Ausgabe-Elementtyp

---

## 3. Eingebaute Gatherers: `java.util.stream.Gatherers`

Java 24 liefert sofort einsatzbereite Gatherers in der Klasse `Gatherers`:

### 3.1 `windowFixed(n)` – Fenster fester Größe

Gruppiert aufeinanderfolgende Elemente in Listen der Größe `n`:

```java
import java.util.stream.Gatherers;

var result = Stream.of(1, 2, 3, 4, 5, 6, 7)
    .gather(Gatherers.windowFixed(3))
    .toList();

// [[1, 2, 3], [4, 5, 6], [7]]
System.out.println(result);
```

### 3.2 `windowSliding(n)` – Gleitendes Fenster

Überlappende Fenster der Größe `n`:

```java
var result = Stream.of(1, 2, 3, 4, 5)
    .gather(Gatherers.windowSliding(3))
    .toList();

// [[1, 2, 3], [2, 3, 4], [3, 4, 5]]
System.out.println(result);
```

### 3.3 `scan(initial, combiner)` – Laufende Akkumulation

Erzeugt einen Stream der Zwischenwerte (wie `reduce`, aber alle Zwischenergebnisse):

```java
var result = Stream.of(1, 2, 3, 4, 5)
    .gather(Gatherers.scan(() -> 0, Integer::sum))
    .toList();

// [1, 3, 6, 10, 15]
System.out.println(result);
```

### 3.4 `fold(initial, combiner)` – Zustandsbehaftete Reduktion

Ähnlich `reduce`, aber als Intermediate Operation:

```java
var result = Stream.of("a", "b", "c")
    .gather(Gatherers.fold(() -> "", (acc, s) -> acc + s))
    .toList();

// ["abc"]
System.out.println(result);
```

### 3.5 `mapConcurrent(n, mapper)` – Paralleles Mapping mit Limit

Führt den Mapper für bis zu `n` Elemente gleichzeitig aus (nützlich mit virtuellen Threads):

```java
var result = Stream.of("url1", "url2", "url3", "url4")
    .gather(Gatherers.mapConcurrent(2, url -> fetch(url)))  // max. 2 parallel
    .toList();
```

---

## 4. Gatherers in der Pipeline

Gatherers lassen sich wie andere Intermediate Operations verketten:

```java
var result = Stream.iterate(1, n -> n + 1)
    .limit(10)
    .gather(Gatherers.windowFixed(3))   // [[1,2,3], [4,5,6], [7,8,9], [10]]
    .filter(window -> window.size() == 3)
    .map(window -> window.stream().mapToInt(Integer::intValue).sum())
    .toList();

// [6, 15, 24]
System.out.println(result);
```

---

## 5. Eigene Gatherers implementieren

```java
import java.util.stream.Gatherer;

// Gatherer, der nur jedes n-te Element durchlässt (wie eine konfigurierbare Version von "every nth")
static <T> Gatherer<T, ?, T> everyNth(int n) {
    return Gatherer.ofSequential(
        () -> new int[]{0},                     // Zustand: Counter-Array
        (state, element, downstream) -> {
            state[0]++;
            if (state[0] % n == 0) {
                downstream.push(element);       // Element weiterreichen
            }
            return true;                        // weiter verarbeiten
        }
    );
}

// Verwendung:
var result = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    .gather(everyNth(3))
    .toList();

// [3, 6, 9]
System.out.println(result);
```

---

## 6. Vergleich: Vor und nach Java 24

| Aufgabe                  | Java 21 (Workaround)              | Java 24 (Gatherer)                |
|--------------------------|-----------------------------------|-----------------------------------|
| Sliding Window           | externe Bibliothek oder Collector | `Gatherers.windowSliding(3)`      |
| Laufende Summe           | imperative Schleife               | `Gatherers.scan(() -> 0, sum)`    |
| Paralleles Mapping       | ExecutorService + collect         | `Gatherers.mapConcurrent(n, fn)`  |
| Eigene Zwischenoperation | Collector-API (komplex)           | `Gatherer.ofSequential(...)`      |

---

## 7. Zusammenfassung

- `gather()` ist die neue Intermediate Operation seit Java 24
- `java.util.stream.Gatherers` liefert sofort nutzbare Implementierungen
- Eigene Gatherers mit `Gatherer.ofSequential()` oder `Gatherer.of()` möglich
- Gut kombinierbar mit virtuellen Threads (z. B. `mapConcurrent`)
- Ergänzt die Stream-API um die letzte fehlende Kategorie
