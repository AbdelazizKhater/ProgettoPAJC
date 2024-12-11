package it.unibs.pajc;
//MODEL

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import it.unibs.pajc.fieldcomponents.Ball;
import it.unibs.pajc.fieldcomponents.Pocket;
import it.unibs.pajc.fieldcomponents.Stick;
import it.unibs.pajc.fieldcomponents.Trapezoid;

import static it.unibs.pajc.util.CostantiStatiche.*;
import static it.unibs.pajc.GameStatus.*;

/**
 *
 */
public class GameField extends BaseModel {

    private final ArrayList<Ball> balls;
    private final ArrayList<Ball> pottedBalls;
    private final ArrayList<Trapezoid> trapezoids;
    private final ArrayList<Pocket> pockets;
    private final ArrayList<Integer> pottedBallsId;
    private final Stick stick;
    private final Ball cueBall;
    private final Player[] players = new Player[2]; // id: 1
    private int currentPlayerIndx;// indx del Player corrente
    private GameStatus status;
    private boolean ballsAssigned = false;
    private int idBallHit = -1;
    private int idFirstBallPocketed = -1;
    private int roundCounter;


    public GameField(Player p) {
        balls = new ArrayList<>();
        pottedBalls = new ArrayList<>();
        trapezoids = new ArrayList<>();
        pockets = new ArrayList<>();
        pottedBallsId = new ArrayList<>();
        stick = new Stick();
        cueBall = new Ball(200, TABLE_HEIGHT / 2.0, 0, 0, 0);
        //Inizializzato il primo giocatore con indice 0
        players[0] = p;
        currentPlayerIndx = 0;
        roundCounter = 0;

        // Add billiard balls in initial positions
        setupInitialPositions();
    }

    public void stepNext() {
        if (!evaluationTriggered && status == roundStart) resetRound();
        for (int i = 0; i < balls.size(); i++) {
            Ball ball = balls.get(i);
            ball.updatePosition();
            ball.checkBounds(trapezoids);
            checkPocketCollision(ball);
            checkOtherBallCollision(i, ball);
        }

    }

    public void addPlayer2(Player p) {
        players[1] = p;
        startNewGame();
    }


    private final Random rnd = new Random();

    public void startNewGame() {
        //Il primo turno viene assegnato a caso tra i due giocatori
        currentPlayerIndx = rnd.nextInt(2);
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

        List<Integer> ballNumbers = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            ballNumbers.add(i);
        }

        // Shuffle the list, but keep 1 at the first position and 8 in the center
        ballNumbers.remove((Integer) 1); // Remove 1 temporarily
        ballNumbers.remove((Integer) 8); // Remove 8 temporarily
        Collections.shuffle(ballNumbers); // Shuffle the rest of the numbers

        // Insert 1 at the top of the triangle
        ballNumbers.add(0, 1);

        // Determine the center position (third row, second column)
        int centerRow = 2; // Row index (starting from 0)
        int centerCol = 1; // Column index within the row
        int centerIndex = 0;

        // Calculate the linear index for the center position
        for (int row = 0; row < centerRow; row++) {
            centerIndex += row + 1;
        }
        centerIndex += centerCol;

        ballNumbers.add(centerIndex, 8); // Place 8-ball in the center

