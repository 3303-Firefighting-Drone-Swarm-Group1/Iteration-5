public class Main {

    // Define host and ports
    private static final String SCHEDULER_HOST = "localhost";
    private static final int SCHEDULER_PORT = 5000; // Port for Scheduler's RPC server
    private static final int DRONE_PORT = 6000;     // Port for DroneSubsystem's RPC server

    // Main method for starting the Scheduler
    public static void startScheduler() {
        Scheduler scheduler = new Scheduler(SCHEDULER_HOST, DRONE_PORT);
        new Thread(new RPCServer(SCHEDULER_PORT, scheduler)).start(); // Start Scheduler's RPC server
        System.out.println("Scheduler started on port " + SCHEDULER_PORT);
    }

    // Main method for starting the DroneSubsystem
    public static void startDroneSubsystem() {
        DroneSubsystem drone = new DroneSubsystem(SCHEDULER_HOST, SCHEDULER_PORT);
        new Thread(new RPCServer(DRONE_PORT, drone)).start(); // Start DroneSubsystem's RPC server
        System.out.println("DroneSubsystem started on port " + DRONE_PORT);
    }

    // Main method for starting the FireIncidentSubsystem
    public static void startFireIncidentSubsystem() {
        FireIncidentSubsystem fireSystem = new FireIncidentSubsystem(
                "Iteration 3/input/sample_zone_file.csv", "Iteration 3/input/Sample_event_file.csv",
                SCHEDULER_HOST,
                SCHEDULER_PORT
        );
        new Thread(fireSystem).start(); // Start FireIncidentSubsystem
        System.out.println("FireIncidentSubsystem started.");
    }

    // Main method (entry point)
    public static void main(String[] args) {
        // Start the Scheduler
        startScheduler();

        // Start the DroneSubsystem
        startDroneSubsystem();

        // Start the FireIncidentSubsystem
        startFireIncidentSubsystem();
    }
}