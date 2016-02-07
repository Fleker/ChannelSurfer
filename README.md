# ChannelSurfer
Simple Live Channels library for Android TV

## What is this?
I had already gotten some experience with creating an app which supported Android TV's Live Channels when I developed and published <a href='https://github.com/fleker/cumulustv'></a>Cumulus TV</a>. 

As a fan of the feature, I wanted more apps to implement it. However, I knew firsthand that it could be difficult and time consuming. You had to deal with sync adapters, sqlite, and a lot of Live Channel code which isn't documented very much. (StackOverflow is not going to help.)

ChannelSurfer takes care of all these headaches. You can simply use a single class to take care of both the EPG (electronic program guide) and playback. It contains all the boilerplate code and XML files which can simply be imported by:

    compile 'com.github.fleker:channelsurfer:0.2.3'
    
## Release Notes
### 0.2.3
* Should now work correctly with restricted profiles
* Can change the account icon by including `@drawable/ic_account`
* Check the section on using the `TimeShiftable` interface to display playback controls
* Sample app shows how to play from a variety of sources: website, HLS stream, local video, local audio with overlay

### 0.2.1
* Imports and uses the `ExoPlayer` library in most of its `TvInputProvider` classes
* More `TvInputProvider` classes for more specific use cases: `ExoPlayerInputProvider`, `MediaPlayerInputProvider`, `StreamingInputProvider`
* `Channel` class now has an `internalProviderData` attribute to store plain data

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

An activity needs to be declared that is opened when Live Channels sets up your source. It must be declared in the manifest as well: 
    
    <activity
            android:name=".SampleTvSetup"
            android:exported="true"
            android:enabled="true"
            android:label="@string/title_activity_sample_tv_setup">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
            </intent-filter>
        </activity>
            
And as a string resource pointing to it must declare the full class name:
    
    <string name="tv_input_activity">com.felkertech.sample.channelsurfer.SampleTvSetup</string>
            
But this can simply extend the `SimpleTvSetup` activity without overriding any methods.

    public class SampleTvSetup extends SimpleTvSetup {
    
    }
    
If you want to provide an icon for your app in the system's account settings, you can provide a custom 
drawable named `ic_account`.

### Customizing Sync Adapter
You can't do this yet. Neither can you configure the DummyAccount class. If people think it's a good idea, I'm all for it.

### Customizing Setup Activity
The default setup activity is not very attractive. You may want to create a setup activity that uses your own branding. If so, your Setup Activity can override a couple of methods.

    @Override
    public void displayLayout() 
    
The `displayLayout` method, as the name suggests, sets the content view of your window based on a layout. Any view based logic should be placed here.

    @Override
    public void setupTvInputProvider()
    
The `setupTvInputProvider` method is called when the syncing process begins. This methods should contain your activity's behaviors. If you override this, make sure you call `requestSync` at some point.

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
#### `void performCustomSync(SyncAdapter syncAdapter, String inputId)`
If you are pulling channel and program data from a web source or are otherwise doing it asynchronously, override the performCustomSync method and grab your data. Once you're done syncing, call `super.performCustomSync` to finish syncing.

#### `List<Channel> getCurrentChannels(Context mContext)`
You can get a list of channels that are already available in Live Channels

#### `List<Program> getPrograms(Context mContext, Uri channelUri)`
You can get a list of programs already available in Live Channels for a given channel

#### `Program getGenericProgram(Channel channel)`
You can create a simple program object that can be placed in the guide if you don't want to develop a static program guide. It'll be called "{channel name} Live".

#### `void notifyVideoAvailable()`
When your video is available, notify the system so that it can display the channel surface.

#### `void notifyVideoUnavailable(int reason)`
When your video is not available, notify the system with a particular reason to not display janky or borken media. You can use on the constants below.

* `REASON_TUNING`
* `REASON_WEAK_SIGNAL`
* `REASON_AUDIO_ONLY`
* `REASON_BUFFERING`
* `REASON_UNKNOWN`

#### `void setOverlayEnabled(boolean enable)`
To display your custom view on top of the channel surface, you must enable it. Additionally, you must notify the system that your video is available.

#### `Program getProgramRightNow(Channel channel)`
What's playing on a given channel right now? This method returns that program.

#### Get Nearest Hour / Half-Hour
Rounding is nice. Many channels will organize programs into hour or half-hour segments so that it's easy for people to remember.  

`getNearestHour()` and `getNearestHalfHour()` will round the current time down to the hour or half-hour respectively so you can quickly insert those times into your programs.

You can also pass any number of milliseconds and that time will be rounded down as well.

#### Get local video uri
If your stream is playing a video that already exists, you can use the method `getLocalVideoUri(int resId, String packageName)` to get the Uri of this video as a string. There is an alias method `getLocalAudioUri(int resId, String packageName)` which can be used for local audio files.

### WebViewInputProvider
What if you could set any website as a Live Channel? There's any easy way to do that. Just extend the `WebViewInputProvider` class. It simplifies the methods that you need to override. 

In your `onTune` method, you can simply call `loadUrl(String url)` to load the site.

### ExoPlayerInputProvider
The `MediaPlayer` class is built-in, but the external <a href='https://github.com/google/ExoPlayer/'>`ExoPlayer`</a> library provides more functionality and support. This class can play most types of video URLs through the `play(String)` method.

### MultimediaInputProvider
If you plan for your `TvInputService` to use both video streams through ExoPlayer and load websites, you can extend this class. When you start a program through the `play` method, it will play a video or open a website depending on the type of URL passed.

### StreamingInputProvider
This is the simplest class, designed for any sort of web URL or web stream. All you have to do is provide a list of channels, making sure you provide a URL in the `setInternalProviderData(String)` method. Program generation and playback are provided. 

