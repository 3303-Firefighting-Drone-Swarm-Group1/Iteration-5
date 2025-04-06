import java.util.ArrayList;

public class Map {
    private ArrayList<Fire> fires;
    private ArrayList<Drone> drones;
    private long lastTime = -1;
    
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

    public void updatePositions(long time){
        if (lastTime == -1){
            lastTime = time;
            return;
        } else {
            long deltaT = time - lastTime;
            for (Drone drone: drones){
                drone.setLocation(drone.getX() + deltaT * drone.getVX() / 1000.0, drone.getY() + deltaT * drone.getVY() / 1000.0);
            }
            lastTime = time;
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
