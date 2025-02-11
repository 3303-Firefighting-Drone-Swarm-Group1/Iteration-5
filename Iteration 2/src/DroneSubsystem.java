import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

/***
 * Drone subsystem with a state machine to manage firefighting operations.
 * @author ahmedbabar, modified by Abdulaziz Alsibakhi
 */
public class DroneSubsystem implements Runnable {
    private enum DroneState {
        IDLE, EN_ROUTE, DROPPING_AGENT, RETURNING_TO_BASE
    }

    private DroneState state;
    private Scheduler scheduler;
    private IncidentMessage currentJobDetails;

    private final double SIZE_OF_TANK = 12;
    private double requiredLiquid;
    private long timeToEmptyTank = (long) 2.4; // 2.4 seconds
    private long openCloseNozzle = 1;
    private double speed = 25; // 25 m/s

    private double timeTaken;
    private double distance;
    private double numReturnTrips;

    private Box sendBox, receiveBox;

    public DroneSubsystem(Scheduler scheduler, Box sendBox, Box receiveBox) {
        this.scheduler = scheduler;
        this.sendBox = sendBox;
        this.receiveBox = receiveBox;
        this.state = DroneState.IDLE;
    }

    /***
     * Runs the drone system, checking for jobs and executing them.
     */
    @Override
    public void run() {
        while (true) {
            if (state == DroneState.IDLE) {
                // Request a job
                jobDetails();
                if (currentJobDetails != null) {
                    state = DroneState.EN_ROUTE;
                    moveToFire();
                    extinguishFire();
                }
            }
        }
    }

    private void moveToFire() {
        System.out.println("Drone en route to fire at Zone " + currentJobDetails.getStartX());
        state = DroneState.DROPPING_AGENT;
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
        numReturnTrips = ceil(requiredLiquid / SIZE_OF_TANK);
    }

    private void jobDetails() {
        System.out.println("Drone requests job...");
        currentJobDetails = (IncidentMessage) receiveBox.get();
    }

    private void notifyJobCompletion() {
        System.out.println("Drone completed job. Notifying scheduler.");
        sendBox.put(true);
    }

    private void returnToBase() {
        System.out.println("Drone returning to base...");
        state = DroneState.IDLE;
    }

    private void droneCalculations() {
        distance = sqrt((currentJobDetails.getEndX())^2 + (currentJobDetails.getEndY())^2);
        timeTaken = 2 * (distance / speed) * numReturnTrips + openCloseNozzle + timeToEmptyTank * (requiredLiquid / SIZE_OF_TANK);
        System.out.println("Job severity: " + currentJobDetails.getSeverity());
        System.out.println("Job Completion Time: " + timeTaken);
    }
}
