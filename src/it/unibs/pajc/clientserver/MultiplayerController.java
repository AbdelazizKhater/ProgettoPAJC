package it.unibs.pajc.clientserver;

import it.unibs.pajc.BilliardController;
import it.unibs.pajc.GameField;
import it.unibs.pajc.GameStatus;
import it.unibs.pajc.Player;
import it.unibs.pajc.fieldcomponents.Ball;

import java.util.Locale;

/**
 * MultiplayerController estende BilliardController per supportare il gioco
 * multiplayer.
 */
public class MultiplayerController extends BilliardController {

    private final Client client;

    /**
     * Costruttore del MultiplayerController.
     *
     * @param gameField Il modello GameField da controllare.
     * @param client    Il client per comunicare con il server.
     */
    public MultiplayerController(GameField gameField, Client client) {
        super(gameField);
        this.client = client;
    }

    /**
     * Override del metodo onStickAnimationComplete per inviare il colpo al server.
     */
    @Override
    public void onStickAnimationComplete() {

        // Verifica se è il turno del giocatore locale
        Player currentPlayer = getCurrentPlayer();
        if (!isMyTurn(currentPlayer)) {
            System.out.println("Non è il tuo turno. Nessun messaggio inviato.");
            return;
        }

        double angle = model.getStick().getAngleDegrees();
        double power = model.getStick().getLastPower();

        System.out.printf("Inviando SHOT: Angolo=%.2f, Potenza=%.2f%n", angle, power);

        // Invia il comando al server
        client.sendMessage(String.format(Locale.US, "SHOT@%.2f@%.2f", angle, power));

    }

    @Override
    public void resetCueBallPosition(int x, int y) {
        Ball cueBall = model.getCueBall();
        if (cueBall.needsReposition()) {
            cueBall.setPosition(x, y);
            model.setStatus(GameStatus.cueBallRepositioning);
            cueBall.setNeedsReposition(false);
            model.getBalls().addFirst(cueBall);
            model.setFoulHandled();
            model.resetRound();

            System.out.printf("Inviando POSITION: X=%d, Y=%d%n", x, y);

            client.sendMessage(String.format("POSITION@%d@%d", x, y));
        }
    }

    /**
     * Aggiorna lo stato del gioco con i dati ricevuti dal server.
     *
     * @param gameState Stato del gioco in formato stringa.
     */
    public void updateModelFromMessage(String gameState) {
        String[] lines = gameState.split("\n");

        for (String line : lines) {
            if (line.startsWith("PLAYER@")) {
                String playerName = line.substring(7);

                if (model.getPlayers().length < 2) {
                    model.addPlayer(new Player(playerName));
                }
            } else if (line.startsWith("TURN@")) {
                String currentPlayerName = model.getCurrentPlayer() == null ? "" : model.getCurrentPlayer().getName();
                System.out.println("Turno del giocatore: " + currentPlayerName);
            } else if (line.startsWith("SHOT@")) {
                String[] parts = line.split("@");

                double angle = Double.parseDouble(parts[1]);
                double power = Double.parseDouble(parts[2]);

                model.getStick().setAngleDegrees(angle);
                model.getStick().setPower(power);
                model.hitBall();

            } else if (line.startsWith("POSITION@")) {
                String[] parts = line.split("@");
                double xCueBall = Double.parseDouble(parts[1]);
                double yCueBall = Double.parseDouble(parts[2]);

                Ball cueBall = model.getCueBall();
                if (cueBall.needsReposition()) {
                    cueBall.setPosition(xCueBall, yCueBall);
                    model.setStatus(GameStatus.cueBallRepositioning);
                    cueBall.setNeedsReposition(false);
                    model.getBalls().addFirst(cueBall);
                    model.setFoulHandled();
                    model.resetRound();
                }
            }

        }
    }

    /**
     * Aggiunge i giocatori al modello dal messaggio iniziale.
     *
     * @param gameData Messaggio contenente i dati iniziali del gioco.
     */
    public void addPlayersFromMessage(String gameData) {
        String[] lines = gameData.split("\n");
        if (lines.length < 3) {
            System.err.println("Dati iniziali del gioco non validi: " + gameData);
            return;
        }

        String player1Name = lines[0].split("@")[1];
        String player2Name = lines[1].split("@")[1];

        Player player1 = new Player(player1Name);
        Player player2 = new Player(player2Name);

        model.addPlayer(player1);
        model.addPlayer(player2);
    }
}
