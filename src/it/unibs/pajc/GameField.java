package it.unibs.pajc;
//MODEL

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unibs.pajc.fieldcomponents.Ball;
import it.unibs.pajc.fieldcomponents.Pocket;
import it.unibs.pajc.fieldcomponents.Stick;
import it.unibs.pajc.fieldcomponents.Trapezoid;

import static it.unibs.pajc.util.CostantiStatiche.*;
import static it.unibs.pajc.GameStatus.*;

/**
 *
 */
public class GameField {

    public AtomicBoolean done;
    private final ArrayList<Ball> balls;
    private final ArrayList<Trapezoid> trapezoids;
    private final ArrayList<Pocket> pockets;
    private final ArrayList<Integer> pottedBallsId;
    private final ArrayList<Integer> pottedBallsIdLastRound;
    private final Stick stick;
    private final Ball cueBall;
    private final Player[] players = new Player[2]; // id: 1
    private int currentPlayerIndx;// indx del Player corrente
    private GameStatus status;
    private boolean ballsAssigned = false;
    private int idBallHit = -1;
    private int idFirstBallPocketed = -1;
    private int roundCounter;
    private int playerCount;
    private boolean foulHandled;
    private Player winningPlayer;

    public GameField() {
        balls = new ArrayList<>();
        trapezoids = new ArrayList<>();
        pockets = new ArrayList<>();
        pottedBallsId = new ArrayList<>();
        pottedBallsIdLastRound = new ArrayList<>();
        stick = new Stick();
        cueBall = new Ball(200, TABLE_HEIGHT / 2.0, 0, 0, 0);
        currentPlayerIndx = 0;
        roundCounter = -1;
        playerCount = 0;
        setupInitialPositions();
    }


    public void stepNext() {
        if (!evaluationTriggered && status == roundStart)
            resetRound();

        for (int i = 0; i < balls.size(); i++) {
            final Ball ball = balls.get(i);

            // Aggiorna la posizione e controlla i limiti
            ball.updatePosition();
            ball.checkBounds(trapezoids);

            // Controlla collisione con le buche
            checkPocketCollision(ball);

            // Controlla collisioni con altre palline
            checkOtherBallCollision(i, ball);
        }

        done.set(true);
    }

    public void addPlayer(Player p) {
        players[playerCount] = p;
        p.setId(playerCount);
        playerCount++;
        if (playerCount == 1) status = gameStart;
    }

    public Player getCurrentPlayer() {
        return players[currentPlayerIndx];
    }

    public Player getWaitingPlayer() {
        int secondPlayerIndex = currentPlayerIndx == 0 ? 1 : 0;
        return players[secondPlayerIndex];
    }

    private void swapPlayers() {
        if(roundCounter > 0) {
            currentPlayerIndx = currentPlayerIndx == 0 ? 1 : 0;
            System.out.println("Turno del giocatore " + (currentPlayerIndx + 1));
        }
    }

    /**
     * metodo statico che viene chiamato all'inizio della partita, dispone le biglie nella loro posizione
     * triangolare.
     */
    private void setupInitialPositions() {
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

        balls.add(cueBall);
        //Posizione di partenza della piramide
        int startX = 800;
        int startY = TABLE_HEIGHT / 2;
        int rows = 5;

        List<Integer> ballNumbers = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            ballNumbers.add(i);
        }

