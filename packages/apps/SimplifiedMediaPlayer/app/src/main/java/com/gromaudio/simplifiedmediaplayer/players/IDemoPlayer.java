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


    void setCallback(IDemoPlayerCallback callback);

}
