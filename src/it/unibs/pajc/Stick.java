package it.unibs.pajc;

public class Stick {
    private double angleDegrees; // Angolo in gradi
    private double power = 0;

    public static final int MAX_POWER = 80;

    // Getter e Setter per l'angolo in gradi
    public double getAngleDegrees() {
        return angleDegrees;
    }

    public void setAngleDegrees(double angleDegrees) {
        this.angleDegrees = angleDegrees;
    }

    // Getter per l'angolo in radianti (usato nei calcoli interni)
    public double getAngleRadians() {
        return Math.toRadians(angleDegrees);
    }

    // Getter e Setter per la potenza
    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = Math.min(Math.max(power, 0), MAX_POWER); 
    }

    // Metodo per calcolare la velocità della palla bianca
    public double[] calculateBallVelocity() {
        double vx = -power * Math.cos(getAngleRadians()); // Componente x della velocità
        double vy = -power * Math.sin(getAngleRadians()); // Componente y della velocità
        return new double[] { vx, vy };
    }

}
