public class Drone {
    private RPCClient client;
    private DroneSubsystem.DroneState state;
    private double x, y;
    private double vX, vY;

    
    public Drone(RPCClient client){
        this.client = client;
        state = DroneSubsystem.DroneState.IDLE;
        x = 0;
        y = 0;
        vX = 0;
        vY = 0;
    }

    public void setState(DroneSubsystem.DroneState state){
        this.state = state;
    }

    public void setVelocity(double x, double y){
        vX = x;
        vY = y;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public double getVX(){
        return vX;
    }

    public double getVY(){
        return vY;
    }

    public void setLocation(double x, double y){
        this.x = x;
        this.y = y;
    }

    public DroneSubsystem.DroneState getState(){
        return state;
    }

    public Object sendRequest(Object request){
        return client.sendRequest(request);
    }

    public int getPort(){
        return client.getPort();
    }
}
