package main;

public class VirtualThreadsMany {

    public static void main(String[] args) throws InterruptedException {

        Thread[] threads = new Thread[3];

        for (int i = 0; i < 3; i++) {
            int id = i;
            threads[i] = Thread.startVirtualThread(() -> {
                System.out.println("Virtueller Thread " + id);
            });
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("Alle virtuellen Threads beendet");
    }
}