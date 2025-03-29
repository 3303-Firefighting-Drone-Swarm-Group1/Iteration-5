import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Scheduler that assigns drones to incidents and handles faults.
 * When a fault message is received or a drone returns a negative response,
 * the faulty drone is removed and the incident is requeued.
 */
public class Scheduler {
    private Queue<IncidentMessage> incidentQueue;
    private ArrayList<IncidentMessage> newMessages;
    private ArrayList<RPCClient> availableDrones;

    public Scheduler(int schedulerPort) {
        this.incidentQueue = new LinkedList<>();
        availableDrones = new ArrayList<>();
        new Thread(new RPCServer(schedulerPort, this)).start(); // Start Scheduler's RPC server
    }

    public Object handleRequest(Object request) {
        if (request instanceof ArrayList) {
            newMessages = (ArrayList<IncidentMessage>) request;
            schedule();
        } else if (request instanceof String) {
            String reqStr = (String) request;
            String[] parts = reqStr.split(":");
            if (parts[0].equals("join")) {
                availableDrones.add(new RPCClient(null, Integer.parseInt(parts[1])));
                System.out.println("Drone joined scheduler.");
            } else if (parts[0].equals("leave")) {
                availableDrones.removeIf(drone -> drone.getPort() == Integer.parseInt(parts[1]));
            } else if (parts[0].equals("fault")) {
                int faultyDronePort = Integer.parseInt(parts[1]);
                String faultType = parts[2];
                availableDrones.removeIf(drone -> drone.getPort() == faultyDronePort);
                System.out.println("[Scheduler] Fault detected: Drone on port " + faultyDronePort +
                        " cancelled due to " + faultType);
            } else if (parts[0].equals("completed")) {
                // Optionally handle completion acknowledgments here.
            }
        }
        return null;
    }

