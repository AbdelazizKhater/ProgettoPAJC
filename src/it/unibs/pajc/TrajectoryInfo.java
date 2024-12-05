package it.unibs.pajc;

public class TrajectoryInfo {
    //public final double impactX;
    //public final double impactY;
    public final double startX;
    public final double startY;
    public final double directionX;
    public final double directionY;

    public TrajectoryInfo(double startX, double startY,
            double directionX, double directionY) {
        //this.impactX = impactX;
        //this.impactY = impactY;
        this.startX = startX;
        this.startY = startY;
        this.directionX = directionX;
        this.directionY = directionY;

    }
}
