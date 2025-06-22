public class Edge {
    public Node from, to;
    public double capacity;
    public double cost;
    public double flow;

    // Konstruktor klasy Edge - tworzy nową krawędź z podanymi węzłami, pojemnością i kosztem.
    // Ustawia początkowy przepływ na 0.
    public Edge(Node from, Node to, double capacity, double cost) {
        this.from = from;
        this.to = to;
        this.capacity = capacity;
        this.cost = cost;
        this.flow = 0;
    }

    // Zwraca czytelną reprezentację krawędzi (od, do, pojemność, koszt, przepływ).
    @Override
    public String toString() {
        return from.id + " -> " + to.id + " | Capacity: " + capacity + " | Cost: " + cost + " | Flow: " + flow;
    }
}