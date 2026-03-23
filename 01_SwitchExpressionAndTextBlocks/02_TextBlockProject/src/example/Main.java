package example;

public class Main {
    public static void main(String[] args) {

        // ---------------------------------------------------------------
        // Text Blocks (final seit Java 15)
        // ---------------------------------------------------------------
        // Mehrzeilige Strings ohne Escape-Chaos, eingeführt als Preview
        // in Java 13 und seit Java 15 offiziell Teil der Sprache.
        String html = """
                <html>
                    <body>Hello World</body>
                </html>
                """;

        System.out.println(html);

        // ---------------------------------------------------------------
        // Zeilenfortsetzung mit \ (backslash line continuation, Java 14)
        // ---------------------------------------------------------------
        // Ein abschließender \ verhindert den Zeilenumbruch im String.
        // Der resultierende String lautet "Hello World" (eine Zeile).
        String text = """
                Hello \
                World
                """;

        System.out.println(text);

        // ---------------------------------------------------------------
        // Variable Einbettung: String.formatted() statt String Templates
        // ---------------------------------------------------------------
        // String Templates (STR-Prozessor, JEP 430) wurden in Java 21
        // als Preview eingeführt, aber in Java 23 wieder zurückgezogen
        // (JEP 465 Withdrawal). Sie sind in Java 25 NICHT verfügbar.
        //
        // Die sauberste Alternative ist String.formatted():
        String name = "Lisa";
        String message = """
                Hello %s,
                Welcome to Java 25!
                """.formatted(name);

        System.out.println(message);

        // ---------------------------------------------------------------
        // Alternative: klassisches String.format() oder Concatenation
        // ---------------------------------------------------------------
        // String.format() ist identisch zu formatted(), aber als
        // statische Methode aufgerufen:
        String greeting = String.format("Hallo %s, du verwendest Java %d!", name, 25);
        System.out.println(greeting);
    }
}
