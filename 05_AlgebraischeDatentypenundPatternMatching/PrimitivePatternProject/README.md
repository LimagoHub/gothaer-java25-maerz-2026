# Primitive Types in Patterns (Java 25, Preview)

> **JEP 507** – Primitive Types in Patterns, instanceof und switch (3. Preview in Java 25)

## Kompilieren und Ausführen

```bash
javac --enable-preview --release 25 -cp . src/example/PrimitivePatternDemo.java
java  --enable-preview -cp src example.PrimitivePatternDemo
```

## Was ist das?

Erlaubt primitive Typen (`int`, `long`, `double`, `boolean`, ...) direkt in Pattern Matching.

### Vorher (Java 21 – Wrapper nötig):
```java
Object obj = 42;
if (obj instanceof Integer i && i > 0) { ... }  // Wrapper-Typ Integer
```

### Nachher (Java 25 Preview):
```java
Object obj = 42;
if (obj instanceof int i && i > 0) { ... }  // primitiver Typ int direkt
```

## Neue switch-Möglichkeiten

```java
switch (obj) {
    case int i when i < 0  -> "negativ";
    case int i when i == 0 -> "null";
    case int i             -> "positiv";
    case double d          -> "Kommazahl";
    case String s          -> "Text";
}
```

## Beispiele im Projekt

- `instanceof` mit `int`
- `switch` über `Object` mit primitiven Patterns
- `switch` direkt über `int` mit erweiterten Guards
- Primitive Types in Record-Dekonstruktion
- `boolean` Pattern
