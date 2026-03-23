package main;

public class Main {
    public static void main(String[] args) {

        // Sealed Class: nur NatPerson und JurPerson dürfen Person implementieren
        Person p1 = new NatPerson("Alice", "Musterstraße 1");
        Person p2 = new JurPerson("ACME GmbH", "DE123456789");

        beschreiben(p1);
        beschreiben(p2);
    }

    // Pattern Matching for switch: Compiler prüft Vollständigkeit automatisch,
    // da Person sealed ist – kein 'default' nötig.
    static void beschreiben(Person p) {
        String beschreibung = switch (p) {
            case NatPerson n -> "Natürliche Person: " + n.name() + ", Adresse: " + n.adresse();
            case JurPerson j -> "Juristische Person: " + j.firmenname() + ", USt-ID: " + j.ustId();
        };
        System.out.println(beschreibung);
    }
}

// Sealed Interface: schließt die Typhierarchie – nur die hier genannten Typen
// dürfen Person implementieren. Records können keine Klassen erweitern (extends),
// wohl aber Interfaces implementieren (implements).
sealed interface Person permits NatPerson, JurPerson {}

// Records sind implizit 'final' – erfüllen damit die sealed-Anforderung.
// Sie implementieren das sealed Interface statt es zu erweitern.
record NatPerson(String name, String adresse) implements Person {}

record JurPerson(String firmenname, String ustId) implements Person {}
