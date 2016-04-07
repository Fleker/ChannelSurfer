package com.felkertech.channelsurfer.players;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.WrapperListAdapter;

/**
 * Created by guest1 on 1/3/2016.
 */
public class WebInputPlayer extends WebView {
    WebViewListener listener;
    public WebInputPlayer(Context c, WebViewListener listener) {
        super(c);
        super.getSettings().setJavaScriptEnabled(true);
        super.getSettings().setSupportZoom(false);
        super.getSettings().setSupportMultipleWindows(false);
        super.setWebViewClient(new BridgeClient());
        super.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.35 Safari/537.36"); //Claim to be a desktop
//        super.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        super.setKeepScreenOn(true);
        this.listener = listener;
    }
    public void load(String url) {
        super.loadUrl(url);
    }
    public class BridgeClient extends WebViewClient {
        public void onPageFinished(WebView v, String url) {
            super.onPageFinished(v, url);
            Handler h = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    listener.onPageFinished();
                }
            };
            h.sendEmptyMessageDelayed(0, 1000);
        }
    }
    public interface WebViewListener {
        void onPageFinished();
    }
}