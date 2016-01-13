package com.felkertech.channelsurfer.utils;

import android.content.Context;
import android.content.Intent;

/**
 * Created by guest1 on 1/12/2016.
 */
public class LiveChannelUtils {
    private static Intent getLiveChannels(Context mContext) {
        return mContext.getPackageManager().getLaunchIntentForPackage("com.google.android.tv");
    }
}
