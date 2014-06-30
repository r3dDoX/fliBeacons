package ch.fork.flibeacons;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.proximity.ibeacon.IBeaconManager;
import com.squareup.otto.Bus;

import java.util.Collection;

import javax.inject.Inject;

import ch.fork.flibeacons.events.RangeEvent;

public class FliBeaconService extends Service implements IBeaconConsumer {
    protected static final String TAG = FliBeaconService.class.getSimpleName();
    @Inject
    FliBeaconApplication fliBeaconApplication;
    @Inject
    Bus bus;
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    public FliBeaconService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FliBeaconApplication fliBeaconApplication = (FliBeaconApplication) getApplication();
        fliBeaconApplication.inject(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        iBeaconManager.unBind(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        iBeaconManager.bind(this);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
        }


    }
}
