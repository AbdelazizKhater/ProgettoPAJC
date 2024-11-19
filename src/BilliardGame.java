import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class BilliardGame extends JPanel implements ActionListener {
    private final List<Ball> balls;
    private final Timer timer;

    // Pool table dimensions
    private static final int TABLE_WIDTH = 1200;
    private static final int TABLE_HEIGHT = 600;

    public BilliardGame() {
        balls = new ArrayList<>();

        // Add billiard balls in initial positions
        setupInitialPositions();

        timer = new Timer(16, this); // Approximately 60 FPS
        timer.start();
    }

    private void setupInitialPositions() {
        // Radius of each ball
        int radius = 20;

        // Position for the white ball
        balls.add(new Ball(200, TABLE_HEIGHT / 2.0, 3, 0, Color.WHITE, 0)); // White ball
        balls.get(0).setVx(80);
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
                Color color = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
                balls.add(new Ball(x, y, 0, 0, color, number++));
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw pool table background
        g2.setColor(new Color(34, 139, 34)); // Green pool table color
        g2.fillRect(0, 0, TABLE_WIDTH, TABLE_HEIGHT);

        // Draw table boundaries
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, TABLE_WIDTH - 1, TABLE_HEIGHT - 1);

        // Draw each ball
        for (Ball ball : balls) {
            ball.draw(g2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < balls.size(); i++) {
            Ball ball = balls.get(i);
            ball.updatePosition();
            ball.checkBounds(TABLE_WIDTH, TABLE_HEIGHT);

            // Check collisions with other balls
            for (int j = i + 1; j < balls.size(); j++) {
                Ball other = balls.get(j);
                if (ball.isColliding(other)) {
                    ball.resolveCollision(other);
                }
            }
        }
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Billiard Game");
        BilliardGame game = new BilliardGame();
        frame.add(game);
        frame.setSize(TABLE_WIDTH + 16, TABLE_HEIGHT + 39); // Account for window borders
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class Ball {
    private double x, y; // Position
    private double vx, vy; // Velocity
    private final int radius = 20;
    private final Color color;
    private final int number; // Ball number
    private double accumulatedDistance = 0; // Distance for rotation effect

    public Ball(double x, double y, double vx, double vy, Color color, int number) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
        this.number = number;
    }

    public void updatePosition() {
        x += vx;
        y += vy;

        // Update accumulated distance for rotation effect
        accumulatedDistance += Math.sqrt(vx * vx + vy * vy);

        // Apply friction
        double friction = 0.97; // Increased friction for more realistic slowdown
        vx *= friction;
        vy *= friction;

        // Stop ball if velocity is very low
        if (Math.abs(vx) < 0.3) vx = 0;
        if (Math.abs(vy) < 0.3) vy = 0;
    }

    public void checkBounds(int width, int height) {
        // Bounce off walls
        if (x - radius < 0 || x + radius > width) {
            vx = -vx;
            x = Math.max(radius, Math.min(width - radius, x)); // Keep ball within bounds
        }
        if (y - radius < 0 || y + radius > height) {
            vy = -vy;
            y = Math.max(radius, Math.min(height - radius, y)); // Keep ball within bounds
        }
    }

    public boolean isColliding(Ball other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double distanceSquared = dx * dx + dy * dy;
        double radiusSum = this.radius + other.radius;

        // Check if the distance between centers is less than or equal to the sum of the radii
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

    public void draw(Graphics2D g) {
        // Disegna il corpo della pallina
        g.setColor(color);
        g.fillOval((int) x - radius, (int) y - radius, radius * 2, radius * 2);

        // Aggiungi la striscia per le palline numerate dalla 9 alla 15
        if (number >= 9) {
            g.setColor(Color.WHITE); // Striscia bianca per le palline strisciate

            // Aumentiamo la larghezza della striscia
            int stripeHeight = radius / 2; // Aumento della larghezza della striscia
            int stripeWidth = radius * 2; // La striscia si estende su tutta la larghezza della pallina

            // Disegna la striscia inclinata
            // Posizioniamo la striscia al centro della pallina e inclinata di 45°
            g.rotate(Math.toRadians(45), x, y);
            g.fillRect((int) (x - radius), (int) (y - stripeHeight / 2), stripeWidth, stripeHeight);
            g.rotate(-Math.toRadians(45), x, y); // Ripristina la rotazione originale
        }

        // Calcola un angolo in base alla distanza accumulata per simulare la rotazione
        double rotationAngle = accumulatedDistance / radius;
        int spotX = (int) (x + radius * 0.6 * Math.cos(rotationAngle));
        int spotY = (int) (y + radius * 0.6 * Math.sin(rotationAngle));

        // Disegna la macchia bianca che ruota attorno al centro della pallina
        g.setColor(Color.WHITE);
        g.fillOval(spotX - 3, spotY - 3, 6, 6);

        // Disegna il numero al centro della pallina
        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(number), (int) x - 5, (int) y + 5);
    }




    public void setVx(double vx) {
        this.vx = vx;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }
}