        // Aggiunge le biglie in una disposizione piramidale.
        int numberIndex = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col <= row; col++) {
                double x = startX + row * (BALL_RADIUS * 2 * Math.sqrt(3) / 2);
                double y = startY - row * BALL_RADIUS + col * BALL_RADIUS * 2;
                balls.add(new Ball(x, y, 0, 0, ballNumbers.get(numberIndex++)));
            }
        }

        stick.setAngleDegrees(180);
    }

    /**
     * Gestione dei check delle collisioni per ogni pallina in gioco
     */
    private void checkOtherBallCollision(int i, Ball ball) {

        for (int j = i + 1; j < balls.size(); j++) {
            Ball other = balls.get(j);
            if (ball.checkCollision(other) && (!ball.isStationary() || !other.isStationary())) {
                if (idBallHit < 0)
                    idBallHit = other.getBallNumber();
                ball.resolveCollision(other);
                SoundControl.BALL_COLLISION.play();
            }
        }
    }

    public ArrayList<Integer> getPottedBallsId() {
        return pottedBallsId;
    }

    /**
     * Viene controllata la collisione con ogni buca, non appena il centro della biglia tocca la buca,
     * la biglia viene tolta dal gioco. Gestione separata della biglia bianca.
     */
    private void checkPocketCollision(Ball ball) {
        for (Pocket pocket : pockets) {
            if (ball.handleCollisionWithPocket(pocket)) {
                SoundControl.BALL_POTTED.play();
                //Per la biglia bianca, viene settato il boolean di riposizionamento.
                if (ball.isWhite()) {
                    ball.setNeedsReposition(true);
                    ball.resetSpeed();
                    pottedBallsIdLastRound.add(0);
                    idFirstBallPocketed = 0;
                    balls.remove(ball);
                } else {
                    if (idFirstBallPocketed < 1) {
                        idFirstBallPocketed = ball.getBallNumber();
                        pottedBallsId.add(ball.getNumber());
                        pottedBallsIdLastRound.add(ball.getNumber());
                        balls.remove(ball);
                    }
                }
                // Una volta che la biglia entra in una delle buche, non vengono fatti altri controlli
                //break;
            }
        }
    }

    private boolean evaluationTriggered = false;

    /**
     * Ritorna true quando tutte le biglie sono ferme, utilizzato per gestire i turni e l'assegnamento di falli
     */
    public boolean allBallsAreStationary() {
        for (Ball ball : balls) {
            if (!ball.isStationary()) {
                status = playing;
                evaluationTriggered = false;
                return false;
            }
        }
        if(status == gameStart) foulDetected();
        if(status != completed) status = roundStart;
        return true;
    }

    public boolean reduceStickVisualPower(double speed) {
        Stick stick = getStick();
        if (stick.getVisualPower() > 0) {
            stick.setVisualPower(stick.getVisualPower() - speed);
            return true;
        }
        return false;
    }

    /**
     * Applica la velocità alla biglia bianca dopo il tiro
     */
    public void hitBall() {
        double[] velocity = stick.calculateBallVelocity();
        cueBall.applyVelocity(velocity);
        stick.setPower(0);
    }

    public GameStatus getStatus() {
        return status;
    }

    /**
     * Metodo che aiuta a tener traccia della prima pallina colpita e della prima
     * pallina in buca
     * utilizzato per capire se è stato commesso un foul e per valutare lo switch
     * del player
     * Chiamato appena le palline si fermano
     */
    public void resetRound() {
        checkIf8BallPotted();
        evaluateValidHit();
        evaluateIfCueBallHitAnything();
        evaluateBallsPotted();
        evaluateRound();
        pottedBallsIdLastRound.clear();
        idBallHit = -1;
        idFirstBallPocketed = -1;
        if(status != completed) status = waitingPlayer2;
        roundCounter++;
    }

    /**
     * Vengono valutati i principali stati di gioco, nei casi non venga messa in
     * buca una biglia
     * si cambia giocatore, se le biglie non sono ancora state assegnate vengono
     * assegnate, se la
     * biglia 8 entra in buca si valuta la condizione di vittoria, se vera il
     * giocatore ha vinto,
     * se falsa ha perso istantaneamente
     */
    private void evaluateRound() {
        evaluationTriggered = true;
        // Se nessuna pallina è stata messa in buca si cambia giocatore
        if (idFirstBallPocketed < 0 && !cueBall.needsReposition() && !foulHandled) {
            swapPlayers();
        } else if (!ballsAssigned && roundCounter > 1) {
            //Si entra nell'if se una pallina è stata messa in buca, e vengono assegnate se non è ancora successo
            assignBallType();
        }
        foulHandled = false;
    }

    /**
     * Una volta che la biglia 8 entra in buca, viene dichiarato un vincitore. Se le condizioni di vittoria
     * sono soddisfatte, il giocatore vince, altrimenti vince l'avversario.
     * La partita è conclusa.
     */
    private void checkIf8BallPotted() {
        if (pottedBallsId.contains(8)) {
            status = completed;
            if (checkWinCondition(getCurrentPlayer())) {
                System.out.println("Il giocatore " + getCurrentPlayer().getName() + " vince!");
                winningPlayer = getCurrentPlayer();
            } else {
                System.out.println("Il giocatore " + getWaitingPlayer().getName() + " vince!");
                winningPlayer = getWaitingPlayer();
            }
        }
    }

    /**
     * Dopo il break, non appena una biglia viene messa in buca viene assegnato il suo tipo al giocatore corrente,
     * l'altro tipo viene automaticamente assegnato al secondo giocatore.
     */
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
     * Se il giocatore colpisce le palline dell'altro o la pallina 8 il prossimo
     * giocatore
     * avrà palla in mano (foul)
     */
    public void evaluateValidHit() {
        if (ballsAssigned && status != cueBallRepositioning) {
            Player p = getCurrentPlayer();
            if (idBallHit == 8 && !checkWinCondition(p)) {
                foulDetected();
            } else if (p.isStripedBalls() && idBallHit < 8) {
                foulDetected();
            } else if (!p.isStripedBalls() && idBallHit > 8) {
                foulDetected();
            }
        }
    }

    /**
     * Viene controllato ogni turno che le biglie messe in buca siano quelle del giocatore corrente. Se
     * il giocatore mette in buca una biglia non sua (o la biglia bianca), viene commesso un fallo.
     */
    private void evaluateBallsPotted() {
        if(ballsAssigned) {
            for (int id : pottedBallsIdLastRound) {
                if (getCurrentPlayer().isStripedBalls() && id < 8) {
                    foulDetected();
                } else if (!getCurrentPlayer().isStripedBalls() && (id > 8 || id == 0)) {
                    foulDetected();
                }
            }
        }
    }

    /**
     * Gestione dei falli, riposizionamento della biglia bianca e cambio turno.
     */
    private void foulDetected() {
        if (!foulHandled) {
            cueBall.setNeedsReposition(true);
            swapPlayers();
            foulHandled = true;
            balls.remove(cueBall);
        }
    }

    /**
     * Controllo dei tiri a vuoto, se nessuna pallina viene colpita, l'idballhit sarà a -1, quindi viene
     * dichiarato fallo.
     */
    private void evaluateIfCueBallHitAnything() {
        if (idBallHit < 0 && roundCounter > 1 && status != cueBallRepositioning && !foulHandled) {
            foulDetected();
        }
    }

    /**
     * Metodo utilizzato per controllare che tutte le biglie del giocatore siano in
     * buca
     * una volta che viene messa in buca la biglia 8. Vengono controllate le biglie
     * con
     * id 1-7 per il giocatore con i solidi, viene applicato un offset di +8 per il
     * giocatore
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
            // Se anche solo una biglia non appare nelle pottedBalls, il giocatore ha perso
            if (!pottedBallsId.contains(i))
                return false;
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
        return cueBall;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public boolean isBallsAssigned() {
        return ballsAssigned;
    }

    public Player[] getPlayers() {
        return players;
    }

    public int getRoundNumber() {
        return roundCounter;
    }

    public int getCurrentPlayerIndx() {
        return currentPlayerIndx;
    }

    public void setFoulHandled() {
        this.foulHandled = true;
    }

    public String getWinningPlayer() {
        return winningPlayer.getName();
    }

}