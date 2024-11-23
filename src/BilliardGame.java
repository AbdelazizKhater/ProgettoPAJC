import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        int[] xPoints1 = { BORDER_WIDTH + POCKET_RADIUS, TABLE_WIDTH / 2 - POCKET_RADIUS - POCKET_RADIUS / 6,
                TABLE_WIDTH / 2 - POCKET_RADIUS - BORDER_WIDTH / 2, BORDER_WIDTH + POCKET_RADIUS * 2 }; // Coordinate x
                                                                                                        // dei vertici
        int[] yPoints1 = { BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH + BORDER_WIDTH / 2,
                BORDER_WIDTH + BORDER_WIDTH / 2 }; // Coordinate y dei vertici
        g.fillPolygon(xPoints1, yPoints1, 4);
        int[] xPoints2 = { TABLE_WIDTH / 2 + +POCKET_RADIUS + POCKET_RADIUS / 6,
                TABLE_WIDTH - POCKET_RADIUS - BORDER_WIDTH, TABLE_WIDTH - BORDER_WIDTH - POCKET_RADIUS * 2,
                TABLE_WIDTH / 2 + POCKET_RADIUS + BORDER_WIDTH / 2 }; // Coordinate x dei vertici
        g.fillPolygon(xPoints2, yPoints1, 4);
        int[] xPoints3 = { BORDER_WIDTH, BORDER_WIDTH + BORDER_WIDTH / 2, BORDER_WIDTH + BORDER_WIDTH / 2,
                BORDER_WIDTH }; // Coordinate x dei vertici
        int[] yPoints3 = { BORDER_WIDTH + POCKET_RADIUS, BORDER_WIDTH + POCKET_RADIUS + BORDER_WIDTH / 2,
                TABLE_HEIGHT - BORDER_WIDTH - POCKET_RADIUS - BORDER_WIDTH / 2,
                TABLE_HEIGHT - BORDER_WIDTH - POCKET_RADIUS }; // Coordinate y dei vertici
        g.fillPolygon(xPoints3, yPoints3, 4);
        int[] xPoints4 = { BORDER_WIDTH + POCKET_RADIUS + BORDER_WIDTH / 2,
                TABLE_WIDTH / 2 - POCKET_RADIUS - BORDER_WIDTH / 2, TABLE_WIDTH / 2 - POCKET_RADIUS - POCKET_RADIUS / 6,
                BORDER_WIDTH + POCKET_RADIUS }; // Coordinate x dei vertici
        int[] yPoints4 = { TABLE_HEIGHT - BORDER_WIDTH - BORDER_WIDTH / 2,
                TABLE_HEIGHT - BORDER_WIDTH - BORDER_WIDTH / 2, TABLE_HEIGHT - BORDER_WIDTH,
                TABLE_HEIGHT - BORDER_WIDTH }; // Coordinate y dei vertici
        g.fillPolygon(xPoints4, yPoints4, 4);
        int[] xPoints5 = { TABLE_WIDTH / 2 + POCKET_RADIUS * 2, TABLE_WIDTH - POCKET_RADIUS * 2 - BORDER_WIDTH,
                TABLE_WIDTH - POCKET_RADIUS - BORDER_WIDTH, TABLE_WIDTH / 2 + POCKET_RADIUS + POCKET_RADIUS / 6 }; // Coordinate
                                                                                                                   // x
                                                                                                                   // dei
                                                                                                                   // vertici
        g.fillPolygon(xPoints5, yPoints4, 4);
        int[] xPoints6 = { TABLE_WIDTH - BORDER_WIDTH - BORDER_WIDTH / 2, TABLE_WIDTH - BORDER_WIDTH,
                TABLE_WIDTH - BORDER_WIDTH, TABLE_WIDTH - BORDER_WIDTH - BORDER_WIDTH / 2 }; // Coordinate x dei vertici
        int[] yPoints6 = { BORDER_WIDTH + POCKET_RADIUS + BORDER_WIDTH / 2, BORDER_WIDTH + POCKET_RADIUS,
                TABLE_HEIGHT - BORDER_WIDTH - POCKET_RADIUS,
                TABLE_HEIGHT - BORDER_WIDTH - POCKET_RADIUS - BORDER_WIDTH / 2 }; // Coordinate y dei vertici
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
            drawBall(g2, ball);
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


    public void drawBall(Graphics2D g, Ball ball) {
        // Ottieni i componenti della pallina (i vari elementi grafici da disegnare)
        List<Object> components = ball.getShapeComponents();
    
        // Itera su ogni componente e disegnalo utilizzando Graphics2D
        for (Object component : components) {
            if (component instanceof ShapeComponent) {
                ShapeComponent shapeComponent = (ShapeComponent) component;
                if (shapeComponent.getGradient() != null) {
                    g.setPaint(shapeComponent.getGradient());
                } else {
                    g.setColor(shapeComponent.getColor());
                }
                g.fill(shapeComponent.getShape());
            } else if (component instanceof TextComponent) {
                TextComponent textComponent = (TextComponent) component;
                g.setFont(textComponent.getFont());
                g.setColor(textComponent.getColor());
    
                // Centrare il testo nella pallina
                FontMetrics metrics = g.getFontMetrics(textComponent.getFont());
                int textWidth = metrics.stringWidth(textComponent.getText());
                int textHeight = metrics.getAscent();
    
                double textX = textComponent.getX() - textWidth / 2.0;
                double textY = textComponent.getY() + textHeight / 2.0;
    
                g.drawString(textComponent.getText(), (float) textX, (float) textY);
            }
        }
    }
}

