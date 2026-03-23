# Neuerungen in einzelnen Klassen (Java 14–24)

Dieses Kapitel fasst Neuerungen in häufig verwendeten Klassen der Java-Standardbibliothek zusammen.

---

## 1. `java.util.Objects`

### Java 16 – `long`-Varianten der Bereichsprüfungen

Bereits seit Java 9 existierten `int`-basierte Bounds-Check-Methoden. Java 16 ergänzte
äquivalente Überladungen für `long` (wichtig für sehr große Datenstrukturen):

```java
// int-Varianten (seit Java 9):
int  Objects.checkIndex(int index, int length);
int  Objects.checkFromToIndex(int from, int to, int length);
int  Objects.checkFromIndexSize(int from, int size, int length);

// long-Varianten (neu in Java 16):
long Objects.checkIndex(long index, long length);
long Objects.checkFromToIndex(long from, long to, long length);
long Objects.checkFromIndexSize(long from, long size, long length);
```

Alle Methoden werfen `IndexOutOfBoundsException` bei ungültigen Werten und
**geben den geprüften Index zurück**, was die Verwendung in Ausdrücken erleichtert.

```java
// Beispiel: Slice-Funktion mit long-Indizes
public static byte[] slice(byte[] data, long fromIndex, long size) {
    Objects.checkFromIndexSize(fromIndex, size, data.length); // wirft bei Fehler
    return Arrays.copyOfRange(data, (int) fromIndex, (int) (fromIndex + size));
}
```

### Java 19 – `Objects.toIdentityString(Object)`

Liefert die **Identitätsdarstellung** eines Objekts – unabhängig davon, ob
`toString()` oder `hashCode()` überschrieben wurden:

```java
record Person(String name) {}  // toString() ist überschrieben

Person p = new Person("Alice");

System.out.println(p.toString());                 // Person[name=Alice]
System.out.println(Objects.toIdentityString(p));  // z.B. Person@5e2de80c
```

Nützlich für Debugging, wenn man die Objekt-Identität sehen will, nicht den überschriebenen Wert.

---

## 2. `java.lang.String`

### Stabile API (Java 17–25)

Die öffentliche API von `String` hat sich seit Java 11 kaum geändert.
Relevante Methoden existieren schon länger:

```java
"Hallo Welt".lines()        // → Stream<String>, seit Java 11
"  trim  ".strip()          // Unicode-aware trim, seit Java 11
"".isBlank()                // seit Java 11
"ha".repeat(3)              // "hahaha", seit Java 11
```

### Java 18 – Default-Encoding ist UTF-8

Seit Java 18 ist UTF-8 das **Standard-Encoding** der JVM – plattformunabhängig:

```java
// Java 17: Encoding abhängig von Plattform (z.B. Windows-1252 auf Windows)
byte[] bytes17 = "Ä".getBytes();  // plattformabhängig!

// Java 18+: garantiert UTF-8
byte[] bytes18 = "Ä".getBytes();  // immer UTF-8
```

> **Migrationshinweis:** Code, der explizit `Charset.defaultCharset()` verwendet,
> kann durch diesen Wechsel ein verändertes Verhalten zeigen.

### String Templates – zurückgezogen!

String Templates (JEP 430, Preview in Java 21) mit dem `STR`-Prozessor wurden in
**Java 23 zurückgezogen** (JEP 465). Sie sind in Java 25 **nicht vorhanden**.

```java
// NICHT verfügbar in Java 25:
// String msg = STR."Hello \{name}";  ❌

// Alternative: String.formatted() (immer verfügbar)
String msg = "Hello %s".formatted(name);

// Oder Text Block mit formatted():
String html = """
        <p>Hallo %s</p>
        """.formatted(name);
```

---

## 3. `java.util.stream.Stream`

### Java 16 – `Stream.toList()`

```java
// Früher (Java 8–15):
List<String> list = stream.collect(Collectors.toList());  // veränderlich, unspezifiziert

// Java 16+:
List<String> list = stream.toList();  // unveränderlich, klar definiert
```

| `toList()`              | `Collectors.toList()`           |
|-------------------------|---------------------------------|
| unveränderliche Liste   | meist veränderlich              |
| klar spezifiziert       | Implementierung bewusst offen   |
| kürzerer Code           | explizitere Semantik            |

### Java 24 – `Stream.gather()` (Stream Gatherers)

→ Vollständige Dokumentation: [../02_StandardAPI/stream_gatherers.md](../02_StandardAPI/stream_gatherers.md)

```java
// Neue Intermediate Operation:
Stream.of(1, 2, 3, 4, 5, 6)
    .gather(Gatherers.windowFixed(3))
    .toList();  // → [[1,2,3], [4,5,6]]
```

### Streams bleiben bewusst stabil

Die Stream-API wird nicht mit neuen Methoden überhäuft – die Stabilität ist gewollt.
Streams laufen durch JVM-Optimierungen heute deutlich schneller als unter Java 11.

---

## 4. NullPointerException – verbesserte Fehlermeldungen (Java 14+)

Seit Java 14 sind NPE-Fehlermeldungen deutlich aussagekräftiger:

```java
// Code:
String result = person.getKontakt().getNachname().toUpperCase();

// Java 13 und älter:
// NullPointerException (ohne weitere Info)

// Java 14+:
// Cannot invoke "Kontakt.getNachname()" because the return value
// of "Person.getKontakt()" is null
```

Die Meldung zeigt **genau**, welche Methode/Variable null war – kein Raten mehr.

### Nützlich in Stream-Pipelines

```java
list.stream()
    .map(x -> x.foo().bar().baz())
    .toList();

// Fehlermeldung zeigt exakt: "Cannot invoke 'Baz.process()' because
// 'x.foo().bar()' is null"
```

---

## 5. Records (Java 16, final)

Records sind kompakte Datenklassen mit automatisch generierten Methoden:

```java
// Normale Klasse (verbose):
public final class Point {
    private final int x, y;
    public Point(int x, int y) { this.x = x; this.y = y; }
    public int x() { return x; }
    public int y() { return y; }
    // equals, hashCode, toString...
}

// Record (kompakt):
record Point(int x, int y) {}  // generiert alles automatisch
```

### Compact Constructor für Validierung

```java
record Range(int from, int to) {
    // Compact Constructor – kein 'this.from = from' nötig (passiert implizit)
    Range {
        if (from > to) throw new IllegalArgumentException("from > to");
    }
}
```

### Records in Pattern Matching (Java 21)

```java
// Record-Dekonstruktion in switch:
switch (shape) {
    case Circle(double r)      -> Math.PI * r * r;
    case Rectangle(double w, double h) -> w * h;
}
```

---

## 6. Zusammenfassung

| Klasse/Feature     | Neuerung                              | Version |
|-------------------|---------------------------------------|---------|
| `Objects`         | `long`-Bereichsprüfungen              | 16      |
| `Objects`         | `toIdentityString()`                  | 19      |
| `String`          | Default-Encoding UTF-8                | 18      |
| `Stream`          | `toList()` (unveränderlich)           | 16      |
| `Stream`          | `gather()` / Stream Gatherers         | 24      |
| `NullPointerException` | Verbesserte Fehlermeldungen      | 14      |
| Records            | Kompakte Datenklassen                 | 16      |
