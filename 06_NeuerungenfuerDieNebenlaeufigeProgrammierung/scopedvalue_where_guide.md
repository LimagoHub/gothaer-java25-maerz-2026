# ScopedValue.where(...) – Möglichkeiten und Verwendung

Dieses Dokument erklärt **systematisch**, was man in  
`ScopedValue.where(...)` angeben kann, wie mehrere `where()` kombiniert werden
und welche Regeln dabei gelten.  
Alle Beispiele beziehen sich auf **Java 25 (final, JEP 506)**.

---

## 1. Grundform von `where`

```java
ScopedValue.where(scopedValue, value)
```

Eigenschaften:

- genau **ein** `ScopedValue` als Schlüssel
- genau **ein** Wert
- der Wert darf **null** sein
- Ergebnis ist ein **Carrier**, auf dem weitere `where()` folgen können

---

## 2. Einfachstes Beispiel

```java
import java.lang.ScopedValue;

public class SimpleWhereExample {

    static final ScopedValue<String> USER =
            ScopedValue.newInstance();

    public static void main(String[] args) {

        ScopedValue.where(USER, "Alice").run(() -> {
            System.out.println(USER.get());
        });
    }
}
```

✅ Ausgabe:
```
Alice
```

---

## 3. Mehrere `where()` kombinieren

Mehrere Scoped Values werden durch **Verkettung** gebunden.

```java
static final ScopedValue<String> USER =
        ScopedValue.newInstance();
static final ScopedValue<String> REQUEST_ID =
        ScopedValue.newInstance();

ScopedValue
    .where(USER, "Alice")
    .where(REQUEST_ID, "req-42")
    .run(() -> {
        System.out.println(USER.get());
        System.out.println(REQUEST_ID.get());
    });
```

✅ Ausgabe:
```
Alice
req-42
```

---

## 4. Verschachtelte Scopes (Shadowing)

Innere Scopes können Werte **überschreiben**, ohne äußere zu verändern.

```java
ScopedValue.where(USER, "Alice").run(() -> {

    System.out.println(USER.get()); // Alice

    ScopedValue.where(USER, "Bob").run(() -> {
        System.out.println(USER.get()); // Bob
    });

    System.out.println(USER.get()); // Alice
});
```

➡️ Lexikalisches Scoping wie bei lokalen Variablen.

---

## 5. Welche Typen sind als Wert erlaubt?

✅ Jeder Objekttyp:

```java
ScopedValue<String>
ScopedValue<Integer>
ScopedValue<User>
ScopedValue<List<String>>
ScopedValue<Map<String, Object>>
```

Beispiel mit eigener Klasse:

```java
record User(String name) {}

static final ScopedValue<User> CURRENT_USER =
        ScopedValue.newInstance();

ScopedValue.where(CURRENT_USER, new User("Alice")).run(() -> {
    System.out.println(CURRENT_USER.get().name());
});
```

---

## 6. Darf der Wert `null` sein?

✅ Ja, `null` ist erlaubt.

```java
ScopedValue.where(USER, null).run(() -> {
    System.out.println(USER.get()); // null
});
```

⚠️ Achtung:
- keine automatische Absicherung
- Null-Prüfungen selbst durchführen

---

## 7. Was man **nicht** in `where()` angeben kann

### ❌ Mehrere Werte auf einmal

```java
ScopedValue.where(USER, "Alice", REQUEST_ID, "42"); // ❌ nicht erlaubt
```

### ❌ Collections als Ersatz für mehrere Scoped Values

```java
ScopedValue.where(CONTEXT_MAP, Map.of(...)); // ❌ Anti-Pattern
```

### ❌ Nachträgliches Setzen oder Entfernen

```java
USER.set("Alice");    // ❌ gibt es nicht
USER.remove();        // ❌ gibt es nicht
```

---

## 8. Zugriff außerhalb des Scopes

```java
USER.get(); // ❌ IllegalStateException
```

Scoped Values existieren **nur innerhalb** des `run()`-Blocks.

---

## 9. Vergleich zu ThreadLocal

| ThreadLocal | ScopedValue |
|------------|------------|
| `set()` / `remove()` | ❌ |
| Thread-gebunden | ❌ |
| Scope-gebunden | ✅ |
| Leaks möglich | ❌ |
| Virtual-Thread-freundlich | ✅ |

---

## 10. Mentales Modell

> `ScopedValue.where()` ist kein Setter,  
> sondern öffnet einen **neuen, klar begrenzten Kontext**.

Oder:

> **ScopedValue ist ein unsichtbarer, unveränderlicher Parameter,  
> der nur innerhalb eines definierten Scopes existiert.**

---

## 11. Merksätze

- ✅ `where(scopedValue, value)` → genau ein Paar
- ✅ mehrere `where()` sind verkettbar
- ✅ Werte können beliebige Objekte sein
- ✅ `null` ist erlaubt
- ❌ kein Setzen oder Entfernen
- ❌ kein Zugriff außerhalb des Scopes

---

Dieses Dokument eignet sich als **Einführungskapitel** zu Scoped Values
oder als **Nachschlagewerk** in Loom-Tutorials (Java 25+).
