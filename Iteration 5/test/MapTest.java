import org.junit.Test;

import static org.junit.Assert.*;

public class MapTest {
    
    @Test 
    public void testMap() {
        Fire fire = new Fire(300, 300, Incident.Severity.HIGH, Incident.Fault.NONE);
        RPCClient client = new RPCClient("localhost", 24525);
        Drone drone = new Drone(client);
        drone.setVelocity(1.0, 1.0);

        Map map = new Map();
        map.addDrone(drone);
        map.addFire(fire);

        // check if drones and fires are added
        assertEquals(map.getDrones().size(), 1);
        assertEquals(map.getFires().size(), 1);


        // drone shouldn't have moved
        assertEquals(map.getDrones().get(0).getX(), 0, 1e-2);
        assertEquals(map.getDrones().get(0).getY(), 0, 1e-2);

        map.updatePositions();

        // drone should have moved by 1 (velocities are 1 on both directions)
        assertEquals(map.getDrones().get(0).getX(), 1, 1e-2);
        assertEquals(map.getDrones().get(0).getY(), 1, 1e-2);

        fire.putWater(100000);

        map.updatePositions();

        // fire should have been removed
        assertEquals(map.getFires().size(), 0);

        // add and remove fire to make sure removal works
        map.addFire(fire);
        assertEquals(map.getFires().size(), 1);

        // remove drone by port and check if there are any drones
        map.removeFire(fire);
        map.removeDrone(24525);
        assertEquals(map.getFires().size(), 0);
        assertEquals(map.getDrones().size(), 0);
    }
}
