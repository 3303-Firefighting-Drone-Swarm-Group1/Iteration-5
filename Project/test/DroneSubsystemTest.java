import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.sql.Time;

public class DroneSubsystemTest {

    @Test
    public void testHandleRequest() throws Exception {

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof java.net.BindException) {
            } else {
                throwable.printStackTrace();
            }
        });

        // Obtain a port for the Scheduler.
        ServerSocket schedSocket = new ServerSocket(0);
        int schedulerPort = schedSocket.getLocalPort();
        schedSocket.close();

        // Start the Scheduler on that port.
        new Scheduler(schedulerPort);

        // Allow the scheduler time to start listening on the port.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }

        // Obtain another port for the Drone.
        ServerSocket droneSocket = new ServerSocket(0);
        int dronePort = droneSocket.getLocalPort();
        droneSocket.close();

        // Create the DroneSubsystem using that port.
        DroneSubsystem drone = new DroneSubsystem("localhost", schedulerPort, dronePort);

        
        Field schedulerClientField = DroneSubsystem.class.getDeclaredField("schedulerClient");
        schedulerClientField.setAccessible(true);
        schedulerClientField.set(drone, new RPCClient("localhost", schedulerPort) {
            @Override
            public Object sendRequest(Object request) {
                // If a join message is sent, return null.
                if (request instanceof String && ((String) request).startsWith("join:")) {
                    return null;
                }
                // Otherwise, echo the request.
                return request;
            }
        });

        // Create a sample IncidentMessage with LOW severity.
        IncidentMessage incident = new IncidentMessage(
                Incident.Severity.LOW,
                new Point(1, 1),
                new Point(2, 2),
                new Time(((5 * 60 + 5) * 60 + 5) * 1000).getTime(),
                Incident.Type.FIRE_DETECTED,
                Incident.Fault.NONE
        );

        // Create a sample IncidentMessage with LOW severity.
        IncidentMessage fault1 = new IncidentMessage(
                Incident.Severity.LOW,
                new Point(1, 1),
                new Point(2, 2),
                new Time(((5 * 60 + 5) * 60 + 5) * 1000).getTime(),
                Incident.Type.FIRE_DETECTED,
                Incident.Fault.DRONE_STUCK
        );

        // Create a sample IncidentMessage with LOW severity.
        IncidentMessage fault2 = new IncidentMessage(
                Incident.Severity.LOW,
                new Point(1, 1),
                new Point(2, 2),
                new Time(((5 * 60 + 5) * 60 + 5) * 1000).getTime(),
                Incident.Type.FIRE_DETECTED,
                Incident.Fault.NOZZLE_JAMMED
        );

        // Create a sample IncidentMessage with LOW severity.
        IncidentMessage fault3 = new IncidentMessage(
                Incident.Severity.LOW,
                new Point(1, 1),
                new Point(2, 2),
                new Time(((5 * 60 + 5) * 60 + 5) * 1000).getTime(),
                Incident.Type.FIRE_DETECTED,
                Incident.Fault.PACKET_LOSS
        );

        // Invoke handleRequest on the drone.
        Object response = drone.handleRequest(incident);

        // Calculate the expected response time (in milliseconds).
        double distance = Math.sqrt(Math.pow(2, 2) + Math.pow(2, 2)); // sqrt(8)
        double requiredLiquid = 10;
        double sizeOfTank = 12;
        double numReturnTrips = Math.ceil(requiredLiquid / sizeOfTank);
        double speed = 25;
        double openCloseNozzle = 1;
        double timeToEmptyTank = 2;
        double expectedTimeTaken =
                2 * (distance / speed) * numReturnTrips
                        + openCloseNozzle
                        + timeToEmptyTank * (requiredLiquid / sizeOfTank);
        double expectedMillis = expectedTimeTaken * 1000;

        // Compare with a small floating-point tolerance.
        assertEquals(expectedMillis, (Double) response, 1e-2);

        //Checking if fault Detected, it should return a negative time.
        assertTrue(0 > drone.processIncident(fault1));
        assertTrue(0 > drone.processIncident(fault2));
        assertTrue(0 > drone.processIncident(fault3));
    }
}
