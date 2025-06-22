import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.Polygon;

public class GraphVisualizerFX extends Application {
    public static Graph graph;

    private Canvas canvas;
    private double scaleX;
    private double scaleY;
    private final double PADDING_RATIO = 0.08;
    private final int GRID_WIDTH = 12;
    private final int GRID_HEIGHT = 12;

    // Główna metoda uruchamiająca aplikację JavaFX i ustawiająca okno oraz obsługę zmiany rozmiaru.
    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(800, 800);
        Pane root = new Pane(canvas);
        Scene scene = new Scene(root);

        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> draw();
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());
        canvas.widthProperty().addListener(resizeListener);
        canvas.heightProperty().addListener(resizeListener);

        draw();

        primaryStage.setTitle("Wizualizacja Grafu");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Rysuje całą zawartość: ćwiartki, krawędzie i węzły na canvasie.
    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double paddingX = canvas.getWidth() * PADDING_RATIO;
        double paddingY = canvas.getHeight() * PADDING_RATIO;

        scaleX = (canvas.getWidth() - 2 * paddingX) / GRID_WIDTH;
        scaleY = (canvas.getHeight() - 2 * paddingY) / GRID_HEIGHT;

        gc.save();
        gc.translate(paddingX, paddingY);

        drawCwiartki(gc);
        drawEdges(gc);
        drawNodes(gc);

        gc.restore();
    }

    // Rysuje ćwiartki (obszary) na tle grafu.
    private void drawCwiartki(GraphicsContext gc) {
        Color[] colors = {
                Color.rgb(255, 200, 200, 0.4),
                Color.rgb(200, 255, 200, 0.4),
                Color.rgb(200, 200, 255, 0.4),
                Color.rgb(255, 255, 150, 0.4)
        };
        int i = 0;
        for (Polygon poly : graph.cwiartki) {
            gc.setFill(colors[i % colors.length]);
            double[] xPoints = new double[poly.npoints];
            double[] yPoints = new double[poly.npoints];
            for (int j = 0; j < poly.npoints; j++) {
                xPoints[j] = poly.xpoints[j] * scaleX;
                yPoints[j] = poly.ypoints[j] * scaleY;
            }
            gc.fillPolygon(xPoints, yPoints, poly.npoints);
            i++;
        }
    }

    // Rysuje krawędzie grafu oraz etykiety z przepływami i pojemnościami.
    private void drawEdges(GraphicsContext gc) {
        gc.setFont(new Font("Arial", 14));
        for (EdgeUser e : graph.edgesUser) {
            double x1 = e.from.position.x * scaleX;
            double y1 = e.from.position.y * scaleY;
            double x2 = e.to.position.x * scaleX;
            double y2 = e.to.position.y * scaleY;

            double flow_jeczmien = graph.edgeJeczmien.get(e).flow;
            double flow_piwo = graph.edgePiwo.get(e).flow;
            gc.setStroke(flow_jeczmien + flow_piwo > 0 ? Color.RED : Color.DARKGRAY);
            gc.setLineWidth(2.5);
            gc.strokeLine(x1, y1, x2, y2);

            double midX = (x1 + x2) / 2;
            double midY = (y1 + y2) / 2;

            String capacityJeczmienStr = (e.capacity_jeczmien == Double.POSITIVE_INFINITY) ? "inf" : String.format("%.1f", e.capacity_jeczmien);
            String capacityPiwoStr = (e.capacity_piwo == Double.POSITIVE_INFINITY) ? "inf" : String.format("%.1f", e.capacity_piwo);
            String label = String.format("K: %.1f | PJ: %.1f/%s | PP: %.1f/%s",
                    e.cost, flow_jeczmien, capacityJeczmienStr, flow_piwo, capacityPiwoStr);

            gc.setFill(Color.BLACK);
            gc.fillText(label, midX + 5, midY - 5);
        }
    }

    // Rysuje węzły grafu oraz etykiety z nazwą, typem i wydajnością.
    private void drawNodes(GraphicsContext gc) {
        for (NodeUser node : graph.nodesUser) {
            double x = node.position.x * scaleX;
            double y = node.position.y * scaleY;

            gc.setFill(getColorForNodeType(node.type));
            gc.fillOval(x - 8, y - 8, 16, 16);

            gc.setFont(new Font("Arial", 17));
            gc.setFill(Color.BLACK);

            String label;
            if (node.type == NodeType.POLE || node.type == NodeType.BROWAR) {
                double wydajnoscPrzeliczona = graph.nodeJeczmien.get(node).wydajnosc;
                label = node.id + " (" + node.type + ", " + String.format("%.1f", wydajnoscPrzeliczona) + ")";
            } else {
                label = node.id + " (" + node.type + ")";
            }
            gc.fillText(label, x + 10, y - 10);
        }
    }

    // Zwraca kolor odpowiadający typowi węzła.
    private Color getColorForNodeType(NodeType type) {
        return switch (type) {
            case POLE -> Color.GREEN;
            case BROWAR -> Color.BROWN;
            case KARCZMA -> Color.ORANGE;
            case SKRZYZOWANIE -> Color.GRAY;
            case SUPERSOURCE -> Color.BLUE;
            case SUPERSINK -> Color.RED;
        };
    }
}