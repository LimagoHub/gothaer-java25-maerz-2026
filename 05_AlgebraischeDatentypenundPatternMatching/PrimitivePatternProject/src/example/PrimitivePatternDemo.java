package example;

/**
 * Primitive Types in Patterns (Java 25, 3. Preview).
 * JEP 507 – 3. Preview-Runde in Java 25.
 *
 * Kompilieren:
 *   javac --enable-preview --release 25 -cp . PrimitivePatternDemo.java
 *
 * Ausführen:
 *   java --enable-preview -cp . example.PrimitivePatternDemo
 *
 * ---------------------------------------------------------------------
 * Was ist neu?
 * ---------------------------------------------------------------------
 * Bisher konnten Pattern Matching und switch nur Referenztypen verarbeiten.
 * Primitive (int, long, double, boolean ...) mussten erst in Wrapper-Typen
 * umgewandelt werden:
 *
 *   // Java 21:
 *   Object obj = 42;
 *   if (obj instanceof Integer i && i > 0) { ... }   // Wrapper nötig
 *
 * Mit Primitive Types in Patterns (Preview):
 *
 *   // Java 25 Preview:
 *   if (obj instanceof int i && i > 0) { ... }       // direkt auf 'int'
 *
 * Außerdem neu: switch über primitive Typen wie byte, short, int, long,
 * float, double, boolean mit Pattern-Syntax.
 * ---------------------------------------------------------------------
 */
public class PrimitivePatternDemo {

    public static void main(String[] args) {
        System.out.println("=== Primitive Types in Patterns (Preview) ===\n");

        // ---------------------------------------------------------------
        // 1. instanceof mit primitivem Typ
        // ---------------------------------------------------------------
        System.out.println("--- 1. instanceof mit int ---");

        Object obj1 = 42;
        Object obj2 = -7;
        Object obj3 = "kein int";

        // Bisher (Java 21):
        if (obj1 instanceof Integer i && i > 0) {
            System.out.println("Java 21-Stil: positiver Integer " + i);
        }

        // Preview (Java 25): direkt auf primitiven Typ matchen
        if (obj1 instanceof int i && i > 0) {
            System.out.println("Java 25-Preview: positives int " + i);
        }

        // ---------------------------------------------------------------
        // 2. switch mit primitivem Typ – Type Patterns
        // ---------------------------------------------------------------
        System.out.println("\n--- 2. switch über Object mit int-Pattern ---");

        for (Object val : new Object[]{42, -7, 3.14, "Text", 100L}) {
            String beschreibung = switch (val) {
                case int i when i > 0  -> "Positives int: " + i;
                case int i when i < 0  -> "Negatives int: " + i;
                case int i             -> "Null int";
                case double d          -> "Double: " + d;
                case long l            -> "Long: " + l;
                case String s          -> "String: " + s;
                default                -> "Sonstiges";
            };
            System.out.println(val + " → " + beschreibung);
        }

        // ---------------------------------------------------------------
        // 3. switch direkt über primitiven Typ
        // ---------------------------------------------------------------
        System.out.println("\n--- 3. switch direkt über int (erweiterte Guards) ---");

        for (int zahl : new int[]{-5, 0, 1, 7, 100, 1000}) {
            String kategorie = switch (zahl) {
                case int i when i < 0     -> "negativ";
                case 0                    -> "null";
                case int i when i < 10    -> "einstellig";
                case int i when i < 100   -> "zweistellig";
                case int i when i < 1000  -> "dreistellig";
                default                   -> "vierstellig oder mehr";
            };
            System.out.printf("%5d → %s%n", zahl, kategorie);
        }

        // ---------------------------------------------------------------
        // 4. Primitive Patterns in Record-Dekonstruktion
        // ---------------------------------------------------------------
        System.out.println("\n--- 4. Primitive Types in Record Patterns ---");

        record Messung(String name, double wert) {}

        Object[] messungen = {
            new Messung("Temperatur", 22.5),
            new Messung("Druck", -0.1),
            new Messung("Geschwindigkeit", 0.0),
            "kein Messobjekt"
        };

        for (Object m : messungen) {
            String bewertung = switch (m) {
                case Messung(String n, double w) when w > 0  -> n + "=" + w + " (positiv)";
                case Messung(String n, double w) when w < 0  -> n + "=" + w + " (negativ!)";
                case Messung(String n, double w)             -> n + "=0 (null)";
                default                                      -> "Unbekanntes Objekt";
            };
            System.out.println(bewertung);
        }

        // ---------------------------------------------------------------
        // 5. boolean Pattern
        // ---------------------------------------------------------------
        System.out.println("\n--- 5. boolean Pattern ---");

        Object flag1 = true;
        Object flag2 = false;
        Object flag3 = "kein boolean";

        for (Object o : new Object[]{flag1, flag2, flag3}) {
            String result = switch (o) {
                case boolean b when b  -> "ist true";
                case boolean b        -> "ist false";
                default               -> "ist kein boolean";
            };
            System.out.println(o + " → " + result);
        }
    }
}
