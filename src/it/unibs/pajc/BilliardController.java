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
    
}
