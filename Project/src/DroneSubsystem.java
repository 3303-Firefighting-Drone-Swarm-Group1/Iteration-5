/***
 * Drone subsystem with a state machine to manage firefighting operations.
 * Fault handling added for iteration 4 with detailed output.
 */
public class DroneSubsystem {
    private RPCClient schedulerClient;
    private int dronePort; // Identifier for this drone

    public enum DroneState {IDLE, EN_ROUTE, DROPPING_AGENT, RETURNING_TO_BASE, FAULTED}
    private DroneState state;
    private TaskMessage currentJobDetails;

    public static final double SIZE_OF_TANK = 12;
    private long timeToEmptyTank = (long) 2400; // 2400 ms
    private long openCloseNozzle = 1000;
    private double speed = 0.025; // 25 m/ms
    private double totalDistance = 0;

    public DroneSubsystem(String schedulerHost, int schedulerPort, int dronePort) {
        this.dronePort = dronePort; // store drone port
        this.state = DroneState.IDLE;
        new Thread(new RPCServer(dronePort, this)).start(); // Start DroneSubsystem's RPC server
        this.schedulerClient = new RPCClient(schedulerHost, schedulerPort);
        schedulerClient.sendRequest("join:" + dronePort);
    }

    public Object handleRequest(Object request) {
        if (request instanceof TaskMessage) {
            currentJobDetails = (TaskMessage) request;
            long responseTime = processIncident(currentJobDetails);
            return responseTime;
        }
        return null;
    }

    public long processIncident(TaskMessage incident) {
        switch (state){
            case EN_ROUTE:
                return enRoute(incident);
            case DROPPING_AGENT:
                return droppingAgent(incident);
            case IDLE:
                return idle(incident);
            case RETURNING_TO_BASE:
                return returningToBase(incident);
            case FAULTED:
                return faulted(incident);
            default:
                System.out.println("states broke");
                return 0;
        }
    }

    private long enRoute(TaskMessage incident){
        switch (incident.getFault()){
            case DRONE_STUCK:
                state = DroneState.FAULTED;
                return -420;
            case NOZZLE_JAMMED:
                state = DroneState.FAULTED;
                return -69;
            case PACKET_LOSS:
                state = DroneState.FAULTED;
                return -69;
            default:
                state = DroneState.DROPPING_AGENT;
                long responseTime = (long) (timeToEmptyTank * incident.getWater() / SIZE_OF_TANK + openCloseNozzle);
                System.out.println("Fire extinguished in " + responseTime + "ms");
                return responseTime;
        }
    }

    private long droppingAgent(TaskMessage incident){
        double distance = Math.sqrt(Math.pow(incident.getFireLocation().getX(), 2) + Math.pow(incident.getFireLocation().getY(), 2));
        /*
        if (dronePort == 6002) {
            try {
                Thread.sleep(4000);
            } catch (Exception e) {}
        }
        */
        state = DroneState.RETURNING_TO_BASE;
        totalDistance += distance;
        return (long) (distance / speed);
    }

    private long idle(TaskMessage incident){
        double distance = Math.sqrt(Math.pow(incident.getFireLocation().getX(), 2) + Math.pow(incident.getFireLocation().getY(), 2));
        System.out.println("Drone " + dronePort + " responding to fire at zone " +
                incident.getFireLocation().getX() + "," + incident.getFireLocation().getY() +
                " (distance: " + String.format("%.2f", distance) + ")");
        totalDistance += distance;
        state = DroneState.EN_ROUTE;
        return (long) (distance / speed);
    }

    private long returningToBase(TaskMessage incident){
        state = DroneState.IDLE;
        return 0;
    }

    private long faulted(TaskMessage incident){
        double distance = Math.sqrt(Math.pow(incident.getFireLocation().getX(), 2) + Math.pow(incident.getFireLocation().getY(), 2));
        totalDistance += distance;
        state = DroneState.RETURNING_TO_BASE;
        return (long) (distance / speed);
    }

    public double getTotalDistance() {
        return totalDistance;
    }
}
