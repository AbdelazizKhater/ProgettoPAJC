package it.unibs.pajc;//MODEL

import java.awt.geom.Area;
import java.util.ArrayList;
import static it.unibs.pajc.CostantiStatiche.*;

public class GameField {

    private final ArrayList<Ball> balls;
    private final ArrayList<Trapezoid> trapezoids;
    private final ArrayList<Pocket> pockets;
    private final Stick stick;

    public GameField() {
        balls = new ArrayList<>();
        trapezoids = new ArrayList<>();
        pockets = new ArrayList<>();
        stick = new Stick();

        // Add billiard balls in initial positions
        setupInitialPositions();
    }

    public void hitBall() {
        double[] velocity = stick.calculateBallVelocity();
        balls.get(0).applyVelocity(velocity);
    }

    /*
     * private void detectCollision() {
     * int numberObjects= objects.size();
     * 
     * GameFieldObject[] objs = new GameFieldObject[][numberObjects];
     * objects.toArray(objs);
     * 
     * for(int i = 0; i < numberObjects; i++) {
     * for(int j = i + 1; j < numberObjects; j++) {
     * if(objs[i].checkCollision(objs[j])) {
     * objs[i].collided();
     * objs[j].collided();
     * }
     * }
     * }
     * }
     * 
     */

    public void stepNext() {
        for (int i = 0; i < balls.size(); i++) {
            Ball ball = balls.get(i);
            ball.updatePosition();
            ball.checkBounds(trapezoids);
            for (Pocket pocket : pockets) {
                if (ball.handleCollisionWithPocket(pocket)) {
                    balls.remove(ball);
                    break;
                }
            }
            for (int j = i + 1; j < balls.size(); j++) {
                Ball other = balls.get(j);
                if (ball.checkCollision(other) && (!ball.isStationary() || !other.isStationary())) {
                    ball.resolveCollision(other);
                }
            }
        }
    }

    public boolean allBallsAreStationary()
    {
        for (Ball ball : balls) {

            if(!ball.isStationary()) return false;
            
        }

        return true;
    }

    private void setupInitialPositions() {
        // Radius of each ball
        int ballRadius = 15;

        //Definizione area di ogni trapezio
        for (int i = 0; i < X_POINTS_TRAPEZI.length; i++) {
            trapezoids.add(new Trapezoid(X_POINTS_TRAPEZI[i], Y_POINTS_TRAPEZI[i]));
        }
        Area a = new Area(trapezoids.get(0).getShape());
        System.out.println(a.isEmpty());

        //Definizione area di ogni buca
        for(int i = 0; i < POCKET_POSITIONS.length; i++) {
            int x = POCKET_POSITIONS[i][0];
            int y = POCKET_POSITIONS[i][1];
            int pocketRadius = POCKET_POSITIONS[i][2] / 2;

            System.out.println("x: " + x + " y: " + y + " r: " + pocketRadius);

            pockets.add(new Pocket(x, y, pocketRadius));
        }

        // Position for the white ball
        balls.add(new Ball(200, TABLE_HEIGHT / 2.0, 0, 0, 0)); // White ball
        balls.get(0).setVx(0);
        // Pyramid starting position for numbered balls
        int startX = 800; // Base X position of the triangle
        int startY = TABLE_HEIGHT / 2; // Center of the table
        int rows = 5; // Number of rows in the triangle

        // Add numbered balls in a triangular configuration
        int number = 1;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col <= row; col++) {
                double x = startX + row * (ballRadius * 2 * Math.sqrt(3) / 2);
                double y = startY - row * ballRadius + col * ballRadius * 2;
                balls.add(new Ball(x, y, 0, 0, number++));
            }
        }

        stick.setAngleDegrees(180);

    }

    public ArrayList<Ball> getBalls() {
        return balls;
    }

    public Stick getStick() {
        return stick;
    }

}
