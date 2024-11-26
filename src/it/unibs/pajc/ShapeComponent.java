package it.unibs.pajc;

import java.awt.Color;
import java.awt.RadialGradientPaint;
import java.awt.Shape;

class ShapeComponent {
    private final Shape shape;
    private final Color color;
    private final RadialGradientPaint gradient;

    public ShapeComponent(Shape shape, Color color) {
        this.shape = shape;
        this.color = color;
        this.gradient = null;
    }

    public ShapeComponent(Shape shape, RadialGradientPaint gradient) {
        this.shape = shape;
        this.gradient = gradient;
        this.color = null;
    }

    public Shape getShape() {
        return shape;
    }

    public Color getColor() {
        return color;
    }

    public RadialGradientPaint getGradient() {
        return gradient;
    }
}