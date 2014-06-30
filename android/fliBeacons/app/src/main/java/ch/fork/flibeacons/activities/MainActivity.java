package ch.fork.flibeacons.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.radiusnetworks.ibeacon.IBeacon;
import com.squareup.otto.Subscribe;

import java.util.Collection;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ch.fork.flibeacons.FliBeaconApplication;
import ch.fork.flibeacons.R;
import ch.fork.flibeacons.events.RangeEvent;


public class MainActivity extends BaseActivity {

    protected static final String TAG = "RangingActivity";
    @InjectView(R.id.distanceTextView)
    TextView distanceTextView;
    @InjectView(R.id.rangeTextView)
    TextView rangeTextView;

    @Inject
    FliBeaconApplication fliBeaconApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Subscribe
    public void onRange(RangeEvent event) {
        final Collection<IBeacon> iBeacons = event.getBeacons();
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
