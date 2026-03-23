# ScopedValue + StructuredTaskScope – Best Practices

Dieses Kapitel zeigt, wie **Scoped Values** und **Structured Concurrency**  
(`StructuredTaskScope`) zusammen eingesetzt werden können – ein **Best-Practice-Muster**
für moderne Java-Anwendungen (z. B. Web-/Service-Backends).

> Gilt für **Java 25** – `ScopedValue` (JEP 506) und `StructuredTaskScope` (JEP 505) sind finale Features.

---

## 1. Zielbild

Wir wollen:

- pro Request einen **Request-Kontext** (z. B. `REQUEST_ID`, `USER`)
- mehrere **IO-lastige Aufgaben parallel** ausführen (Profil laden, Bestellungen laden, etc.)
- überall im Code sauber auf den Kontext zugreifen können – **ohne ThreadLocal**
- eine **strukturierte Fehlerbehandlung** (alles erfolgreich oder Fehler)

Dafür kombinieren wir:

- `ScopedValue` → Kontext (Request-scope, immutable, sauber begrenzt)  
- `StructuredTaskScope` → Nebenläufigkeit (virtuelle Threads, klarer Lebenszyklus)

---

## 2. Vollständiges Java-Beispiel

```java
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

    private static void handleRequest() throws Exception {

        // Strukturierte Nebenläufigkeit für diesen Request
        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {

            var profileTask = scope.fork(() -> loadUserProfile());
            var ordersTask  = scope.fork(() -> loadOpenOrders());

            // Warten, bis alle Tasks fertig oder abgebrochen sind;
            // wirft automatisch FailedException wenn einer fehlschlug
            scope.join();

            // Ab hier sind alle Subtasks erfolgreich
            String profile = profileTask.get();
            String orders  = ordersTask.get();

            log("Antwort an Client: " + profile + " | " + orders);
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
```

---

## 3. Kompilieren & Ausführen (Java 25)

```bash
javac --release 25 ScopedValueStructuredScopeExample.java
java  ScopedValueStructuredScopeExample
```

Typische Ausgabe (vereinfacht):

```text
[req-123] user=alice | Starte loadUserProfile()
[req-123] user=alice | Starte loadOpenOrders()
[req-123] user=alice | Beende loadUserProfile()
[req-123] user=alice | Beende loadOpenOrders()
[req-123] user=alice | Antwort an Client: Profil[alice] | Bestellungen[2 offen]
```

---

## 4. Was hier passiert (Schritt für Schritt)

### 4.1 Request-Kontext setzen

```java
ScopedValue
    .where(REQUEST_ID, "req-123")
    .where(USERNAME, "alice")
    .run(() -> handleRequest());
```

- Öffnet einen dynamischen Scope
- Bindet `REQUEST_ID` und `USERNAME` für **alle Aufrufe innerhalb von `handleRequest()`**
- Kein `set()` oder `remove()` nötig – automatische Bereinigung am Ende

---

### 4.2 Nebenläufige Aufgaben mit StructuredTaskScope

```java
try (var scope = StructuredTaskScope.open(
        StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {

    var profileTask = scope.fork(() -> loadUserProfile());
    var ordersTask  = scope.fork(() -> loadOpenOrders());

    scope.join();  // wirft FailedException wenn eine Aufgabe fehlschlägt

    String profile = profileTask.get();
    String orders  = ordersTask.get();
}
```

- Jeder `fork()` startet einen **virtuellen Thread**
- `awaitAllSuccessfulOrThrow` bedeutet:
  - wenn **eine** Aufgabe fehlschlägt → andere werden abgebrochen
  - `join()` wirft automatisch eine `FailedException`
- Ergebnisse sind nach `join()` sicher abrufbar

---

### 4.3 Nutzung von Scoped Values in tieferen Methoden

```java
private static void log(String message) {
    System.out.println(
            "[" + REQUEST_ID.get() + "] " +
            "user=" + USERNAME.get() + " | " +
            message
    );
}
```

- `log()` bekommt keinen Request-Kontext als Parameter
- trotzdem kann es sauber auf `REQUEST_ID` und `USERNAME` zugreifen
- alle Aufrufe laufen **innerhalb des Scopes**, den wir in `main()` gesetzt haben
- funktioniert auch in den **virtuellen Threads** der geforkten Tasks

---

## 5. Best Practices: ScopedValue + StructuredTaskScope

### ✅ Do

- pro Request/Operation einen klaren Scope verwenden
- `ScopedValue` für:
  - `REQUEST_ID`
  - `USER` / Security-Principal
  - Locale / Region
  - Logging-/Tracing-Kontext
- `StructuredTaskScope` für logisch zusammengehörige, parallele Aufgaben
- Logging/Tracing über Hilfsmethoden, die `ScopedValue` lesen

---

### ❌ Don’t

- ScopedValue außerhalb von `where(...).run(...)` lesen
- `ScopedValue` als Ersatz für globale mutable Zustände verwenden
- `ScopedValue` in klassischen Thread-Pools verwenden, deren Threads
  **außerhalb** des Scopes/Request-Lebenszyklus leben
- lange laufende, „entfliehende“ Tasks aus einem Request-Scope starten

---

## 6. Vorteile dieser Kombination

- **klare Struktur**: Ein Request = ein Kontext = ein Scope
- **keine ThreadLocal-Leaks**: Kontext endet zuverlässig am Ende des Scopes
- **virtuelle Threads** integriert: `StructuredTaskScope` nutzt sie automatisch
- **bessere Testbarkeit**:
  - Tests können `ScopedValue.where(...).run(...)` nutzen
  - kein globaler versteckter Zustand
- **saubere Trennung** von:
  - Nebenläufigkeit (Scope, `fork()`, `join()`)
  - Kontext (Scoped Values)
  - Fachlogik (z. B. `loadUserProfile`)

---

## 7. Merksatz

> **Scoped Values transportieren Kontext,
> StructuredTaskScope strukturiert Nebenläufigkeit.**
>
> Zusammen eingesetzt sind sie das „moderne Duo”
> für Request-Handling im Zeitalter von Virtual Threads (Java 25+).
