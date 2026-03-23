package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {

        new Main().go();
    }

   /* private void go() {
        try {
            ExecutorService executor = Executors.newFixedThreadPool(4);
            for(int i = 0; i < 10; i++) {
                executor.execute(new MyWorker());
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

            System.out.println("Fertig");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
*/


    private void go() {
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(22);
            for (int i = 0; i < 10; i++) {
                futures.add(executor.submit(new MyFutureWorker()));
            }
            executor.shutdown();
            for (Future<Integer> future : futures) {
                System.out.println(future.get());
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    class MyWorker implements Runnable {

        Random random = new Random();
        @Override
        public void run() {
            try {
                Thread.sleep(random.nextInt(1000));
                System.out.println(Thread.currentThread().getId() + " trminated");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class MyFutureWorker implements Callable<Integer> {

        Random random = new Random();

        @Override
        public Integer call() throws Exception {
            int i = 0;
            try {
                Thread.sleep(i = random.nextInt(1000));
                System.out.println(Thread.currentThread().getId() + " trminated");
                return i;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
