package com.gromaudio.simplifiedmediaplayer.ui.customElements;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;

public class TrackInfo extends FontTextView {

    private String mArtist;
    private String mAlbum;
    private String mTrack;
    private STATE mCurrentState = STATE.STATE_TRACK;

    @NonNull
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int mTrackDisplayTime = 5;
    private int mArtistDisplayTime = 3;
    private int mAlbumDisplayTime = 3;

    private int mTimerTickCount = 0;
    private boolean mIsAnimated = false;

    private enum STATE {
        STATE_TRACK,
        STATE_ARTIST,
        STATE_ALBUM
    }

    public TrackInfo(Context context) {
        super(context);
        initView();
    }

    public TrackInfo(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TrackInfo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
    }

    public void onDestroy() {
        stopRepeatingTask();
        mHandler.removeCallbacksAndMessages(null);
    }

    public void setInfo(String artist, String album, String track) {
        mArtist = artist;
        mAlbum = album;
        mTrack = track;
        initDefaultState();
    }

    public void animation(boolean isAnimated) {
        if (isAnimated != mIsAnimated) {
            stopRepeatingTask();
            initDefaultState();
            if (isAnimated) {
                startRepeatingTask();
            }
        }
    }

    private void initDefaultState() {
        if (!TextUtils.isEmpty(mTrack)) {
            mCurrentState = STATE.STATE_TRACK;
            setText(mTrack);
        } else if (!TextUtils.isEmpty(mAlbum)) {
            mCurrentState = STATE.STATE_ALBUM;
            setText(mAlbum);
        } else if (!TextUtils.isEmpty(mArtist)) {
            mCurrentState = STATE.STATE_ARTIST;
            setText(mArtist);
        }
    }

    private void startRepeatingTask() {
        mIsAnimated = true;
        mTimerTickCount = 0;
        mStatusChecker.run();
    }

    private void stopRepeatingTask() {
        mIsAnimated = false;
        mHandler.removeCallbacks(mStatusChecker);
    }

    private Runnable mStatusChecker = new Runnable() {

        private static final int UPDATE_INTERVAL = 1000;

        @Override
        public void run() {
            try {
                updateStatus();
            } finally {
                mHandler.postDelayed(mStatusChecker, UPDATE_INTERVAL);
            }
        }
    };

    private void updateStatus() {
        mTimerTickCount++;
        switch (mCurrentState) {
            case STATE_TRACK:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mTimerTickCount > mTrackDisplayTime) {
                            mTimerTickCount = 0;
                            if (!TextUtils.isEmpty(mAlbum)) {
                                mCurrentState = STATE.STATE_ALBUM;
                                setText(mAlbum);
                            } else if (!TextUtils.isEmpty(mArtist)) {
                                mCurrentState = STATE.STATE_ARTIST;
                                setText(mArtist);
                            }
                        }
                    }
                });
                break;
            case STATE_ALBUM:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mTimerTickCount > mAlbumDisplayTime) {
                            mTimerTickCount = 0;
                            if (!TextUtils.isEmpty(mArtist)) {
                                mCurrentState = STATE.STATE_ARTIST;
                                setText(mArtist);
                            } else if (!TextUtils.isEmpty(mTrack)) {
                                mCurrentState = STATE.STATE_TRACK;
                                setText(mTrack);
                            }
                        }
                    }
                });
                break;
            case STATE_ARTIST:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mTimerTickCount > mArtistDisplayTime) {
                            mTimerTickCount = 0;
                            if (!TextUtils.isEmpty(mTrack)) {
                                mCurrentState = STATE.STATE_TRACK;
                                setText(mTrack);
                            } else if (!TextUtils.isEmpty(mAlbum)) {
                                mCurrentState = STATE.STATE_ALBUM;
                                setText(mAlbum);
                            }
                        }
                    }
                });
                break;
        }
    }
}
