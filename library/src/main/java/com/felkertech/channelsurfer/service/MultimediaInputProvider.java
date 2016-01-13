package com.felkertech.channelsurfer.service;

import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.felkertech.channelsurfer.players.TvInputPlayer;
import com.felkertech.channelsurfer.players.WebInputPlayer;
import com.google.android.exoplayer.ExoPlaybackException;

/**
 * This is a combination of a video and web input provider. Playing a URL will try to play
 * the video. If unsuccessful, it will try to display the URL in a web browser view.
 * Created by Nick on 1/12/2016.
 */
public abstract class MultimediaInputProvider extends ExoPlayerInputProvider {
    private String TAG = "MultimediaInputProvider";
    private boolean isWeb = false;
    private String URL = "";
    @Override
    public View onCreateOverlayView() {
        Log.d(TAG, "Create overlay view. For web? "+isWeb);
        if(!isWeb) {
            return onCreateVideoView();
        } else {
            //Website
            Log.d(TAG, "Load "+URL);
            WebInputPlayer webView = new WebInputPlayer(getApplicationContext());
            webView.load(URL);
            return webView;
        }
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
        /*if(webInputPlayer == null)
            webInputPlayer = new WebInputPlayer(getApplicationContext());
        webInputPlayer.loadUrl(url);*/
        URL = url;
    }

    /**
     * Tries to load a video file. If one cannot be played, it will be interpreted as a website
     * and begin to load it in a web browser
     * @param uri The URL for the file or website
     */
    @Override
    public void play(final String uri) {
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
//                Log.e(TAG, "Callback2");
                Log.e(TAG, e.getMessage()+"");
                if(e.getMessage().contains("Extractor")) {
                    Log.d(TAG, "Cannot play the stream, try loading it as a website");
                    //Pretend this is a website
                    /*isWeb = true;
                    loadUrl(uri);
                    notifyVideoAvailable();
                    setOverlayEnabled(true);*/
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
        try {
            exoPlayer.prepare(getApplicationContext(), Uri.parse(uri), TvInputPlayer.SOURCE_TYPE_HLS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        exoPlayer.setPlayWhenReady(true);
    }
}
