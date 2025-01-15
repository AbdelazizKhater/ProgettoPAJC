package it.unibs.pajc.clientserver;

import it.unibs.pajc.GameField;
import it.unibs.pajc.GameView;

import javax.swing.*;

import static it.unibs.pajc.util.CostantiStatiche.TABLE_HEIGHT;
import static it.unibs.pajc.util.CostantiStatiche.TABLE_WIDTH;

import java.awt.*;
import java.io.*;
import java.net.Socket;


public class Client {
    private String serverAddress;
    private int port;
    String username;
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private JFrame frame;
    private GameField gameField;
    private GameView gameView;
    private MultiplayerController controller;

    /**
     * Costruttore del Client.
     *
     * @param serverAddress Indirizzo IP del server.
     * @param port          Porta del server.
     * @param username      Nome del giocatore.
     */
    public Client(String serverAddress, int port, String username) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.username = username;
    }

    /**
     * Avvia la connessione al server.
     */
    public boolean start() {
        try {
            socket = new Socket(serverAddress, port);
            sOutput = new ObjectOutputStream(socket.getOutputStream());
            sInput = new ObjectInputStream(socket.getInputStream());

            // Invia il messaggio di JOIN
            sendMessage("JOIN@" + username);

            // Avvia il listener per i messaggi del server
            new ServerListener().start();

            // Inizializza l'interfaccia grafica
            SwingUtilities.invokeLater(this::initGUI);

            return true;
        } catch (IOException e) {
            System.err.println("Errore nella connessione al server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Invia un messaggio al server.
     */
    public void sendMessage(String message) {
        try {
            sOutput.writeObject(message);
        } catch (IOException e) {
            System.err.println("Errore nell'invio del messaggio: " + e.getMessage());
        }
    }

    /**
     * Listener per ricevere messaggi dal server.
     */
    private class ServerListener extends Thread {
        public void run() {
            try {
                while (true) {
                    String message = (String) sInput.readObject();
                    handleServerMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Connessione al server persa: " + e.getMessage());
            }
        }
    }

    /**
     * Gestisce i messaggi ricevuti dal server.
     */
    private void handleServerMessage(String message) {
        if (message.startsWith("START@")) {
            // Avvia il gioco con i dati ricevuti dal server
            SwingUtilities.invokeLater(() -> setupGame(message.substring(6)));
        } else if (message.startsWith("STATE@")) {
            // Aggiorna lo stato del gioco
            SwingUtilities.invokeLater(() -> updateGameState(message.substring(6)));
        } else if (message.startsWith("SYN@")) {
            // Gestione del messaggio di sincronizzazione
            SwingUtilities.invokeLater(() -> controller.updateModelFromMessage(message.substring(0)));
        }
    }

    /**
     * Imposta il gioco con i dati iniziali ricevuti dal server.
     *
     * @param gameData Dati iniziali del gioco.
     */
    private void setupGame(String gameData) {

        gameField = new GameField();
        controller = new MultiplayerController(gameField, this);

        controller.updateModelFromMessage(gameData);
        controller.addPlayersFromMessage(gameData);

        startGame();
    }

    /**
     * Aggiorna lo stato del gioco con i dati ricevuti dal server.
     *
     * @param gameState Stato aggiornato del gioco.
     */
    private void updateGameState(String gameState) {
        controller.updateModelFromMessage(gameState);
        if (gameView != null) {
            gameView.repaint();
        }
    }

    /**
     * Inizializza l'interfaccia grafica.
     */
    private void initGUI() {
        frame = new JFrame("Billiard Multiplayer - " + username);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(TABLE_WIDTH + 16, TABLE_HEIGHT + 39 + 130);
        frame.setLayout(new BorderLayout());

        // Messaggio iniziale di attesa del secondo giocatore
        JLabel waitingLabel = new JLabel("In attesa del secondo giocatore...", SwingConstants.CENTER);
        waitingLabel.setFont(new Font("Arial", Font.BOLD, 18));
        frame.add(waitingLabel, BorderLayout.CENTER);

        centerFrame(frame);

        frame.setResizable(false);
        frame.setVisible(true);
    }

    /**
     * Metodo per avviare il gioco e aggiornare la GUI.
     */
    private void startGame() {
        // Configura la GameView
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            gameView = new GameView(controller, this);
            frame.add(gameView, BorderLayout.CENTER);

            frame.revalidate(); 
            frame.repaint(); 
        });
    }

    /**
     * Centra il frame nello schermo.
     */
    private void centerFrame(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) (screenSize.getWidth() / 2 - frame.getWidth() / 2);
        int y = (int) (screenSize.getHeight() / 2 - frame.getHeight() / 2);
        frame.setLocation(x, y);
    }

    /**
     * Chiude le connessioni al server.
     */
    public void disconnect() {
        try {
            if (sOutput != null)
                sOutput.close();
            if (sInput != null)
                sInput.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            System.err.println("Errore durante la disconnessione: " + e.getMessage());
        }
    }
}
