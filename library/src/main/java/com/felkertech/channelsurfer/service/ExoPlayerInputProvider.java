package com.felkertech.channelsurfer.service;

import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import com.felkertech.channelsurfer.players.TvInputPlayer;

/**
 * Created by guest1 on 1/12/2016.
 */
public abstract class ExoPlayerInputProvider extends TvInputProvider {
    protected TvInputPlayer exoPlayer;
    protected Surface mSurface;
    private String TAG = "ExoPlayerInputProvider";
    private float mVolume;

    @Override
    public boolean onSetSurface(Surface surface) {
        if(exoPlayer == null)
            exoPlayer = new TvInputPlayer();
        Log.d(TAG, "Set to surface");
        exoPlayer.setSurface(surface);
        mSurface = surface;
        return true;
    }

    @Override
    public void onSetStreamVolume(float volume) {
        if (exoPlayer != null) {
            exoPlayer.setVolume(volume);
        }
        mVolume = volume;
    }

    @Override
    public void onRelease() {
        if (exoPlayer != null) {
            Log.d(TAG, "Released from surface");
            exoPlayer.release();
        }
    }

    /**
     * Will load and begin to play any RTMP, HLS, or MPEG2-DASH stream
     * Should also be able to play local videos and audio files
     * @param uri The URL where the file resides
     */
    public void play(String uri) {
        exoPlayer.setSurface(mSurface);
        try {
            exoPlayer.prepare(getApplicationContext(), Uri.parse(uri), TvInputPlayer.SOURCE_TYPE_HLS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        exoPlayer.setPlayWhenReady(true);
    }
}