    private void schedule() {
        ConcurrentHashMap<RPCClient, Time> unavailableDrones = new ConcurrentHashMap<>();
        ConcurrentHashMap<RPCClient, Time> faultBoundDrones = new ConcurrentHashMap<>();
        ConcurrentHashMap<RPCClient, Time> returningDrones = new ConcurrentHashMap<>();
        ArrayList<IncidentMessage> readyHigh = new ArrayList<>();
        ArrayList<IncidentMessage> readyModerate = new ArrayList<>();
        ArrayList<IncidentMessage> readyLow = new ArrayList<>();
        ConcurrentHashMap<RPCClient, IncidentMessage> scheduled = new ConcurrentHashMap<>();

        // For simulation, use a simple time counter.
        long time = getMinTime();
        while (!readyHigh.isEmpty() || !readyModerate.isEmpty() || !readyLow.isEmpty() || unavailableDrones.size() != 0 || newMessages.size() != 0 || !scheduled.isEmpty()) {
            for (int i = 0; i < newMessages.size(); i++){
                if (newMessages.get(i).getTime().getTime() / 1000 <= time){
                    IncidentMessage temp = newMessages.remove(i);
                    int j = 0;
                    switch (temp.getSeverity()){
                        case HIGH:
                            while(j < readyHigh.size() && (temp.getDistance() > readyHigh.get(j).getDistance())) j++;
                            readyHigh.add(temp);
                            break;
                        case MODERATE:
                            while(j < readyModerate.size() && (temp.getDistance() > readyModerate.get(j).getDistance())) j++;
                            readyModerate.add(temp);
                            break;
                        case LOW:
                            while(j < readyLow.size() && (temp.getDistance() > readyLow.get(j).getDistance())) j++;
                            readyLow.add(temp);
                            break;
                    }
                }
            }

            for (RPCClient drone : unavailableDrones.keySet()){
                if (unavailableDrones.get(drone).getTime() / 1000 <= time){
                    availableDrones.add(drone); 
                    unavailableDrones.remove(drone);
                    scheduled.remove(drone);
                    System.out.println("\nTask completed at time: " + new Time(time * 1000 + 18000000));
                }
            }

            for (RPCClient drone : returningDrones.keySet()){
                if (returningDrones.get(drone).getTime() / 1000 <= time){
                    availableDrones.add(drone); 
                    unavailableDrones.remove(drone);
                    returningDrones.remove(drone);
                    System.out.println("\nDrone returned at time: " + new Time(time * 1000 + 18000000));
                }
            }

            for (RPCClient drone : faultBoundDrones.keySet()){
                if (faultBoundDrones.get(drone).getTime() / 1000 <= time){
                    System.out.println("[Scheduler] Fault detected: Drone on port " + drone.getPort() +
                            " cancelled due to " + scheduled.get(drone).getFault());
                    System.out.println("[" + new Time(time * 1000 + 18000000) +  "] Scheduler reassigns incident: " + scheduled.get(drone).getType());
                    IncidentMessage temp = scheduled.remove(drone);
                    temp.clearFault();
                    newMessages.add(temp);
                    faultBoundDrones.remove(drone);
                }
            }

            // Process high-severity incidents.
            while (!readyHigh.isEmpty() && !availableDrones.isEmpty()) {
                IncidentMessage incident = readyHigh.get(0);
                RPCClient drone = availableDrones.get(0);
                double response = (double) drone.sendRequest(incident);
                System.out.println("Drone sent response: " + response);
                scheduled.put(drone, incident);
                if (response < 0) {
                    
                    returningDrones.put(drone, new Time((long) (time * 1000 - 2 * response)));
                    availableDrones.remove(0);
                    readyHigh.remove(0);
                    faultBoundDrones.put(drone, new Time((long) (time * 1000 - response)));
                } else {
                    readyHigh.remove(0);
                    Time temp = new Time(time * 1000 + (long) response);
                    unavailableDrones.put(drone, temp);
                    System.out.println("Scheduler assigned incident: " + incident.getType() +
                            " with drone on port " + drone.getPort() +
                            " at time: " + new Time(time * 1000 + 18000000));
                    availableDrones.remove(0);
                }
            }

            // Process moderate-severity incidents.
            while (!readyModerate.isEmpty() && !availableDrones.isEmpty()) {
                IncidentMessage incident = readyModerate.get(0);
                RPCClient drone = availableDrones.get(0);
                double response = (double) drone.sendRequest(incident);
                scheduled.put(drone, incident);
                if (response < 0) {
                    returningDrones.put(drone, new Time((long) (time * 1000 - 2 * response)));
                    availableDrones.remove(0);
                    readyModerate.remove(0);
                    faultBoundDrones.put(drone, new Time((long) (time * 1000 - response)));
                } else {
                    readyModerate.remove(0);
                    Time temp = new Time(time * 1000 + (long) response);
                    unavailableDrones.put(drone, temp);
                    System.out.println("Scheduler assigned incident: " + incident.getType() +
                            " with drone on port " + drone.getPort() +
                            " at time: " + new Time(time * 1000 + 18000000));
                    availableDrones.remove(0);
                }
            }

            // Process low-severity incidents.
            while (!readyLow.isEmpty() && !availableDrones.isEmpty()) {
                IncidentMessage incident = readyLow.get(0);
                RPCClient drone = availableDrones.get(0);
                double response = (double) drone.sendRequest(incident);
                scheduled.put(drone, incident);
                if (response < 0) {
                    returningDrones.put(drone, new Time((long) (time * 1000 - 2 * response)));
                    availableDrones.remove(0);
                    readyLow.remove(0);
                    faultBoundDrones.put(drone, new Time((long) (time * 1000 - response)));
                } else {
                    readyLow.remove(0);
                    Time temp = new Time(time * 1000 + (long) response);
                    unavailableDrones.put(drone, temp);
                    System.out.println("Scheduler assigned incident: " + incident.getType() +
                            " with drone on port " + drone.getPort() +
                            " at time: " + new Time(time * 1000 + 18000000));
                    availableDrones.remove(0);
                }
            }
            // Increment time for simulation.
            time++;
        }
        System.out.println("Scheduling finished.");
    }

    private long getMinTime() {
        long minTime = Long.MAX_VALUE;
        for (IncidentMessage message : newMessages) {
            long t = message.getTime().getTime() / 1000;
            if (t < minTime) minTime = t;
        }
        return minTime;
    }
}
