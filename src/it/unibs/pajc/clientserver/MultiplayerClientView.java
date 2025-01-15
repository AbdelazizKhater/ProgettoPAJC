package it.unibs.pajc.clientserver;

import it.unibs.pajc.BilliardController;
import it.unibs.pajc.GameFieldView;

import java.awt.*;

/**
 * Classe che estende la View originale per la visualizzazione del gioco in
 * modalità multiplayer
 */
public class MultiplayerClientView extends GameFieldView {
    private Client client;

    public MultiplayerClientView(BilliardController cntrl, Client client) {
        super(cntrl);
        this.client = client;
    }

    public void setIsMyTurn() {
        this.isMyTurn = (client.username.equals(getCurrentPlayerName()));
    }

    private String getCurrentPlayerName() {
        return cntrl.getCurrentPlayer().getName();
    }

    @Override
    protected void paintComponent(Graphics g) {
        setIsMyTurn();
        super.paintComponent(g);
    }
}