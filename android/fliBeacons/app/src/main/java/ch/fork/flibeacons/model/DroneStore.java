package ch.fork.flibeacons.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by lufr on 30.06.2014.
 */
public class DroneStore {

    private Map<String, Drone> drones = new HashMap<String, Drone>();

    public Map<String, Drone> getDrones() {
        return drones;
    }

    private void clearDrones(){
        drones.clear();
    }

    private void addDrone(Drone drone){
        String uuid = drone.getBeacon().getUuid();
        if(!drones.containsKey(uuid)){
            drones.put(uuid, drone);
        }
    }

    public void setNewDrones(Collection<Drone> newDrones){
        clearDrones();
        for(Drone drone : newDrones){
            addDrone(drone);
        }
    }

    public Collection<Drone> getLeftDrones(Collection<Drone> newDrones) {
        Collection<Drone> oldDrones = new ArrayList<Drone>(drones.values());
        oldDrones.removeAll(newDrones);
        return oldDrones;
    }
}
