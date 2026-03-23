package main;

import tiere.Schwein;

public class Main {
    static void main() {
        Schwein schwein = new Schwein("Piggy");
        System.out.println(schwein);
        schwein.fuettern();

        System.out.println(schwein);


        // Busywait bis der Thread fertig ist
        while(schwein.getGewicht() < 11);

        System.out.println(schwein);
    }
}
