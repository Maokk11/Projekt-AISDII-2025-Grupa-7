import java.awt.Polygon;
import java.util.*;

public class Graph {
    public List<Node> nodes = new ArrayList<>();
    public List<Edge> edges = new ArrayList<>();

    public List<NodeUser> nodesUser = new ArrayList<>();
    public List<EdgeUser> edgesUser = new ArrayList<>();

    public Map<NodeUser, Node> nodePiwo = new HashMap<>();
    public Map<NodeUser, Node> nodeJeczmien = new HashMap<>();

    public Map<EdgeUser, Edge> edgePiwo = new HashMap<>();
    public Map<EdgeUser, Edge> edgeJeczmien = new HashMap<>();

    public Map<Node, List<Edge>> adjacencyList = new HashMap<>();
    public List<Polygon> cwiartki = new ArrayList<>();
    public Map<Integer, Double> wspolczynnikiCwiartek = new HashMap<>();

    // Konstruktor klasy Graph - inicjalizuje współczynniki ćwiartek.
    public Graph() {
        wspolczynnikiCwiartek.put(0, 1.5);
        wspolczynnikiCwiartek.put(1, 1.2);
        wspolczynnikiCwiartek.put(2, 1.0);
        wspolczynnikiCwiartek.put(3, 0.8);
    }

    // Dodaje węzeł typu Node do grafu i tworzy dla niego pustą listę sąsiadów.
    public void addNode(Node node) {
        nodes.add(node);
        adjacencyList.put(node, new ArrayList<>());
    }

    // Dodaje węzeł typu NodeUser do grafu, tworzy odpowiadające mu węzły jęczmienne i piwne.
    // Jeśli to browar, łączy je krawędzią.
    public void addNode(NodeUser node) {
        nodeJeczmien.put(node, new Node(node.id + "[J]", node.type, node.wydajnosc, node.position));
        nodePiwo.put(node, new Node(node.id + "[P]", node.type, node.wydajnosc, node.position));
        nodesUser.add(node);

        addNode(nodePiwo.get(node));
        addNode(nodeJeczmien.get(node));

        if (node.type == NodeType.BROWAR) {
            addEdge(new Edge(nodeJeczmien.get(node), nodePiwo.get(node), node.wydajnosc, 0));
        }
    }

    // Dodaje listę węzłów typu NodeUser do grafu.
    public void addNodes(List<NodeUser> nodes) {
        for (NodeUser n : nodes) {
            addNode(n);
        }
    }

    // Dodaje krawędź typu Edge do grafu i do listy sąsiedztwa.
    public void addEdge(Edge edge) {
        edges.add(edge);
        adjacencyList.get(edge.from).add(edge);
    }

    // Dodaje krawędź typu EdgeUser do grafu, tworząc dwie krawędzie (jęczmienną i piwną).
    public void addEdge(EdgeUser edge) {
        Edge jeczmieniowa = new Edge(nodeJeczmien.get(edge.from), nodeJeczmien.get(edge.to), edge.capacity_jeczmien, edge.cost);
        Edge piwna = new Edge(nodePiwo.get(edge.from), nodePiwo.get(edge.to), edge.capacity_piwo, edge.cost);

        addEdge(jeczmieniowa);
        addEdge(piwna);

        edgeJeczmien.put(edge, jeczmieniowa);
        edgePiwo.put(edge, piwna);

        edgesUser.add(edge);
    }

    // Zwraca listę krawędzi wychodzących z danego węzła.
    public List<Edge> getEdgesFrom(Node node) {
        System.err.println(node);
        return adjacencyList.get(node);
    }

    // Dodaje nową ćwiartkę (obszar) do listy ćwiartek.
    public void addCwiartka(Polygon cwiartka) {
        cwiartki.add(cwiartka);
    }

    // Przypisuje węzłom odpowiednie ćwiartki na podstawie ich położenia.
    public void assignCwiartki() {
        for (Node node : nodes) {
            if (node.type == NodeType.POLE) {
                boolean assigned = false;
                for (int i = 0; i < cwiartki.size(); i++) {
                    if (isPointInPolygon(node.position, cwiartki.get(i))) {
                        node.cwiartka = "Ćwiartka " + (i + 1);
                        node.wspolczynnikWydajnosci = wspolczynnikiCwiartek.get(i);
                        node.wydajnosc = node.wydajnosc * node.wspolczynnikWydajnosci;
                        assigned = true;
                        break;
                    }
                }
                if (!assigned) {
                    node.cwiartka = "Ćwiartka bazowa";
                    node.wspolczynnikWydajnosci = 1.0;
                }
            }
        }
    }

    // Sprawdza, czy wszystkie pola mają przypisaną ćwiartkę.
    public void validateCwiartki() {
        for (Node node : nodes) {
            if ((node.type == NodeType.POLE)
                    && node.cwiartka.equals("Brak")) {
                System.err.println("UWAGA: " + node.id + " nie ma przypisanej ćwiartki!");
            }
        }
    }

    // Sprawdza, czy punkt znajduje się w danym wielokącie (ćwiartce).
    private boolean isPointInPolygon(Point point, Polygon polygon) {
        int n = polygon.npoints;
        boolean inside = false;
        double x = point.x, y = point.y;
        double[] xpoints = new double[n];
        double[] ypoints = new double[n];
        for (int i = 0; i < n; i++) {
            xpoints[i] = polygon.xpoints[i];
            ypoints[i] = polygon.ypoints[i];
        }
        int j = n - 1;
        // (0,n-1)
        for (int i = 0; i < n; i++) {
            if ((ypoints[i] > y) != (ypoints[j] > y) &&
                (x < (xpoints[j] - xpoints[i]) * (y - ypoints[i]) / (ypoints[j] - ypoints[i]) + xpoints[i])) {
                inside = !inside;
            }
            j = i;
        }
        return inside;
    }

    // Wypisuje na konsolę wszystkie węzły i krawędzie grafu wraz z informacjami.
    public void printGraph() {
        System.out.println("\n--- WĘZŁY ---");
        for (Node node : nodes) {
            String wydajnoscInfo;
            if (node.type == NodeType.POLE) {
                double wydajnoscBazowa = node.wydajnosc / node.wspolczynnikWydajnosci;
                wydajnoscInfo = String.format("Wyd: %.1f (bazowa: %.1f × %.1f)",
                        node.wydajnosc, wydajnoscBazowa, node.wspolczynnikWydajnosci);
            } else {
                wydajnoscInfo = "Wyd: " + node.wydajnosc;
            }
            System.out.printf("%s | Typ: %-12s | %s | Ćwiartka: %s%n",
                    node.id, node.type, wydajnoscInfo, node.cwiartka);
        }
        System.out.println("\n--- KRAWĘDZIE ---");
        for (Edge edge : edges) {
            System.out.println(edge);
        }
    }

    // Aktualizuje pojemności krawędzi wychodzących z supersource na podstawie wydajności pól.
    public void updateSourceEdgesCapacities() {
        for (Edge edge : edges) {
            if (edge.from.type == NodeType.SUPERSOURCE && edge.to.type == NodeType.POLE) {
                edge.capacity = edge.to.wydajnosc;
            }
        }

        for (EdgeUser edge : edgesUser) {
            if (edge.from.type == NodeType.SUPERSOURCE && edge.to.type == NodeType.POLE) {
                double nowaWydajnosc = nodeJeczmien.get(edge.to).wydajnosc;
                edge.capacity_jeczmien = nowaWydajnosc;
            }
        }
    }
}
