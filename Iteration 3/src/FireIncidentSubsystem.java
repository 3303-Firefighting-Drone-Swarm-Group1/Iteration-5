import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Manages fire incidents and communicates with the scheduler.
 * Reads input files containing zone and incident data.
 * @author Lucas Warburton, modified by Abdulaziz Alsibakhi
 */
public class FireIncidentSubsystem implements Runnable {
    private Scheduler scheduler;
    private String zoneInput, eventInput;
    private Box sendBox, receiveBox;
    private int numCompleted;

    public FireIncidentSubsystem(Scheduler scheduler, String zoneInput, String eventInput, Box sendBox, Box receiveBox) {
        this.scheduler = scheduler;
        this.zoneInput = zoneInput;
        this.eventInput = eventInput;
        this.sendBox = sendBox;
        this.receiveBox = receiveBox;
        numCompleted = 0;
    }

    /**
     * Reads input files and transmits incidents to the scheduler.
     */
    public void run() {
        HashMap<Integer, Zone> zones;
        try {
            zones = readZones();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Zone file not found: " + e.getMessage());
        }

        ArrayList<Incident> incidents;
        try {
            incidents = readIncidents();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Incident file not found: " + e.getMessage());
        }

        System.out.printf("There are %d incidents loaded.\n", incidents.size());

        for (Incident incident : incidents) {
            sendBox.put(new IncidentMessage(incident.getSeverity(), zones.get(incident.getID()).getStart(),
                    zones.get(incident.getID()).getEnd(), incident.getTime(), incident.getType()));
            receiveBox.get(); // Wait for completion acknowledgment
            System.out.println("Fire Incident Received Job Completion Token.");
            numCompleted++;
        }
        sendBox.put(null); // Signal end of input
    }

    /**
     * Reads the zone input file.
     * @return a HashMap containing the information about each zone
     */
    private HashMap<Integer, Zone> readZones() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(zoneInput));
        HashMap<Integer, Zone> zones = new HashMap<>();
        sc.nextLine();
        while (sc.hasNextLine()){
            String[] line = sc.nextLine().trim().split(",");
            String[] start = line[1].split("();");
            String[] end = line[2].split("();");
            zones.put(
                    Integer.parseInt(line[0]),
                    new Zone(Integer.parseInt(start[0].substring(1)),
                            Integer.parseInt(start[1].substring(0,1)),
                            Integer.parseInt(end[0].substring(1)),
                            Integer.parseInt(end[1].substring(0, (end[1].length())-1))));
        }
        sc.close();
        return zones;
    }


    /**
     * Reads incident data from the input file.
     * @return ArrayList<Incident> containing incidents
     */
    private ArrayList<Incident> readIncidents() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(eventInput));
        ArrayList<Incident> incidents = new ArrayList<>();
        sc.nextLine(); // Skip header
        while (sc.hasNextLine()) {
            String[] line = sc.nextLine().trim().split(",");

            String[] time = line[0].split(":");

            Incident.Type type = line[2].equals("FIRE_DETECTED") ? Incident.Type.FIRE_DETECTED : Incident.Type.DRONE_REQUEST;

            Incident.Severity severity;
            switch (line[3]) {
                case "High":
                    severity = Incident.Severity.HIGH;
                    break;
                case "Moderate":
                    severity = Incident.Severity.MODERATE;
                    break;
                default:
                    severity = Incident.Severity.LOW;
                    break;
            }

            incidents.add(new Incident(
                    Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]),
                    Integer.parseInt(line[1]), severity, type));
        }
        sc.close();
        return incidents;
    }

    /**
     * Gets the number of incidents that were fully processed.
     * @return Number of processed incidents.
     */
    public int getNumCompleted() {
        return numCompleted;
    }
}
