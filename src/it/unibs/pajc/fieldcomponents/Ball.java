package it.unibs.pajc.fieldcomponents;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;


public class Ball extends GameFieldObject {
    private double vx, vy; // Velocity
    private final int radius = 15;
    private final int number; // Numero sulla pallina
    private boolean needsReposition;

    // Array per memorizzare i colori delle palline in base al loro numero


    public Ball(double x, double y, double vx, double vy, int number) {
        super();
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.number = number;
        if(number == 0) needsReposition = false;
        this.shape = new Area(new Ellipse2D.Double(-radius, -radius, radius * 2, radius * 2));
    }

    /**
     * Attraverso la velocità, viene riposizionata la biglia. Tramite un fattore 0.99 di attrito,
     * la pallina viene rallentata in modo realistico.
     */
    public void updatePosition() {
        x += vx;
        y += vy;

        // Attrito
        double friction = 0.99;
        vx *= friction;
        vy *= friction;

        // La biglia viene fermata quando è troppo lenta, per evitare attese inutili
        if (Math.abs(vx) < 0.1 && Math.abs(vy) < 0.1) {
            vx = 0;
            vy = 0;
        }
    }

    /**
     * Calcolo della distanza del centro della biglia dalla buca, una volta che il centro della biglia
     * tocca la buca, viene considerata fuori gioco.
     */
    public boolean handleCollisionWithPocket(Pocket pocket) {
        double dx = this.x - pocket.x - pocket.getRadius();
        double dy = this.y - pocket.y - pocket.getRadius();
        double distance = Math.sqrt(dx * dx + dy * dy);

       return distance <= pocket.getRadius();
    }

    /**
     * Calcolo della collisione con i bordi, composti da 6 trapezi.
     */
    public void checkBounds(ArrayList<Trapezoid> trapezoids) {
        if (trapezoids != null) {
            for (Trapezoid trapezoid : trapezoids) {
                if (this.checkCollision(trapezoid)) {
                    handleCollisionWithShape(trapezoid);
                }
            }
        }
    }

    /**
     * Imposta la velocità a 0, utilizzato nella collisione con buche per evitare errori di clipping.
     */
    public void resetSpeed(){
        this.vx = 0;
        this.vy = 0;
    }

    /**
     * Metodo generico di check di collisione tramite intersezione di aree con altri oggetti del campo di gioco.
     */
    private void handleCollisionWithShape(GameFieldObject object) {
        // Crea oggetti Area per la pallina e l'oggetto
        Area ballArea = new Area(this.getShape());
        Area objectArea = new Area(object.getShape());
    
        // Interseca le aree per rilevare la sovrapposizione
        ballArea.intersect(objectArea);
    
        if (!ballArea.isEmpty()) { 
            // Ottieni il bounding box dell'area di intersezione
            Rectangle bounds = ballArea.getBounds();
            double intersectionCenterX = bounds.getCenterX();
            double intersectionCenterY = bounds.getCenterY();
    
            // Calcola il vettore normale (dal centro dell'intersezione al centro della pallina)
            double nx = x - intersectionCenterX;
            double ny = y - intersectionCenterY;
            double length = Math.sqrt(nx * nx + ny * ny);
    
            // Evita la divisione per zero
            if (length < 1e-6) {
                nx = 1; 
                ny = 0;
                length = 1;
            }
    
            // Normalizza il vettore normale
            nx /= length; 
            ny /= length;
    
            // Riflette la velocità lungo la normale con un fattore di smorzamento
            double elasticity = 0.8; // Coefficiente di elasticità
            double dotProduct = vx * nx + vy * ny;
            vx -= (1 + elasticity) * dotProduct * nx;
            vy -= (1 + elasticity) * dotProduct * ny;
    
            // Corregge la posizione per evitare sovrapposizioni
            double overlap = radius - length; 
            if (overlap > 0) {
                x += nx * (overlap + 0.01) * 0.5; // Correzione meno aggressiva (solo metà dell'overlap)
                y += ny * (overlap + 0.01) * 0.5;
            }
        }
    }

    /**
     * Risoluzione delle collisioni tramite calcolo fisico degli urti.
     */
    public void resolveCollision(Ball other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Evita divisioni per zero
        if (distance < 1e-6) {
            distance = 1e-6;
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

        // Inverti le velocità lungo la normale 
        double newV1n = v2n;
        double newV2n = v1n;

        // Ricombina le velocità
        this.vx = newV1n * nx + v1t * tx;
        this.vy = newV1n * ny + v1t * ty;
        other.vx = newV2n * nx + v2t * tx;
        other.vy = newV2n * ny + v2t * ty;
    }

    @Override
    public String toString() {
        return getX() + "@" + getY();
    }

    public void applyVelocity(double[] velocity) {
        this.vx = velocity[0];
        this.vy = velocity[1];
    }

    public int getBallRadius() {
        return radius;
    }

    public int getBallNumber() {
        return number;
    }

    public Boolean isStationary() {
        return vx == 0 && vy == 0;
    }

    public boolean isWhite() {
        return this.number == 0;
    }

    public boolean needsReposition() {return needsReposition;}

    public void setNeedsReposition(boolean needsReposition) {
        this.needsReposition = needsReposition;
    }

    public int getRadius() {
        return radius;
    }

    public int getNumber() {
        return number;
    }
}
