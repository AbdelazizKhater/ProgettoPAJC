package it.unibs.pajc;
//MODEL

import java.util.ArrayList;
import java.util.Random;

import static it.unibs.pajc.CostantiStatiche.*;
import static it.unibs.pajc.GameStatus.*;

/**
 *
 */
public class GameField extends BaseModel {

    private final ArrayList<Ball> balls;
    private final ArrayList<Ball> pocketedBalls;
    private final ArrayList<Trapezoid> trapezoids;
    private final ArrayList<Pocket> pockets;
    private final Stick stick;
    private final Ball cueBall;
    private final Player[] players = new Player[2]; // id: 1
    private int currentPlayerIndx;// indx del Player corrente
    private GameStatus status;
    private boolean ballsAssigned = false;
    private boolean foulDetected = false;
    private int idBallHit = -1;
    private int idFirstBallPocketed = -1;
    private int roundCounter;


    public GameField(Player p) {
        balls = new ArrayList<>();
        pocketedBalls = new ArrayList<>();
        trapezoids = new ArrayList<>();
        pockets = new ArrayList<>();
        stick = new Stick();
        cueBall = new Ball(200, TABLE_HEIGHT / 2.0, 0, 0, 0);
        //Inizializzato il primo giocatore con indice 0
        players[0] = p;
        currentPlayerIndx = 0;
        roundCounter = 0;

        // Add billiard balls in initial positions
        setupInitialPositions();
    }

    public void addPlayer2(Player p) {
        players[1] = p;
        startNewGame();
    }


    private final Random rnd = new Random();
    public void startNewGame() {
        //Il primo turno viene assegnato a caso tra i due giocatori
        currentPlayerIndx = rnd.nextInt(2);
        status = playing;
        fireChangeListener();
    }

    public Player getCurrentPlayer() {
        return players[currentPlayerIndx];
    }

    public Player getWaitingPlayer() {
        int secondPlayerIndex = currentPlayerIndx == 0 ? 1 : 0;
        return players[secondPlayerIndex];
    }

    private void swapPlayers() {
        currentPlayerIndx = currentPlayerIndx == 0 ? 1 : 0;
        System.out.println("Turno del giocatore " + (currentPlayerIndx + 1));
        fireChangeListener();
    }


