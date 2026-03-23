# Java 17–25 – Praktische Neuerungen im Überblick

Dieses Repository zeigt anhand kleiner, fokussierter Beispiele die wichtigsten Sprach- und API-Neuerungen von **Java 17** bis **Java 25 (LTS)**.

Ziel ist ein **praxisnahes Tutorial**, das du lokal ausführen und als Grundlage für eigene Schulungen oder Workshops nutzen kannst.

> **Umsteigerkurs:** Besonders geeignet für Java-Entwickler, die von einer älteren LTS-Version (8, 11, 17, 21) auf **Java 25** umsteigen.

---

## Inhaltsverzeichnis

- [Erweiterungen in der Syntax von Java](#1-erweiterungen-in-der-syntax-von-java)
  - [switch Expressions (Java 14, final)](#switch-expressions)
  - [Text Blocks (Java 15, final)](#text-blocks)
  - [Pattern Matching for switch (Java 21, final)](#pattern-matching-for-switch)
  - [Unnamed Variables & Patterns (Java 22, final)](#unnamed-variables--patterns)

- [Neuerungen in der Standard-API](#2-neuerungen-in-der-standard-api)
  - [SequencedCollection API (Java 21)](#sequencedcollection-api)
  - [Stream Gatherers (Java 24, final)](#stream-gatherers)

- [Neuerungen in einzelnen Klassen](#3-neuerungen-in-einzelnen-klassen)
  - [Objects, String](#objects-string)
  - [Stream API](#stream-api)
  - [NullPointerException](#nullpointerexception)

- [Entfernte APIs und Bibliotheken](#4-entfernte-apis-und-bibliotheken)
  - [Deprecated APIs und Klassen](#deprecated-apis)
  - [Werkzeuge jdeps und jdeprscan](#werkzeuge)

- [Algebraische Datentypen und Pattern Matching](#5-algebraische-datentypen-und-pattern-matching)
  - [Sealed Classes](#sealed-classes)
  - [Records](#records)
  - [Pattern Matching für instanceof und switch](#pattern-matching)

- [Neuerungen für die nebenläufige Programmierung](#6-neuerungen-fuer-die-nebenlaeufige-programmierung)
  - [Virtual Threads (Java 21, final)](#virtual-threads)
  - [Structured Concurrency (Java 25, final)](#structured-concurrency)
  - [Scoped Values (Java 25, final)](#scoped-values)

- [Weitere Neuerungen im Überblick](#7-weitere-neuerungen-im-ueberblick)
  - [JVM-Änderungen, Wegfall der Finalization](#jvm-aenderungen)
  - [JShell](#jshell)
  - [Direkte Programmausführung](#direkte-programmausfuehrung)
  - [Simple Web Server](#simple-web-server)
  - [Simple Main Methods (Java 25, Preview)](#simple-main-methods)

---

## Voraussetzungen

- Java Development Kit (**JDK 25**) installiert
  (einige Features sind erst in späteren Versionen final; Preview-Features erfordern `--enable-preview`)

---

## 1. Erweiterungen in der Syntax von Java

### Switch Expressions

Kapitel: `01_SwitchExpressionAndTextBlocks/` → [README.md](01_SwitchExpressionAndTextBlocks/README.md)

| Feature                        | Version  | Status    |
|-------------------------------|----------|-----------|
| Switch Expressions             | Java 14  | final     |
| Pattern Matching for switch    | Java 21  | final     |
| Unnamed Variables & Patterns   | Java 22  | final     |
| Primitive Types in Patterns    | Java 25  | Preview   |

**Wichtige Änderung:** Guard-Syntax in Java 21 ist `when`, nicht `&&`:

```java
// Java 21 (korrekt):
case Integer i when i > 0 -> "positiv";

// Java 17–20 Preview (alt, nicht mehr gültig):
// case Integer i && i > 0 -> "positiv";
```

### Text Blocks

Final seit Java 15. String Templates (JEP 430) wurden in Java 21 als Preview eingeführt,
aber in **Java 23 zurückgezogen** – sie sind in Java 25 **nicht vorhanden**.

```java
// Text Block (final, funktioniert in Java 25):
String json = """
        { "name": "%s" }
        """.formatted(name);

// STR-Prozessor (NICHT verfügbar in Java 25 – wurde zurückgezogen!):
// String msg = STR."Hello \{name}";  ❌
```

### Unnamed Variables & Patterns

```java
// Unbenutzte Variable klar kennzeichnen:
try { ... } catch (IOException _) { }

// Pattern ohne Variablenbindung:
if (obj instanceof String _) { ... }
```

---

## 2. Neuerungen in der Standard-API

### SequencedCollection API

Kapitel: `02_StandardAPI/` → [README.md](02_StandardAPI/README.md)

Java 21 führte drei neue Interfaces ein: `SequencedCollection`, `SequencedSet`, `SequencedMap`.

```java
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
String first = list.getFirst();     // statt list.get(0)
String last  = list.getLast();      // statt list.get(list.size()-1)
list.addFirst("x");
list.removeLast();
List<String> rev = list.reversed(); // kehrt Reihenfolge um
```

### Stream Gatherers

Kapitel: `02_StandardAPI/stream_gatherers.md` | Java 24, final (JEP 485)

Die neue Intermediate Operation `gather()` erlaubt benutzerdefinierte Transformationen:

```java
// Elemente in Gruppen von 3 aufteilen:
Stream.of(1, 2, 3, 4, 5, 6, 7)
    .gather(Gatherers.windowFixed(3))
    .toList();
// → [[1,2,3], [4,5,6], [7]]

// Gleitendes Fenster:
Stream.of(1, 2, 3, 4, 5)
    .gather(Gatherers.windowSliding(3))
    .toList();
// → [[1,2,3], [2,3,4], [3,4,5]]

// Laufende Summe (scan):
Stream.of(1, 2, 3, 4, 5)
    .gather(Gatherers.scan(() -> 0, Integer::sum))
    .toList();
// → [1, 3, 6, 10, 15]
```

---

## 3. Neuerungen in einzelnen Klassen

Kapitel: `03_NeuerungenInEinzelnenKlassen/` → [README.md](03_NeuerungenInEinzelnenKlassen/README.md)

| Klasse       | Neuerung                                    | Version  |
|-------------|---------------------------------------------|----------|
| `Objects`   | `checkIndex()`-Überladungen für `long`      | Java 16  |
| `Objects`   | `toIdentityString()`                        | Java 19  |
| `Stream`    | `toList()` (unveränderlich)                 | Java 16  |
| `Stream`    | `gather()` (Gatherers)                      | Java 24  |
| `String`    | Default-Encoding UTF-8                      | Java 18  |
| NPE         | Verbesserte Fehlermeldungen                 | Java 14  |

---

## 4. Entfernte APIs und Bibliotheken

Kapitel: `04_EntfernteAPIsUundBibliotheken/` → [README.md](04_EntfernteAPIsUundBibliotheken/README.md)

- `SecurityManager` entfernt (Java 17 deprecated, Java 24 entfernt)
- `Applet` API entfernt (Java 23)
- Finalization entfernt (Java 21)
- Werkzeuge: `jdeps`, `jdeprscan` für Migrationsanalyse

---

## 5. Algebraische Datentypen und Pattern Matching

Kapitel: `05_AlgebraischeDatentypenundPatternMatching/` → [README.md](05_AlgebraischeDatentypenundPatternMatching/README.md)

| Feature                    | Version  | Status  |
|---------------------------|----------|---------|
| `instanceof`-Patterns      | Java 16  | final   |
| Records                    | Java 16  | final   |
| Sealed Classes             | Java 17  | final   |
| Pattern Matching for switch| Java 21  | final   |
| Record Patterns            | Java 21  | final   |
| Unnamed Patterns (`_`)     | Java 22  | final   |

---

## 6. Neuerungen für die nebenläufige Programmierung

Kapitel: `06_NeuerungenfuerDieNebenlaeufigeProgrammierung/` → [README.md](06_NeuerungenfuerDieNebenlaeufigeProgrammierung/README.md)

| Feature                | Version  | Status         |
|-----------------------|----------|----------------|
| Virtual Threads        | Java 21  | final          |
| Structured Concurrency | Java 25  | **final**      |
| Scoped Values          | Java 25  | **final**      |
| Sync ohne Pinning      | Java 24  | final (JEP 491)|

### Virtual Threads

```java
// Einfachste Form:
Thread.startVirtualThread(() -> doWork());

// Mit ExecutorService (empfohlen):
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> fetchData());
}
```

### Kein Pinning mehr durch synchronized (Java 24+)

> **Wichtige Änderung gegenüber Java 21:** JEP 491 behebt das Pinning-Problem.
> `synchronized`-Blöcke verursachen in Java 24+ **kein Pinning** mehr.

```java
// Java 24+: sicher für virtuelle Threads
synchronized (lock) {
    Thread.sleep(1000);  // ✅ Carrier-Thread wird freigegeben
}
```

Pinning tritt in Java 25 noch auf bei: JNI-Aufrufen und sehr langem CPU-Code ohne Yield.

### Structured Concurrency (final in Java 25)

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var user   = scope.fork(() -> loadUser(id));
    var orders = scope.fork(() -> loadOrders(id));
    scope.join().throwIfFailed();
    return new Dashboard(user.get(), orders.get());
}
```

### Scoped Values (final in Java 25)

```java
static final ScopedValue<String> USER = ScopedValue.newInstance();

ScopedValue.where(USER, "alice").run(() -> {
    System.out.println(USER.get()); // "alice"
});
```

---

## 7. Weitere Neuerungen im Überblick

Kapitel: `07_WeitereNeuerungenimUeberblick/` → [README.md](07_WeitereNeuerungenimUeberblick/README.md)

| Feature                         | Version  | Status    |
|---------------------------------|----------|-----------|
| JShell (REPL)                   | Java 9   | stabil    |
| Single-File-Ausführung          | Java 11  | stabil    |
| Simple Web Server (`jwebserver`) | Java 18  | stabil    |
| Finalization entfernt           | Java 21  | –         |
| Simple Main Methods             | Java 25  | Preview   |

### Simple Main Methods (Java 25, Preview)

Vereinfachte Einstiegspunkte für kleine Programme – ohne `public static void main(String[] args)`:

```java
// Java 25 Preview (--enable-preview):
void main() {
    System.out.println("Hello Java 25!");
}
```

---

## Projektstruktur

```
01_SwitchExpressionAndTextBlocks/
  ├── README.md                     switch, Text Blocks, Unnamed Vars, Primitive Patterns
  ├── 01_YieldDemoProjekt/          yield in switch Expressions
  ├── 02_TextBlockProject/          Text Blocks + String.formatted()
  └── 03_UnnamedVariablesProject/   Unnamed Variables & Patterns (Java 22) ★NEU

02_StandardAPI/
  ├── README.md                     SequencedCollection, Stream Gatherers
  ├── 03_SequencedCollectionProject/
  ├── 04_StreamGatherersProject/    Stream Gatherers (Java 24) ★NEU
  └── stream_gatherers.md           vollständige Gatherers-Dokumentation ★NEU

03_NeuerungenInEinzelnenKlassen/
  ├── README.md                     Objects, String, Stream, NPE (konsolidiert)
  ├── 04_ObjectsDemo/
  ├── NullPointer/, RecordsErzeugen/, SealedClassesUndRecords/

04_EntfernteAPIsUundBibliotheken/
  ├── README.md                     Entfernte APIs, jdeps, jdeprscan

05_AlgebraischeDatentypenundPatternMatching/
  ├── README.md                     Sealed Classes, Records, Pattern Matching
  └── PrimitivePatternProject/      Primitive Types in Patterns (Java 25 Preview) ★NEU

06_NeuerungenfuerDieNebenlaeufigeProgrammierung/
  ├── README.md                     Virtual Threads, Structured Concurrency, Scoped Values
  ├── A_SimpleVirtualThread/        Thread.startVirtualThread()
  ├── B_SimpleExcecutorService/     newVirtualThreadPerTaskExecutor()
  ├── C_StructuredTaskScopeDemo/    ShutdownOnFailure
  ├── J_ShutdownOnSuccess/          ShutdownOnSuccess – erster Treffer gewinnt ★NEU
  ├── I_Pinning/                    Pinning-Demo (Java 24: kein Problem mehr)
  └── pinning_virtual_threads.md    Pinning-Historie und JEP 491 ★AKTUALISIERT

07_WeitereNeuerungenimUeberblick/
  ├── README.md                     JShell, Single-File, Web Server, Simple Main
  └── 01_SimpleMainProject/         Simple Main Methods (Java 25 Preview) ★NEU
```

> **★NEU** = im Zuge des Java-25-Updates hinzugefügt oder wesentlich aktualisiert
