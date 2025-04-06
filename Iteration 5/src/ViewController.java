import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;

public class ViewController implements ActionListener {
    public Scheduler scheduler;
    public FireIncidentSubsystem fireIncidentSubsystem;
    public HashMap availableZones;
    //private String objectName = "ViewController";
    ArrayList<Fire> fireList;
    ArrayList<Fire> extinguishedFireList = new ArrayList<>();
    ArrayList<Drone> droneList;

    private static final String SCHEDULER_HOST = "localhost";
    private static final int SCHEDULER_PORT = 5010; // Port for Scheduler's RPC server


    public ViewController(Scheduler scheduler, FireIncidentSubsystem fireIncidentSubsystem) {
        this.scheduler = scheduler;
        this.fireIncidentSubsystem = fireIncidentSubsystem;
        initialize();
    }


    public void initialize() {
        this.availableZones = getZoneData();
        this.fireList = getFireIncidents();
        this.droneList = getDrones();
        Timer timer = new Timer(150, this);
        timer.start();
    }


    public void update() {
        //Recall Drones method to add new drones and location
        updateFires();
        updateDrones();
        //updateGUI
    }



    public HashMap<Integer, Zone> getZoneData() {
        return fireIncidentSubsystem.zones;
    }

    public ArrayList<Fire> getFireIncidents() {
        return scheduler.getMap().getFires();
    }

    public ArrayList<Drone> getDrones() {
        return scheduler.getMap().getDrones();
    }

    public void updateFires() {
        ArrayList<Fire> currentFires = getFireIncidents();
        ArrayList<Fire> firesToRemove = new ArrayList<>();

        for (Fire fire : fireList) {
            if (!currentFires.contains(fire)) {
                fireExtinguished(fire);
                firesToRemove.add(fire);
            }
        }
        fireList.removeAll(firesToRemove);
        fireList.clear();
        fireList.addAll(currentFires);
    }

    public void updateDrones(){
        this.droneList = getDrones();
    }

    //May need to change
    public void fireExtinguished(Fire fire) {
        extinguishedFireList.add(fire);
    }


    public static void main(String[] args) {
//        private Scheduler scheduler;
//        private FireIncidentSubsystem fireIncidentSubsystem;

        Scheduler scheduler1 = new Scheduler(SCHEDULER_PORT);
        FireIncidentSubsystem fireSystem = new FireIncidentSubsystem(
                "Iteration 5/input/sample_zone_file.csv", "Iteration 5/input/Sample_event_file.csv",
                SCHEDULER_HOST,
                SCHEDULER_PORT
        );

        new DroneSubsystem(SCHEDULER_HOST, SCHEDULER_PORT, 6000);
        new DroneSubsystem(SCHEDULER_HOST, SCHEDULER_PORT, 6001);
        new Thread(fireSystem).start();

        ViewController viewController = new ViewController(scheduler1, fireSystem);
        EventUI eventUI = new EventUI(viewController);
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        update();
    }
}
