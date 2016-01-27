package com.felkertech.channelsurfer.model;

import java.util.List;

public class Channel {
    private String number;
    private String name;
    private String logoUrl;
    private String internalProviderData;
    private int originalNetworkId;
    private int transportStreamId;
    private int serviceId;
    private int videoWidth;
    private int videoHeight;
    private List<Program> programs;

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

    @Override
    public String toString() {
        return name+" ("+number+")";
    }
}
