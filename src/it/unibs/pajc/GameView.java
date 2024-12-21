package it.unibs.pajc;

import java.awt.BorderLayout;
import java.awt.Color;

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

        Timer timer = new Timer(0, e -> {
            long startTime = System.nanoTime();

            cntrl.stepNext();
            infoPanel.update();
            repaint();

            long elapsedTime = (System.nanoTime() - startTime) / 1_000_000; // Tempo in ms

            // Calcola il tempo rimanente per il prossimo frame
            int delay = Math.max(1, 8 - (int) elapsedTime);

            ((Timer) e.getSource()).setDelay(delay);
        });

        timer.start();

    }

}
