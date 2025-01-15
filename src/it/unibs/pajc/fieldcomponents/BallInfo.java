package it.unibs.pajc.fieldcomponents;

/**
 * Classe che rappresenta le informazioni di una pallina.
 * Questa classe è stata creata per evitare di passare informazioni inutili alla view.
 */
public class BallInfo {
    private final double x;
    private final double y;
    private final int radius;
    private final int number;



    public BallInfo(double x, double y, int radius, int number) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.number = number;

    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }

    public int getNumber() {
        return number;
    }
}
