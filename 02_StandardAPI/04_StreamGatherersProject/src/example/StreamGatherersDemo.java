package example;

import java.util.List;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

/**
 * Demonstration der Stream Gatherers API (Java 24, JEP 485).
 *
 * Gatherers ermöglichen benutzerdefinierte Zwischenoperationen in Stream-Pipelines.
 * Sie schließen die letzte große Lücke der Stream-API: Vor Java 24 waren nur
 * die eingebauten Intermediate Operations (filter, map, flatMap, ...) verfügbar.
 *
 * Neue Methode: Stream.gather(Gatherer) – eine Intermediate Operation
 * Neue Klasse:  java.util.stream.Gatherers – eingebaute Implementierungen
 */
public class StreamGatherersDemo {

    public static void main(String[] args) {
        System.out.println("=== 1. windowFixed – Fenster fester Größe ===");
        demonstrateWindowFixed();

        System.out.println("\n=== 2. windowSliding – Gleitendes Fenster ===");
        demonstrateWindowSliding();

        System.out.println("\n=== 3. scan – Laufende Akkumulation ===");
        demonstrateScan();

        System.out.println("\n=== 4. fold – Zustandsbehaftete Reduktion ===");
        demonstrateFold();

        System.out.println("\n=== 5. Eigener Gatherer: everyNth ===");
        demonstrateCustomGatherer();

        System.out.println("\n=== 6. Kombination mit anderen Stream-Operationen ===");
        demonstrateCombination();
    }

    // -----------------------------------------------------------------------
    // 1. windowFixed(n) – teilt Stream in nicht-überlappende Gruppen der Größe n
    // -----------------------------------------------------------------------
    static void demonstrateWindowFixed() {
        List<List<Integer>> result = Stream.of(1, 2, 3, 4, 5, 6, 7)
                .gather(Gatherers.windowFixed(3))  // Gruppen: [1,2,3], [4,5,6], [7]
                .toList();

        System.out.println(result);
        // → [[1, 2, 3], [4, 5, 6], [7]]
        // Das letzte Fenster kann kürzer sein, wenn die Elemente nicht aufgehen
    }

    // -----------------------------------------------------------------------
    // 2. windowSliding(n) – überlappende Fenster der Größe n
    // -----------------------------------------------------------------------
    static void demonstrateWindowSliding() {
        List<List<Integer>> result = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.windowSliding(3))  // jedes Fenster verschiebt sich um 1
                .toList();

        System.out.println(result);
        // → [[1, 2, 3], [2, 3, 4], [3, 4, 5]]
        // Nützlich für: Moving Average, Pattern-Erkennung in Sequenzen
    }

    // -----------------------------------------------------------------------
    // 3. scan(initial, combiner) – laufende Akkumulation (alle Zwischenwerte)
    //    Wie reduce(), aber gibt alle Zwischenergebnisse aus, nicht nur das finale
    // -----------------------------------------------------------------------
    static void demonstrateScan() {
        // Laufende Summe:
        List<Integer> laufendeSumme = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.scan(() -> 0, Integer::sum))
                .toList();

        System.out.println("Laufende Summe: " + laufendeSumme);
        // → [1, 3, 6, 10, 15]

        // Laufendes Maximum:
        List<Integer> laufendesMax = Stream.of(3, 1, 4, 1, 5, 9, 2, 6)
                .gather(Gatherers.scan(() -> Integer.MIN_VALUE, Math::max))
                .toList();

        System.out.println("Laufendes Maximum: " + laufendesMax);
        // → [3, 3, 4, 4, 5, 9, 9, 9]
    }

    // -----------------------------------------------------------------------
    // 4. fold(initial, combiner) – ähnlich wie reduce, aber als Intermediate Op.
    //    Gibt eine Liste mit genau einem Element zurück (dem Endwert)
    // -----------------------------------------------------------------------
    static void demonstrateFold() {
        List<String> result = Stream.of("Hallo", " ", "Java", " ", "25")
                .gather(Gatherers.fold(() -> "", String::concat))
                .toList();

        System.out.println(result);
        // → ["Hallo Java 25"]
    }

    // -----------------------------------------------------------------------
    // 5. Eigener Gatherer – mit Gatherer.ofSequential()
    //    Beispiel: jedes n-te Element durchlassen
    // -----------------------------------------------------------------------
    static void demonstrateCustomGatherer() {
        List<Integer> result = Stream.iterate(1, n -> n + 1)
                .limit(12)
                .gather(everyNth(3))  // nur 3, 6, 9, 12
                .toList();

        System.out.println("Jedes 3. Element: " + result);
        // → [3, 6, 9, 12]
    }

    /**
     * Eigener Gatherer: lässt nur jedes n-te Element durch.
     *
     * Gatherer.ofSequential() erwartet:
     *  - Initializer: erzeugt den Zustand (hier: int[]{counter})
     *  - Integrator: verarbeitet ein Element, gibt true zurück wenn weiter
     */
    static <T> Gatherer<T, ?, T> everyNth(int n) {
        return Gatherer.ofSequential(
                () -> new int[]{0},                           // Zustand: Zähler
                (state, element, downstream) -> {
                    state[0]++;
                    if (state[0] % n == 0) {                  // jedes n-te Element
                        downstream.push(element);             // weiterreichen
                    }
                    return true;                              // weiter verarbeiten
                }
        );
    }

    // -----------------------------------------------------------------------
    // 6. Kombination: Gatherer + andere Stream-Operationen
    // -----------------------------------------------------------------------
    static void demonstrateCombination() {
        // Sliding-Window für Moving Average:
        List<Double> movingAverage = Stream.of(10, 20, 30, 40, 50, 60, 70, 80, 90, 100)
                .gather(Gatherers.windowSliding(3))           // Fenster der Größe 3
                .filter(window -> window.size() == 3)         // unvollständige Fenster am Ende ausschließen
                .map(window -> window.stream()
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0))                           // Durchschnitt pro Fenster
                .toList();

        System.out.println("Moving Average (Fenster=3): " + movingAverage);
        // → [20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0]

        // Batched Processing – Gruppen verarbeiten:
        long verarbeitet = Stream.of("a", "b", "c", "d", "e", "f", "g")
                .gather(Gatherers.windowFixed(3))             // in 3er-Gruppen aufteilen
                .peek(batch -> System.out.println("Verarbeite Batch: " + batch))
                .count();

        System.out.println("Verarbeitete Batches: " + verarbeitet);
    }
}
