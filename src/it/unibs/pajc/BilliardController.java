package it.unibs.pajc;

import it.unibs.pajc.fieldcomponents.Ball;
import it.unibs.pajc.fieldcomponents.BallInfo;
import it.unibs.pajc.fieldcomponents.GameFieldObject;
import it.unibs.pajc.fieldcomponents.Stick;

import java.awt.Rectangle;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import java.awt.geom.Point2D;

import static it.unibs.pajc.util.CostantiStatiche.*;

public class BilliardController {

    protected final GameField model;

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

    public int getCurrentPlayerIndex() {
        return model.getCurrentPlayerIndx();
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

    public void updateStickPower(double distance) {
        model.getStick().setPower(Math.min(distance, Stick.MAX_POWER));
    }

    public boolean isStickCharged() {
        return model.getStick().getPower() > 2;
    }

    public double stickAngleDirection() {
        return model.getStick().getAngleDegrees();
    }

    public boolean reduceStickVisualPowerForAnimation() {
        // Velocità dell'animazione del ritorno della stecca
        return model.reduceStickVisualPower(10); 
    }

    /**
     * Quando l'animazione del rituale della stecca è completata, colpisci la pallina
     */
    public void onStickAnimationComplete() {
        model.hitBall();
    }

    /**
     * Calcolo della traiettoria tramite l'angolo della stecca, la posizione della biglia bianca e la posizione
     * di ogni biglia che interseca la linea creata dal punto finale della stecca al bordo del tavolo di gioco.
     */
    public TrajectoryInfo[] calculateTrajectory() {

        double angle = Math.toRadians(model.getStick().getAngleDegrees() + 180);
        double dx = Math.cos(angle);
        double dy = Math.sin(angle);

        double startX = model.getCueBall().getX();
        double startY = model.getCueBall().getY();

        // Inizializza le coordinate finali ad un valore molto alto
        double endX = Double.MAX_VALUE;
        double endY = Double.MAX_VALUE;

        // Bordo sinistro (x = INNER_MARGIN)
        if (dx != 0) {
            double t = (INNER_MARGIN + BALL_RADIUS - startX) / dx; 
            double y = startY + t * dy;
            if (t > 0 && y >= INNER_MARGIN + BALL_RADIUS + 1 && y <= INNER_FIELD_LIMIT_Y - BALL_RADIUS - 1) {
                endX = INNER_MARGIN + BALL_RADIUS;
                endY = y;
            }
        }

        // Bordo destro (x = INNER_FIELD_LIMIT_X)
        if (dx != 0) {
            double t = (INNER_FIELD_LIMIT_X - BALL_RADIUS - startX) / dx; 
            double y = startY + t * dy;
            if (t > 0 && y >= INNER_MARGIN + BALL_RADIUS + 1 && y <= INNER_FIELD_LIMIT_Y - BALL_RADIUS - 1) {
                endX = INNER_FIELD_LIMIT_X - BALL_RADIUS;
                endY = y;
            }
        }

        // Bordo superiore (y = INNER_MARGIN)
        if (dy != 0) {
            double t = (INNER_MARGIN + BALL_RADIUS - startY) / dy; 
            double x = startX + t * dx;
            if (t > 0 && x >= INNER_MARGIN + BALL_RADIUS + 1 && x <= INNER_FIELD_LIMIT_X - BALL_RADIUS - 1) {
                endX = x;
                endY = INNER_MARGIN + BALL_RADIUS;
            }
        }

        // Bordo inferiore (y = INNER_FIELD_LIMIT_Y)
        if (dy != 0) {
            double t = (INNER_FIELD_LIMIT_Y - BALL_RADIUS - startY) / dy; 
            double x = startX + t * dx;
            if (t > 0 && x >= INNER_MARGIN + BALL_RADIUS + 1 && x <= INNER_FIELD_LIMIT_X - BALL_RADIUS - 1) {
                endX = x;
                endY = INNER_FIELD_LIMIT_Y - BALL_RADIUS;
            }
        }



        // Crea un rettangolo con la lunghezza della traiettoria e larghezza pari al diametro della pallina
        double length = Math.sqrt(Math.pow(startY - endY, 2) + Math.pow(startX - endX, 2));
        Rectangle rectangle = new Rectangle(0, 0, BALL_RADIUS * 2, (int) length);
        AffineTransform t = new AffineTransform();

        t.translate(startX - BALL_RADIUS * Math.sin(angle), startY + BALL_RADIUS * Math.cos(angle));
        t.rotate(angle - Math.toRadians(90));

        // crea un oggetto GameFieldObject per la traiettoria, serve per il controllo delle collisioni
        GameFieldObject trajectory = new GameFieldObject(t.createTransformedShape(rectangle));

        double shortestDistance = Double.MAX_VALUE;

        TrajectoryInfo[] trajectories = new TrajectoryInfo[3];

        trajectories[1] = new TrajectoryInfo(0, 0, 0, 0);
        trajectories[2] = new TrajectoryInfo(0, 0, 0, 0);

        for (Ball ball : model.getBalls()) {
            if (ball.checkCollision(trajectory)) {
                if (ball.equals(model.getCueBall())) {
                    continue; // Salta la biglia bianca
                }

                Point2D intersection = getCircleCircleIntersection(
                        startX, startY, dx, dy, ball.getX(), ball.getY(), model.getCueBall().getRadius());

                if (intersection != null) {

                    // la prima biglia che sarà colpita è la più vicina
                    double distance = intersection.distance(startX, startY);
                    if (distance < shortestDistance) {
                        shortestDistance = distance;
                        endX = intersection.getX();
                        endY = intersection.getY();

                        TrajectoryInfo[] deflectedTrajectories = calculateCollisionTrajectories(intersection,
                                getCueBall(), ball, angle);

                        trajectories[1] = deflectedTrajectories[0];
                        trajectories[2] = deflectedTrajectories[1];

                    }
                }
            }
        }

        trajectories[0] = new TrajectoryInfo(startX, startY, endX, endY);

        return trajectories;
    }

    /**
     * Calcola il punto di intersezione tra due cerchi. Serve per calcolare il punto di impatto tra due biglie.
     * @param x1 coordinata x del punto di partenza
     * @param y1 coordinata y del punto di partenza
     * @param dx direzione x della traiettoria
     * @param dy direzione y della traiettoria
     * @param cx coordinata x del centro del cerchio
     * @param cy coordinata y del centro del cerchio
     * @param radius raggio del cerchio
     * @return il punto di intersezione tra i due cerchi
     */
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

    /**
     * Calcola le traiettoria delle biglie dopo l'impatto tra la cue ball e la target ball.
     */
    public TrajectoryInfo[] calculateCollisionTrajectories(Point2D impactPoint, Ball cueBall, Ball targetBall,
            double trajectoryAngle) {
        // Calcolo della traiettoria della target ball dopo l'impatto
        double dx = targetBall.getX() - impactPoint.getX();
        double dy = targetBall.getY() - impactPoint.getY();

        double angleToTarget = Math.atan2(dy, dx); // Calcolo dell'angolo dalla posizione di impatto

        // Lunghezza variabile della traiettoria in base all'angolo
        double targetBallDistance = Math.abs(Math.cos(trajectoryAngle - angleToTarget)) * 80;

        double targetEndX = impactPoint.getX() + targetBallDistance * Math.cos(angleToTarget);
        double targetEndY = impactPoint.getY() + targetBallDistance * Math.sin(angleToTarget);

        // Creazione della traiettoria della pallina colpita
        TrajectoryInfo targetBallTrajectory = new TrajectoryInfo(
                impactPoint.getX(),
                impactPoint.getY(),
                targetEndX,
                targetEndY);

        // Calcolo della traiettoria della cue ball dopo l'impatto (scegliendo
        // dinamicamente +90° o -90°)
        double cueAngle;
        double crossProduct = dx * Math.sin(trajectoryAngle) - dy * Math.cos(trajectoryAngle);
        if (crossProduct > 0) {
            cueAngle = angleToTarget + Math.PI / 2; // Sfasata di +90°
        } else {
            cueAngle = angleToTarget - Math.PI / 2; // Sfasata di -90°
        }

        // Lunghezza variabile della traiettoria della cue ball in base all'angolo
        double cueBallDistance = Math.abs(Math.sin(trajectoryAngle - angleToTarget)) * 80;
        double cueEndX = impactPoint.getX() + cueBallDistance * Math.cos(cueAngle);
        double cueEndY = impactPoint.getY() + cueBallDistance * Math.sin(cueAngle);

        // Creazione della traiettoria della cue ball
        TrajectoryInfo cueBallTrajectory = new TrajectoryInfo(
                impactPoint.getX(),
                impactPoint.getY(),
                cueEndX,
                cueEndY);

        return new TrajectoryInfo[] { cueBallTrajectory, targetBallTrajectory };
    }

    /**
     * Gestione del riposizionamento della biglia bianca quando viene commesso un foul.
     */
    public void resetCueBallPosition(int x, int y) {
        Ball cueBall = model.getCueBall();
        if (cueBall.needsReposition()) {
            cueBall.setPosition(x, y);
            model.setStatus(GameStatus.cueBallRepositioning);
            cueBall.setNeedsReposition(false);
            model.getBalls().addFirst(cueBall);
            model.setFoulHandled();
            model.resetRound();
        }
    }

    /**
     * Verifica se una biglia è in una certa posizione.
     */
    public boolean isAnyBallInSight(int x, int y) {
        for (Ball ball : model.getBalls()) {
            double dx = ball.getX() - x;
            double dy = ball.getY() - y;
            double distanceSquared = dx * dx + dy * dy;

            double combinedRadiusSquared = Math.pow(ball.getRadius(), 2) * 4;

            if (distanceSquared <= combinedRadiusSquared) {
                return true;
            }
        }
        return false;
    }

    public boolean cueBallNeedsReposition() {
        return model.getCueBall().needsReposition();
    }

    public int getRoundNumber() {
        return model.getRoundNumber();
    }

    public String winningPlayer() {
        return model.getWinningPlayer();
    }
}