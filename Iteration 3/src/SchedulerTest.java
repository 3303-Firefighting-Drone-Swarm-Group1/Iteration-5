/**
 * A test class for the Scheduler class.
 * @author Lucas Warburton (101276823)
 */

 import org.junit.Test;
 import java.awt.*;
 import java.sql.Time;
 
 import static org.junit.Assert.*;
 
 public class SchedulerTest {
 
     @Test
     public void schedulerTest(){
         Box fireSendBox = new Box();
         Box fireReceiveBox = new Box();
         Box droneSendBox = new Box();
         Box droneReceiveBox = new Box();
 
         Thread schedulerThread = new Thread(new Scheduler(fireSendBox, droneSendBox, fireReceiveBox, droneReceiveBox));
         schedulerThread.start();
 
         IncidentMessage testMessage = new IncidentMessage(Incident.Severity.LOW, new Point(1, 1), new Point(2, 2), new Time(((5* 60 + 5)* 60 + 5) * 1000), Incident.Type.FIRE_DETECTED);
         fireReceiveBox.put(testMessage);
         assertEquals(testMessage, droneSendBox.get());
 
         droneReceiveBox.put(true);
         assertTrue((boolean)fireSendBox.get());
     }
 }
