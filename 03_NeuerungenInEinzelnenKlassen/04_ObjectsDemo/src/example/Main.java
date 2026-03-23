package example;

import java.util.Objects;
/*

    Debug-Ausgaben, bei denen du die echte Objektidentitaet sehen willst,
    unabhängig von ueberschriebenem toString().

    Logging von Objekten, deren toString() stark formatiert oder „geschoent“ ist.
 */
public class Main {
    public static void main(String[] args) {
        Person p = new Person("Alice");

        System.out.println(p.toString());
        // Person[name=Alice]

        System.out.println(Objects.toIdentityString(p));
        // z.B. my.pkg.Person@5e2de80c (unabhängig von Record-toString)
    }
    record Person(String name) {}
}
