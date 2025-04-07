/**
 * Incident represents an incident in the Firefighting Drone Swarm system.
 * @author Lucas Warburton, 101276823
 */

import java.io.Serializable;
import java.sql.Time;

public class Incident implements Serializable {
    public enum Severity{
        LOW,
        MODERATE,
        HIGH
    }

    public enum Type{
        FIRE_DETECTED,
        DRONE_REQUEST
    }

    public enum Fault{
        NONE,
        DRONE_STUCK,
        NOZZLE_JAMMED,
        PACKET_LOSS
    }

    private long time;
    private int id;
    private Severity severity;
    private Fault fault;
    private Type type;

    public Incident(int hour, int minute, int second, int id, Severity severity, Type type, Fault fault) {
        this.time = ((hour * 60 + minute) * 60 + second) * 1000;
        this.id = id;
        this.severity = severity;
        this.type = type;
        this.fault = fault;
    }

    /**
     * Gets the ID of the zone in which the incident happened.
     * @return the ID
     */
    public int getID(){
        return id;
    }

    /**
     * Gets the severity of the incident.
     * @return the severity
     */
    public Severity getSeverity(){
        return severity;
    }

    /**
     * Gets the time at which the incident happened.
     * @return the time
     */
    public long getTime(){
        return time;
    }

    /**
     * Gets the type of incident.
     * @return the type
     */
    public Type getType(){
        return type;
    }

    /**
     * Gets the fault that will occur on the
     * drone that is tasked with this incident.
     * @return the fault
     */
    public Fault getFault() {
        return fault;
    }
}
