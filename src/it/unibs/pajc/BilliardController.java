package it.unibs.pajc;

import java.awt.Rectangle;

import java.awt.geom.AffineTransform;

import java.util.List;

import java.awt.geom.Point2D;

import static it.unibs.pajc.CostantiStatiche.*;

public class BilliardController {

    private final GameField model;

    public BilliardController(GameField model) {
        this.model = model;
    }

    public void stepNext() {
        model.stepNext();
    }

    public List<BallInfo> getBallInfos() {
        return model.getBalls().stream()
                .map(ball -> new BallInfo(ball.getX(), ball.getY(), ball.getRadius(), ball.getNumber()))
                .toList();
    }

    public Stick getStick() {
        return model.getStick();
    }

    public Ball getWhiteBall() {
        return model.getBalls().getFirst();
    }

    public void hitWhiteBall() {
        model.hitBall();
    }

    public Boolean checkAllStationary() {
        return model.allBallsAreStationary();
    }

    public void placeBall(int x, int y) {
        // TODO: turni giocatori
        // model.placeBall(x, y);
    }

    public boolean isMyTurn(Player p) {
        return p != null && model.getCurrentPlayer().id == p.getId();
    }

    public GameField.GameStatus getGameStatus() {
        return model.getStatus();
    }

    public boolean foulDetected() {
        return model.isFoulDetected();
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
        model.hitBall(); // Esegui il colpo
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
                endX = INNER_MARGIN;
                endY = y;
            }
        }

        // Bordo destro (x = INNER_FIELD_LIMIT_X)
        if (dx != 0) {
            double t = (INNER_FIELD_LIMIT_X - startX) / dx;
            double y = startY + t * dy;
            if (t > 0 && y >= INNER_MARGIN && y <= INNER_FIELD_LIMIT_Y) {
                endX = INNER_FIELD_LIMIT_X;
                endY = y;
            }
        }

        // Bordo superiore (y = INNER_MARGIN)
        if (dy != 0) {
            double t = (INNER_MARGIN - startY) / dy;
            double x = startX + t * dx;
            if (t > 0 && x >= INNER_MARGIN && x <= INNER_FIELD_LIMIT_X) {
                endX = x;
                endY = INNER_MARGIN ;
            }
        }

        // Bordo inferiore (y = INNER_FIELD_LIMIT_Y)
        if (dy != 0) {
            double t = (INNER_FIELD_LIMIT_Y - startY) / dy;
            double x = startX + t * dx;
            if (t > 0 && x >= INNER_MARGIN && x <= INNER_FIELD_LIMIT_X) {
                endX = x;
                endY = INNER_FIELD_LIMIT_Y;
            }
        }

        // Crea l'oggetto GameFieldObject rappresentante la traiettoria
        double length = Math.sqrt(Math.pow(startY - endY, 2) + Math.pow(startX - endX, 2));
        Rectangle rectangle = new Rectangle(0, 0, 2, (int) length);
        AffineTransform t = new AffineTransform();
        t.translate(startX, startY);
        t.rotate(angle - Math.toRadians(90));

        GameFieldObject trajectory = new GameFieldObject(t.createTransformedShape(rectangle));

        trajectory.setX(0);
        trajectory.setY(0);

        double shortestDistance = Double.MAX_VALUE;

        for (Ball ball : model.getBalls()) {
            if (ball.equals(model.getCueBall())) {
                continue; // Salta la pallina bianca
            }

            Point2D intersection = getLineCircleIntersection(
                    startX, startY, dx, dy, ball.getX(), ball.getY(), ball.getRadius());

            if (intersection != null) {
                double distance = intersection.distance(startX, startY);
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    endX = intersection.getX();
                    endY = intersection.getY();
                }
            }
        }

        return new TrajectoryInfo(startX, startY, endX, endY);
    }

    private Point2D getLineCircleIntersection(double x1, double y1, double dx, double dy, double cx, double cy,
            double r) {
        // Calcola i coefficienti dell'equazione quadratica
        double a = dx * dx + dy * dy;
        double b = 2 * (dx * (x1 - cx) + dy * (y1 - cy));
        double c = (x1 - cx) * (x1 - cx) + (y1 - cy) * (y1 - cy) - r * r;

        // Calcola il discriminante
        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            return null; // Nessuna intersezione
        }

        // Calcola i valori di t per le due intersezioni
        double t1 = (-b - Math.sqrt(discriminant)) / (2 * a);
        double t2 = (-b + Math.sqrt(discriminant)) / (2 * a);

        // Scegli il t più piccolo positivo
        double t = Math.min(t1, t2);
        if (t < 0) {
            t = Math.max(t1, t2); // Prova l'altro
        }
        if (t < 0) {
            return null; // Entrambi negativi, nessuna intersezione valida
        }

        // Calcola il punto di intersezione
        double px = x1 + t * dx;
        double py = y1 + t * dy;
        return new Point2D.Double(px, py);
    }

}
