package it.unibs.pajc.fieldcomponents;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

public class GameFieldObject {

    public double x, y; // Position

    protected Shape shape;

    public GameFieldObject(Shape shape) {
        this.shape = shape;
        this.x = 0;
        this.y = 0;
    }

    public GameFieldObject()
    {

    }

    public Shape getShape() {
        AffineTransform t = new AffineTransform();
        t.translate(getX(), getY());

        return t.createTransformedShape(shape);
    }

    /**
     * Prima di creare aree complesse, viene controllata la collisione tramite rettangoli.
     * Se gli oggetti sono distanti l'uno dall'altro, risparmia calcoli di aree inutili.
     */
    public boolean checkCollision(GameFieldObject o) {
        Rectangle aabb1 = this.getShape().getBounds();
        Rectangle aabb2 = o.getShape().getBounds();

        if (!aabb1.intersects(aabb2)) {
            return false;
        }

        //Se collidono i rettangoli, si passa al check vero di collisione.
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

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }


}
