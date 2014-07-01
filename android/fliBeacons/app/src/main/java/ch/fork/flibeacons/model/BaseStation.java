package ch.fork.flibeacons.model;

/**
 * Created by fork on 01.07.14.
 */
public class BaseStation {

    private String name;
    private double lat;
    private double lng;

    public BaseStation() {

    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
