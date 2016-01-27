package com.felkertech.sample.channelsurfer;

import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.felkertech.channelsurfer.model.Channel;
import com.felkertech.channelsurfer.model.Program;
import com.felkertech.channelsurfer.service.MultimediaInputProvider;
import com.felkertech.channelsurfer.service.WebViewInputProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guest1 on 1/7/2016.
 */
public class SampleTvInputProvider extends MultimediaInputProvider {
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
                        .setVideoHeight(1080)
                        .setVideoWidth(1920)
                        .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                        .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                        .build();
            } else if(channelInfo.getNumber().equals("2")) {
                p = new Program.Builder(getGenericProgram(channelInfo))
                        .setTitle("Big Buck Bunny")
                        .setInternalProviderData("http://www.nacentapps.com/m3u8/index.m3u8")
                        .setVideoHeight(1080)
                        .setVideoWidth(1920)
                        .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                        .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                        .build();
            }
            programList.add(p);
        }
        return programList;
    }

    @Override
    public boolean onTune(Channel channel) {
        Log.d(TAG, "Tuning to " + channel.getName());
        Log.d(TAG, "Playing "+getProgramRightNow(channel).getTitle());
        Log.d(TAG, "Play the video "+getProgramRightNow(channel).getInternalProviderData());
        play(getProgramRightNow(channel).getInternalProviderData());
        return true;
    }

    @Override
    public View onCreateVideoView() {
        return null;
    }
}
