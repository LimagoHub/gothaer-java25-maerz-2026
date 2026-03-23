package main;

public class Main {

    public static void main(String[] args) {
        System.out.println(new Person().kontakte.nachname.length());
    }
}

class Kontakte {
    String nachname;
}

class Person {
    Kontakte kontakte;
}
