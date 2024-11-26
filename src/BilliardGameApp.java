import javax.swing.*;
import java.awt.*;

public class BilliardGameApp {

    private static final int TABLE_WIDTH = 1200;
    private static final int TABLE_HEIGHT = 600;

    private BilliardController cntrl;
    private GameField model;

    private JFrame frame;

    public static void main(String[] args) {
        EventQueue.invokeLater(()-> {
            BilliardGameApp window = new BilliardGameApp();
            window.frame.setVisible(true);
        });
    }

    public BilliardGameApp() {
        model = new GameField();
        cntrl = new BilliardController(model);

        initialize();
    }

    public void initialize() {
        frame = new JFrame("Billiard Game");
        GameView gameView = new GameView(cntrl);
        frame.add(gameView);
        frame.setSize(TABLE_WIDTH + 16, TABLE_HEIGHT + 39);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }


}
