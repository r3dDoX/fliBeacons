package ch.fork.flibeacons;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.radiusnetworks.ibeacon.IBeacon;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Collection;

import javax.inject.Inject;

import ch.fork.flibeacons.events.RangeEvent;

public class FliBeaconDroneService extends Service {

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
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Subscribe
    public void onRange(RangeEvent event) {
        final Collection<IBeacon> iBeacons = event.getBeacons();
    }

    public class FliBeaconBinder extends Binder {
        public FliBeaconService getService() {
            return FliBeaconDroneService.this;
        }
    }
}
