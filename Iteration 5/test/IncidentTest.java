import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.Time;

public class IncidentTest {
    @Test
    public void incidentTest() {
        Incident incident = new Incident(5, 5, 5, 100, Incident.Severity.LOW, Incident.Type.FIRE_DETECTED, Incident.Fault.NONE);

        assertEquals(Incident.Severity.LOW, incident.getSeverity());
        assertEquals(Incident.Type.FIRE_DETECTED, incident.getType());
        assertEquals(100, incident.getID());
        assertEquals(new Time(((5 * 60 + 5) * 60 + 5) * 1000), incident.getTime());
    }
}
