/**
 * An entity object used to organize the information to be displayed on the GUI
 */

import java.util.ArrayList;

public class Map {
    private ArrayList<Fire> fires;
    private ArrayList<Drone> drones;
    
    public Map(){
        fires = new ArrayList<>();
        drones = new ArrayList<>();
    }

    /**
     * Adds a fire to the map
     * @param fire the fire to be added
     */
    public void addFire(Fire fire){
        fires.add(fire);
    }

    /**
     * Adds a drone to the map
     * @param drone the drone to be added
     */
    public void addDrone(Drone drone){
        drones.add(drone);
    }

    /**
     * Updates positions of all drones, and removes all extinguished fires
     * assuming it has been 1 second since the last update
     */
    public void updatePositions(){
        for (int i = 0; i < drones.size(); i++) {
            Drone drone = drones.get(i);
            drone.setLocation(drone.getX() + drone.getVX(), drone.getY() + drone.getVY());
        }

        for (int i = 0; i < fires.size(); i++) {
            if (fires.get(i).getWater() <= 0) fires.remove(i);
        }
    }

    /**
     * Removes a fire from the map
     * @param fire the fire to be removed
     */
    public void removeFire(Fire fire){
        fires.removeIf(f -> f == fire);
    }

    /**
     * Removes a drone from the map
     * @param port the port of the drone to be removed
     */
    public void removeDrone(int port){
        drones.removeIf(drone -> drone.getPort() == port);
    }

    /**
     * Gets the list of fires
     * @return the list of fires
     */
    public ArrayList<Fire> getFires(){
        return fires;
    }

    /**
     * Gets the list of drones
     * @return the list of drones
     */
    public ArrayList<Drone> getDrones(){
        return drones;
    }
}
