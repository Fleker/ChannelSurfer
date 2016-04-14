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
    protected TvInputPlayer tvInputPlayer;
    protected Surface mSurface;
    private String TAG = "ExoPlayerInputProvider";
    private float mVolume;

    @Override
    public boolean onSetSurface(Surface surface) {
        if(tvInputPlayer == null)
            tvInputPlayer = new TvInputPlayer();
        if(surface != null) {
            Log.d(TAG, "Set to surface");
            tvInputPlayer.setSurface(surface);
            mSurface = surface;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onSetStreamVolume(float volume) {
        if (tvInputPlayer != null) {
            tvInputPlayer.setVolume(volume);
        }
        mVolume = volume;
    }

    @Override
    public void onRelease() {
        if (tvInputPlayer != null) {
            Log.d(TAG, "Released from surface");
            tvInputPlayer.release();
        }
    }

    /**
     * Will load and begin to play any RTMP, HLS, or MPEG2-DASH stream
     * Should also be able to play local videos and audio files
     * @param uri The URL where the file resides
     */
    public void play(String uri) {
        tvInputPlayer.setSurface(mSurface);
        try {
            Log.d(TAG, "Play "+uri+"; "+uri.indexOf("asset:///"));
            if(uri.contains("asset:///")) {
                Log.i(TAG, "Is a local file");
                DataSource dataSource=new AssetDataSource(getApplicationContext());
                ExtractorSampleSource extractorSampleSource=new ExtractorSampleSource(Uri.parse(uri),dataSource,new DefaultAllocator(1000),5000);
                TrackRenderer audio=new MediaCodecAudioTrackRenderer(extractorSampleSource,null,true);
                tvInputPlayer.prepare(audio, null, null);
            } else {
                tvInputPlayer.prepare(getApplicationContext(), Uri.parse(uri), TvInputPlayer.SOURCE_TYPE_HLS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tvInputPlayer.setPlayWhenReady(true);

    }
    @Override
    public void onMediaPause() {
        tvInputPlayer.setPlayWhenReady(false);
    }

    @Override
    public void onMediaResume() {
        tvInputPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onMediaSeekTo(long timeMs) {
        Log.d(TAG, "Seek from "+mediaGetCurrentMs()+" to "+timeMs);
        tvInputPlayer.seekTo(timeMs - mediaGetStartMs());
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
        return tvInputPlayer.getCurrentPosition()+mediaGetStartMs();
    }

    @Override
    public void onMediaSetPlaybackParams(PlaybackParams playbackParams) {
        //Do nothing
    }
}
