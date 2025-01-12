package it.unibs.pajc.fieldcomponents;


public class Stick {
    private double angleDegrees; // Angolo in gradi
    private double lastPower; // L'ultima misura di power, da mandare al server
    private double power; // Potenza reale per colpire la pallina
    private double visualPower; // Potenza visiva per l'animazione

    public static final int MAX_POWER = 20;

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
        // Sincronizza la potenza visiva iniziale
        this.visualPower = this.power;
        //Tiene memoria dell'ultima potenza, questa non viene resettata
        if (this.power > 0) this.lastPower = this.power;
    }

    public double getLastPower() {
        return lastPower;
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
