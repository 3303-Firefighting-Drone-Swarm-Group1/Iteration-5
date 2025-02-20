/**
 * A test class for the Incident class.
 * @author Lucas Warburton (101276823)
 */

 import org.junit.Test;

 import java.sql.Time;
 
 import static org.junit.Assert.*;
 
 public class IncidentTest {
     @Test
     public void incidentTest() {
         Incident incident = new Incident(5, 5, 5, 100, Incident.Severity.LOW, Incident.Type.FIRE_DETECTED);
 
         assertEquals(Incident.Severity.LOW, incident.getSeverity());
         assertEquals(Incident.Type.FIRE_DETECTED, incident.getType());
         assertEquals(100, incident.getID());
         assertEquals(new Time(((5* 60 + 5)* 60 + 5) * 1000), incident.getTime());
     }
 }