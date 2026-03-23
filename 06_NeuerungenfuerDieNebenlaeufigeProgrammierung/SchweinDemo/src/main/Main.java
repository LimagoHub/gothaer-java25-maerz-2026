package main;

import tiere.Schwein;

public class Main {

    public static void main(String[] args) {
        Schwein piggy = new Schwein("Miss Piggy");

        System.out.println(piggy);

        piggy.fuettern();

        System.out.println(piggy);

        while( piggy.getGewicht() < 11);


        System.out.println(piggy);
    }
}
