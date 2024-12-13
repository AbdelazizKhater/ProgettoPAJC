package it.unibs.pajc;



import java.awt.BorderLayout;

import javax.swing.*;


public class GameView extends JPanel{
    
    private final BilliardController cntrl;

    public GameView(BilliardController cntrl) {

        super(new BorderLayout());
        
        this.cntrl = cntrl;
        GameFieldView gameFieldPanel = new GameFieldView(cntrl);

        this.add(gameFieldPanel, BorderLayout.CENTER);
        
        //Animation Timer
        Timer timer = new Timer(5, e -> {
            cntrl.stepNext();

            gameFieldPanel.repaint();
            this.repaint();
        });

        timer.start();
    }

    

}
