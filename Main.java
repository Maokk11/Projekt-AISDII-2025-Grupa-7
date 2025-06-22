import java.awt.Polygon;
import java.io.*;
import java.util.*;

import javafx.application.Application;

public class Main {
    // Główna metoda programu - wczytuje graf, uruchamia algorytmy przepływu i wizualizację.
    public static void main(String[] args) {
        String filename = (args.length > 0) ? args[0] : "graph.txt";
        Graph graph;
        try {
            graph = loadGraphFromFile(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        graph.assignCwiartki();
        graph.validateCwiartki();
        graph.updateSourceEdgesCapacities();
        Node superSource = null, superSink = null;
        for (NodeUser node : graph.nodesUser) {
            if (node.type == NodeType.SUPERSOURCE)
                superSource = graph.nodeJeczmien.get(node);
            if (node.type == NodeType.SUPERSINK)
                superSink = graph.nodePiwo.get(node);
        }

        System.out.println("=====================================");
        System.out.println("GRAF POCZĄTKOWY:");
        graph.printGraph();
        System.out.println("=====================================");

        // Uruchamia algorytm Edmondsa-Karpa do wyznaczenia maksymalnego przepływu.
        double maxFlow = edmondsKarp(graph, superSource, superSink);

        System.out.println("=====================================");
        System.out.println("WYNIKI PO ALGORYTMIE EDMONDSA-KARPA:");
        System.out.println("Maksymalny przepływ: " + maxFlow);

        System.out.println("\nPrzepływy na krawędziach:");
        double totalCost = 0;
        for (Edge e : graph.edges) {
            if (e.flow > 0) {
                System.out.printf("%s -> %s | przepływ: %.1f | koszt: %.1f\n", e.from.id, e.to.id, e.flow, e.cost);
                totalCost += e.cost;
            }
        }
        System.out.println("\nŁączny koszt: " + totalCost);
        System.out.println("=====================================");

        // Minimalizuje koszt przepływu przy zachowaniu maksymalnego przepływu.
        minimizeCostFlow(graph, superSource, superSink);

        System.out.println("=====================================");
        System.out.println("WYNIKI PO MINIMALIZACJI KOSZTU:");
        System.out.println("Maksymalny przepływ: " + maxFlow + " (niezmieniony)");

        System.out.println("\nZoptymalizowane przepływy:");
        totalCost = 0;
        for (Edge e : graph.edges) {
            if (e.flow > 0) {
                System.out.printf("%s -> %s | przepływ: %.1f | koszt: %.1f\n", e.from.id, e.to.id, e.flow, e.cost);
                totalCost += e.cost;
            }
        }
        System.out.println("\nŁączny koszt po optymalizacji: " + totalCost);
        System.out.println("=====================================");

        // Uruchamia wizualizację grafu w JavaFX.
        GraphVisualizerFX.graph = graph;
        Application.launch(GraphVisualizerFX.class);
    }

    // Wczytuje graf z pliku tekstowego.
    public static Graph loadGraphFromFile(String filename) throws IOException {
        Graph graph = new Graph();
        Map<String, NodeUser> nodeMap = new HashMap<>();

        InputStream is = Main.class.getClassLoader().getResourceAsStream(filename);
        if (is == null) throw new FileNotFoundException(filename + " (Nie można odnaleźć określonego pliku)");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;
        String section = "";
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                if (line.startsWith("# Nodes")) section = "nodes";
                else if (line.startsWith("# Edges")) section = "edges";
                else if (line.startsWith("# Cwiartki")) section = "cwiartki";
                continue;
            }
            String[] t = line.split("\\s+");
            switch (section) {
                case "nodes":
                    if (t.length < 5) break;
                    String id = t[0];
                    NodeType type = NodeType.valueOf(t[1]);
                    double wydajnosc = Double.parseDouble(t[2]);
                    double x = Double.parseDouble(t[3]);
                    double y = Double.parseDouble(t[4]);
                    NodeUser node = new NodeUser(id, type, wydajnosc, new Point(x, y));
                    graph.addNode(node);
                    nodeMap.put(id, node);
                    break;
                case "edges":
                    if (t.length < 5) break;
                    NodeUser from = nodeMap.get(t[0]);
                    NodeUser to = nodeMap.get(t[1]);
                    double capPiwo = (t[2].equalsIgnoreCase("infinite") || t[2].equals("∞"))
                            ? Double.POSITIVE_INFINITY
                            : Double.parseDouble(t[2]);
                    double capJeczmien = (t[3].equalsIgnoreCase("infinite") || t[3].equals("∞"))
                            ? Double.POSITIVE_INFINITY
                            : Double.parseDouble(t[3]);
                    double cost = Double.parseDouble(t[4]);
                    graph.addEdge(new EdgeUser(from, to, capPiwo, capJeczmien, cost));
                    break;
                case "cwiartki":
                    if (t.length < 8) break;
                    int[] xs = new int[4];
                    int[] ys = new int[4];
                    for (int j = 0; j < 4; j++) {
                        xs[j] = (int) Double.parseDouble(t[j * 2]);
                        ys[j] = (int) Double.parseDouble(t[j * 2 + 1]);
                    }
                    graph.addCwiartka(new Polygon(xs, ys, 4));
                    break;
            }
        }
        return graph;
    }

