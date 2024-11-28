package it.unibs.pajc;

import java.awt.Color;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import static it.unibs.pajc.CostantiStatiche.*;

class Ball extends GameFieldObject {
    //private double x, y; // Position
    private double vx, vy; // Velocity
    private final int radius = 15;
    private final Color color;
    private final int number; // it.unibs.pajc.Ball number
    private double accumulatedDistance = 0; // Distance for rotation effect

    // Array per memorizzare i colori delle palline in base al loro numero
    private static final Color[] BALL_COLORS = {
            Color.WHITE, // Palla 0 (bianca)
            new Color(255, 255, 0), // Palla 1 (gialla)
            new Color(0, 0, 255), // Palla 2 (blu)
            new Color(255, 0, 0), // Palla 3 (rossa)
            new Color(128, 0, 128), // Palla 4 (viola)
            new Color(255, 165, 0), // Palla 5 (arancione)
            new Color(0, 128, 0), // Palla 6 (verde)
            new Color(128, 0, 0), // Palla 7 (bordeaux)
            new Color(0, 0, 0), // Palla 8 (nera)
            new Color(255, 255, 0), // Palla 9 (gialla striata)
            new Color(0, 0, 255), // Palla 10 (blu striata)
            new Color(255, 0, 0), // Palla 11 (rossa striata)
            new Color(128, 0, 128), // Palla 12 (viola striata)
            new Color(255, 165, 0), // Palla 13 (arancione striata)
            new Color(0, 128, 0), // Palla 14 (verde striata)
            new Color(128, 0, 0) // Palla 15 (bordeaux striata)
    };

