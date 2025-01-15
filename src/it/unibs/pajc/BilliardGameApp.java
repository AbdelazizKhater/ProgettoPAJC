package it.unibs.pajc;

import javax.swing.*;
import it.unibs.pajc.clientserver.IpMenuFrame;
import java.awt.*;
import java.awt.event.ActionEvent;

import static it.unibs.pajc.util.CostantiStatiche.*;

public class BilliardGameApp extends JPanel {

    private BilliardController cntrl;
    private GameField model;
    private Image mainTitleImage;

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
        loadImage();
        frame = new JFrame();
        frame.setSize(TABLE_WIDTH + 16, TABLE_HEIGHT + 84);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        centerFrame(frame);

        // Use the custom BackgroundPanel
        JPanel menuPanel = new BackgroundPanel();
        frame.getContentPane().add(menuPanel, BorderLayout.CENTER);
        menuPanel.setLayout(null);

        JButton btnSinglePlayer = new JButton("LOCAL GAME");
        btnSinglePlayer.setFont(new Font("Arial Black", Font.PLAIN, 30));
        btnSinglePlayer.setFocusPainted(false);
        btnSinglePlayer.setForeground(Color.WHITE);
        btnSinglePlayer.setBackground(new Color(32, 32, 32, 255));
        btnSinglePlayer.setBounds(358, 400, 500, 60);
        menuPanel.add(btnSinglePlayer);
        btnSinglePlayer.addActionListener(this::startLocalGame);

        JButton btnJoinGame = new JButton("JOIN REMOTE GAME");
        btnJoinGame.addActionListener(this::joinGame);
        btnJoinGame.setFocusPainted(false);
        btnJoinGame.setForeground(Color.WHITE);
        btnJoinGame.setBackground(new Color(32, 32, 32, 255));
        btnJoinGame.setFont(new Font("Arial Black", Font.PLAIN, 30));
        btnJoinGame.setBounds(358, 500, 500, 60);
        menuPanel.add(btnJoinGame);

        frame.setVisible(true);
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
            IpMenuFrame ipMenuFrame = new IpMenuFrame(); // Crea una nuova istanza di IpMenuFrame
            ipMenuFrame.setVisible(true); // Mostra la finestra della IpMenuFrame
        });
    }

    public void initialize() {
        frame = new JFrame("Billiard Game");
        GameView gameView = new GameView(cntrl);
        frame.add(gameView, BorderLayout.CENTER);
                frame.setSize(TABLE_WIDTH + 16, TABLE_HEIGHT + 39 + 130);
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

    private void loadImage() {
        // Replace "background.jpg" with your actual image file path
        mainTitleImage = Toolkit.getDefaultToolkit().getImage("resources/title_screen.png");

        // Ensures the image is fully loaded
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(mainTitleImage, 0);
        try {
            tracker.waitForAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (mainTitleImage != null) {
                g.drawImage(mainTitleImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
