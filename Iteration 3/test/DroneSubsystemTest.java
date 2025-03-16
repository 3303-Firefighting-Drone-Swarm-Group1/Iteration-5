import org.junit.Test;
import static org.junit.Assert.*;
import java.awt.Point;
import java.sql.Time;

public class DroneSubsystemTest {

    @Test
    public void testHandleRequest() {
        // Start a Scheduler on port 5000 (ensure this port is free)
        int schedulerPort = 5000;
        new Thread(new Scheduler(schedulerPort)).start();

        // Give the server time to start
        try { Thread.sleep(100); } catch (InterruptedException e) { }

        // Create a DroneSubsystem instance on port 6000
        DroneSubsystem drone = new DroneSubsystem("localhost", schedulerPort, 6000);

        // Create a sample IncidentMessage with LOW severity
        IncidentMessage incident = new IncidentMessage(
                Incident.Severity.LOW,
                new Point(1, 1),
                new Point(2, 2),
                new Time(((5 * 60 + 5) * 60 + 5) * 1000),
                Incident.Type.FIRE_DETECTED
        );

        // Invoke the handleRequest method
        Object response = drone.handleRequest(incident);

        // Calculate the expected time taken.
        // Calculation based on DroneSubsystem.droneCalculations():
        // distance = sqrt(2^2 + 2^2) = sqrt(8)
        // For LOW severity: requiredLiquid = 10, SIZE_OF_TANK = 12, so numReturnTrips = ceil(10/12)=1.
        // speed = 25, openCloseNozzle = 1, and timeToEmptyTank is (long)2.4 = 2.
        // expected timeTaken = 2*(distance/25)*1 + 1 + 2*(10/12)
        double distance = Math.sqrt(Math.pow(2, 2) + Math.pow(2, 2)); // sqrt(8)
        double requiredLiquid = 10;
        double sizeOfTank = 12;
        double numReturnTrips = Math.ceil(requiredLiquid / sizeOfTank);
        double speed = 25;
        double openCloseNozzle = 1;
        double timeToEmptyTank = 2; // (long)2.4 becomes 2
        double expectedTimeTaken = 2 * (distance / speed) * numReturnTrips + openCloseNozzle + timeToEmptyTank * (requiredLiquid / sizeOfTank);
        double expectedMillis = expectedTimeTaken * 1000;

        // Allow a small tolerance for floating point arithmetic.
        assertEquals(expectedMillis, (Double)response, 1e-2);
    }
}
