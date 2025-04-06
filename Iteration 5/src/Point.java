import java.io.Serializable;

public class Point implements Serializable{
    private double x;
    private double y;


    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX(){
        return x;
    }




    public double getY(){
        //System.out.println("Point x: " + x + " y: " + y);
        return y;
    }


    public int getIntY() {
        int y = (int) this.y;
        return y;
    }

    public static double distance(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow((y2 - y1), 2) + Math.pow((x2 - x1), 2));
    }
}
