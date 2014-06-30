package ch.fork.flibeacons;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.squareup.otto.Bus;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.fork.flibeacons.services.FliLocationService;
import dagger.ObjectGraph;

/**
 * Created by fork on 30.06.14.
 */
public class FliBeaconApplication extends Application {

    @Inject
    Bus bus;
    private FliBeaconService fliBeaconService;

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
    private Socket socket;

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


        try {
            socket = IO.socket("http://flibeacons1.ngrok.com");
        } catch (URISyntaxException e) {
        }

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Unbind from the service
        if (bound) {
            unbindService(beaconServiceConnection);
            bound = false;
        }
    }

    public Socket getSocket() {
        return socket;
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
