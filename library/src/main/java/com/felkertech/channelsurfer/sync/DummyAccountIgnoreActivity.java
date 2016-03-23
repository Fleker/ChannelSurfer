package com.felkertech.channelsurfer.sync;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class DummyAccountIgnoreActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(DummyAccountIgnoreActivity.this, "You can't add a new account.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
