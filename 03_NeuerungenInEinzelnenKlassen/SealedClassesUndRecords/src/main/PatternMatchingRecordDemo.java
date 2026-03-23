package main;
public class PatternMatchingRecordDemo {

    // 1) Eine kleine, data-orientierte Hierarchie mit Records
    sealed interface Shape permits Circle, Rectangle, Square {}

    record Circle(double radius) implements Shape {}

    record Rectangle(double width, double height) implements Shape {}

    record Square(double side) implements Shape {}

    public static void main(String[] args) {
        Shape s1 = new Circle(2.0);
        Shape s2 = new Rectangle(3.0, 4.0);
        Shape s3 = new Square(5.0);

        System.out.println("=== Pattern Matching für instanceof ===");
        printShapeInfoInstanceof(s1);
        printShapeInfoInstanceof(s2);
        printShapeInfoInstanceof(s3);

        System.out.println("\n=== Pattern Matching für switch ===");
        printShapeAreaSwitch(s1);
        printShapeAreaSwitch(s2);
        printShapeAreaSwitch(s3);

        System.out.println("\n=== Record-Dekonstruktion in Patterns ===");
        describeShapeDeconstruction(s1);
        describeShapeDeconstruction(s2);
        describeShapeDeconstruction(s3);
    }

    // 2) Pattern Matching für instanceof
    private static void printShapeInfoInstanceof(Shape shape) {
        if (shape instanceof Circle c) {
            System.out.println("Es ist ein Circle mit radius = " + c.radius());
        } else if (shape instanceof Rectangle r) {
            System.out.println("Es ist ein Rectangle mit width = " + r.width()
                    + ", height = " + r.height());
        } else if (shape instanceof Square sq) {
            System.out.println("Es ist ein Square mit side = " + sq.side());
        } else {
            System.out.println("Unbekannte Shape: " + shape);
        }
    }

    // 3) Pattern Matching für switch (Pattern Switch)
    private static void printShapeAreaSwitch(Shape shape) {
        double area = switch (shape) {
            case Circle c      -> Math.PI * c.radius() * c.radius();
            case Rectangle r   -> r.width() * r.height();
            case Square sq     -> sq.side() * sq.side();
        };

        System.out.printf("Fläche von %s = %.2f%n", shape, area);
    }

    // 4) Record-Dekonstruktion in Patterns (Record Patterns)
    private static void describeShapeDeconstruction(Shape shape) {
        String description = switch (shape) {
            // Record-Pattern: Circle(radius)
            case Circle(double radius) ->
                    "Circle mit radius=" + radius;

            // Record-Pattern: Rectangle(width, height)
            case Rectangle(double w, double h) ->
                    "Rectangle w=" + w + ", h=" + h;

            // Record-Pattern mit Binding in einem Schritt
            case Square(double side) ->
                    "Square side=" + side;
        };

        System.out.println("Beschreibung: " + description);
    }
}
