package ch.fork.flibeacons;

import android.app.Application;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

/**
 * Created by fork on 30.06.14.
 */
public class FliBeaconApplication extends Application {

    private ObjectGraph objectGraph;

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


}