    private void setupInitialPositions() {
        // Radius of each ball
        int ballRadius = 15;

        // Definizione area di ogni trapezio
        for (int i = 0; i < X_POINTS_TRAPEZI.length; i++) {
            trapezoids.add(new Trapezoid(X_POINTS_TRAPEZI[i], Y_POINTS_TRAPEZI[i]));
        }

        // Definizione area di ogni buca
        for (int[] pocketPosition : POCKET_POSITIONS) {
            int x = pocketPosition[0];
            int y = pocketPosition[1];
            int pocketRadius = pocketPosition[2] / 2;

            pockets.add(new Pocket(x, y, pocketRadius));
        }

        // Position for the white ball
        balls.add(cueBall); // White ball
        // Pyramid starting position for numbered balls
        int startX = 800; // Base X position of the triangle
        int startY = TABLE_HEIGHT / 2; // Center of the table
        int rows = 5; // Number of rows in the triangle

        // Add numbered balls in a triangular configuration
        int number = 1;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col <= row; col++) {
                double x = startX + row * (ballRadius * 2 * Math.sqrt(3) / 2);
                double y = startY - row * ballRadius + col * ballRadius * 2;
                balls.add(new Ball(x, y, 0, 0, number++));
            }
        }
        stick.setAngleDegrees(180);
    }

    public void stepNext() {
        if(!evaluationTriggered && status == roundStart && !foulDetected) resetRound();
        for (int i = 0; i < balls.size(); i++) {
            Ball ball = balls.get(i);
            ball.updatePosition();
            ball.checkBounds(trapezoids);
            checkPocketCollision(ball);
            checkOtherBallCollision(i, ball);
        }
    }

    private void checkOtherBallCollision(int i, Ball ball) {
        for (int j = i + 1; j < balls.size(); j++) {
            Ball other = balls.get(j);
            if (ball.checkCollision(other) && (!ball.isStationary() || !other.isStationary())) {
                if(idBallHit < 0) idBallHit = other.getBallNumber();
                ball.resolveCollision(other);
            }
        }
    }

    private void checkPocketCollision(Ball ball) {
        for (Pocket pocket : pockets) {
            if (ball.handleCollisionWithPocket(pocket)) {
                if (ball.isWhite()) {
                    foulDetected = true;
                    ball.setInPlay(false);
                    ball.resetSpeed();
                    balls.remove(ball);
                    balls.addFirst(ball);
                } else {
                    if (idFirstBallPocketed < 1) idFirstBallPocketed = ball.getBallNumber();
                    ball.setInPlay(false);
                    pocketedBalls.add(ball);
                    balls.remove(ball);
                    // TODO: aggiungere le palline in un panel view
                }
                // Una volta che la pallina entra in buca, non vengono fatti altri controlli
                break;
            }
        }
    }

    private boolean evaluationTriggered = false;

    public boolean allBallsAreStationary() {
        for (Ball ball : balls) {
            if (!ball.isStationary()) {
                status = playing;
                evaluationTriggered = false;
                return false;
            }
        }
        status = roundStart;
        return true;
    }

    public ArrayList<Ball> getBalls() {
        return balls;
    }

    public Stick getStick() {
        return stick;
    }

    public Ball getCueBall() {
        return cueBall;
    }

    public boolean reduceStickVisualPower(double speed) {
        Stick stick = getStick();
        if (stick.getVisualPower() > 0) {
            stick.setVisualPower(stick.getVisualPower() - speed);
            return true; // Animazione in corso
        }
        return false; // Animazione completata
    }

    public void hitBall() {
        double[] velocity = stick.calculateBallVelocity();
        cueBall.applyVelocity(velocity);
        stick.setPower(0); // Reset della potenza reale dopo il colpo
    }

    public GameStatus getStatus() { return status; }

    /**
     * Metodo che aiuta a tener traccia della prima pallina colpita e della prima pallina in buca
     * utilizzato per capire se è stato commesso un foul e per valutare lo switch del player
     * Chiamato appena le palline si fermano
     */
    public void resetRound() {
        evaluateRound();
        evaluateValidHit();
        idBallHit = -1;
        idFirstBallPocketed = -1;
        foulDetected = false;
        status = waitingPlayer2;
        roundCounter++;
    }

    private void evaluateRound() {
        //Se nessuna pallina è stata messa in buca si cambia giocatore
        evaluationTriggered = true;
        if(idFirstBallPocketed < 1) {
            swapPlayers();
        } else if(!ballsAssigned && roundCounter > 1) {
            Player p1 = getCurrentPlayer();
            Player p2 = getWaitingPlayer();
            assignBallType(p1, p2);
        }
    }

    private void assignBallType(Player p1, Player p2) {
        if (idFirstBallPocketed < 8 && idFirstBallPocketed > 0) {
            p1.setStripedBalls(false);
            p2.setStripedBalls(true);
            System.out.println("Giocatore " + p1.id + " gioca con le biglie piene");
            System.out.println("Giocatore " + p2.id + " gioca con le biglie striate");
        } else {
            p1.setStripedBalls(true);
            p2.setStripedBalls(false);
            System.out.println("Giocatore " + p1.id + " gioca con le biglie striate");
            System.out.println("Giocatore " + p2.id + " gioca con le biglie piene");
        }
        ballsAssigned = true;
    }

    /**
     * Se il giocatore colpisce le palline dell'altro o la pallina 8 il prossimo giocatore
     * avrà palla in mano (foul)
     */
    private void evaluateValidHit() {
        if(ballsAssigned) {
            Player p = getCurrentPlayer();
            if(p.isStripedBalls() && idBallHit < 9) {
                foulDetected = true;
            }
            else if(!p.isStripedBalls() && idBallHit > 7) {
                foulDetected = true;
            }
        }
    }

    public boolean isFoulDetected() {
        return foulDetected;
    }
}