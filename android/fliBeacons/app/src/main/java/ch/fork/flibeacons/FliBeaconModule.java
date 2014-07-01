package ch.fork.flibeacons;

import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import javax.inject.Singleton;

import ch.fork.flibeacons.activities.MainActivity;
import ch.fork.flibeacons.activities.SettingsActivity;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fork on 30.06.14.
 */
@Module(
        injects = {FliBeaconApplication.class, MainActivity.class, FliBeaconService.class, StartActivity.class, SettingsActivity.class, FliBeaconDroneService.class},
        library = true
)
public class FliBeaconModule {

    private final Context context;
    private FliBeaconApplication fliBeaconApplication;

    FliBeaconModule(FliBeaconApplication adHocRailwayApplication) {
        this.fliBeaconApplication = adHocRailwayApplication;
        this.context = fliBeaconApplication.getApplicationContext();
    }

    @Provides
    public FliBeaconApplication providesFliBeaconApplication() {
        return this.fliBeaconApplication;
    }

    @Provides
    @Singleton
    public Bus providesEventBus() {
        return new Bus(ThreadEnforcer.ANY);
    }
}
