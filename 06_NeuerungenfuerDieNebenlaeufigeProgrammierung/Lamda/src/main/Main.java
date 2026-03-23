package main;

public class Main {

    public static void main(String[] args) {
        new Main().run();

    }

    private void run() {

        final int ab = 10;

        Peter peter = x -> 42;
        System.out.println(peter.babara(ab));

        Operation add = (a, b)->a + b ;
        Operation sub = (a, b)->a - b;

        double erbegnis = add.calculate(10.0, 20.0);

        Peter p = getPeter();
        p.babara(10);
    }

    public Peter getPeter() {
        int z = 10;
        return x->x+x-z;
    }

    int foo(int x) {
        return 42;
    }


}

/*

    runnable  void(void);
    consumer  void(T);
    supplier  T(void);
    function  T(T);
 */

interface Operation{
    double calculate    (double a, double b);
}



@FunctionalInterface
interface Peter {
    int babara(int x);

}
