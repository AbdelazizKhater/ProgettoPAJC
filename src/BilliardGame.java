import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class BilliardGame extends JPanel implements ActionListener {
    private final List<Ball> balls;
    private final Timer timer;

    // Pool table dimensions and constants
    private static final int TABLE_WIDTH = 1200;
    private static final int TABLE_HEIGHT = 600;
    public static final int BORDER_WIDTH = 50;
    public static final int POCKET_RADIUS = 30;

    public BilliardGame() {
        balls = new ArrayList<>();

        // Add billiard balls in initial positions
        setupInitialPositions();

        timer = new Timer(16, this); // Approximately 60 FPS
        timer.start();
    }

    private void setupInitialPositions() {
        // Radius of each ball
        int radius = 15;

        // Position for the white ball
        balls.add(new Ball(200, TABLE_HEIGHT / 2.0, 3, 0, Color.WHITE, 0)); // White ball
        balls.get(0).setVx(53);
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
                Color color = new Color((int) (Math.random() * 255), (int) (Math.random() * 255),
                        (int) (Math.random() * 255));
                balls.add(new Ball(x, y, 0, 0, color, number++));
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw table background
        g2.setColor(new Color(34, 139, 34));
        g2.fillRect(0, 0, TABLE_WIDTH, TABLE_HEIGHT);

        // Draw brown borders
        g2.setColor(new Color(139, 69, 19));
        g2.fillRect(0, 0, BORDER_WIDTH, TABLE_HEIGHT);
        g2.fillRect(TABLE_WIDTH - BORDER_WIDTH, 0, BORDER_WIDTH, TABLE_HEIGHT);
        g2.fillRect(0, 0, TABLE_WIDTH, BORDER_WIDTH);
        g2.fillRect(0, TABLE_HEIGHT - BORDER_WIDTH, TABLE_WIDTH, BORDER_WIDTH);

        g.setColor(Color.RED);
        int[] xPoints1 = { BORDER_WIDTH + POCKET_RADIUS, TABLE_WIDTH / 2 - POCKET_RADIUS - 5,
                TABLE_WIDTH / 2 - POCKET_RADIUS - 25, BORDER_WIDTH + POCKET_RADIUS + 30 }; // Coordinate x dei vertici
        int[] yPoints1 = { BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH + 25, BORDER_WIDTH + 25 }; // Coordinate y dei
                                                                                               // vertici
        g.fillPolygon(xPoints1, yPoints1, 4);
        int[] xPoints2 = { TABLE_WIDTH / 2 + POCKET_RADIUS + 5, TABLE_WIDTH - POCKET_RADIUS - BORDER_WIDTH,
                TABLE_WIDTH - BORDER_WIDTH - POCKET_RADIUS - 30, TABLE_WIDTH / 2 + POCKET_RADIUS + 25 }; // Coordinate x
                                                                                                         // dei vertici
        g.fillPolygon(xPoints2, yPoints1, 4);
        int[] xPoints3 = { BORDER_WIDTH, BORDER_WIDTH + 25, BORDER_WIDTH + 25, BORDER_WIDTH }; // Coordinate x dei
                                                                                               // vertici
        int[] yPoints3 = { BORDER_WIDTH + POCKET_RADIUS, BORDER_WIDTH + POCKET_RADIUS + 25,
                TABLE_HEIGHT - BORDER_WIDTH - POCKET_RADIUS - 25, TABLE_HEIGHT - BORDER_WIDTH - POCKET_RADIUS }; // Coordinate
                                                                                                                 // y
                                                                                                                 // dei
                                                                                                                 // vertici
        g.fillPolygon(xPoints3, yPoints3, 4);
        int[] xPoints4 = { BORDER_WIDTH + POCKET_RADIUS + 25, TABLE_WIDTH / 2 - POCKET_RADIUS - 25,
                TABLE_WIDTH / 2 - POCKET_RADIUS - 5, BORDER_WIDTH + POCKET_RADIUS }; // Coordinate x dei vertici
        int[] yPoints4 = { TABLE_HEIGHT - BORDER_WIDTH - 25, TABLE_HEIGHT - BORDER_WIDTH - 25,
                TABLE_HEIGHT - BORDER_WIDTH, TABLE_HEIGHT - BORDER_WIDTH }; // Coordinate y dei vertici
        g.fillPolygon(xPoints4, yPoints4, 4);
        int[] xPoints5 = { TABLE_WIDTH / 2 + POCKET_RADIUS + 30, TABLE_WIDTH - POCKET_RADIUS - BORDER_WIDTH - 30,
                TABLE_WIDTH - POCKET_RADIUS - BORDER_WIDTH, TABLE_WIDTH / 2 + POCKET_RADIUS + 5 }; // Coordinate x dei
                                                                                                   // vertici
        g.fillPolygon(xPoints5, yPoints4, 4);
        int[] xPoints6 = { TABLE_WIDTH - BORDER_WIDTH - 25, TABLE_WIDTH - BORDER_WIDTH, TABLE_WIDTH - BORDER_WIDTH,
                TABLE_WIDTH - BORDER_WIDTH - 25 }; // Coordinate x dei vertici
        int[] yPoints6 = { BORDER_WIDTH + POCKET_RADIUS + 25, BORDER_WIDTH + POCKET_RADIUS,
                TABLE_HEIGHT - BORDER_WIDTH - POCKET_RADIUS, TABLE_HEIGHT - BORDER_WIDTH - POCKET_RADIUS - 25 }; // Coordinate
                                                                                                                 // y
                                                                                                                 // dei
                                                                                                                 // vertici
        g.fillPolygon(xPoints6, yPoints6, 4);

        // Draw pockets
        g2.setColor(Color.BLACK);
        g2.fillOval(BORDER_WIDTH - POCKET_RADIUS - 5, BORDER_WIDTH - POCKET_RADIUS - 5, POCKET_RADIUS * 2,
                POCKET_RADIUS * 2);
        g2.fillOval((TABLE_WIDTH / 2) - POCKET_RADIUS, BORDER_WIDTH - POCKET_RADIUS - 5, POCKET_RADIUS * 2,
                POCKET_RADIUS * 2);
        g2.fillOval(TABLE_WIDTH - BORDER_WIDTH - POCKET_RADIUS + 5, BORDER_WIDTH - POCKET_RADIUS - 5, POCKET_RADIUS * 2,
                POCKET_RADIUS * 2);
        g2.fillOval(BORDER_WIDTH - POCKET_RADIUS - 5, TABLE_HEIGHT - BORDER_WIDTH - POCKET_RADIUS + 5,
                POCKET_RADIUS * 2, POCKET_RADIUS * 2);
        g2.fillOval((TABLE_WIDTH / 2) - POCKET_RADIUS, TABLE_HEIGHT - BORDER_WIDTH - POCKET_RADIUS + 5,
                POCKET_RADIUS * 2, POCKET_RADIUS * 2);
        g2.fillOval(TABLE_WIDTH - BORDER_WIDTH - POCKET_RADIUS + 5, TABLE_HEIGHT - BORDER_WIDTH - POCKET_RADIUS + 5,
                POCKET_RADIUS * 2, POCKET_RADIUS * 2);

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
        frame.setSize(TABLE_WIDTH + 16, TABLE_HEIGHT + 39);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class Ball {
    private double x, y; // Position
    private double vx, vy; // Velocity
    private final int radius = 15;
    private final Color color;
    private final int number; // Ball number
    private double accumulatedDistance = 0; // Distance for rotation effect
    private static final int TABLE_WIDTH = 1200;
    private static final int TABLE_HEIGHT = 600;

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
        if (Math.abs(vx) < 0.3)
            vx = 0;
        if (Math.abs(vy) < 0.3)
            vy = 0;
    }

    public void checkBounds(int width, int height) {
        // Bounce off walls
        if (x - radius < BilliardGame.BORDER_WIDTH || x + radius > width - BilliardGame.BORDER_WIDTH) {
            vx = -vx;
            x = Math.max(radius + BilliardGame.BORDER_WIDTH, Math.min(width - radius - BilliardGame.BORDER_WIDTH, x));
        }
        if (y - radius < BilliardGame.BORDER_WIDTH || y + radius > height - BilliardGame.BORDER_WIDTH) {
            vy = -vy;
            y = Math.max(radius + BilliardGame.BORDER_WIDTH, Math.min(height - radius - BilliardGame.BORDER_WIDTH, y));
        }

        // Check for pocket collisions
        checkPocketCollision(width, height);
    }

    private void checkPocketCollision(int width, int height) {
        int pocketCenterX, pocketCenterY;

        // Check top left pocket
        pocketCenterX = BilliardGame.BORDER_WIDTH + BilliardGame.POCKET_RADIUS;
        pocketCenterY = BilliardGame.BORDER_WIDTH + BilliardGame.POCKET_RADIUS;
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

        return distanceSquared <= BilliardGame.POCKET_RADIUS * BilliardGame.POCKET_RADIUS;
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

    public void draw(Graphics2D g) {
        // Colori delle palline in base al numero
        Color[] ballColors = {
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

        // Disegna il corpo della pallina con il colore appropriato
        g.setColor(ballColors[number]);
        g.fillOval((int) x - radius, (int) y - radius, radius * 2, radius * 2);

        // Usa un'area di clipping per disegnare le strisce solo all'interno del cerchio
        // (per le palline numerate dalla 9 alla 15)
        if (number >= 9) {
            Shape originalClip = g.getClip();
            g.setClip(new Ellipse2D.Double((int) x - radius, (int) y - radius, radius * 2, radius * 2));

            // Disegna le bande bianche superiori e inferiori
            g.setColor(Color.WHITE);
            int bandHeight = radius / 2;
            g.fillRect((int) x - radius, (int) y - radius, radius * 2, bandHeight); // Banda superiore
            g.fillRect((int) x - radius, (int) y + radius - bandHeight, radius * 2, bandHeight); // Banda inferiore

            // Ripristina l'area di clipping originale
            g.setClip(originalClip);
        }

        // Disegna il cerchio bianco al centro per il numero (per tutte le palline
        // numerate)
        if (number > 0) {
            // int whiteCircleDiameter = radius; // Diametro del cerchio bianco
            // double whiteCircleX = x - whiteCircleDiameter / 2;
            // double whiteCircleY = y - whiteCircleDiameter / 2;

            // g.setColor(Color.WHITE);
            // g.fillOval((int) whiteCircleX, (int) whiteCircleY, whiteCircleDiameter,
            // whiteCircleDiameter);

            // Disegna il numero al centro del cerchio bianco
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 11));
            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(String.valueOf(number));
            int textHeight = metrics.getAscent();
            g.drawString(String.valueOf(number), (int) x - textWidth / 2, (int) y + textHeight / 4);
        }

        // Disegna un riflesso fisso sul lato superiore della pallina
        Graphics2D g2d = (Graphics2D) g.create(); // Crea una copia per mantenere il contesto originale

        // Crea un gradiente radiale per il riflesso
        RadialGradientPaint gradient = new RadialGradientPaint(
                new Point2D.Double(x, y - radius * 0.7), // Centro del riflesso (più lontano dal centro della pallina)
                radius * 0.4f, // Raggio del riflesso
                new float[] { 0f, 1f }, // Posizioni dei colori nel gradiente
                new Color[] { new Color(255, 255, 255, 200), new Color(255, 255, 255, 0) } // Bianco opaco al centro,
                                                                                           // trasparente ai bordi
        );

        // Applica il gradiente al contesto grafico
        g2d.setPaint(gradient);

        // Disegna un cerchio per simulare il riflesso
        g2d.fill(new Ellipse2D.Double(
                x - radius * 0.4, // Posizione X del cerchio
                y - radius * 1.1, // Posizione Y del cerchio (più distante dal centro)
                radius * 0.8, // Diametro del cerchio
                radius * 0.8 // Diametro del cerchio
        ));

        g2d.dispose(); // Libera la copia del contesto grafico

    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }
}
