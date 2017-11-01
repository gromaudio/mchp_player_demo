package com.gromaudio.simplifiedmediaplayer.players;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.gromaudio.vlineservice.aoap.IAOAPService;

/**
 * Created by Vitaly Kuznetsov <v.kuznetsov.work@gmail.com> on 28.06.17.
 */
public class AoapPlayer implements IDemoPlayer, IDemoPlayer.IDemoPlayerCtl {

    private static final String TAG = "AoapPlayer";

    private static final String NATIVE_SERVICE_NAME = "vline.aoap";

    // AOAP_MEDIA_CONTROLS
    private static final int MEDIA_CONTROL_PLAY = 0;
    private static final int MEDIA_CONTROL_STOP = 1;
    private static final int MEDIA_CONTROL_PAUSE = 2;
    private static final int MEDIA_CONTROL_NEXT_TRACK = 6;
    private static final int MEDIA_CONTROL_PREV_TRACK = 7;

    private String mArtistName = "USB streaming";

    private String mAlbumName = "USB streaming";

    private String mTrackName = "Android phone";

    private IDemoPlayerCallback mPlayerCallback = null;

    private IAOAPService mNativeService = null;

    private DemoPlayerState mMediaState = DemoPlayerState.ST_STOPPED;

    private boolean mRepeatSwitch = false;

    private boolean mShuffleSwitch = false;


    /**********************************************************************
     ******                IDemoPlayer.IDemoPlayerCtl                ******
     *********************************************************************/
    @Override
    public void init(String param) {
        mMediaState = DemoPlayerState.ST_STOPPED;
        updateNativeServiceLink();
        if (mNativeService!=null) {
            try {
                mNativeService.onEvent(0, MEDIA_CONTROL_STOP);
            } catch (RemoteException ex) {
                updateNativeServiceLink();
            }
        }
    }

    @Override
    public void close() {
        mMediaState = DemoPlayerState.ST_STOPPED;
        if (mNativeService!=null) {
            try {
                mNativeService.onEvent(0, MEDIA_CONTROL_STOP);
            } catch (RemoteException ex) {
            }
        }
        mNativeService = null;
    }


    private IAOAPService getNativeService() {
        if (mNativeService == null) {
            updateNativeServiceLink();
        }
        return mNativeService;
    }

    private IAOAPService updateNativeServiceLink() {
        mNativeService = null;
        Log.d(TAG, "Connection to service " + NATIVE_SERVICE_NAME);
        IBinder binder = ServiceManager.getService(NATIVE_SERVICE_NAME);
        if (binder == null) {
            Log.e(TAG, "Unable to get service " + NATIVE_SERVICE_NAME);
            return null;
        }
        mNativeService = IAOAPService.Stub.asInterface(binder);
        Log.d(TAG, "mNativeService=" + mNativeService);
        return mNativeService;
    }

    private void setState(DemoPlayerState state) {
        if (mMediaState != state) {
            mMediaState = state;
            if (mPlayerCallback!=null) {
                mPlayerCallback.onStateChanged();
            }
        }
    }


    /******************************************************
    ******                IDemoPlayer                ******
    ******************************************************/
    /*
     * Controls
     */
    @Override
    public boolean play() {
        try {
            if (getNativeService()==null) {
                return false;
            }
            int res = getNativeService().onEvent(0, MEDIA_CONTROL_PLAY);
            if (res==0) {
                setState(DemoPlayerState.ST_PLAYED);
            }
            return (res==0);
        } catch (RemoteException ex) {
            updateNativeServiceLink();
            return false;
        }
    }
    @Override
    public boolean pause() {
        try {
            if (getNativeService()==null) {
                return false;
            }
            int res = getNativeService().onEvent(0, MEDIA_CONTROL_PAUSE);
            if (res==0) {
                setState(DemoPlayerState.ST_PAUSED);
            }
            return (res==0);
        } catch (RemoteException ex) {
            updateNativeServiceLink();
            return false;
        }
    }

    @Override
    public boolean next() {
        try {
            if (getNativeService()==null) {
                return false;
            }
            int res = getNativeService().onEvent(0, MEDIA_CONTROL_NEXT_TRACK);
            if (res==0) {
                if (mPlayerCallback != null) {
                    mPlayerCallback.onStateChanged();
                }
            }
            return (res==0);
        } catch (RemoteException ex) {
            updateNativeServiceLink();
            return false;
        }
    }

    @Override
    public boolean prev() {
        try {
            if (getNativeService()==null) {
                return false;
            }
            int res = getNativeService().onEvent(0, MEDIA_CONTROL_PREV_TRACK);
            if (res==0) {
                if (mPlayerCallback != null) {
                    mPlayerCallback.onStateChanged();
                }
            }
            return (res==0);
        } catch (RemoteException ex) {
            updateNativeServiceLink();
            return false;
        }
    }

    @Override
    public boolean seekTo(int position) {
        return false;
    }

    @Override
    public boolean repeatSwitch() {
        mRepeatSwitch = !mRepeatSwitch;
        return true;
    }

    @Override
    public boolean shuffleSwitch() {
        mShuffleSwitch = !mShuffleSwitch;
        return true;
    }

    /*
     * States
     */
    @Override
    public DemoPlayerState getState() {
        return mMediaState;
    }

    @Override
    public String getTrackName() {
        return mTrackName;
    }

    @Override
    public String getArtistName() {
        return mArtistName;
    }

    @Override
    public String getAlbumName() {
        return mAlbumName;
    }

    @Override
    public int getDuration() {
        return 100;
    }

    @Override
    public int getPosition() {
        return 50;
    }

    @Override
    public int getShuffle() {
        return mShuffleSwitch ? 1 : 0;
    }

    @Override
    public int getRepeat() {
        return mRepeatSwitch ? 1 : 0;
    }

    @Override
    public int getCapabilities() {
        return CAP_COMMON;
    }

    @Override
    public void setCallback(IDemoPlayerCallback callback) {
        mPlayerCallback = callback;
    }

}
