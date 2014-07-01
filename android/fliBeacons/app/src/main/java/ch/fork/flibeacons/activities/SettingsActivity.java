package ch.fork.flibeacons.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import ch.fork.flibeacons.FliBeaconApplication;
import ch.fork.flibeacons.fragments.SettingsFragment;

public class SettingsActivity extends PreferenceActivity {

    public static final String KEY_BASESTATION_UUID = "\"baseStationUUID\"";
    public static final String KEY_BASESTATION_NAME = "baseStationName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FliBeaconApplication fliBeaconApplication = (FliBeaconApplication) getApplication();
        fliBeaconApplication.inject(this);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
