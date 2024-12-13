package it.unibs.pajc;

import java.util.ArrayList;

import javax.swing.JPanel;

import it.unibs.pajc.fieldcomponents.BallInfo;

import static it.unibs.pajc.util.CostantiStatiche.BALL_COLORS;
import static it.unibs.pajc.util.CostantiStatiche.BALL_RADIUS;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.List;

public class InformationPanel extends JPanel {

    private BallsPanel player1BallsPanel;
    private BallsPanel player2BallsPanel;

    private final BilliardController cntrl;

    public InformationPanel(BilliardController cntrl) {
        super();
        this.cntrl = cntrl;

        player1BallsPanel = new BallsPanel(cntrl.getPottedBallsId(), cntrl.getPlayers()[0].getName(),
        true, false);

        player2BallsPanel = new BallsPanel(cntrl.getPottedBallsId(), cntrl.getPlayers()[1].getName(),
        true, false);


        this.setLayout(new GridLayout(1, 2)); // Due celle orizzontali


        JPanel player1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); // Pannello centrato
        player1Panel.setBackground(Color.gray);
        player1Panel.add(player1BallsPanel);
        add(player1Panel);

        JPanel player2Panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); // Pannello centrato
        player2Panel.setBackground(Color.gray);
        player2Panel.add(player2BallsPanel);
        add(player2Panel);
    }

    public void update() {

        player1BallsPanel.setBallsAssigned(cntrl.isBallsAssigned());
        player2BallsPanel.setBallsAssigned(cntrl.isBallsAssigned());

        player1BallsPanel.setStripedBalls(cntrl.getPlayers()[0].isStripedBalls());
        player2BallsPanel.setStripedBalls(cntrl.getPlayers()[1].isStripedBalls());
    
    }

    private class BallsPanel extends JPanel {
        private List<Integer> pottedBallsId;
        private String playerName;
        private boolean isStripedBalls;
        private boolean ballsAssigned;

        public BallsPanel(ArrayList<Integer> pottedBallsId, String playerName, boolean isStripedBalls,
                boolean ballsAssigned) {
            super();
            this.pottedBallsId = pottedBallsId;
            this.playerName = playerName;
            this.isStripedBalls = isStripedBalls;
            this.setPreferredSize(new Dimension(BALL_RADIUS * 7 * 2, 60));
            this.ballsAssigned = ballsAssigned;

            this.setBackground(Color.GRAY);
        }

        public void setBallsAssigned(boolean value) {
            this.ballsAssigned = value;
        }

        public void setStripedBalls(boolean value)
        {
            this.isStripedBalls = value;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Disegna il nome del giocatore in alto
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(Color.BLACK);
            g2.drawString(playerName, 50, 20);

            int firstId = isStripedBalls ? 9 : 1;
            int lastId = isStripedBalls ? 15 : 7;

            for (int i = firstId; i <= lastId; i++) {

                int ballx = (i - firstId) * 2 * BALL_RADIUS + BALL_RADIUS;
                int bally = this.getHeight() / 1 - BALL_RADIUS;

                if (pottedBallsId.contains(i) || !ballsAssigned) {

                    g2.setColor(Color.DARK_GRAY);
                    g2.fill(new Ellipse2D.Double(ballx - BALL_RADIUS, bally - BALL_RADIUS, BALL_RADIUS * 2,
                            BALL_RADIUS * 2));

                } else {
                    BallInfo ballInfo = new BallInfo(ballx, bally, BALL_RADIUS, i);
                    drawBall(g2, ballInfo);
                }

            }

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
            g.setColor(BALL_COLORS[ballInfo.getNumber()]);
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

    }
}
