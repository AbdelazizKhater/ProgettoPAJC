package it.unibs.pajc;
//MODEL

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static it.unibs.pajc.CostantiStatiche.*;

/**
 *
 */
public class GameField extends BaseModel {
    public enum GameStatus {
        waitingPlayer2,
        playing,
        completed
    }

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
    private boolean firstShot = true;
    private boolean foulDetected = false;
    private int idBallHit = -1;

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

        // Add billiard balls in initial positions
        setupInitialPositions();
    }

    public void addPlayer2(Player p) {
        if(status != GameStatus.waitingPlayer2)
            return;
        players[1] = p;
        startNewGame();
    }


    private Random rnd = new Random();
    public void startNewGame() {
        //Il primo turno viene assegnato a caso tra i due giocatori
        currentPlayerIndx = rnd.nextInt(2);
        status = GameStatus.playing;
        fireChangeListener();
    }

    public Player getCurrentPlayer() {
        return players[currentPlayerIndx];
    }

    private void swapPlayers() {
        currentPlayerIndx = currentPlayerIndx == 0 ? 1 : 0;
        fireChangeListener();
    }


    public List<GameFieldObject> getGameFieldObjects() {
        List<GameFieldObject> gameFieldObjects = new ArrayList<>();
        // Aggiungi tutte le palline
        gameFieldObjects.addAll(balls);
        // Aggiungi tutti i trapezi
        gameFieldObjects.addAll(trapezoids);
        // Aggiungi tutte le buche
        gameFieldObjects.addAll(pockets);
        return gameFieldObjects;
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

    public boolean allBallsAreStationary() {
        for (Ball ball : balls) {
            if (!ball.isStationary()) {
                status = GameStatus.playing;
                firstShot = false;
                return false;
            }
        }
        status = GameStatus.waitingPlayer2;
        return true;
    }

    public boolean ballsHit(int firstBall, int finalBall) {
        for(int i = firstBall; i < finalBall; i++) {
            //Se una delle palline controllate si muove, è stata colpita
            if(!balls.get(i).isStationary()) return true;
        }
        return false;
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

    public boolean allBallsInPlay() {
        return (balls.size() == 16);
    }

    /**
     * Metodo che aiuta a tener traccia della prima palla colpita, utilizzato per capire se è stato commesso
     * un foul
     */
    public void resetRound() {
        idBallHit = -1;
        foulDetected = false;
    }

    //RULES

    public void assignBallType(Player p1, Player p2) {
        Ball firstPocketedBall = pocketedBalls.getFirst();
        if (firstPocketedBall.getBallNumber() < 8) {
            p1.setStripedBalls(false);
            p2.setStripedBalls(true);
        } else if (firstPocketedBall.getBallNumber() > 8) {
            p1.setStripedBalls(true);
            p2.setStripedBalls(false);
        }
        ballsAssigned = true;
    }

    /**
     * Se il giocatore colpisce le palline dell'altro o la pallina 8 il prossimo giocatore
     * avrà palla in mano (foul)
     */
    public void evaluateValidHit() {
        Player p = getCurrentPlayer();
        if(p.isStripedBalls() && idBallHit < 9) {
            foulDetected = true;
        }
        else if(!p.isStripedBalls() && idBallHit > 7) {
            foulDetected = true;
        }
    }

    public boolean isFoulDetected() {
        return foulDetected;
    }
}
