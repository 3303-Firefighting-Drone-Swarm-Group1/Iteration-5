import java.util.ArrayList;

public class Map {
    private ArrayList<Fire> fires;
    private ArrayList<Drone> drones;
    
    public Map(){
        fires = new ArrayList<>();
        drones = new ArrayList<>();
    }

    public void addFire(Fire fire){
        fires.add(fire);
    }

    public void addDrone(Drone drone){
        drones.add(drone);
    }

    public void updatePositions(){
        for (Drone drone: drones){
            drone.setLocation(drone.getX() + drone.getVX(), drone.getY() + drone.getVY());
        }

        for (Fire fire: fires){
            if (fire.getWater() <= 0) fires.remove(fire);
        }
    }

    public void removeFire(Fire fire){
        fires.removeIf(f -> f == fire);
    }

    public void removeDrone(int port){
        drones.removeIf(drone -> drone.getPort() == port);
    }

    public ArrayList<Fire> getFires(){
        return fires;
    }

    public ArrayList<Drone> getDrones(){
        return drones;
    }
}
