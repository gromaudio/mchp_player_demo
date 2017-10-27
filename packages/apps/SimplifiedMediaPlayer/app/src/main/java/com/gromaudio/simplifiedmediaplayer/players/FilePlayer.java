package com.gromaudio.simplifiedmediaplayer.players;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Vitaly Kuznetsov <v.kuznetsov.work@gmail.com> on 28.06.17.
 */
public class FilePlayer implements IDemoPlayer, IDemoPlayer.IDemoPlayerCtl {

    private static final String TAG = "UsbPlayer";

    private static final int MAX_TRACKS_LIMIT = 100;

    private String mArtistName = "noname";

    private String mAlbumName = "noname";

    private String mTrackName = "noname";

    private String mMediaDir = "";

    private ArrayList<String> mMediaFiles = new ArrayList<String>();

    private int mCurrentIndex = -1;

    private MediaPlayer mMediaPlayer = null;

    private IDemoPlayerCallback mPlayerCallback = null;

    private DemoPlayerState mMediaState = DemoPlayerState.ST_STOPPED;

    private int mRepeatSwitch = 0;

    private int mShuffleSwitch = 0;

    private Random mRandom = new Random();


    /**********************************************************************
     ******                IDemoPlayer.IDemoPlayerCtl                ******
     *********************************************************************/
    @Override
    public void init(String dir) {
        mMediaDir = dir;
        mMediaState = DemoPlayerState.ST_STOPPED;
        mMediaFiles.clear();
        mCurrentIndex = -1;
        //mMediaPlayer = new MediaPlayer();
        if (mMediaDir != null) {
            new ScanFilesTask().execute(new File(mMediaDir));
        }
    }

    @Override
    public void close() {
        mMediaState = DemoPlayerState.ST_STOPPED;
        mMediaDir = "";
        mMediaFiles.clear();
        mCurrentIndex = -1;
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mMediaPlayer = null;
    }



    private void setState(DemoPlayerState state) {
        if (mMediaState != state) {
            mMediaState = state;
            if (mPlayerCallback!=null) {
                mPlayerCallback.onStateChanged();
            }
        }
    }

    private boolean setCurrentTrack(int index) {
        String sFile = "";
        synchronized (mMediaFiles) {
            sFile = mMediaFiles.get(index);
        }
        Log.d(TAG, String.format("setCurrentTrack( %s );", sFile) );
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(sFile);
            mMediaPlayer.prepare();
            if (mMediaState == DemoPlayerState.ST_PLAYED) {
                mMediaPlayer.start();
            }
            mCurrentIndex = index;
            mTrackName = new File(sFile).getName();

            if (mPlayerCallback != null) {
                mPlayerCallback.onStateChanged();
            }

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d(TAG, "onCompletion()");
                    if (mRepeatSwitch==0) {
                        if (mShuffleSwitch==1) {
                            int index = mRandom.nextInt(mMediaFiles.size());
                            while (index == mCurrentIndex) {
                                index = mRandom.nextInt(mMediaFiles.size());
                            }
                            setCurrentTrack( index );
                        }
                        else {
                            next();
                        }
                    }
                    else {
                        setCurrentTrack( mCurrentIndex );
                    }
                }
            });

        } catch (IOException ex) {
            Log.d(TAG, "mMediaPlayer ex: ", ex);
            return false;
        }
        return true;
    }

    private void onMediaFileFound(int count) {
        //If first media file found, set it to AudioTrack()
        if (count == 1) {
            setCurrentTrack(0);
        }
    }

    private class ScanFilesTask extends AsyncTask<File, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(File... dir) {
            Log.d(TAG, "doInBackground(scanDir);");
            return scanDir(dir[0]);
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            onMediaFileFound(progress[0]);
        }
        @Override
        protected void onPostExecute(Boolean result) {
        }

        /**
         * Searches all media (.mp3) files in the directory.
         * Need to be run in the separate task.
         */
        private boolean scanDir(File dir) {
            Log.d(TAG, "scanDir: " + dir.getAbsolutePath());
            if (!dir.exists()) {
                Log.e(TAG, "Error: Dir doesn't exists.");
                return false;
            }
            if (mMediaFiles.size() >= MAX_TRACKS_LIMIT) {
                Log.d(TAG, "MAX_TRACKS_LIMIT is reached.");
                return true;
            }
            File[] files = dir.listFiles();
            Log.d(TAG, "Found " + files.length + "files.");
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDir(file);
                }
                else {
                    if (isMediaFile(file)) {
                        //add to list
                        if (mMediaFiles.size() >= MAX_TRACKS_LIMIT) {
                            Log.d(TAG, "MAX_TRACKS_LIMIT is reached.");
                            return true;
                        }
                        synchronized (mMediaFiles) {
                            mMediaFiles.add(file.getAbsolutePath());
                            Log.d(TAG, "Media file found: " + file.getAbsolutePath());
                        }
                        publishProgress(mMediaFiles.size());
                    }
                    else {
                        Log.d(TAG, "Skip not media file...");
                    }
                }
            }
            return true;
        }

        private boolean isMediaFile(File file) {
            if (file.exists() && file.isFile()) {
                //check an extension of the file
                String sFile = file.getAbsolutePath();
                if (sFile.endsWith(".mp3") || sFile.endsWith(".MP3") ||
                        sFile.endsWith(".wav") || sFile.endsWith(".WAV") ) {
                    return true;
                }
            }
            return false;
        }
    }


    /*******************************************************
     ******                IDemoPlayer                ******
     ******************************************************/
    /*
     * Controls
     */
    @Override
    public boolean play() {
        if (mMediaPlayer!=null) {
            mMediaPlayer.start();
            setState(DemoPlayerState.ST_PLAYED);
            return true;
        }
        return false;
    }

    @Override
    public boolean pause() {
        if (mMediaPlayer!=null) {
            mMediaPlayer.pause();
            setState(DemoPlayerState.ST_PAUSED);
            return true;
        }
        return false;
    }

    @Override
    public boolean next() {
        int size = 0;
        synchronized (mMediaFiles) {
            size = mMediaFiles.size();
        }
        Log.d(TAG, "mMediaFiles.size="+size + "; mCurrentIndex="+mCurrentIndex);
        if (mCurrentIndex+1 < size) {
            return setCurrentTrack( mCurrentIndex+1 );
        }
        return false;
    }

    @Override
    public boolean prev() {
        Log.d(TAG, "mCurrentIndex="+mCurrentIndex);
        if (mCurrentIndex > 0) {
            return setCurrentTrack( mCurrentIndex-1 );
        }
        return false;
    }

    @Override
    public boolean seekTo(int position) {
        if (mMediaPlayer!=null) {
            mMediaPlayer.seekTo(position);
            if (mPlayerCallback!=null) {
                mPlayerCallback.onStateChanged();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean repeatSwitch() {
        Log.d(TAG, "repeatSwitch()");
        mRepeatSwitch = mRepeatSwitch==1 ? 0 : 1;
        return true;
    }

    @Override
    public boolean shuffleSwitch() {
        Log.d(TAG, "shuffleSwitch()");
        mShuffleSwitch = mShuffleSwitch==1 ? 0 : 1;
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
        if (mMediaPlayer != null) {
            int dur = mMediaPlayer.getDuration();
            return (dur!=-1) ? dur : 0;
        }
        return 0;
    }

    @Override
    public int getPosition() {
        if (mMediaPlayer != null) {
            int pos = mMediaPlayer.getCurrentPosition();
            return (pos!=-1) ? pos : 0;
        }
        return 0;
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

}
