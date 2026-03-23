# Switch Expressions, Text Blocks & Pattern Matching (Java 12–25)

Dieses Kapitel behandelt die Entwicklung der `switch`-Syntax, Text Blocks und verwandter Sprachfeatures von Java 12 bis Java 25.

---

## Inhalt

- [Switch Expressions](#switch-expressions)
- [Text Blocks](#text-blocks)
- [Pattern Matching for switch (Java 21, final)](#pattern-matching-for-switch)
- [Unnamed Variables & Patterns (Java 22, final)](#unnamed-variables--patterns-java-22)
- [Primitive Types in Patterns (Java 25, Preview)](#primitive-types-in-patterns-java-25-preview)

---

## Switch Expressions

### Java 11 und älter – Klassischer `switch` (nur Statement)

- `switch` ist **kein Ausdruck**, liefert keinen Wert
- **Fallthrough** ist Standard → `break` zwingend
- Keine kompakten Case-Labels möglich

```java
int numLetters;
switch (day) {
    case MONDAY:
    case FRIDAY:
    case SUNDAY:
        numLetters = 6;
        break;
    case TUESDAY:
        numLetters = 7;
        break;
    default:
        numLetters = 8;
}
```

---

### Java 12 – Switch Expressions (Preview)

- `switch` kann **Ausdruck** sein
- **Arrow-Cases** ohne Fallthrough (`->`)
- Mehrere Labels pro Case: `case A, B ->`
- Blockarme nutzen noch `break value`

```java
int numLetters = switch (day) {
    case MONDAY, FRIDAY, SUNDAY -> 6;
    case TUESDAY                -> 7;
    default                     -> 8;
};
```

---

### Java 13 – `yield` ersetzt `break value`

```java
int result = switch (day) {
    case MONDAY -> 0;
    case TUESDAY -> 1;
    default -> {
        int temp = compute(day);
        yield temp;   // yield gibt den Wert der switch-Expression zurück
    }
};
```

---

### Java 14 – Switch Expressions final

- Switch Expressions sind **offizieller Sprachbestandteil**
- Arrow-Syntax und `yield` sind final
- Switch-Expressions müssen **exhaustiv** sein (alle Fälle abgedeckt)

---

### Java 17–20 – Pattern Matching (Preview)

- Typ-Patterns in `switch`
- Record-Dekonstruktion
- `when`-Guards (in Preview-Versionen hieß es noch `&&`)

---

## Pattern Matching for switch

### Java 21 – Final

Pattern Matching for switch ist seit Java 21 **offizieller Sprachbestandteil**.

```java
static String describe(Object obj) {
    return switch (obj) {
        case null              -> "null";
        case String s          -> "String: " + s;
        case Integer i when i > 0 -> "Positive Zahl: " + i;  // 'when' statt '&&'
        case Integer i         -> "Nicht-positive Zahl: " + i;
        case Point(int x, int y) -> "Point(" + x + ", " + y + ")";
        default                -> "Unbekannter Typ";
    };
}
```

> **Hinweis:** In Preview-Versionen (Java 17–20) wurden Guards noch mit `&&` geschrieben.
> In Java 21 ist der Guard-Ausdruck das Keyword **`when`** – nicht `&&`.

```java
record Point(int x, int y) {}

String desc = switch (p) {
    case Point(int x, int y) when x == 0 && y == 0 -> "Origin";
    case Point(int x, int y)                        -> "Point(" + x + "," + y + ")";
};
```

### Was kann alles in `case` stehen? (Java 21)

| Art des Case-Labels   | Beispiel                        | Seit            |
|-----------------------|---------------------------------|-----------------|
| Konstante Werte       | `case 1:`                       | immer           |
| Mehrere Werte         | `case A, B ->`                  | Java 12         |
| Arrow-Syntax          | `case X ->`                     | Java 12         |
| `default`             | `default ->`                    | immer           |
| Type Patterns         | `case String s ->`              | final Java 21   |
| Record Patterns       | `case Point(int x, int y)`      | final Java 21   |
| `null`                | `case null ->`                  | Java 21         |
| Guards                | `case X when cond ->`           | Java 21         |
| `yield`-Blöcke        | `case X -> { yield v; }`        | final Java 14   |

### Sealed Classes und Exhaustiveness

Bei Sealed Classes erkennt der Compiler automatisch, dass alle Fälle abgedeckt sind:

```java
sealed interface Shape permits Circle, Square, Rectangle {}

String area = switch (shape) {
    case Circle c    -> "Kreis r=" + c.radius();
    case Square s    -> "Quadrat a=" + s.side();
    case Rectangle r -> "Rechteck " + r.width() + "x" + r.height();
    // kein 'default' nötig – Compiler prüft Vollständigkeit
};
```

---

## Text Blocks

### Java 13 – Erste Preview

Text Blocks erlauben mehrzeilige Strings ohne `\n` und `\"`:

```java
String html = """
        <html>
            <body>Hello World</body>
        </html>
        """;
```

### Java 14 – Neue Escape-Sequenzen

- `\s` → explizites Leerzeichen (verhindert trailing-whitespace-Trimming)
- `\` am Zeilenende → kein Zeilenumbruch im String (Zeilenfortsetzung)

```java
String einzeiler = """
        Hello \
        World
        """;
// Ergebnis: "Hello World\n"
```

### Java 15 – Text Blocks final

Text Blocks sind seit Java 15 **offizieller Sprachbestandteil** – keine Preview-Flags mehr nötig.

### Java 21 – String Templates (Preview → zurückgezogen)

> **Wichtig für Java 25:** String Templates (JEP 430) mit dem `STR`-Prozessor wurden in Java 21 als Preview eingeführt, aber in **Java 23 zurückgezogen** (JEP 465 Withdrawal). Sie sind in Java 25 **nicht vorhanden**.
>
> Verwende stattdessen `String.formatted()`:

```java
String name = "Lisa";

// Java 25 – korrekt:
String msg = """
        Hello %s,
        Welcome to Java 25!
        """.formatted(name);

// NICHT mehr verfügbar (zurückgezogen):
// String msg = STR."Hello \{name}";  // ❌ kompiliert nicht
```

---

## Unnamed Variables & Patterns (Java 22)

Unnamed Variables (JEP 456) sind seit Java 22 **finalisiert**.

Der Unterstrich `_` markiert eine Variable, deren Wert nicht benötigt wird. Das verbessert die Lesbarkeit bei Pattern Matching.

### Unnamed Variables

```java
// Klassisch: Variablenname erzwungen, obwohl nicht genutzt
try {
    riskyOperation();
} catch (IOException ignored) { }   // 'ignored' – aber nie gelesen

// Modern (Java 22):
try {
    riskyOperation();
} catch (IOException _) { }         // _ macht die Absicht klar
```

### Unnamed Patterns

```java
// Nur den Typ prüfen, Wert nicht binden:
if (obj instanceof String _) {
    System.out.println("Es ist ein String");
}

// In switch – ignorierte Record-Komponenten:
record Point(int x, int y, int z) {}

switch (point) {
    case Point(int x, _, _) -> System.out.println("X=" + x);
}
```

### Unnamed Patterns in Schleifen

```java
List<Object> items = List.of("a", 42, "b", 3.14);

// Nur Strings zählen, Wert ignorieren:
long count = items.stream()
    .filter(o -> o instanceof String _)
    .count();
```

---

## Primitive Types in Patterns (Java 25, Preview)

> **Status in Java 25:** Preview (JEP 507, 3. Preview-Runde)

Erlaubt primitive Typen direkt in Patterns:

```java
// Bisher musste man auf Wrapper-Typen matchen:
Object obj = 42;
if (obj instanceof Integer i && i > 0) { ... }

// Mit Primitive Types in Patterns (Preview):
if (obj instanceof int i && i > 0) { ... }  // direkt auf 'int'

// In switch:
switch (someNumber) {
    case int i when i < 0  -> "negativ";
    case int i when i == 0 -> "null";
    case int i             -> "positiv: " + i;
}
```

> Da dies noch in Preview ist, muss die Datei mit `--enable-preview` kompiliert werden.

---

## Übersicht: Evolution der switch-Syntax

| Java-Version | Status       | Änderungen                                      |
|--------------|--------------|-------------------------------------------------|
| 11           | Nur Statement| kein Wert, Fallthrough                          |
| 12           | Preview      | erste Expressions, `case A, B ->`, `break value`|
| 13           | Preview      | `yield` ersetzt `break value`                   |
| 14           | **Final**    | Switch Expressions final                        |
| 17–20        | Preview      | Pattern Matching (Guard noch `&&`)              |
| 21           | **Final**    | Pattern Matching for switch, Guard = `when`     |
| 22           | **Final**    | Unnamed Variables & Patterns (`_`)              |
| 25           | Preview      | Primitive Types in Patterns                     |
