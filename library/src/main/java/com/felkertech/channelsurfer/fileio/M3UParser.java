package com.felkertech.channelsurfer.fileio;

import android.util.Log;

import com.felkertech.channelsurfer.model.Channel;
import com.felkertech.channelsurfer.model.Program;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class takes in an inputstream which corresponds to an M3U8 playlist and parses it
 *  into channels. Streams are stored in the `internalProviderData`
 * Created by Nick on 11/25/2015. Updated 5/1/2016.
 */
public class M3UParser {
    private static final String TAG = "M3UParser";

    /**
     * When you have an inputStream that is a valid M3U8 playlist, this function parses it into
     *  a model that can be applied to Live Channels
     * @param inputStream Valid M3U8 file, which can be found through any method
     * @return A TvListing object, which contains channels and programs
     * @throws IOException
     */
    public static TvListing parse(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        List<Channel> channels = new ArrayList<>();
        List<Program> programs = new ArrayList<>();
        Map<Integer, Integer> channelMap = new HashMap<>();
        int defaultDisplayNumber = 0;

        while ((line = in.readLine()) != null) {
            if (line.startsWith("#EXTINF:")) {
                // #EXTINF:0051 tvg-id="blizz.de" group-title="DE Spartensender" tvg-logo="897815.png", [COLOR orangered]blizz TV HD[/COLOR]
                String id = null;
                String displayName = null;
                String displayNumber = null;
                int originalNetworkId = 0;
                String icon = null;

                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    for (String part : parts[0].split(" ")) {
                        if (part.startsWith("#EXTINF:")) {
//                            Log.d(TAG, "Part: "+part);
                            displayNumber = part.substring(8).replaceAll("^0+", "");
//                            Log.d(TAG, "Display Number: "+displayNumber);
                            if(displayNumber.isEmpty())
                                displayNumber = defaultDisplayNumber+"";
                            if(displayNumber.equals("-1"))
                                displayNumber = defaultDisplayNumber+"";
                            defaultDisplayNumber++;
                            originalNetworkId = Integer.parseInt(displayNumber);
                        } else if (part.startsWith("tvg-id=")) {
                            int end = part.indexOf("\"", 8);
                            if (end > 8) {
                                id = part.substring(8, end);
                            }
                        } else if (part.startsWith("tvg-logo=")) {
                            int end = part.indexOf("\"", 10);
                            if (end > 10) {
                                icon = "http://logo.iptv.ink/"
                                        + part.substring(10, end);
                            }
                        }
                    }
                    displayName = parts[1].replaceAll("\\[\\/?(COLOR |)[^\\]]*\\]", "");
                }

                if (originalNetworkId != 0 && displayName != null) {
                    Channel channel =
                            new Channel()
                                .setChannelId(Integer.parseInt(id))
                                .setName(displayName)
                                .setNumber(displayNumber)
                                .setLogoUrl(icon)
                                .setOriginalNetworkId(originalNetworkId);
                    if (channelMap.containsKey(originalNetworkId)) {
                        int freeChannel = 1;
                        while(channelMap.containsKey(new Integer(freeChannel))) {
                            freeChannel++;
                        }
                        channelMap.put(freeChannel, channels.size());
                        channel.setNumber(freeChannel+"");
                        channels.add(channel);
                    } else {
                        channelMap.put(originalNetworkId, channels.size());
                        channels.add(channel);
                    }
                } else {
                    Log.d(TAG, "Import failed: "+originalNetworkId+"= "+line);
                }
            } else if (line.startsWith("http") && channels.size() > 0) {
                channels.get(channels.size()-1).setInternalProviderData(line);
            } else if(line.startsWith("rtmp") && channels.size() > 0) {
                channels.get(channels.size()-1).setInternalProviderData(line);
            }
        }
        TvListing tvl = new TvListing(channels, programs);
        Log.d(TAG, "Done parsing");
        Log.d(TAG, tvl.toString());
        return new TvListing(channels, programs);
    }

    /**
     * This class is a containing model with a list of channels and programs parsed from an M3U8
     *  playlist
     */
    public static class TvListing {
        private List<Channel> channels;
        private final List<Program> programs;

        /**
         * Constructs a listing with a list of channels and programs
         * @param channels List of channels from the playlist
         * @param programs List of programs from the playlist
         */
        public TvListing(List<Channel> channels, List<Program> programs) {
            this.channels = channels;
            //Validate channels, making sure they have urls
            Iterator<Channel> xmlTvChannelIterator = channels.iterator();
            while(xmlTvChannelIterator.hasNext()) {
                Channel tvChannel = xmlTvChannelIterator.next();
                if(tvChannel.getInternalProviderData() == null) {
                    Log.e(TAG, tvChannel.getName()+" has no url!");
                    xmlTvChannelIterator.remove();
                }

            }
            this.programs = programs;
        }

        /**
         * Sets the channels from the playlist
         * @param channels List of channels
         */
        protected void setChannels(List<Channel> channels) {
            this.channels = channels;
        }

        /**
         * Prints out each of the channels as a String
         * @return Each channel printed as a String
         */
        @Override
        public String toString() {
            String out = "";
            for(Channel tvChannel: channels) {
                out += tvChannel.toString()+"\n";
            }
            return out;
        }

        /**
         * For debugging purposes, gets a printable list of channel names and numbers as a string
         * @return String of channels from the playlist
         */
        public String getChannelList() {
            String out = "";
            for(Channel tvChannel: channels) {
                out += tvChannel.getNumber()+" - "+tvChannel.getName()+"\n";
            }
            return out;
        }

        /**
         * @return List of channel objects from the playlist
         */
        public List<Channel> getChannels() {
            return channels;
        }

        /**
         * @return List of program objects from the playlist if they exist. Note: this'll probably
         *  return null
         */
        public List<Program> getPrograms() {
            return programs;
        }
    }
 /*   public static class XmlTvChannel {
        public final String id;
        public final String displayName;
        public String displayNumber;
        public final XmlTvIcon icon;
        public final int originalNetworkId;
        public final int transportStreamId;
        public final int serviceId;
        public final boolean repeatPrograms;
        public String url;

        public XmlTvChannel(String id, String displayName, String displayNumber, XmlTvIcon icon,
                            int originalNetworkId, int transportStreamId, int serviceId,
                            boolean repeatPrograms) {
            this(id, displayName, displayNumber, icon, originalNetworkId, transportStreamId,
                    serviceId, repeatPrograms, null);
        }

        public XmlTvChannel(String id, String displayName, String displayNumber, XmlTvIcon icon,
                            int originalNetworkId, int transportStreamId, int serviceId,
                            boolean repeatPrograms, String url) {
            this.id = id;
            this.displayName = displayName;
            this.displayNumber = displayNumber;
            this.icon = icon;
            this.originalNetworkId = originalNetworkId;
            this.transportStreamId = transportStreamId;
            this.serviceId = serviceId;
            this.repeatPrograms = repeatPrograms;
            this.url = url;
        }

        @Override
        public String toString() {
            return displayNumber+" - "+displayName+": "+url+"\n";
        }
    }

    public static class XmlTvProgram {
        public final String channelId;
        public final String title;
        public final String description;
        public final XmlTvIcon icon;
        public final String[] category;
        public final long startTimeUtcMillis;
        public final long endTimeUtcMillis;
        public final XmlTvRating[] rating;
        public final String videoSrc;
        public final int videoType;

        private XmlTvProgram(String channelId, String title, String description, XmlTvIcon icon,
                             String[] category, long startTimeUtcMillis, long endTimeUtcMillis,
                             XmlTvRating[] rating, String videoSrc, int videoType) {
            this.channelId = channelId;
            this.title = title;
            this.description = description;
            this.icon = icon;
            this.category = category;
            this.startTimeUtcMillis = startTimeUtcMillis;
            this.endTimeUtcMillis = endTimeUtcMillis;
            this.rating = rating;
            this.videoSrc = videoSrc;
            this.videoType = videoType;
        }

        public long getDurationMillis() {
            return endTimeUtcMillis - startTimeUtcMillis;
        }
    }

    public static class XmlTvIcon {
        public final String src;

        public XmlTvIcon(String src) {
            this.src = src;
        }
    }

    public static class XmlTvRating {
        public final String system;
        public final String value;

        public XmlTvRating(String system, String value) {
            this.system = system;
            this.value = value;
        }
    }*/
}