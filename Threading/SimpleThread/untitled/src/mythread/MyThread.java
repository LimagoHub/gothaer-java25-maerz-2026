package mythread;

public class MyThread implements Runnable{

    private final String text;

    public MyThread(final String text) {
        this.text = text;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                System.out.println(text);
                Thread.sleep((long)(Math.random()* 1000));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
