package it.unibs.pajc;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

public class GameFieldObject {

    public double x, y; // Position
    private double vx, vy;

    protected Shape shape;

    public Shape getShape() {
        AffineTransform t = new AffineTransform();
        t.translate(getX(), getY());

        return t.createTransformedShape(shape);
    }

    public boolean checkCollision(GameFieldObject o) {
        // Assumendo che gli oggetti siano palline
        if (o instanceof Ball && this instanceof Ball) {
            Ball ball1 = (Ball) this;
            Ball ball2 = (Ball) o;

            // Calcolo della distanza tra i centri
            double dx = ball2.getX() - ball1.getX();
            double dy = ball2.getY() - ball1.getY();
            double distanceSquared = dx * dx + dy * dy;

            // Somma dei raggi delle due palline
            double radiusSum = ball1.getBallRadius() + ball2.getBallRadius();

            // Controllo collisione
            return distanceSquared <= radiusSum * radiusSum;
        }

        // Per altre forme, usa Area come fallback
        Area a = new Area(this.getShape());
        Area objArea = new Area(o.getShape());

        a.intersect(objArea);

        return !a.isEmpty();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
