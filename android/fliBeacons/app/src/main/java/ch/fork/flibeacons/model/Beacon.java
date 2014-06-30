package ch.fork.flibeacons.model;

import java.util.UUID;

/**
 * Created by lufr on 30.06.2014.
 */
public class Beacon {
    private UUID uuid;
    private int major;
    private int minor;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
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
