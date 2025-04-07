import org.junit.Test;
import static org.junit.Assert.*;

public class PointTest {
    @Test 
    public void testPoint() {
        double x = 3.0;
        double y = 4.0;

        Point p = new Point(x, y);

        assertEquals(p.getX(), 3.0, 1e-2);
        assertEquals(p.getY(), 4.0, 1e-2);
        assertEquals(p.getIntY(), 4);
        assertEquals(Point.distance(0.0, 0.0, x, y), 5.0, 1e-2);
    }
}
