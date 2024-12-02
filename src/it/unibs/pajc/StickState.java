package it.unibs.pajc;

public interface StickState {
    void handleMouseDragged(Stick stick, double deltaX, double deltaY);
    void handleMouseReleased(Stick stick);
    void update(Stick stick, GameField model);
}

