package com.felkertech.sample.channelsurfer;

import android.content.Intent;
import android.media.PlaybackParams;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
    public void onCreate() {
        super.onCreate();
        Toast.makeText(SampleTvInputProvider.this, "onCreate called", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onCreate called");
    }
    @Override
    public void onRebind(Intent i) {
        super.onRebind(i);
        Toast.makeText(SampleTvInputProvider.this, "onRebind called", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onRebind called");
    }
    @Override
    public void onRelease() {
        super.onRelease();
        Toast.makeText(SampleTvInputProvider.this, "onRelease called", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onRelease called");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(SampleTvInputProvider.this, "onDestroy called", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onDestroy called");
    }

    @Override
    public List<Channel> getAllChannels() {
        Log.d(TAG, "Get all channels");
//        Toast.makeText(SampleTvInputProvider.this, "Get all channels", Toast.LENGTH_SHORT).show();
        List<Channel> channels = new ArrayList<>();
        channels.add(new Channel()
            .setName("Time.Is")
            .setNumber("1"));
        channels.add(new Channel()
            .setName("Big Buck Bunny")
            .setNumber("2"));
        channels.add(new Channel()
            .setName("androidtv.news")
            .setNumber("3"));
        channels.add(new Channel()
            .setName("Dance Party")
            .setNumber("4"));
        Log.d(TAG, "Get channels");
        return channels;
    }

    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channelInfo, long startTimeMs, long endTimeMs) {
        Log.d(TAG, "Get programs for channel "+channelInfo.getName());
//        Toast.makeText(SampleTvInputProvider.this, "Get all programs for "+channelInfo.getName(), Toast.LENGTH_SHORT).show();
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
                        .setContentRatings(new TvContentRating[]{RATING_MA})
                        .setVideoWidth(1920)
                        .setVideoHeight(1080)
                        .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                        .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                        .build();
            } else if(channelInfo.getNumber().equals("2")) {
                p = new Program.Builder(getGenericProgram(channelInfo))
                        .setTitle("Big Buck Bunny")
                        .setInternalProviderData("http://184.72.239.149/vod/smil:BigBuckBunny.smil/playlist.m3u8")
                        .setVideoWidth(1920)
                        .setVideoHeight(1080)
                        .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                        .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                        .build();
            } else if(channelInfo.getNumber().equals("3")) {
                p = new Program.Builder(getGenericProgram(channelInfo))
                        .setTitle("Sample Video")
                        .setDescription("Visit http://androidtv.news, the one-stop shop for everything Android TV")
                        .setInternalProviderData(getLocalVideoUri(SampleTvSetup.LOCAL_FILES_FOLDER+"/androidtvnews.mp4")) //b/c getPackageName is broken
                        .setVideoWidth(1600)
                        .setVideoHeight(900)
                        .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                        .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                        .build();
            } else if(channelInfo.getNumber().equals("4")) {
                p = new Program.Builder(getGenericProgram(channelInfo))
                        .setTitle("Ectasy")
                        .setDescription("An example of building a music-based app, with local (or online) music. Ectasy is a song from PurplePlanet.")
                        .setInternalProviderData(getLocalAudioUri(SampleTvSetup.LOCAL_FILES_FOLDER+"/ectasy.mp3"))
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
        Program p = getProgramRightNow(channel);
        Toast.makeText(SampleTvInputProvider.this, "Tuning to "+channel.getName()+" with program "+p.getTitle()+" at "+p.getInternalProviderData(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Tuning to " + channel.getName());
        Log.d(TAG, "Playing "+p.getTitle());
        Log.d(TAG, "Play the video "+p.getInternalProviderData());

        //Only my local channels will have the ability to be time shifted, so I should update that every tuning.
        //Timeshifting only works for API >= 23
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isLocal()) {
                getSession().notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_AVAILABLE);
            } else {
                //If it's not a local channel, I cannot pause or seek in the program
                getSession().notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_UNAVAILABLE);
            }
        }

        play(getProgramRightNow(channel).getInternalProviderData());
        if(currentChannel.getNumber().equals("4")) {
            Handler h = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    setOverlayEnabled(true);
                }
            };
            h.sendEmptyMessageDelayed(0, 16);
        }
        return true;
    }

    @Override
    public View onCreateVideoView() {
        Toast.makeText(SampleTvInputProvider.this, "onCreateVideoView", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Create video view");
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
        return currentChannel != null && (currentChannel.getNumber().equals("3") || currentChannel.getNumber().equals("4"));
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
        if(lastTune == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return TvInputManager.TIME_SHIFT_INVALID_TIME;
        else if(lastTune == null)
            return -1;
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
