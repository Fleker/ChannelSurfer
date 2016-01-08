package com.felkertech.sample.channelsurfer;

import android.net.Uri;
import android.util.Log;

import com.felkertech.channelsurfer.model.Channel;
import com.felkertech.channelsurfer.model.Program;
import com.felkertech.channelsurfer.service.WebViewInputProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guest1 on 1/7/2016.
 */
public class SampleTvInputProvider extends WebViewInputProvider {
    private String TAG = "SampleTvInputProvider";

    @Override
    public List<Channel> getAllChannels() {
        List<Channel> channels = new ArrayList<>();
        channels.add(new Channel()
            .setName("Time.Is")
            .setNumber("1"));
        return channels;
    }

    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channelInfo, long startTimeMs, long endTimeMs) {
        int programs = (int) ((endTimeMs-startTimeMs)/1000/60/60); //Hour long segments
        int SEGMENT = 1000*60*60; //Hour long segments
        List<Program> programList = new ArrayList<>();
        for(int i=0;i<programs;i++) {
            programList.add(new Program.Builder(getGenericProgram(channelInfo))
                            .setTitle("What Time is It?")
                            .setVideoHeight(1080)
                            .setVideoWidth(1920)
                            .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                            .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                            .build()
            );
        }
        return programList;
    }

    @Override
    public boolean onTune(Channel channel) {
        Log.d(TAG, "Tuning to " + channel.getName());
        Log.d(TAG, "Playing "+getProgramRightNow(channel).getTitle());
        loadUrl("http://time.is");
        return true;
    }
}
