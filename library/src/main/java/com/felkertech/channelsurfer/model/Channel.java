package com.felkertech.channelsurfer.model;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.text.TextUtils;

import com.felkertech.channelsurfer.utils.TvContractUtils;

import java.util.List;

public class Channel {
    private String number;
    private String name;
    private String logoUrl;
    private String internalProviderData;
    private String description;
    private int originalNetworkId;
    private int transportStreamId;
    private int serviceId;
    private int videoWidth;
    private int videoHeight;
    private List<Program> programs;
    private int channelId;
    private boolean audioOnly = false;
    //App Links
    private String appLinkColor;
    private String appLinkIcon;
    private String appLinkIntent;
    private String appLinkPoster;
    private String appLinkText;

    private static final long INVALID_LONG_VALUE = -1;
    private static final int INVALID_INT_VALUE = -1;

    public Channel() {}
    public Channel(String number, String name, String logoUrl, int originalNetworkId,
                       int transportStreamId, int serviceId, int videoWidth, int videoHeight,
                       List<Program> programs) {
        this.number = number;
        this.name = name;
        this.logoUrl = logoUrl;
        this.originalNetworkId = originalNetworkId;
        this.transportStreamId = transportStreamId;
        this.serviceId = serviceId;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.programs = programs;
    }

    public String getNumber() {
        return number;
    }

    public Channel setNumber(String number) {
        this.number = number;
        return this;
    }

    public String getName() {
        return name;
    }

    public Channel setName(String name) {
        this.name = name;
        return this;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public Channel setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
        return this;
    }

    /**
     * If the logo is local, you can provide a drawable instead of a URL
     * Provide the id from R.drawable.{id} as a String
     * @param id resource name of your logo
     * @param yourPackageName Package name of your app (should be a temporary thing)
     * @return Itself
     */
    public Channel setLogoDrawable(String id, String yourPackageName) {
        String endpoint = "android.resource://"+id+"/drawable/";
        this.logoUrl = endpoint + id;
        return this;
    }

    public int getOriginalNetworkId() {
        return originalNetworkId;
    }

    public Channel setOriginalNetworkId(int originalNetworkId) {
        this.originalNetworkId = originalNetworkId;
        return this;
    }

    public int getTransportStreamId() {
        return transportStreamId;
    }

    public Channel setTransportStreamId(int transportStreamId) {
        this.transportStreamId = transportStreamId;
        return this;
    }

    public int getServiceId() {
        return serviceId;
    }

    public Channel setServiceId(int serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public Channel setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
        return this;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public Channel setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
        return this;
    }

    public List<Program> getPrograms() {
        return programs;
    }

    public Channel setPrograms(List<Program> programs) {
        this.programs = programs;
        return this;
    }

    public String getInternalProviderData() {
        return internalProviderData;
    }

    public Channel setInternalProviderData(String internalProviderData) {
        this.internalProviderData = internalProviderData;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Channel setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return name+" ("+number+")";
    }

    public int getChannelId() {
        return channelId;
    }

