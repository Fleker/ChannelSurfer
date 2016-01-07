# ChannelSurfer
Simple Live Channels library for Android TV

## CAUTION
Hello. If you're reading this it means you're very enthusiastic about this libary. So am I. That's why I wrote the README ahead of time. The library isn't live yet, but check back soon.

## What is this?
I had already gotten some experience with creating an app which supported Android TV's Live Channels when I developed and published <a href='https://github.com/fleker/cumulustv'></a>Cumulus TV</a>. 

As a fan of the feature, I wanted more apps to implement it. However, I knew firsthand that it could be difficult and time consuming. You had to deal with sync adapters, sqlite, and a lot of Live Channel code which isn't documented very much. (StackOverflow is not going to help.)

ChannelSurfer takes care of all these headaches. You can simply use a single class to take care of both the EPG (electronic program guide) and playback. It contains all the boilerplate code and XML files which can simply be imported by:

    compile 'com.github.fleker:channelsurfer:0.1.0'
    
## Manifest Changes
The necessary permissions are already added to your app, as are a built-in `SyncAdapter` and `DummyAccount` implementation. 

You do need to add a service to the manifest. This service is your Tv Input Provider.

    <service
            android:name=".livechannels.VineInputProvider"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.BIND_TV_INPUT">
            <intent-filter>
                <action android:name="android.media.tv.TvInputService" />
            </intent-filter>
            <meta-data
                android:name="android.media.tv.input"
                android:resource="@xml/channel_surfer_tv_input" />
        </service>
        
It is using the permission `android.permission.BIND_TV_INPUT` and has an intnet for being a Tv Input Service. This service will also have meta-data linking to an xml file that is included in the library.

You also need to have another piece of meta-data inside your application. This will tell the library which service you have selected so it can interface with it.

    <meta-data
            android:name="TvInputService"
            android:value="com.hitherejoe.vineyard.livechannels.VineInputProvider" />

### Customizing Sync Adapter
You can't do this yet. Neither can you configure the DummyAccount class. If people think it's a good idea, I'm all for it.

## `TvInputProvider`
Create your service with the same package and class name as stated above. This service should extend one of the following classes:

* `MediaPlayerInputProvider` if you plan on using Android's built-in `MediaPlayer`, this class removes more boilerplate code
* `TvInputProvider` otherwise

When this happens you will be asked to implement a handful of classes. 

**NOTE:** This service must have a constructor with no parameters. It doesn't have to be populated.

    public SampleTvInputProvider() { }
    
### Methods to Override
#### `List<Channel> getAllChannels()`
This method is meant to return all of the channels that this app provides. You can return a list where each item is a channel that you want to appear in the Live Channels app.

#### `List<Program> getProgramsForChannel(Uri channelUri, Channel channelInfo, long startTimeMs, long endTimeMs)`
This method is meant to return all of the programs within a given time interval (now to two weeks from now) for a specific channel. If you don't have any specific programs, you can use the `getGenericProgram` method listed a little later. However, you still need to adjust the start and end time for each program.

~ Show an example

#### `boolean onSetSurface(Surface surface)` 
*NOT required in the `MediaPlayerInputProvider`*

When the channel is loaded, it applies a surface. You can use this surface to display media.

#### `void onSetStreamVolume(float volume)`
*NOT required in the `MediaPlayerInputProvider`*

This method is called when the channel's volume changes.

#### `void onRelease()` 
*NOT required in the `MediaPlayerInputProvider`*

This method is called when the service closes.

#### `View onCreateOverlayView()`
You can display a view on top of your media source. Ordinarily this will be used for something simple like closed captioning but it can consist of any type of view. 

If you don't plan on using this, you can simply return null.

#### `boolean onTune(Channel channel)`
This method is called when a particular channel is selected. You will have to adjust what media is being played to reflect the channel switch.

### Other Useful Methods
#### `List<Channel> getCurrentChannels(Context mContext)`
You can get a list of channels that are already available in Live Channels

#### `List<Program> getPrograms(Context mContext, Uri channelUri)`
You can get a list of programs already available in Live Channels for a given channel

#### `Program getGenericProgram(Channel channel)`
You can create a simple program object that can be placed in the guide if you don't want to develop a static program guide. It'll be called "{channel name} Live".

#### `void notifyVideoAvailable()`

#### `void notifyVideoUnavailable(int reason)`

* `REASON_TUNING`
* `REASON_WEAK_SIGNAL`
* `REASON_AUDIO_ONLY`
* `REASON_BUFFERING`
* `REASON_UNKNOWN`

#### `void setOverlayEnabled(boolean enable)`


## Model
### Channel
The channel class represents a single channel, or a single stream of content.

### Program
The program class represents a single program, ie. a single tv show, movie, or video.

## Examples/Guides