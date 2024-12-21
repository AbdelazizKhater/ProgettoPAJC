package it.unibs.pajc.clientserver;

import it.unibs.pajc.BilliardController;
import it.unibs.pajc.GameFieldView;
import it.unibs.pajc.InformationPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    //componenti grafiche
    public static JFrame frame;
    public static GameFieldView mainFieldView;
    public static InformationPanel infoPanel;

    // variabili gioco
    protected  String turno;
    protected int score1, score2;
    protected String team = null;

    // variabili client
    protected static boolean close = false;
    protected ObjectInputStream sInput;        // to read from the socket
    protected ObjectOutputStream sOutput;        // to write on the socket
    protected Socket socket;                    // socket object
    protected String server, username;    // server and username
    protected int port;
    protected BilliardController cntrl;


    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public int getScore1() {
        return score1;
    }

    public void setScore1(int score1) {
        this.score1 = score1;
    }

    public int getScore2() {
        return score2;
    }

    public void setScore2(int score2) {
        this.score2 = score2;
    }
    /*
     *  costruttore
     *  server: Ip del server
     *  port: Numero di porta per la connessione
     *  username: Username
     */

    Client(String server, int port, String username, BilliardController cntrl) {
        this.server = server;
        this.port = port;
        this.username = username;
        HomePage home = new HomePage();
        home.setVisible(true);
    }

    /**
     * conessione al server
     *
     * @return true se la connessione va a buon fine
     */

    public boolean start() {
        try {
            socket = new Socket(server, port);
        } catch (Exception ec) {
            display("CONNESIONE NON RIUSCITA, SERVER PIENO");
            return false;
        }

        //Creazione Data Stream ( per scambiare messaggi)
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            display("CONNESIONE NON RIUSCITA, SERVER PIENO");
            frame.dispose();
            System.exit(0);
            return false;
        }

        // creazione thread per ascoltare dal server
        new ListenFromServer().start();
        try {
            //invio username per giocare
            sOutput.writeObject(username);
        } catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            frame.dispose();
            System.exit(0);
            return false;
        }
        return true;
    }

    /**
     * Stampa a video in un pop-up una certa stringa msg
     *
     * @param msg
     */
    private void display(String msg) {
        JOptionPane.showMessageDialog(mainFieldView, msg);
    }

    /**
     * Manda un messaggio msg al server
     *
     * @param msg
     */
    public void sendMessage(Message msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            display("Eccezione scrittura su server: " + e);
            frame.dispose();
            System.exit(0);
        }
    }

    /**
     * Quando la connessione salta
     * Vengono chiusi tutti gli stream di dati
     */
    private void disconnect() {
        try {
            if (sInput != null) sInput.close();
        } catch (Exception e) {
        }
        try {
            if (sOutput != null) sOutput.close();
        } catch (Exception e) {
        }
        try {
            if (socket != null) socket.close();
        } catch (Exception e) {
        }
    }

    /**
     * Creazione del pannello grafico per il gioco
     *
     * @param portNumber
     * @param serverAddress
     * @param userName
     */
    public static void avvioClient(int portNumber, String serverAddress, String userName) {
        Client client = new Client(serverAddress, portNumber, userName);

        infoPanel = new InformationPanel(client);
        frame = new JFrame();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setBounds(100, 50, 1300, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        infoPanel.setPreferredSize(new Dimension(1300, 120));

        mainFieldView = new GameFieldView();
        mainFieldView.setVisible(true);

        frame.add(mainFieldView, BorderLayout.CENTER);
        frame.add(infoPanel, BorderLayout.NORTH);

        if (!client.start())
            return;

        mainFieldView.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

                mainFieldView.setXnew(e.getX() - mainFieldView.getW() / 2);
                mainFieldView.setYnew(-(e.getY() - mainFieldView.getH() / 2));

                if (mainFieldView.getValido() != null)
                    mainFieldView.setRadiusPower(
                            Math.min((int) (Math.sqrt(((mainFieldView.getPosValidX() - mainFieldView.getXnew()) *
                                    (mainFieldView.getPosValidX() - mainFieldView.getXnew())) + ((mainFieldView.getPosValidY() - mainFieldView.getYnew()) *
                                    (mainFieldView.getPosValidY() - mainFieldView.getYnew())))), 150));

                mainFieldView.repaint();
            }
        });


        mainFieldView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {

                if (mainFieldView.getValido() != null) {
                    if (!(mainFieldView.getDistance() <= mainFieldView.getValidoRadius())) {

                        //Dare forma al messaggio x@y@distance@angle che verrà in seguito inviato al server
                        buildMessage(mainFieldView.getPosValidX() + "@" + mainFieldView.getPosValidY() + "@" + mainFieldView.getDistance() + "@" + mainFieldView.getAngle(), client);
                    }
                }
                mainFieldView.setValido(null);
                mainFieldView.setRadiusPower(0);
                mainFieldView.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //prendiamo coordinate x e y di dove è stato premuto il mouse
                if (client.getTeam().equals(client.getTurno())) {
                    int x = e.getX() - mainFieldView.getW() / 2;
                    int y = -(e.getY() - mainFieldView.getH() / 2);
                    mainFieldView.setValido(mainFieldView.checkClickAble(x, y));
                    if (mainFieldView.getValido() != null) {
                        if (mainFieldView.getValidoTeam().equals(client.getTeam())) {
                            mainFieldView.repaint();
                        } else {
                            mainFieldView.setValido(null);
                        }
                    }
                }
            }

        });

        if (close == true) {
            // Client ha finito il suo lavoro, disconnessione client
            client.disconnect();
        }
    }

    /**
     * Richiamo il metodo che ci permette di inviare il messaggio al server
     * dopo aver costruito il messaggio
     *
     * @param msg    Contentuto messaggio
     * @param client
     */
    private static void buildMessage(String msg, Client client) {

        if (msg.equalsIgnoreCase("LOGOUT")) {
            client.sendMessage(new Message(Message.LOGOUT, ""));
            close = true;
        } else {
            client.sendMessage(new Message(Message.MESSAGE, msg));
        }
    }

    /**
     * Classe che resta in ascolto dei messaggi dal server
     * Ed elabora le informazioni ricevute
     */
    class ListenFromServer extends Thread {

        public void run() {
            String msg = null;

            while (true) {
                try {
                    //Legge il messaggio inviato dal dataStream
                    msg = (String) sInput.readObject();

                    mainFieldView.setPos(msg);
                    String[] parts = msg.split("\n");

                    String[] riga11 = parts[11].split("@");

                    Runnable task = ()->{
                        if(riga11[5].equals("true")){
                            SoundClip collision = new SoundClip("collision");
                            collision.startSound();
                        }
                    };
                    task.run();

                    Thread thread = new Thread(task);
                    thread.start();

                    int nUsers = Integer.parseInt(riga11[0]);
                    int newScore1 = Integer.parseInt(riga11[3]);
                    int newScore2 = Integer.parseInt(riga11[4]);

                    setTeam(riga11[1]);
                    setTurno(riga11[2]);

                    if ((newScore1 != getScore1() && newScore1 < 3) || (newScore2 != getScore2() && newScore2 < 3)) {
                        SoundClip gol = new SoundClip("Goal");
                        gol.startSound();
                    }

                    setScore1(newScore1);
                    setScore2(newScore2);

                    infoPanel.setScore(score1, score2);

                    if (nUsers >= 1)
                        if (parts[12].equals(username))
                            parts[12] += "(you)";

                    if (nUsers >= 2)
                        if (parts[13].equals(username))
                            parts[13] += "(you)";

                    if (nUsers == 1) {
                        infoPanel.setUsername1(parts[12]);
                    } else {
                        infoPanel.setUsernames(parts[12], parts[13]);
                    }

                    checkWinner();

                } catch (IOException e) {
                    display("La connessione al server è stata interrotta");
                    frame.dispose();
                    System.exit(0);
                    break;
                } catch (ClassNotFoundException e2) {
                }
            }

        }
    }

    /**
     * Metodo che controlla se il vincitore
     * E stampa a video gif vincitore e perdente
     */
    public void checkWinner() {
        ImageIcon winner = new ImageIcon("winner.gif");
        ImageIcon loser = new ImageIcon("loser.gif");
        if (score1 == 3) {
            if (infoPanel.getTeam2().equals(username + "(you)")) {
                SoundClip win = new SoundClip("win");
                win.startSound();
                JOptionPane.showMessageDialog(null, null, "Hai vinto", JOptionPane.INFORMATION_MESSAGE, winner);
            } else {
                SoundClip los = new SoundClip("Loser");
                los.startSound();
                JOptionPane.showMessageDialog(null, null, "Hai perso", JOptionPane.INFORMATION_MESSAGE, loser);
            }
        } else if (score2 == 3) {
            if (infoPanel.getTeam1().equals(username + "(you)")) {
                SoundClip win = new SoundClip("win");
                win.startSound();
                JOptionPane.showMessageDialog(null, null, "Hai vinto", JOptionPane.INFORMATION_MESSAGE, winner);
            } else {
                SoundClip los = new SoundClip("Loser");
                los.startSound();
                JOptionPane.showMessageDialog(null, null, "Hai perso", JOptionPane.INFORMATION_MESSAGE, loser);

            }
        }
    }
}

