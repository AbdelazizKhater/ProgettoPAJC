package it.unibs.pajc;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class BilliardController {

    private GameField model;

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

    // Metodo per simulare il colpo
    public void hitBall() {
        Stick stick = model.getStick();
        Ball whiteBall = model.getBalls().get(0);

        System.out.println(stick.getPower());
        double[] velocity = stick.calculateBallVelocity();
        whiteBall.applyVelocity(velocity);
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
        return model.reduceStickVisualPower(15); // Velocità dell'animazione
    }

    public void onStickAnimationComplete() {
        model.hitBall(); // Esegui il colpo
    }

}
