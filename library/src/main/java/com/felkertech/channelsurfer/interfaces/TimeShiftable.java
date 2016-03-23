package com.felkertech.channelsurfer.interfaces;

import android.media.PlaybackParams;

/**
 * Created by Nick on 1/30/2016.
 */
public interface TimeShiftable {
    /**
     * Called when the media should be paused
     */
    void onMediaPause();

    /**
     * Called when the media should resume playing after being paused
     */
    void onMediaResume();

    /**
     * Called when the media is supposed to be played at a given timestamp
     * @param timeMs The time to seek to in milliseconds
     */
    void onMediaSeekTo(long timeMs);

    /**
     * Returns the start playback position, in milliseconds since the unix epoch
     */
    long mediaGetStartMs();

    /**
     * Returns the current playback position, in milliseconds since the unix epoch
     * @return milliseconds
     */
    long mediaGetCurrentMs();

    /**
     * Sets playback parameters
     * @param playbackParams
     */
    void onMediaSetPlaybackParams(PlaybackParams playbackParams);
}
