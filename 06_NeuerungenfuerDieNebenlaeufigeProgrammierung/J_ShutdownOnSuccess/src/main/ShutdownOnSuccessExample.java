package main;

import java.util.concurrent.StructuredTaskScope;

/**
 * Demonstration von Joiner.anySuccessfulResultOrThrow() (Java 25, JEP 505 final).
 * Entspricht dem früheren StructuredTaskScope.ShutdownOnSuccess aus Java 21 (Preview).
 *
 * anySuccessfulResultOrThrow:
 *  - Startet mehrere Tasks parallel
 *  - Sobald der ERSTE Task erfolgreich abschließt: alle anderen werden abgebrochen
 *  - join() gibt das erste erfolgreiche Ergebnis direkt zurück
 *
 * Typisches Einsatzszenario: Redundante Abfragen für Ausfallsicherheit
 *  - Dasselbe Ergebnis von mehreren Quellen anfragen
 *  - Die schnellste Antwort verwenden
 *  - Alle anderen Anfragen abbrechen (keine Ressourcenverschwendung)
 *
 * Vergleich mit awaitAllSuccessfulOrThrow:
 *  - awaitAllSuccessfulOrThrow: bricht alle ab, wenn EINER fehlschlägt
 *  - anySuccessfulResultOrThrow: bricht alle ab, wenn EINER erfolgreich ist
 */
public class ShutdownOnSuccessExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== anySuccessfulResultOrThrow: Schnellster Server gewinnt ===\n");

        // Szenario 1: Redundante Server-Abfrage
        demonstrateRedundantQuery();

        System.out.println("\n=== awaitAllSuccessfulOrThrow (Vergleich): Alle müssen erfolgreich sein ===\n");

        // Szenario 2: Vergleich mit ShutdownOnFailure
        demonstrateShutdownOnFailure();

        System.out.println("\n=== anySuccessfulResultOrThrow mit Fehlerbehandlung ===\n");

        // Szenario 3: ShutdownOnSuccess wenn alle fehlschlagen
        demonstrateAllFailing();
    }

    // -----------------------------------------------------------------------
    // Szenario 1: Redundante Server-Abfrage – erster Treffer gewinnt
    // -----------------------------------------------------------------------
    static void demonstrateRedundantQuery() throws Exception {
        System.out.println("Frage drei Server gleichzeitig ab – schnellster gewinnt...");

        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.<String>anySuccessfulResultOrThrow())) {

            // Alle drei Tasks starten – jeder bekommt einen virtuellen Thread
            scope.fork(() -> queryServer("Server-A", 300));
            scope.fork(() -> queryServer("Server-B", 100));  // dieser gewinnt
            scope.fork(() -> queryServer("Server-C", 200));

            // join() wartet bis mindestens einer fertig ist und gibt dessen Ergebnis zurück
            // (oder wirft FailedException, wenn alle fehlgeschlagen sind)
            String ergebnis = scope.join();
            System.out.println("Antwort erhalten: " + ergebnis);
            // Die anderen zwei Tasks wurden automatisch abgebrochen
        }
    }

    // Simuliert eine Server-Abfrage mit definierter Latenz
    static String queryServer(String name, long latenzMs) throws InterruptedException {
        Thread.sleep(latenzMs);
        System.out.println("  " + name + " antwortet nach " + latenzMs + "ms");
        return "Daten von " + name;
    }

    // -----------------------------------------------------------------------
    // Szenario 2: ShutdownOnFailure (zum Vergleich)
    // -----------------------------------------------------------------------
    static void demonstrateShutdownOnFailure() throws Exception {
        System.out.println("Lade User und Bestellungen parallel – beide müssen gelingen...");

        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {

            var user   = scope.fork(() -> ladeUser(42));
            var orders = scope.fork(() -> ladeBestellungen(42));

            scope.join();  // wirft FailedException wenn einer scheitert

            System.out.println("User: " + user.get());
            System.out.println("Bestellungen: " + orders.get());
        }
    }

    static String ladeUser(int id) throws InterruptedException {
        Thread.sleep(50);
        return "User#" + id;
    }

    static String ladeBestellungen(int id) throws InterruptedException {
        Thread.sleep(80);
        return "3 Bestellungen für User#" + id;
    }

    // -----------------------------------------------------------------------
    // Szenario 3: ShutdownOnSuccess wenn alle fehlschlagen
    // -----------------------------------------------------------------------
    static void demonstrateAllFailing() {
        System.out.println("Alle Server simulieren einen Ausfall...");

        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.<String>anySuccessfulResultOrThrow())) {

            scope.fork(() -> { throw new RuntimeException("Server-X ausgefallen"); });
            scope.fork(() -> { throw new RuntimeException("Server-Y ausgefallen"); });
            scope.fork(() -> { throw new RuntimeException("Server-Z ausgefallen"); });

            // join() wirft FailedException, wenn alle Tasks fehlgeschlagen sind
            String ergebnis = scope.join();
            System.out.println("Ergebnis: " + ergebnis);

        } catch (Exception e) {
            // Wenn kein Task erfolgreich war:
            System.out.println("Alle Server ausgefallen: " + e.getMessage());
        }
    }
}
