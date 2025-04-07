/**
 * Entity object used to store a representation of a drone.
 */

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

    /**
     * Sets the state of the drone
     * @param state The new state
     */
    public void setState(DroneSubsystem.DroneState state){
        this.state = state;
    }

    /**
     * Sets the velocity of the drone in m/s
     * @param x the new x velocity
     * @param y the new y velocity
     */
    public void setVelocity(double x, double y){
        vX = x;
        vY = y;
    }

    /**
     * Gets the x coordinate of the drone
     * @return the x coordinate
     */
    public double getX(){
        return x;
    }

    /**
     * Gets the y coordinate of the drone
     * @return the y coordinate
     */
    public double getY(){
        return y;
    }

    /**
     * Gets the x velocity of the drone
     * @return the x velocity
     */
    public double getVX(){
        return vX;
    }

    /**
     * Gets the y velocity of the drone
     * @return the y velocity
     */
    public double getVY(){
        return vY;
    }

    /**
     * Sets the location of the drone
     * @param x the new x coordinate
     * @param y the new y coordinate
     */
    public void setLocation(double x, double y){
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the state of the drone
     * @return the state
     */
    public DroneSubsystem.DroneState getState(){
        return state;
    }

    /**
     * Sends an RPC message to the DroneSubsystem associated with this Drone
     * @param request A serializable object to be sent to the DroneSubsystem
     * @return The return message
     */
    public Object sendRequest(Object request){
        return client.sendRequest(request);
    }

    /**
     * Sends an RPC message to the DroneSubsystem associated with this Drone
     * @param request A serializable object to be sent to the DroneSubsystem
     * @param timeout The time allowed for the DroneSubsystem to respond
     * @return The return message
     */
    public Object sendRequest(Object request, int timeout){
        return client.sendRequest(request, timeout);
    }

    /**
     * Gets the port of the DroneSubsystem associated with the Drone
     * @return
     */
    public int getPort(){
        return client.getPort();
    }
}
