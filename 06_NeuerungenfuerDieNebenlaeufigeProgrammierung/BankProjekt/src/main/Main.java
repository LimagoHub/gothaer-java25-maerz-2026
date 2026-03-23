package main;

import bank.Angestellter;
import bank.Bank;

public class Main {

    public static void main(String[] args) {
        Bank bank = new Bank();

        Angestellter angestellter[] = {
                new Angestellter(bank, 0,1, 20),
                new Angestellter(bank, 1, 2, 20),
                new Angestellter(bank, 2,0, 20)
        };

        System.out.println("Konto vorher");
        bank.kontostand();

        for (Angestellter angestell : angestellter) {
            angestell.start();
        }
    }
}
