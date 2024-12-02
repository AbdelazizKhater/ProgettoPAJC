package it.unibs.pajc;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.*;

import static it.unibs.pajc.CostantiStatiche.*;

public class GameView extends JPanel implements MouseMotionListener, MouseListener {

    public static final int MAX_POWER = 80;
    private Point mousePoint;
    private final BilliardController cntrl;
    private Boolean isHitting = false;

    public GameView(BilliardController cntrl) {
        this.cntrl = cntrl;

        this.addMouseMotionListener(this);
        this.addMouseListener(this);

        Timer timer = new Timer(1, e -> {
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

        g2.setColor(new Color(109, 39, 9));

        //Disegno trapezi
        for (int i = 0; i < X_POINTS_TRAPEZI.length; i++) {
            g2.fillPolygon(X_POINTS_TRAPEZI[i], Y_POINTS_TRAPEZI[i], 4);
        }

        // Draw pockets
        g2.setColor(Color.BLACK);
        for (int[] pocket : POCKET_POSITIONS) {
            int x = pocket[0];
            int y = pocket[1];
            int width = pocket[2];
            int height = pocket[3];
            g2.fillOval(x, y, width, height);
        }

        // Draw each ball
        for (Ball ball : cntrl.listBalls()) {
            drawBall(g2, ball);
        }

        if (cntrl.checkAllStationary()) {
            //Funzionamento normale, viene disegnata la stecca
            if(cntrl.getWhiteBall().isInPlay()) {
                drawStick(g2, cntrl.getWhiteBall(), cntrl.getStick(), isHitting);
            }
            //Pallina bianca in buca
            else {
                visualizeCueBallReposition(g2);
            }
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

    private void visualizeCueBallReposition(Graphics2D g2) {
        if(mousePoint != null)
        {
            if (isWithinBounds(mousePoint.x, mousePoint.y)) {
                g2.setColor(Color.WHITE);
            } else {
                //Pallina out of bounds
                g2.setColor(Color.RED);
            }
            g2.fillOval(mousePoint.x - BALL_RADIUS, mousePoint.y - BALL_RADIUS,
                    BALL_RADIUS * 2, BALL_RADIUS * 2);
        }
    }
    private void drawStick(Graphics2D g, Ball whiteBall, Stick stick, Boolean isHitting) {
        // Salva lo stato originale del Graphics2D
        Shape originalClip = g.getClip();
    
        // Imposta il clipping all'area visibile del pannello
        g.setClip(0, 0, this.getWidth(), this.getHeight());
    
        g.setStroke(new BasicStroke(10));
        g.setColor(Color.BLACK);
    
        
        if (isHitting)
            releaseStick(stick, whiteBall);
    
        double stickDistance = whiteBall.getBallRadius() + (isHitting ? 0 : 10) + stick.getPower();
        double stickLength = 300; // Lunghezza totale della stecca
    
        // Calcola le coordinate della stecca
        double startX = whiteBall.getX() + stickDistance * Math.cos(stick.getAngleRadians());
        double startY = whiteBall.getY() + stickDistance * Math.sin(stick.getAngleRadians());
        double endX = whiteBall.getX() + (stickDistance + stickLength) * Math.cos(stick.getAngleRadians());
        double endY = whiteBall.getY() + (stickDistance + stickLength) * Math.sin(stick.getAngleRadians());
    
        // Disegna la linea della stecca (limitata dal clipping)
        g.drawLine((int) startX, (int) startY, (int) endX, (int) endY);
    
        // Ripristina lo stato originale del Graphics2D
        g.setClip(originalClip);
    }
    

    private double originalStickPower;

    public void releaseStick(Stick stick, Ball whiteBall) {
        final double RETURN_SPEED = 15;

        if (stick.getPower() > 0) {
            // Riduci solo la distanza visiva della stecca
            stick.setPower(Math.max(0, stick.getPower() - RETURN_SPEED));

        } else {

            stick.setPower(originalStickPower);
            cntrl.hitBall();
            isHitting = false;
            stick.setPower(0);
        }

    }


    private int dragStartX;
    private int dragStartY;
    private Boolean isCharging = false;

    @Override
    public void mousePressed(MouseEvent e) {
        dragStartX = e.getX();
        dragStartY = e.getY();
        isCharging = true;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!isHitting) {
            Stick stick = cntrl.getStick();

            int mouseX = e.getX();
            int mouseY = e.getY();

            double deltaX = mouseX - dragStartX;
            double deltaY = mouseY - dragStartY;

            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            stick.setPower(Math.min(distance, MAX_POWER));

        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        if (!cntrl.getWhiteBall().isInPlay()) {
            mousePoint = getMousePosition();
        }
        else if (!isHitting && !isCharging) {
            Ball whiteBall = cntrl.getWhiteBall();
            Stick stick = cntrl.getStick();

            // Ottieni le coordinate del mouse
            int mouseX = e.getX();
            int mouseY = e.getY();

            // Calcola l'angolo tra il centro della palla e il mouse
            double ballX = whiteBall.getX();
            double ballY = whiteBall.getY();
            double angle = Math.atan2(mouseY - ballY, mouseX - ballX); // Angolo in radianti

            // Converti l'angolo in gradi e aggiornalo nella stecca
            stick.setAngleDegrees(Math.toDegrees(angle));
        }
    }
    public boolean isWithinBounds(int x, int y) {
        return  x > MIN_BOUND && x < MAX_BOUND_X && y > MIN_BOUND && y < MAX_BOUND_Y;
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();
        if(!cntrl.getWhiteBall().isInPlay() && isWithinBounds(mouseX, mouseY)) {
            System.out.println("sdkfgh");
            cntrl.getWhiteBall().setPosition(mouseX, mouseY);
            cntrl.getWhiteBall().setInPlay(true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        if (cntrl.getStick().getPower() > 2) {
            isHitting = true;
            originalStickPower = cntrl.getStick().getPower();
        } else {
            cntrl.getStick().setPower(0);
        }

        isCharging = false;

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

}
