package ch.fork.flibeacons.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.radiusnetworks.ibeacon.IBeacon;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ch.fork.flibeacons.FliBeaconApplication;
import ch.fork.flibeacons.R;
import ch.fork.flibeacons.events.DisconnectedEvent;
import ch.fork.flibeacons.events.PictureTakenEvent;
import ch.fork.flibeacons.events.RangeEvent;
import ch.fork.flibeacons.events.ServerEvent;
import ch.fork.flibeacons.model.BaseStation;
import ch.fork.flibeacons.services.FliBeaconRangingService;
import ch.fork.flibeacons.util.ShowCamera;


public class MainActivity extends BaseActivity {


    public static final int CAMERA_ROTATION = 90;
    protected static final String TAG = "RangingActivity";
    private static final int IMAGE_HEIGHT = 400;
    @InjectView(R.id.uuid_range_sv)
    ScrollView uuidRangeSV;
    @InjectView(R.id.layout_container)
    LinearLayout layoutContainer;
    @InjectView(R.id.camera_preview)
    FrameLayout preview;
    @InjectView(R.id.startRanging)
    Button startRangingButton;
    @InjectView(R.id.stopRanging)
    Button stopRangingButton;
    @InjectView(R.id.log)
    TextView logTextview;
    @Inject
    FliBeaconApplication fliBeaconApplication;
    private FliBeaconRangingService fliBeaconService;
    private boolean bound;
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
    private Camera camera;
    private MediaPlayer activeBaseStationSound;
    private boolean pictureProcessing = false;
    private final Camera.PictureCallback capturedImage = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            long start = System.currentTimeMillis();

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            //rotate image
            Matrix matrix = new Matrix();
            matrix.postRotate(CAMERA_ROTATION);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

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
    private boolean isBaseStationActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        startRangingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uuidRangeSV.removeAllViews();
                startRangingButton.setEnabled(false);
                stopRangingButton.setEnabled(true);
                fliBeaconService.startBeaconRanging();

            }
        });

        stopRangingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uuidRangeSV.removeAllViews();
                startRangingButton.setEnabled(true);
                stopRangingButton.setEnabled(false);
                fliBeaconService.stopBeaconRanging();
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    protected void onResume() {
        super.onResume();
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
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

        activeBaseStationSound = MediaPlayer.create(getApplicationContext(), R.raw.basestation);
        activeBaseStationSound.setLooping(true);
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
    public void onDisconnectedEvent(DisconnectedEvent event) {
        logTextview.setText("DISCONNECTED!!!!!!!!");
        layoutContainer.setBackgroundColor(getResources().getColor(R.color.disconnected));
    }


    @Subscribe
    public void onServerEvent(final ServerEvent event) {
        logTextview.post(new Runnable() {
            @Override
            public void run() {
                if (event.getEvent().equals("drone")) {
                    return;
                }
                Log.i(TAG, "Got server event " + event.getEvent());
                logTextview.setText(event.getEvent());
                if (event.getEvent().equals("activate")) {
                    isBaseStationActive = true;
                    BaseStation activeBaseStation = new Gson().fromJson(event.getArgs()[0], BaseStation.class);
                    if (activeBaseStation.getId().equals(fliBeaconApplication.getBaseStationUUID())) {
                        Log.i(TAG, "This is the active base station!");
                        activeBaseStationSound.start();
                        layoutContainer.setBackgroundColor(getResources().getColor(R.color.station_activated));
                    } else {
                        Log.i(TAG, "Not the active base station!");
                        isBaseStationActive = false;
                        activeBaseStationSound.stop();

                        activeBaseStationSound = MediaPlayer.create(getApplicationContext(), R.raw.basestation);
                        activeBaseStationSound.setLooping(true);
                        layoutContainer.setBackgroundColor(getResources().getColor(R.color.station_deactivated));
                    }
                } else if (event.getEvent().equals("finished")) {
                    Log.i(TAG, "Game is finished");
                    isBaseStationActive = false;
                    activeBaseStationSound.stop();


                    activeBaseStationSound = MediaPlayer.create(getApplicationContext(), R.raw.basestation);
                    activeBaseStationSound.setLooping(true);
                    layoutContainer.setBackgroundColor(getResources().getColor(R.color.station_offline));
                }
            }
        });

    }

    @Subscribe
    public void onRange(RangeEvent event) {
        final Collection<IBeacon> iBeacons = event.getBeacons();
        if (iBeacons.size() > 0) {

            if (!pictureProcessing && isBaseStationActive) {
                pictureProcessing = true;
                AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        //CAPTURE IMAGE
                        if (camera != null) {
                            try {
                                Log.i(TAG, "taking picture");

                                Camera.Parameters param = camera.getParameters();
                                List<Camera.Size> sizes = param.getSupportedPictureSizes();
                                // Set pre lowest possible picture size
                                Camera.Size mSize = sizes.get(sizes.size() - 2);
                                param.setPictureSize(mSize.width, mSize.height);
                                camera.setParameters(param);

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

            uuidRangeSV.removeAllViews();
            for (IBeacon iBeacon : iBeacons) {
                View row = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.uuid_range_row, null);
                ((TextView) row.findViewById(R.id.uuidTextView)).setText(iBeacon.getProximityUuid());
                ((TextView) row.findViewById(R.id.rangeTextView)).setText(getRangeFromProximity(iBeacon));
                uuidRangeSV.addView(row);
            }
        }
    }

    private String getRangeFromProximity(IBeacon iBeacon) {
        String range = "UNKNOWN";
        int proximity = iBeacon.getProximity();
        if (proximity == IBeacon.PROXIMITY_FAR) {
            range = "FAR";
        } else if (proximity == IBeacon.PROXIMITY_NEAR) {
            range = "NEAR";
        } else if (proximity == IBeacon.PROXIMITY_IMMEDIATE) {
            range = "IMMEDIATE";
        }
        return range;
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
