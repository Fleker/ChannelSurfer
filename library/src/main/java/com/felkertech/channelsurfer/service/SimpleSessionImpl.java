package com.felkertech.channelsurfer.service;

import android.content.Context;
import android.database.Cursor;
import android.media.PlaybackParams;
import android.media.session.MediaSession;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputService;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.felkertech.channelsurfer.R;
import com.felkertech.channelsurfer.TimeShiftable;
import com.felkertech.channelsurfer.model.Channel;

/**
 * Simple session implementation which plays videos on the application's tune request, integrated
 * with a TvInputProvider.
 */
public class SimpleSessionImpl extends TvInputService.Session {
    private String TAG = "SimpleSession";
    private Channel currentChannel;
    private TvInputProvider tvInputProvider;
    private TvInputManager inputManager;
    SimpleSessionImpl(TvInputProvider tvInputProvider) {
        super(tvInputProvider);
        this.tvInputProvider = tvInputProvider;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && tvInputProvider instanceof TimeShiftable) {
            notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_AVAILABLE);
        }
    }
    @Override
    public void onRelease() {
        tvInputProvider.onRelease();
    }
    @Override
    public boolean onSetSurface(Surface surface) {
        return tvInputProvider.onSetSurface(surface);
    }
    @Override
    public void onSetStreamVolume(float volume) {
        tvInputProvider.onSetStreamVolume(volume);
    }

    @Override
    public void onSetCaptionEnabled(boolean enabled) {
        // The sample content does not have caption. Nothing to do in this sample input.
        // NOTE: If the channel has caption, the implementation should turn on/off the caption
        // based on {@code enabled}.
        // For the example implementation for the case, please see {@link RichTvInputService}.
    }
    @Override
    public View onCreateOverlayView() {
        return tvInputProvider.onCreateOverlayView();
    }

    @Override
    public boolean onTune(Uri channelUri) {
        notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);
        setOverlayViewEnabled(true);
        Log.d(TAG, "Tuning to " + channelUri.toString());
        String[] projection = {TvContract.Channels.COLUMN_DISPLAY_NAME, TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID,
                TvContract.Channels.COLUMN_SERVICE_ID, TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID,
                TvContract.Channels.COLUMN_INPUT_ID, TvContract.Channels.COLUMN_DISPLAY_NUMBER, TvContract.Channels._ID};
        //Now look up this channel in the DB
        try (Cursor cursor = tvInputProvider.getContentResolver().query(channelUri, projection, null, null, null)) {
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            }
            cursor.moveToNext();
            Log.d(TAG, "Tune to "+cursor.getInt(cursor.getColumnIndex(TvContract.Channels._ID)));
            Log.d(TAG, "And toon 2 "+channelUri);
            Channel channel = new Channel()
                    .setNumber(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NUMBER)))
                    .setName(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME)))
                    .setOriginalNetworkId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID)))
                    .setTransportStreamId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID)))
                    .setServiceId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_SERVICE_ID)))
                    .setChannelId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels._ID)))
                    .setVideoHeight(1080)
                    .setVideoWidth(1920);
            this.currentChannel = channel;
            TvInputManager mTvInputManager = (TvInputManager) tvInputProvider.getApplicationContext().getSystemService(Context.TV_INPUT_SERVICE);
            if(tvInputProvider.getApplicationContext().getResources().getBoolean(R.bool.channel_surfer_lifecycle_toasts))
                Toast.makeText(tvInputProvider.getApplicationContext(), "Parental controls enabled? "+mTvInputManager.isParentalControlsEnabled(), Toast.LENGTH_SHORT).show();
            if(mTvInputManager.isParentalControlsEnabled()) {
                TvContentRating blockedRating = null;
                for(int i=0;i<tvInputProvider.getProgramRightNow(channel).getContentRatings().length;i++) {
                    blockedRating = (mTvInputManager.isRatingBlocked(tvInputProvider.getProgramRightNow(channel).getContentRatings()[i]) && blockedRating == null)?tvInputProvider.getProgramRightNow(channel).getContentRatings()[i]:null;
                }
                if(tvInputProvider.getApplicationContext().getResources().getBoolean(R.bool.channel_surfer_lifecycle_toasts))
                    Toast.makeText(tvInputProvider.getApplicationContext(), "Is channel blocked w/ "+blockedRating+"? Only if not null", Toast.LENGTH_SHORT).show();
                if(blockedRating != null) {
                    notifyContentBlocked(blockedRating);
                }
            }
            notifyContentAllowed();
            return tvInputProvider.onTune(channel);
        } catch (Exception e) {
            Log.e(TAG, "Tuning error");
            if(tvInputProvider.getApplicationContext().getResources().getBoolean(R.bool.channel_surfer_lifecycle_toasts))
                Toast.makeText(tvInputProvider.getApplicationContext(), "There's an issue w/ tuning: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void layoutSurface(final int left, final int top, final int right,
                              final int bottom) {
        int[] surfaceDimensions = tvInputProvider.getLayoutDimensions();
        if(surfaceDimensions == null)
            super.layoutSurface(left, top, right, bottom);
        else
            super.layoutSurface(surfaceDimensions[0], surfaceDimensions[1], surfaceDimensions[2], surfaceDimensions[3]);
    }

    @Override
    public void onTimeShiftPause() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (tvInputProvider instanceof TimeShiftable) {
                ((TimeShiftable) tvInputProvider).onMediaPause();
            }
        }
    }

    @Override
    public void onTimeShiftResume() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (tvInputProvider instanceof TimeShiftable) {
                ((TimeShiftable) tvInputProvider).onMediaResume();
            }
        }
    }

    @Override
    public void onTimeShiftSeekTo(long timeMs) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (tvInputProvider instanceof TimeShiftable) {
                ((TimeShiftable) tvInputProvider).onMediaSeekTo(timeMs);
            }
        }
    }

    @Override
    public void onTimeShiftSetPlaybackParams(PlaybackParams params) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (tvInputProvider instanceof TimeShiftable) {
                ((TimeShiftable) tvInputProvider).onMediaSetPlaybackParams(params);
            }
        }
    }

    @Override
    public long onTimeShiftGetStartPosition() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (tvInputProvider instanceof TimeShiftable) {
                return ((TimeShiftable) tvInputProvider).mediaGetStartMs();
            } else {
                return TvInputManager.TIME_SHIFT_INVALID_TIME;
            }
        } else {
            return -1;
        }
    }

    @Override
    public long onTimeShiftGetCurrentPosition() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (tvInputProvider instanceof TimeShiftable) {
                return ((TimeShiftable) tvInputProvider).mediaGetCurrentMs();
            } else {
                return TvInputManager.TIME_SHIFT_INVALID_TIME;
            }
        } else {
            return -1;
        }
    }
}