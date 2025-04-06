import java.sql.Time;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Scheduler that assigns drones to incidents and handles faults.
 * When a fault message is received or a drone returns a negative response,
 * the faulty drone is removed and the incident is requeued.
 */
public class Scheduler {
    public static final int TIMEOUT = 2000;
    private ArrayList<IncidentMessage> newMessages;
    private ArrayList<Drone> idle;
    private Map map;

    public Scheduler(int schedulerPort) {
        idle = new ArrayList<>();
        new Thread(new RPCServer(schedulerPort, this)).start(); // Start Scheduler's RPC server
        map = new Map();
    }
    public Object handleRequest(Object request) {
        if (request instanceof ArrayList<?>) {
            newMessages = (ArrayList<IncidentMessage>) request;
            schedule();
        } else if (request instanceof String) {
            String reqStr = (String) request;
            String[] parts = reqStr.split(":");
            if (parts[0].equals("join")) {
                Drone d = new Drone(new RPCClient(null, Integer.parseInt(parts[1])));
                idle.add(d);
                map.addDrone(d);
                System.out.println("Drone joined scheduler.");
            } else if (parts[0].equals("leave")) {
                idle.removeIf(drone -> drone.getPort() == Integer.parseInt(parts[1]));
                map.removeDrone(Integer.parseInt(parts[1]));
            } else if (parts[0].equals("completed")) {
                // Optionally handle completion acknowledgments here.
            }
        }
        return null;
    }

