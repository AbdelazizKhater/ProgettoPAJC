package it.unibs.pajc.clientserver;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Classe che gestisce la finestra di connessione al server.
 */
public class IpMenuFrame extends JFrame {

    private JPanel contentPane;
    private JTextField txtIp;
    private JTextField txtUser;
    private JButton btnEsterno;

    /**
     * Costruzione schermata principale
     */
    public IpMenuFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 350);
        setLocationRelativeTo(null); // Centra il frame
        this.setTitle("UniPool");
        this.setResizable(false);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(45, 45, 45));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel titleLabel = new JLabel("UniPool - Connetti al Server");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(10, 10, 460, 30);
        contentPane.add(titleLabel);

        txtUser = new JTextField("Inserisci il tuo nome");
        txtUser.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtUser.setHorizontalAlignment(SwingConstants.LEFT);
        txtUser.setBounds(10, 60, 460, 40);
        txtUser.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtUser.getText().equals("Inserisci il tuo nome")) {
                    txtUser.setText("");
                    txtUser.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtUser.getText().isEmpty()) {
                    txtUser.setText("Inserisci il tuo nome");
                    txtUser.setForeground(Color.DARK_GRAY);
                }
            }
        });
        txtUser.setForeground(Color.DARK_GRAY);
        contentPane.add(txtUser);

        txtIp = new JTextField("IP del server");
        txtIp.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtIp.setBounds(10, 120, 460, 40);
        txtIp.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtIp.getText().equals("IP del server")) {
                    txtIp.setText("");
                    txtIp.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtIp.getText().isEmpty()) {
                    txtIp.setText("IP del server");
                    txtIp.setForeground(Color.DARK_GRAY);
                }
            }
        });
        txtIp.setForeground(Color.DARK_GRAY);
        contentPane.add(txtIp);

        JButton btnLocal = new JButton("Connetti a LocalHost");
        btnLocal.setFont(new Font("Arial", Font.BOLD, 14));
        btnLocal.setBackground(new Color(70, 130, 180));
        btnLocal.setForeground(Color.WHITE);
        btnLocal.setFocusPainted(false);
        btnLocal.addActionListener(e -> {
            if (isValidInput(txtUser.getText())) {
                startClient("localhost", txtUser.getText());
            } else {
                showError(txtUser, "Errore: Nome non valido");
            }
        });
        btnLocal.setBounds(10, 180, 220, 50);
        contentPane.add(btnLocal);

        btnEsterno = new JButton("Connetti a Server Esterno");
        btnEsterno.setFont(new Font("Arial", Font.BOLD, 14));
        btnEsterno.setBackground(new Color(70, 130, 180));
        btnEsterno.setForeground(Color.WHITE);
        btnEsterno.setFocusPainted(false);
        btnEsterno.addActionListener(e -> {
            if (isValidInput(txtUser.getText()) && isValidIp(txtIp.getText())) {
                startClient(txtIp.getText(), txtUser.getText());
            } else {
                if (!isValidIp(txtIp.getText())) {
                    showError(txtIp, "Errore: IP non valido");
                } else {
                    showError(txtUser, "Errore: Nome non valido");
                }
            }
        });
        btnEsterno.setBounds(250, 180, 220, 50);
        contentPane.add(btnEsterno);

        JLabel footerLabel = new JLabel("Versione 1.0");
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerLabel.setForeground(Color.LIGHT_GRAY);
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerLabel.setBounds(10, 250, 460, 20);
        contentPane.add(footerLabel);
    }

    /**
     * Avvia il client con i parametri forniti e nasconde la IpMenuFrame.
     */
    private void startClient(String serverAddress, String username) {
        int port = 1234; // Porta fissa per ora
        SwingUtilities.invokeLater(() -> {
            Client client = new Client(serverAddress, port, username);
            if (client.start()) {
                this.dispose(); // Chiude la finestra corrente
            } else {
                JOptionPane.showMessageDialog(this, "Errore nella connessione al server!", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Mostra un messaggio di errore e colora il campo di rosso.
     */
    private void showError(JTextField field, String errorMessage) {
        field.setText(errorMessage);
        field.setBackground(Color.RED);
    }

    /**
     * Controlla se il nome utente è valido.
     */
    private boolean isValidInput(String input) {
        return input != null && !input.isEmpty() && !input.equals("Inserisci il tuo nome");
    }

    /**
     * Metodo che controlla se l'IP inserito è scritto nella forma corretta.
     *
     * @return True se è valido, False altrimenti.
     */
    public static boolean isValidIp(final String ip) {
        return ip.matches("^[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}$");
    }
}
