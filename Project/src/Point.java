/**
 * An entity class used to represent a location
 */
import java.io.Serializable;

public class Point implements Serializable{
    private double x;
    private double y;


    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x coordinate
     * @return the x coordinate
     */
    public double getX(){
        return x;
    }

    /**
     * Gets the y coordinate
     * @return the y coordinate
     */
    public double getY(){
        return y;
    }

    /**
     * Gets the distance between 2 sets of coordinates
     * @param x1 the x coordinate of the first set
     * @param y1 the y coordinate of the first set
     * @param x2 the x coordinate of the second set
     * @param y2 the y coordinate of the second set
     * @return the distance
     */
    public static double distance(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow((y2 - y1), 2) + Math.pow((x2 - x1), 2));
    }
}
