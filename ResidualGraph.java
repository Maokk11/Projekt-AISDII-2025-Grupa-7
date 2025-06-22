import java.util.*;

public class ResidualGraph {
    public Map<Node, List<ResidualEdge>> adjacencyList = new HashMap<>();
    public List<ResidualEdge> residualEdges = new ArrayList<>();

    // Konstruktor klasy ResidualGraph - tworzy graf rezydualny na podstawie oryginalnego grafu.
    // Dodaje krawędzie rezydualne (do przodu i do tyłu) zgodnie z aktualnym przepływem.
    public ResidualGraph(Graph originalGraph) {
        for (Node node : originalGraph.nodes) {
            adjacencyList.put(node, new ArrayList<>());
        }
        for (Edge edge : originalGraph.edges) {
            double forwardCapacity = edge.capacity - edge.flow;
            if (forwardCapacity > 0) {
                ResidualEdge forwardEdge = new ResidualEdge(edge.from, edge.to, forwardCapacity, edge.cost, edge);
                residualEdges.add(forwardEdge);
                adjacencyList.get(edge.from).add(forwardEdge);
            }
            if (edge.flow > 0) {
                ResidualEdge backwardEdge = new ResidualEdge(edge.to, edge.from, edge.flow, -edge.cost, edge);
                residualEdges.add(backwardEdge);
                adjacencyList.get(edge.to).add(backwardEdge);
            }
        }
    }

    // Zwraca listę krawędzi rezydualnych wychodzących z danego węzła.
    public List<ResidualEdge> getEdgesFrom(Node node) {
        return adjacencyList.getOrDefault(node, new ArrayList<>());
    }
}