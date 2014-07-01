package ch.fork.flibeacons.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;

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
                SocketIO socket = fliBeaconApplication.getSocket();
                Ready ready = new Ready();
                ready.setClientType("baseStation");
                socket.emit("ready", new Gson().toJson(ready));
                fliBeaconApplication.getFliLocationService().start();
                startActivity(new Intent(StartActivity.this, MainActivity.class));
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
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
