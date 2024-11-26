import java.awt.*;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.event.MouseInputListener;

public class GameView extends JPanel implements MouseInputListener
{
    private static final int TABLE_WIDTH = 1200;
    private static final int TABLE_HEIGHT = 600;
    public static final int BORDER_WIDTH = 50;
    public static final int POCKET_RADIUS = 30;

    private BilliardController cntrl;


    public GameView(BilliardController cntrl) {
        this.cntrl = cntrl;

        Timer timer = new Timer(5, e -> {
            cntrl.stepNext();
            repaint();
        });
        timer.start();
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
        for (Ball ball : cntrl.listBalls()) {
            drawBall(g2, ball);
        }
    }

    public void drawBall(Graphics2D g, Ball ball) {
        // Ottieni i componenti della pallina (i vari elementi grafici da disegnare)
        java.util.List<Object> components = ball.getShapeComponents();

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


    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseClicked'");
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mousePressed'");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseReleased'");
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseEntered'");
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseExited'");
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseDragged'");
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseMoved'");
    }
    
}
