package ch.fork.flibeacons;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import com.squareup.otto.Bus;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;

/**
 * Created by fork on 30.06.14.
 */
public class FliBeaconApplication extends Application {

    @Inject
    Bus bus;
    private FliBeaconService fliBeaconService;
    private FliBeaconDroneService fliBeaconDroneService;
    private ObjectGraph objectGraph;
    private Handler handler;
    private boolean bound;
    private boolean boundDroneService;
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            FliBeaconService.FliBeaconBinder binder = (FliBeaconService.FliBeaconBinder) service;
            fliBeaconService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection droneServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            FliBeaconDroneService.FliBeaconBinder binder = (FliBeaconDroneService.FliBeaconBinder) service;
            fliBeaconDroneService = binder.getService();
            boundDroneService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            boundDroneService = false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        setupDagger();
        bus.register(this);
        handler = new Handler();

        Intent intent = new Intent(this, FliBeaconService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Intent intentDroneService = new Intent(this, FliBeaconDroneService.class);
        bindService(intentDroneService, droneServiceConnection, Context.BIND_AUTO_CREATE);

        //startService(new Intent(this, FliBeaconService.class));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Unbind from the service
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
        if (boundDroneService) {
            unbindService(droneServiceConnection);
            boundDroneService = false;
        }
    }

    protected void setupDagger() {
        Object[] modules = getModules().toArray();
        objectGraph = ObjectGraph.create(modules);
        objectGraph.inject(this);
    }

    protected List<Object> getModules() {
        return Arrays.<Object>asList(
                new FliBeaconModule(this)
        );
    }

    public <T> T inject(T obj) {
        return objectGraph.inject(obj);
    }

    public void postEvent(final Object event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                bus.post(event);
            }
        });
    }

}
