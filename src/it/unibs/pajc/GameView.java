package it.unibs.pajc;

import it.unibs.pajc.clientserver.Client;
import it.unibs.pajc.clientserver.MultiplayerClientView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;

import javax.swing.*;

public class GameView extends JPanel {

    private final BilliardController cntrl;


    public GameView(BilliardController cntrl) {
        super(new BorderLayout(0, 0));
        this.cntrl = cntrl;
        this.setOpaque(false);
        GameFieldView gameFieldPanel = new GameFieldView(cntrl);
        InformationPanel infoPanel = new InformationPanel(cntrl);
        this.add(infoPanel, BorderLayout.NORTH);
        this.add(gameFieldPanel, BorderLayout.CENTER);
        startTimer(cntrl, infoPanel);
    }

    public GameView(BilliardController cntrl, Client client) {
        super(new BorderLayout(0, 0));
        this.cntrl = cntrl;
        this.setOpaque(false);
        MultiplayerClientView gameFieldPanel = new MultiplayerClientView(cntrl, client);
        InformationPanel infoPanel = new InformationPanel(cntrl);
        this.add(infoPanel, BorderLayout.NORTH);
        this.add(gameFieldPanel, BorderLayout.CENTER);
        startTimer(cntrl, infoPanel);
    }


    private long previousTime;
    private long lag;

    private void startTimer(BilliardController cntrl, InformationPanel infoPanel) {
        final long timeStep = 16_666_667; // Fixed timestep: ~16.67ms (60 updates per second)
        final long maxLag = timeStep * 60; // Avoid excessive lag buildup

        Timer timer = new Timer(0, e -> {
            long currentTime = System.nanoTime();
            long elapsedTime = currentTime - previousTime;
            previousTime = currentTime;

            lag += elapsedTime;
            lag = Math.min(lag, maxLag); // Cap lag to avoid spiral of death

            while (lag >= timeStep) {
                cntrl.stepNext(); // Fixed physics update
                lag -= timeStep;
            }

            infoPanel.update(); // Update UI (can be decoupled if necessary)
            repaint(); // Render the frame
        });

        previousTime = System.nanoTime();
        lag = 0;
        timer.start();
    }

}