    private void schedule() {
        //fires
        ArrayList<Fire> readyHigh = new ArrayList<>();
        ArrayList<Fire> readyModerate = new ArrayList<>();
        ArrayList<Fire> readyLow = new ArrayList<>();

        //drones
        ConcurrentHashMap<Drone, Long> enRoute = new ConcurrentHashMap<>();
        ConcurrentHashMap<Drone, Long> droppingAgent = new ConcurrentHashMap<>();
        ConcurrentHashMap<Drone, Long> returning = new ConcurrentHashMap<>();
        ArrayList<Drone> hardFaulted = new ArrayList<>();
        ConcurrentHashMap<Drone, Long> transientFaulted = new ConcurrentHashMap<>();


        ConcurrentHashMap<Drone, Fire> scheduled = new ConcurrentHashMap<>();

        // For simulation, use a simple time counter.
        long time = getMinTime();
        while (!newMessages.isEmpty() || !readyHigh.isEmpty() || !readyModerate.isEmpty() || !readyLow.isEmpty() || !enRoute.isEmpty() || !droppingAgent.isEmpty() || !returning.isEmpty() || !transientFaulted.isEmpty()) {

            //check for newly active fires
            for (int i = 0; i < newMessages.size(); i++){
                if (newMessages.get(i).getTime() <= time){
                    IncidentMessage temp = newMessages.remove(i);
                    int j = 0;
                    switch (temp.getSeverity()){
                        case HIGH:
                            while(j < readyHigh.size() && (temp.getDistance() > readyHigh.get(j).getDistance())) j++;
                            Fire f1 = new Fire((temp.getStartX() + temp.getEndX()) / 2.0, (temp.getStartY() + temp.getEndY()) / 2.0, temp.getSeverity(), temp.getFault());
                            readyHigh.add(j, f1);
                            map.addFire(f1);
                            break;
                        case MODERATE:
                            while(j < readyModerate.size() && (temp.getDistance() > readyModerate.get(j).getDistance())) j++;
                            Fire f2 = new Fire((temp.getStartX() + temp.getEndX()) / 2.0, (temp.getStartY() + temp.getEndY()) / 2.0, temp.getSeverity(), temp.getFault());
                            readyModerate.add(j, f2);
                            map.addFire(f2);
                            break;
                        case LOW:
                            while(j < readyLow.size() && (temp.getDistance() > readyLow.get(j).getDistance())) j++;
                            Fire f3 = new Fire((temp.getStartX() + temp.getEndX()) / 2.0, (temp.getStartY() + temp.getEndY()) / 2.0, temp.getSeverity(), temp.getFault());
                            readyLow.add(j, f3);
                            map.addFire(f3);
                            break;
                    }
                    System.out.println("Fire exists at: " + new Time(time + 18000000));
                }
            }

            //check if any returning drones are back
            for (Drone drone: returning.keySet()){
                if (returning.get(drone) <= time){
                    if (drone.sendRequest(makeTaskMessage(drone), TIMEOUT) != null){
                        idle.add(drone);
                        drone.setVelocity(0, 0);
                        drone.setLocation(0, 0);
                    } else {
                        System.out.println("Packet loss detected. Drone deleted.");
                    }
                    returning.remove(drone);
                    
                    System.out.println("Drone returned at: " + new Time(time + 18000000));
                }
            }

            //assign fires to drones
            while (!idle.isEmpty() && (!readyHigh.isEmpty() || !readyModerate.isEmpty() || !readyLow.isEmpty())){
                if (!readyHigh.isEmpty()){
                    Drone drone = idle.remove(0);
                    Fire fire = readyHigh.remove(0);
                    scheduled.put(drone, fire);
                    Object response = drone.sendRequest(makeTaskMessage(fire, drone), TIMEOUT);
                    if (response != null){
                        long t = (long) response;
                        enRoute.put(drone, time + t);
                        drone.setVelocity(fire.getX() / (double)t, fire.getY() / (double)t);
                        drone.setLocation(0, 0);
                    } else {
                        readyHigh.add(0, scheduled.get(drone));
                        scheduled.remove(drone);
                        System.out.println("Packet loss detected. Drone deleted.");
                    }  
                } else if (!readyModerate.isEmpty()){
                    Drone drone = idle.remove(0);
                    Fire fire = readyModerate.remove(0);
                    scheduled.put(drone, fire);
                    Object response = drone.sendRequest(makeTaskMessage(fire, drone), TIMEOUT);
                    if (response != null){
                        long t = (long) response;
                        enRoute.put(drone, time + t);
                        drone.setVelocity(fire.getX() / (double)t, fire.getY() / (double)t);
                        drone.setLocation(0, 0);
                    } else {
                        readyModerate.add(0, scheduled.get(drone));
                        scheduled.remove(drone);
                        System.out.println("Packet loss detected. Drone deleted.");
                    }  
                } else {
                    Drone drone = idle.remove(0);
                    Fire fire = readyLow.remove(0);
                    scheduled.put(drone, fire);
                    Object response = drone.sendRequest(makeTaskMessage(fire, drone), TIMEOUT);
                    if (response != null){
                        long t = (long) response;
                        enRoute.put(drone, time + t);
                        drone.setVelocity(fire.getX() / (double)t, fire.getY() / (double)t);
                        drone.setLocation(0, 0);
                    } else {
                        readyLow.add(0, scheduled.get(drone));
                        scheduled.remove(drone);
                        System.out.println("Packet loss detected. Drone deleted.");
                    }  
                }
                System.out.println("Fire assigned to Drone at: " + new Time(time + 18000000));
            }

            //check if any drones arrived at fire
            for (Drone drone: enRoute.keySet()){
                if (enRoute.get(drone) <= time){
                    enRoute.remove(drone);
                    Object response = drone.sendRequest(makeTaskMessage(scheduled.get(drone), drone));
                    if (response != null){
                        long t = (long) response;
                        if (t > 0){
                            droppingAgent.put(drone, time + t);
                            System.out.println("Drone arrived at fire at time: " + new Time(time + 18000000));
                        }
                        else if (t == -69){
                            transientFaulted.put(drone, time + 60000);
                            Fire fire = scheduled.get(drone);
                            fire.clearFault();
                            int j = 0;
                            switch (fire.getSeverity()){
                                case HIGH:
                                    while(j < readyHigh.size() && (fire.getDistance() > readyHigh.get(j).getDistance())) j++;
                                    readyHigh.add(j, fire);
                                    break;
                                case MODERATE:
                                    while(j < readyModerate.size() && (fire.getDistance() > readyModerate.get(j).getDistance())) j++;
                                    readyModerate.add(j, fire);
                                    break;
                                case LOW:
                                    while(j < readyLow.size() && (fire.getDistance() > readyLow.get(j).getDistance())) j++;
                                    readyLow.add(j, fire);
                                    break;
                            }
                            scheduled.remove(drone);
                            System.out.println("Drone experienced a transient fault at time: " + new Time(time + 18000000));
                        }
                        else if (t == -420){
                            Fire fire = scheduled.get(drone);
                            fire.clearFault();
                            int j = 0;
                            switch (fire.getSeverity()){
                                case HIGH:
                                    while(j < readyHigh.size() && (fire.getDistance() > readyHigh.get(j).getDistance())) j++;
                                    readyHigh.add(j, fire);
                                    break;
                                case MODERATE:
                                    while(j < readyModerate.size() && (fire.getDistance() > readyModerate.get(j).getDistance())) j++;
                                    readyModerate.add(j, fire);
                                    break;
                                case LOW:
                                    while(j < readyLow.size() && (fire.getDistance() > readyLow.get(j).getDistance())) j++;
                                    readyLow.add(j, fire);
                                    break;
                            }
                            scheduled.remove(drone);
                            hardFaulted.add(drone);
                            System.out.println("Drone experienced a hard fault at time: " + new Time(time + 18000000));
                        }
                        drone.setVelocity(0, 0);
                        if (t > 0) drone.setLocation(scheduled.get(drone).getX(), scheduled.get(drone).getY());
                    } else {
                        System.out.println("Packet loss detected. Drone deleted.");
                        Fire fire = scheduled.get(drone);
                        int j = 0;
                        switch (scheduled.get(drone).getSeverity()){
                            case HIGH:
                                    while(j < readyHigh.size() && (fire.getDistance() > readyHigh.get(j).getDistance())) j++;
                                    readyHigh.add(j, fire);
                                    break;
                                case MODERATE:
                                    while(j < readyModerate.size() && (fire.getDistance() > readyModerate.get(j).getDistance())) j++;
                                    readyModerate.add(j, fire);
                                    break;
                                case LOW:
                                    while(j < readyLow.size() && (fire.getDistance() > readyLow.get(j).getDistance())) j++;
                                    readyLow.add(j, fire);
                                    break;
                        }
                        scheduled.remove(drone);
                    }
                }
                    
            }
        

            //check if any drones finished dropping agent
            for (Drone drone: droppingAgent.keySet()){
                if (droppingAgent.get(drone) <= time){
                    droppingAgent.remove(drone);
                    Object response = drone.sendRequest(makeTaskMessage(scheduled.get(drone), drone), TIMEOUT);
                    if (response != null){
                        long t = (long) response;
                        returning.put(drone, time + t);
                        Fire fire = scheduled.get(drone);
                        fire.putWater(Math.min(fire.getWater(), DroneSubsystem.SIZE_OF_TANK));
                        if (fire.getWater() > 0){
                            int j = 0;
                            switch (fire.getSeverity()){
                                case HIGH:
                                    while(j < readyHigh.size() && (fire.getDistance() > readyHigh.get(j).getDistance())) j++;
                                    readyHigh.add(j, fire);
                                    break;
                                case MODERATE:
                                    while(j < readyModerate.size() && (fire.getDistance() > readyModerate.get(j).getDistance())) j++;
                                    readyModerate.add(j, fire);
                                    break;
                                case LOW:
                                    while(j < readyLow.size() && (fire.getDistance() > readyLow.get(j).getDistance())) j++;
                                    readyLow.add(j, fire);
                                    break;
                            }
                        }
                        drone.setVelocity(-drone.getX() / (double)t, -drone.getY() / (double)t);
                        drone.setLocation(scheduled.get(drone).getX(), scheduled.get(drone).getY());
                        scheduled.remove(drone);
                        System.out.println("Drone finished dropping water at: " + new Time(time + 18000000));
                    } else {
                        System.out.println("Packet loss detected. Drone deleted.");
                        Fire fire = scheduled.get(drone);
                        int j = 0;
                        switch (scheduled.get(drone).getSeverity()){
                            case HIGH:
                                    while(j < readyHigh.size() && (fire.getDistance() > readyHigh.get(j).getDistance())) j++;
                                    readyHigh.add(j, fire);
                                    break;
                                case MODERATE:
                                    while(j < readyModerate.size() && (fire.getDistance() > readyModerate.get(j).getDistance())) j++;
                                    readyModerate.add(j, fire);
                                    break;
                                case LOW:
                                    while(j < readyLow.size() && (fire.getDistance() > readyLow.get(j).getDistance())) j++;
                                    readyLow.add(j, fire);
                                    break;
                        }
                        scheduled.remove(drone);
                    }
                }
            }

            //check if any drones have recovered from a fault
            for (Drone drone: transientFaulted.keySet()){
                if (transientFaulted.get(drone) <= time){
                    transientFaulted.remove(drone);
                    Object response = drone.sendRequest(makeTaskMessage(drone) ,2000);
                    if (response != null){
                        long t = (long) response;
                        returning.put(drone, time + t);
                        drone.setVelocity(-drone.getX() / (double)t, -drone.getY() / (double)t);
                        System.out.println("Drone recovered from a transient fault at: " + new Time(time + 18000000));
                    } else {
                        System.out.println("Packet loss detected. Drone deleted.");
                        scheduled.remove(drone);
                    }
                }
            }

            // Increment time for simulation.
            map.updatePositions(time);
            time++;
        }
        System.out.println("Scheduling finished.");
    }

    private long getMinTime() {
        long minTime = Long.MAX_VALUE;
        for (IncidentMessage message : newMessages) {
            long t = message.getTime();
            if (t < minTime) minTime = t;
        }
        return minTime;
    }

    private TaskMessage makeTaskMessage(Fire fire, Drone drone){
        return new TaskMessage(Math.min(fire.getWater(), DroneSubsystem.SIZE_OF_TANK), new Point(fire.getX(), fire.getY()), new Point(drone.getX(), drone.getY()), fire.getFault());
    }

    private TaskMessage makeTaskMessage(Drone drone){
        return new TaskMessage(0, new Point(drone.getX(), drone.getY()), new Point(drone.getX(), drone.getY()), null);
    }
}
