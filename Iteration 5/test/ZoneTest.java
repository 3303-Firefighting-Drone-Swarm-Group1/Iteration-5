import org.junit.Test;
import java.awt.Point;
import static org.junit.Assert.*;

public class ZoneTest {
    @Test
    public void zoneTest(){
        Zone zone = new Zone(1, 1, 5, 5);
        assertEquals(new Point(5, 4), zone.getClosestPoint(69, 4));
    }
}
