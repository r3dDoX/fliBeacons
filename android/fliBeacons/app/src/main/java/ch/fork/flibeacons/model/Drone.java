package ch.fork.flibeacons.model;

import com.google.gson.Gson;

/**
 * Created by lufr on 30.06.2014.
 */
public class Drone {

    private Type type;
    private Proximity proximity;
    private double distance;
    private Beacon beacon;
    private String baseStationId;
    private String image;

    public Drone(Type type, Proximity proximity, double distance, Beacon beacon, String baseStationId, String image) {
        this.type = type;
        this.proximity = proximity;
        this.distance = distance;
        this.beacon = beacon;
        this.baseStationId = baseStationId;
        this.image = image;
    }

    public String getBaseStationId() {
        return baseStationId;
    }

    public void setBaseStationId(String baseStationId) {
        this.baseStationId = baseStationId;
    }

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

    public String getImage(){
        return image;
    }

    public void setImage(String image){
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Drone drone = (Drone) o;

        if (!beacon.getUuid().equals(drone.beacon.getUuid())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return beacon.getUuid().hashCode();
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }

    public enum Type {
        entered, left, moved
    }

    public enum Proximity {
        near, far, immediate
    }
}
