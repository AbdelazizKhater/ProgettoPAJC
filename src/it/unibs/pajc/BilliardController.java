package it.unibs.pajc;

import it.unibs.pajc.fieldcomponents.Ball;
import it.unibs.pajc.fieldcomponents.BallInfo;
import it.unibs.pajc.fieldcomponents.GameFieldObject;
import it.unibs.pajc.fieldcomponents.Stick;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import java.awt.geom.Point2D;

import static it.unibs.pajc.util.CostantiStatiche.*;

public class BilliardController {

    private final GameField model;

    public BilliardController(GameField model) {
        this.model = model;
    }

    public void stepNext() {
        model.stepNext();
    }

    public Player[] getPlayers() {
        return model.getPlayers();
    }

    public List<BallInfo> getBallInfos() {
        return model.getBalls().stream()
                .map(ball -> new BallInfo(ball.getX(), ball.getY(), ball.getRadius(), ball.getNumber()))
                .toList();
    }

    public List<Ball> getBalls() {
        return model.getBalls();
    }

    public Stick getStick() {
        return model.getStick();
    }

    public Ball getCueBall() {
        return model.getCueBall();
    }

    public Boolean checkAllStationary() {
        return model.allBallsAreStationary();
    }

    public boolean isMyTurn(Player p) {
        return p != null && model.getCurrentPlayer().id == p.getId();
    }

    public GameStatus getGameStatus() {
        return model.getStatus();
    }

    public void resetRound() {
        model.resetRound();
    }

    public Player getCurrentPlayer() {
        return model.getCurrentPlayer();
    }

    public void handleMouseDragged(double deltaX, double deltaY) {

        Stick stick = model.getStick();

        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        stick.setPower(Math.min(distance, Stick.MAX_POWER));
    }

    public ArrayList<Integer> getPottedBallsId() {

        return model.getPottedBallsId();

    }

    public boolean isBallsAssigned() {
        return model.isBallsAssigned();
    }

    public void updateStickAngle(int mouseX, int mouseY) {

        Stick stick = model.getStick();
        Ball cueBall = model.getCueBall();

        // Calcola il delta rispetto alla posizione della pallina bianca
        double deltaX = mouseX - cueBall.getX();
        double deltaY = mouseY - cueBall.getY();

        // Calcola l'angolo rispetto alla pallina bianca
        double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
        stick.setAngleDegrees(angle);
    }

    public void updateStickPower(double deltaX, double deltaY) {
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        model.getStick().setPower(Math.min(distance, Stick.MAX_POWER));
    }

    public boolean isStickCharged() {
        return model.getStick().getPower() > 2;
    }

    public boolean reduceStickVisualPowerForAnimation() {
        return model.reduceStickVisualPower(10); // Velocità dell'animazione
    }

    public void onStickAnimationComplete() {
        model.hitBall();
    }

