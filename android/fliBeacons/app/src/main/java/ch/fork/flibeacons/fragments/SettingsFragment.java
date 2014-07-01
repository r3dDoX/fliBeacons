package ch.fork.flibeacons.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import ch.fork.flibeacons.R;

/**
 * Created by fork on 4/24/14.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);
    }
}
