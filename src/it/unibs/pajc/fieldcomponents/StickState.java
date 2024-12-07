package it.unibs.pajc.fieldcomponents;

import it.unibs.pajc.GameField;

public interface StickState {
    void handleMouseDragged(Stick stick, double deltaX, double deltaY);
    void handleMouseReleased(Stick stick);
    void update(Stick stick, GameField model);
}

