package com.felkertech.channelsurfer.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputService;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.felkertech.channelsurfer.R;
import com.felkertech.channelsurfer.model.Channel;
import com.felkertech.channelsurfer.model.Program;
import com.felkertech.channelsurfer.sync.SyncAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by guest1 on 1/6/2016.
 */
public abstract class TvInputProvider extends TvInputService {
    private static String TAG = "TvInputProvider";
    /** Default constructor **/
    public TvInputProvider() { Log.d(TAG, "Created TvInputProvider"); }

    /**
     * Return a list of all the channels that you currently have created
     */
    public abstract List<Channel> getAllChannels();

    /**
     * Return a list of programs for a particular channel
     * @param channelUri The database Uri for this channel
     * @param channelInfo The channel
     * @param startTimeMs The starting period from which to get programs
     * @param endTimeMs The ending period from which to get programs
     * @return A list of programs
     */
    public abstract List<Program> getProgramsForChannel(Uri channelUri, Channel channelInfo, long startTimeMs, long endTimeMs);

    /**
     * Set your media player view onto the surface
     * @param surface The Live Channels surface
     * @return true if this was successful
     */
    public abstract boolean onSetSurface(Surface surface);

    /**
     * Called when the stream's volume is told to change
     * @param volume A percent, 0 - 1, for the volume
     */
    public abstract void onSetStreamVolume(float volume);
    /**
     * Called when the channel is going to close. Do clean up actions here.
     */
    public abstract void onRelease();

    /**
     * In addition to being able to display media or something else on a surface, you can
     * display some sort of view on top. This can be as simple as closed captioning or as complex
     * as a WebView.  Another idea is a screen that appears while your content is still buffering or loading.
     *
     * You will also need to set this overlay to be enabled
     * @return A view to be displayed on top of your media at certain times
     */
    public abstract View onCreateOverlayView();

    /**
     * This method is called when a user is initially opening your channel.
     * When first tuning, the session is going to declare the stream is unavailable due to
     * tuning. To display an overlay, you must enable the overlay and say your stream is available
     *
     * @param channel The channel that is tuning
     * @return true if the tuning was successful
     */
    public abstract boolean onTune(Channel channel);

    /**
     * If you are pulling channel and program data from a web source or are otherwise
     * doing it asynchronously, override the performCustomSync method and grab your
     * data. Once you're done syncing, call super.performCustomSync to finish syncing.
     *
     * @param syncAdapter
     * @param inputId
     */
    public void performCustomSync(SyncAdapter syncAdapter, String inputId) {
        syncAdapter.performSync(this, inputId);
    }

    /* Here are some helpful methods that I'd like to place in this class */

