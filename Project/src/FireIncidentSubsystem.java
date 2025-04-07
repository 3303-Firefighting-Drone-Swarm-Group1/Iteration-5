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
    private String zoneInput, eventInput;
    private RPCClient schedulerClient;
    public HashMap<Integer, Zone> zones;

    public FireIncidentSubsystem(String zoneInput, String eventInput, String schedulerHost, int schedulerPort) {
        this.zoneInput = zoneInput;
        this.eventInput = eventInput;
        this.schedulerClient = new RPCClient(schedulerHost, schedulerPort);
    }

    @Override
    public void run() {
        HashMap<Integer, Zone> zones;
        try {
            zones = readZones();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Zone file not found: " + e.getMessage());
        }
        this.zones = zones;
        ArrayList<Incident> incidents;
        try {
            incidents = readIncidents();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Incident file not found: " + e.getMessage());
        }

        System.out.printf("\nThere are %d incidents loaded.\n", incidents.size());

        ArrayList<IncidentMessage> messages = new ArrayList<>();

        for (Incident incident : incidents) {
            IncidentMessage message = new IncidentMessage(incident.getSeverity(), zones.get(incident.getID()).getStart(),
                    zones.get(incident.getID()).getEnd(), incident.getTime(), incident.getType(), incident.getFault());


            messages.add(message);

        }
        System.out.println("FireIncidentSubsystem sent incident list.");
        schedulerClient.sendRequest(messages);
    }

    /**
     * Reads the Zone info from the zone input file
     * @return
     * @throws FileNotFoundException
     */
    private HashMap<Integer, Zone> readZones() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(zoneInput));
        HashMap<Integer, Zone> zones = new HashMap<>();
        sc.nextLine();
        while (sc.hasNextLine()) {
            String[] line = sc.nextLine().trim().split(",");
            String[] start = line[1].split("();");
            String[] end = line[2].split("();");
            String startx = start[0].replace("(", "");
            String starty = start[1].replace(")", "");
            String endx = end[0].replace("(", "");
            String endy = end[1].replace(")", "");
            //System.out.println(line[0] +
            //        " " + startx + " " + starty + " " + endx + " " + endy);
            zones.put(
                    Integer.parseInt(line[0]),
                    new Zone(Integer.parseInt(startx),
                            Integer.parseInt(starty),
                            Integer.parseInt(endx),
                            Integer.parseInt(endy)));
//            System.out.println(
//                      "First Value: " + Integer.parseInt(start[0].substring(1))
//                    + " Second Value: " + Integer.parseInt(start[1].substring(0, 2))
//                    + " Third Value: " +  Integer.parseInt(end[0].substring(1))
//                    + " Fourth Value: " + Integer.parseInt(end[1].substring(0, (end[1].length()) - 1)));
        }
        sc.close();
        return zones;
    }

    /**
     * Prints the zone info
     */
    public void printZones() {
        for (Zone zone : zones.values()) {
            System.out.println(zone);
        }
    }

    /**
     * Reads the incidents from the event input file
     * @return
     * @throws FileNotFoundException
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

            Incident.Fault fault;
            switch (line[4]) {
                case "DRONE_STUCK":
                    fault = Incident.Fault.DRONE_STUCK;
                    break;
                case "NOZZLE_JAMMED":
                    fault = Incident.Fault.NOZZLE_JAMMED;
                    break;
                case "PACKET_LOSS":
                    fault = Incident.Fault.PACKET_LOSS;
                    break;
                default:
                    fault = Incident.Fault.NONE;
                    break;
            }

            int zoneId = Integer.parseInt(line[1]); // Correctly parse Zone ID
            incidents.add(new Incident(
                    Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]),
                    zoneId, severity, type, fault));
        }
        sc.close();
        return incidents;
    }
}
