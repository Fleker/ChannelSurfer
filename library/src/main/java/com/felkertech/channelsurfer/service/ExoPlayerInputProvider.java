package com.felkertech.channelsurfer.service;

import android.media.PlaybackParams;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.felkertech.channelsurfer.interfaces.TimeShiftable;
import com.felkertech.channelsurfer.players.TvInputPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.AssetDataSource;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;

import java.util.Date;

/**
 * Created by Nick on 1/12/2016.
 */
public abstract class ExoPlayerInputProvider extends TvInputProvider
        implements TimeShiftable {
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
            Log.d(TAG, "Play "+uri+"; "+uri.indexOf("asset:///"));
            if(uri.contains("asset:///")) {
                Log.i(TAG, "Is a local file");
                DataSource dataSource=new AssetDataSource(getApplicationContext());
                ExtractorSampleSource extractorSampleSource=new ExtractorSampleSource(Uri.parse(uri),dataSource,new DefaultAllocator(1000),5000);
                TrackRenderer audio=new MediaCodecAudioTrackRenderer(extractorSampleSource,null,true);
                exoPlayer.prepare(audio, null, null);
            } else {
                exoPlayer.prepare(getApplicationContext(), Uri.parse(uri), TvInputPlayer.SOURCE_TYPE_HLS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        exoPlayer.setPlayWhenReady(true);

    }
    @Override
    public void onMediaPause() {
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void onMediaResume() {
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onMediaSeekTo(long timeMs) {
        Log.d(TAG, "Seek from "+mediaGetCurrentMs()+" to "+timeMs);
        exoPlayer.seekTo(timeMs - mediaGetStartMs());
    }

    @Override
    public long mediaGetStartMs() {
        if(simpleSession.lastTune == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return TvInputManager.TIME_SHIFT_INVALID_TIME;
        else if(simpleSession.lastTune == null) {
            simpleSession.lastTune = new Date();
        }
        return simpleSession.lastTune.getTime();
    }

    @Override
    public long mediaGetCurrentMs() {
        return exoPlayer.getCurrentPosition()+mediaGetStartMs();
    }

    @Override
    public void onMediaSetPlaybackParams(PlaybackParams playbackParams) {
        //Do nothing
    }
}