    public Ball(double x, double y, double vx, double vy, int number) {
        super();
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.number = number;
        this.color = BALL_COLORS[number];// Imposta il colore basato sul numero della pallina
        this.shape = new Area(new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2));
    }

    public void updatePosition() {
        x += vx;
        y += vy;

        // Update accumulated distance for rotation effect
        accumulatedDistance += Math.sqrt(vx * vx + vy * vy);

        // Apply friction
        double friction = 0.98; // Increased friction for more realistic slowdown
        vx *= friction;
        vy *= friction;

        // Stop ball if velocity is very low
        if (Math.abs(vx) < 0.1)
            vx = 0;
        if (Math.abs(vy) < 0.1)
            vy = 0;
    }

    /*
     * public void checkBounds(int width, int height) {
     * // Bounce off walls
     * if (x - radius < BORDER_WIDTH || x + radius > width - BORDER_WIDTH) {
     * vx = -vx;
     * x = Math.max(radius + BORDER_WIDTH, Math.min(width - radius - BORDER_WIDTH,
     * x));
     * }
     * if (y - radius < BORDER_WIDTH || y + radius > height - BORDER_WIDTH) {
     * vy = -vy;
     * y = Math.max(radius + BORDER_WIDTH, Math.min(height - radius - BORDER_WIDTH,
     * y));
     * }
     * 
     * // Check for pocket collisions
     * checkPocketCollision(width, height);
     * }
     * 
     */

    public void checkBounds(List<Trapezoid> trapezoids) {
        if (trapezoids != null) {
            for (Trapezoid trapezoid : trapezoids) {
                for (Line2D edge : trapezoid.getEdges()) {
                    if (isCollidingWithEdge(edge)) {
                        handleCollisionWithEdge(edge);
                    }
                }
            }
        }
    }

    private boolean isCollidingWithEdge(Line2D edge) {
        double dx = edge.getX2() - edge.getX1();
        double dy = edge.getY2() - edge.getY1();
        double lengthSquared = dx * dx + dy * dy;

        // Closest point on edge
        double t = ((x - edge.getX1()) * dx + (y - edge.getY1()) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t)); // Clamp t to [0, 1]

        double closestX = edge.getX1() + t * dx;
        double closestY = edge.getY1() + t * dy;

        double distSquared = (x - closestX) * (x - closestX) + (y - closestY) * (y - closestY);
        if (distSquared <= radius * radius) {
            return true;
        }

        // Check endpoints
        double distToStart = (x - edge.getX1()) * (x - edge.getX1()) + (y - edge.getY1()) * (y - edge.getY1());
        double distToEnd = (x - edge.getX2()) * (x - edge.getX2()) + (y - edge.getY2()) * (y - edge.getY2());

        return distToStart <= radius * radius || distToEnd <= radius * radius;
    }

    private void handleCollisionWithEdge(Line2D edge) {
        // Calculate the edge's normal vector
        double dx = edge.getX2() - edge.getX1();
        double dy = edge.getY2() - edge.getY1();
        double length = Math.sqrt(dx * dx + dy * dy);
        double nx = -dy / length; // Normal vector (perpendicular to edge)
        double ny = dx / length;

        // Reflect velocity using the edge normal
        double dotProduct = vx * nx + vy * ny;
        vx -= 2 * dotProduct * nx;
        vy -= 2 * dotProduct * ny;

        // Move the ball outside the collision area
        double overlap = radius
                - Math.sqrt((x - edge.getX1()) * (x - edge.getX1()) + (y - edge.getY1()) * (y - edge.getY1()));
        if (overlap > 0) {
            x += overlap * nx;
            y += overlap * ny;
        }
    }

    private void checkPocketCollision(int width, int height) {
        int pocketCenterX, pocketCenterY;

        // Check top left pocket
        pocketCenterX = BORDER_WIDTH + POCKET_RADIUS;
        pocketCenterY = BORDER_WIDTH + POCKET_RADIUS;
        if (isInsidePocket(x, y, pocketCenterX, pocketCenterY)) {
            // TODO: balls.remove(this);
        }

        // Check other pockets similarly
        // ... (implement checks for other pockets)
    }

    private boolean isInsidePocket(double x, double y, double pocketX, double pocketY) {
        double dx = x - pocketX;
        double dy = y - pocketY;
        double distanceSquared = dx * dx + dy * dy;

        return distanceSquared <= POCKET_RADIUS * POCKET_RADIUS;
    }

    public boolean isColliding(Ball other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double distanceSquared = dx * dx + dy * dy;
        double radiusSum = this.radius + other.radius;

        // Check if the distance between centers is less than or equal to the sum of the
        // radii
        return distanceSquared <= radiusSum * radiusSum;
    }

    public void resolveCollision(Ball other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance == 0) {
            distance = 0.1; // Avoid division by zero
        }

        double overlap = this.radius + other.radius - distance;
        if (overlap > 0) {
            double correctionFactor = overlap / 2.0 / distance;
            double correctionX = dx * correctionFactor;
            double correctionY = dy * correctionFactor;

            this.x -= correctionX;
            this.y -= correctionY;
            other.x += correctionX;
            other.y += correctionY;
        }

        double nx = dx / distance;
        double ny = dy / distance;

        double tx = -ny;
        double ty = nx;

        double v1n = this.vx * nx + this.vy * ny;
        double v1t = this.vx * tx + this.vy * ty;
        double v2n = other.vx * nx + other.vy * ny;
        double v2t = other.vx * tx + other.vy * ty;

        double newV1n = v2n;
        double newV2n = v1n;

        double newV1t = v1t;
        double newV2t = v2t;

        this.vx = newV1n * nx + newV1t * tx;
        this.vy = newV1n * ny + newV1t * ty;
        other.vx = newV2n * nx + newV2t * tx;
        other.vy = newV2n * ny + newV2t * ty;
    }

    public List<Object> getShapeComponents() {
        List<Object> components = new ArrayList<>();

        // Corpo principale della pallina
        Ellipse2D body = new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
        components.add(new ShapeComponent(body, color));

        // Cerchio bianco per il numero (se non è la palla bianca)
        if (number > 0) {
            int whiteCircleDiameter = radius; // Diametro del cerchio bianco
            double whiteCircleX = x - whiteCircleDiameter / 2.0;
            double whiteCircleY = y - whiteCircleDiameter / 2.0;

            Ellipse2D whiteCircle = new Ellipse2D.Double(whiteCircleX, whiteCircleY, whiteCircleDiameter,
                    whiteCircleDiameter);
            components.add(new ShapeComponent(whiteCircle, Color.WHITE));

            // Aggiungi il numero al centro del cerchio bianco
            components.add(
                    new TextComponent(String.valueOf(number), Color.BLACK, x, y));
        }
        // Bande per le palline dalla 9 alla 15
        if (number >= 9) {
            // Crea un'area di clipping limitata al cerchio della pallina
            Ellipse2D clippingArea = new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);

            // Riduci l'altezza delle bande
            int bandHeight = radius / 3; // Altezza ridotta delle bande (prima era radius / 2)

            // Banda superiore (clipping limitato)
            Rectangle bandTop = new Rectangle((int) (x - radius), (int) (y - radius), radius * 2, bandHeight);
            Shape bandTopClipped = new java.awt.geom.Area(clippingArea);
            ((java.awt.geom.Area) bandTopClipped).intersect(new java.awt.geom.Area(bandTop));

            // Banda inferiore (clipping limitato)
            Rectangle bandBottom = new Rectangle((int) (x - radius), (int) (y + radius - bandHeight), radius * 2,
                    bandHeight);
            Shape bandBottomClipped = new java.awt.geom.Area(clippingArea);
            ((java.awt.geom.Area) bandBottomClipped).intersect(new java.awt.geom.Area(bandBottom));

            // Aggiungi le bande come componenti grafici
            components.add(new ShapeComponent(bandTopClipped, Color.WHITE));
            components.add(new ShapeComponent(bandBottomClipped, Color.WHITE));
        }

        // Riflesso
        RadialGradientPaint gradient = new RadialGradientPaint(
                new Point2D.Double(x, y - radius * 0.7), // Centro del riflesso
                radius * 0.6f, // Raggio del riflesso
                new float[] { 0f, 1f }, // Posizioni dei colori nel gradiente
                new Color[] { new Color(255, 255, 255, 200), new Color(255, 255, 255, 0) } // Bianco opaco al centro,
                                                                                           // trasparente ai bordi
        );
        Ellipse2D reflection = new Ellipse2D.Double(
                x - radius * 0.4, // Posizione X del cerchio
                y - radius * 1.1, // Posizione Y del cerchio
                radius * 0.8, // Diametro del cerchio
                radius * 0.8 // Diametro del cerchio
        );
        components.add(new ShapeComponent(reflection, gradient));

        return components;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public double getVx() {
        return vx;
    }
    public double getVy() {
        return vy;
    }
    public void applyVelocity(double[] velocity) {
        this.vx = velocity[0];
        this.vy = velocity[1];
    }

    public int getBallRadius()
    {
        return radius;
    }

    public Boolean isStationary()
    {
        return vx == 0 && vy == 0;
    }
}
