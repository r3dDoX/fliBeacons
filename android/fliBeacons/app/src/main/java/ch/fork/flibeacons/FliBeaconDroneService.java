package ch.fork.flibeacons;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import ch.fork.flibeacons.events.RangeEvent;
import ch.fork.flibeacons.model.Beacon;
import ch.fork.flibeacons.model.Drone;
import ch.fork.flibeacons.model.DroneStore;

public class FliBeaconDroneService extends Service {
    protected static final String TAG = FliBeaconDroneService.class.getSimpleName();

    // Binder given to clients
    private final IBinder mBinder = new FliBeaconBinder();
    private DroneStore droneStore = new DroneStore();
    @Inject
    FliBeaconApplication fliBeaconApplication;
    @Inject
    Bus bus;

    public FliBeaconDroneService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FliBeaconApplication fliBeaconApplication = (FliBeaconApplication) getApplication();
        fliBeaconApplication.inject(this);
        bus.register(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        bus.unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Subscribe
    public void onRange(RangeEvent event) {
        final Collection<IBeacon> iBeacons = event.getBeacons();
        final Collection<Drone> currentDrones = getCurrentDrones(iBeacons);
        final Collection<Drone> leftDrones = getLeftDrones(currentDrones);
        droneStore.setNewDrones(currentDrones);
        //SEND DRONES TO SERVER
    }

    private Collection<Drone> getCurrentDrones(Collection<IBeacon> iBeacons){
        Collection<Drone> currentDrones = new ArrayList<Drone>();
        for(IBeacon iBeacon : iBeacons){
            final String uuid = iBeacon.getProximityUuid();
            Drone.Type type;

            if(droneStore.getDrones().containsKey(uuid)){
                type = Drone.Type.moved;
            }else{
                type = Drone.Type.entered;
            }

            Beacon beacon = new Beacon(iBeacon.getProximityUuid(), iBeacon.getMajor(), iBeacon.getMinor());
            Drone drone = new Drone(type, getProximity(iBeacon.getProximity()), iBeacon.getAccuracy(), beacon);
            currentDrones.add(drone);

            Log.d(TAG, "Drone in range: " + drone.toJSON());
        }

        return currentDrones;
    }

    private Collection<Drone> getLeftDrones(Collection<Drone> currentDrones){
        Collection<Drone> leftDrones = droneStore.getLeftDrones(currentDrones);
        for(Drone leftDrone : leftDrones){
            leftDrone.setType(Drone.Type.left);
            Log.d(TAG, "Drone out of range: " + leftDrone.toJSON());
        }

        return leftDrones;
    }

    private Drone.Proximity getProximity(int iBeaconProximity) {
        Drone.Proximity proximity;
        if (iBeaconProximity == IBeacon.PROXIMITY_FAR) {
            proximity = Drone.Proximity.far;
        } else if (iBeaconProximity == IBeacon.PROXIMITY_NEAR) {
            proximity = Drone.Proximity.near;
        } else if (iBeaconProximity == IBeacon.PROXIMITY_IMMEDIATE) {
            proximity = Drone.Proximity.immediate;
        } else {
            throw new IllegalStateException("Unknown Proximity " + iBeaconProximity);
        }
        return proximity;
    }

    public class FliBeaconBinder extends Binder {
        public FliBeaconDroneService getService() {
            return FliBeaconDroneService.this;
        }
    }
}
