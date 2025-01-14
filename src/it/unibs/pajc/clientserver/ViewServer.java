package it.unibs.pajc.clientserver;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ViewServer extends JFrame {

    private JPanel contentPane;
    private JTextArea logTextArea;
    private JTextArea participantsTextArea;
    private JTextArea sessionsTextArea;
    private JScrollPane logScrollPane;

    /**
     * Costruttore per creare la finestra del server.
     *
     * @param ip      Indirizzo IP del server.
     * @param pNumber Porta del server.
     */
    public ViewServer(String ip, int pNumber) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Server - Billiard Multiplayer");
        setBounds(100, 100, 1200, 600);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(240, 240, 240));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(new BorderLayout(10, 10));
        setContentPane(contentPane);

        // Titolo del server
        JLabel titleLabel = new JLabel("Console Server", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.DARK_GRAY);
        contentPane.add(titleLabel, BorderLayout.NORTH);

        // Pannello centrale per log, partecipanti e sessioni
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(1, 3, 10, 10));

        // Log del server
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setBackground(Color.WHITE);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        logScrollPane = new JScrollPane(logTextArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log del Server"));
        centerPanel.add(logScrollPane);

        // Partecipanti
        participantsTextArea = new JTextArea();
        participantsTextArea.setEditable(false);
        participantsTextArea.setBackground(Color.WHITE);
        participantsTextArea.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane participantsScrollPane = new JScrollPane(participantsTextArea);
        participantsScrollPane.setBorder(BorderFactory.createTitledBorder("Partecipanti Connessi"));
        centerPanel.add(participantsScrollPane);

        // Sessioni di gioco
        sessionsTextArea = new JTextArea();
        sessionsTextArea.setEditable(false);
        sessionsTextArea.setBackground(Color.WHITE);
        sessionsTextArea.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane sessionsScrollPane = new JScrollPane(sessionsTextArea);
        sessionsScrollPane.setBorder(BorderFactory.createTitledBorder("Sessioni di Gioco"));
        centerPanel.add(sessionsScrollPane);

        contentPane.add(centerPanel, BorderLayout.CENTER);

        // Informazioni sul server
        JLabel serverInfoLabel = new JLabel("IP: " + ip + " | Porta: " + pNumber, SwingConstants.CENTER);
        serverInfoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        serverInfoLabel.setForeground(Color.GRAY);
        contentPane.add(serverInfoLabel, BorderLayout.SOUTH);
    }

    /**
     * Aggiorna la lista dei partecipanti con i dati forniti.
     *
     * @param clientThreadsList Lista dei client connessi.
     */
    public void updateParticipants(List<Server.ClientThread> clientThreadsList) {
        StringBuilder participants = new StringBuilder("Partecipanti Connessi:\n");
        for (Server.ClientThread client : clientThreadsList) {
            participants.append("ID: ").append(client.getClientId())
                        .append(" - Nome: ").append(client.getPlayerName() != null ? client.getPlayerName() : "Sconosciuto")
                        .append("\n");
        }
        participantsTextArea.setText(participants.toString());
    }

    /**
     * Aggiorna la lista delle sessioni di gioco con i dati forniti.
     *
     * @param gameSessions Mappa delle sessioni di gioco, con ID e lista di giocatori.
     */
    public void updateGameSessions(Map<Integer, List<Server.ClientThread>> gameSessions) {
        StringBuilder sessions = new StringBuilder("Sessioni di Gioco Attive:\n");
        for (Map.Entry<Integer, List<Server.ClientThread>> entry : gameSessions.entrySet()) {
            int sessionId = entry.getKey();
            List<Server.ClientThread> clients = entry.getValue();
            String players = clients.stream()
                    .map(client -> client.getPlayerName() != null ? client.getPlayerName() : "Sconosciuto")
                    .collect(Collectors.joining(", "));
            sessions.append("Sessione ID: ").append(sessionId).append(" - Giocatori: ").append(players).append("\n");
        }
        sessionsTextArea.setText(sessions.toString());
    }

    /**
     * Aggiunge una riga di log al pannello del server.
     *
     * @param message Messaggio da aggiungere al log.
     */
    public void appendLog(String message) {
        logTextArea.append(message + "\n");
    }
}
