import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BilliardGame extends JPanel implements ActionListener {
    private final List<Ball> balls;
    private final Timer timer;

    // Pool table dimensions
    private static final int TABLE_WIDTH = 1200;
    private static final int TABLE_HEIGHT = 600;
    private static final int TOTAL_BALL_NUMBER = 10;

    public BilliardGame() {
        balls = new ArrayList<>();
        Random rand = new Random();

        // Add balls with random positions and velocities
        for (int i = 0; i < TOTAL_BALL_NUMBER; i++) {
            int x = rand.nextInt(TABLE_WIDTH - 60) + 30; // Ensures balls are away from the edges
            int y = rand.nextInt(TABLE_HEIGHT - 60) + 30;
            double vx = rand.nextDouble() * 40 - 20; // Random speed between -2 and 2
            double vy = rand.nextDouble() * 40 - 20;
            Color color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            balls.add(new Ball(x, y, vx, vy, color, i + 1));
        }

        timer = new Timer(16, this); // Approximately 60 FPS
        timer.start();
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

            // Controlla collisioni con le altre palline
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

        // Update accumulated distance
        accumulatedDistance += Math.sqrt(vx * vx + vy * vy);

        // Apply friction
        vx *= .97;
        vy *= .97;

        // Arresta la pallina se la velocità è molto bassa
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

    public void draw(Graphics2D g) {
        // Draw ball
        g.setColor(color);
        g.fillOval((int) x - radius, (int) y - radius, radius * 2, radius * 2);

        // Draw spinning effect
        double rotationAngle = accumulatedDistance / radius;
        int spotX = (int) (x + radius * 0.6 * Math.cos(rotationAngle));
        int spotY = (int) (y + radius * 0.6 * Math.sin(rotationAngle));

        g.setColor(Color.WHITE);
        g.fillOval(spotX - 3, spotY - 3, 6, 6);

        // Draw ball number
        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(number), (int) x - 5, (int) y + 5);
    }

    public boolean isColliding(Ball other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double distanceSquared = dx * dx + dy * dy;
        double radiusSum = this.radius + other.radius;

        // Controlla se la distanza tra i centri è minore o uguale alla somma dei raggi
        return distanceSquared <= radiusSum * radiusSum;
    }

    public void resolveCollision(Ball other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Evita divisioni per zero nel caso improbabile di due palline perfettamente sovrapposte
        if (distance == 0) {
            distance = 0.1;
        }

        // Corregge la sovrapposizione
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

        // Normale alla superficie di collisione
        double nx = dx / distance;
        double ny = dy / distance;

        // Tangente alla superficie di collisione
        double tx = -ny;
        double ty = nx;

        // Proiezione delle velocità sulla normale e sulla tangente
        double v1n = this.vx * nx + this.vy * ny;
        double v1t = this.vx * tx + this.vy * ty;
        double v2n = other.vx * nx + other.vy * ny;
        double v2t = other.vx * tx + other.vy * ty;

        // Scambio delle componenti normali (collisione elastica)
        double newV1n = v2n;
        double newV2n = v1n;

        // Le componenti tangenti rimangono invariate
        double newV1t = v1t;
        double newV2t = v2t;

        // Converti le velocità da normale/tangente a x/y
        this.vx = newV1n * nx + newV1t * tx;
        this.vy = newV1n * ny + newV1t * ty;
        other.vx = newV2n * nx + newV2t * tx;
        other.vy = newV2n * ny + newV2t * ty;
    }
}
