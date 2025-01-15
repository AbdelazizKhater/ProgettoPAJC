package it.unibs.pajc;

public class Player {
    protected int id;
    protected String name;
    private boolean stripedBalls;

    
    public Player(String name) {
        this.name = name;
    }
    
    public String getName() { return name; }

    public void setStripedBalls(boolean stripedBalls) {
        this.stripedBalls = stripedBalls;
    }

    public boolean isStripedBalls() {
        return stripedBalls;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
