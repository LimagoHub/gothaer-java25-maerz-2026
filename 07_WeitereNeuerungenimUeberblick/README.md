# Weitere Neuerungen im Überblick (Java 9–25)

Dieses Kapitel fasst **wichtige, oft unterschätzte Neuerungen** zusammen,
die nicht direkt Sprachsyntax betreffen, aber den Java-Alltag stark verändert haben.

---

## 1. JVM-Änderungen: Wegfall der Finalization

### Was war Finalization?

```java
// ❌ Nie mehr verwenden – entfernt in Java 21:
@Override
protected void finalize() throws Throwable {
    closeResource();  // unvorhersehbar, fehleranfällig
}
```

Probleme:

- unvorhersehbarer Aufrufzeitpunkt (oder gar keiner)
- schlechte GC-Performance
- Sicherheitsprobleme
- Ressourcen-Leaks

### Entwicklung

| Java-Version | Status                         |
|-------------|--------------------------------|
| Java 9      | Deprecated (Warnung)           |
| Java 18     | Deprecated for Removal         |
| Java 21     | Finalization faktisch entfernt |

### Ersatz

```java
// ✅ try-with-resources (Standardlösung):
try (var stream = new FileInputStream("data.txt")) {
    // Ressource wird garantiert freigegeben
}

// ✅ Cleaner API (für spezielle Fälle ohne AutoCloseable):
static final Cleaner CLEANER = Cleaner.create();

CLEANER.register(myObject, () -> System.out.println("GC: Objekt aufgeräumt"));
```

---

## 2. JShell – Interaktive Java-Shell

### Was ist JShell? (Java 9+)

`jshell` ist eine **REPL** (Read-Eval-Print Loop) für Java:
→ Java-Code ohne Projekt, ohne Klasse, ohne `main`-Methode

```bash
jshell
```

```java
jshell> int x = 10;
jshell> x * 2
$2 ==> 20
jshell> "Hallo".repeat(3)
$3 ==> "HalloHalloHallo"
jshell> List.of(1, 2, 3).stream().mapToInt(i -> i).sum()
$4 ==> 6
```

### Methoden und Klassen in JShell

```java
jshell> int add(int a, int b) { return a + b; }
|  created method add(int,int)
jshell> add(3, 4)
$2 ==> 7
```

### Typische Einsatzfälle

- Schnelles Ausprobieren von APIs
- Lernen von Java-Features
- Prototyping und Experimente
- Debugging kleiner Logik

---

## 3. Direkte Programmausführung ohne Compile-Schritt

### Single-File-Ausführung (Java 11+)

```bash
# Klassisch:
javac Hello.java
java Hello

# Modern (Java 11+):
java Hello.java   # javac wird implizit ausgeführt
```

Ideal für:
- kleine Tools und Skripte
- Lernbeispiele
- schnelle Prototypen

Einschränkungen:
- nur **eine** Quelldatei
- keine expliziten Module
- nicht für größere Projekte

---

## 4. Simple Main Methods (Java 25, Preview)

> **JEP 512** – Simple Source Files and Instance Main Methods (Java 25, Preview)
>
> Aktivierung: `javac --enable-preview --release 25 Hello.java`

Java 25 (Preview) vereinfacht den Einstieg weiter: `main()` muss weder
`public` noch `static` sein, und `String[] args` ist optional.

```java
// Java 25 Preview – minimales Programm:
void main() {
    System.out.println("Hello Java 25!");
}
```

```java
// Mit Zugriff auf args (ebenfalls gültig):
void main(String[] args) {
    System.out.println("Args: " + List.of(args));
}
```

Kombination mit impliziter Klasse (kein `class`-Wrapper nötig):

```java
// Diese Datei ist komplett gültig in Java 25 Preview:
import java.util.List;

void main() {
    var namen = List.of("Alice", "Bob", "Charlie");
    namen.forEach(System.out::println);
}
```

> Da dies noch Preview ist, ist `--enable-preview` beim Kompilieren und Ausführen nötig.

---

## 5. Simple Web Server (Java 18+)

Schnell einen **lokalen HTTP-Server** starten – ohne Framework, ohne Code, ohne Build:

```bash
# In einem Verzeichnis starten:
jwebserver

# Ausgabe:
# Serving /home/user at http://0.0.0.0:8000
```

### Konfiguration

```bash
# Port und Verzeichnis konfigurieren:
jwebserver --port 9000 --directory ./public

# Nur localhost:
jwebserver --bind-address 127.0.0.1
```

### Typische Einsatzfälle

- Lokale Tests von statischen HTML/JS/CSS-Dateien
- Schulungsumgebungen
- Schnelle Demos
- Frontend-Entwicklung ohne komplexen Server-Stack

> **Kein Produktionsserver!** Nur für Entwicklung und Tests gedacht.

---

## 6. Class-File API (Java 24, final)

> **JEP 484** – final in Java 24

Die Class-File API ermöglicht das programmatische Lesen, Schreiben und Transformieren
von `.class`-Dateien direkt im JDK – ohne externe Bibliotheken wie ASM oder Javassist.

```java
// Beispiel: Class-File lesen
ClassFile cf = ClassFile.of();
ClassModel classModel = cf.parse(Path.of("MyClass.class"));

classModel.methods().forEach(method ->
    System.out.println("Methode: " + method.methodName().stringValue())
);
```

Relevant für:
- Framework-Entwickler (Spring, Hibernate)
- Build-Tool-Entwickler
- Code-Analyse und Instrumentierung
- Ersatz für externe Bytecode-Bibliotheken

---

## 7. Zusammenfassung: Feature-Matrix

| Feature                        | Version  | Status    |
|-------------------------------|----------|-----------|
| JShell (REPL)                  | Java 9   | stabil    |
| Single-File-Ausführung         | Java 11  | stabil    |
| Simple Web Server (`jwebserver`)| Java 18  | stabil    |
| Finalization entfernt          | Java 21  | –         |
| Class-File API                 | Java 24  | final     |
| Simple Main Methods            | Java 25  | Preview   |

---

## 8. Merksätze

- **Finalization ist tot** – nutze `try-with-resources` oder `Cleaner`.
- **JShell** ist perfekt zum Lernen und schnellen Experimentieren.
- **`java Hello.java`** ist ideal für kleine Tools und Skripte.
- **`jwebserver`** ist schnell, aber nur für lokale Nutzung.
- **Simple Main Methods** (Preview) reduzieren Boilerplate für Einsteiger.
- **Class-File API** ersetzt externe Bytecode-Bibliotheken für Framework-Entwickler.
