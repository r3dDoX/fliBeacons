package ch.fork.flibeacons.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;

import java.net.MalformedURLException;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ch.fork.flibeacons.FliBeaconApplication;
import ch.fork.flibeacons.R;
import ch.fork.flibeacons.model.Ready;
import io.socket.SocketIO;

public class StartActivity extends BaseActivity {

    private static final String TAG = StartActivity.class.getSimpleName();

    @InjectView(R.id.registerAndStart)
    Button registerAndStartButton;
    @InjectView(R.id.disconnect)
    Button disconnectButton;

    @Inject
    FliBeaconApplication fliBeaconApplication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ButterKnife.inject(this);

        registerAndStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(StartActivity.this);
                    String url = defaultSharedPreferences.getString(SettingsActivity.KEY_URL, "http://flibeacons.ngrok.com");
                    Log.i(TAG, "Connecting to URL: " + url);
                    SocketIO socket = new SocketIO(url);
                    fliBeaconApplication.setSocket(socket);
                    socket.connect(fliBeaconApplication);

                    Ready ready = new Ready();
                    ready.setClientType("baseStation");
                    socket.emit("ready", new Gson().toJson(ready));
                    fliBeaconApplication.getFliLocationService().start();
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fliBeaconApplication.getSocket().disconnect();
            }
        });
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
