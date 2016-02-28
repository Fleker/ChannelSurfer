package com.felkertech.channelsurfer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.felkertech.channelsurfer.R;
import com.felkertech.channelsurfer.service.TvInputProvider;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by Nick on 2/7/2016.
 */
public class LiveChannelsUtils {
    private static String TAG = "LiveUtils";
    private static String ANDROID_TV_LIVE_CHANNELS = "com.google.android.tv";
    private static String SONY_LIVE_CHANNELS = "com.sony.dtv.tvplayer";
    public static Intent getLiveChannels(Activity mActivity) {
        if(isPackageInstalled(ANDROID_TV_LIVE_CHANNELS, mActivity)) {
            Intent i = mActivity.getPackageManager().getLaunchIntentForPackage(ANDROID_TV_LIVE_CHANNELS);
            return i;
        } else if(isPackageInstalled(SONY_LIVE_CHANNELS, mActivity)) {
            Intent i = mActivity.getPackageManager().getLaunchIntentForPackage(SONY_LIVE_CHANNELS);
            return i;
        }
        return null;
    }
    private static boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    /**
     Returns the TvInputProvider that was defined by the project's manifest
     **/
    public static TvInputProvider getTvInputProvider(Context mContext, final TvInputProviderCallback callback) {
        ApplicationInfo app = null;
        try {
            Log.d(TAG, mContext.getPackageName()+" >");
            app = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            final String service = bundle.getString("TvInputService");
            Log.d(TAG, service);
            Log.d(TAG, mContext.getString(R.string.app_name));
            try {
                Log.d(TAG, "Constructors: " + Class.forName(service).getConstructors().length);
//                Log.d(TAG, "Constructor 1: " + Class.forName(service).getConstructors()[0].toString());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        TvInputProvider provider = null;
                        try {
                            provider = (TvInputProvider) Class.forName(service).getConstructors()[0].newInstance();
                            Log.d(TAG, provider.toString());
                            callback.onTvInputProviderCallback(provider);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch(ClassNotFoundException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            } catch(Exception e) {
                e.printStackTrace();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface TvInputProviderCallback {
        void onTvInputProviderCallback(TvInputProvider provider);
    }
}
