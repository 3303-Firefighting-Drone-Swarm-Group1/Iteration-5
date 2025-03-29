import org.junit.Test;
import static org.junit.Assert.*;
import java.awt.Point;
import java.sql.Time;

public class IncidentMessageTest {
    @Test
    public void incidentMessageTest(){
        IncidentMessage incidentMessage = new IncidentMessage(
                Incident.Severity.LOW,
                new Point(1, 1),
                new Point(2, 2),
                new Time(((5 * 60 + 5) * 60 + 5) * 1000),
                Incident.Type.FIRE_DETECTED
        );

        assertEquals(Incident.Severity.LOW, incidentMessage.getSeverity());
        assertEquals(Incident.Type.FIRE_DETECTED, incidentMessage.getType());
        assertEquals(new Time(((5 * 60 + 5) * 60 + 5) * 1000), incidentMessage.getTime());
        // Expected value first, then actual.
        assertEquals(new Point(2, 1), incidentMessage.getClosestPoint(6, 1));
    }
}
