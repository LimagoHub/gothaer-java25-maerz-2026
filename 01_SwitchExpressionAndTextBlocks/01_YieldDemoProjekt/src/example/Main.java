package example;

import java.awt.*;

public class Main {

    public static void main(String[] args) {

        /*
        Hier liefert yield den Wert der Expression zurück.

        Ein yield in einem switch-Statement waere ungueltig.
         */
        int x = 1;
        int value = switch (x) {
            case 1 -> 10;
            case 2 -> {
                int r = compute();
                yield r;
            }
            default -> 0;
        };

        /*

            Gueltig waere auch
         */

        int result = switch (new Object()) {
            case String s -> {
                yield s.length();
            }
            default -> 0;
        };

        /* NICHT Gueltig

        // yield ohne Block nicht erlaubt
        int result = switch (x) {
            case 1 -> yield 10; // Fehler!
            default -> 0;
        };

         */


        // Der Block muss immer einen Wert zurueckgeben
        int r = switch (x) {
            case 1 -> {
                if (x > 0) yield 10;
                else yield -10;
            }
            default -> 0;
        };

        //Wichtig: Ein Block ohne yield ist ungueltig:
        /*
        case 1 -> {
            compute();
            // kein yield → switch-Expression hätte keinen Wert
        }

         */

        /************************************
         * yield endet nur den Case-Block, nicht den ganzen Switch
         *
         * Im Gegensatz zu break hat yield keine Kontrollflussbedeutung, sondern nur „dies ist der Expression-Wert“.
         */

        // In Kombination mit Pattern Matching (Java 21+)
        String s = switch (new Object()) {
            case Point(int a, int b) -> {
                if (a == 0 && b == 0) yield "Origin";
                yield "Point";
            }
            default -> "Unknown";
        };

    }

    private static int compute() {return 42;}
}
record Point(int a, int b) {}


