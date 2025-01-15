package it.unibs.pajc;

import it.unibs.pajc.clientserver.Client;
import it.unibs.pajc.clientserver.MultiplayerClientView;

import java.awt.BorderLayout;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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


    private void startTimer(BilliardController cntrl, InformationPanel infoPanel) {
        final long timeStep = 16_666_667; // ~16.67ms per 60 FPS
        final long[] nextUpdateTime = { System.nanoTime() }; // Variabile mutabile per il tempo del prossimo aggiornamento
    
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.execute(() -> {
            while (true) {
                long currentTime = System.nanoTime();
    
                if (currentTime >= nextUpdateTime[0]) {
                    long drift = currentTime - nextUpdateTime[0];
    
                    // Esegui stepNext e repaint
                    cntrl.isCalculationDone().set(false);

                    cntrl.stepNext();

                    // wait
                    while(!cntrl.isCalculationDone().get()) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    repaint();

                    if(cntrl.getGameStatus() == GameStatus.completed ) {
                        JOptionPane.showMessageDialog(this, "Il giocatore " + cntrl.winningPlayer() + " vince!", "Vincitore", JOptionPane.INFORMATION_MESSAGE);
                        //gameFinished = true;
                        scheduler.close();
                    }

                    // Aggiorna la UI
                    infoPanel.update();
    
                    // Calcola il prossimo aggiornamento
                    nextUpdateTime[0] += timeStep;
    
                    // Correggi la sincronizzazione se il drift è troppo elevato
                    if (Math.abs(drift) > timeStep * 2) {
                        nextUpdateTime[0] = System.nanoTime() + timeStep;
                    }
                }
    
                // Evita loop inutili (riduce il carico della CPU)
                try {
                    Thread.sleep(1); // Riposa per 1 millisecondo
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    

}
