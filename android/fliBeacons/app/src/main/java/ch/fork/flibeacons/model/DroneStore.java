package ch.fork.flibeacons.model;

import java.util.Map;
import java.util.UUID;

/**
 * Created by lufr on 30.06.2014.
 */
public class DroneStore {

    private Map<UUID, Drone> drones;


    public Map<UUID, Drone> getDrones() {
        return drones;
    }

    public void clearDrones(){
        drones.clear();
    }

    public void addDrone(Drone drone){
        UUID uuid = drone.getBeacon().getUuid();
        if(!drones.containsKey(uuid)){
            drones.put(uuid, drone);
        }
    }
}
