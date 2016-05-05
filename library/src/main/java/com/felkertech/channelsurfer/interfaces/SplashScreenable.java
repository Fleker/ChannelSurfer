package com.felkertech.channelsurfer.interfaces;

import android.net.Uri;
import android.view.View;

/**
 * This interface should be used in a TvInputProvider if you plan on using
 * custom splashscreens and want to control the splashscreen that displays
 * while the channel is loading.
 *
 * A splashscreen can be any custom view
 * Created by Nick on 5/4/2016.
 */
public interface SplashScreenable {
    /**
     * This method is called just as a tuning request is sent. Use this method
     * to generate an appropriate splashscreen
     * @param channelUri The built-in channel URI from Live Channels. Note this is not a channel
     *                   object. In order to parse it, you'll need custom logic to associate it
     *                   with a channel. It would take too long to get you a full Channel object
     * @return The splashscreen view
     */
    View getSplashscreen(Uri channelUri);
}
