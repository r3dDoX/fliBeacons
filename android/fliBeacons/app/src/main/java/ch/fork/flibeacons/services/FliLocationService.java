package ch.fork.flibeacons.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import ch.fork.flibeacons.FliBeaconService;

public class FliLocationService extends Service {

    // Binder given to clients
    private final IBinder mBinder = new FliLocationBinder();

    public FliLocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class FliLocationBinder extends Binder {
        public FliLocationService getService() {
            return FliLocationService.this;
        }
    }

}