        // Add numbered balls in a triangular configuration
        int numberIndex = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col <= row; col++) {
                double x = startX + row * (ballRadius * 2 * Math.sqrt(3) / 2);
                double y = startY - row * ballRadius + col * ballRadius * 2;
                balls.add(new Ball(x, y, 0, 0, ballNumbers.get(numberIndex++)));
            }
        }

        stick.setAngleDegrees(180);
    }

    private void checkOtherBallCollision(int i, Ball ball) {
        for (int j = i + 1; j < balls.size(); j++) {
            Ball other = balls.get(j);
            if (ball.checkCollision(other) && (!ball.isStationary() || !other.isStationary())) {
                if (idBallHit < 0) idBallHit = other.getBallNumber();
                ball.resolveCollision(other);
                SoundControl.BALL_COLLISION.play();
            }
        }
    }

    private void checkPocketCollision(Ball ball) {
        for (Pocket pocket : pockets) {
            if (ball.handleCollisionWithPocket(pocket)) {
                SoundControl.BALL_POTTED.play();
                if (ball.isWhite()) {
                    ball.setNeedsReposition(true);
                    ball.resetSpeed();
                    balls.remove(ball);
                    //balls.addFirst(ball);
                } else {
                    if (idFirstBallPocketed < 1) idFirstBallPocketed = ball.getBallNumber();
                    pottedBalls.add(ball);
                    pottedBallsId.add(ball.getNumber());
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

    public GameStatus getStatus() {
        return status;
    }

    /**
     * Metodo che aiuta a tener traccia della prima pallina colpita e della prima pallina in buca
     * utilizzato per capire se è stato commesso un foul e per valutare lo switch del player
     * Chiamato appena le palline si fermano
     */
    public void resetRound() {
        evaluateValidHit();
        evaluateIfCueBallHitAnything();
        evaluateRound();
        idBallHit = -1;
        idFirstBallPocketed = -1;
        status = waitingPlayer2;
        roundCounter++;
    }

    /**
     * Vengono valutati i principali stati di gioco, nei casi non venga messa in buca una biglia
     * si cambia giocatore, se le biglie non sono ancora state assegnate vengono assegnate, se la
     * biglia 8 entra in buca si valuta la condizione di vittoria, se vera il giocatore ha vinto,
     * se falsa ha perso istantaneamente
     */
    private void evaluateRound() {
        evaluationTriggered = true;
        //Se nessuna pallina è stata messa in buca si cambia giocatore
        if (idFirstBallPocketed < 1 && !cueBall.needsReposition()) {
            swapPlayers();
        } else if (!ballsAssigned && roundCounter > 1) {
            assignBallType();
        }
        checkIf8BallPotted();
    }

    private void checkIf8BallPotted() {
        if (pottedBallsId.contains(8)) {
            status = completed;
            if (checkWinCondition(getCurrentPlayer())) {
                System.out.println("Il giocatore " + getCurrentPlayer().getName() + " vince!");
            } else {
                System.out.println("Il giocatore " + getWaitingPlayer().getName() + " vince!");
            }
        }
    }

    private void assignBallType() {
        Player p1 = getCurrentPlayer();
        Player p2 = getWaitingPlayer();
        if (idFirstBallPocketed > 0) {
            if (idFirstBallPocketed < 8) {
                p1.setStripedBalls(false);
                p2.setStripedBalls(true);
                System.out.println("Giocatore " + p1.name + " gioca con le biglie piene");
                System.out.println("Giocatore " + p2.name + " gioca con le biglie striate");
            } else {
                p1.setStripedBalls(true);
                p2.setStripedBalls(false);
                System.out.println("Giocatore " + p1.name + " gioca con le biglie striate");
                System.out.println("Giocatore " + p2.name + " gioca con le biglie piene");
            }
            ballsAssigned = true;
        }
    }

    /**
     * Se il giocatore colpisce le palline dell'altro o la pallina 8 il prossimo giocatore
     * avrà palla in mano (foul)
     */
    public void evaluateValidHit() {
        if (ballsAssigned && status != cueBallRepositioning) {
            Player p = getCurrentPlayer();
            if (idBallHit == 8 && !checkWinCondition(p)) {
                cueBall.setNeedsReposition(true);
            } else if (p.isStripedBalls() && idBallHit < 8) {
                cueBall.setNeedsReposition(true);
            } else if (!p.isStripedBalls() && idBallHit > 8) {
                cueBall.setNeedsReposition(true);
            }
        }
    }


    private void evaluateIfCueBallHitAnything() {
        if (idBallHit < 0 && roundCounter > 1 && status != cueBallRepositioning) {
            cueBall.setNeedsReposition(true);
        }
    }

    /**
     * Metodo utilizzato per controllare che tutte le biglie del giocatore siano in buca
     * una volta che viene messa in buca la biglia 8. Vengono controllate le biglie con
     * id 1-7 per il giocatore con i solidi, viene applicato un offset di +8 per il giocatore
     * con le biglie striate
     */
    private boolean checkWinCondition(Player p) {
        int offset = 8;
        int i = 1, end = 8;
        if (p.isStripedBalls()) {
            i += offset;
            end += offset;
        }
        for (; i < end; i++) {
            //Se anche solo una biglia non appare nelle pottedBalls, il giocatore ha perso
            if (!pottedBallsId.contains(i)) return false;
        }
        return true;
    }

    public ArrayList<Ball> getBalls() {
        return balls;
    }

    public Stick getStick() {
        return stick;
    }

    public Ball getCueBall() {
        return balls.getFirst();
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }
}