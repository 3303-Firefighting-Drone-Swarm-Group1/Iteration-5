/**
 * IncidentMessage represents an message containing information about an incident.
 * @author Lucas Warburton, 101276823
 */

import java.sql.Time;

import java.awt.Point;

public class IncidentMessage {
    private Incident.Severity severity;
    private Point start;
    private Point end;
    private Time time;
    private Incident.Type type;

    public IncidentMessage(Incident.Severity severity, Point start, Point end, Time time, Incident.Type type){
        this.severity = severity;
        this.start = start;
        this.end = end;
        this.time = time;
        this.type = type;
    }

    /**
     * Gets the severity of the incident
     * @return the severity
     */
    public Incident.Severity getSeverity(){
        return severity;
    }

    /**
     * Gets the x coordinate of the start of the zone.
     * @return the x coordinate
     */
    public int getStartX(){
        return (int)start.getX();
    }

    /**
     * Gets the y coordinate of the start of the zone.
     * @return the y coordinate
     */
    public int getStartY(){
        return (int)start.getY();
    }

    /**
     * Gets the x coordinate of the end of the zone.
     * @return the x coordinate
     */
    public int getEndX(){
        return (int)end.getX();
    }

    /**
     * Gets the y coordinate of the end of the zone.
     * @return the y coordinate
     */
    public int getEndY(){
        return (int)end.getY();
    }

    /**
     * Gets a point which is the closest point to a given set of coordinates.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the closest point
     */
    public Point getClosestPoint(int x, int y){
        int closestX, closestY;
        if (x >= getStartX() && x <= getEndX()) closestX = x;
        else if (x < getStartX()) closestX = getStartX();
        else closestX = getEndX();

        if (y >= getStartY() && y <= getEndY()) closestY = y;
        else if (y < getStartY()) closestY = getStartY();
        else closestY = getEndY();

        return new Point(closestX, closestY);
    }

    /**
     * Gets the time at which the incident happened.
     * @return the time
     */
    public Time getTime(){
        return time;
    }

    /**
     * Gets the type of the incident.
     * @return the type
     */
    public Incident.Type getType(){
        return type;
    }

}

