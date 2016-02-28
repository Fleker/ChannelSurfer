package com.felkertech.channelsurfer.service;

import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.tv.TvInputManager;
import android.os.Build;
import android.view.Surface;

import com.felkertech.channelsurfer.TimeShiftable;

import java.util.Date;

/**
 * Created by guest1 on 1/7/2016.
 */
public abstract class MediaPlayerInputProvider extends TvInputProvider
        implements TimeShiftable {
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

    @Override
    public void onMediaPause() {
        mediaPlayer.pause();
    }

    @Override
    public void onMediaResume() {
        mediaPlayer.start();
    }

    @Override
    public void onMediaSeekTo(long timeMs) {
        mediaPlayer.seekTo((int) (timeMs - mediaGetStartMs()));
    }

    @Override
    public long mediaGetStartMs() {
        if(simpleSession.lastTune == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return TvInputManager.TIME_SHIFT_INVALID_TIME;
        else if(simpleSession.lastTune == null)
            return -1;
        return simpleSession.lastTune.getTime();
    }

    @Override
    public long mediaGetCurrentMs() {
        return mediaPlayer.getCurrentPosition()+mediaGetStartMs();
    }

    @Override
    public void onMediaSetPlaybackParams(PlaybackParams playbackParams) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer.setPlaybackParams(playbackParams);
        }
    }
}
