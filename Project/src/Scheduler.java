import java.awt.event.ActionEvent;
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

    //Subscribed Classes
    private ViewController viewController;

    public Scheduler(int schedulerPort, ViewController viewController) {
        this.viewController = viewController;
        idle = new ArrayList<>();
        new Thread(new RPCServer(schedulerPort, this)).start(); // Start Scheduler's RPC server
        map = new Map();
    }

    public Scheduler(int schedulerPort) {
        this.viewController = null;
        idle = new ArrayList<>();
        new Thread(new RPCServer(schedulerPort, this)).start(); // Start Scheduler's RPC server
        map = new Map();
    }

    /**
     * Deals with incoming RPC messages
     * @param request the incoming message
     * @return the response
     */
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

    /**
     * Simulates fire extinguishing with the currrent drones.
     */
    private void schedule()
    {
        //fires
        ArrayList<Fire> readyHigh = new ArrayList<>();
        ArrayList<Fire> readyModerate = new ArrayList<>();
        ArrayList<Fire> readyLow = new ArrayList<>();
        ConcurrentHashMap<Fire, Long> fireTimes = new ConcurrentHashMap<>();
        ArrayList<Long> responseTimes = new ArrayList<>();
        ArrayList<Long> extinguishedTimes = new ArrayList<>();

        //drones
        ConcurrentHashMap<Drone, Long> enRoute = new ConcurrentHashMap<>();
        ConcurrentHashMap<Drone, Long> droppingAgent = new ConcurrentHashMap<>();
        ConcurrentHashMap<Drone, Long> returning = new ConcurrentHashMap<>();
        ArrayList<Drone> hardFaulted = new ArrayList<>();
        ConcurrentHashMap<Drone, Long> transientFaulted = new ConcurrentHashMap<>();


        ConcurrentHashMap<Drone, Fire> scheduled = new ConcurrentHashMap<>();

        // For simulation, use a simple time counter.
        long minTime = getMinTime();
        long time = minTime;
        while (!newMessages.isEmpty() || !readyHigh.isEmpty() || !readyModerate.isEmpty() || !readyLow.isEmpty() || !enRoute.isEmpty() || !droppingAgent.isEmpty() || !returning.isEmpty() || !transientFaulted.isEmpty()) {

            //check for newly active fires
            for (int i = 0; i < newMessages.size(); i++){
                if (newMessages.get(i).getTime() <= time){
                    IncidentMessage temp = newMessages.remove(i);
                    Fire f = new Fire((temp.getStartX() + temp.getEndX()) / 2.0, (temp.getStartY() + temp.getEndY()) / 2.0, temp.getSeverity(), temp.getFault());
                    fireTimes.put(f, time);
                    int j = 0;
                    switch (temp.getSeverity()){
                        case HIGH:
                            while(j < readyHigh.size() && (temp.getDistance() > readyHigh.get(j).getDistance())) j++;
                            readyHigh.add(j, f);
                            map.addFire(f);
                            break;
                        case MODERATE:
                            while(j < readyModerate.size() && (temp.getDistance() > readyModerate.get(j).getDistance())) j++;
                            readyModerate.add(j, f);
                            map.addFire(f);
                            break;
                        case LOW:
                            while(j < readyLow.size() && (temp.getDistance() > readyLow.get(j).getDistance())) j++;
                            readyLow.add(j, f);
                            map.addFire(f);
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
                        drone.setState(DroneSubsystem.DroneState.FAULTED);
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
                    responseTimes.add(time - fireTimes.get(fire));
                    scheduled.put(drone, fire);
                    Object response = drone.sendRequest(makeTaskMessage(fire, drone), TIMEOUT);
                    if (response != null){
                        long t = (long) response;
                        enRoute.put(drone, time + t);
                        drone.setVelocity(1000 * fire.getX() / (double)t, 1000 * fire.getY() / (double)t);
                        drone.setLocation(0, 0);
                        drone.setState(DroneSubsystem.DroneState.EN_ROUTE);
                    } else {
                        readyHigh.add(0, scheduled.get(drone));
                        scheduled.remove(drone);
                        System.out.println("Packet loss detected. Drone deleted.");
                        drone.setState(DroneSubsystem.DroneState.FAULTED);
                    }  
                } else if (!readyModerate.isEmpty()){
                    Drone drone = idle.remove(0);
                    Fire fire = readyModerate.remove(0);
                    responseTimes.add(time - fireTimes.get(fire));
                    scheduled.put(drone, fire);
                    Object response = drone.sendRequest(makeTaskMessage(fire, drone), TIMEOUT);
                    if (response != null){
                        long t = (long) response;
                        enRoute.put(drone, time + t);
                        drone.setVelocity(1000 * fire.getX() / (double)t, 1000 * fire.getY() / (double)t);
                        drone.setLocation(0, 0);
                        drone.setState(DroneSubsystem.DroneState.EN_ROUTE);
                    } else {
                        readyModerate.add(0, scheduled.get(drone));
                        scheduled.remove(drone);
                        System.out.println("Packet loss detected. Drone deleted.");
                        drone.setState(DroneSubsystem.DroneState.FAULTED);
                    }  
                } else {
                    Drone drone = idle.remove(0);
                    Fire fire = readyLow.remove(0);
                    responseTimes.add(time - fireTimes.get(fire));
                    scheduled.put(drone, fire);
                    Object response = drone.sendRequest(makeTaskMessage(fire, drone), TIMEOUT);
                    if (response != null){
                        long t = (long) response;
                        enRoute.put(drone, time + t);
                        drone.setVelocity(1000 * fire.getX() / (double)t, 1000 * fire.getY() / (double)t);
                        drone.setLocation(0, 0);
                        drone.setState(DroneSubsystem.DroneState.EN_ROUTE);
                    } else {
                        readyLow.add(0, scheduled.get(drone));
                        scheduled.remove(drone);
                        System.out.println("Packet loss detected. Drone deleted.");
                        drone.setState(DroneSubsystem.DroneState.FAULTED);
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
                            drone.setState(DroneSubsystem.DroneState.DROPPING_AGENT);
                        }
                        else if (t == -69){
                            transientFaulted.put(drone, time + 60000);
                            drone.setState(DroneSubsystem.DroneState.FAULTED);
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
                            drone.setState(DroneSubsystem.DroneState.FAULTED);
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
                            drone.setState(DroneSubsystem.DroneState.FAULTED);
                        }
                        drone.setVelocity(0, 0);
                        
                        if (t > 0) drone.setLocation(scheduled.get(drone).getX(), scheduled.get(drone).getY());
                    } else {
                        System.out.println("Packet loss detected. Drone deleted.");
                        drone.setState(DroneSubsystem.DroneState.FAULTED);
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
                        } else {
                            extinguishedTimes.add(time - fireTimes.get(fire));
                            map.removeFire(fire);
                        }
                        drone.setVelocity(-1000 * drone.getX() / (double)t, -1000 * drone.getY() / (double)t);
                        drone.setLocation(scheduled.get(drone).getX(), scheduled.get(drone).getY());
                        drone.setState(DroneSubsystem.DroneState.RETURNING_TO_BASE);
                        scheduled.remove(drone);
                        System.out.println("Drone finished dropping water at: " + new Time(time + 18000000));
                    } else {
                        
                        System.out.println("Packet loss detected. Drone deleted.");
                        drone.setState(DroneSubsystem.DroneState.FAULTED);
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
                        drone.setVelocity(-1000 * drone.getX() / (double)t, -1000 * drone.getY() / (double)t);
                        drone.setState(DroneSubsystem.DroneState.RETURNING_TO_BASE);
                        System.out.println("Drone recovered from a transient fault at: " + new Time(time + 18000000));
                    } else {
                        System.out.println("Packet loss detected. Drone deleted.");
                        scheduled.remove(drone);
                    }
                }
            }

            // Increment time for simulation.
            map.updatePositions();
            time+= 1000;
            try {
                Thread.sleep(20);
            } catch (Exception e) {}
        }

        System.out.println("=== Simulation Summary ===");
        System.out.printf("Total time to extinguish all fires: %.2f minutes\n", (time - minTime) / 60000.0);
        long sum = 0;
        for (long responseTime : responseTimes) {
            sum += responseTime;
        }
        System.out.printf("Average response time: %.2f minutes\n", sum / (60000.0 * (double)responseTimes.size()));
        sum = 0;
        for (long extinguishedTime: extinguishedTimes) {
            sum += extinguishedTime;
        }
        System.out.printf("Average extinguished time: %.2f minutes\n", sum / (60000.0 * (double)extinguishedTimes.size()));
    }

    /**
     * Gets the first time that a fire appears, in ms
     * @return the first time
     */
    private long getMinTime() {
        long minTime = Long.MAX_VALUE;
        for (IncidentMessage message : newMessages) {
            long t = message.getTime();
            if (t < minTime) minTime = t;
        }
        return minTime;
    }

    public TaskMessage makeTaskMessage(Fire fire, Drone drone){
        return new TaskMessage(Math.min(fire.getWater(), DroneSubsystem.SIZE_OF_TANK), new Point(fire.getX(), fire.getY()), new Point(drone.getX(), drone.getY()), fire.getFault());
    }

    /**
     * Makes a task message to be sent to a drone
     * @param drone The drone handling the task
     * @return The task message
     */
    private TaskMessage makeTaskMessage(Drone drone){
        return new TaskMessage(0, new Point(drone.getX(), drone.getY()), new Point(drone.getX(), drone.getY()), null);
    }

    /**
     * Gets the map of the scheduler's current state
     * @return the map
     */
    public Map getMap() {
        return map;
    }

    /**
     * Sets the view controller associated with the scheduler
     * @param viewController the view controller
     */
    public void setViewController(ViewController viewController) {
        this.viewController = viewController;
    }
}