    public Channel setChannelId(int channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getAppLinkColor() {
        return appLinkColor;
    }

    public Channel setAppLinkColor(String appLinkColor) {
        this.appLinkColor = appLinkColor;
        return this;
    }

    /**
     * Sets the color based on a resource. Even if the app linking intent does not work,
     * this color will still be changed.
     * @param appLinkColor
     * @return
     */
    public Channel setAppLinkColor(int appLinkColor) {
        this.appLinkColor = appLinkColor+"";
        return this;
    }

    public String getAppLinkIcon() {
        return appLinkIcon;
    }

    /**
     * Sets the icon which will appear in the App linking tile. If this is not defined,
     * it will default to your app's icon
     * @param appLinkIcon
     * @return
     */
    public Channel setAppLinkIcon(String appLinkIcon) {
        this.appLinkIcon = appLinkIcon;
        return this;
    }

    public String getAppLinkIntent() {
        return appLinkIntent;
    }

    public Channel setAppLinkIntent(String appLinkIntent) {
        this.appLinkIntent = appLinkIntent;
        return this;
    }

    /**
     * This sets the action that will occur when the user presses the channel
     * If this doesn't work, or the text/icon doesn't display, then this intent will
     * not work. Try a different one.
     *
     * If this intent isn't set, the text and icon will not appear
     * @param appLinkIntent
     * @return
     */
    public Channel setAppLinkIntent(Intent appLinkIntent) {
        this.appLinkIntent = appLinkIntent.toUri(Intent.URI_INTENT_SCHEME);
        return this;
    }

    public String getAppLinkPoster() {
        return appLinkPoster;
    }

    public Channel setAppLinkPoster(String appLinkPoster) {
        this.appLinkPoster = appLinkPoster;
        return this;
    }

    public String getAppLinkText() {
        return appLinkText;
    }

    public Channel setAppLinkText(String appLinkText) {
        this.appLinkText = appLinkText;
        return this;
    }

    public boolean isAudioOnly() {
        return audioOnly;
    }

    public void copyFrom(Channel other) {
        if (this == other) {
            return;
        }

        internalProviderData = other.internalProviderData;
        name = other.name;
        number = other.number;
        videoHeight = other.videoHeight;
        videoWidth = other.videoWidth;
        appLinkColor = other.appLinkColor;
        appLinkIcon = other.appLinkIcon;
        appLinkIntent = other.appLinkIntent;
        appLinkPoster = other.appLinkPoster;
        appLinkText = other.appLinkText;
        channelId = other.channelId;
        description = other.description;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(name)) {
            values.put(TvContract.Channels.COLUMN_DISPLAY_NAME, name);
        } else {
            values.putNull(TvContract.Channels.COLUMN_DISPLAY_NAME);
        }
        if (!TextUtils.isEmpty(number)) {
            values.put(TvContract.Channels.COLUMN_DISPLAY_NUMBER, number);
        } else {
            values.putNull(TvContract.Channels.COLUMN_DISPLAY_NUMBER);
        }
        if (channelId != INVALID_INT_VALUE) {
            values.put(TvContract.Channels.COLUMN_INPUT_ID, channelId);
        } else {
            values.putNull(TvContract.Channels.COLUMN_INPUT_ID);
        }
        if (!TextUtils.isEmpty(description)) {
            values.put(TvContract.Channels.COLUMN_DESCRIPTION, description);
        } else {
            values.putNull(TvContract.Channels.COLUMN_DESCRIPTION);
        }
        if (!TextUtils.isEmpty(appLinkPoster)) {
            values.put(TvContract.Channels.COLUMN_APP_LINK_POSTER_ART_URI, appLinkPoster);
        } else {
            values.putNull(TvContract.Channels.COLUMN_APP_LINK_POSTER_ART_URI);
        }
        if (!TextUtils.isEmpty(appLinkIcon)) {
            values.put(TvContract.Channels.COLUMN_APP_LINK_ICON_URI, appLinkIcon);
        } else {
            values.putNull(TvContract.Channels.COLUMN_APP_LINK_ICON_URI);
        }
        if (!TextUtils.isEmpty(appLinkColor)) {
            values.put(TvContract.Channels.COLUMN_APP_LINK_COLOR, appLinkColor);
        } else {
            values.putNull(TvContract.Channels.COLUMN_APP_LINK_COLOR);
        }
        if (!TextUtils.isEmpty(appLinkIntent)) {
            values.put(TvContract.Channels.COLUMN_APP_LINK_INTENT_URI, appLinkIntent);
        } else {
            values.putNull(TvContract.Channels.COLUMN_APP_LINK_INTENT_URI);
        }
        if (!TextUtils.isEmpty(appLinkText)) {
            values.put(TvContract.Channels.COLUMN_APP_LINK_TEXT, appLinkText);
        } else {
            values.putNull(TvContract.Channels.COLUMN_APP_LINK_TEXT);
        }
        if (!TextUtils.isEmpty(internalProviderData)) {
            values.put(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA, internalProviderData);
        } else {
            values.putNull(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA);
        }
        if(isAudioOnly()) {
            values.put(TvContract.Channels.COLUMN_SERVICE_TYPE, TvContract.Channels.SERVICE_TYPE_AUDIO);
        } else {
            values.put(TvContract.Channels.COLUMN_SERVICE_TYPE, TvContract.Channels.SERVICE_TYPE_AUDIO_VIDEO);
        }
        return values;
    }

