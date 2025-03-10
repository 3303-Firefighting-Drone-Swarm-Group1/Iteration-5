/**
 * A test class for the DroneSubsystem class.
 * @author Lucas Warburton (101276823)
 */

 import org.junit.jupiter.api.Test;

 import java.awt.*;
 import java.sql.Time;
 
 import static org.junit.Assert.*;
 
 public class DroneSubsystemTest {
 
     @Test
     public void droneSubsystemTest(){
         Box fireSendBox = new Box();
         Box fireReceiveBox = new Box();
         Box droneSendBox = new Box();
         Box droneReceiveBox = new Box();
 
         Thread droneThread = new Thread(new DroneSubsystem(new Scheduler(fireSendBox, droneSendBox, fireReceiveBox, droneReceiveBox), droneReceiveBox, droneSendBox));
         droneThread.start();
 
         IncidentMessage testMessage = new IncidentMessage(Incident.Severity.LOW, new Point(1, 1), new Point(2, 2), new Time(((5* 60 + 5)* 60 + 5) * 1000), Incident.Type.FIRE_DETECTED);
         droneSendBox.put(testMessage);

         assertTrue((boolean)droneReceiveBox.get());
     }
 }