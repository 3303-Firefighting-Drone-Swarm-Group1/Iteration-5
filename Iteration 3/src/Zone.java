/**
 * Zone represents a zone in which fires may start.
 * @author Lucas Warburton, 101276823
 */

import java.awt.Point;

public class Zone {
    private Point start;
    private Point end;

    public Zone(int startx, int starty, int endx, int endy){
        start = new Point(Math.min(startx, endx), Math.min(starty, endy));
        end = new Point(Math.max(startx, endx), Math.max(starty, endy));
    }

    public Zone(Point start, Point end){
        this.start = start;
        this.end = end;
    }
    
    /**
     * Gets the coordinates of the start of the zone.
     * @return the coordinates
     */
    public Point getStart(){
        return start;
    }

    /**
     * Gets the coordinates of the end of the zone.
     * @return the coordinate
     */
    public Point getEnd(){
        return end;
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
        if (x >= start.getX() && x <= end.getX()) closestX = x;
        else if (x < start.getX()) closestX = (int)start.getX();
        else closestX = (int)end.getX();

        if (y >= start.getY() && y <= end.getY()) closestY = y;
        else if (y < start.getY()) closestY = (int)start.getY();
        else closestY = (int)end.getY();

        return new Point(closestX, closestY);
    }
}
