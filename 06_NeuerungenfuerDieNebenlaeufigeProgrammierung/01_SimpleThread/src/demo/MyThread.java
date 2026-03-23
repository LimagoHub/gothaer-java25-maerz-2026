package demo;

public class MyThread implements Runnable {

    private final String message;

    public MyThread(final String message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 10 ; i++) {
                System.out.println(message);
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
