//MODEL

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameField {
    private static final int TABLE_WIDTH = 1200;
    private static final int TABLE_HEIGHT = 600;

    private final ArrayList<Ball> balls;

    public GameField() {
        balls = new ArrayList<>();

        // Add billiard balls in initial positions
        setupInitialPositions();
    }

    public void stepNext(){
        for (int i = 0; i < balls.size(); i++) {
            Ball ball = balls.get(i);
            ball.updatePosition();
            ball.checkBounds(TABLE_WIDTH, TABLE_HEIGHT);

            for (int j = i + 1; j < balls.size(); j++) {
                Ball other = balls.get(j);
                if (ball.isColliding(other)) {
                    ball.resolveCollision(other);
                }
            }
        }
    }
    private void setupInitialPositions() {
        // Radius of each ball
        int radius = 15;

        // Position for the white ball
        balls.add(new Ball(200, TABLE_HEIGHT / 2.0, 3, 0,  0)); // White ball
        balls.get(0).setVx(100);
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
