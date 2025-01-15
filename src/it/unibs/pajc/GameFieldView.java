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

    /**
     * Carica le immagini utilizzate per il campo e le stecche.
     */
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
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw table background
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }



        // Draw each ball
        for (BallInfo ballInfo : cntrl.getBallInfos()) {
            drawBall(g2, ballInfo);
        }

        if (cntrl.checkAllStationary()) {
            if (isHitting) {
                releaseStick();
            }

            
            if(isMyTurn && cntrl.getGameStatus() != GameStatus.completed) {
                if (!cntrl.cueBallNeedsReposition()) {
                    drawStick(g2, cntrl.getCueBall(), cntrl.getStick(), cntrl.getCurrentPlayerIndex() == 0 ? blueStickImage : redStickImage);
                    drawTrajectory(g2, cntrl.calculateTrajectory());
                } else {
                    visualizeCueBallReposition(g2);
                }
            }
        }
    }


    /**
     * Disegna la linea di collisione con le altre biglie.
     */
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
                    (int) (trajectoryInfo[i].endX * scaleFactor),
                    (int) (trajectoryInfo[i].endY * scaleFactor));
        }

        g.draw(new Ellipse2D.Double((trajectoryInfo[0].endX - BALL_RADIUS) * scaleFactor,
                (trajectoryInfo[0].endY - BALL_RADIUS) * scaleFactor,
                2 * BALL_RADIUS * scaleFactor, 2 * BALL_RADIUS * scaleFactor));

        // Ripristina il contesto grafico originale
        g.setTransform(originalTransform);

    }

    /**
     * Metodo principale che gestisce la grafica di ogni biglia, a seconda del proprio id.
     */
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
            int bandHeight = (int) (scaledRadius / 3); 

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

    /**
     * Visualizza la biglia bianca quando si può riposizionare, rossa se si sta provando a riposizionare
     * sul bordo o su altre biglie.
     */
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

    /**
     * Metodo principale per il disegno della stecca.
     */
    private void drawStick(Graphics2D g, Ball cueBall, Stick stick, Image stickImage) {

        double stickDistance = cueBall.getBallRadius() + 10 + stick.getVisualPower()*3;
        double stickAngleRadians = Math.toRadians(stick.getAngleDegrees()) ;
        double stickX = cueBall.getX() + stickDistance * Math.cos(stickAngleRadians);
        double stickY = cueBall.getY() + stickDistance * Math.sin(stickAngleRadians);

        AffineTransform originalTransform = g.getTransform();

        g.translate(stickX, stickY);
        g.rotate(stickAngleRadians + Math.PI/2);

        // Center the stick image relative to its width (13 pixels)
        int stickWidth = 13;
        int stickHeight = 421;
        g.drawImage(stickImage, -stickWidth / 2, -stickHeight, stickWidth, stickHeight, this);

        g.setTransform(originalTransform);
    }

    /**
     * Una volta rilasciato il mouse, viene utilizzato questo metodo per gestire l'animazione del colpo.
     */
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

        // Riposiziona la biglia bianca se la posizione è valida
        if (isWithinBounds(mouseX, mouseY) && !cntrl.isAnyBallInSight(mouseX, mouseY) && isMyTurn && cntrl.getGameStatus() != GameStatus.completed) {
            cntrl.resetCueBallPosition(mouseX, mouseY);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        if (!isHitting && !cntrl.getCueBall().needsReposition() && isMyTurn  && cntrl.getGameStatus() != GameStatus.completed) {

            double dragX = e.getX();
            double dragY = e.getY();

            double cueAngleDegrees = cntrl.stickAngleDirection(); 
            double cueAngleRadians = Math.toRadians(cueAngleDegrees); 
            double cueDirX = Math.cos(cueAngleRadians);
            double cueDirY = Math.sin(cueAngleRadians);

            // Calcola la differenza tra la posizione iniziale e quella attuale
            double deltaX = dragX - dragStartX;
            double deltaY = dragY - dragStartY;

            // Proiezione del vettore di trascinamento sulla direzione della stecca
            double projection = deltaX * cueDirX + deltaY * cueDirY;
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
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
       
    }

}
