# Pinning bei Virtual Threads – Historie, Lösung und Hintergrund

> **Zusammenfassung:** Das Pinning-Problem durch `synchronized` wurde in **Java 24 (JEP 491)** behoben.
> In Java 25 ist `synchronized` mit virtuellen Threads **vollständig unbedenklich** für IO und `sleep`.

---

## 1. Was war Pinning? (Kontext Java 21)

**Pinning** bedeutete in Java 21:

> Ein **virtueller Thread** konnte seinen **Carrier-Thread (OS-Thread)**
> **nicht freigeben**, obwohl er blockierte.

Normalerweise gilt bei Virtual Threads:

1. Virtueller Thread macht blockierenden Aufruf (IO, `sleep`, Lock-Warten)
2. Virtueller Thread wird **geparkt** → Carrier-Thread frei für andere ✅

Bei Pinning in Java 21:

1. Virtueller Thread blockiert **innerhalb von `synchronized`**
2. **Carrier-Thread bleibt gebunden** → Skalierungsvorteil verloren ❌

---

## 2. Ursache (Java 21)

`synchronized` nutzt **intrinsische Monitore**, die historisch an OS-Threads
gebunden sind. Die JVM konnte in Java 21 den Monitor-Zustand nicht vom
OS-Thread trennen.

```java
// Java 21: dies verursachte Pinning!
synchronized (lock) {
    Thread.sleep(2000);  // ❌ Carrier-Thread blockiert
}
```

### Workaround in Java 21: ReentrantLock

`ReentrantLock` ist Loom-aware und verursachte auch in Java 21 kein Pinning:

```java
// Java 21 Lösung: ReentrantLock statt synchronized
lock.lock();
try {
    Thread.sleep(2000);  // ✅ Carrier-Thread wird freigegeben
} finally {
    lock.unlock();
}
```

---

## 3. Lösung: JEP 491 (Java 24)

**JEP 491 – Synchronize Virtual Threads without Pinning** wurde in Java 24 finalisiert.

Die JVM wurde so umgebaut, dass virtuelle Threads auch in `synchronized`-Blöcken
korrekt geparkt werden können. Der Monitor-Zustand wird nun vom virtuellen Thread
verwaltet, nicht mehr vom Carrier-Thread.

```java
// Java 24+: synchronized verursacht kein Pinning mehr ✅
synchronized (lock) {
    Thread.sleep(2000);  // ✅ Carrier-Thread wird freigegeben
}
```

---

## 4. Wann tritt Pinning in Java 24+ noch auf?

| Ursache                      | Pinning?       | Hinweis                              |
|------------------------------|----------------|--------------------------------------|
| `synchronized` + IO/sleep    | **Nein** ✅    | gelöst durch JEP 491 (Java 24)       |
| `ReentrantLock` + IO/sleep   | **Nein** ✅    | war schon in Java 21 ok              |
| JNI-Aufrufe (native Code)    | **Ja** ⚠️     | JVM kann Stack nicht sicher trennen  |
| Sehr langer CPU-Code (kein Yield) | **Ja** ⚠️| kein Blocking → kein Park-Punkt     |

---

## 5. Was ändert sich für die Praxis?

### Kein pauschaler Ersatz von synchronized nötig

In Java 24/25 muss `synchronized` **nicht** mehr ersetzt werden, nur
weil virtuelle Threads verwendet werden.

```java
// Java 24+: beide Varianten sind gleichwertig für VTs
synchronized (lock) { doIO(); }   // ✅ kein Pinning

lock.lock();
try { doIO(); } finally { lock.unlock(); }  // ✅ ebenfalls ok
```

### Wann ReentrantLock trotzdem bevorzugen?

- `tryLock()` mit Timeout benötigt
- Faire Lock-Vergabe gewünscht: `new ReentrantLock(true)`
- Conditions benötigt: `lock.newCondition()`

---

## 6. Pinning erkennen (falls noch relevant, z. B. wegen JNI)

```bash
java -Djdk.tracePinnedThreads=full MyApp
```

Ausgabe bei Pinning:
```
VirtualThread[#23] pinned on monitor ...
```

---

## 7. Merksätze

- ✅ Java 24+: `synchronized` ist sicher für virtuelle Threads
- ⚠️ JNI-Aufrufe verursachen weiterhin Pinning
- `ReentrantLock` bleibt wertvoll für tryLock, fairness, Conditions
- Monitoring mit `-Djdk.tracePinnedThreads=full` weiterhin möglich
