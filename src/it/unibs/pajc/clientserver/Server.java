package it.unibs.pajc.clientserver;

import it.unibs.pajc.GameField;
import it.unibs.pajc.GameStatus;
import it.unibs.pajc.Player;
import it.unibs.pajc.fieldcomponents.Ball;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Classe Server aggiornata per gestire sessioni multiple di gioco con gestione
 * separata dei GameField.
 */
public class Server {
    private static int uniqueId;
    private final List<ClientThread> clientThreads; // Tutti i client connessi
    private final List<GameSession> gameSessions; // Tutte le sessioni di gioco
    private final List<ClientThread> waitingClients; // Client che hanno completato il processo di JOIN@
    private final int port;
    private ViewServer viewServer;

    public Server(int port) {
        this.port = port;
        this.clientThreads = new CopyOnWriteArrayList<>();
        this.gameSessions = new CopyOnWriteArrayList<>();
        this.waitingClients = new CopyOnWriteArrayList<>();
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
            viewServer = new ViewServer(Inet4Address.getLocalHost().getHostAddress(), port);
            viewServer.setVisible(true);
            appendLog("Server avviato sulla porta " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(socket);
                clientThreads.add(clientThread);
                clientThread.start();
                updateViewServer();
            }
        } catch (IOException e) {
            e.printStackTrace();
            appendLog("Errore nel server: " + e.getMessage());
        }
    }

    /**
     * Assegna i client in attesa a una nuova sessione di gioco.
     */
    private synchronized void assignToGameSession() {
        if (waitingClients.size() >= 2) {
            ClientThread player1 = waitingClients.remove(0);
            ClientThread player2 = waitingClients.remove(0);

            GameSession gameSession = new GameSession(player1, player2);
            gameSessions.add(gameSession);

            player1.setGameSession(gameSession);
            player2.setGameSession(gameSession);

            appendLog("Nuova partita creata tra: " + player1.getPlayerName() + " e " + player2.getPlayerName());

            gameSession.startGame();
            updateViewServer();
        }
    }

    /**
     * Aggiorna la lista dei client e delle sessioni nella ViewServer.
     */
    private void updateViewServer() {
        // Rimuovi sessioni vuote
        gameSessions.removeIf(session -> {
            if (session.isEmpty()) {
                appendLog("Sessione rimossa: ID " + session.getSessionId());
                return true;
            }
            return false;
        });

        // Aggiorna i partecipanti
        viewServer.updateParticipants(clientThreads);

        // Prepara una mappa per le sessioni di gioco da passare alla ViewServer
        Map<Integer, List<ClientThread>> gameSessionData = new HashMap<>();
        for (GameSession session : gameSessions) {
            gameSessionData.put(session.getSessionId(), session.getPlayers());
        }

        // Aggiorna le sessioni di gioco nella ViewServer
        viewServer.updateGameSessions(gameSessionData);
    }

    /**
     * Aggiunge un messaggio di log alla ViewServer.
     */
    private void appendLog(String message) {
        viewServer.appendLog(message);
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
        private GameSession gameSession;

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

        public void setGameSession(GameSession session) {
            this.gameSession = session;
        }

        public GameSession getGameSession() {
            return this.gameSession;
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
                    waitingClients.remove(this); // Rimuovi anche dai client in attesa
                    if (gameSession != null) {
                        gameSession.removeClient(this);
                        if (gameSession.isEmpty()) {
                            gameSessions.remove(gameSession);
                            appendLog("Sessione di gioco rimossa: " + gameSession.getSessionId());
                        }
                    }
                    keepGoing = false;
                }
            }
            close();
            updateViewServer();
        }

        private void handleClientMessage(String message) {
            if (message.startsWith("JOIN@")) {
                this.playerName = message.split("@")[1];
                appendLog("Giocatore " + playerName + " connesso.");
                waitingClients.add(this); // Aggiungi il client ai client in attesa
                assignToGameSession(); // Prova a creare una nuova sessione
            } else if (gameSession != null) {
                gameSession.handleMessage(this, message);
            }
        }

        public boolean writeMsg(String msg) {
            try {
                sOutput.writeObject(msg);
                return true;
            } catch (IOException e) {
                appendLog("Errore invio messaggio al client " + id);
                return false;
            }
        }

        private void close() {
            try {
                if (sOutput != null)
                    sOutput.close();
                if (sInput != null)
                    sInput.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Classe per gestire una sessione di gioco tra due client.
     */
    private static class GameSession {
        private static int sessionCounter;
        private final int sessionId;
        private final ClientThread player1;
        private final ClientThread player2;
        private final GameField gameField;

        public GameSession(ClientThread player1, ClientThread player2) {
            this.sessionId = ++sessionCounter;
            this.player1 = player1;
            this.player2 = player2;
            this.gameField = new GameField();
        }

        public int getSessionId() {
            return sessionId;
        }

        public List<ClientThread> getPlayers() {
            return List.of(player1, player2);
        }

        public void startGame() {
            Player p1 = new Player(player1.getPlayerName());
            Player p2 = new Player(player2.getPlayerName());
            gameField.addPlayer(p1);
            gameField.addPlayer(p2);

            broadcastMessage("START@" + formatGameState(""));
        }

        public void handleMessage(ClientThread sender, String message) {
            if (message.startsWith("SHOT@")) {
                String[] parts = message.split("@");
                double angle = Double.parseDouble(parts[1]);
                double power = Double.parseDouble(parts[2]);

                gameField.getStick().setAngleDegrees(angle);
                gameField.getStick().setPower(power);
                gameField.hitBall();

                broadcastMessage("STATE@" + formatGameState(message));
            } else if (message.startsWith("POSITION@")) {
                String[] parts = message.split("@");
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);

                Ball cueBall = gameField.getCueBall();
                cueBall.setPosition(x, y);
                gameField.setStatus(GameStatus.cueBallRepositioning);
                cueBall.setNeedsReposition(false);
                gameField.resetRound();

                broadcastMessage("STATE@" + formatGameState(message));
            } else if (message.startsWith("SYN@")) {
                broadcastMessage(message);
            }
        }

        private String formatGameState(String shotMessage) {
            StringBuilder formattedState = new StringBuilder();
            Locale.setDefault(Locale.US);

            for (var player : gameField.getPlayers()) {
                formattedState.append(String.format("PLAYER@%s\n", player.getName()));
            }

            Player currentPlayer = gameField.getCurrentPlayer();
            formattedState.append(String.format("TURN@%s\n", currentPlayer.getName()));
            formattedState.append(String.format("%s\n", shotMessage));

            return formattedState.toString();
        }

        private void broadcastMessage(String message) {
            player1.writeMsg(message);
            player2.writeMsg(message);
        }

        public void removeClient(ClientThread client) {
            if (client == player1 || client == player2) {
                broadcastMessage("DISCONNECT@" + client.getPlayerName());
            }
        }

        public boolean isEmpty() {
            return !player1.isAlive() && !player2.isAlive();
        }
    }
}