    // Implementacja algorytmu Edmondsa-Karpa do wyznaczania maksymalnego przepływu w grafie.
    public static double edmondsKarp(Graph graph, Node source, Node sink) {
        double maxFlow = 0;
        Map<Edge, Edge> reverseEdges = new HashMap<>();

        while (true) {
            Map<Node, Edge> parentMap = new HashMap<>();
            Queue<Node> queue = new LinkedList<>();
            queue.add(source);

            Set<Node> visited = new HashSet<>();
            visited.add(source);

            while (!queue.isEmpty()) {
                Node current = queue.poll();

                for (Edge edge : graph.getEdgesFrom(current)) {
                    Node next = edge.to;
                    double residual = edge.capacity - edge.flow;

                    if (residual > 0 && !visited.contains(next)) {
                        visited.add(next);
                        parentMap.put(next, edge);
                        queue.add(next);
                    }
                }
            }

            if (!parentMap.containsKey(sink)) {
                break;
            }

            double bottleneck = Double.POSITIVE_INFINITY;
            for (Node v = sink; v != source; ) {
                Edge edge = parentMap.get(v);
                bottleneck = Math.min(bottleneck, edge.capacity - edge.flow);
                v = edge.from;
            }

            for (Node v = sink; v != source; ) {
                Edge edge = parentMap.get(v);
                edge.flow += bottleneck;

                Edge reverseEdge = reverseEdges.computeIfAbsent(edge, e -> new Edge(e.to, e.from, 0, e.cost));
                reverseEdge.flow -= bottleneck;

                v = edge.from;
            }

            maxFlow += bottleneck;
        }

        return maxFlow;
    }

    // Minimalizuje koszt przepływu w grafie przy zachowaniu maksymalnego przepływu (cykle ujemne).
    public static void minimizeCostFlow(Graph graph, Node source, Node sink) {
        boolean hasNegativeCycle;

        do {
            hasNegativeCycle = false;
            ResidualGraph residualGraph = new ResidualGraph(graph);

            Map<Node, Double> distance = new HashMap<>();
            Map<Node, ResidualEdge> predecessor = new HashMap<>();

            for (Node node : graph.nodes) {
                distance.put(node, Double.POSITIVE_INFINITY);
            }
            distance.put(source, 0.0);

            for (int i = 0; i < graph.nodes.size() - 1; i++) {
                for (ResidualEdge edge : residualGraph.residualEdges) {
                    if (distance.get(edge.from) != Double.POSITIVE_INFINITY &&
                            distance.get(edge.from) + edge.cost < distance.get(edge.to)) {
                        distance.put(edge.to, distance.get(edge.from) + edge.cost);
                        predecessor.put(edge.to, edge);
                    }
                }
            }

            Node cycleSeed = null;
            for (ResidualEdge edge : residualGraph.residualEdges) {
                if (distance.get(edge.from) != Double.POSITIVE_INFINITY &&
                        distance.get(edge.from) + edge.cost < distance.get(edge.to)) {
                    cycleSeed = edge.to;
                    break;
                }
            }

            if (cycleSeed != null) {
                Node current = cycleSeed;
                for (int i = 0; i < graph.nodes.size(); i++) {
                    current = predecessor.get(current).from;
                }

                List<ResidualEdge> cycle = new ArrayList<>();
                Node start = current;
                do {
                    ResidualEdge edge = predecessor.get(current);
                    cycle.add(edge);
                    current = edge.from;
                } while (current != start);

                double maxFlow = Double.POSITIVE_INFINITY;
                for (ResidualEdge edge : cycle) {
                    maxFlow = Math.min(maxFlow, edge.capacity);
                }

                for (ResidualEdge edge : cycle) {
                    Edge originalEdge = edge.originalEdge;
                    if (originalEdge.from == edge.from) {
                        originalEdge.flow += maxFlow;
                    } else {
                        originalEdge.flow -= maxFlow;
                    }
                }

                hasNegativeCycle = true;
            }
        } while (hasNegativeCycle);
    }
}