import java.util.LinkedList;
import java.util.Queue;

/***
 * This is a class where data is exchanged between Fire Incident System and the Drones
 * Implements scheduling logic for assigning drones to fire incidents.
 * @author ahmedbabar, modified by Abdulaziz Alsibakhi
 */

public class Scheduler {
    private Queue<IncidentMessage> incidentQueue;
    private RPCClient droneClient;

    public Scheduler(String droneHost, int dronePort) {
        this.incidentQueue = new LinkedList<>();
        this.droneClient = new RPCClient(droneHost, dronePort);
    }

    public Object handleRequest(Object request) {
        if (request instanceof IncidentMessage) {
            IncidentMessage incident = (IncidentMessage) request;
            System.out.println("Scheduler received incident: " + incident.getType() + " at Zone " + incident.getStartX());
            incidentQueue.add(incident);
            assignIncident(incident); // Directly assign the incident
            return "Incident received by Scheduler.";
        }
        return null;
    }

    public void assignIncident(IncidentMessage incident) {
        System.out.println("Scheduler assigned incident: " + incident.getType() + " at Zone " + incident.getStartX());
        droneClient.sendRequest(incident); // Assign incident to Drone

        // Wait for drone completion confirmation
        Object droneAck = droneClient.sendRequest(true);
        if (droneAck instanceof Boolean && (Boolean) droneAck) {
            System.out.println("Scheduler marked incident as resolved.");
        }
    }
}