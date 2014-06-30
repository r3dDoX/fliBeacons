package ch.fork.flibeacons;

import android.app.Application;
import android.os.Handler;

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
    private ObjectGraph objectGraph;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        setupDagger();
        bus.register(this);
        handler = new Handler();
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
