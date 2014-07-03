package ch.fork.flibeacons.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.FrameLayout;

import com.radiusnetworks.ibeacon.IBeacon;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.InjectView;
import ch.fork.flibeacons.FliBeaconApplication;
import ch.fork.flibeacons.R;
import ch.fork.flibeacons.activities.SettingsActivity;
import ch.fork.flibeacons.events.PictureTakenEvent;
import ch.fork.flibeacons.events.RangeEvent;
import ch.fork.flibeacons.model.Beacon;
import ch.fork.flibeacons.model.Drone;
import ch.fork.flibeacons.model.DroneStore;

public class FliBeaconDroneService extends Service {
    protected static final String TAG = FliBeaconDroneService.class.getSimpleName();

    // Binder given to clients
    private final IBinder mBinder = new FliBeaconBinder();
    @Inject
    FliBeaconApplication fliBeaconApplication;
    @Inject
    Bus bus;
    @InjectView(R.id.camera_preview)
    FrameLayout preview;
    private DroneStore droneStore = new DroneStore();
    private String baseStationUUID;
    private String lastImage;

    public FliBeaconDroneService() {
    }



    @Override
    public void onCreate() {
        super.onCreate();
        FliBeaconApplication fliBeaconApplication = (FliBeaconApplication) getApplication();
        fliBeaconApplication.inject(this);
        bus.register(this);
        baseStationUUID = fliBeaconApplication.getBaseStationUUID();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Subscribe
    public void onPictureTaken(PictureTakenEvent event){
        lastImage = event.getBase64Image();
        Log.d(TAG, "new image with length: " + lastImage.length());
    }

    @Subscribe
    public void onRange(RangeEvent event) {
        final Collection<IBeacon> iBeacons = event.getBeacons();

        final Collection<Drone> currentDrones = getCurrentDrones(iBeacons);
        for (Drone drone : currentDrones) {
            fliBeaconApplication.getSocket().emit("drone", drone.toJSON());
            Log.d(TAG, "Drone in range: " + drone.toJSON());
        }

        final Collection<Drone> leftDrones = getLeftDrones(currentDrones);
        for (Drone drone : leftDrones) {
            fliBeaconApplication.getSocket().emit("drone", drone.toJSON());
            Log.d(TAG, "Drone out of range: " + drone.toJSON());
        }

        droneStore.setNewDrones(currentDrones);
    }

    private Collection<Drone> getCurrentDrones(Collection<IBeacon> iBeacons) {
        Collection<Drone> currentDrones = new ArrayList<Drone>();
        for (IBeacon iBeacon : iBeacons) {
            final String uuid = iBeacon.getProximityUuid();
            Drone.Type type;

            if (droneStore.getDrones().containsKey(uuid)) {
                type = Drone.Type.moved;
            } else {
                type = Drone.Type.entered;
            }

            Beacon beacon = new Beacon(iBeacon.getProximityUuid(), iBeacon.getMajor(), iBeacon.getMinor());
            Drone drone = new Drone(type, getProximity(iBeacon.getProximity()), iBeacon.getAccuracy(), beacon, baseStationUUID, lastImage);
            currentDrones.add(drone);
        }

        return currentDrones;
    }

    private Collection<Drone> getLeftDrones(Collection<Drone> currentDrones) {
        Collection<Drone> leftDrones = droneStore.getLeftDrones(currentDrones);
        for (Drone leftDrone : leftDrones) {
            leftDrone.setType(Drone.Type.left);
            leftDrone.setImage(null); //no image needed for left drones
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
