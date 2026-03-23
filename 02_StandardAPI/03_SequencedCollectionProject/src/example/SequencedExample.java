package example;

import java.util.*;

public class SequencedExample {

    public static void main(String[] args) {

        // ---------------------------------------------
        // 1) Klassische ArrayList
        // ---------------------------------------------
        List<String> classicList = new ArrayList<>(List.of("A", "B", "C"));

        System.out.println("Original ArrayList:     " + classicList);

        // letztes Element löschen (klassisch über Index)
        classicList.remove(classicList.size() - 1);

        System.out.println("Nach remove(last):      " + classicList);
        System.out.println();


        // ---------------------------------------------
        // 2) Dasselbe mit einer SequencedCollection
        //    (ArrayList implementiert SequencedCollection seit Java 21)
        // ---------------------------------------------
        SequencedCollection<String> seqList =
                new ArrayList<>(List.of("A", "B", "C"));

        System.out.println("Original SequencedList: " + seqList);

        // letztes Element löschen (Java 21: removeLast())
        seqList.removeLast();

        System.out.println("Nach removeLast():      " + seqList);
    }
}
