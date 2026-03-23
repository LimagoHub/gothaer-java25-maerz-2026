import java.util.List;
import java.util.stream.Gatherers;

/**
 * Simple Source Files und Instance Main Methods (Java 25, Preview).
 * JEP 512 – 5. Preview-Runde in Java 25.
 *
 * Kompilieren:
 *   javac --enable-preview --release 25 SimpleMainDemo.java
 *
 * Ausführen:
 *   java --enable-preview SimpleMainDemo
 *
 * ODER direkt (Single-File):
 *   java --enable-preview SimpleMainDemo.java
 *
 * ---------------------------------------------------------------------
 * Was ist neu?
 * ---------------------------------------------------------------------
 * Früher war exakt dieser Boilerplate für jedes Java-Programm nötig:
 *
 *   public class HelloWorld {
 *       public static void main(String[] args) {
 *           System.out.println("Hello World");
 *       }
 *   }
 *
 * Mit Simple Main Methods (Preview) reicht:
 *
 *   void main() {
 *       System.out.println("Hello World");
 *   }
 *
 * Die Klasse wird aus dem Dateinamen abgeleitet.
 * 'main()' darf Instanzmethode sein (kein 'static' nötig).
 * 'String[] args' ist optional.
 * Kein 'public' erforderlich.
 * ---------------------------------------------------------------------
 *
 * Die Aufrufpriorität wenn mehrere main-Varianten vorhanden sind:
 *   1. public static void main(String[] args)   – klassisch (höchste Prio)
 *   2. static void main(String[] args)
 *   3. static void main()
 *   4. void main(String[] args)
 *   5. void main()                              – neue Minimalform (niedrigste Prio)
 */
void main() {
    System.out.println("=== Simple Main Methods Demo ===\n");

    gruessen("Alice");
    gruessen("Bob");

    System.out.println("\nDie ersten 5 Quadratzahlen:");
    var zahlen = List.of(1, 2, 3, 4, 5);
    zahlen.stream()
          .map(n -> n * n)
          .forEach(System.out::println);

    System.out.println("\nGleitendes Fenster (windowSliding, Java 24):");
    zahlen.stream()
          .gather(Gatherers.windowSliding(3))
          .forEach(System.out::println);
}

// Hilfsmethode – gehört zur implizit erzeugten Klasse
void gruessen(String name) {
    System.out.println("Hallo, " + name + "!");
}
