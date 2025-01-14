package it.unibs.pajc;

import it.unibs.pajc.fieldcomponents.Ball;
import it.unibs.pajc.fieldcomponents.BallInfo;
import it.unibs.pajc.fieldcomponents.Stick;

import static it.unibs.pajc.util.CostantiStatiche.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

public class GameFieldView extends JPanel implements MouseMotionListener, MouseListener {

    public static final int MAX_POWER = 80;
    private Boolean isHitting = false;
    private Point mousePoint;
    protected Image backgroundImage;
    protected Image redStickImage;
    protected Image blueStickImage;
    protected final BilliardController cntrl;
    protected boolean isMyTurn;

    public GameFieldView(BilliardController cntrl) {
        this.cntrl = cntrl;
        loadImage();
        isMyTurn = true;
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
    }

    private void loadImage() {
        // Replace "background.jpg" with your actual image file path
        backgroundImage = Toolkit.getDefaultToolkit().getImage("resources/background.png");
        redStickImage = Toolkit.getDefaultToolkit().getImage("resources/red_stick.png");
        blueStickImage = Toolkit.getDefaultToolkit().getImage("resources/blue_stick.png");

        // Ensures the image is fully loaded
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(backgroundImage, 0);
        try {
            tracker.waitForAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        paintComponentLogic(g);
    }

    protected void paintComponentLogic(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        // Draw table background

        // Draw each ball
        for (BallInfo ballInfo : cntrl.getBallInfos()) {
            drawBall(g2, ballInfo);
        }

        if (cntrl.checkAllStationary()) {
            if (isHitting) {
                releaseStick();
            }

            if(isMyTurn) {
                if (!cntrl.cueBallNeedsReposition()) {
                    drawStick(g2, cntrl.getCueBall(), cntrl.getStick(), cntrl.getCurrentPlayerIndex() == 0 ? blueStickImage : redStickImage);
                    drawTrajectory(g2, cntrl.calculateTrajectory());
                } else {
                    visualizeCueBallReposition(g2);
                }
            }
            // Se non è stato commesso nessun fallo, si procede con il turno regolarmente
        }
    }

    public void drawTrajectory(Graphics2D g, TrajectoryInfo[] trajectoryInfo) {

        AffineTransform originalTransform = g.getTransform();
        double scaleFactor = 1000;

        g.scale(1 / scaleFactor, 1 / scaleFactor);

        // Disegna con precisione maggiore
        g.setStroke(new BasicStroke((int) scaleFactor * 2));
        g.setColor(Color.LIGHT_GRAY);

        for (int i = 0; i < trajectoryInfo.length; i++) {

            g.drawLine(
                    (int) (trajectoryInfo[i].startX * scaleFactor),
                    (int) (trajectoryInfo[i].startY * scaleFactor),
                    (int) (trajectoryInfo[i].directionX * scaleFactor),
                    (int) (trajectoryInfo[i].directionY * scaleFactor));
        }

        g.draw(new Ellipse2D.Double((trajectoryInfo[0].directionX - BALL_RADIUS) * scaleFactor,
                (trajectoryInfo[0].directionY - BALL_RADIUS) * scaleFactor,
                2 * BALL_RADIUS * scaleFactor, 2 * BALL_RADIUS * scaleFactor));

        // Ripristina il contesto grafico originale
        g.setTransform(originalTransform);

    }

    public void drawBall(Graphics2D g, BallInfo ballInfo) {
        AffineTransform originalTransform = g.getTransform();
        double scaleFactor = 1000;

        // Applica una scala inversa
        g.scale(1 / scaleFactor, 1 / scaleFactor);

        // Scala le coordinate e i raggi manualmente
        double scaledX = ballInfo.getX() * scaleFactor;
        double scaledY = ballInfo.getY() * scaleFactor;
        double scaledRadius = ballInfo.getRadius() * scaleFactor;

        Shape transformedShape = new Area(new Ellipse2D.Double(
                scaledX - scaledRadius,
                scaledY - scaledRadius,
                scaledRadius * 2,
                scaledRadius * 2));

        // Disegna il cerchio principale
        g.setColor(getBallColor(ballInfo.getNumber()));
        g.fill(transformedShape);

        // Bande per le palline dalla 9 alla 15
        if (ballInfo.getNumber() >= 9) {
            // Crea un'area di clipping limitata al cerchio della pallina
            Shape clippingArea = transformedShape;

            int bandHeight = (int) (scaledRadius / 3); // Altezza delle bande

            // Banda superiore (clipping limitato)
            Rectangle bandTop = new Rectangle(
                    (int) (scaledX - scaledRadius),
                    (int) (scaledY - scaledRadius),
                    (int) (scaledRadius * 2),
                    bandHeight);
            Area bandTopClipped = new Area(clippingArea);
            bandTopClipped.intersect(new Area(bandTop));

            // Banda inferiore (clipping limitato)
            Rectangle bandBottom = new Rectangle(
                    (int) (scaledX - scaledRadius),
                    (int) (scaledY + scaledRadius - bandHeight),
                    (int) (scaledRadius * 2),
                    bandHeight);

            Area bandBottomClipped = new Area(clippingArea);
            bandBottomClipped.intersect(new Area(bandBottom));

            g.setColor(Color.WHITE);

            // Disegna le bande come componenti grafici
            g.fill(bandBottomClipped);
            g.fill(bandTopClipped);
        }

        // Disegna il cerchio bianco per il numero (se non è la pallina bianca)
        if (ballInfo.getNumber() > 0) {
            double innerDiameter = scaledRadius;
            g.setColor(Color.WHITE);
            g.fillOval(
                    (int) (scaledX - innerDiameter / 2.0),
                    (int) (scaledY - innerDiameter / 2.0),
                    (int) innerDiameter,
                    (int) innerDiameter);

            // Disegna il numero
            g.setColor(Color.BLACK);
            g.setFont(new Font("Open Sans", Font.BOLD, (int) (scaledRadius / 1.5)));
            String number = String.valueOf(ballInfo.getNumber());
            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(number);
            int textHeight = metrics.getAscent();
            g.drawString(
                    number,
                    (int) (scaledX - textWidth / 2.0),
                    (int) (scaledY + textHeight / 2.0));
        }

        // Ripristina il contesto grafico originale
        g.setTransform(originalTransform);
    }

    private Color getBallColor(int number) {
        return BALL_COLORS[number];
    }

    private void visualizeCueBallReposition(Graphics2D g2) {
        if (mousePoint != null && isMyTurn) {
            if (isWithinBounds(mousePoint.x, mousePoint.y) && !cntrl.isAnyBallInSight(mousePoint.x, mousePoint.y)) {
                g2.setColor(Color.WHITE);
            } else {
                // Pallina out of bounds
                g2.setColor(Color.RED);
            }
            g2.fillOval(mousePoint.x - BALL_RADIUS, mousePoint.y - BALL_RADIUS,
                    BALL_RADIUS * 2, BALL_RADIUS * 2);
        }
    }

    private void drawStick(Graphics2D g, Ball cueBall, Stick stick, Image stickImage) {
        // Calculate the stick's position and rotation
        double stickDistance = cueBall.getBallRadius() + 10 + stick.getVisualPower()*3;
        double stickAngleRadians = Math.toRadians(stick.getAngleDegrees()) ;
        double stickX = cueBall.getX() + stickDistance * Math.cos(stickAngleRadians);
        double stickY = cueBall.getY() + stickDistance * Math.sin(stickAngleRadians);

        // Save the original transform
        AffineTransform originalTransform = g.getTransform();

        // Move to the stick's position and rotate around its top-left corner
        g.translate(stickX, stickY);
        g.rotate(stickAngleRadians + Math.PI/2);

        // Center the stick image relative to its width (13 pixels)
        int stickWidth = 13;
        int stickHeight = 421;
        g.drawImage(stickImage, -stickWidth / 2, -stickHeight, stickWidth, stickHeight, this);

        // Restore the original transform
        g.setTransform(originalTransform);
    }

    public void releaseStick() {
        if (!cntrl.reduceStickVisualPowerForAnimation()) {
            cntrl.onStickAnimationComplete(); // Notifica che l'animazione è completata
            isHitting = false;
            SoundControl.CUEBALL_HIT.play();
        }
    }

    private int dragStartX;
    private int dragStartY;
    private Boolean isCharging = false;

    @Override
    public void mousePressed(MouseEvent e) {

        int mouseX = e.getX();
        int mouseY = e.getY();

        dragStartX = mouseX;
        dragStartY = mouseY;
        isCharging = true;

        if (isWithinBounds(mouseX, mouseY) && !cntrl.isAnyBallInSight(mouseX, mouseY) && isMyTurn) {
            cntrl.resetCueBallPosition(mouseX, mouseY);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!isHitting && !cntrl.getCueBall().needsReposition() && isMyTurn) {
            // Get the current drag position
            double dragX = e.getX();
            double dragY = e.getY();

            // Direction vector of the cue (convert angle from degrees to radians)
            double cueAngleDegrees = cntrl.stickAngleDirection(); // Angle in degrees
            double cueAngleRadians = Math.toRadians(cueAngleDegrees); // Convert to radians
            double cueDirX = Math.cos(cueAngleRadians);
            double cueDirY = Math.sin(cueAngleRadians);

            // Calculate displacement from the drag start
            double deltaX = dragX - dragStartX;
            double deltaY = dragY - dragStartY;

            // Project displacement onto the cue's direction
            double projection = deltaX * cueDirX + deltaY * cueDirY;

            // Update stick power using the projection magnitude
            cntrl.updateStickPower(projection);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (cntrl.cueBallNeedsReposition()) {
            mousePoint = getMousePosition();
        } else if (!isHitting && !isCharging) {
            cntrl.updateStickAngle(e.getX(), e.getY());
        }
    }

    public boolean isWithinBounds(int x, int y) {
        double maxBoundX = (cntrl.getRoundNumber() == 0) ? MAX_BOUND_X_FIRST_ROUND : MAX_BOUND_X;
        return x > MIN_BOUND && x < maxBoundX && y > MIN_BOUND && y < MAX_BOUND_Y;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (cntrl.isStickCharged()) {
            isHitting = true;
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
