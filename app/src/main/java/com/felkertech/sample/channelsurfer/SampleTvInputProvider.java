package com.felkertech.sample.channelsurfer;

import android.media.PlaybackParams;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.felkertech.channelsurfer.TimeShiftable;
import com.felkertech.channelsurfer.model.Channel;
import com.felkertech.channelsurfer.model.Program;
import com.felkertech.channelsurfer.service.MultimediaInputProvider;
import com.felkertech.channelsurfer.service.WebViewInputProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by guest1 on 1/7/2016.
 */
public class SampleTvInputProvider extends MultimediaInputProvider
        implements TimeShiftable {
    private String TAG = "SampleTvInputProvider";

    @Override
    public List<Channel> getAllChannels() {
        List<Channel> channels = new ArrayList<>();
        channels.add(new Channel()
            .setName("Time.Is")
            .setNumber("1"));
        channels.add(new Channel()
            .setName("Big Buck Bunny")
            .setNumber("2"));
        channels.add(new Channel()
            .setName("AndroidTV.news")
            .setNumber("3"));
        channels.add(new Channel()
            .setName("Dance Party")
            .setNumber("4"));
        Log.d(TAG, "Get channels");
        return channels;
    }

    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channelInfo, long startTimeMs, long endTimeMs) {
        int programs = (int) ((endTimeMs-startTimeMs)/1000/60/60); //Hour long segments
        int SEGMENT = 1000*60*60; //Hour long segments
        List<Program> programList = new ArrayList<>();
        Log.d(TAG, "Get programs");
        for(int i=0;i<programs;i++) {
            Program p = null;
            if(channelInfo.getNumber().equals("1")) {
                p = new Program.Builder(getGenericProgram(channelInfo))
                        .setTitle("What Time is It?")
                        .setInternalProviderData("http://time.is")
                        .setVideoWidth(1920)
                        .setVideoHeight(1080)
                        .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                        .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                        .build();
            } else if(channelInfo.getNumber().equals("2")) {
                p = new Program.Builder(getGenericProgram(channelInfo))
                        .setTitle("Big Buck Bunny")
                        .setInternalProviderData("http://www.nacentapps.com/m3u8/index.m3u8")
                        .setVideoWidth(1920)
                        .setVideoHeight(1080)
                        .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                        .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                        .build();
            } else if(channelInfo.getNumber().equals("3")) {
                p = new Program.Builder(getGenericProgram(channelInfo))
                        .setTitle("Sample Video")
                        .setDescription("Visit http://androidtv.news, the one-stop shop for everything Android TV")
                        .setInternalProviderData(getLocalVideoUri(R.raw.androidtvnews))
                        .setVideoWidth(1600)
                        .setVideoHeight(900)
                        .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                        .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                        .build();
            } else if(channelInfo.getNumber().equals("4")) {
                p = new Program.Builder(getGenericProgram(channelInfo))
                        .setTitle("Ectasy")
                        .setDescription("An example of building a music-based app, with local (or online) music. Ectasy is a song from PurplePlanet.")
                        .setInternalProviderData(getLocalAudioUri(R.raw.ectasy))
                        .setVideoWidth(1920)
                        .setVideoHeight(1080)
                        .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                        .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                        .build();
            }
            programList.add(p);
        }
        return programList;
    }

    Channel currentChannel;
    Date lastTune;
    @Override
    public boolean onTune(Channel channel) {
        this.currentChannel = channel;
        this.lastTune = new Date();
        Log.d(TAG, "Tuning to " + channel.getName());
        Log.d(TAG, "Playing "+getProgramRightNow(channel).getTitle());
        Log.d(TAG, "Play the video "+getProgramRightNow(channel).getInternalProviderData());

        //Only my local channels will have the ability to be time shifted, so I should update that every tuning.
        //Timeshifting only works for API >= 23
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isLocal()) {
                getSession().notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_AVAILABLE);
            } else {
                //If it's not a local channel, I cannot pause or seek in the program
                getSession().notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_UNAVAILABLE);
            }
        }

        play(getProgramRightNow(channel).getInternalProviderData());
        return true;
    }

    @Override
    public View onCreateVideoView() {
        if(currentChannel != null && currentChannel.getNumber().equals("4")) {
            TextView tv = new TextView(this);
            tv.setText("We're playing some music! ♫ ♬");
            tv.setPadding(32, 32, 32, 32);
            tv.setTextSize(36);
            tv.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
            tv.setTextColor(getResources().getColor(android.R.color.background_dark));
            return tv;
        } else
            return null;
    }

    public boolean isLocal() {
        return currentChannel != null && currentChannel.getNumber().equals("3") || currentChannel.getNumber().equals("4");
    }

    @Override
    public void onMediaPause() {
        if(isLocal()) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void onMediaResume() {
        if(isLocal()) {
            exoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void onMediaSeekTo(long timeMs) {
        if(isLocal()) {
            exoPlayer.seekTo(mediaGetCurrentMs() - timeMs);
        }
    }

    @Override
    public long mediaGetStartMs() {
        return lastTune.getTime();
    }

    @Override
    public long mediaGetCurrentMs() {
        return new Date().getTime();
    }

    @Override
    public void onMediaSetPlaybackParams(PlaybackParams playbackParams) {
        //Do nothing
    }
}
