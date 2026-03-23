package main;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.StructuredTaskScope;

public class LoomComparison {

    public static void main(String[] args) throws Exception {

        // ============================================================
        // VARIANTE 1: ExecutorService mit Virtual Threads
        // ============================================================
        //
        // + Gut geeignet als "Infrastruktur":
        //   - Bekanntes API (ExecutorService, Future)
        //   - Einfach, bestehenden Code von Plattform-Threads auf Virtual Threads umzustellen
        //   - Kein Preview-API (ExecutorService selbst ist final)
        //
        // - Nachteile:
        //   - Fehlerbehandlung pro Future (kein zentrales Fehlerhandling)
        //   - Tasks können theoretisch "entkommen", wenn man den Executor nicht sauber schließt
        //   - Abbruchlogik muss man selbst implementieren
        //
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            List<Callable<String>> tasks = List.of(
                    () -> {
                        Thread.sleep(300);
                        System.out.println("[Executor] Task 1 in " + Thread.currentThread());
                        return "Resultat 1";
                    },
                    () -> {
                        Thread.sleep(500);
                        System.out.println("[Executor] Task 2 in " + Thread.currentThread());
                        return "Resultat 2";
                    }
            );

            // Alle Tasks einreichen
            List<Future<String>> futures = tasks.stream()
                    .map(executor::submit)
                    .toList();

            // Ergebnisse einsammeln (Fehler pro Future behandeln)
            for (Future<String> f : futures) {
                try {
                    String result = f.get();   // wirft ExecutionException bei Fehlern
                    System.out.println("[Executor] Ergebnis: " + result);
                } catch (Exception e) {
                    // - Nachteil: Fehlerbehandlung hier pro Future
                    System.err.println("[Executor] Fehler in Task: " + e.getMessage());
                }
            }
        } // try-with-resources sorgt für shutdown() + awaitTermination()

        System.out.println("--------------------------------------------------");

        // ============================================================
        // VARIANTE 2: StructuredTaskScope (Structured Concurrency)
        // ============================================================
        //
        // + Gut geeignet als "Kontrollfluss":
        //   - Tasks gehören logisch zu einer übergeordneten Operation
        //   - Lebensdauer der Tasks ist an den Scope gekoppelt
        //   - Zentrales Fehlerhandling (join() wirft automatisch bei Fehler)
        //   - Implementiert direkt das Konzept "alle Teilaufgaben fertig oder abbrechen"
        //
        // - Nachteile:
        //   - In Java 25 finales Feature (JEP 505) – kein --enable-preview mehr nötig
        //   - Weniger als generelle Infrastruktur gedacht, eher für lokal begrenzte Operationen
        //
        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {

            // Aufgaben parallel starten (laufen automatisch in virtuellen Threads)
            StructuredTaskScope.Subtask<String> userTask = scope.fork(() -> {
                Thread.sleep(300);
                System.out.println("[Scope]   Task 1 in " + Thread.currentThread());
                return "User: Alice";
            });

            StructuredTaskScope.Subtask<String> ordersTask = scope.fork(() -> {
                Thread.sleep(500);
                System.out.println("[Scope]   Task 2 in " + Thread.currentThread());
                // Beispiel: kein Fehler hier – könnte aber Exception werfen
                return "Bestellungen: 3";
            });

            // Warten auf alle geforkten Tasks;
            // bei Fehler: andere Tasks werden abgebrochen, Exception wird geworfen.
            scope.join();

            // Ergebnisse sind hier sicher verfügbar (join() ist durch)
            String user   = userTask.get();
            String orders = ordersTask.get();

            System.out.println("[Scope]   Ergebnis kombiniert: " + user + " / " + orders);
        }

        // Gesamtfazit (informell):
        //
        // - ExecutorService:
        //     "Ich habe einen Pool/Mechanismus, der beliebige Aufgaben ausführt."
        //     → gut als Infrastrukturbaustein, gerade mit Virtual Threads.
        //
        // - StructuredTaskScope:
        //     "Ich habe eine konkrete Operation, die aus mehreren Teilaufgaben besteht,
        //      und alle sollen gemeinsam erfolgreich oder gemeinsam fehlschlagen."
        //     → gut für klar begrenzte, logisch zusammenhängende Nebenläufigkeit.
    }
}
