
/**
 * An entity object used to represent a task for a drone
 */
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

    /**
     * Gets the water to be put on the fire
     * @return The amount of water in L
     */
    public double getWater(){
        return water;
    }

    /**
     * Gets the location of the fire
     * @return A point representing the location of the fire
     */
    public Point getFireLocation(){
        return fireLocation;
    }

    /**
     * Gets the location of the drone
     * @return A point representing the location of the drone
     */
    public Point getDroneLocation(){
        return droneLocation;
    }

    /**
     * Gets the fault associated with the fire
     * @return The fault
     */
    public Incident.Fault getFault(){
        return fault;
    }
}

