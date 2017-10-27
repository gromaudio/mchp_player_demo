package com.gromaudio.simplifiedmediaplayer.players;

/**
 * Created by Vitaly Kuznetsov <v.kuznetsov.work@gmail.com> on 28.06.17.
 * Common interface for players (Android streaming (AOAP), USB, iTunes, etc).
 */

public interface IDemoPlayer {

    enum DemoPlayerState {
        ST_STOPPED,
        ST_PAUSED,
        ST_PLAYED
    };

    //DemoPlayer Capabilities
    public static final int CAP_COMMON   = 0x0000;
    public static final int CAP_PROGRESS = 0x0001;
    public static final int CAP_REPEAT   = 0x0002;
    public static final int CAP_SHUFFLE  = 0x0004;
    public static final int CAP_ALL  = CAP_PROGRESS|CAP_REPEAT|CAP_SHUFFLE;

    interface IDemoPlayerCallback {
        void onStateChanged();
    };

    interface IDemoPlayerCtl {
        void init(String param);
        void close();
    }

    /*
     * Controls
     */
    boolean play();

    boolean pause();

    boolean next();

    boolean prev();

    boolean seekTo(int position);

    boolean repeatSwitch();

    boolean shuffleSwitch();

    /*
     * States
     */
    DemoPlayerState getState();

    String getTrackName();

    String getArtistName();

    String getAlbumName();

    int getDuration();

    int getPosition();

    int getShuffle();

    int getRepeat();

    int getCapabilities();

    void setCallback(IDemoPlayerCallback callback);

}
