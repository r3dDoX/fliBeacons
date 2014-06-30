package ch.fork.flibeacons;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.proximity.ibeacon.IBeaconManager;

import java.util.Collection;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity implements IBeaconConsumer {

    protected static final String TAG = "RangingActivity";
    @InjectView(R.id.distanceTextView)
    TextView distanceTextView;
    @InjectView(R.id.rangeTextView)
    TextView rangeTextView;
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iBeaconManager.bind(this);

        ButterKnife.inject(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iBeaconManager.unBind(this);
    }

    @Override
    public void onIBeaconServiceConnect() {

        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<IBeacon> iBeacons, Region region) {
                distanceTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (iBeacons.size() > 0) {
                            IBeacon iBeacon = iBeacons.iterator().next();
                            Log.i(TAG, "The first iBeacon I see is about " + iBeacon.getAccuracy() + " meters away.");
                            distanceTextView.setText("" + iBeacon.getAccuracy());
                            int proximity = iBeacon.getProximity();
                            if (proximity == IBeacon.PROXIMITY_FAR) {
                                rangeTextView.setText("FAR");
                            } else if (proximity == IBeacon.PROXIMITY_NEAR) {
                                rangeTextView.setText("NEAR");
                            } else if (proximity == IBeacon.PROXIMITY_IMMEDIATE) {
                                rangeTextView.setText("IMMEDIATE");
                            } else {
                                rangeTextView.setText("UNKNOWN");
                            }
                        }
                    }
                });

            }
        });

        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }

        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an iBeacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an iBeacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing iBeacons: " + state);
            }
        });

        try {
            iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
