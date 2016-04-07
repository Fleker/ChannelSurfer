package com.felkertech.channelsurfer.service;

import android.media.MediaCodec;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.webkit.WebView;

import com.felkertech.channelsurfer.players.TvInputPlayer;
import com.felkertech.channelsurfer.players.WebInputPlayer;
import com.google.android.exoplayer.DummyTrackRenderer;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;

/**
 * This is a combination of a video and web input provider. Playing a URL will try to play
 * the video. If unsuccessful, it will try to display the URL in a web browser view.
 * Created by Nick on 1/12/2016.
 */
public abstract class MultimediaInputProvider extends ExoPlayerInputProvider {
    private String TAG = "MultimediaInputProvider";
    private boolean isWeb = false;
    private String URL = "";
    WebInputPlayer webView;
    @Override
    public View onCreateOverlayView() {
        Log.d(TAG, "Create overlay view. For web? "+isWeb);
        if(!isWeb) {
            return onCreateVideoView();
        } else {
            //Website
            Log.d(TAG, "Load "+URL);
            if(webView == null)
                webView = new WebInputPlayer(getApplicationContext(), new WebInputPlayer.WebViewListener() {
                    @Override
                    public void onPageFinished() {
                        onWebsiteFinishedLoading();
                    }
                });
            webView.load(URL);
            return webView;
        }
    }

    protected void onWebsiteFinishedLoading() {

    }

    protected void runJS(final String js) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                if (js.length() < 50)
                    Log.d(TAG, "Execute " + js);
                else
                    Log.d(TAG, "Execute " + js.substring(0, 49));
                webView.loadUrl("javascript:try { " + js + "} catch(error) { Android.onError(error.message) }");
            }
        });
    }

    /**
     * If a video is going to be played, what kind of overlay will be displayed
     * @return An overlay view that may appear when a video is playing
     */
    public abstract View onCreateVideoView();

    /**
     * Loads a website in a standard browser
     * @param url A web url
     */
    public void loadUrl(String url) {
        Log.d(TAG, "Load url "+url);
        URL = url;
    }

    /**
     * Tries to load a video file. If one cannot be played, it will be interpreted as a website
     * and begin to load it in a web browser
     * @param uri The URL for the file or website
     */
    @Override
    public void play(final String uri) {
        Log.d(TAG, "Start playing "+uri);
        notifyVideoUnavailable(REASON_BUFFERING);
        isWeb = false;
        TvInputPlayer.Callback callback = new TvInputPlayer.Callback() {
            @Override
            public void onPrepared() {
                notifyVideoAvailable();
                setOverlayEnabled(false);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int state) {

            }

            @Override
            public void onPlayWhenReadyCommitted() {

            }

            @Override
            public void onPlayerError(ExoPlaybackException e) {
                Log.e(TAG, e.getMessage()+"");
                if(e.getMessage().contains("Extractor")) {
                    Log.d(TAG, "Cannot play the stream, try loading it as a website");
                    Log.d(TAG, "Open "+uri);
                    //Pretend this is a website
                    loadUrl(uri);
                    isWeb = true;
                    setOverlayEnabled(false);
                    notifyVideoAvailable();
                    isWeb = true;
                    setOverlayEnabled(false);
                    setOverlayEnabled(true);
                    isWeb = true;
                }
            }

            @Override
            public void onDrawnToSurface(Surface surface) {

            }

            @Override
            public void onText(String text) {

            }
        };
        try {
            exoPlayer.removeCallback(callback);
        } catch(NullPointerException e) {
            Log.w(TAG, "exoplayer.removeCallback error "+e.getMessage());
        }
        exoPlayer.addCallback(callback);
        exoPlayer.setSurface(mSurface);
        Log.d(TAG, "Play "+uri+"; "+uri.indexOf("file:///"));
        if(uri.contains("file:///")) {
            Log.i(TAG, "Is a local file");
            //Find appropriate extractor

            DataSource dataSource=new DefaultUriDataSource(getApplicationContext(), TvInputPlayer.getUserAgent(getApplicationContext()));
            ExtractorSampleSource extractorSampleSource=new ExtractorSampleSource(Uri.parse(uri),dataSource,
                    new DefaultAllocator(TvInputPlayer.BUFFER_SEGMENT_SIZE), TvInputPlayer.BUFFER_SEGMENTS * TvInputPlayer.BUFFER_SEGMENT_SIZE,
                    new Mp4Extractor(), new Mp3Extractor());
            TrackRenderer audio=new MediaCodecAudioTrackRenderer(extractorSampleSource);
            TrackRenderer video=new MediaCodecVideoTrackRenderer(getApplicationContext(),extractorSampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            exoPlayer.prepare(audio, video, new DummyTrackRenderer());
        } else {
            try {
                exoPlayer.prepare(getApplicationContext(), Uri.parse(uri), TvInputPlayer.SOURCE_TYPE_HLS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        exoPlayer.setPlayWhenReady(true);
    }
}
