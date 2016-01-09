package com.felkertech.sample.channelsurfer;

import android.content.Context;

import com.felkertech.channelsurfer.sync.SyncAdapter;

/**
 * Created by guest1 on 1/8/2016.
 */
public class SampleSyncAdapter extends SyncAdapter {
    public SampleSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public SampleSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }
}
