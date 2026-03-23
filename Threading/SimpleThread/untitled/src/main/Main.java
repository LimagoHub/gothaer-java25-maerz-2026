package main;

import mythread.MyThread;

public class Main {
    public static void main() throws InterruptedException{
        MyThread hund = new MyThread("Wau");
        MyThread katze = new MyThread("miau");
        MyThread maus = new MyThread("piep");


        Thread t1 = new Thread(hund);
        Thread t2 = new Thread(katze);
        Thread t3 = new Thread(maus);

        t1.start();
        t2.start();
        t3.start();
    }
}
