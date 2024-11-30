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
