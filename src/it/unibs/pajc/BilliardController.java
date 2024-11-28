package it.unibs.pajc;

import java.util.ArrayList;

public class BilliardController {

    private GameField model;

    public BilliardController(GameField model) {
        this.model = model;
    }

    public void stepNext() {
        model.stepNext();
    }

    public ArrayList<Ball> listBalls() {
        return model.getBalls();
    }
    
    // Metodo per simulare il colpo
    public void hitBall() {
        Stick stick = model.getStick();
        Ball whiteBall = model.getBalls().get(0);

        System.out.println(stick.getPower());
        double[] velocity = stick.calculateBallVelocity();
        whiteBall.applyVelocity(velocity);
    }

    public Stick geStick()
    {
        return model.getStick();
    }

    public Ball getWhiteBall()
    {
        return model.getBalls().get(0);
    }

    public void hitWhiteBall()
    {
        model.hitBall();
    }
}
