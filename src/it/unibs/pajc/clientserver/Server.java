package it.unibs.pajc.clientserver;

import it.unibs.pajc.GameField;
import it.unibs.pajc.GameStatus;
import it.unibs.pajc.Player;
import it.unibs.pajc.fieldcomponents.Ball;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket; 
import java.util.ArrayList;
import java.util.Locale;

/**
 * Classe Server aggiornata per gestire un GameField condiviso con gestione corretta dei giocatori e ViewServer.
 */
public class Server {
    private static int uniqueId;
    private final ArrayList<ClientThread> clientThreads;
    private final int port;
    private GameField gameField; // GameField condiviso per la partita corrente
    private ViewServer viewServer; // Interfaccia grafica per il server

    public Server(int port) {
        this.port = port;
        this.clientThreads = new ArrayList<>();
    }

    public static void main(String[] args) {
        int portNumber = 1234;
        Server server = new Server(portNumber);
        server.start();
    }

    /**
     * Avvia il server e gestisce le connessioni.
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Inizializza la ViewServer
            viewServer = new ViewServer(Inet4Address.getLocalHost().getHostAddress(), port);
            viewServer.setVisible(true);
            appendLog("Server avviato sulla porta " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                if (clientThreads.size() < 2) {
                    ClientThread clientThread = new ClientThread(socket);
                    clientThreads.add(clientThread);
                    clientThread.start();

                    updateViewServer(); // Aggiorna la lista dei client nella GUI

                    if (clientThreads.size() == 2) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        startGame();
                    }
                } else {
                    socket.close();
                    appendLog("Connessione rifiutata: troppi client connessi.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            appendLog("Errore nel server: " + e.getMessage());
        }
    }

    /**
     * Avvia una nuova partita creando un GameField condiviso e configurando i giocatori.
     */
    private void startGame() {
        gameField = new GameField(); // Crea un nuovo modello di gioco

        // Aggiungi i giocatori al modello
        for (int i = 0; i < clientThreads.size(); i++) {
            Player player = new Player(clientThreads.get(i).getPlayerName());
            gameField.addPlayer(player);
        }

        broadcastMessage("START@" + formatGameState(""));
        appendLog("Partita avviata.");
    }

    /**
     * Invia un messaggio a tutti i client connessi.
     */
    private void broadcastMessage(String message) {
        appendLog("Inviando messaggio a tutti i client: " + message);
        for (ClientThread client : clientThreads) {
            client.writeMsg(message);
        }
    }

    /**
     * Formatta lo stato del gioco per essere inviato ai client.
     */
    private String formatGameState(String shotMessage) {
        StringBuilder formattedState = new StringBuilder();
        Locale.setDefault(Locale.US); // Assicura il formato numerico con '.'

        // Aggiungi i giocatori
        for (var player : gameField.getPlayers()) {
            formattedState.append(String.format("PLAYER@%s\n", player.getName()));
        }

        // Aggiungi il turno iniziale
        Player currentPlayer = gameField.getCurrentPlayer();
        formattedState.append(String.format("TURN@%s\n", currentPlayer.getName()));

        // Manda informazioni sul colpo tirato
        formattedState.append(String.format("%s\n", shotMessage));
        return formattedState.toString();
    }

    /**
     * Aggiunge un messaggio di log alla ViewServer.
     */
    private void appendLog(String message) {
        viewServer.appendLog(message);
    }

    /**
     * Aggiorna la lista dei partecipanti nella ViewServer.
     */
    private void updateViewServer() {
        viewServer.updateParticipants(clientThreads);
    }

    /**
     * Classe per gestire i thread dei client.
     */
    protected class ClientThread extends Thread {
        private final Socket socket;
        private ObjectInputStream sInput;
        private ObjectOutputStream sOutput;
        private int id;
        private String playerName;

        public ClientThread(Socket socket) {
            this.id = ++uniqueId;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int getClientId() {
            return this.id;
        }

        public String getPlayerName() {
            return this.playerName;
        }

        public void run() {
            boolean keepGoing = true;
            while (keepGoing) {
                try {
                    String message = (String) sInput.readObject();
                    appendLog("Messaggio ricevuto dal client " + id + ": " + message);
                    handleClientMessage(message);
                } catch (IOException | ClassNotFoundException e) {
                    appendLog("Client " + id + " disconnesso.");
                    clientThreads.remove(this);
                    keepGoing = false;
                }
            }
            close();
            updateViewServer();
        }

        /**
         * Gestisce i messaggi ricevuti dai client.
         */
        private void handleClientMessage(String message) {
            if (message.startsWith("JOIN@")) {
                // Estrai il nome del giocatore
                this.playerName = message.split("@")[1];
                appendLog("Giocatore " + playerName + " connesso.");
            } else if (message.startsWith("SHOT@")) {
                String[] parts = message.split("@");
                double angle = Double.parseDouble(parts[1]);
                double power = Double.parseDouble(parts[2]);

                // Configura il colpo sul modello condiviso
                gameField.getStick().setAngleDegrees(angle);
                gameField.getStick().setPower(power);
                gameField.hitBall();

                // Invia lo stato aggiornato a tutti i client
                broadcastMessage("STATE@" + formatGameState(message));
            } else if (message.startsWith("POSITION@")) {
                String[] parts = message.split("@");
                double xCueBall = Double.parseDouble(parts[1]);
                double yCueBall = Double.parseDouble(parts[2]);

                Ball cueBall = gameField.getCueBall();

                cueBall.setPosition(xCueBall, yCueBall);
                gameField.setStatus(GameStatus.cueBallRepositioning);
                cueBall.setNeedsReposition(false);
                gameField.getBalls().addFirst(cueBall);
                gameField.resetRound();
                broadcastMessage("STATE@" + formatGameState(message));

            }
        }

        /**
         * Scrive un messaggio al client.
         */
        private boolean writeMsg(String msg) {
            try {
                sOutput.writeObject(msg);
                return true;
            } catch (IOException e) {
                appendLog("Errore invio messaggio al client " + id);
                return false;
            }
        }

        /**
         * Chiude le connessioni.
         */
        private void close() {
            try {
                if (sOutput != null) sOutput.close();
                if (sInput != null) sInput.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
