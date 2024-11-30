package it.unibs.pajc;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

public class Pocket extends GameFieldObject {

    private final int radius;

    public Pocket(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.shape = new Area(new Ellipse2D.Double(-radius, -radius, radius * 2, radius * 2));
    }

    public int getRadius() {
        return radius;
    }

}
