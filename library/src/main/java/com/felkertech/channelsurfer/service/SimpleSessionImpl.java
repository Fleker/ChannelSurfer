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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.felkertech.channelsurfer.R;
import com.felkertech.channelsurfer.TimeShiftable;
import com.felkertech.channelsurfer.model.Channel;

import java.sql.Time;
import java.util.Date;

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
        Log.d(TAG, "Time shiftable? "+tvInputProvider.getClass().getSimpleName());
        Log.d(TAG, "Time shiftable? "+(tvInputProvider instanceof TimeShiftable) + " && "+ (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && tvInputProvider instanceof TimeShiftable) {
            Log.d(TAG, "Notifying that we can time shift");
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

//    private TvContentRating blocked;
    protected Date lastTune;
    @Override
    public boolean onTune(Uri channelUri) {
        lastTune = new Date();
        notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);
        setOverlayViewEnabled(true);
        new TuningTask().execute(channelUri, this);
        return true;
    }
//    private boolean isBlocked() {
//        return blocked!=null;
//    }

    @Override
    public void onUnblockContent(TvContentRating unblockedRating) {
        super.onUnblockContent(unblockedRating);
        if(tvInputProvider.getApplicationContext().getResources().getBoolean(R.bool.channel_surfer_lifecycle_toasts))
            Toast.makeText(tvInputProvider.getApplicationContext(), "Unblocked "+unblockedRating.flattenToString(), Toast.LENGTH_SHORT).show();
        notifyContentAllowed();
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

    /**
     * This AsyncTask is used to do tuning operations in the background in order to reduce the load
     * on the main UI thread. The TuningTask takes in two key parameters in the doInBackground method:
     *   * Uri channelUri: This is the URI corresponding to the channel you're tuning
     *   * SimpleSessionImpl session: `this`, which is used to set variables in the main class
     */
    private class TuningTask extends AsyncTask<Object, Void, Void> {
        Channel channel;
        @Override
        protected Void doInBackground(Object... params) {
            Uri channelUri = (Uri) params[0];
            SimpleSessionImpl session = (SimpleSessionImpl) params[1];
            TvContentRating blocked = null; //Reset our channel blocking until we check again
            Log.d(TAG, "Tuning to " + channelUri.toString());
            String[] projection = {TvContract.Channels.COLUMN_DISPLAY_NAME, TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID,
                    TvContract.Channels.COLUMN_SERVICE_ID, TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID,
                    TvContract.Channels.COLUMN_INPUT_ID, TvContract.Channels.COLUMN_DISPLAY_NUMBER, TvContract.Channels._ID};
            //Now look up this channel in the DB
            try (Cursor cursor = tvInputProvider.getContentResolver().query(channelUri, projection, null, null, null)) {
                if (cursor == null || cursor.getCount() == 0) {
                    return null;
                }
                cursor.moveToNext();
                Log.d(TAG, "Tune to "+cursor.getInt(cursor.getColumnIndex(TvContract.Channels._ID)));
                Log.d(TAG, "And toon 2 "+channelUri);
                channel = new Channel()
                        .setNumber(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NUMBER)))
                        .setName(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME)))
                        .setOriginalNetworkId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID)))
                        .setTransportStreamId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID)))
                        .setServiceId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_SERVICE_ID)))
                        .setChannelId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels._ID)))
                        .setVideoHeight(1080)
                        .setVideoWidth(1920);
                session.currentChannel = channel;
                TvInputManager mTvInputManager = (TvInputManager) tvInputProvider.getApplicationContext().getSystemService(Context.TV_INPUT_SERVICE);
                sendToast("Parental controls enabled? "+mTvInputManager.isParentalControlsEnabled());
                if(mTvInputManager.isParentalControlsEnabled()) {
                    TvContentRating blockedRating = null;
                    for(int i=0;i<tvInputProvider.getProgramRightNow(channel).getContentRatings().length;i++) {
                        blockedRating = (mTvInputManager.isRatingBlocked(tvInputProvider.getProgramRightNow(channel).getContentRatings()[i]) && blockedRating == null)?tvInputProvider.getProgramRightNow(channel).getContentRatings()[i]:null;
                    }
                    sendToast("Is channel blocked w/ "+blockedRating+"? Only if not null");
                    blocked = blockedRating;
                    if(blockedRating != null) {
                        notifyContentBlocked(blockedRating);
                    } else {
                        notifyContentAllowed();
                    }
                }
                Bundle b = new Bundle();
                b.putBoolean("tune", true);
                Message complete = new Message();
                complete.setData(b);
                toaster.sendMessage(complete);
            } catch (Exception e) {
                Log.e(TAG, "Tuning error");
                sendToast("There's an issue w/ tuning: "+e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        Handler toaster = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.getData().getBoolean("tune")) {
                    tvInputProvider.onTune(channel);
                } else {
                    if (tvInputProvider.getApplicationContext().getResources().getBoolean(R.bool.channel_surfer_lifecycle_toasts))
                        Toast.makeText(tvInputProvider.getApplicationContext(), msg.getData().getString("msg"), Toast.LENGTH_SHORT).show();
                }
            }
        };

        private void sendToast(String msg) {
            Bundle b = new Bundle();
            b.putString("msg", msg);
            Message m = new Message();
            m.setData(b);
            toaster.sendMessage(m);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}