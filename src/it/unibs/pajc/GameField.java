package it.unibs.pajc;//MODEL

import java.util.ArrayList;
import static it.unibs.pajc.CostantiStatiche.*;

public class GameField {

    private final ArrayList<Ball> balls;
    private final ArrayList<Ball> pocketedBalls;
    private final ArrayList<Trapezoid> trapezoids;
    private final ArrayList<Pocket> pockets;
    private final Stick stick;
    private final Ball cueBall;

    public GameField() {
        balls = new ArrayList<>();
        pocketedBalls = new ArrayList<>();
        trapezoids = new ArrayList<>();
        pockets = new ArrayList<>();
        stick = new Stick();
        cueBall = new Ball(200, TABLE_HEIGHT / 2.0, 0, 0, 0);

        // Add billiard balls in initial positions
        setupInitialPositions();
    }

    private void setupInitialPositions() {
        // Radius of each ball
        int ballRadius = 15;

        //Definizione area di ogni trapezio
        for (int i = 0; i < X_POINTS_TRAPEZI.length; i++) {
            trapezoids.add(new Trapezoid(X_POINTS_TRAPEZI[i], Y_POINTS_TRAPEZI[i]));
        }

        //Definizione area di ogni buca
        for (int[] pocketPosition : POCKET_POSITIONS) {
            int x = pocketPosition[0];
            int y = pocketPosition[1];
            int pocketRadius = pocketPosition[2] / 2;

            pockets.add(new Pocket(x, y, pocketRadius));
        }

        // Position for the white ball
        balls.add(cueBall); // White ball
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

    public void stepNext() {
        for (int i = 0; i < balls.size(); i++) {
            Ball ball = balls.get(i);
            ball.updatePosition();
            ball.checkBounds(trapezoids);
            for (Pocket pocket : pockets) {
                if (ball.handleCollisionWithPocket(pocket)) {
                    if(ball.isWhite()) {
                        ball.setInPlay(false);
                        ball.resetSpeed();
                        balls.remove(ball);
                        balls.addFirst(ball);
                    } else {
                        ball.setInPlay(false);
                        pocketedBalls.add(ball);
                        balls.remove(ball);
                        //TODO: aggiungere le palline in un panel view
                    }
                    //Una volta che la pallina entra in buca, non vengono fatti altri controlli
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

    public ArrayList<Ball> getBalls() {
        return balls;
    }

    public Stick getStick() {
        return stick;
    }

    public Ball getCueBall() {
        return cueBall;
    }

    public boolean reduceStickVisualPower(double speed) {
        Stick stick = getStick();
        if (stick.getVisualPower() > 0) {
            stick.setVisualPower(stick.getVisualPower() - speed);
            return true; // Animazione in corso
        }
        return false; // Animazione completata
    }
    
    
    public void hitBall() {
        double[] velocity = stick.calculateBallVelocity();
        cueBall.applyVelocity(velocity);
        stick.setPower(0); // Reset della potenza reale dopo il colpo
    }
    
    
    

}
