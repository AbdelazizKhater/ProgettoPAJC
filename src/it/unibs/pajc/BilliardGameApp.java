package it.unibs.pajc;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

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
        Player p1 = new Player("P1");
        Player p2 = new Player("P2");
        model = new GameField(p1);
        model.addPlayer2(p2);
        cntrl = new BilliardController(model);

        model.addChangeListener(this::modelUpdated);

        initialize();
    }

    public void initialize() {
        frame = new JFrame("Billiard Game");
        GameView gameView = new GameView(cntrl);
        frame.add(gameView, BorderLayout.CENTER);
        frame.setSize(TABLE_WIDTH + 16, TABLE_HEIGHT + 39);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        frame.setAlwaysOnTop(true);
        frame.setResizable(false);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) (screenSize.getWidth() / 2 - frame.getWidth() / 2);
        int y = (int) (screenSize.getHeight() / 2 - frame.getHeight() / 2);

        frame.setLocation(x, y);

    }

    private void modelUpdated(ChangeEvent e) {
        Runnable task = () -> frame.repaint();
        ;

        if (EventQueue.isDispatchThread())
            task.run();
        else
            SwingUtilities.invokeLater(task);
    }
}
