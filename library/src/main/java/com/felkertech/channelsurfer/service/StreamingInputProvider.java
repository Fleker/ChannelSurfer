package com.felkertech.channelsurfer.service;

import android.content.Context;
import android.net.Uri;
import android.view.View;

import com.felkertech.channelsurfer.model.Channel;
import com.felkertech.channelsurfer.model.Program;

import java.util.ArrayList;
import java.util.List;

/**
 * Do you want to make adding live streams literally as easy as possible. You can just extend
 * this class and add your channel list. Just assign each channel a URL through the
 * `internalProviderData` variable.
 *
 * Adding programs and playback is handled automatically.
 * Created by Nick on 1/12/2016.
 */
public abstract class StreamingInputProvider extends MultimediaInputProvider {
    @Override
    public List<Program> getProgramsForChannel(Context c, Uri channelUri, Channel channelInfo, long startTimeMs, long endTimeMs) {
        int programs = (int) ((endTimeMs-startTimeMs)/1000/60/60); //Hour long segments
        int SEGMENT = 1000*60*60; //Hour long segments
        List<Program> programList = new ArrayList<>();
        for(int i=0;i<programs;i++) {
            programList.add(new Program.Builder(getGenericProgram(channelInfo))
                    .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                    .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                    .setInternalProviderData(channelInfo.getInternalProviderData())
                    .build()
            );
        }
        return programList;
    }

    @Override
    public boolean onTune(Channel channel) {
        play(getProgramRightNow(channel).getInternalProviderData());
        return true;
    }

    @Override
    public View onCreateVideoView() {
        return null;
    }
}
