public class EdgeUser {
    public NodeUser from, to;
    public double capacity_jeczmien;
    public double capacity_piwo;
    public double cost;
    public double flow;

    // Konstruktor klasy EdgeUser - tworzy nową krawędź z podanymi węzłami, pojemnościami (jęczmień, piwo) i kosztem.
    // Ustawia początkowy przepływ na 0.
    public EdgeUser(NodeUser from, NodeUser to, double capacity_piwo, double capacity_jeczmien, double cost) {
        this.from = from;
        this.to = to;
        this.capacity_piwo = capacity_piwo;
        this.capacity_jeczmien = capacity_jeczmien;
        this.cost = cost;
        this.flow = 0;
    }

    // Zwraca czytelną reprezentację krawędzi (od, do, pojemności jęczmienia i piwa, koszt, przepływ).
    @Override
    public String toString() {
        return from.id + " -> " + to.id + " | Capacity: (jęczmień " + capacity_jeczmien + ", piwo " + capacity_piwo + ")" +  "| Cost: " + cost + " | Flow: " + flow;
    }
}