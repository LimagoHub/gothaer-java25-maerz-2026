package bank;

public class Bank {

    private volatile int konten[] = {30, 50, 100};


    public void kontostand() {
        System.out.println("Konten:");
        for (int i = 0; i < konten.length; i++) {
            System.out.println(String.format("Konto Nr. %d hat den Saldo %s", i, konten[i]));
        }
    }

    // synchronized + Thread.sleep() war in Java 21 ein Pinning-Problem:
    // Der Carrier-Thread virtueller Threads blieb blockiert.
    // Seit Java 24 (JEP 491) ist dies behoben – synchronized verursacht kein Pinning mehr.
    public synchronized void ueberweisen(int von, int nach, int betrag) {

            try {
                int zettel = konten[von];
                zettel -= betrag;
                Thread.sleep((long)(Math.random()* 2000));  // Java 24+: kein Pinning
                konten[von] = zettel;
                konten[nach] += betrag;
                kontostand();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }



    }
}
