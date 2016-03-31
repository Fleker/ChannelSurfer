package com.felkertech.channelsurfer.sync;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.AndroidRuntimeException;
import android.widget.Toast;

public class DummyAccountIgnoreActivity extends Activity {
    public static final String ACTION_ADD = "Add";
    public static final String ACTION_REMOVE = "Remove";
    public static final String ACTION_DELETE = ACTION_REMOVE;
    public static final String INTENT_ACTION = "action";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().getStringExtra(INTENT_ACTION).equals(ACTION_ADD))
            Toast.makeText(DummyAccountIgnoreActivity.this, "You can't add a new account.", Toast.LENGTH_SHORT).show();
        else if(getIntent().getStringExtra(INTENT_ACTION).equals(ACTION_REMOVE))
            Toast.makeText(DummyAccountIgnoreActivity.this, "You can't delete this account.", Toast.LENGTH_SHORT).show();

        finish();
    }
}
