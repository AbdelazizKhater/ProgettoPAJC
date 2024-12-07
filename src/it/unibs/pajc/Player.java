package it.unibs.pajc;

public class Player {
    private static int lastId = 0;
    protected int id;
    protected String name;
    private static boolean stripedBalls;

    public int getId() { return id; }
    public String getName() { return name; }

    public Player(String name) {
        this.name = name;
        this.id = ++lastId;
    }

    public void setStripedBalls(boolean stripedBalls) {
        Player.stripedBalls = stripedBalls;
    }

    public boolean isStripedBalls() {
        return stripedBalls;
    }
}