## Model
### Channel
The channel class represents a single channel, or a single stream of content. A channel should, at the very least, have a channel name and channel number.

| Methods | Return | Description |
| :---    | :---   | :---        |
| `Channel()` | `Channel` | Creates a new channel object |
| `getNumber()` | `String` | Returns the channel number | 
| `setNumber(String)` | `Channel` | Changes the channel number, returns itself |
| `getName()` | `String` | Returns the name of the channel |
| `setName(String)` | `Channel` | Changes the channel name, returns itself |
| `getLogoUrl()` | `String` | Returns the URL of the channel's logo |
| `setLogoUrl(String)` | `Channel` | Changes the channel logo, returns itself |
| `getOriginalNetworkId()` | `int` | Returns the channel's original network id (don't worry about this) |
| `setOriginalNetworkId(int)` | `Channel` | Changes the channel's original network id (don't wory about this) |
| `getTransportStreamId()` | `int` | Returns the channel's transport stream id (don't worry about this) |
| `setTransportStreamId(int)` | `Channel` | Changes the channel's transport stream id (don't wory about this) |
| `getServiceId()` | `int` | Returns the channel's service id (don't worry about this) |
| `setServiceId(int)` | `Channel` | Changes the channel's service id (don't wory about this) |
| `getVideoWidth()` | `int` | Gets the width of a standard video on this channel 
| `setVideoWidth(int)` | `Channel` | Changes the width of a standard video on the channel (ie. 1920), returns itself
| `getVideoheight()` | `int` | Gets the height of a standard video on this channel |
| `setVideoheight(int)` | `Channel` | Changes the height of a standard video on the channel (ie. 1080), returns itself |
| `getInternalProviderData()` | `String` | Gets a user-defined string of data |
| `setInternalProviderData(String)` | `Channel` | Changes the user-defined string of data |
| `getPrograms()` | `List<Program>` | Returns a list of programs that are on this channel |
| `setPrograms(List<Program>)` | `Channel` | Sets the list of programs that are on this channel, returns itself. Note this will not be used. You should set your programs through the `getProgramsForChannel` method.
| `toString()` | `String` | Returns a string representation of this channel


### Program
The program class represents a single program, ie. a single tv show, movie, or video.

To create a new Program, create a new `Program.Builder()` object.

#### Methods in Program.Builder
| Method Name | Description |
| :--- |        :--- |         :--- |
| `setProgramId(long)` | Sets the id of this program |
| `setTitle(String)`   | Sets the name of this program, ie. the series name | 
| `setEpisodeTitle(String)` | For episodic content, this sets the  title of that particular episode |
| `setSeasonNumber(int)` | For episodic content, this sets the season or series of that particular episode |
| `setEpisodeNumber(int)` | For episodic content, this sets the number of that particular episode within a given season |
| `setStartTimeUtcMillis(long)` | Sets the start time of this program, in MS since the Unix epoch |
| `setEndTimeUtcMillis(long)` | Sets the end time of this program, in MS since the Unix epoch |
| `setDescription(String)` | Sets the description of your program, can be an episode summary. It should <= 255 chars |
| `setLongDescription(String)` | Sets a long description of your program. This is optional, and it doesn't seem exposed in the UI at the moment |
| `setVideoWidth(int)` | Sets the video width for this program |
| `setVideoHeight(int)` | Sets the video height for this program |
| `setContentRatings(TvContentRating[])` | Sets the ratings for your program ,useful for parental controls, but not strictly necessary |
| `setPosterArtUri(String)` | Sets the location for the program's poster art; can be null |
| `setThumbnailUri(String)` | Sets the location for the program's thumbnail; can be null |
| `setCanonicalGenres(String)` | Sets the program's genres. When set, they will allow users to find this program by filtering in the program guide |
| `setInternalProviderData(String)` | If you want to set any particular data, here is where you would do it |
| `build()` | This creates your program object |
| `setChannelId(long)` | Sets the channel. (Don't worry about this) |

#### Methods in Program Class
When you finished building your program, there are more things you can do with it. There are getters for all of the properties that you have set. Additionally,

`setStartTimeUtcMillis(long)` and `setEndTimeUtcMillis(long)` allow you to adjust the start and end time for a given program, fine tuning it until you return the final list for syncing. 

To find out how long a program is in milliseconds, call the `getDuration()` method.

## Time Shifting
In many modern channel streaming services, you don't only have to provide a live stream of the chosen content but also the ability to pause and move back in time. For some Tv input services, this may not be valid if it's being streamed directly from a third-party stream. However, for channels and programs that have this ability, it should be implemented to provide users the best experience.

In order to support time shifting, your `TvInputProvider` must implement the `TimeShiftable` interface and then implement the methods. It does not appear as if these APIs enabled in the Live Channels app though.

**Note: these APIs on only available on Android Marshmallow (API version 23) or higher**

## Examples/Guides
### Returning generic channels
In a Live Channel for quick videos, like a stream of Vines, it may not make sense to add hundreds of tiny videos to a guide. Instead, you can just display long chunks of time devoted to streams.

In this snippet, we create a number of programs, each an hour long on the hour, with some custom properties based on the channel name.

    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channelInfo, long startTimeMs, long endTimeMs) {
        int programs = (int) ((endTimeMs-startTimeMs)/1000/60/60); //Hour long segments
        int SEGMENT = 1000*60*60; //Hour long segments
        List<Program> programList = new ArrayList<>();
        for(int i=0;i<programs;i++) {
            programList.add(new Program.Builder(getGenericProgram(channelInfo))
                            .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                            .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
                            .build()
            );
        }
        return programList;
    }