package example;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Demonstration von Unnamed Variables & Patterns (Java 22, final).
 * JEP 456 – finalisiert in Java 22.
 *
 * Der Unterstrich '_' kennzeichnet eine Variable oder Pattern-Komponente,
 * deren Wert nicht benötigt wird. Das verbessert die Lesbarkeit und
 * kommuniziert die Absicht explizit an den Leser.
 */
public class UnnamedVariablesDemo {

    // Records für die Beispiele
    record Point(int x, int y) {}
    record Point3D(int x, int y, int z) {}
    record Person(String name, String email, int age) {}

    public static void main(String[] args) {
        System.out.println("=== 1. Unnamed Variable in catch ===");
        demonstrateCatch();

        System.out.println("\n=== 2. Unnamed Pattern in instanceof ===");
        demonstrateInstanceof();

        System.out.println("\n=== 3. Unnamed Pattern in switch ===");
        demonstrateSwitch();

        System.out.println("\n=== 4. Unnamed Record-Komponenten ===");
        demonstrateRecordPattern();

        System.out.println("\n=== 5. Unnamed Variable in for-Schleife ===");
        demonstrateForLoop();
    }

    // -----------------------------------------------------------------------
    // 1. Unnamed Variable in catch-Klauseln
    // -----------------------------------------------------------------------
    static void demonstrateCatch() {
        // Klassisch: 'ignored' oder 'e' – aber nie gelesen
        try {
            Integer.parseInt("kein Integer");
        } catch (NumberFormatException ignored) {
            // Variable 'ignored' macht deutlich: Exception absichtlich ignoriert
            System.out.println("Kein gültiger Integer");
        }

        // Java 22: '_' macht die Absicht noch klarer
        try {
            Integer.parseInt("auch kein Integer");
        } catch (NumberFormatException _) {
            // '_' = diese Exception wird bewusst ignoriert
            System.out.println("Kein gültiger Integer (mit _)");
        }

        // Mehrere catch-Klauseln mit _:
        try {
            riskyOperation();
        } catch (IOException _) {
            System.out.println("IO-Fehler (ignoriert)");
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();  // Interrupt-Status wiederherstellen
        }
    }

    static void riskyOperation() throws IOException, InterruptedException {}

    // -----------------------------------------------------------------------
    // 2. Unnamed Pattern in instanceof
    // -----------------------------------------------------------------------
    static void demonstrateInstanceof() {
        List<Object> items = List.of("Hallo", 42, 3.14, "Welt", true);

        // Typ prüfen, Wert aber nicht benötigen:
        long stringCount = items.stream()
                .filter(o -> o instanceof String _)   // _ = nur Typ prüfen, kein cast
                .count();

        System.out.println("Anzahl Strings: " + stringCount);  // 2

        // Vergleich: Mit gebundener Variable (wenn Wert benötigt wird):
        items.stream()
                .filter(o -> o instanceof String s && !s.isBlank())  // s wird genutzt
                .forEach(o -> System.out.println("String: " + o));
    }

    // -----------------------------------------------------------------------
    // 3. Unnamed Pattern in switch
    // -----------------------------------------------------------------------
    static void demonstrateSwitch() {
        List<Object> values = List.of("Text", 42, 3.14, true, List.of(1, 2, 3));

        for (Object value : values) {
            String kategorie = switch (value) {
                case String _   -> "Zeichenkette";      // Typ wichtig, Wert nicht
                case Integer _  -> "Ganzzahl";
                case Double _   -> "Kommazahl";
                case Boolean _  -> "Wahrheitswert";
                default         -> "Sonstiges: " + value.getClass().getSimpleName();
            };
            System.out.println(value + " → " + kategorie);
        }
    }

    // -----------------------------------------------------------------------
    // 4. Unnamed Record-Komponenten
    // -----------------------------------------------------------------------
    static void demonstrateRecordPattern() {
        List<Point3D> points = List.of(
                new Point3D(1, 2, 3),
                new Point3D(4, 5, 6),
                new Point3D(7, 8, 9)
        );

        System.out.println("Nur X-Koordinaten:");
        for (Point3D p : points) {
            // Nur x extrahieren, y und z ignorieren:
            if (p instanceof Point3D(int x, _, _)) {
                System.out.println("  x=" + x);
            }
        }

        // In switch mit gemischten Typen:
        List<Object> shapes = List.of(
                new Point(3, 4),
                new Point3D(1, 2, 3),
                "kein Shape"
        );

        for (Object s : shapes) {
            String desc = switch (s) {
                case Point(int x, int y)    -> "2D-Punkt: x=%d, y=%d".formatted(x, y);
                case Point3D(int x, _, _)   -> "3D-Punkt: x=%d".formatted(x);  // nur x interessant
                default                     -> "Unbekannt";
            };
            System.out.println(desc);
        }
    }

    // -----------------------------------------------------------------------
    // 5. Unnamed Variable in for-Schleife (wenn Index nicht benötigt)
    // -----------------------------------------------------------------------
    static void demonstrateForLoop() {
        int count = 0;
        List<String> names = List.of("Alice", "Bob", "Charlie", "Dave");

        // Schleife, wenn Index nicht gebraucht wird:
        for (String _ : names) {
            count++;  // wir zählen nur, der Wert ist egal
        }

        System.out.println("Anzahl: " + count);  // 4

        // Klassische for-Schleife (wenn Variable nicht benötigt):
        int sum = 0;
        for (int _ = 0; _ < 5; _++) {  // Hinweis: so eher unüblich
            sum += (int) (Math.random() * 10);
        }
        System.out.println("Summe zufälliger Zahlen: " + sum);

        // Typischer Anwendungsfall: Ergebnis einer Methode verwerfen
        Map<String, Integer> scoreMap = Map.of("Alice", 90, "Bob", 85);
        for (var _ : scoreMap.entrySet()) {
            // Nur die Anzahl der Einträge zählen, Inhalt irrelevant
            count++;
        }
    }
}
