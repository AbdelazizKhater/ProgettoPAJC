package it.unibs.pajc;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.List;
import static it.unibs.pajc.CostantiStatiche.*;

class Ball extends GameFieldObject {
    // private double x, y; // Position
    private double vx, vy; // Velocity
    private final int radius = 15;
    private boolean inPlay;
    private final int number; // it.unibs.pajc.Ball number
    private double accumulatedDistance = 0;// Distance for rotation effect

    // Array per memorizzare i colori delle palline in base al loro numero


    public Ball(double x, double y, double vx, double vy, int number) {
        super();
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.number = number;
        this.inPlay = true;
        this.shape = new Area(new Ellipse2D.Double(-radius, -radius, radius * 2, radius * 2));
        
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

    public boolean handleCollisionWithPocket(Pocket pocket) {
        double dx = this.x - pocket.x - pocket.getRadius();
        double dy = this.y - pocket.y - pocket.getRadius();
        double distance = Math.sqrt(dx * dx + dy * dy);

       return distance <= pocket.getRadius();
    }

    public void checkBounds(List<Trapezoid> trapezoids) {
        if (trapezoids != null) {
            for (Trapezoid trapezoid : trapezoids) {
                if (isCollidingWithEdge(trapezoid)) {
                    handleCollisionWithShape(trapezoid);
                }
            }
        }
    }

    private boolean isCollidingWithEdge(Trapezoid trapezoid) {
        // Check collision with the edge itself
        return this.checkCollision(trapezoid);
    }

    public void resetSpeed(){
        this.vx = 0;
        this.vy = 0;
    }





    private void handleCollisionWithShape(GameFieldObject object) {
        // Crea oggetti Area per la pallina e l'oggetto
        Area ballArea = new Area(this.getShape());
        Area objectArea = new Area(object.getShape());
    
        // Interseca le aree per rilevare la sovrapposizione
        ballArea.intersect(objectArea);
    
        if (!ballArea.isEmpty()) { // Collisione rilevata
            // Ottieni il bounding box dell'area di intersezione
            Rectangle bounds = ballArea.getBounds();
            double intersectionCenterX = bounds.getCenterX();
            double intersectionCenterY = bounds.getCenterY();
    
            // Calcola il vettore normale (dal centro dell'intersezione al centro della pallina)
            double nx = x - intersectionCenterX;
            double ny = y - intersectionCenterY;
            double length = Math.sqrt(nx * nx + ny * ny);
    
            if (length < 1e-6) {
                nx = 1; // Evita la divisione per zero
                ny = 0;
                length = 1;
            }
    
            nx /= length; // Normalizza il vettore normale
            ny /= length;
    
            // Riflette la velocità lungo la normale con un fattore di smorzamento
            double elasticity = 0.8; // Coefficiente di restituzione (0 = inelastic, 1 = elastic)
            double dotProduct = vx * nx + vy * ny;
            vx -= (1 + elasticity) * dotProduct * nx;
            vy -= (1 + elasticity) * dotProduct * ny;
    
            // Corregge la posizione per evitare sovrapposizioni
            double overlap = radius - length; // Correzione basata sulla distanza effettiva
            if (overlap > 0) {
                x += nx * overlap * 0.5; // Correzione meno aggressiva (solo metà dell'overlap)
                y += ny * overlap * 0.5;
            }
        }
    }

    public void resolveCollision(Ball other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Evita divisioni per zero o instabilità numerica
        if (distance < 1e-6) {
            distance = 1e-6; // Distanza minima per evitare errore
        }

        // Correggi il sovrapposizionamento
        double overlap = (this.radius + other.radius) - distance;
        if (overlap > 0) {
            double correctionFactor = overlap / 2.0 / distance;
            double correctionX = dx * correctionFactor;
            double correctionY = dy * correctionFactor;

            this.x -= correctionX;
            this.y -= correctionY;
            other.x += correctionX;
            other.y += correctionY;
        }

        // Calcola il vettore normale e tangenziale
        double nx = dx / distance;
        double ny = dy / distance;

        double tx = -ny;
        double ty = nx;

        // Decomponi le velocità lungo normale e tangente
        double v1n = this.vx * nx + this.vy * ny;
        double v1t = this.vx * tx + this.vy * ty;
        double v2n = other.vx * nx + other.vy * ny;
        double v2t = other.vx * tx + other.vy * ty;

        // Inverti le velocità lungo la normale (urto elastico)
        double newV1n = v2n;
        double newV2n = v1n;

        // Ricombina le velocità
        this.vx = newV1n * nx + v1t * tx;
        this.vy = newV1n * ny + v1t * ty;
        other.vx = newV2n * nx + v2t * tx;
        other.vy = newV2n * ny + v2t * ty;
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

    public int getBallRadius() {
        return radius;
    }

    public Boolean isStationary() {
        return vx == 0 && vy == 0;
    }
    public boolean isWhite() {
        return this.number == 0;
    }

    public void setInPlay(boolean inPlay) {
        this.inPlay = inPlay;
    }

    public boolean isInPlay() {
        return inPlay;
    }

    public BallInfo getBallInfo()
    {
        return new BallInfo(this.x, this.y, this.radius, this.number);
    }
}
