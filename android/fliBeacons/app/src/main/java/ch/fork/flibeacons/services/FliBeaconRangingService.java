package ch.fork.flibeacons.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.proximity.ibeacon.IBeaconManager;
import com.squareup.otto.Bus;

import java.util.Collection;

import javax.inject.Inject;

import ch.fork.flibeacons.FliBeaconApplication;
import ch.fork.flibeacons.events.RangeEvent;

public class FliBeaconRangingService extends Service implements IBeaconConsumer {
    protected static final String TAG = FliBeaconRangingService.class.getSimpleName();

    // Binder given to clients
    private final IBinder mBinder = new FliBeaconBinder();
    private final Region region;
    @Inject
    FliBeaconApplication fliBeaconApplication;
    @Inject
    Bus bus;
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    public FliBeaconRangingService() {
        region = new Region("myRangingUniqueId", null, null, null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FliBeaconApplication fliBeaconApplication = (FliBeaconApplication) getApplication();
        fliBeaconApplication.inject(this);
        iBeaconManager.bind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        iBeaconManager.unBind(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void startBeaconRanging() {
        try {
            iBeaconManager.startRangingBeaconsInRegion(region);
            iBeaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
        }
    }

    public void stopBeaconRanging() {

        try {
            iBeaconManager.stopMonitoringBeaconsInRegion(region);
            iBeaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
        }
    }

    @Override
    public void onIBeaconServiceConnect() {

        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<IBeacon> iBeacons, final Region region) {
                fliBeaconApplication.postEvent(new RangeEvent(iBeacons, region));
            }
        });


        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an iBeacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an iBeacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing iBeacons: " + state);
            }
        });


    }

    public class FliBeaconBinder extends Binder {
        public FliBeaconRangingService getService() {
            return FliBeaconRangingService.this;
        }
    }
}
