
import java.io.Serializable;

public class TaskMessage implements Serializable{
    private double water; //in liters
    private Point fireLocation;
    private Point droneLocation;
    private Incident.Fault fault;

    public TaskMessage(double water, Point fireLocation, Point droneLocation, Incident.Fault fault){
        this.water = water;
        this.fireLocation = fireLocation;
        this.droneLocation = droneLocation;
        this.fault = fault;
    }

    public double getWater(){
        return water;
    }

    public Point getFireLocation(){
        return fireLocation;
    }

    public Point getDroneLocation(){
        return droneLocation;
    }

    public Incident.Fault getFault(){
        return fault;
    }
}

