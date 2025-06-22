public class Point {
    public double x, y;

    // Konstruktor klasy Point - tworzy punkt o zadanych współrzędnych x i y.
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Zwraca tekstową reprezentację punktu (x, y).
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}