    /**
     * Goes into the TV guide and obtains the channels currently registered
     * @return An ArrayList of channels
     */
    public List<Channel> getCurrentChannels(Context mContext) {
        TvContentRating rating = TvContentRating.createRating(
                "com.android.tv",
                "US_TV",
                "US_TV_PG",
                "US_TV_D", "US_TV_L");
        try {
            ApplicationInfo app = getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            final String service = bundle.getString("TvInputService");
            String channels = TvContract.buildInputId(new ComponentName(getPackageName(), service.substring(getPackageName().length())));
            Uri channelsQuery = TvContract.buildChannelsUriForInput(channels);
            Log.d(TAG, channels + " " + channelsQuery.toString());
            List<Channel> list = new ArrayList<>();
            Cursor cursor = null;
            try {
                cursor = mContext.getContentResolver().query(channelsQuery, null, null, null, null);
                while(cursor != null && cursor.moveToNext()) {
                    Channel channel = new Channel()
                            .setNumber(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NUMBER)))
                            .setName(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME)))
                            .setOriginalNetworkId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID)))
                            .setTransportStreamId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID)))
                            .setServiceId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_SERVICE_ID)))
                            .setVideoHeight(1080)
                            .setVideoWidth(1920)
                            .setPrograms(getPrograms(getApplicationContext(),
                                    TvContract.buildChannelUri(cursor.getInt(cursor.getColumnIndex(TvContract.Channels._ID)))));
                    list.add(channel);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return list;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds already existing programs for a given channel
     * @param mContext The context of this application
     * @param channelUri The URI of the channel
     * @return
     */
    public List<Program> getPrograms(Context mContext, Uri channelUri) {
        ContentResolver resolver = mContext.getContentResolver();
        Uri uri = TvContract.buildProgramsUriForChannel(channelUri);
        Cursor cursor = null;
        List<Program> programs = new ArrayList<>();
        try {
            // TvProvider returns programs chronological order by default.
            cursor = resolver.query(uri, null, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                return programs;
            }
            while (cursor.moveToNext()) {
                programs.add(Program.fromCursor(cursor));
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to get programs for " + channelUri, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return programs;
    }

    /**
     * If you don't have access to an EPG or don't want to supply programs, you can simply
     * add several instances of this generic program object.
     *
     * Note you will have to set the start and end times manually.
     * @param channel The channel for which the program will be displayed
     * @return A very generic program object
     */
    public Program getGenericProgram(Channel channel) {
        TvContentRating rating = TvContentRating.createRating(
                "com.android.tv",
                "US_TV",
                "US_TV_PG",
                "US_TV_D", "US_TV_L");
        return new Program.Builder()
                .setTitle(channel.getName() + " Live")
                .setProgramId(channel.getServiceId())
//                .setEpisodeNumber(1)
//                .setSeasonNumber(1)
//                .setEpisodeTitle("Streaming")
                .setDescription("Currently streaming")
                .setLongDescription(channel.getName() + " is currently streaming live.")
                .setCanonicalGenres(new String[]{TvContract.Programs.Genres.ENTERTAINMENT})
                .setThumbnailUri(channel.getLogoUrl())
                .setPosterArtUri(channel.getLogoUrl())
                .setInternalProviderData(channel.getName())
                .setContentRatings(new TvContentRating[] {rating})
                .setVideoHeight(1080)
                .setVideoWidth(1920)
                .build();
    }

    public Program getProgramRightNow(Channel channel) {
        ApplicationInfo app = null;
        try {
            app = getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            final String service = bundle.getString("TvInputService");
            String channels = TvContract.buildInputId(new ComponentName(getPackageName(), service.substring(getPackageName().length())));
            Log.d(TAG, new ComponentName(getPackageName(), service.substring(getPackageName().length())).flattenToString());

            Uri channelsQuery = TvContract.buildChannelsUriForInput(channels);
            Cursor cursor = null;
            try {
                cursor = getApplicationContext().getContentResolver().query(channelsQuery, null, null, null, null);
                while(cursor != null && cursor.moveToNext()) {
                    Channel cursorChannel = new Channel()
                            .setNumber(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NUMBER)))
                            .setName(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME)))
                            .setOriginalNetworkId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID)))
                            .setTransportStreamId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID)))
                            .setServiceId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_SERVICE_ID)))
                            .setVideoHeight(1080)
                            .setVideoWidth(1920);
                    if(cursorChannel.getName().equals(channel.getName())) {
                        //!
                        List<Program> programs = getPrograms(getApplicationContext(),
                                TvContract.buildChannelUri(cursor.getInt(cursor.getColumnIndex(TvContract.Channels._ID))));
                        Program currentProgram = null;
                        for(Program p: programs) {
                            if(p.getStartTimeUtcMillis() < new Date().getTime()) {
                                currentProgram = p;
                                //Log.d(TAG, p.toString());
                            }
                        }
                        //Log.d(TAG, "OK");
                        return currentProgram;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds the last exact hour and returns that in MS. 1:52 would become 1:00.
     * @return An exact hour in milliseconds
     */
    public long getNearestHour() {
        return getNearestHour(new Date().getTime());
    }
    /**
     * Finds the last exact hour and returns that in MS. 1:52 would become 1:00.
     * @param startMs Your starting time
     * @return An exact hour in milliseconds
     */
    public long getNearestHour(long startMs) {
        return (long) (Math.floor(startMs/1000/60/60)*1000*60*60);
    }
    /**
     * Finds the last exact half hour and returns that in MS. 1:52 would become 1:30.
     * @return An exact half hour in milliseconds
     */
    public long getNearestHalfHour() {
        return (long) (Math.floor(new Date().getTime()/1000/60/30)*1000*60*30);
    }
    /**
     * Finds the last exact half hour and returns that in MS. 1:52 would become 1:30.
     * @param startMs Your starting time
     * @return An exact half hour in milliseconds
     */
    public long getNearestHalfHour(long startMs) {
        return (long) (Math.floor(startMs/1000/60/30)*1000*60*30);
    }

    /* TV Input Methods */
    SimpleSessionImpl simpleSession;
    @Nullable
    @Override
    public Session onCreateSession(String inputId) {
        simpleSession = new SimpleSessionImpl(this);
        Log.d(TAG, "Start session "+inputId);
        simpleSession.setOverlayViewEnabled(true);
        return simpleSession;
    }

    /**
     * You can notify Live Channels once your stream is available. It will allow the overlay to
     * appear
     */
    public void notifyVideoAvailable() {
        simpleSession.notifyVideoAvailable();
    }

    /**
     * You can notify Live Channels that this content is allowed to be displayed within the
     * confines of the parental controls
     */
    public void notifyVideoAllowed() {
        simpleSession.notifyContentAllowed();
    }

    /**
     * You can notify Live Channels that the content about to be played is disallowed due to
     * parental settings and will not be displayed
     */
    public void notifyVideoBlocked() {
//        simpleSession.notifyContentBlocked(getProgramRightNow());
    }

    /**
     * When your video is available, should an overlay be displayed on top of the stream
     * @param enable true if you want your overlay to be drawn on top
     */
    public void setOverlayEnabled(boolean enable) {
        simpleSession.setOverlayViewEnabled(enable);
    }

    protected static final int REASON_TUNING = TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING;
    protected static final int REASON_WEAK_SIGNAL = TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL;
    protected static final int REASON_AUDIO_ONLY = TvInputManager.VIDEO_UNAVAILABLE_REASON_AUDIO_ONLY;
    protected static final int REASON_BUFFERING = TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING;
    protected static final int REASON_UNKNOWN = TvInputManager.VIDEO_UNAVAILABLE_REASON_UNKNOWN;
    /**
     * You can notify Live Channels if your stream is currently unavailable
     * @param reason A common reason, look at the constants provided in this class
     */
    public void notifyVideoUnavailable(int reason) {
        simpleSession.notifyVideoUnavailable(reason);
    }

    /**
     * You can change the dimensions of the layout surface by providing the overlay size in pixels.
     * If none are provided, the default dimensions will be selected
     * @return an integer array for the left, top, right, and bottom directions in pixels
     */
    public int[] getLayoutDimensions() {
        return null;
    }

    /**
     * Gets a valid Uri of a local video file
     * @param assetname The resource id of the video
     * @return A Uri as a string
     */
    public String getLocalVideoUri(String assetname) {
        File f = new File(assetname);
        Log.d(TAG, "Video path "+f.getAbsolutePath());
        String uri = Uri.fromFile(f).toString();
        Log.d(TAG, "Uri "+uri);
        return uri;
//        return "file:/"+assetname;
//        return "asset:///"+assetname;
    }
    /**
     * An alias for the getLocalVideoUri method
     * @param assetname The resource id of the audio
     * @return A Uri as a string
     */
    public String getLocalAudioUri(String assetname) {
        return getLocalVideoUri(assetname);
    }

    public SimpleSessionImpl getSession() {
        return simpleSession;
    }
}
