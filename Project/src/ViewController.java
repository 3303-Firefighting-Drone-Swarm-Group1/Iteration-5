/**
 * The controller for the GUI showing the Fire system.
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

public class ViewController implements ActionListener {
    public Scheduler scheduler;
    public FireIncidentSubsystem fireIncidentSubsystem;
    public HashMap availableZones;
    //private String objectName = "ViewController";
    ArrayList<Fire> fireList;
    ArrayList<Drone> droneList;

    private static final String SCHEDULER_HOST = "localhost";
    private static final int SCHEDULER_PORT = 5030; // Port for Scheduler's RPC server


    public ViewController(Scheduler scheduler, FireIncidentSubsystem fireIncidentSubsystem) {
        this.scheduler = scheduler;
        this.fireIncidentSubsystem = fireIncidentSubsystem;
        initialize();
    }


    /**
     * Gets the info needed for the GUI
     */
    public void initialize() {
        this.availableZones = getZoneData();
        this.fireList = getFireIncidents();
        this.droneList = getDrones();
        Timer timer = new Timer(15, this);
        timer.start();
    }

    /**
     * Gets the zone information
     * @return The zone information
     */
    public HashMap<Integer, Zone> getZoneData() {
        return fireIncidentSubsystem.zones;
    }

    /**
     * Gets the fires
     * @return the fires
     */
    public ArrayList<Fire> getFireIncidents() {
        return scheduler.getMap().getFires();
    }

    /**
     * Gets the drones
     * @return the drones
     */
    public ArrayList<Drone> getDrones() {
        return scheduler.getMap().getDrones();
    }



    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

//        private Scheduler scheduler;
//        private FireIncidentSubsystem fireIncidentSubsystem;

        Scheduler scheduler1 = new Scheduler(SCHEDULER_PORT);
        FireIncidentSubsystem fireSystem = new FireIncidentSubsystem(
                "Project/input/sample_zone_file.csv", "Project/input/Sample_event_file.csv",
                SCHEDULER_HOST,
                SCHEDULER_PORT
        );

        DroneSubsystem drone1 = new DroneSubsystem(SCHEDULER_HOST, SCHEDULER_PORT, 6000);
        DroneSubsystem drone2 = new DroneSubsystem(SCHEDULER_HOST, SCHEDULER_PORT, 6001);
        DroneSubsystem drone3 = new DroneSubsystem(SCHEDULER_HOST, SCHEDULER_PORT, 6002);
        DroneSubsystem drone4 = new DroneSubsystem(SCHEDULER_HOST, SCHEDULER_PORT, 6003);
        DroneSubsystem drone5 = new DroneSubsystem(SCHEDULER_HOST, SCHEDULER_PORT, 6004);
        DroneSubsystem drone6 = new DroneSubsystem(SCHEDULER_HOST, SCHEDULER_PORT, 6005);
        DroneSubsystem drone7 = new DroneSubsystem(SCHEDULER_HOST, SCHEDULER_PORT, 6006);
        DroneSubsystem drone8 = new DroneSubsystem(SCHEDULER_HOST, SCHEDULER_PORT, 6007);
        DroneSubsystem drone9 = new DroneSubsystem(SCHEDULER_HOST, SCHEDULER_PORT, 6008);
        DroneSubsystem drone10 = new DroneSubsystem(SCHEDULER_HOST, SCHEDULER_PORT, 6009);
        DroneSubsystem drone11 = new DroneSubsystem(SCHEDULER_HOST, SCHEDULER_PORT, 6010);

        try {
            Thread.sleep(10); // let threads initialize
        } catch (Exception e) {}

        new Thread(fireSystem).start();

        ViewController viewController = new ViewController(scheduler1, fireSystem);
        EventUI eventUI = new EventUI(viewController);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;


        try {
            Thread.sleep(10000);
        } catch (Exception e) {}


        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