    public TrajectoryInfo calculateTrajectory() {
        double angle = Math.toRadians(model.getStick().getAngleDegrees() + 180);
        double dx = Math.cos(angle);
        double dy = Math.sin(angle);

        double startX = model.getCueBall().getX() + model.getCueBall().getRadius() * dx;
        double startY = model.getCueBall().getY() + model.getCueBall().getRadius() * dy;

        // Inizializza le coordinate finali
        double endX = Double.MAX_VALUE;
        double endY = Double.MAX_VALUE;

        // Bordo sinistro (x = INNER_MARGIN)
        if (dx != 0) {
            double t = (INNER_MARGIN - startX) / dx;
            double y = startY + t * dy;
            if (t > 0 && y >= INNER_MARGIN && y <= INNER_FIELD_LIMIT_Y) {
                endX = INNER_MARGIN + BALL_RADIUS;
                endY = y;
            }
        }

        // Bordo destro (x = INNER_FIELD_LIMIT_X)
        if (dx != 0) {
            double t = (INNER_FIELD_LIMIT_X - startX) / dx;
            double y = startY + t * dy;
            if (t > 0 && y >= INNER_MARGIN && y <= INNER_FIELD_LIMIT_Y) {
                endX = INNER_FIELD_LIMIT_X - BALL_RADIUS;
                endY = y;
            }
        }

        // Bordo superiore (y = INNER_MARGIN)
        if (dy != 0) {
            double t = (INNER_MARGIN - startY) / dy;
            double x = startX + t * dx;
            if (t > 0 && x >= INNER_MARGIN && x <= INNER_FIELD_LIMIT_X) {
                endX = x;
                endY = INNER_MARGIN + BALL_RADIUS;
            }
        }

        // Bordo inferiore (y = INNER_FIELD_LIMIT_Y)
        if (dy != 0) {
            double t = (INNER_FIELD_LIMIT_Y - startY) / dy;
            double x = startX + t * dx;
            if (t > 0 && x >= INNER_MARGIN && x <= INNER_FIELD_LIMIT_X) {
                endX = x;
                endY = INNER_FIELD_LIMIT_Y - BALL_RADIUS;
            }
        }

        // Crea l'oggetto GameFieldObject rappresentante la traiettoria
        double length = Math.sqrt(Math.pow(startY - endY, 2) + Math.pow(startX - endX, 2));
        Rectangle rectangle = new Rectangle(0, 0, BALL_RADIUS * 2, (int) length);
        AffineTransform t = new AffineTransform();

        t.translate(startX - BALL_RADIUS * Math.sin(angle), startY + BALL_RADIUS * Math.cos(angle));
        t.rotate(angle - Math.toRadians(90));

        GameFieldObject trajectory = new GameFieldObject(t.createTransformedShape(rectangle));

        double shortestDistance = Double.MAX_VALUE;

        for (Ball ball : model.getBalls()) {
            if (ball.checkCollision(trajectory)) {
                if (ball.equals(model.getCueBall())) {
                    continue; // Salta la pallina bianca
                }

                Point2D intersection = getCircleCircleIntersection(
                        startX, startY, dx, dy, ball.getX(), ball.getY(), model.getCueBall().getRadius());

                if (intersection != null) {
                    double distance = intersection.distance(startX, startY);
                    if (distance < shortestDistance) {
                        shortestDistance = distance;
                        endX = intersection.getX();
                        endY = intersection.getY();
                    }
                }
            }
        }

        return new TrajectoryInfo(startX, startY, endX, endY);
    }

    private Point2D getCircleCircleIntersection(double x1, double y1, double dx, double dy,
            double cx, double cy, double radius) {
        // Sposta il sistema di coordinate: il centro della cue ball è l'origine
        double relX = cx - x1;
        double relY = cy - y1;

        // Distanza tra i due centri richiesta per la collisione
        double collisionDistance = 2 * radius;

        // Proiezione del centro della pallina bersaglio sulla traiettoria
        double projectionLength = (relX * dx + relY * dy) / Math.sqrt(dx * dx + dy * dy);

        // Calcola il punto più vicino sulla traiettoria al centro della pallina
        // bersaglio
        double closestX = x1 + projectionLength * dx;
        double closestY = y1 + projectionLength * dy;

        // Distanza minima tra la traiettoria e il centro della pallina bersaglio
        double distanceToCenter = Math.sqrt(Math.pow(closestX - cx, 2) + Math.pow(closestY - cy, 2));

        // Verifica che la distanza minima sia compatibile con la collisione
        if (distanceToCenter > collisionDistance) {
            return null; // Nessuna intersezione
        }

        // Calcola la distanza lungo la traiettoria al punto di impatto
        double impactDistance = Math.sqrt(collisionDistance * collisionDistance - distanceToCenter * distanceToCenter);

        // Calcola il punto di intersezione finale
        double intersectionX = closestX - impactDistance * dx;
        double intersectionY = closestY - impactDistance * dy;

        return new Point2D.Double(intersectionX, intersectionY);
    }

    public void resetCueBallPosition(int x, int y) {
        Ball cueBall = model.getCueBall();
        if (cueBall.needsReposition()) {
            cueBall.setPosition(x, y);
            model.setStatus(GameStatus.cueBallRepositioning);
            cueBall.setNeedsReposition(false);
            model.getBalls().addFirst(cueBall);
            model.resetRound();
        }
    }

    public boolean cueBallNeedsReposition() {
        return model.getCueBall().needsReposition();
    }

    public void setStatus(GameStatus status) {
        model.setStatus(status);
    }
}
