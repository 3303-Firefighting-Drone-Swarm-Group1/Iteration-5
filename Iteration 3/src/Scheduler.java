import java.net.InetAddress;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
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
    private ArrayList<IncidentMessage> newMessages;
    private ArrayList<RPCClient> availableDrones;
    

    public Scheduler(InetAddress droneHost, int dronePort) {
        this.incidentQueue = new LinkedList<>();
        this.droneClient = new RPCClient(droneHost, dronePort);
        availableDrones = new ArrayList<>();

        droneClient.sendRequest("join:" + dronePort);
    }

    public Object handleRequest(Object request) {
        if (request instanceof ArrayList) {
            newMessages = (ArrayList)request;
            schedule();
        } else if (request instanceof String){
            if (((String)request).split(":")[0].equals("join")){
                availableDrones.add(new RPCClient(null, Integer.parseInt(((String)request).split(":")[1])));
            } else if (((String)request).split(":")[0].equals("leave")){
                for (int i = 0; i < availableDrones.size(); i++){
                    if (availableDrones.get(i).getPort() == Integer.parseInt(((String)request).split(":")[1])){
                        availableDrones.remove(i);
                        break;
                    }
                }
            } else {
                
                
            }
        }
        return null;
    }

    private void schedule(){
        HashMap<RPCClient, Time> unavailableDrones = new HashMap<>();
        ArrayList<IncidentMessage> ready = new ArrayList<>();
        ArrayList<IncidentMessage> scheduled = new ArrayList<>();
        
        for (long time = getMinTime(); time < 86400 && newMessages.size() != 0 && ready.size() != 0 && scheduled.size() != 0; time += 1){
            for (int i = 0; i < newMessages.size(); i++){
                if (newMessages.get(i).getTime().getTime() / 1000 <= time){
                    IncidentMessage temp = newMessages.remove(i);
                    int j = 0;
                    while (j < ready.size() && ((temp.getSeverity() == Incident.Severity.LOW && (ready.get(j).getSeverity() == Incident.Severity.HIGH || ready.get(j).getSeverity() == Incident.Severity.MODERATE)) || (temp.getSeverity() == Incident.Severity.MODERATE && ready.get(j).getSeverity() == Incident.Severity.HIGH))) j++;
                    while(j < ready.size() && (temp.getDistance() > ready.get(j).getDistance())) j++;
                    ready.add(j, temp);
                }
            }

            for (RPCClient drone : unavailableDrones.keySet()){
                if (unavailableDrones.get(drone).getTime() / 1000 <= time){
                    availableDrones.add(drone);
                    unavailableDrones.remove(drone);
                    System.out.println("Task completed.");
                }
            }

            while (ready.size() != 0 && availableDrones.size() != 0){
                scheduled.add(ready.remove(0));
                unavailableDrones.put(availableDrones.remove(0), (Time) availableDrones.get(0).sendRequest(ready.get(0))); // RECEIVE TIME
            }
        }
    }

    private long getMinTime(){
        long minTime = 86400;
        for (IncidentMessage message: newMessages){
            if (message.getTime().getTime() / 1000 < minTime) minTime = message.getTime().getTime() / 1000;
        }
        return minTime;
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