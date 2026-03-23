# Virtuelle Threads: Skalierung und Performance (Java 21–25)

> **Hauptdokumentation:** [README.md](README.md)
>
> Dieses Dokument ergänzt die Hauptdokumentation mit Details zu **Skalierung und Performance**.

Dieses Kapitel erklärt, warum virtuelle Threads skalieren, und enthält Details zu
Speicherverbrauch, Scheduling und dem Vergleich mit klassischen Threads.

---

## 1. Motivation: Warum virtuelle Threads?

Klassische Java-Threads sind **1:1 an Betriebssystem-Threads gebunden**.  
Das führt bei vielen gleichzeitigen, meist IO-blockierenden Aufgaben zu:

- hohem Speicherverbrauch (Stack pro Thread)
- begrenzter Skalierbarkeit
- komplexem asynchronem Code (Callbacks, Reactive APIs)

**Virtuelle Threads** lösen dieses Problem:

- extrem leichtgewichtig
- Millionen Threads pro JVM möglich
- blockierende Aufrufe blockieren *keine* OS-Threads
- synchroner, gut lesbarer Code bleibt erhalten

---

## 2. Was sind virtuelle Threads?

Virtuelle Threads sind Java-Threads, die:

- von der JVM statt vom OS geplant werden
- auf **Carrier Threads** (OS-Threads) ausgeführt werden
- bei Blockierung automatisch **geparkt** werden
- später auf einem beliebigen Carrier fortgesetzt werden können

```java
Thread.startVirtualThread(() -> {
    Thread.sleep(1000);
    System.out.println("Hello from virtual thread");
});
```

---

## 3. Lebenszyklus virtueller Threads

- virtuelle Threads sind **keine Daemon-Threads**
- sie laufen weiter, auch wenn `main` endet
- die JVM beendet sich erst, wenn alle virtuellen Threads beendet sind
- Synchronisation erfolgt wie bei klassischen Threads (`join`, Locks, etc.)

Empfohlene Verwaltung:

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> doWork());
}
```

---

## 4. ExecutorService mit virtuellen Threads

```java
ExecutorService executor =
        Executors.newVirtualThreadPerTaskExecutor();
```

Eigenschaften:

- jeder Task bekommt einen eigenen virtuellen Thread
- kein Pool, keine Begrenzung
- ideal für IO-lastige Workloads
- `submit()` liefert ein `Future`

Best Practice:

- immer mit try-with-resources verwenden
- für CPU-bound Tasks weiterhin klassische Thread-Pools nutzen

---

## 5. Structured Concurrency & StructuredTaskScope

`StructuredTaskScope` (final in Java 25, JEP 505) ermöglicht **strukturierte Nebenläufigkeit**:

```java
try (var scope = StructuredTaskScope.open(
        StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {
    var a = scope.fork(() -> taskA());
    var b = scope.fork(() -> taskB());

    scope.join();  // wartet & wirft bei Fehler automatisch

    return a.get() + b.get();
}
```

Vorteile:

- Tasks sind an einen Scope gebunden
- automatische Fehlerbehandlung
- kein „verlorener“ Hintergrund-Thread
- deutlich übersichtlicher als ExecutorService

### fork() vs submit()

| submit() | fork() |
|--------|-------|
| unstrukturierte Tasks | strukturierte Tasks |
| manuelles Shutdown | automatisches Aufräumen |
| Fehler pro Future | Fehler zentral im Scope |

---

## 6. Locks und Synchronisation

Virtuelle Threads ändern **nicht** das Java Memory Model.

Alle bekannten Mechanismen gelten weiterhin:

- `synchronized`
- `ReentrantLock`
- `ReadWriteLock`
- `StampedLock`
- `volatile`
- Atomics (`AtomicInteger`, …)

Empfehlung für virtuelle Threads:

- kritische Abschnitte sehr kurz halten
- keine Blockierung innerhalb von Locks
- atomare oder lockfreie Strukturen bevorzugen

---

## 7. Contention

**Contention** bedeutet, dass mehrere Threads um dieselbe Ressource konkurrieren.

- hohe Contention → viel Warten, wenig Fortschritt
- viele virtuelle Threads machen Contention sichtbarer
- mehr Threads ≠ mehr Performance

Strategien zur Reduktion:

- weniger geteilten Zustand
- feinere Locks
- lockfreie Datenstrukturen
- Arbeit außerhalb von Locks erledigen

---

## 8. Starvation

**Starvation** liegt vor, wenn ein Thread dauerhaft keinen Fortschritt macht.

Ursachen:

- unfaire Locks
- sehr viele kurze Tasks
- Reader/Writer-Ungleichgewicht

Beispiel:

```java
new ReentrantLock(true); // fair
```

Fairness verhindert Starvation, kostet aber Performance.

---

## 9. Pinning – gelöst in Java 24

> **JEP 491**, finalisiert in Java 24.

In Java 21 verursachte `synchronized` + blockierende Aufrufe **Pinning**:
der Carrier-Thread konnte nicht freigegeben werden.

**Java 24 hat dieses Problem behoben.** `synchronized` ist in Java 24+ vollständig
unbedenklich für virtuelle Threads.

Pinning tritt in Java 24+ noch auf bei:

- JNI-Aufrufen (native Methoden)
- sehr langem CPU-Code ohne Yield-Punkt

→ Details: [pinning_virtual_threads.md](pinning_virtual_threads.md)

---

## 10. Scoped Values (final in Java 25)

> **JEP 506** – finalisiert in Java 25 (war Preview in Java 21–24)

Scoped Values sind eine Alternative zu `ThreadLocal`:

```java
static final ScopedValue<String> USER = ScopedValue.newInstance();

ScopedValue.where(USER, "Alice").run(() -> {
    System.out.println(USER.get());  // "Alice"
    // Auch in geforkten Tasks (StructuredTaskScope) sichtbar
});

// Außerhalb des Scopes: nicht verfügbar
// USER.get(); // → NoSuchElementException
```

Vorteile:

- unveränderlich (immutable) – kein `set()`
- automatisch aufgeräumt wenn Scope endet
- perfekt für virtuelle Threads
- sicher bei parallelem Zugriff

→ Details: [scopedvalue_where_guide.md](scopedvalue_where_guide.md)

---

## 11. Fazit

Virtuelle Threads ermöglichen:

- massive Skalierbarkeit (Millionen Threads)
- einfachen, synchronen Code
- bessere Nutzung moderner Hardware

In Java 25:

- `synchronized` verursacht kein Pinning mehr (JEP 491)
- Structured Concurrency ist final (JEP 505)
- Scoped Values sind final (JEP 506)

**Best Practices:**

- IO-bound Tasks → virtuelle Threads
- CPU-bound Tasks → klassische Pools
- strukturierte Concurrency bevorzugen (`StructuredTaskScope`)
- `ScopedValue` statt `ThreadLocal` für Kontext
- JNI-Aufrufe in virtuellen Threads vermeiden (letztes verbleibendes Pinning-Risiko)

---

Dieses Kapitel gilt für Java 21–25.
→ Hauptdokumentation: [README.md](README.md)
