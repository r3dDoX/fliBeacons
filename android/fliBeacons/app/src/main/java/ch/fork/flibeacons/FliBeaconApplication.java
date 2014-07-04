package ch.fork.flibeacons;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.JsonElement;
import com.squareup.otto.Bus;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import ch.fork.flibeacons.events.ConnectedEvent;
import ch.fork.flibeacons.events.DisconnectedEvent;
import ch.fork.flibeacons.events.ServerEvent;
import ch.fork.flibeacons.services.FliBeaconDroneService;
import ch.fork.flibeacons.services.FliBeaconLocationService;
import ch.fork.flibeacons.services.FliBeaconRangingService;
import dagger.ObjectGraph;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

/**
 * Created by fork on 30.06.14.
 */
public class FliBeaconApplication extends Application implements IOCallback {

    private static final String TAG = FliBeaconApplication.class.getSimpleName();
    @Inject
    Bus bus;
    private FliBeaconRangingService fliBeaconService;
    private FliBeaconDroneService fliBeaconDroneService;
    private boolean boundDroneService;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
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
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private FliBeaconLocationService fliLocationService;
    private ObjectGraph objectGraph;
    private Handler handler;
    private boolean bound;
    private ServiceConnection beaconServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            FliBeaconRangingService.FliBeaconBinder binder = (FliBeaconRangingService.FliBeaconBinder) service;
            fliBeaconService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
    private ServiceConnection locationServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            FliBeaconLocationService.FliLocationBinder binder = (FliBeaconLocationService.FliLocationBinder) service;
            fliLocationService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
    private SocketIO socket;
    private String baseStationUUID;

    @Override
    public void onCreate() {
        super.onCreate();
        setupDagger();
        bus.register(this);
        handler = new Handler();

        Intent intent = new Intent(this, FliBeaconRangingService.class);
        bindService(intent, beaconServiceConnection, Context.BIND_AUTO_CREATE);
        Intent locationIntent = new Intent(this, FliBeaconLocationService.class);
        bindService(locationIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);

        Intent intentDroneService = new Intent(this, FliBeaconDroneService.class);
        bindService(intentDroneService, droneServiceConnection, Context.BIND_AUTO_CREATE);

        baseStationUUID = UUID.randomUUID().toString();


    }

    public SocketIO getSocket() {
        return socket;
    }

    public void setSocket(SocketIO socket) {
        this.socket = socket;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Unbind from the service
        if (bound) {
            unbindService(beaconServiceConnection);
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

    public FliBeaconLocationService getFliLocationService() {
        return fliLocationService;
    }

    @Override
    public void onDisconnect() {
        Log.i(TAG, "disconnected");
        postEvent(new DisconnectedEvent());

    }

    @Override
    public void onConnect() {
        Log.i(TAG, "connected");
        postEvent(new ConnectedEvent());
    }

    @Override
    public void onMessage(String data, IOAcknowledge ack) {

    }

    @Override
    public void onMessage(JsonElement json, IOAcknowledge ack) {

    }

    @Override
    public void on(String event, IOAcknowledge ack, JsonElement... args) {
        Log.i(TAG, "received event: " + event + " --> " + args);

        postEvent(new ServerEvent(event, args));
    }

    @Override
    public void onError(SocketIOException socketIOException) {

    }

    public String getBaseStationUUID() {
        return baseStationUUID;
    }
}
