package it.unibs.pajc.clientserver;

import it.unibs.pajc.BilliardController;
import it.unibs.pajc.GameFieldView;

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
}
