public class Node {
    public String id;
    public NodeType type;
    public double wydajnosc;
    public Point position;
    public String cwiartka;
    public double wspolczynnikWydajnosci;

    public Node n_piwo;
    public Node n_jeczmien;

    // Konstruktor klasy Node - tworzy nowy węzeł z podanym id, typem, wydajnością i pozycją.
    // Domyślnie ćwiartka to "Brak", a współczynnik wydajności to 1.0.
    public Node(String id, NodeType type, double wydajnosc, Point position) {
        this.id = id;
        this.type = type;
        this.wydajnosc = wydajnosc;
        this.position = position;
        this.cwiartka = "Brak";
        this.wspolczynnikWydajnosci = 1.0;
    }

    // Zwraca czytelną reprezentację węzła (id, typ, wydajność, ćwiartka).
    @Override
    public String toString() {
        return id + " (" + type + ", Wyd: " + wydajnosc + ", Ćwiartka: " + cwiartka + ")";
    }
}