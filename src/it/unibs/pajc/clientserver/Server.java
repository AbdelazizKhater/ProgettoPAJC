package it.unibs.pajc.clientserver;


import it.unibs.pajc.Player;
import it.unibs.pajc.GameField;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Classe Server
 */
public class Server {
    private static int uniqueId;
    // Lista client connessi
    private final ArrayList<ClientThread> clientThreadList;
    // numero porta connessione
    private final int port;
    private final String notif = " *** ";
    //Model del gioco
    public static GameField model;
    //Frame che ci permette di vedere cosa succede sul server
    public static ViewServer frame;

    /**
     * Costruttore classe Server
     */
    public Server() throws UnknownHostException {
        this.port = 1500;
        clientThreadList = new ArrayList<ClientThread>();
        frame = new ViewServer(Inet4Address.getLocalHost().getHostAddress(), port);
        frame.setVisible(true);

        model = new GameField(new Player("Prova"));
    }

    /**
     * Metodo che avvia il server
     */
    public void start() {
        // attributo che indica se il server è in esecuzione
        boolean keepGoing = true;
        //crea il socket e aspetta connessioni dai client
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            // Loop infinito per aspettare connessioni
            while (keepGoing) {
                display("Server waiting for Clients on port " + port + ".");

                if (!keepGoing) break;

                //Accetto connessione al server se gli utenti connessi sono meno di 2
                //altrimenti la richiesta di connessione viene rifuitata
                Socket socket = serverSocket.accept();
                if (clientThreadList.size() < 2) {

                    // thread client
                    ClientThread t = new ClientThread(socket);
                    clientThreadList.add(t);

                    frame.repaintPeople(clientThreadList);
                    //broadcastFerme(clientThreadList.size());
                    t.start();
                } else {
                    socket.close();
                    serverSocket.close();
                }
            }

            // chiusura del server
            try {
                serverSocket.close();
                for (int i = 0; i < clientThreadList.size(); ++i) {
                    ClientThread clientThread = clientThreadList.get(i);
                    try {
                        // Chiusura di DataStream
                        clientThread.sInput.close();
                        clientThread.sOutput.close();
                        clientThread.socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                display("Exception closing the server and clients: " + e);
            }
        } catch (IOException e) {
            String msg = " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }


    /**
     * Stampa in console messaggi per controllare stato server
     * @param msg
     */
    private void display(String msg) {
        String time = msg;
        System.out.println(time);
    }

    //messaggio per il cambio turno (quando tutte le palline sono ferme)

    private boolean broadcastFerme(int connectedClients) {

        for (int i = clientThreadList.size(); --i >= 0; ) {
            ClientThread clientThread = clientThreadList.get(i);
            String team;
            if (i % 2 == 0) {
                team = "T1";
            } else {
                team = "T2";
            }

            //TODO: figure out the fucking message
            StringBuilder messageLf = new StringBuilder(model.messaggioPos() + connectedClients + "\n");
            sendMessageToAllClients(messageLf, connectedClients, clientThread, i);
        }

        return true;
    }

    //messaggio che inviamo a tutti i client conessi in cui aggiorna la posizione, quando sono in movimento

    private boolean broadcast(int connectedClients) {

        for (int i = clientThreadList.size(); --i >= 0; ) {
            ClientThread clientThread = clientThreadList.get(i);
            String team;
            if (i % 2 == 0) {
                team = "T1";
            } else {
                team = "T2";
            }
            StringBuilder messageLf = new StringBuilder(model.messaggioPos() + connectedClients +
                    "@" + team + "@null" + "@" + "\n");
            sendMessageToAllClients(messageLf, connectedClients, clientThread, i);
        }

        return true;
    }

    private void sendMessageToAllClients(StringBuilder msg, int connectedClients, ClientThread clientThread, int i) {
        for (int j = 0; j < connectedClients; j++) {
            msg.append(clientThreadList.get(j).username).append("\n");
        }

        // Provo a scrivere al Client, se la procedura fallisce lo elimino dall'array
        if (!clientThread.writeMsg(msg.toString())) {
            clientThreadList.remove(i);
            display("Disconnected Client " + clientThread.username + " removed from list.");
        }
    }



    /**
     * Se il client invia un messaggio di tipo LOGOUT
     * il client viene elimintao dalla lista al
     *
     * @param id
     */
    synchronized void remove(int id) {

        String disconnectedClient = "";
        for (int i = 0; i < clientThreadList.size(); ++i) {
            ClientThread ct = clientThreadList.get(i);
            if (ct.id == id) {
                disconnectedClient = ct.getUsername();
                clientThreadList.remove(i);
                break;
            }
        }
        //broadcast(clientThreadList.size());
    }


    /**
     * Un instanza di questo thread sarà eseguita per ogni client
     */
    public class ClientThread extends Thread {
        // socket ricezione messaggio client
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        public String username;
        Message clientMessage;
        String date;
        Timer timer;

        //Costruttore
        ClientThread(Socket socket) {
            id = ++uniqueId;
            this.socket = socket;

            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                // Legge il primo messaggio inviato
                username = (String) sInput.readObject();
                //broadcast(clientThreadList.size());

            } catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            } catch (ClassNotFoundException e) {
            }
            date = new Date().toString() + "\n";
        }

        public String getUsername() {
            return username;
        }

        /**
         * Metodo che esegue un loop infinito e prende i messaggi del client
         * finchè non viene eseguito il messaggio di LOGOUT
         */
        public void run() {
            boolean keepGoing = true;
            while (keepGoing) {
                try {
                    clientMessage = (Message) sInput.readObject();
                } catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    break;
                } catch (ClassNotFoundException e2) {
                    break;
                }
                // Prende il messaggio ricevuto dal client
                String message = this.clientMessage.getMessage();

                // Azioni differenti in base all'azione scelta
                switch (this.clientMessage.getType()) {
                    case Message.MESSAGE:
                        //formato messaggio x@y@distance@angle
                        //dove x e y sono le coordinate del primo oggetto che si muove
                        if (!message.isEmpty()) {
                            model.cambioTurno();
                            System.out.println("MESSAGGIO INVIATO" + message);
                            String part[] = message.split("@");
                            FieldObject selezionata = model.pedinaSelezionata(Double.parseDouble(part[0]), Double.parseDouble(part[1]));
                            if (selezionata != null)
                                selezionata.start(Integer.parseInt(part[2]), Double.parseDouble(part[3]));
                        }
                        timer = new Timer(10, (e) -> {
                            if (!model.allBallsAreStationary()) {
                                model.updateGame();
                                broadcast(clientThreadList.size());
                            } else {
                                timer.stop();
                                broadcastFerme(clientThreadList.size());
                            }
                        });
                        timer.start();
                        break;

                }

            }
            remove(id);

            close();
            frame.repaintPeople(clientThreadList);
        }

        /**
         * Chiude tutto
         */
        private void close() {
            try {
                if (sOutput != null) sOutput.close();
            } catch (Exception e) {
            }
            try {
                if (sInput != null) sInput.close();
            } catch (Exception e) {
            }
            ;
            try {
                if (socket != null) socket.close();
            } catch (Exception e) {
            }
        }

        /**
         * Metodo che ci permette di scrivere il messaggio al client
         * @param msg
         * @return
         */
        private boolean writeMsg(String msg) {
            //invia il messaggio se il client è ancora connesso
            if (!socket.isConnected()) {
                close();
                return false;
            }
            try {
                sOutput.writeObject(msg);

            } catch (IOException e) {
                display(notif + "Error sending message to " + username + notif);
                display(e.toString());
            }
            return true;
        }
    }

}

