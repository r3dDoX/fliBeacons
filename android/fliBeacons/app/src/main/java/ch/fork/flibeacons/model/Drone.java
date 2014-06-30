package ch.fork.flibeacons.model;

import org.json.JSONObject;

/**
 * Created by lufr on 30.06.2014.
 */
public class Drone {

    private enum Type{
        entered, left, moved
    }

    private enum Proximity{
        near, far, immediate
    }

    private Type type;
    private Proximity proximity;
    private double distance;
    private Beacon beacon;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Proximity getProximity() {
        return proximity;
    }

    public void setProximity(Proximity proximity) {
        this.proximity = proximity;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public void setBeacon(Beacon beacon) {
        this.beacon = beacon;
    }
}
