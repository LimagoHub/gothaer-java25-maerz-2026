# Simple Main Methods (Java 25, Preview)

> **JEP 512** – Simple Source Files and Instance Main Methods (5. Preview in Java 25)

## Kompilieren und Ausführen

```bash
# Als Einzeldatei (empfohlen für dieses Demo):
java --enable-preview --source 25 SimpleMainDemo.java

# Oder klassisch:
javac --enable-preview --release 25 SimpleMainDemo.java
java --enable-preview SimpleMainDemo
```

## Was ist das?

Vereinfachte Einstiegspunkte für Java-Programme – kein `public static void main(String[] args)` mehr nötig.

### Vorher (klassisch):
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello!");
    }
}
```

### Nachher (Java 25 Preview):
```java
void main() {
    System.out.println("Hello!");
}
```

## Aufrufpriorität

Falls mehrere Varianten vorhanden sind:

| Priorität | Signatur |
|-----------|---------|
| 1 (höchste) | `public static void main(String[] args)` |
| 2 | `static void main(String[] args)` |
| 3 | `static void main()` |
| 4 | `void main(String[] args)` |
| 5 (niedrigste) | `void main()` |

## Zielgruppe

Primär für Einsteiger und kurze Skripte. In Produktionscode weiterhin die klassische Form.
