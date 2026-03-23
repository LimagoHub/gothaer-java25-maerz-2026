package main;

public class DontDoIt {

    /*
    public static void main(String[] args) {
        for (int i = 0; i < 50_000; i++) {
            new Thread(() -> {
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException ignored) {
                }
            }).start();
        }

    }


     */
    public static void main(String[] args) throws Exception {

        for (int i = 0; i < 50_000; i++) {
            Thread.startVirtualThread(() -> {
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException ignored) {}
            });
        }

        Thread.sleep(15_000); // JVM am Leben halten
    }


}
