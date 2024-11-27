package it.unibs.pajc;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

class Trapezoid extends GameFieldObject {
    private List<Line2D> edges;

    public Trapezoid(int[] xPoints, int[] yPoints) {
        edges = new ArrayList<>();
        for (int i = 0; i < xPoints.length; i++) {
            int next = (i + 1) % xPoints.length; // Ensure wrapping around to the first point
            edges.add(new Line2D.Double(xPoints[i], yPoints[i], xPoints[next], yPoints[next]));
        }
    }

    public List<Line2D> getEdges() {
        return edges;
    }
}