# Algebraische Datentypen und Pattern Matching (Java 16–22)

Dieses Kapitel behandelt Records, Sealed Classes und Pattern Matching –
die Grundlage für algebraische Datentypen in Java.

---

## Inhalt

1. [Records (Java 16, final)](#1-records-java-16-final)
2. [Sealed Classes (Java 17, final)](#2-sealed-classes-java-17-final)
3. [Pattern Matching für instanceof (Java 16, final)](#3-pattern-matching-für-instanceof)
4. [Pattern Matching für switch (Java 21, final)](#4-pattern-matching-für-switch)
5. [Record Patterns (Java 21, final)](#5-record-patterns)
6. [Unnamed Patterns (Java 22, final)](#6-unnamed-patterns-java-22)
7. [Zusammenspiel: Algebraische Datentypen](#7-zusammenspiel-algebraische-datentypen)

---

## 1. Records (Java 16, final)

Records sind **unveränderliche Datenträger** – die kompakte Alternative zu klassischen
Value-Klassen mit `equals`, `hashCode`, `toString`.

```java
// Klassische Klasse (verbose):
public final class Point {
    private final int x, y;
    public Point(int x, int y) { this.x = x; this.y = y; }
    public int x() { return x; }
    public int y() { return y; }
    @Override public boolean equals(Object o) { ... }
    @Override public int hashCode() { ... }
    @Override public String toString() { ... }
}

// Record (Java 16):
record Point(int x, int y) {}  // generiert alles automatisch
```

### Compact Constructor (Validierung)

```java
record Range(int from, int to) {
    // Kein 'this.from = from' nötig – passiert automatisch nach dem Block
    Range {
        if (from > to) throw new IllegalArgumentException(
            "from (%d) darf nicht größer als to (%d) sein".formatted(from, to));
    }
}
```

### Records und Interfaces

```java
interface Drawable {
    void draw();
}

record Circle(double radius) implements Drawable {
    @Override
    public void draw() {
        System.out.println("Kreis mit r=" + radius);
    }
}
```

### Records als DTOs

```java
record UserDto(String name, String email) {}

// Aus einer Datenbank-Abfrage:
record Row(String col1, String col2) {}
UserDto toUser(Row row) {
    return new UserDto(row.col1(), row.col2());
}
```

---

## 2. Sealed Classes (Java 17, final)

Sealed Classes beschränken, welche Klassen eine Klasse/ein Interface erweitern dürfen.
Das ist die Grundlage für **geschlossene Typhierarchien** (algebraische Summentypen).

```java
// Sealed Interface: nur Circle, Rectangle und Triangle dürfen es implementieren
sealed interface Shape permits Circle, Rectangle, Triangle {}

// Jede implementierende Klasse muss final, sealed oder non-sealed sein:
record Circle(double radius) implements Shape {}
record Rectangle(double width, double height) implements Shape {}
final class Triangle implements Shape {
    double base, height;
    Triangle(double base, double height) { this.base = base; this.height = height; }
}
```

### Sealed Classes und Exhaustiveness

Der Compiler kennt alle Untertypen und kann prüfen, ob alle Fälle abgedeckt sind:

```java
// Compiler-Fehler, falls ein Subtyp fehlt:
double area = switch (shape) {
    case Circle c    -> Math.PI * c.radius() * c.radius();
    case Rectangle r -> r.width() * r.height();
    case Triangle t  -> 0.5 * t.base * t.height;
    // kein 'default' nötig!
};
```

---

## 3. Pattern Matching für instanceof

### Java 16 – final

```java
// Klassisch (Java 15 und älter):
if (obj instanceof String) {
    String s = (String) obj;  // doppelter Cast
    System.out.println(s.toUpperCase());
}

// Java 16 (Pattern Matching):
if (obj instanceof String s) {
    System.out.println(s.toUpperCase());  // s direkt gebunden
}
```

### Kombination mit Logik (Java 16)

```java
// Kompakter Check mit zusätzlicher Bedingung:
if (obj instanceof String s && !s.isBlank()) {
    System.out.println("Nicht-leerer String: " + s);
}
```

---

## 4. Pattern Matching für switch

### Java 21 – final

```java
static String describe(Object obj) {
    return switch (obj) {
        case null              -> "null";
        case Integer i when i < 0 -> "Negative Zahl: " + i;
        case Integer i         -> "Positive/Null Zahl: " + i;
        case String s          -> "String: " + s;
        case Double d          -> "Double: " + d;
        default                -> "Sonstiges: " + obj.getClass().getSimpleName();
    };
}
```

> **Hinweis:** Der Guard-Ausdruck heißt **`when`** (nicht `&&` wie in alten Previews).

### Exhaustiveness bei Sealed Types

```java
sealed interface Result<T> permits Ok, Err {}
record Ok<T>(T value) implements Result<T> {}
record Err<T>(String message) implements Result<T> {}

// Compiler weiß: Result ist entweder Ok oder Err → kein default nötig
String text = switch (result) {
    case Ok<String>  ok  -> "Erfolg: " + ok.value();
    case Err<String> err -> "Fehler: " + err.message();
};
```

---

## 5. Record Patterns

### Java 21 – final

Record Patterns erlauben die **direkte Dekonstruktion** von Records in Patterns:

```java
record Point(int x, int y) {}

Object obj = new Point(3, 7);

// Dekonstruktion in instanceof:
if (obj instanceof Point(int x, int y)) {
    System.out.println("X=" + x + ", Y=" + y);  // x und y direkt gebunden
}

// Dekonstruktion in switch:
switch (obj) {
    case Point(int x, int y) when x == 0 && y == 0 -> System.out.println("Ursprung");
    case Point(int x, int y) -> System.out.println("Punkt " + x + "/" + y);
    default -> System.out.println("Kein Punkt");
}
```

### Verschachtelte Record Patterns

```java
record Address(String city, String country) {}
record Person(String name, Address address) {}

// Nested record pattern:
if (person instanceof Person(String name, Address(String city, _))) {
    System.out.println(name + " wohnt in " + city);
}
```

---

## 6. Unnamed Patterns (Java 22)

Der Unterstrich `_` ignoriert Teile eines Patterns (JEP 456, final in Java 22):

```java
// Nur den Typ prüfen, Wert nicht binden:
if (obj instanceof String _) {
    System.out.println("Ist ein String, Wert irrelevant");
}

// Record-Komponenten ignorieren:
record Point3D(int x, int y, int z) {}

switch (point) {
    case Point3D(int x, _, _) -> System.out.println("X=" + x);  // y und z ignoriert
}

// In Schleifen:
for (var entry : map.entrySet()) {
    if (entry.getValue() instanceof Integer _) {
        count++;  // Wert nicht gebraucht
    }
}
```

---

## 7. Zusammenspiel: Algebraische Datentypen

Records + Sealed Classes + Pattern Matching ergeben zusammen ein mächtiges
Werkzeug für **typsichere Datenmodelle**:

```java
// Sealed Hierarchy = algebraischer Summentyp
sealed interface JsonValue permits
    JsonNull, JsonBool, JsonNumber, JsonString, JsonArray, JsonObject {}

record JsonNull()                         implements JsonValue {}
record JsonBool(boolean value)            implements JsonValue {}
record JsonNumber(double value)           implements JsonValue {}
record JsonString(String value)           implements JsonValue {}
record JsonArray(List<JsonValue> items)   implements JsonValue {}
record JsonObject(Map<String, JsonValue> fields) implements JsonValue {}

// Exhaustive Pattern Matching ohne default:
String stringify(JsonValue v) {
    return switch (v) {
        case JsonNull _            -> "null";
        case JsonBool(boolean b)   -> String.valueOf(b);
        case JsonNumber(double n)  -> String.valueOf(n);
        case JsonString(String s)  -> "\"" + s + "\"";
        case JsonArray(var items)  -> items.stream()
                                         .map(this::stringify)
                                         .collect(joining(", ", "[", "]"));
        case JsonObject(var fields)-> fields.entrySet().stream()
                                         .map(e -> "\"" + e.getKey() + "\":" + stringify(e.getValue()))
                                         .collect(joining(", ", "{", "}"));
    };
}
```

---

## 8. Primitive Types in Patterns (Java 25, Preview)

→ Demo-Projekt: [PrimitivePatternProject/](PrimitivePatternProject/) (benötigt `--enable-preview`)

```java
// Bisher (Java 21): Wrapper-Typ nötig
if (obj instanceof Integer i && i > 0) { ... }

// Java 25 Preview: primitiver Typ direkt
if (obj instanceof int i && i > 0) { ... }

// switch über Objekt mit gemischten primitiven Patterns:
switch (obj) {
    case int i when i < 0 -> "negatives int";
    case int i            -> "nicht-negatives int";
    case double d         -> "double: " + d;
    case boolean b        -> "boolean: " + b;
    case String s         -> "String: " + s;
}
```

---

## 9. Übersicht

| Feature                    | Java-Version | Status  |
|---------------------------|--------------|---------|
| Records                    | 16           | final   |
| Sealed Classes             | 17           | final   |
| Pattern Matching instanceof| 16           | final   |
| Pattern Matching switch    | 21           | final   |
| Record Patterns            | 21           | final   |
| Unnamed Patterns (`_`)     | 22           | final   |
| Primitive Types in Patterns| 25           | Preview |
