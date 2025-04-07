import org.junit.Test;
import static org.junit.Assert.*;

public class ZoneTest {
    @Test
    public void zoneTest(){
        Zone zone = new Zone(1, 1, 5, 5);
        assertEquals(5, zone.getClosestPoint(69, 4).getX(), 1e-2);
        assertEquals(4, zone.getClosestPoint(69, 4).getY(), 1e-2);
    }
}
