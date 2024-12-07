package it.unibs.pajc.fieldcomponents;


public class Stick {
    private double angleDegrees; // Angolo in gradi
    private double power; // Potenza reale per colpire la pallina
    private double visualPower; // Potenza visiva per l'animazione

    public static final int MAX_POWER = 30;

    public double getAngleDegrees() {
        return angleDegrees;
    }

    public void setAngleDegrees(double angleDegrees) {
        this.angleDegrees = angleDegrees;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = Math.min(Math.max(power, 0), MAX_POWER);
        this.visualPower = this.power; // Sincronizza la potenza visiva iniziale
    }

    public double getVisualPower() {
        return visualPower;
    }

    public void setVisualPower(double visualPower) {
        this.visualPower = Math.max(0, visualPower);
    }

    public double[] calculateBallVelocity() {
        double vx = -power * Math.cos(Math.toRadians(angleDegrees));
        double vy = -power * Math.sin(Math.toRadians(angleDegrees));
        return new double[]{vx, vy};
    }
}
