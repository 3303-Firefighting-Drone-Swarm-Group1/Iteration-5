/**
 * Incident represents an incident in the Firefighting Drone Swarm system.
 * @author Lucas Warburton, 101276823
 */

 import java.sql.Time;

public class Incident {
    public enum Severity {
        LOW,
        MODERATE,
        HIGH
    }

    public enum Type {
        FIRE_DETECTED,
        DRONE_REQUEST
    }

    private Time time;
    private int id;
    private Severity severity;
    private Type type;
    
    public Incident(int hour, int minute, int second, int id, Severity severity, Type type){
        time = new Time(((hour* 60 + minute)* 60 + second) * 1000);
        this.id = id;
        this.severity = severity;
        this.type = type;
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
    public Time getTime(){
        return time;
    }

    /**
     * Gets the type of incident.
     * @return the type
     */
    public Type getType(){
        return type;
    }
}
