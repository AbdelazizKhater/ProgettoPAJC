package it.unibs.pajc;

import javax.swing.*;
import java.awt.*;

import static it.unibs.pajc.CostantiStatiche.*;

public class BilliardGameApp {

    private final BilliardController cntrl;
    private final GameField model;

    private JFrame frame;

    public static void main(String[] args) {
        EventQueue.invokeLater(()-> {
            BilliardGameApp window = new BilliardGameApp();
            window.frame.setVisible(true);
        });
    }

    public BilliardGameApp() {
        model = new GameField();
        cntrl = new BilliardController(model);

        initialize();
    }

    public void initialize() {
        frame = new JFrame("Billiard Game");
        GameView gameView = new GameView(cntrl);
        frame.add(gameView);
        frame.setSize(TABLE_WIDTH + 16, TABLE_HEIGHT + 39);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }


}
