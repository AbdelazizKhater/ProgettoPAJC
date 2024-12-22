package it.unibs.pajc.util;

import java.awt.*;

public class CostantiStatiche {

        public static final int TABLE_WIDTH = 1200;
        public static final int TABLE_HEIGHT = 600;
        public static final int BORDER_WIDTH = 50;
        public static final int BALL_RADIUS = 15;
        public static final int POCKET_RADIUS = 30;
        public static final int BIG_POCKET_RADIUS = 35;
        public static final double MIN_BOUND = BORDER_WIDTH * 1.5 + BALL_RADIUS;
        public static final double MAX_BOUND_X = TABLE_WIDTH - MIN_BOUND;
        public static final double MAX_BOUND_Y = TABLE_HEIGHT - MIN_BOUND;
        public static final double INNER_MARGIN = BORDER_WIDTH * 1.5;
        public static final double INNER_FIELD_LIMIT_X = TABLE_WIDTH - INNER_MARGIN;
        public static final double INNER_FIELD_LIMIT_Y = TABLE_HEIGHT - INNER_MARGIN;
        public static final String MSG_NOME = "Inserisci qui il tuo username";
        public static final String ERRORE_NOME = "INSERIRE IL NOME";
        public static final String ERRORE_IP = "IP SERVER NON CORRETTO";
        public static final int PORT = 1234;


        public static final Color[] BALL_COLORS = {
                        Color.WHITE, // Palla 0 (bianca)
                        new Color(255, 255, 0), // Palla 1 (gialla)
                        new Color(0, 0, 255), // Palla 2 (blu)
                        new Color(255, 0, 0), // Palla 3 (rossa)
                        new Color(128, 0, 128), // Palla 4 (viola)
                        new Color(255, 165, 0), // Palla 5 (arancione)
                        new Color(0, 128, 0), // Palla 6 (verde)
                        new Color(128, 0, 0), // Palla 7 (bordeaux)
                        new Color(0, 0, 0), // Palla 8 (nera)
                        new Color(255, 255, 0), // Palla 9 (gialla striata)
                        new Color(0, 0, 255), // Palla 10 (blu striata)
                        new Color(255, 0, 0), // Palla 11 (rossa striata)
                        new Color(128, 0, 128), // Palla 12 (viola striata)
                        new Color(255, 165, 0), // Palla 13 (arancione striata)
                        new Color(0, 128, 0), // Palla 14 (verde striata)
                        new Color(128, 0, 0) // Palla 15 (bordeaux striata)
        };



        public static final int[][] X_POINTS_TRAPEZI = {
                        // TRAPEZIO 1
                        { BORDER_WIDTH + BIG_POCKET_RADIUS, TABLE_WIDTH / 2 - POCKET_RADIUS,
                                        TABLE_WIDTH / 2 - POCKET_RADIUS - BORDER_WIDTH / 6,
                                        BORDER_WIDTH + POCKET_RADIUS * 2 },
                        // TRAPEZIO 2
                        { TABLE_WIDTH / 2 + POCKET_RADIUS, TABLE_WIDTH - BIG_POCKET_RADIUS - BORDER_WIDTH,
                                        TABLE_WIDTH - BORDER_WIDTH - POCKET_RADIUS * 2,
                                        TABLE_WIDTH / 2 + POCKET_RADIUS + BORDER_WIDTH / 6 },
                        // TRAPEZIO 3
                        { BORDER_WIDTH, BORDER_WIDTH + BORDER_WIDTH / 2, BORDER_WIDTH + BORDER_WIDTH / 2,
                                        BORDER_WIDTH },
                        // TRAPEZIO 4
                        { BORDER_WIDTH + POCKET_RADIUS * 2,
                                        TABLE_WIDTH / 2 - POCKET_RADIUS - BORDER_WIDTH / 6,
                                        TABLE_WIDTH / 2 - POCKET_RADIUS, BORDER_WIDTH + BIG_POCKET_RADIUS },
                        // TRAPEZIO 5
                        { TABLE_WIDTH / 2 + POCKET_RADIUS + BORDER_WIDTH / 6, TABLE_WIDTH - BORDER_WIDTH - POCKET_RADIUS * 2,
                                        TABLE_WIDTH - BIG_POCKET_RADIUS - BORDER_WIDTH,
                                        TABLE_WIDTH / 2 + POCKET_RADIUS },
                        // TRAPEZIO 6
                        { TABLE_WIDTH - BORDER_WIDTH - BORDER_WIDTH / 2, TABLE_WIDTH - BORDER_WIDTH,
                                        TABLE_WIDTH - BORDER_WIDTH,
                                        TABLE_WIDTH - BORDER_WIDTH - BORDER_WIDTH / 2 }
        };

