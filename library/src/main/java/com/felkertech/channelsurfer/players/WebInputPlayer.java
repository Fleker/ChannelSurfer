package com.felkertech.channelsurfer.players;

import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by guest1 on 1/3/2016.
 */
public class WebInputPlayer extends WebView {
    public WebInputPlayer(Context c) {
        super(c);
        super.getSettings().setJavaScriptEnabled(true);
        super.getSettings().setSupportZoom(false);
        super.getSettings().setSupportMultipleWindows(false);
        super.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.35 Safari/537.36"); //Claim to be a desktop
//        super.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        super.setKeepScreenOn(true);
    }
    public void load(String url) {
        super.loadUrl(url);
    }
}