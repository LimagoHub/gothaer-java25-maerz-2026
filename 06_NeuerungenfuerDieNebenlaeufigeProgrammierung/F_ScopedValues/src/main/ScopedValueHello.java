package main;

import java.lang.ScopedValue;

public class ScopedValueHello {

    // 1. ScopedValue deklarieren
    static final ScopedValue<String> USER =
            ScopedValue.newInstance();

    public static void main(String[] args) {

        // 2. ScopedValue binden und Code im Scope ausführen
        ScopedValue.where(USER, "Alice").run(() -> {

            // 3. ScopedValue lesen
            sayHello();

        });

        // 4. Außerhalb des Scopes NICHT verfügbar
        // USER.get(); // -> IllegalStateException
    }

    private static void sayHello() {
        // ScopedValue ist hier verfügbar, obwohl es kein Parameter ist
        System.out.println("Hallo " + USER.get());

    }
}
