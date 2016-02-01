package com.felkertech.channelsurfer.service;

import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import com.felkertech.channelsurfer.players.TvInputPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.AssetDataSource;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;

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
}
