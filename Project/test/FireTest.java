import org.junit.Test;
import static org.junit.Assert.*;

public class FireTest {
    
    @Test 
    public void testFire() {
        double water = 10.0;
        double x = 25.0;
        double y = 26.0;

        double distance = Math.sqrt(x * x + y * y);
        Incident.Severity severity = Incident.Severity.HIGH;
        Incident.Fault fault = Incident.Fault.DRONE_STUCK;

        Fire fire = new Fire(x, y, severity, fault);

        // test values that are set on creation

        assertEquals(fire.getX(), 25.0, 1e-2);
        assertEquals(fire.getY(), 26.0, 1e-2);
        assertEquals(fire.getDistance(), distance, 1e-2);
        assertEquals(fire.getSeverity(), severity);

        assertEquals(fire.getWater(), 30, 1e-2);

        // test putting water on a fire

        fire.putWater(water);
        assertEquals(fire.getWater(), 20, 1e-2);

        fire.putWater(water);
        assertEquals(fire.getWater(), 10, 1e-2);

        fire.putWater(water);
        assertEquals(fire.getWater(), 0, 1e-2);

        // fault should be set to none after cleared
        fire.clearFault();
        assertEquals(fire.getFault(), Incident.Fault.NONE);

    }
}
