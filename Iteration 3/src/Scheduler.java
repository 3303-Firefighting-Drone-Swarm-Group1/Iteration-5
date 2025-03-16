import java.net.InetAddress;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/***
 * This is a class where data is exchanged between Fire Incident System and the Drones
 * Implements scheduling logic for assigning drones to fire incidents.
 * @author ahmedbabar, modified by Abdulaziz Alsibakhi
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
            newMessages = (ArrayList)request;
            schedule();
        } else if (request instanceof String){
            if (((String)request).split(":")[0].equals("join")){
                availableDrones.add(new RPCClient(null, Integer.parseInt(((String)request).split(":")[1])));
                System.out.println("Drone joined scheduler.");
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
        ConcurrentHashMap<RPCClient, Time> unavailableDrones = new ConcurrentHashMap<>();
        ArrayList<IncidentMessage> readyHigh = new ArrayList<>();
        ArrayList<IncidentMessage> readyModerate = new ArrayList<>();
        ArrayList<IncidentMessage> readyLow = new ArrayList<>();
        ArrayList<IncidentMessage> scheduled = new ArrayList<>();
        

        for (long time = getMinTime(); time < 864000 && (newMessages.size() != 0 || readyHigh.size() != 0 || readyModerate.size() != 0 || readyLow.size() != 0 || scheduled.size() != 0 || unavailableDrones.size() != 0); time += 1){
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
                    System.out.println("\nTask completed at time: " + new Time(time * 1000 + 18000000));
                }
            }


            while (readyHigh.size() != 0 && availableDrones.size() != 0){
                scheduled.add(readyHigh.get(0));
                Time temp = new Time((long)(time * 1000 + (double)availableDrones.get(0).sendRequest(readyHigh.remove(0))));
                unavailableDrones.put(availableDrones.remove(0), temp);
                System.out.println("Scheduler assigned incident: " + scheduled.get(scheduled.size() - 1).getType() + " at time: " + new Time(time * 1000 + 18000000));
            }

            while (readyModerate.size() != 0 && availableDrones.size() != 0){
                scheduled.add(readyModerate.get(0));
                Time temp = new Time((long)(time * 1000 + (double)availableDrones.get(0).sendRequest(readyModerate.remove(0))));
                unavailableDrones.put(availableDrones.remove(0), temp);
                System.out.println("Scheduler assigned incident: " + scheduled.get(scheduled.size() - 1).getType() + " at time: " + new Time(time * 1000 + 18000000));
            }

            while (readyLow.size() != 0 && availableDrones.size() != 0){
                scheduled.add(readyLow.get(0));
                Time temp = new Time((long)(time * 1000 + (double)availableDrones.get(0).sendRequest(readyLow.remove(0))));
                unavailableDrones.put(availableDrones.remove(0), temp);
                System.out.println("Scheduler assigned incident: " + scheduled.get(scheduled.size() - 1).getType() + " at time: " + new Time(time * 1000 + 18000000));
            }
        }
        System.out.println("Scheduling finished.");
    }

    private long getMinTime(){
        long minTime = 86400;
        for (IncidentMessage message: newMessages){
            if (message.getTime().getTime() / 1000 < minTime) minTime = message.getTime().getTime() / 1000;
        }
        return minTime;
    }
}