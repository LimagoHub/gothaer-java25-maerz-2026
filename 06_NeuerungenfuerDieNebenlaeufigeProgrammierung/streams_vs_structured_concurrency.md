# Streams vs. Structured Concurrency (Java 17–25)

Dieses Kapitel vergleicht die **Java Stream API** mit **Structured Concurrency (Project Loom)**.  
Beide Konzepte ermöglichen parallele Verarbeitung, verfolgen aber **grundlegend unterschiedliche Ziele** und Einsatzgebiete.

---

## 1. Grundidee beider Konzepte

### Streams
Streams sind ein **deklaratives Datenverarbeitungsmodell**:

- Fokus: *Transformation von Daten*
- Pipeline aus `map`, `filter`, `reduce`
- optional parallel (`parallelStream()`)

```java
list.stream()
    .filter(x -> x > 10)
    .map(x -> x * 2)
    .toList();
```

---

### Structured Concurrency
Structured Concurrency ist ein **Kontrollflussmodell für Nebenläufigkeit**:

- Fokus: *Zerlegung einer Aufgabe in Teilaufgaben*
- klare Lebensdauer von Tasks
- explizite Fehler- und Abbruchlogik

```java
try (var scope = StructuredTaskScope.open(
        StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {
    var a = scope.fork(this::loadUser);
    var b = scope.fork(this::loadOrders);

    scope.join();  // wartet & wirft bei Fehler automatisch

    return new Result(a.get(), b.get());
}
```

---

## 2. Zielsetzung im Vergleich

| Aspekt | Streams | Structured Concurrency |
|------|--------|------------------------|
| Hauptzweck | Datenverarbeitung | Aufgabenkoordination |
| Abstraktion | Pipeline | Scope |
| Denkmodell | funktional | strukturiert / imperativ |
| Typische Nutzung | Collections, Datenströme | IO, Services, Requests |

---

## 3. Parallelität: Wie wird Arbeit verteilt?

### Streams
- `stream()` → sequentiell
- `parallelStream()` → ForkJoinPool (`commonPool`)
- Parallelität ist *implizit*

```java
list.parallelStream().forEach(this::process);
```

⚠️ Nachteile:
- wenig Kontrolle über Threading
- schwer vorherzusagen
- schlechter kombinierbar mit Virtual Threads

---

### Structured Concurrency
- jede Aufgabe explizit
- standardmäßig **virtuelle Threads**
- Parallelität ist *explizit und kontrolliert*

```java
scope.fork(() -> remoteCall());
```

✔ klare Lebensdauer  
✔ kontrollierte Fehlerbehandlung  
✔ Virtual-Thread-freundlich  

---

## 4. Fehlerbehandlung

### Streams

```java
list.stream()
    .map(this::mayFail) // Exception beendet Pipeline
    .toList();
```

- Fehler abbrechen den Stream
- keine Aggregation
- kein partieller Erfolg

---

### Structured Concurrency

```java
scope.throwIfFailed();
```

- Fehler werden gesammelt
- andere Tasks können automatisch abgebrochen werden
- saubere Weitergabe an den Aufrufer

---

## 5. Cancellation & Abbruch

| Aspekt | Streams | Structured Concurrency |
|------|--------|------------------------|
| Abbruch möglich | sehr eingeschränkt | explizit |
| Teilaufgaben stoppen | nein | ja |
| Timeout-Unterstützung | nein | ja (kombinierbar) |

---

## 6. Umgang mit Blockierung (IO)

### Streams
- blockierendes IO im Stream ist problematisch
- `parallelStream()` blockiert ForkJoin-Threads
- kann gesamte JVM ausbremsen

```java
list.parallelStream()
    .map(this::blockingHttpCall) // schlecht
    .toList();
```

---

### Structured Concurrency
- speziell für blockierende IO entworfen
- Virtual Threads blockieren keine OS-Threads

```java
scope.fork(this::blockingHttpCall); // gut
```

---

## 7. Skalierbarkeit

| Kriterium | Streams | Structured Concurrency |
|---------|--------|------------------------|
| Millionen Tasks | ❌ nein | ✅ ja |
| IO-lastige Workloads | ❌ ungeeignet | ✅ ideal |
| CPU-bound Workloads | ✅ gut | ✅ (mit Begrenzung) |

---

## 8. Lesbarkeit & Wartbarkeit

### Streams
✔ sehr kompakt  
✔ ideal für Transformationen  
❌ komplex bei Fehlerlogik  
❌ schwer zu debuggen bei Nebenläufigkeit  

### Structured Concurrency
✔ expliziter Kontrollfluss  
✔ sehr gut debuggbar  
✔ klare Scope-Grenzen  
✔ wartungsfreundlich  

---

## 9. Typische Einsatzszenarien

### Streams verwenden, wenn:
- Daten transformiert oder gefiltert werden
- kein blockierendes IO enthalten ist
- Parallelität nebensächlich ist
- Code funktional & kurz bleiben soll

### Structured Concurrency verwenden, wenn:
- mehrere IO-Operationen parallel laufen
- Tasks logisch zusammengehören
- Fehler konsistent behandelt werden müssen
- Virtual Threads genutzt werden

---

## 10. Kombination beider Konzepte

Streams und Structured Concurrency schließen sich **nicht aus**:

```java
try (var scope = StructuredTaskScope.open(
        StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {
    var results = scope.fork(() ->
        list.stream()
            .map(this::transform)
            .toList()
    );

    scope.join();
    return results.get();
}
```

➡️ Structured Concurrency für *Task-Koordination*  
➡️ Streams für *Datenverarbeitung*

---

## 11. Fazit

**Streams und Structured Concurrency lösen unterschiedliche Probleme.**

- Streams = *Was passiert mit den Daten?*
- Structured Concurrency = *Wie laufen Aufgaben zusammen?*

### Merksatz:
> **Streams transformieren Daten.  
> Structured Concurrency strukturiert Nebenläufigkeit.**

In modernen Java-Anwendungen (17–25+) ergänzen sich beide Konzepte optimal.
