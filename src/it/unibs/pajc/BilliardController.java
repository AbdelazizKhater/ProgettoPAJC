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

    public List<BallInfo> listBallInfos() {
        return model.getBalls().stream()
                .map(Ball::getBallInfo)
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
}
