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

    public double getWater(){
        return water;
    }

    public void putWater(double amount){
        water -= amount;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public Incident.Severity getSeverity(){
        return severity;
    }

    public double getDistance(){
        return Point.distance(x, y, 0, 0);
    }

    public Incident.Fault getFault(){
        return fault;
    }

    public void clearFault(){
        fault = Incident.Fault.NONE;
    }
}
