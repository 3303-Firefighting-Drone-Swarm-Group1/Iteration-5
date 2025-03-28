import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

import java.net.InetAddress;
import java.sql.Time;

/***
 * Drone subsystem with a state machine to manage firefighting operations.
 * @author ahmedbabar, modified by Abdulaziz Alsibakhi
 */
public class DroneSubsystem {
    private String schedulerHost;
    private int schedulerPort;
    private RPCClient schedulerClient;

    private enum DroneState {IDLE, EN_ROUTE, DROPPING_AGENT, RETURNING_TO_BASE}

    private DroneState state;
    private IncidentMessage currentJobDetails;


    private final double SIZE_OF_TANK = 12;
    private double requiredLiquid;
    private long timeToEmptyTank = (long) 2.4; // 2.4 seconds
    private long openCloseNozzle = 1;
    private double speed = 25; // 25 m/s

    private double timeTaken;
    private double distance;
    private double numReturnTrips;

    private boolean sendPacket = true;

    public DroneSubsystem(String schedulerHost, int schedulerPort, int dronePort) {
        this.state = DroneState.IDLE;
        new Thread(new RPCServer(dronePort, this)).start(); // Start DroneSubsystem's RPC server
        this.schedulerClient = new RPCClient(schedulerHost, schedulerPort);
        schedulerClient.sendRequest("join:" + dronePort);
    }

    public Object handleRequest(Object request) {
        if (request instanceof IncidentMessage) {
            currentJobDetails = (IncidentMessage) request;
            System.out.println("\nDrone received incident: " + currentJobDetails.getType());
            processIncident(currentJobDetails);

            return timeTaken * 1000; // Notify scheduler of job completion
        }
        return null;
    }

    private void processIncident(IncidentMessage incident) {
        state = DroneState.EN_ROUTE;
        System.out.println("Drone en route to fire at X,Y " + (currentJobDetails.getStartX() + currentJobDetails.getEndX())/2 + "," + (currentJobDetails.getStartY() + currentJobDetails.getEndY())/2);

        switch (incident.getFault()) {
            case PACKET_LOSS:
                sendPacket = false;
                break;
            case DRONE_STUCK:
                System.out.println("Drone on port " + schedulerPort + "'s bay doors are stuck, shutting down drone.");
                // Able to send message to scheduler if necessary.
                return;
            case NOZZLE_JAMMED:
                System.out.println("Drone on port " + schedulerPort + "'s nozzles are jammed, shutting down drone.");
                // Able to send message to scheduler if necessary.
                return;
            default:
                break;
        }

        extinguishFire();
    }

    private void extinguishFire() {
        determineLiquidNeeded();
        droneCalculations();
        notifyJobCompletion();
        state = DroneState.RETURNING_TO_BASE;
        returnToBase();
    }

    private void determineLiquidNeeded() {
        switch (currentJobDetails.getSeverity()) {
            case LOW:
                requiredLiquid = 10;
                break;
            case MODERATE:
                requiredLiquid = 20;
                break;
            case HIGH:
                requiredLiquid = 30;
                break;
        }
        numReturnTrips = Math.ceil(requiredLiquid / SIZE_OF_TANK);
    }

    private void notifyJobCompletion() {
        if (sendPacket) 
            schedulerClient.sendRequest("completed:" + schedulerPort);
    }

    private void returnToBase() {
        state = DroneState.IDLE;
    }

    private void droneCalculations() {
        distance = Math.sqrt(Math.pow(currentJobDetails.getEndX(), 2) + Math.pow(currentJobDetails.getEndY(), 2));
        timeTaken = 2 * (distance / speed) * numReturnTrips + openCloseNozzle + timeToEmptyTank * (requiredLiquid / SIZE_OF_TANK);
        System.out.println("Job severity: " + currentJobDetails.getSeverity());
        System.out.println("Job Completion Time: " + timeTaken);
    }
}
