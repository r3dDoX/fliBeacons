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
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ch.fork.flibeacons.FliBeaconApplication;
import ch.fork.flibeacons.R;
import ch.fork.flibeacons.events.ConnectedEvent;
import io.socket.SocketIO;

public class StartActivity extends BaseActivity {

    private static final String TAG = StartActivity.class.getSimpleName();

    @InjectView(R.id.registerAndStart)
    Button registerAndStartButton;
    @InjectView(R.id.disconnect)
    Button disconnectButton;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;

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
                connect();
            }
        });
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fliBeaconApplication.getSocket().disconnect();
                progressBar.setVisibility(View.INVISIBLE);
                progressBar.setIndeterminate(false);
            }
        });
    }

    private void connect() {
        try {

            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);

            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String url = defaultSharedPreferences.getString(SettingsActivity.KEY_URL, "http://flibeacons.ngrok.com");
            Log.i(TAG, "Connecting to URL: " + url);

            SocketIO socket = new SocketIO(url);

            socket.setDefaultSSLSocketFactory(SSLContext.getDefault());
            fliBeaconApplication.setSocket(socket);
            socket.connect(fliBeaconApplication);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();

        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setIndeterminate(false);
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

    @Subscribe
    public void event(ConnectedEvent ev) {
        fliBeaconApplication.getFliLocationService().start();
        startActivity(new Intent(StartActivity.this, MainActivity.class));
    }
}
