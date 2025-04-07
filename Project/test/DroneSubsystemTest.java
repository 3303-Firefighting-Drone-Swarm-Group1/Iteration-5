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
        Scheduler s = new Scheduler(schedulerPort);

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

        Drone d = new Drone(null);

        
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
                Incident.Severity.HIGH,
                new Point(1, 1),
                new Point(2, 2),
                new Time(((5 * 60 + 5) * 60 + 5) * 1000).getTime(),
                Incident.Type.FIRE_DETECTED,
                Incident.Fault.NONE
        );

        Fire f = new Fire((incident.getStartX() + incident.getEndX()) / 2.0, (incident.getStartY() + incident.getEndY()) / 2.0, incident.getSeverity(), incident.getFault());

        TaskMessage tm = s.makeTaskMessage(f, d);


        // Calculate the expected response time (in milliseconds).
        double distance = Math.sqrt(Math.pow(1.5, 2) + Math.pow(1.5, 2)); // sqrt(8)
        double requiredLiquid = 30;
        double sizeOfTank = 12;
        double numReturnTrips = Math.ceil(requiredLiquid / sizeOfTank);
        double speed = 0.025;
        double openCloseNozzle = 1000;
        double timeToEmptyTank = 2400;
        double expectedTimeTaken =
                2 * (distance / speed) * numReturnTrips
                        + openCloseNozzle
                        + timeToEmptyTank * (requiredLiquid / sizeOfTank);
        double expectedMillis = expectedTimeTaken * 1000;

        //Checking if fault Detected, it should return a negative time.
        assertEquals(Math.floor(distance / speed), drone.processIncident(tm), 1e-2);
        assertEquals(openCloseNozzle + timeToEmptyTank, drone.processIncident(tm), 1e-2);
    }
}
