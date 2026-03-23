package main;
import java.util.concurrent.StructuredTaskScope;

public class StructuredScopeErrorExample {

    public static void main(String[] args) {
        try {
            loadUserAndOrders();
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Daten: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private static void loadUserAndOrders() throws Exception {

        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {

            // Zwei Tasks: einer ok, einer wirft absichtlich eine Exception
            var userTask = scope.fork(() -> loadUser());
            var ordersTask = scope.fork(() -> loadOrdersWithError());

            // Warten, bis beide Tasks fertig sind (oder abgebrochen wurden).
            // WIRKLICHER Fehlerpunkt: join() wirft automatisch FailedException,
            // wenn einer der Tasks fehlgeschlagen ist.
            scope.join();

            // Wenn wir hier ankommen, sind BEIDE Tasks erfolgreich.
            String user   = userTask.get();
            String orders = ordersTask.get();

            System.out.println("User:   " + user);
            System.out.println("Orders: " + orders);
        }
    }

    private static String loadUser() throws InterruptedException {
        System.out.println("loadUser() gestartet in " + Thread.currentThread());
        Thread.sleep(500); // simuliert IO
        System.out.println("loadUser() fertig");
        return "Alice";
    }

    private static String loadOrdersWithError() throws InterruptedException {
        System.out.println("loadOrdersWithError() gestartet in " + Thread.currentThread());
        Thread.sleep(300); // simuliert IO
        System.out.println("loadOrdersWithError() wirft Exception");
        throw new RuntimeException("Order-Service nicht erreichbar");
    }
}
