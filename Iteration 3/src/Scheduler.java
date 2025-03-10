import java.util.LinkedList;
import java.util.Queue;

/***
 * This is a class where data is exchanged between Fire Incident System and the Drones
 * Implements scheduling logic for assigning drones to fire incidents.
 * @author ahmedbabar, modified by Abdulaziz Alsibakhi
 */
public class Scheduler implements Runnable {

    private enum SchedulerState {WAITING_FOR_MESSAGE, ASSIGNING_TO_DRONE};
    private SchedulerState state;

    /*
    * Communication between each system is carried out using Box Objects. You can think of them as Endpoints
    */
    private Box fireIncidentSendBox;
    private Box fireIncidentReceiveBox;
    private Box droneSendBox;
    private Box droneReceiveBox;

    private Queue<IncidentMessage> incidentQueue;

    /***
     * Constructor for Scheduler.
     */
    public Scheduler(Box fireIncidentSendBox, Box droneSendBox, Box fireIncidentReceiveBox, Box droneReceiveBox) {
        this.state = SchedulerState.WAITING_FOR_MESSAGE;
        this.fireIncidentSendBox = fireIncidentSendBox;
        this.droneSendBox = droneSendBox;
        this.fireIncidentReceiveBox = fireIncidentReceiveBox;
        this.droneReceiveBox = droneReceiveBox;
        this.incidentQueue = new LinkedList<>();
    }

    @Override
    public void run() {
        while (true) {

            switch (state) {
                case WAITING_FOR_MESSAGE:
                    // Retrieve incident message from FireIncidentSubsystem
                    Object iMessage = fireIncidentReceiveBox.get();
                    if (iMessage instanceof IncidentMessage) {
                        IncidentMessage incident = (IncidentMessage) iMessage;
                        incidentQueue.add(incident);
                    }
                    state = SchedulerState.ASSIGNING_TO_DRONE;
                    break;
                case ASSIGNING_TO_DRONE:
                    // If there are pending incidents, try to assign them to a drone
                    if (!incidentQueue.isEmpty()) {
                        IncidentMessage nextIncident = incidentQueue.poll();
                        System.out.println("Scheduler assigned incident: " + nextIncident.getType() + " at Zone " + nextIncident.getStartX());

                        droneSendBox.put(nextIncident); // Assign to a drone

                        // Wait for drone completion confirmation
                        Object droneAck = droneReceiveBox.get();
                        if (droneAck instanceof Boolean && (Boolean) droneAck) {
                            fireIncidentSendBox.put(true); // Notify FireIncidentSubsystem of completion
                            System.out.println("Scheduler marked incident as resolved.");
                        }
                    }
                    state = SchedulerState.WAITING_FOR_MESSAGE;
                    break;
            }
            
        }
    }
}
