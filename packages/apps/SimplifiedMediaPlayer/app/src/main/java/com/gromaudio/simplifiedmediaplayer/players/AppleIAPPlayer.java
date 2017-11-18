package com.gromaudio.simplifiedmediaplayer.players;

import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.gromaudio.vlineservice.apple.IAppleIAPService;
import com.gromaudio.vlineservice.apple.IAppleIAPServiceListener;

/**
 * Created by Vitaly Kuznetsov <v.kuznetsov.work@gmail.com> on 24.10.17.
 */
public class AppleIAPPlayer implements IDemoPlayer, IDemoPlayer.IDemoPlayerCtl {

    private static final String TAG = "AppleIAPPlayer";

    private static final String NATIVE_SERVICE_NAME = "vline.apple";

    // MEDIA_CONTROLS
    private static final int MEDIA_CONTROL_PLAY = 0;
    private static final int MEDIA_CONTROL_STOP = 1;
    private static final int MEDIA_CONTROL_PAUSE = 2;
    private static final int MEDIA_CONTROL_NEXT_TRACK = 6;
    private static final int MEDIA_CONTROL_PREV_TRACK = 7;
    private static final int MEDIA_CONTROL_REPEAT = 8;
    private static final int MEDIA_CONTROL_RANDOM = 9;
    
    private String mArtistName = "iTunes streaming";

    private String mAlbumName = "iTunes streaming";

    private String mTrackName = "iPhone USB streaming";

    private int mDuration = 100;

    private int mPosition = 0;

    private IDemoPlayerCallback mPlayerCallback = null;

    private IAppleIAPService mNativeService = null;

    private AppleIAPServiceListener mNativeServiceListener = new AppleIAPServiceListener();

    private DemoPlayerState mMediaState = DemoPlayerState.ST_STOPPED;

    private int mRepeatSwitch = 0;

    private int mShuffleSwitch = 0;

    private Handler mHandler = new Handler();

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
                mNativeService.removeListener(mNativeServiceListener);
            }catch (RemoteException ex) {
                Log.e(TAG, " removeListener() ex: " + ex);
            }
        }
        mNativeService = null;
    }


    private IAppleIAPService getNativeService() {
        if (mNativeService == null) {
            updateNativeServiceLink();
        }
        return mNativeService;
    }

    private IAppleIAPService updateNativeServiceLink() {
        mNativeService = null;
        Log.d(TAG, "Connection to service " + NATIVE_SERVICE_NAME);
        IBinder binder = ServiceManager.getService(NATIVE_SERVICE_NAME);
        if (binder == null) {
            Log.e(TAG, "Unable to get service " + NATIVE_SERVICE_NAME);
            return null;
        }
        mNativeService = IAppleIAPService.Stub.asInterface(binder);
        Log.d(TAG, "mNativeService=" + mNativeService);

        try {
            mNativeService.addListener(mNativeServiceListener);
        }catch (RemoteException ex) {
            Log.e(TAG, " addListener() ex: " + ex);
        }

        return mNativeService;
    }

    private void setState(DemoPlayerState state) {
        if (mMediaState != state) {
            mMediaState = state;
            onStateChanged();
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
            if(updateNativeServiceLink() != null) {
                play();
            }
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
            if(updateNativeServiceLink() != null) {
                pause();
            }
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
                onStateChanged();
            }
            return (res==0);
        } catch (RemoteException ex) {
            if(updateNativeServiceLink() != null) {
                next();
            }
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
                onStateChanged();
            }
            return (res==0);
        } catch (RemoteException ex) {
            if(updateNativeServiceLink() != null) {
                prev();
            }
            return false;
        }
    }

    @Override
    public boolean seekTo(int position) {
        return false;
    }

    @Override
    public boolean repeatSwitch() {
        try {
            if (getNativeService()!=null) {
                int res = getNativeService().onEvent(0, MEDIA_CONTROL_REPEAT);
                if (res==0) {
                    if (mRepeatSwitch == 0) {
                        mRepeatSwitch = 2;
                    }
                    else if (mRepeatSwitch == 2) {
                        mRepeatSwitch = 1;
                    }
                    else {
                        mRepeatSwitch = 0;
                    }
                }
            }
        } catch (RemoteException ex) {
            if(updateNativeServiceLink() != null) {
                repeatSwitch();
            }
        }
        return true;
    }

    @Override
    public boolean shuffleSwitch() {
        try {
            if (getNativeService()!=null) {
                int res = getNativeService().onEvent(0, MEDIA_CONTROL_RANDOM);
                if (res==0) {
                    mShuffleSwitch = mShuffleSwitch==1 ? 0 : 1;
                }
            }
        } catch (RemoteException ex) {
            if(updateNativeServiceLink() != null) {
                shuffleSwitch();
            }
        }
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
        return mDuration;
    }

    @Override
    public int getPosition() {
        return mPosition;
    }

    @Override
    public int getShuffle() {
        return mShuffleSwitch;
    }

    @Override
    public int getRepeat() {
        return mRepeatSwitch;
    }

    @Override
    public int getCapabilities() {
        return CAP_ALL;
    }

    @Override
    public void setCallback(IDemoPlayerCallback callback) {
        mPlayerCallback = callback;
    }


    /****************************************************************
     ******                IBaseServiceListener                ******
     ***************************************************************/
    class AppleIAPServiceListener extends IAppleIAPServiceListener.Stub {

        @Override
        public void onMediaStateChanged(int playbackState, int randomState, int repeatState) {
            Log.d(TAG, "onMediaStateChanged: playbackState="+playbackState);
            Log.d(TAG, "onMediaStateChanged: randomState="+randomState);
            Log.d(TAG, "onMediaStateChanged: repeatState="+repeatState);

            switch (playbackState) {
                case MEDIA_CONTROL_PLAY:
                    mMediaState = DemoPlayerState.ST_PLAYED;
                    break;
                case MEDIA_CONTROL_PAUSE:
                    mMediaState = DemoPlayerState.ST_PAUSED;
                    break;
                case MEDIA_CONTROL_STOP:
                    mMediaState = DemoPlayerState.ST_STOPPED;
                    break;
            }
            mShuffleSwitch = randomState;
            mRepeatSwitch = repeatState;

            onStateChanged();
        }

        @Override
        public void onTrackPositionChanged(int trackPosition, int trackLength) {
            Log.d(TAG, "onTrackPositionChanged: trackPosition="+trackPosition);
            Log.d(TAG, "onTrackPositionChanged: trackLength="+trackLength);
            mPosition = trackPosition;
            mDuration = trackLength;
            onStateChanged();
        }

        @Override
        public void onTrackChanged(String title, String album, String artist) {
            Log.d(TAG, "onTrackChanged: title="+title);
            Log.d(TAG, "onTrackChanged: album="+album);
            Log.d(TAG, "onTrackChanged: artist="+artist);
            mTrackName = title;
            mArtistName = artist;
            mAlbumName = album;
            onStateChanged();
        }
    }

    private void onStateChanged() {
        if (mPlayerCallback!=null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mPlayerCallback != null) {
                        mPlayerCallback.onStateChanged();
                    }
                }
            });
        }
    }

}
