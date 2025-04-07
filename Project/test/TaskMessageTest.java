import org.junit.Test;
import static org.junit.Assert.*;

public class TaskMessageTest {
    @Test 
    public void testTaskMessage() {
        double water = 42.0;
        Point fireLocation = new Point(2.0, 5.0);
        Point droneLocation = new Point(3.0, 6.0);
        Incident.Fault fault = Incident.Fault.DRONE_STUCK;

        TaskMessage tm = new TaskMessage(water, fireLocation, droneLocation, fault);

        assertEquals(tm.getWater(), 42.0, 1e-2);
        assertEquals(tm.getFireLocation(), fireLocation);
        assertEquals(tm.getDroneLocation(), droneLocation);
        assertEquals(tm.getFault(), Incident.Fault.DRONE_STUCK);

    }
}
