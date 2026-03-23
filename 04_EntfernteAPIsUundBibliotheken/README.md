# Entfernte APIs und Bibliotheken (Java 17–25)

Dieses Kapitel behandelt APIs, die zwischen Java 17 und Java 25 **deprecated** wurden
oder **entfernt** wurden, und wie man damit umgeht.

---

## 1. Überblick: Entfernte APIs

| API / Feature        | Deprecated  | Entfernt    | Ersatz                        |
|---------------------|-------------|-------------|-------------------------------|
| `finalize()`        | Java 9      | Java 21     | `try-with-resources`, `Cleaner` |
| `SecurityManager`   | Java 17     | Java 24     | externe Security-Frameworks   |
| Applet API          | Java 9      | Java 23     | moderne Web-Technologien      |
| `Thread.stop()` etc.| früh        | Java 20     | `interrupt()`, kooperativ     |
| RMI Activation      | Java 15     | Java 21     | moderne Remoting-Lösungen     |

---

## 2. Finalization (entfernt in Java 21)

### Was war Finalization?

```java
// Nie mehr verwenden!
@Override
protected void finalize() throws Throwable {
    closeConnection();  // ❌ unvorhersehbar, langsam, fehlerhaft
}
```

Probleme:

- unvorhersehbarer Aufrufzeitpunkt (oder gar kein Aufruf)
- schlechte Performance des GC
- Sicherheitsprobleme (Objekt kann sich "wiederbeleben")
- Ressourcen-Leaks

### Ersatz: try-with-resources (empfohlen)

```java
// ✅ Ressourcen werden garantiert freigegeben:
try (var conn = getConnection();
     var stmt = conn.prepareStatement(sql)) {
    return stmt.executeQuery();
}   // conn und stmt werden hier automatisch geschlossen
```

### Ersatz: Cleaner API (für Sonderfälle)

```java
// Für Klassen, die AutoCloseable nicht implementieren können:
static final Cleaner cleaner = Cleaner.create();

class Resource {
    private final Cleaner.Cleanable cleanable;

    Resource() {
        // Registriert eine Aktion, die ausgeführt wird, wenn das Objekt GC-reif ist
        this.cleanable = cleaner.register(this, () -> releaseNativeResource());
    }
}
```

---

## 3. SecurityManager (entfernt in Java 24)

Der `SecurityManager` war ein komplexes, oft falsch verwendetes Sicherheitsmodell.

```java
// Java 17: Deprecated-Warnung
System.setSecurityManager(new MySecurityManager());  // ⚠️ deprecated

// Java 24: wirft UnsupportedOperationException
// System.setSecurityManager(...);  // ❌ entfernt
```

**Alternativen:**

- OS-Ebene: Container (Docker), OS-Sandboxing, SELinux
- JVM-Ebene: Modulpfade und `--limit-modules`
- Anwendungsebene: eigene Validierungslogik, Input-Sanitization

---

## 4. Thread-Methoden (Java 20)

```java
// ❌ Entfernt in Java 20:
thread.stop();     // unsicher (Locks werden nicht freigegeben)
thread.suspend();  // Deadlock-anfällig
thread.resume();   // zusammen mit suspend entfernt

// ✅ Richtig: kooperatives Stoppen mit interrupt()
thread.interrupt();

// Im Thread prüfen:
while (!Thread.currentThread().isInterrupted()) {
    doWork();
}
```

---

## 5. Migrationsanalyse mit jdeps und jdeprscan

### 5.1 jdeprscan – Deprecated APIs finden

```bash
# Eigenes JAR auf deprecated/entfernte APIs prüfen:
jdeprscan --release 25 myapp.jar

# Nur APIs, die bereits entfernt wurden:
jdeprscan --release 25 --for-removal myapp.jar
```

Typische Ausgabe:

```
class com/example/LegacyClass uses deprecated method java/lang/Thread::stop()V
```

### 5.2 jdeps – Modulabhängigkeiten analysieren

```bash
# Abhängigkeiten des JARs anzeigen:
jdeps myapp.jar

# Auf JDK-interne APIs prüfen (sun.*, com.sun.*):
jdeps --jdk-internals myapp.jar

# Vollständige Modul-Analyse:
jdeps --module-path libs --check mymodule
```

### 5.3 Typischer Migrationsprozess

```
1. jdeprscan --release 25 --for-removal myapp.jar
   → Zeigt sofort handlungspflichtigen Code

2. jdeprscan --release 25 myapp.jar
   → Zeigt Code, der zwar noch funktioniert, aber zukünftig entfernt wird

3. jdeps --jdk-internals myapp.jar
   → Zeigt Nutzung interner JDK-APIs (sun.misc.Unsafe etc.)

4. Abhängigkeiten aktualisieren (commons-lang etc.)
   → Viele Libraries haben inzwischen Java 17/21/25-kompatible Versionen

5. Mit JDK 25 testen
```

---

## 6. Häufige Bibliotheks-Probleme beim Upgrade

| Bibliothek           | Problem                                      | Lösung                  |
|---------------------|----------------------------------------------|-------------------------|
| Apache Commons      | Nutzt `SecurityManager`                      | Update auf aktuelle Version |
| Spring < 6.x        | Diverse interne JDK-APIs                     | Upgrade auf Spring 6+   |
| Hibernate < 6.x     | Byte-Code-Manipulation inkompatibel          | Upgrade auf Hibernate 6+|
| Log4j < 2.x         | Entfernte APIs                               | Migration auf Log4j 2   |
| EE-Container        | `SecurityManager`-Abhängigkeiten             | Update des Containers   |

---

## 7. Merksätze

- `finalize()` → immer `try-with-resources` bevorzugen
- `SecurityManager` → seit Java 24 entfernt, externe Security nutzen
- `thread.stop()` → `thread.interrupt()` und kooperatives Design
- `jdeprscan --for-removal` vor jedem Major-Upgrade ausführen
- Bibliotheken auf aktuelle Versionen halten
