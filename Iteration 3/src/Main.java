public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Firefighting Drone Swarm Simulation...");

        // Create communication channels
        Box fireToSchedulerBox = new Box();
        Box schedulerToFireBox = new Box();
        Box schedulerToDroneBox = new Box();
        Box droneToSchedulerBox = new Box();

        // Initialize the Scheduler
        Scheduler scheduler = new Scheduler(schedulerToFireBox, schedulerToDroneBox, fireToSchedulerBox, droneToSchedulerBox);

        // Paths to input files (ensure these files exist in the correct directory)
        String zoneFilePath = "input/sample_zone_file.csv";
        String eventFilePath = "input/Sample_event_file.csv";

        // Initialize Fire Incident Subsystem
        FireIncidentSubsystem fireSystem = new FireIncidentSubsystem(scheduler, zoneFilePath, eventFilePath, fireToSchedulerBox, schedulerToFireBox);

        // Initialize Drone Subsystem (Only one drone for now)
        DroneSubsystem drone = new DroneSubsystem(scheduler, droneToSchedulerBox, schedulerToDroneBox);

        // Start the system components as separate threads
        Thread schedulerThread = new Thread(scheduler);
        Thread fireThread = new Thread(fireSystem);
        Thread droneThread = new Thread(drone);

        schedulerThread.start();
        fireThread.start();
        droneThread.start();

        // Wait for system completion (Optional: Adjust time as needed)
        try {
            Thread.sleep(10000); // Let the system run for 10 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Shutdown system (Interrupt threads)
        fireThread.interrupt();
        droneThread.interrupt();
        schedulerThread.interrupt();

        try {
            fireThread.join();
            droneThread.join();
            schedulerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Simulation Complete.");
    }
}
