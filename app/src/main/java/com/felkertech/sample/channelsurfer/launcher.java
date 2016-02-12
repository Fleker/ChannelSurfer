package com.felkertech.sample.channelsurfer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.felkertech.channelsurfer.utils.LiveChannelsUtils;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class Launcher extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_launcher);
        findViewById(R.id.live).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = LiveChannelsUtils.getLiveChannels(Launcher.this);
                if(i != null)
                    startActivity(i);
                else
                    Toast.makeText(Launcher.this, "Live Channels disabled or not installed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
