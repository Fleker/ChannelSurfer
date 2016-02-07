package com.felkertech.channelsurfer;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Nick on 2/7/2016.
 */
public class LiveChannelsUtils {
    public static Intent getLiveChannels(Activity mActivity) {
        Intent i = mActivity.getPackageManager().getLaunchIntentForPackage("com.google.android.tv");
        if (i != null) {
            return i;
        }
        return null;
    }
}
