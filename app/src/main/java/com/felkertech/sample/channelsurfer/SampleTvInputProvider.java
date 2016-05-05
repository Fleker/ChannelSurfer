package com.felkertech.sample.channelsurfer;

import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.felkertech.channelsurfer.interfaces.SplashScreenable;
import com.felkertech.channelsurfer.model.Channel;
import com.felkertech.channelsurfer.model.Program;
import com.felkertech.channelsurfer.service.MultimediaInputProvider;
import com.felkertech.settingsmanager.SettingsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 1/7/2016.
 */
public class SampleTvInputProvider extends MultimediaInputProvider
        implements SplashScreenable {
    private String TAG = "SampleTvInputProvider";
    private boolean stillActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        if(shouldShowToasts())
            Toast.makeText(SampleTvInputProvider.this, "onCreate called", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onCreate called");
    }
    @Override
    public boolean onUnbind(Intent intent) {
        if(shouldShowToasts()) {
            Toast.makeText(SampleTvInputProvider.this, "onUnbind called", Toast.LENGTH_SHORT).show();
        }
        return super.onUnbind(intent);
    }
    @Override
    public void onRebind(Intent i) {
        super.onRebind(i);
        if(shouldShowToasts())
            Toast.makeText(SampleTvInputProvider.this, "onRebind called", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onRebind called");
    }
    @Override
    public void onRelease() {
        super.onRelease();
        if(getResources().getBoolean(R.bool.channel_surfer_lifecycle_toasts))
            Toast.makeText(SampleTvInputProvider.this, "onRelease called", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onRelease called");
        stillActive = true;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(shouldShowToasts())
            Toast.makeText(SampleTvInputProvider.this, "onDestroy called", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onDestroy called");
    }

    @Override
    public boolean onSetSurface(Surface surface) {
        Toast.makeText(SampleTvInputProvider.this, "onSetSurface called. Is surface null? "+(surface==null), Toast.LENGTH_SHORT).show();
        return super.onSetSurface(surface);
    }


    @Override
    public List<Channel> getAllChannels(Context mContext) {
        Log.d(TAG, "Get all channels");
        List<Channel> channels = new ArrayList<>();
        channels.add(new Channel()
            .setName("Time.Is")
            .setAppLinkColor(mContext.getResources().getColor(R.color.md_red_500))
            .setAppLinkIntent(new Intent("com.google.android.music"))
            .setAppLinkText("Open Google Play Music")
            .setAppLinkIcon("android.resource://com.felkertech.sample.channelsurfer/drawable/md_library_music")
            .setNumber("1"));
        channels.add(new Channel()
            .setName("Big Buck Bunny")
            .setAppLinkColor(mContext.getResources().getColor(R.color.cs_blue_500))
            .setAppLinkText("Open Netflix")
            .setAppLinkIntent(new Intent("com.netflix.ninja"))
            .setAppLinkIcon("android.resource://com.felkertech.sample.channelsurfer/drawable/md_movies")
            .setNumber("2"));
        channels.add(new Channel()
            .setName("Sintel")
            .setAppLinkText("Hello")
            .setAppLinkColor(mContext.getResources().getColor(R.color.md_green_500))
            .setNumber("2-1"));
        channels.add(new Channel()
            .setName("androidtv.news")
            .setAppLinkText("Visit site")
            .setAppLinkColor(mContext.getResources().getColor(R.color.md_purple_500))
            .setNumber("3"));
        channels.add(new Channel()
            .setName("Dance Party")
            .setAppLinkText("Hi")
            .setAppLinkColor(mContext.getResources().getColor(R.color.md_brown_500))
            .setNumber("4"));
        Log.d(TAG, "Get channels");
        return channels;
    }

    @Override
    public List<Program> getProgramsForChannel(Context ignored, Uri channelUri, Channel channelInfo, long startTimeMs, long endTimeMs) {
        Log.d(TAG, "Get programs for channel "+channelInfo.getName());
        int programs = (int) ((endTimeMs-startTimeMs)/1000/60/60); //Hour long segments
        int SEGMENT = 1000*60*60; //Hour long segments
        List<Program> programList = new ArrayList<>();
        Log.d(TAG, "Get programs");
        for(int i=0;i<programs;i++) {
            Program p = null;
            if (channelInfo.getNumber().equals("1")) {
                p = new Program.Builder(getGenericProgram(channelInfo))
                        .setTitle("What Time is It?")
                        .setInternalProviderData("http://time.is")
                        .setDescription("A website that updates continually")
                        .setContentRatings(new TvContentRating[]{RATING_MA})
                        .setVideoWidth(1920)
                        .setVideoHeight(1080)
                        .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                        .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                        .build();
            } else if (channelInfo.getNumber().equals("2")) {
                p = new Program.Builder(getGenericProgram(channelInfo))
                        .setTitle("Big Buck Bunny")
                        .setDescription("The Blender movie playing from a stream online")
                        .setInternalProviderData("http://184.72.239.149/vod/smil:BigBuckBunny.smil/playlist.m3u8")
                        .setVideoWidth(1920)
                        .setVideoHeight(1080)
                        .setCanonicalGenres(new String[]{TvContract.Programs.Genres.MOVIES})
                        .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                        .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                        .build();
            } else if (channelInfo.getNumber().equals("2-1")) {
                p = new Program.Builder(getGenericProgram(channelInfo))
                        .setTitle("Sintel")
                        .setInternalProviderData("https://www.youtube.com/embed/HomAZcKm3Jo?autoplay=1")
                        .setDescription("The Blender movie Sintel playing straight from YouTube")
                        .setVideoWidth(1920)
                        .setVideoHeight(1080)
                        .setCanonicalGenres(new String[]{TvContract.Programs.Genres.MOVIES, TvContract.Programs.Genres.FAMILY_KIDS})
                        .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                        .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                        .build();
            } else if(channelInfo.getNumber().equals("3")) {
                p = new Program.Builder(getGenericProgram(channelInfo))
                        .setTitle("Sample Video")
                        .setDescription("Visit http://androidtv.news, the one-stop shop for everything Android TV")
                        .setInternalProviderData(getLocalVideoUri(SampleTvSetup.LOCAL_FILES_FOLDER+"/androidtvnews.mp4"))
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
    @Override
    public boolean onTune(Channel channel) {
        this.currentChannel = channel;
        Program p = getProgramRightNow(channel);
        if(shouldShowToasts())
            Toast.makeText(SampleTvInputProvider.this, "Tuning to "+channel.getName()+" with program "+p.getTitle()+" at "+p.getInternalProviderData(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Tuning to " + channel.getName());
        Log.d(TAG, "Playing "+p.getTitle());
        Log.d(TAG, "Play the video "+p.getInternalProviderData());
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
    protected void onWebsiteFinishedLoading() {
        if(currentChannel.getNumber().equals("2-1")) {
            //Hopefully the thing is loaded
            Log.d(TAG, "Running JS");
            runJS("yt.player.getPlayerByElement('player').playVideo()");
        }
    }

    @Override
    public View onCreateVideoView() {
        if(shouldShowToasts())
            Toast.makeText(SampleTvInputProvider.this, "onCreateVideoView. Still active? "+stillActive, Toast.LENGTH_SHORT).show();
        if(stillActive) {
            //We're picking up where we left off
            if(mSurface != null) {
                Toast.makeText(SampleTvInputProvider.this, "Reset surface", Toast.LENGTH_SHORT).show();
                onSetSurface(mSurface);
            } else {
                Toast.makeText(SampleTvInputProvider.this, "Try a forced reset", Toast.LENGTH_SHORT).show();
                onUnbind(null);
                onDestroy();
                stillActive = false;
                onCreate();
            }
        }
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

    public boolean shouldShowToasts() {
        return new SettingsManager(this).getBoolean(R.string.sm_debug);
    }

    @Override
    public View getSplashscreen(Uri channelUri) {
        return ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.tv_splashscreen, null);
    }
}
