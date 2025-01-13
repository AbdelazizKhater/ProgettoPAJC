package it.unibs.pajc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;

import javax.swing.*;

public class GameView extends JPanel {

    private final BilliardController cntrl;

    public GameView(BilliardController cntrl) {

        super(new BorderLayout(0, 10));
        this.setBackground(Color.gray);

        this.cntrl = cntrl;

        GameFieldView gameFieldPanel = new GameFieldView(cntrl);
        InformationPanel infoPanel = new InformationPanel(cntrl);

        this.add(infoPanel, BorderLayout.NORTH);
        this.add(gameFieldPanel, BorderLayout.CENTER);

        Timer timer = new Timer(10, e -> {
            cntrl.stepNext();
            infoPanel.update();
            repaint();
        });

        timer.start();

    }

}
