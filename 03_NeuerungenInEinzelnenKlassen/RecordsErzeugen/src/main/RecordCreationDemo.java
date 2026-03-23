package main;
public class RecordCreationDemo {

    public static void main(String[] args) {

        // 1) Einfachste Erzeugung eines Records
        Person p1 = new Person("Alice", 30);
        System.out.println("p1 = " + p1);

        // 2) Erzeugung mit kompakter Validierung im Konstruktor
        Person p2 = new Person("Bob", 25);
        System.out.println("p2 = " + p2);

        // 3) Erzeugung über zusätzlichen (überladenen) Konstruktor
        Person p3 = new Person("Carol");
        System.out.println("p3 = " + p3);

        // 4) Erzeugung über statische Factory-Methode (Best Practice)
        Person p4 = Person.of("Dave", 40);
        System.out.println("p4 = " + p4);

        // 5) Erzeugung über zweite Factory-Methode
        Person p5 = Person.unknownAge("Eve");
        System.out.println("p5 = " + p5);

        // 6) Nutzung abgeleiteter Methoden
        System.out.println("Full name: " + p4.fullName());

        // 7) Record als Interface-Implementierung
        Identifiable user = new User("u-1", "Frank");
        System.out.println("User id = " + user.id());

        // 8) Record als Rückgabewert
        Result result = doWork(true);
        System.out.println("Result = " + result);

        // 9) Pattern Matching mit Record-Dekonstruktion
        Object obj = new Person("Grace", 18);

        if (obj instanceof Person(String name, int age)) {
            System.out.println("Pattern Matching: " + name + " is " + age);
        }
    }

    // ===== Records & Interfaces =====

    /*
     * Einfacher Record mit:
     * - kompaktem Konstruktor (Validierung)
     * - zusätzlichem Konstruktor
     * - Factory-Methoden
     * - zusätzlicher Logik
     */
    public record Person(String name, int age) {

        // 2) Kompakter Konstruktor (Validierung)
        public Person {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name must not be blank");
            }
            if (age < -1) {
                throw new IllegalArgumentException("age must be >= -1");
            }
        }

        // 3) Zusätzlicher Konstruktor (Delegation ist Pflicht!)
        public Person(String name) {
            this(name, -1); // Delegation an kanonischen Konstruktor
        }

        // 4) Statische Factory-Methode
        public static Person of(String name, int age) {
            return new Person(name, age);
        }

        // 5) Zweite Factory-Methode mit sprechendem Namen
        public static Person unknownAge(String name) {
            return new Person(name, -1);
        }

        // 6) Abgeleitete Methode (kein zusätzlicher Zustand!)
        public String fullName() {
            return name;
        }
    }

    /*
     * Records können Interfaces implementieren
     */
    interface Identifiable {
        String id();
    }

    public record User(String id, String name) implements Identifiable {}

    /*
     * Typischer Einsatz als Rückgabewert (DTO)
     */
    public record Result(boolean success, String message) {}

    static Result doWork(boolean ok) {
        return ok
                ? new Result(true, "Alles OK")
                : new Result(false, "Fehler");
    }
}
