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

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.fork.flibeacons.events.ServerEvent;
import ch.fork.flibeacons.services.FliLocationService;
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
    private FliBeaconService fliBeaconService;
    private FliBeaconDroneService fliBeaconDroneService;
    private boolean boundDroneService;
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
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private FliLocationService fliLocationService;
    private ObjectGraph objectGraph;
    private Handler handler;
    private boolean bound;
    private ServiceConnection beaconServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            FliBeaconService.FliBeaconBinder binder = (FliBeaconService.FliBeaconBinder) service;
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
            FliLocationService.FliLocationBinder binder = (FliLocationService.FliLocationBinder) service;
            fliLocationService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
    private SocketIO socket;

    @Override
    public void onCreate() {
        super.onCreate();
        setupDagger();
        bus.register(this);
        handler = new Handler();

        Intent intent = new Intent(this, FliBeaconService.class);
        bindService(intent, beaconServiceConnection, Context.BIND_AUTO_CREATE);
        Intent locationIntent = new Intent(this, FliLocationService.class);
        bindService(locationIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);

        Intent intentDroneService = new Intent(this, FliBeaconDroneService.class);
        bindService(intentDroneService, droneServiceConnection, Context.BIND_AUTO_CREATE);

        try {
            socket = new SocketIO("http://flibeacons1.ngrok.com/");
            socket.connect(this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public SocketIO getSocket() {
        return socket;
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

    public FliLocationService getFliLocationService() {
        return fliLocationService;
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onConnect() {
        Log.i(TAG, "connected");
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
}
