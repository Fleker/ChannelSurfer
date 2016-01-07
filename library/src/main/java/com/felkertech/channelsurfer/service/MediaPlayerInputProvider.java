package com.felkertech.channelsurfer.service;

import android.media.MediaPlayer;
import android.view.Surface;

/**
 * Created by guest1 on 1/7/2016.
 */
public abstract class MediaPlayerInputProvider extends TvInputProvider {
    protected MediaPlayer mediaPlayer;
    protected Surface mSurface;

    @Override
    public boolean onSetSurface(Surface surface) {
        mSurface = surface;
        return true;
    }

    @Override
    public void onSetStreamVolume(float volume) {
        if(mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        mediaPlayer.setVolume(volume, volume);
    }

    @Override
    public void onRelease() {
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
    }
}
