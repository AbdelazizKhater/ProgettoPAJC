package it.unibs.pajc;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

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

        // Disegno trapezi
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
        for (BallInfo ballInfo : cntrl.getBallInfos()) {
            drawBall(g2, ballInfo);
        }

        if (cntrl.checkAllStationary()) {
            if (isHitting) {
                releaseStick(); 
            }

            drawStick(g2, cntrl.getWhiteBall(), cntrl.getStick());

        }

    }

    public void drawBall(Graphics2D g, BallInfo ballInfo) {

        Shape transformedShape = new Area(new Ellipse2D.Double(ballInfo.getX() - ballInfo.getRadius(),
                ballInfo.getY() - ballInfo.getRadius(), ballInfo.getRadius() * 2, ballInfo.getRadius() * 2));

        // Disegna il cerchio principale
        g.setColor(getBallColor(ballInfo.getNumber()));
        g.fill(transformedShape);

        // Bande per le palline dalla 9 alla 15
        if (ballInfo.getNumber() >= 9) {

            // Crea un'area di clipping limitata al cerchio della pallina
            Shape clippingArea = transformedShape;

            int bandHeight = ballInfo.getRadius() / 3; // Altezza delle bande

            // Banda superiore (clipping limitato)
            Rectangle bandTop = new Rectangle((int) (ballInfo.getX() - ballInfo.getRadius()),
                    (int) (ballInfo.getY() - ballInfo.getRadius()), ballInfo.getRadius() * 2, bandHeight);
            Area bandTopClipped = new Area(clippingArea);
            bandTopClipped.intersect(new Area(bandTop));

            // Banda inferiore (clipping limitato)
            Rectangle bandBottom = new Rectangle((int) (ballInfo.getX() - ballInfo.getRadius()),
                    (int) (ballInfo.getY() + ballInfo.getRadius() - bandHeight), ballInfo.getRadius() * 2, bandHeight);
            Area bandBottomClipped = new Area(clippingArea);
            bandBottomClipped.intersect(new Area(bandBottom));

            g.setColor(Color.WHITE);

            // Aggiungi le bande come componenti grafici
            g.fill(bandBottomClipped);
            g.fill(bandTopClipped);
        }

        // Disegna il cerchio bianco per il numero (se non è la pallina bianca)
        if (ballInfo.getNumber() > 0) {
            int innerDiameter = ballInfo.getRadius();
            g.setColor(Color.WHITE);
            g.fillOval((int) (ballInfo.getX() - innerDiameter / 2.0),
                    (int) (ballInfo.getY() - innerDiameter / 2.0),
                    innerDiameter,
                    innerDiameter);

            // Disegna il numero
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, (int) (ballInfo.getRadius() / 1.5)));
            String number = String.valueOf(ballInfo.getNumber());
            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(number);
            int textHeight = metrics.getAscent();
            g.drawString(number, (int) (ballInfo.getX() - textWidth / 2.0),
                    (int) (ballInfo.getY() + textHeight / 2.0));
        }
    }

    private Color getBallColor(int number) {
        return BALL_COLORS[number];

    }

    private void visualizeCueBallReposition(Graphics2D g2) {
        if (mousePoint != null) {
            if (isWithinBounds(mousePoint.x, mousePoint.y)) {
                g2.setColor(Color.WHITE);
            } else {
                // Pallina out of bounds
                g2.setColor(Color.RED);
            }
            g2.fillOval(mousePoint.x - BALL_RADIUS, mousePoint.y - BALL_RADIUS,
                    BALL_RADIUS * 2, BALL_RADIUS * 2);
        }
    }

    private void drawStick(Graphics2D g, Ball cueBall, Stick stick) {
        g.setStroke(new BasicStroke(10));
        g.setColor(Color.BLACK);

        double stickDistance = cueBall.getBallRadius() + 10 + stick.getVisualPower();
        double stickLength = 300; // Lunghezza totale della stecca

        double startX = cueBall.getX() + stickDistance * Math.cos(Math.toRadians(stick.getAngleDegrees()));
        double startY = cueBall.getY() + stickDistance * Math.sin(Math.toRadians(stick.getAngleDegrees()));
        double endX = cueBall.getX()
                + (stickDistance + stickLength) * Math.cos(Math.toRadians(stick.getAngleDegrees()));
        double endY = cueBall.getY()
                + (stickDistance + stickLength) * Math.sin(Math.toRadians(stick.getAngleDegrees()));

        g.drawLine((int) startX, (int) startY, (int) endX, (int) endY);
    }

    public void releaseStick() {
        if (!cntrl.reduceStickVisualPowerForAnimation()) {
            cntrl.onStickAnimationComplete(); // Notifica che l'animazione è completata
            isHitting = false;
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

            double deltaX = e.getX() - dragStartX;
            double deltaY = e.getY() - dragStartY;

            cntrl.updateStickPower(deltaX, deltaY);

        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        if (!cntrl.getWhiteBall().isInPlay()) {
            mousePoint = getMousePosition();
        } else if (!isHitting && !isCharging) {

            cntrl.updateStickAngle(e.getX(), e.getY());
        }
    }

    public boolean isWithinBounds(int x, int y) {
        return x > MIN_BOUND && x < MAX_BOUND_X && y > MIN_BOUND && y < MAX_BOUND_Y;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();
        if (!cntrl.getWhiteBall().isInPlay() && isWithinBounds(mouseX, mouseY)) {
            System.out.println("sdkfgh");
            cntrl.getWhiteBall().setPosition(mouseX, mouseY);
            cntrl.getWhiteBall().setInPlay(true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        if (cntrl.isStickCharged())
            isHitting = true;

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
