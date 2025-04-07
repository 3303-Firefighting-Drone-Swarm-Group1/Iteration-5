/**
 * Entity class used to represent a fire
 */
public class Fire {
    private double water; //in liters
    private double x, y;
    private Incident.Severity severity;
    private Incident.Fault fault;

    public Fire(double x, double y, Incident.Severity severity, Incident.Fault fault){
        this.x = x;
        this.y = y;
        this.severity = severity;
        this.fault = fault;
        switch (severity){
            case LOW:
                water = 10;
                break;
            case MODERATE:
                water = 20;
                break;
            case HIGH:
            default:
                water = 30;
                break;
        }
    }

    /**
     * Gets the amount of water needed to put out the fire
     * @return The amount of water needed in L
     */
    public double getWater(){
        return water;
    }

    /**
     * Puts water on the fire
     * @param amount The amount of water added
     */
    public void putWater(double amount){
        water -= amount;
    }

    /**
     * Gets the x coordinate of the fire
     * @return the x coordinate
     */
    public double getX(){
        return x;
    }

    /**
     * Gets the y coordinate of the fire
     * @return the y coordinate
     */
    public double getY(){
        return y;
    }

    /**
     * Gets the severity of the fire
     * @return the severity
     */
    public Incident.Severity getSeverity(){
        return severity;
    }

    /**
     * Gets the distance of the fire from the origin
     * @return the distance from the origin
     */
    public double getDistance(){
        return Point.distance(x, y, 0, 0);
    }

    /**
     * Gets the fault status of the fire
     * @return the fault
     */
    public Incident.Fault getFault(){
        return fault;
    }

    /**
     * Clears the fault from the fire
     */
    public void clearFault(){
        fault = Incident.Fault.NONE;
    }
}
