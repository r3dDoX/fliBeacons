package ch.fork.flibeacons.events;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.Region;

import java.util.Collection;

/**
 * Created by fork on 30.06.14.
 */
public class RangeEvent {
    private Collection<IBeacon> iBeacons;
    private Region region;

    public RangeEvent(Collection<IBeacon> iBeacons, Region region) {
        this.iBeacons = iBeacons;
        this.region = region;
    }

    public Collection<IBeacon> getBeacons() {
        return iBeacons;
    }

    public Region getRegion() {
        return region;
    }
}