    public static Channel fromCursor(Cursor cursor) {
        Builder builder = new Builder();
        int index = cursor.getColumnIndex(TvContract.Channels._ID);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setChannelId((int) cursor.getLong(index));
        }
        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setTitle(cursor.getString(index));
        }
        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NUMBER);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setChannelNumber(cursor.getString(index));
        }
        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_APP_LINK_COLOR);
        if(index >= 0 && !cursor.isNull(index)) {
            builder.setAppLinkColor(cursor.getString(index));
        }
        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_APP_LINK_ICON_URI);
        if(index >= 0 && !cursor.isNull(index)) {
            builder.setAppLinkIcon(cursor.getString(index));
        }
        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_APP_LINK_INTENT_URI);
        if(index >= 0 && !cursor.isNull(index)) {
            builder.setAppLinkIntent(cursor.getString(index));
        }
        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_APP_LINK_POSTER_ART_URI);
        if(index >= 0 && !cursor.isNull(index)) {
            builder.setAppLinkPoster(cursor.getString(index));
        }
        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_APP_LINK_TEXT);
        if(index >= 0 && !cursor.isNull(index)) {
            builder.setAppLinkText(cursor.getString(index));
        }
        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_DESCRIPTION);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setDescription(cursor.getString(index));
        }
        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setInternalProviderData(cursor.getString(index));
        }
        return builder.build();
    }

    public static final class Builder {
        private final Channel mChannel;

        public Builder() {
            mChannel = new Channel();
        }

        public Builder(Channel other) {
            mChannel = new Channel();
            mChannel.copyFrom(other);
        }

        public Builder setChannelId(int channelId) {
            mChannel.channelId = channelId;
            return this;
        }

        public Builder setTitle(String title) {
            mChannel.name = title;
            return this;
        }

        public Builder setChannelNumber(String title) {
            mChannel.number = title;
            return this;
        }

        public Builder setDescription(String description) {
            mChannel.description = description;
            return this;
        }

        public Builder setVideoWidth(int width) {
            mChannel.videoWidth = width;
            return this;
        }

        public Builder setVideoHeight(int height) {
            mChannel.videoHeight = height;
            return this;
        }

        public Builder setInternalProviderData(String data) {
            mChannel.internalProviderData = data;
            return this;
        }

        public Builder setAppLinkColor(String appLinkColor) {
            mChannel.appLinkColor = appLinkColor;
            return this;
        }

        public Builder setAppLinkIcon(String appIconUri) {
            mChannel.appLinkIcon = appIconUri;
            return this;
        }

        public Builder setAppLinkIntent(String appLinkIntent) {
            mChannel.appLinkIntent = appLinkIntent;
            return this;
        }
        public Builder setAppLinkPoster(String appLinkIntent) {
            mChannel.appLinkPoster = appLinkIntent;
            return this;
        }
        public Builder setAppLinkText(String appLinkIntent) {
            mChannel.appLinkText = appLinkIntent;
            return this;
        }
        public Builder setAudioOnly(boolean isAudioOnly) {
            mChannel.audioOnly = isAudioOnly;
            return this;
        }

        public Channel build() {
            return mChannel;
        }
    }
}
