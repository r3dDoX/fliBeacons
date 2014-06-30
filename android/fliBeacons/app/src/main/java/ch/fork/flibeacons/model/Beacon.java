package ch.fork.flibeacons.model;

import java.util.UUID;

/**
 * Created by lufr on 30.06.2014.
 */
public class Beacon {
    private String uuid;
    private int major;
    private int minor;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }
}
