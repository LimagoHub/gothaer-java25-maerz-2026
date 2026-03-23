package bank;

public class Angestellter extends Thread {

    private Bank bank;
    private int von;
    private int nach;
    private int betrag;

    public Angestellter(final Bank bank, final int von, final int nach, final int betrag) {
        this.bank = bank;
        this.von = von;
        this.nach = nach;
        this.betrag = betrag;
    }

    public void run() {
        bank.ueberweisen(von, nach, betrag);
    }
}
