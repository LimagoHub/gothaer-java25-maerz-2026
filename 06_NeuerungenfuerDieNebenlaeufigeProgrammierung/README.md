# Neuerungen für die nebenläufige Programmierung (Java 21–25)

Dieses Kapitel behandelt **Virtual Threads**, **Structured Concurrency**, **Scoped Values** und verwandte Themen aus Project Loom.

---

## Inhalt

1. [Motivation: Warum virtuelle Threads?](#1-motivation-warum-virtuelle-threads)
2. [Was sind virtuelle Threads?](#2-was-sind-virtuelle-threads)
3. [Lebenszyklus virtueller Threads](#3-lebenszyklus-virtueller-threads)
4. [ExecutorService mit virtuellen Threads](#4-executorservice-mit-virtuellen-threads)
5. [Structured Concurrency & StructuredTaskScope](#5-structured-concurrency--structuredtaskscope)
6. [Scoped Values](#6-scoped-values)
7. [Locks und Synchronisation](#7-locks-und-synchronisation)
8. [Contention](#8-contention)
9. [Starvation](#9-starvation)
10. [Pinning – gelöst in Java 24](#10-pinning--gelöst-in-java-24)
11. [Langer CPU-Code ohne Yield](#11-langer-cpu-code-ohne-yield)
12. [Best Practices](#12-best-practices)

---

## 1. Motivation: Warum virtuelle Threads?

Klassische Java-Threads sind **1:1 an Betriebssystem-Threads gebunden**.
Das führt bei vielen gleichzeitigen, meist IO-blockierenden Aufgaben zu:

- hohem Speicherverbrauch (~1–2 MB Stack pro Thread)
- begrenzter Skalierbarkeit (typisch: max. 10.000 Threads pro JVM)
- komplexem asynchronem Code (Callbacks, Reactive APIs)

**Virtuelle Threads** lösen dieses Problem:

- extrem leichtgewichtig (wenige KB pro Thread)
- Millionen Threads pro JVM möglich
- blockierende Aufrufe blockieren *keinen* OS-Thread mehr
- synchroner, gut lesbarer Code bleibt erhalten

---

## 2. Was sind virtuelle Threads?

Virtuelle Threads sind Java-Threads, die:

- von der **JVM** statt vom OS geplant werden
- auf **Carrier Threads** (OS-Threads aus einem Pool) ausgeführt werden
- bei Blockierung automatisch **geparkt** werden (Carrier-Thread frei für andere)
- später auf einem beliebigen Carrier fortgesetzt werden

```java
// Einfachste Form:
Thread.startVirtualThread(() -> {
    Thread.sleep(1000);                         // parkt, blockiert keinen OS-Thread
    System.out.println("Hello from virtual thread");
});
```

---

## 3. Lebenszyklus virtueller Threads

- Virtuelle Threads sind **keine Daemon-Threads**
- Sie laufen weiter, auch wenn `main()` endet
- Die JVM beendet sich erst, wenn **alle** virtuellen Threads fertig sind
- Synchronisation erfolgt wie bei klassischen Threads (`join`, Locks, usw.)

Empfohlene Verwaltung (automatisches Shutdown mit try-with-resources):

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> doWork());
}   // wartet hier auf alle Tasks und schließt den ExecutorService
```

---

## 4. ExecutorService mit virtuellen Threads

```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
```

Eigenschaften:

- jeder Task bekommt einen **eigenen** virtuellen Thread
- kein Pool, keine Begrenzung der Taskanzahl
- ideal für IO-lastige Workloads
- `submit()` liefert ein `Future`

**Best Practice:**

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    var future = executor.submit(() -> fetchData());
    System.out.println(future.get());
}
```

Für CPU-lastige Aufgaben weiterhin klassische Thread-Pools nutzen:

```java
// CPU-bound: fester Pool mit Runtime.availableProcessors() Threads
ExecutorService cpu = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors());
```

---

## 5. Structured Concurrency & StructuredTaskScope

> **Status in Java 25:** **final** (JEP 505)

`StructuredTaskScope` ermöglicht **strukturierte Nebenläufigkeit**: Tasks sind an
einen lexikalischen Scope gebunden und werden automatisch aufgeräumt.

```java
try (var scope = StructuredTaskScope.open(
        StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {

    // Tasks starten – jeder bekommt einen virtuellen Thread
    var user   = scope.fork(() -> loadUser(id));
    var orders = scope.fork(() -> loadOrders(id));

    scope.join();  // wartet auf beide Tasks; wirft bei Fehler automatisch

    return new Dashboard(user.get(), orders.get());
}
```

### fork() vs. submit()

| `submit()` (ExecutorService) | `fork()` (StructuredTaskScope) |
|------------------------------|-------------------------------|
| unstrukturierte Tasks        | Tasks an Scope gebunden       |
| manuelles Shutdown           | automatisches Aufräumen       |
| Fehler pro `Future`          | Fehler zentral im Scope       |
| `Future.get()` blockiert     | `Subtask.get()` nach join     |

### Joiner-Strategien (Java 25)

```java
// awaitAllSuccessfulOrThrow: bricht alle ab, wenn einer fehlschlägt
try (var scope = StructuredTaskScope.open(
        StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) { ... }

// anySuccessfulResultOrThrow: bricht alle ab, sobald einer erfolgreich ist
try (var scope = StructuredTaskScope.open(
        StructuredTaskScope.Joiner.<String>anySuccessfulResultOrThrow())) {
    scope.fork(() -> tryServer1());
    scope.fork(() -> tryServer2());
    String result = scope.join();  // das erste erfolgreiche Ergebnis
}
```

---

## 6. Scoped Values

> **Status in Java 25:** **final** (JEP 506)

Scoped Values sind eine **Thread-sichere Alternative zu `ThreadLocal`**,
optimiert für virtuelle Threads.

```java
static final ScopedValue<String> CURRENT_USER = ScopedValue.newInstance();

// Wert im Scope setzen und Code ausführen:
ScopedValue.where(CURRENT_USER, "alice").run(() -> {
    System.out.println(CURRENT_USER.get()); // "alice"
    handleRequest();
});

// Außerhalb des Scopes: kein Zugriff möglich
// CURRENT_USER.get(); // → NoSuchElementException
```

### Vorteile gegenüber ThreadLocal

| ThreadLocal                          | ScopedValue                          |
|--------------------------------------|--------------------------------------|
| veränderlich (`set()`/`remove()`)    | unveränderlich (immutable)           |
| explizites Aufräumen nötig           | automatisch aufgeräumt               |
| geerbt durch Kinder-Threads          | explizit weitergegeben               |
| problematisch bei Thread-Pooling     | ideal für virtuelle Threads          |

### Kombination mit StructuredTaskScope

```java
static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

ScopedValue.where(REQUEST_ID, "req-42").run(() -> {
    try (var scope = StructuredTaskScope.open(
            StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {
        // Alle geforkten Tasks sehen REQUEST_ID automatisch
        scope.fork(() -> {
            log("Task A, Request: " + REQUEST_ID.get());
            return fetchA();
        });
        scope.fork(() -> {
            log("Task B, Request: " + REQUEST_ID.get());
            return fetchB();
        });
        scope.join();  // wirft automatisch bei Fehler
    }
});
```

---

## 7. Locks und Synchronisation

Virtuelle Threads ändern **nicht** das Java Memory Model.
Alle bekannten Mechanismen gelten weiterhin:

| Mechanismus        | Beschreibung                                  |
|--------------------|-----------------------------------------------|
| `synchronized`     | einfach, seit Java 24 auch mit VTs unbedenklich|
| `ReentrantLock`    | mehr Kontrolle (tryLock, fairness, Conditions)|
| `ReadWriteLock`    | parallele Leser, exklusive Schreiber          |
| `StampedLock`      | optimistic reads, höchste Performance         |
| `volatile`         | Sichtbarkeitsgarantie ohne Lock               |
| `AtomicInteger` etc.| lockfreie CAS-Operationen                   |

**Allgemeine Empfehlung:**

- kritische Abschnitte kurz halten
- lockfreie oder immutable Strukturen bevorzugen
- `StructuredTaskScope` und `ScopedValue` für Kontext nutzen

---

## 8. Contention

**Contention** = mehrere Threads konkurrieren um dieselbe Ressource.

- hohe Contention → viel Warten, wenig Fortschritt
- viele virtuelle Threads machen Contention **sichtbarer**
- mehr Threads ≠ mehr Performance

Strategien zur Reduktion:

- weniger geteilten Zustand
- feinere Locks
- lockfreie Datenstrukturen (`ConcurrentHashMap`, `AtomicReference`)
- Arbeit außerhalb von Locks erledigen

---

## 9. Starvation

**Starvation** = ein Thread macht dauerhaft keinen Fortschritt.

Ursachen:

- unfaire Locks
- sehr viele kurze Tasks verdrängen lange Tasks
- Reader/Writer-Ungleichgewicht

```java
// Fairer Lock verhindert Starvation (kostet aber Performance):
new ReentrantLock(true);  // fair = true
```

---

## 10. Pinning – gelöst in Java 24

> **JEP 491, finalisiert in Java 24**

### Pinning in Java 21

In Java 21 gab es noch ein bekanntes Problem: Wenn ein virtueller Thread
innerhalb eines `synchronized`-Blocks blockierte (IO, `sleep`, Lock-Warten),
wurde der Carrier-Thread **angeheftet (pinned)** und stand anderen virtuellen
Threads nicht zur Verfügung.

```java
// In Java 21: dies verursachte Pinning
synchronized (lock) {
    Thread.sleep(1000); // Carrier-Thread blieb blockiert
}
```

### Gelöst: Synchronized ohne Pinning (Java 24+)

**JEP 491** hat dieses Problem behoben: Seit Java 24 kann die JVM virtuelle
Threads auch innerhalb von `synchronized`-Blöcken korrekt parken.

```java
// Java 24+: kein Pinning mehr, auch mit synchronized
synchronized (lock) {
    Thread.sleep(1000); // Carrier-Thread wird freigegeben ✅
}
```

**Wann tritt Pinning noch auf?**

| Ursache                  | Pinning?   | Hinweis                            |
|--------------------------|------------|------------------------------------|
| `synchronized` + IO      | ✅ gelöst  | seit Java 24 (JEP 491)             |
| `synchronized` + `sleep` | ✅ gelöst  | seit Java 24 (JEP 491)             |
| JNI-Aufrufe (native)     | ⚠️ ja      | JVM kann Stack nicht trennen       |
| Sehr langer CPU-Code     | ⚠️ ja      | kein Yield-Punkt → Carrier belegt  |

**Wichtig für Migration:** Falls du Code von Java 21 auf Java 24/25 migrierst,
musst du `synchronized`-Blöcke **nicht** mehr pauschal durch `ReentrantLock`
ersetzen. `ReentrantLock` bleibt trotzdem nützlich wegen tryLock, fairness
und Conditions.

### Erkennung von Pinning (falls noch nötig)

```bash
java -Djdk.tracePinnedThreads=full MyApp
```

Ausgabe bei Pinning:
```
VirtualThread[#23] pinned on monitor ...
```

---

## 11. Langer CPU-Code ohne Yield

Virtuelle Threads können den Carrier-Thread nur dann freigeben, wenn sie an einem
**Yield-Punkt** pausieren. Solche Punkte entstehen automatisch bei:

- `Thread.sleep()`
- Blockierendem IO (Netzwerk, Datei, DB)
- Lock-Warten (`ReentrantLock`, `synchronized` seit Java 24)
- `Object.wait()`

Führt ein virtueller Thread **reinen CPU-Code** aus – also rechnet er ohne jemals zu
blockieren – gibt es keine Yield-Punkte. Der Carrier-Thread ist die ganze Zeit belegt:

```java
Thread.startVirtualThread(() -> {
    // Kein sleep, kein IO, kein Lock – kein einziger Yield-Punkt
    long sum = 0;
    for (long i = 0; i < 10_000_000_000L; i++) {
        sum += i;   // Carrier-Thread bleibt die gesamte Zeit gebunden
    }
    System.out.println(sum);
});
```

> Die JVM kann virtuelle Threads **nicht präemptiv unterbrechen** – anders als das
> Betriebssystem mit OS-Threads (Timer-Interrupt). Virtuelle Threads müssen
> **kooperativ** pausieren.

### Wann ist das ein Problem?

Wenn viele CPU-lastige virtuelle Threads gleichzeitig laufen und die Carrier-Threads
erschöpft sind (Standard: Anzahl CPU-Kerne):

```
4 CPU-Kerne  →  4 Carrier-Threads
8 virtuelle Threads mit langen CPU-Berechnungen
→ 4 warten, obwohl sie "ready" sind – kein Vorteil gegenüber klassischen Threads
```

### Lösung: Das richtige Werkzeug wählen

Das eigentliche Problem ist eine **falsche Tool-Wahl**: CPU-lastige Aufgaben
gehören nicht in virtuelle Threads.

```java
// ❌ Falsch: CPU-intensiv mit virtuellen Threads
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> berechnePrimzahlen(1_000_000));
}

// ✅ Richtig: CPU-intensiv mit klassischem Pool
try (var executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors())) {
    executor.submit(() -> berechnePrimzahlen(1_000_000));
}
```

| Aufgabentyp                        | Richtige Wahl             |
|------------------------------------|---------------------------|
| IO-lastig (Netzwerk, DB, Dateien)  | Virtuelle Threads         |
| CPU-lastig (Berechnung, Kompression) | Klassischer Thread-Pool |

---

## 12. Best Practices

| Situation                        | Empfehlung                              |
|----------------------------------|-----------------------------------------|
| IO-lastige Aufgaben              | Virtuelle Threads                       |
| CPU-lastige Aufgaben             | Klassischer Thread-Pool                 |
| Strukturierte Task-Gruppen       | `StructuredTaskScope`                   |
| Kontext über Tasks teilen        | `ScopedValue` statt `ThreadLocal`       |
| Lange kritische Abschnitte       | `ReentrantLock` für mehr Kontrolle      |
| Synchronisation                  | Alle Mechanismen funktionieren          |
| Lange CPU-Schleifen in VTs       | Vermeiden – kein Yield-Punkt möglich    |
| JNI-Aufrufe in VTs               | Vermeiden – verursachen noch Pinning    |

**Merksätze:**

- IO-bound Tasks → virtuelle Threads
- CPU-bound Tasks → klassische Pools
- Strukturierte Concurrency bevorzugen (`StructuredTaskScope`)
- `ScopedValue` statt `ThreadLocal` für Kontext
- `synchronized` ist in Java 24+ unbedenklich für virtuelle Threads
- JNI-Aufrufe weiterhin vermeiden in virtuellen Threads

---

*Dieses Kapitel deckt Java 21–25 ab. Weitere Detaildokumente:*

- `virtual_threads.md` – Scaling & Performance Details
- `pinning_virtual_threads.md` – Pinning-Historie und Hintergrund
- `scopedvalue_where_guide.md` – ScopedValue API im Detail
- `streams_vs_structured_concurrency.md` – Vergleich beider Konzepte