        public static final int[][] Y_POINTS_TRAPEZI = {
                        // TRAPEZIO 1
                        { BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH + BORDER_WIDTH / 2,
                                        BORDER_WIDTH + BORDER_WIDTH / 2 },
                        // TRAPEZIO 2
                        { BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH + BORDER_WIDTH / 2,
                                        BORDER_WIDTH + BORDER_WIDTH / 2 },
                        // TRAPEZIO 3
                        { BORDER_WIDTH + BIG_POCKET_RADIUS, BORDER_WIDTH + BIG_POCKET_RADIUS + BORDER_WIDTH / 2,
                                        TABLE_HEIGHT - BORDER_WIDTH - BIG_POCKET_RADIUS - BORDER_WIDTH / 2,
                                        TABLE_HEIGHT - BORDER_WIDTH - BIG_POCKET_RADIUS },
                        // TRAPEZIO 4
                        { TABLE_HEIGHT - BORDER_WIDTH - BORDER_WIDTH / 2,
                                        TABLE_HEIGHT - BORDER_WIDTH - BORDER_WIDTH / 2,
                                        TABLE_HEIGHT - BORDER_WIDTH, TABLE_HEIGHT - BORDER_WIDTH },
                        // TRAPEZIO 5
                        { TABLE_HEIGHT - BORDER_WIDTH - BORDER_WIDTH / 2,
                                        TABLE_HEIGHT - BORDER_WIDTH - BORDER_WIDTH / 2,
                                        TABLE_HEIGHT - BORDER_WIDTH, TABLE_HEIGHT - BORDER_WIDTH },
                        // TRAPEZIO 6
                        { BORDER_WIDTH + BIG_POCKET_RADIUS + BORDER_WIDTH / 2, BORDER_WIDTH + BIG_POCKET_RADIUS,
                                        TABLE_HEIGHT - BORDER_WIDTH - BIG_POCKET_RADIUS,
                                        TABLE_HEIGHT - BORDER_WIDTH - BIG_POCKET_RADIUS - BORDER_WIDTH / 2 }
        };

        public static final int[][] POCKET_POSITIONS = {
                        // BUCA TOP LEFT
                        { BORDER_WIDTH - BIG_POCKET_RADIUS, BORDER_WIDTH - BIG_POCKET_RADIUS, BIG_POCKET_RADIUS * 2,
                                        BIG_POCKET_RADIUS * 2 },
                        // BUCA TOP
                        { (TABLE_WIDTH / 2) - POCKET_RADIUS, BORDER_WIDTH - POCKET_RADIUS - 5, POCKET_RADIUS * 2,
                                        POCKET_RADIUS * 2 },
                        // BUCA TOP RIGHT
                        { TABLE_WIDTH - BORDER_WIDTH - BIG_POCKET_RADIUS, BORDER_WIDTH - BIG_POCKET_RADIUS,
                                        BIG_POCKET_RADIUS * 2, BIG_POCKET_RADIUS * 2 },
                        // BUCA BOTTOM LEFT
                        { BORDER_WIDTH - BIG_POCKET_RADIUS, TABLE_HEIGHT - BORDER_WIDTH - BIG_POCKET_RADIUS,
                                        BIG_POCKET_RADIUS * 2, BIG_POCKET_RADIUS * 2 },
                        // BUCA BOTTOM
                        { (TABLE_WIDTH / 2) - POCKET_RADIUS, TABLE_HEIGHT - BORDER_WIDTH - POCKET_RADIUS + 5,
                                        POCKET_RADIUS * 2, POCKET_RADIUS * 2 },
                        // BUCA BOTTOM RIGHT
                        { TABLE_WIDTH - BORDER_WIDTH - BIG_POCKET_RADIUS,
                                        TABLE_HEIGHT - BORDER_WIDTH - BIG_POCKET_RADIUS,
                                        BIG_POCKET_RADIUS * 2, BIG_POCKET_RADIUS * 2 }
        };
}
