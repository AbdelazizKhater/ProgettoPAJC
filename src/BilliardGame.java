import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BilliardGame extends JPanel implements ActionListener {
    private List<Ball> balls;
    private Timer timer;

    public BilliardGame() {
        balls = new ArrayList<>();
        Random rand = new Random();

        // Aggiunge 5 palline con posizioni e velocità casuali
        for (int i = 0; i < 5; i++) {
            int x = rand.nextInt(400) + 50;
            int y = rand.nextInt(400) + 50;
            double vx = rand.nextDouble() * 40 - 2;
            double vy = rand.nextDouble() * 40 - 2;
            Color color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            balls.add(new Ball(x, y, vx, vy, color, i + 1));
        }

        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Ball ball : balls) {
            ball.draw(g2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Ball ball : balls) {
            ball.updatePosition();
            ball.checkBounds(getWidth(), getHeight());
        }
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Billiard Game");
        BilliardGame game = new BilliardGame();
        frame.add(game);
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class Ball {
    private double x, y; // Posizione
    private double vx, vy; // Velocità
    private final int radius = 20;
    private final Color color;
    private final int number; // Numero della pallina
    private double accumulatedDistance = 0; // Distanza totale per l'effetto rotazione

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

        // Aggiorna la distanza accumulata in base alla velocità
        accumulatedDistance += Math.sqrt(vx * vx + vy * vy);

        // Riduce la velocità per simulare l'attrito
        vx *= 0.98;
        vy *= 0.98;
    }

    public void checkBounds(int width, int height) {
        // Controlla i limiti della finestra per rimbalzo
        if (x - radius < 0 || x + radius > width) {
            vx = -vx;
        }
        if (y - radius < 0 || y + radius > height) {
            vy = -vy;
        }
    }

    public void draw(Graphics2D g) {
        // Disegna il corpo della pallina
        g.setColor(color);
        g.fillOval((int) x - radius, (int) y - radius, radius * 2, radius * 2);

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
}
