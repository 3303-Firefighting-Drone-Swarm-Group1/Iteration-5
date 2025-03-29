import static java.lang.Math.ceil;
import static java.lang.Math.incrementExact;
import static java.lang.Math.sqrt;
import java.sql.Time;

/***
 * Drone subsystem with a state machine to manage firefighting operations.
 * Fault handling added for iteration 4 with detailed output.
 */
public class DroneSubsystem {
    private String schedulerHost;
    private int schedulerPort;
    private RPCClient schedulerClient;
    private int dronePort; // Identifier for this drone

    private enum DroneState {IDLE, EN_ROUTE, DROPPING_AGENT, RETURNING_TO_BASE, FAULTED}
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
        this.schedulerHost = schedulerHost;
        this.schedulerPort = schedulerPort;
        this.dronePort = dronePort; // store drone port
        this.state = DroneState.IDLE;
        new Thread(new RPCServer(dronePort, this)).start(); // Start DroneSubsystem's RPC server
        this.schedulerClient = new RPCClient(schedulerHost, schedulerPort);
        schedulerClient.sendRequest("join:" + dronePort);
    }

    public Object handleRequest(Object request) {
        if (request instanceof IncidentMessage) {
            currentJobDetails = (IncidentMessage) request;
            System.out.println("\nDrone on port " + dronePort + " received incident: " + currentJobDetails.getType());
            double responseTime = processIncident(currentJobDetails);
            return responseTime;
        }
        return null;
    }

    public double processIncident(IncidentMessage incident) {
        state = DroneState.EN_ROUTE;
        int avgX = (currentJobDetails.getStartX() + currentJobDetails.getEndX()) / 2;
        int avgY = (currentJobDetails.getStartY() + currentJobDetails.getEndY()) / 2;
        System.out.println("Drone on port " + dronePort + " en route to fire at X,Y " + avgX + "," + avgY);

        // Process fault conditions
        System.out.println(incident.getFault().toString());
        extinguishFire();
        switch (incident.getFault()) {
            case DRONE_STUCK:
                System.out.println("Drone on port " + dronePort + "'s bay doors are stuck, shutting down drone.");
                //schedulerClient.sendRequest("fault:" + dronePort + ":DRONE_STUCK");
                state = DroneState.FAULTED;
                timeTaken = -(distance / speed) * numReturnTrips;
                break;
            case NOZZLE_JAMMED:
                System.out.println("Drone on port " + dronePort + "'s nozzles are jammed, shutting down drone.");
                //schedulerClient.sendRequest("fault:" + dronePort + ":NOZZLE_JAMMED");
                state = DroneState.FAULTED;
                timeTaken = -(distance / speed) * numReturnTrips;
                break;
            case PACKET_LOSS:
                System.out.println("Due to PACKET_LOSS, no completion packet was sent. Scheduler will eventually reassign this incident.");
                sendPacket = false;
                timeTaken = -(distance / speed) * numReturnTrips;
                break;
            case NONE:
                // No fault; continue normally.
                break;
        }

        return timeTaken * 1000; // Return job completion time in milliseconds.
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
    }

    private void returnToBase() {
        state = DroneState.IDLE;
    }

    private void droneCalculations() {
        distance = Math.sqrt(Math.pow(currentJobDetails.getEndX(), 2) + Math.pow(currentJobDetails.getEndY(), 2));
        timeTaken = 2 * (distance / speed) * numReturnTrips + openCloseNozzle +
                timeToEmptyTank * (requiredLiquid / SIZE_OF_TANK);
        System.out.println("Job severity: " + currentJobDetails.getSeverity());
        System.out.println("Job Completion Time: " + timeTaken);
    }
}
