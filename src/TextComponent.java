import java.awt.Color;
import java.awt.Font;

class TextComponent {
    private final String text;
    private final Font font = new Font("Arial", Font.BOLD, 11);
    private final Color color;
    private final double x;
    private final double y;

    public TextComponent(String text, Color color, double x, double y) {
        this.text = text;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public String getText() {
        return text;
    }

    public Font getFont() {
        return font;
    }

    public Color getColor() {
        return color;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
