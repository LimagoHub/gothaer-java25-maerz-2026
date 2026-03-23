package main;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadsExecutorExample {

    public static void main(String[] args) {

        // Executor, der für jede Aufgabe einen virtuellen Thread erzeugt
        try (ExecutorService executor =
                     Executors.newVirtualThreadPerTaskExecutor()) {

            for (int i = 0; i < 3; i++) {
                int id = i; // wichtig: effectively final

                executor.submit(() -> {
                    System.out.println(
                            "Task " + id +
                                    " läuft in " + Thread.currentThread()
                    );
                });
            }

            // Executor wird am Ende des try-Blocks automatisch geschlossen
        }

        System.out.println("Main-Methode beendet");
    }
}
