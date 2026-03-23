package main;

import java.util.concurrent.StructuredTaskScope;


public class StructuredScopeExample {

    public static void main(String[] args) throws Exception {

        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {

            // Zwei Aufgaben parallel ausführen
            var userTask = scope.fork(() -> loadUser());
            var orderTask = scope.fork(() -> loadOrders());

            // Auf alle Tasks warten; wirft automatisch bei Fehler (FailedException)
            scope.join();

            // Ergebnisse abholen (get() blockiert hier nicht mehr, da join() schon fertig ist)
            String user = userTask.get();
            String orders = orderTask.get();

            System.out.println("Resultat:");
            System.out.println("User:   " + user);
            System.out.println("Orders: " + orders);
        } // hier wird sichergestellt: alle Threads des Scopes sind beendet
    }

    private static String loadUser() throws InterruptedException {
        Thread.sleep(500); // simuliert langsame IO-Operation
        System.out.println("loadUser() läuft in " + Thread.currentThread());
        return "Alice";
    }

    private static String loadOrders() throws InterruptedException {
        Thread.sleep(800); // simuliert langsame IO-Operation
        System.out.println("loadOrders() läuft in " + Thread.currentThread());
        return "3 offene Bestellungen";
    }
}
