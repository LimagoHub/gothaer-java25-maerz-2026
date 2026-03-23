package main;
import java.lang.ScopedValue;
import java.util.concurrent.StructuredTaskScope;

public class ScopedValueStructuredScopeExample {

    // Scoped Values für Request-Kontext
    static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
    static final ScopedValue<String> USERNAME   = ScopedValue.newInstance();

    public static void main(String[] args) throws Exception {

        // Simulierter "Request-Entry-Point"
        ScopedValue
                .where(REQUEST_ID, "req-123")
                .where(USERNAME, "alice")
                .run(() -> handleRequest());
    }

    private static void handleRequest(){

        // Strukturierte Nebenläufigkeit für diesen Request
        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {

            var profileTask = scope.fork(() -> loadUserProfile());
            var ordersTask  = scope.fork(() -> loadOpenOrders());

            // Warten, bis alle Tasks fertig sind; wirft bei Fehler automatisch
            scope.join();

            // Ab hier sind alle Subtasks erfolgreich
            String profile = profileTask.get();
            String orders  = ordersTask.get();

            log("Antwort an Client: " + profile + " | " + orders);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String loadUserProfile() throws InterruptedException {
        log("Starte loadUserProfile()");
        Thread.sleep(300); // simuliert IO
        log("Beende loadUserProfile()");
        return "Profil[" + USERNAME.get() + "]";
    }

    private static String loadOpenOrders() throws InterruptedException {
        log("Starte loadOpenOrders()");
        Thread.sleep(500); // simuliert IO
        log("Beende loadOpenOrders()");
        return "Bestellungen[2 offen]";
    }

    private static void log(String message) {
        // Logging mit Request-Kontext aus Scoped Values
        System.out.println(
                "[" + REQUEST_ID.get() + "] " +
                        "user=" + USERNAME.get() + " | " +
                        message
        );
    }
}
