package ch.fork.flibeacons.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.UUID;

import ch.fork.flibeacons.FliBeaconApplication;
import ch.fork.flibeacons.activities.SettingsActivity;
import ch.fork.flibeacons.model.BaseStation;

public class FliBeaconLocationService extends Service implements LocationListener {

    private static final String TAG = FliBeaconLocationService.class.getSimpleName();
    // Binder given to clients
    private final IBinder mBinder = new FliLocationBinder();
    private LocationManager locationManager;
    private FliBeaconApplication fliBeaconApplication;
    private Gson gson;
    private SharedPreferences sharedPref;
    private String baseStationUUID;

    public FliBeaconLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        fliBeaconApplication = (FliBeaconApplication) getApplication();
        fliBeaconApplication.inject(this);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(fliBeaconApplication);

        baseStationUUID = fliBeaconApplication.getBaseStationUUID();
        gson = new Gson();

    }

    private String getBaseStationNameFromSettings() {
        return sharedPref.getString(SettingsActivity.KEY_BASESTATION_NAME, UUID.randomUUID().toString());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: " + location);

        sendLocationUpdate(location);
    }

    private void sendLocationUpdate(Location location) {
        BaseStation baseStation = new BaseStation();

        String baseStationNameFromSettings = getBaseStationNameFromSettings();
        baseStation.setId(baseStationUUID);
        baseStation.setName(baseStationNameFromSettings);
        if(location != null) {
            baseStation.setLat(location.getLatitude());
            baseStation.setLng(location.getLongitude());
        } else {
            baseStation.setLat(47.670162);
            baseStation.setLng(8.95015);
        }


        fliBeaconApplication.getSocket().emit("baseStation", gson.toJson(baseStation));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: " + provider);

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: " + provider);
    }

    public void start() {

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, this);

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        sendLocationUpdate(lastKnownLocation);
    }

    public void stop() {
        locationManager.removeUpdates(this);
    }

    public class FliLocationBinder extends Binder {
        public FliBeaconLocationService getService() {
            return FliBeaconLocationService.this;
        }
    }
}
