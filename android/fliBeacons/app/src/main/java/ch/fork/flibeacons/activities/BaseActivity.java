package ch.fork.flibeacons.activities;

import android.app.Activity;
import android.os.Bundle;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import ch.fork.flibeacons.FliBeaconApplication;

/**
 * Created by fork on 30.05.14.
 */
public abstract class BaseActivity extends Activity {

    @Inject
    Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FliBeaconApplication fliBeaconApplication = (FliBeaconApplication) getApplication();
        fliBeaconApplication.inject(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
    }
}
