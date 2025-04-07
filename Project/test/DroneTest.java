import org.junit.Test;
import static org.junit.Assert.*;

public class DroneTest {
    
    @Test 
    public void testDrone() {
        RPCClient client = new RPCClient("localhost", 4012);

        int x = 50;
        int y = 51;
        int vX = 52;
        int vY = 53;
        DroneSubsystem.DroneState state = DroneSubsystem.DroneState.DROPPING_AGENT;

        Drone drone = new Drone(client);

        // test values that are set on creation
        assertEquals(drone.getPort(), 4012);
        assertEquals(drone.getX(), 0);
        assertEquals(drone.getY(), 0);
        assertEquals(drone.getVX(), 0);
        assertEquals(drone.getVY(), 0);
        assertEquals(drone.getState(), DroneSubsystem.DroneState.IDLE);

        // test sending a request with a timeout, should return null because no
        // server is available
        assertEquals(drone.sendRequest("test", 2000), null);
        
        drone.setLocation(x, y);
        drone.setVelocity(vX, vY);
        drone.setState(state);
        
        // test values after changing them
        assertEquals(drone.getPort(), 4012);
        assertEquals(drone.getX(), 50);
        assertEquals(drone.getY(), 51);
        assertEquals(drone.getVX(), 52);
        assertEquals(drone.getVY(), 53);
        assertEquals(drone.getState(), DroneSubsystem.DroneState.DROPPING_AGENT);
    }
}
