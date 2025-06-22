public class ResidualEdge {
    public Node from, to;
    public double capacity;
    public double cost;
    public Edge originalEdge;

    // Konstruktor klasy ResidualEdge - tworzy krawędź rezydualną na podstawie podanych parametrów i oryginalnej krawędzi.
    public ResidualEdge(Node from, Node to, double capacity, double cost, Edge originalEdge) {
        this.from = from;
        this.to = to;
        this.capacity = capacity;
        this.cost = cost;
        this.originalEdge = originalEdge;
    }
}