package it.unibs.pajc;

//import it.unibs.pajc.clientserver.Client;

import javax.swing.*;

import it.unibs.pajc.clientserver.HomePage;

import java.awt.*;
import java.awt.event.ActionEvent;

import static it.unibs.pajc.util.CostantiStatiche.*;

public class BilliardGameApp {

    private BilliardController cntrl;
    private GameField model;
    Player localPlayer;

    private JFrame frame;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            BilliardGameApp window = new BilliardGameApp();
            window.frame.setVisible(true);
        });
    }

    public BilliardGameApp() {
        startGameMenu();
    }

    private void startGameMenu() {
        frame = new JFrame();
        frame.setSize(TABLE_WIDTH + 16, TABLE_HEIGHT + 39 + 70);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.requestFocus();
        frame.setResizable(false);
        centerFrame(frame);

        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(Color.GRAY);
        frame.getContentPane().add(menuPanel, BorderLayout.CENTER);
        menuPanel.setLayout(null);

        JLabel lblTitle = new JLabel("Uni Ball Pool");
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial Black", Font.PLAIN, 70));
        lblTitle.setBounds(291, 47, 673, 153);
        menuPanel.add(lblTitle);

        JButton btnSinglePlayer = new JButton("SINGLE PLAYER");
        btnSinglePlayer.setFont(new Font("Arial Black", Font.PLAIN, 30));
        btnSinglePlayer.setBounds(236, 244, 728, 96);
        menuPanel.add(btnSinglePlayer);
        btnSinglePlayer.addActionListener(this::startLocalGame);

        JButton btnJoinGame = new JButton("JOIN GAME");
        btnJoinGame.addActionListener(this::joinGame);
        btnJoinGame.setFont(new Font("Arial Black", Font.PLAIN, 30));
        btnJoinGame.setBounds(236, 400, 728, 96);
        menuPanel.add(btnJoinGame);

    }

    private void startLocalGame(ActionEvent e) {
        frame.getContentPane().removeAll();
        frame.setVisible(false);
        Player p1 = new Player("PLAYER BLUE");
        Player p2 = new Player("PLAYER RED");
        model = new GameField();
        model.addPlayer(p1);
        model.addPlayer(p2);
        cntrl = new BilliardController(model);

        initialize();
    }

    private void joinGame(ActionEvent e) {
        frame.dispose(); // Chiude la finestra attuale
        SwingUtilities.invokeLater(() -> {
            HomePage homePage = new HomePage(); // Crea una nuova istanza di HomePage
            homePage.setVisible(true); // Mostra la finestra della HomePage
        });
    }

    public void initialize() {
        frame = new JFrame("Billiard Game");
        GameView gameView = new GameView(cntrl);
        frame.add(gameView, BorderLayout.CENTER);
        frame.setSize(TABLE_WIDTH + 16, TABLE_HEIGHT + 39 + 70);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        frame.setAlwaysOnTop(true);
        centerFrame(frame);
    }

    public void centerFrame(JFrame frame) {
        frame.setResizable(false);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) (screenSize.getWidth() / 2 - frame.getWidth() / 2);
        int y = (int) (screenSize.getHeight() / 2 - frame.getHeight() / 2);

        frame.setLocation(x, y);
    }

}
