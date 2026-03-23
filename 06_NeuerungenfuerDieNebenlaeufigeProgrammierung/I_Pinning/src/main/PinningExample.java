package main;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstration des Pinning-Problems und seiner Lösung.
 *
 * HISTORY:
 *  - Java 21: synchronized + blockierender Aufruf = PINNING
 *             (Carrier-Thread blieb gebunden, kein Vorteil virtueller Threads)
 *  - Java 24: JEP 491 löst das Problem – synchronized verursacht kein Pinning mehr.
 *
 * In Java 25 (und ab Java 24) ist der synchronized-Block für virtuelle Threads
 * unbedenklich. ReentrantLock ist trotzdem nützlich wegen tryLock, fairness
 * und Conditions.
 */
public class PinningExample {

    static final Object SYNC_LOCK = new Object();
    static final ReentrantLock REENTRANT_LOCK = new ReentrantLock();

    public static void main(String[] args) throws Exception {

        System.out.println("=== Variante A: synchronized (Java 24+: kein Pinning mehr) ===");
        demonstrateSynchronized();

        Thread.sleep(1000);

        System.out.println("\n=== Variante B: ReentrantLock (war die Lösung für Java 21) ===");
        demonstrateReentrantLock();

        Thread.sleep(7000);
    }

    /**
     * FRÜHER (Java 21): synchronized + sleep = PINNING.
     * HEUTE  (Java 24+): kein Pinning mehr – JEP 491 hat dieses Problem gelöst.
     *
     * Der Carrier-Thread wird nun auch innerhalb von synchronized-Blöcken
     * korrekt freigegeben, wenn der virtuelle Thread parkt.
     */
    static void demonstrateSynchronized() {
        for (int i = 0; i < 3; i++) {
            int id = i;
            Thread.startVirtualThread(() -> {
                synchronized (SYNC_LOCK) {
                    try {
                        System.out.println("Thread " + id + " hält SYNC_LOCK");
                        Thread.sleep(2000); // ✅ Java 24+: kein Pinning (war ❌ in Java 21)
                        System.out.println("Thread " + id + " fertig");
                    } catch (InterruptedException ignored) {}
                }
            });
        }
    }

    /**
     * ReentrantLock war in Java 21 die empfohlene Lösung gegen Pinning.
     * In Java 24+ ist er weiterhin nützlich für:
     *  - tryLock mit Timeout
     *  - faire Lock-Vergabe (new ReentrantLock(true))
     *  - Conditions (lock.newCondition())
     *
     * Für einfaches gegenseitiges Ausschließen kann heute synchronized verwendet werden.
     */
    static void demonstrateReentrantLock() {
        for (int i = 0; i < 3; i++) {
            int id = i;
            Thread.startVirtualThread(() -> {
                REENTRANT_LOCK.lock();
                try {
                    System.out.println("Thread " + id + " hält REENTRANT_LOCK");
                    Thread.sleep(2000); // ✅ kein Pinning (war schon in Java 21 ok)
                    System.out.println("Thread " + id + " fertig");
                } catch (InterruptedException ignored) {
                } finally {
                    REENTRANT_LOCK.unlock(); // IMMER im finally-Block!
                }
            });
        }
    }
}
