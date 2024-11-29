package it.unibs.pajc;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

class Trapezoid extends GameFieldObject {

    public Trapezoid(int[] xPoints, int[] yPoints) {
        super();

        Path2D path = new Path2D.Double();
        path.moveTo(xPoints[0], yPoints[0]);
        for (int i = 1; i < 4; i++) {
            path.lineTo(xPoints[i], yPoints[i]);
        }
        path.closePath();

        this.shape = path;
    }

}