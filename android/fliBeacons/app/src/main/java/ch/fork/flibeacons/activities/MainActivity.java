package ch.fork.flibeacons.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.radiusnetworks.ibeacon.IBeacon;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ch.fork.flibeacons.FliBeaconApplication;
import ch.fork.flibeacons.R;
import ch.fork.flibeacons.events.PictureTakenEvent;
import ch.fork.flibeacons.events.RangeEvent;
import ch.fork.flibeacons.services.FliBeaconRangingService;
import ch.fork.flibeacons.util.BitmapScaler;
import ch.fork.flibeacons.util.ShowCamera;


public class MainActivity extends BaseActivity {


    protected static final String TAG = "RangingActivity";
    @InjectView(R.id.distanceTextView)
    TextView distanceTextView;
    @InjectView(R.id.rangeTextView)
    TextView rangeTextView;
    @InjectView(R.id.camera_preview)
    FrameLayout preview;
    @InjectView(R.id.startRanging)
    Button startRangingButton;
    @InjectView(R.id.stopRanging)
    Button stopRangingButton;
    @Inject
    FliBeaconApplication fliBeaconApplication;

    private FliBeaconRangingService fliBeaconService;

    private boolean bound;
    private Camera camera;

    private static final int IMAGE_HEIGHT = 400;
    public static final int CAMERA_ROTATION = 90;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            FliBeaconRangingService.FliBeaconBinder binder = (FliBeaconRangingService.FliBeaconBinder) service;
            fliBeaconService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    private final Camera.PictureCallback capturedImage = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            long start = System.currentTimeMillis();

            //scale bitmap
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            Bitmap scaledBitmap = BitmapScaler.scaleToFitHeight(BitmapFactory.decodeStream(bis), IMAGE_HEIGHT);

            //rotate image
            Matrix matrix = new Matrix();
            matrix.postRotate(CAMERA_ROTATION);
            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, false);

            //compress and convert bitmap to byte[]
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 65, byteArrayOutputStream);
            byte[] compressData = byteArrayOutputStream.toByteArray();

            //convert byte[] to base64 string
            String imageData = Base64.encodeToString(compressData, Base64.NO_WRAP);

            //post event to bus
            fliBeaconApplication.postEvent(new PictureTakenEvent(imageData));
            Log.i(TAG, "time to compute: " + (System.currentTimeMillis() - start));
            pictureProcessing = false;
        }
    };
    private boolean pictureProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        startRangingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fliBeaconService.startBeaconRanging();
                //startCapturing();
            }
        });

        stopRangingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distanceTextView.setText("");
                rangeTextView.setText("");
                fliBeaconService.stopBeaconRanging();
                //stopCapturing();
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void onResume() {
        super.onResume();
        try {
            camera = Camera.open();
            if (camera != null) {
                camera.setDisplayOrientation(CAMERA_ROTATION);

                preview.removeAllViews();
                preview.addView(new ShowCamera(getApplicationContext(), camera));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, FliBeaconRangingService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Subscribe
    public void onRange(RangeEvent event) {
        final Collection<IBeacon> iBeacons = event.getBeacons();
        if (iBeacons.size() > 0) {

            if (!pictureProcessing) {
                pictureProcessing = true;
                AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        //CAPTURE IMAGE
                        if (camera != null) {
                            try {
                                Log.i(TAG, "taking picture");
                                Camera.Parameters param = camera.getParameters();
                                param.setPictureSize(IMAGE_HEIGHT, IMAGE_HEIGHT);
                                camera.startPreview();
                                camera.takePicture(null, null, capturedImage);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to take picture");
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }
                };
                asyncTask.execute();
            }

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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
