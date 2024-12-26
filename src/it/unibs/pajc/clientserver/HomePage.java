package it.unibs.pajc.clientserver;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HomePage extends JFrame {

    private JPanel contentPane;
    private JTextField txtIp;
    private JTextField txtUser;
    private JButton btnEsterno;

    /**
     * Costruzione schermata principale
     */
    public HomePage() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 287);
        ImageIcon img = new ImageIcon("Ball.png");
        this.setIconImage(img.getImage());
        this.setTitle("TikiTaka");

        contentPane = new JPanel();
        contentPane.setBackground(Color.GREEN);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        txtIp = new JTextField();
        txtIp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                txtIp.setBackground(Color.WHITE);
                txtIp.setText("");
            }
        });
        txtIp.setFont(new Font("Tahoma", Font.PLAIN, 17));
        txtIp.setText(" ip server (no localhost)");
        txtIp.setBounds(10, 76, 386, 36);
        contentPane.add(txtIp);
        txtIp.setColumns(10);

        JButton btnLocal = new JButton("LocalHost");
        btnLocal.setFont(new Font("Arial", Font.BOLD, 14));
        btnLocal.addActionListener(e -> {
            if (isValidInput(txtUser.getText())) {
                startClient("localhost", txtUser.getText());
            } else {
                showError(txtUser, "Errore: Nome non valido");
            }
        });
        btnLocal.setBounds(10, 142, 174, 48);
        contentPane.add(btnLocal);

        txtUser = new JTextField();
        txtUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                txtUser.setText("");
                txtUser.setBackground(Color.WHITE);
            }
        });
        txtUser.setFont(new Font("Tahoma", Font.PLAIN, 15));
        txtUser.setText("Inserisci il tuo nome");
        txtUser.setHorizontalAlignment(SwingConstants.LEFT);
        txtUser.setBounds(10, 30, 386, 36);
        contentPane.add(txtUser);
        txtUser.setColumns(10);

        btnEsterno = new JButton("Server Esterno");
        btnEsterno.setFont(new Font("Arial", Font.BOLD, 14));

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
        btnEsterno.setBounds(235, 143, 180, 47);
        contentPane.add(btnEsterno);
    }

    /**
     * Avvia il client con i parametri forniti e nasconde la HomePage.
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
