package it.unibs.pajc;//MODEL

import java.util.ArrayList;
import static it.unibs.pajc.CostantiStatiche.*;

public class GameField {

    private final ArrayList<Ball> balls;
    private final ArrayList<Trapezoid> trapezoids;

    private final ArrayList<GameFieldObject> objects;

    public GameField() {
        balls = new ArrayList<>();
        trapezoids = new ArrayList<>();
        objects = new ArrayList<>();
        objects.addAll(balls);
        objects.addAll(trapezoids);

        // Add billiard balls in initial positions
        setupInitialPositions();
    }

    /*
    private void detectCollision() {
        int numberObjects= objects.size();

        GameFieldObject[] objs = new GameFieldObject[][numberObjects];
        objects.toArray(objs);

        for(int i = 0; i < numberObjects; i++) {
            for(int j = i + 1; j < numberObjects; j++) {
                if(objs[i].checkCollision(objs[j])) {
                    objs[i].collided();
                    objs[j].collided();
                }
            }
        }
    }

     */

    public void stepNext(){
        for (int i = 0; i < balls.size(); i++) {
            Ball ball = balls.get(i);
            ball.updatePosition();
            ball.checkBounds(trapezoids);


            for (int j = i + 1; j < balls.size(); j++) {
                Ball other = balls.get(j);
                if (ball.checkCollision(other)) {
                    ball.resolveCollision(other);
                }
            }
        }
    }
    private void setupInitialPositions() {
        // Radius of each ball
        int radius = 15;

        for (int i = 0; i < X_POINTS_TRAPEZI.length; i++) {
            trapezoids.add(new Trapezoid(X_POINTS_TRAPEZI[i], Y_POINTS_TRAPEZI[i]));
        }

        // Position for the white ball
        balls.add(new Ball(200, TABLE_HEIGHT / 2.0, 3, 0,  0)); // White ball
        balls.get(0).setVx(30);
        // Pyramid starting position for numbered balls
        int startX = 800; // Base X position of the triangle
        int startY = TABLE_HEIGHT / 2; // Center of the table
        int rows = 5; // Number of rows in the triangle

        // Add numbered balls in a triangular configuration
        int number = 1;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col <= row; col++) {
                double x = startX + row * (radius * 2 * Math.sqrt(3) / 2);
                double y = startY - row * radius + col * radius * 2;
                balls.add(new Ball(x, y, 0, 0, number++));
            }
        }


    }


    public ArrayList<Ball> getBalls() {
        return balls;
    }
}
