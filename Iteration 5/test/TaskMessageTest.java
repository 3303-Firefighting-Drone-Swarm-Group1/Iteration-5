import org.junit.Test;
import static org.junit.Assert.*;

public class TaskMessageTest {
    @Test 
    public void testTaskMessage() {
        private double water = 42.0;
        private Point fireLocation = new Point(2.0, 5.0);
        private Point droneLocation = new Point(3.0, 6.0);
        private Incident.Fault fault = Incident.Fault.DRONE_STUCK;

        TaskMessage tm = new TaskMessage(water, fireLocation, droneLocation, fault);

        assertEquals(tm.getWater(), 42.0);
        assertEquals(tm.getFireLocation(), fireLocation);
        assertEquals(tm.getDroneLocation(), droneLocation);
        assertEquals(tm.getFault(), Incident.Fault.DRONE_STUCK);

    }
}
