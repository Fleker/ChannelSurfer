package com.felkertech.channelsurfer.setup;

import android.app.Activity;
import android.media.tv.TvInputInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.felkertech.channelsurfer.R;

import com.felkertech.channelsurfer.sync.SyncUtils;

/**
 * Created by guest1 on 1/6/2016.
 */
public abstract class SetupTvInputProviderActivity extends Activity {
    private String TAG = "SetupTvInputProviderActivity";
    private String info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayLayout();
        Log.d(TAG, "Created me");

        info = "";
        if(getIntent() != null) {
            info = getIntent().getStringExtra(TvInputInfo.EXTRA_INPUT_ID);
            Log.d(TAG, info);
        }

        SyncUtils.setUpPeriodicSync(this, info);
        setupTvInputProvider();
    }

    public void displayLayout() {
        setContentView(R.layout.channel_surfer_setup);
    }

    public void requestSync() {
        SyncUtils.requestSync(info);
    }

    public void setupTvInputProvider() {
        Log.d(TAG, "Requesting sync");
        requestSync();
        Handler h = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                finish();
            }
        };
        h.sendEmptyMessageDelayed(0, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "Setup complete. Make sure you enable these channels in the channel list.", Toast.LENGTH_SHORT).show();
    }
}
