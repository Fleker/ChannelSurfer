package com.felkertech.channelsurfer.service;

import android.view.Surface;
import android.view.View;
import android.webkit.WebView;

/**
 * Created by guest1 on 1/8/2016.
 */
public abstract class WebViewInputProvider extends TvInputProvider {
    protected WebView browser;

    @Override
    public boolean onSetSurface(Surface surface) {
        return true;
    }

    @Override
    public void onSetStreamVolume(float volume) {

    }

    @Override
    public void onRelease() {

    }

    @Override
    public View onCreateOverlayView() {
        if(browser == null) {
            browser = new WebView(getApplicationContext());
            browser.getSettings().setJavaScriptEnabled(true);
            browser.getSettings().setSupportZoom(false);
            browser.getSettings().setSupportMultipleWindows(false);
            browser.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.35 Safari/537.36"); //Claim to be a desktop
            browser.setKeepScreenOn(true);
        }
        return browser;
    }

    public void loadUrl(String url) {
        notifyVideoAvailable();
        setOverlayEnabled(true);
        browser.loadUrl(url);
    }
}
