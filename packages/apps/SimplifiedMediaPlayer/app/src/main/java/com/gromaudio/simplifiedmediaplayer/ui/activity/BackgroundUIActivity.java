package com.gromaudio.simplifiedmediaplayer.ui.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import android.os.ServiceManager;
import android.widget.TextView;

import com.gromaudio.vlineservice.IBaseService;
import com.gromaudio.vlineservice.IBaseServiceListener;

import com.gromaudio.simplifiedmediaplayer.R;

/**
 * Created by Vitaly Kuznetsov <v.kuznetsov.work@gmail.com> on 13.12.17.
 *
 * It plays the role of background activity for CarPlay or AndroidAuto UI.
 */
public class BackgroundUIActivity extends Activity {

    private static final String TAG = "BackgroundUIActivity";


    public static final String EXTRA_PLAYER_TYPE = "extra_player_type";

    public static final int PLAYER_TYPE_CARPLAY = 0;

    public static final int PLAYER_TYPE_AAUTO = 1;


    private static final String NATIVE_SERVICE_NAME = "demo.base_daemon";

    private static final int BOTTOM_PANEL_HEIGHT = 60;

    private IBaseService mNativeService = null;

    private BaseServiceListener mBaseServiceListener = new BaseServiceListener();

    private Handler mHandler = new Handler();

    private TextView mContentView;

    private View mControlsView;

    private int mPlayerType;


    private final View.OnTouchListener mButtonTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            finish();
            return false;
        }
    };

    private final View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.d(TAG, "onTouch: X="+motionEvent.getX()+"; Y="+motionEvent.getY());
            if (mNativeService!=null) {
                if (mPlayerType == PLAYER_TYPE_CARPLAY) {
                    try {
                        mNativeService.sendCarPlayTouchEvent((int) motionEvent.getX(), (int) motionEvent.getY());
                    } catch (RemoteException ex) {
                        Log.d(TAG, "sendCarPlayTouchEvent() ex: " + ex);
                    }
                }
                else {
                    try {
                        mNativeService.sendAAutoTouchEvent((int) motionEvent.getX(), (int) motionEvent.getY());
                    } catch (RemoteException ex) {
                        Log.d(TAG, "sendAAutoTouchEvent() ex: " + ex);
                    }
                }
            }
            return false;
        }
    };

    private final View.OnGenericMotionListener mOnGenericMotionListener = new View.OnGenericMotionListener() {
        @Override
        public boolean onGenericMotion(View view, MotionEvent motionEvent) {
            Log.d(TAG, "onGenericMotion: X="+motionEvent.getX()+"; Y="+motionEvent.getY() + "; pressure=" + motionEvent.getPressure());
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_car_play);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent() != null) {
            mPlayerType = getIntent().getIntExtra(EXTRA_PLAYER_TYPE, mPlayerType);
            Log.d(TAG, "onCreate: from intent mPlayerType="+mPlayerType);
        }

        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = (TextView)findViewById(R.id.fullscreen_content);
        mContentView.setText( mPlayerType==PLAYER_TYPE_CARPLAY ? R.string.carplay_connection : R.string.aauto_connection);
        mContentView.setOnTouchListener(mOnTouchListener);
        mContentView.setOnGenericMotionListener(mOnGenericMotionListener);
        findViewById(R.id.dummy_button).setOnTouchListener(mButtonTouchListener);
        connectToNativeService();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent()");
        if (intent != null) {
            mPlayerType = intent.getIntExtra(EXTRA_PLAYER_TYPE, mPlayerType);
            Log.d(TAG, "onNewIntent: mPlayerType="+mPlayerType);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNativeService != null) {
            if (mPlayerType == PLAYER_TYPE_CARPLAY) {
                try {
                    mNativeService.activateCarPlay(BOTTOM_PANEL_HEIGHT);
                } catch (RemoteException ex) {
                    Log.d(TAG, "activateCarPlay() ex: " + ex);
                }
            }
            else {
                try {
                    mNativeService.activateAAuto(BOTTOM_PANEL_HEIGHT);
                } catch (RemoteException ex) {
                    Log.d(TAG, "activateAAuto() ex: " + ex);
                }
            }
        }
        hide();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNativeService != null) {
            if (mPlayerType == PLAYER_TYPE_CARPLAY) {
                try {
                    mNativeService.activateCarPlay(0);
                } catch (RemoteException ex) {
                    Log.d(TAG, "activateCarPlay() ex: " + ex);
                }
            }
            else {
                try {
                    mNativeService.activateAAuto(0);
                } catch (RemoteException ex) {
                    Log.d(TAG, "activateAAuto() ex: " + ex);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectFromNativeService();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        hide();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void connectToNativeService() {
        updateNativeServiceLink();
        if (mNativeService != null) {
            try {
                mNativeService.addListener(mBaseServiceListener);
            }catch (RemoteException ex) {
                Log.e(TAG, "addListener() ex: " + ex);
            }
        }
    }

    private void disconnectFromNativeService() {
        if (mNativeService != null) {
            try {
                mNativeService.removeListener(mBaseServiceListener);
            }catch (RemoteException ex) {
                Log.e(TAG, " removeListener() ex: " + ex);
            }
            mNativeService = null;
        }
    }

    private IBaseService updateNativeServiceLink() {
        mNativeService = null;
        Log.d(TAG, "Connection to service " + NATIVE_SERVICE_NAME);
        IBinder binder = ServiceManager.getService(NATIVE_SERVICE_NAME);
        if (binder == null) {
            Log.e(TAG, "Unable to get service " + NATIVE_SERVICE_NAME);
            return null;
        }
        mNativeService = IBaseService.Stub.asInterface(binder);
        Log.d(TAG, "mNativeService=" + mNativeService);
        return mNativeService;
    }

    /****************************************************************
     ******                IBaseServiceListener                ******
     ***************************************************************/
    class BaseServiceListener extends IBaseServiceListener.Stub {

        private static final int STKEY_ACTIVE = 1;
        private static final int STKEY_DISPLAY = 2;
        private static final int STKEY_MAIN_AUDIO = 3;
        private static final int STKEY_UI_BUTTON = 4;
        private static final int STKEY_SPEECH_AUDIO = 5;
        private static final int STKEY_SYSTEM_AUDIO = 6;

        @Override
        public void onCarPlayStatus(int key, int value) {
            if (mPlayerType == PLAYER_TYPE_CARPLAY) {
                if (((key == STKEY_ACTIVE) && (value == 0)) || ((key == STKEY_UI_BUTTON) && (value == 1))) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
            }
        }

        @Override
        public void onAAutoStatus(int key, int value) {
            if (mPlayerType == PLAYER_TYPE_AAUTO) {
                if (((key == STKEY_ACTIVE) && (value == 0)) || ((key == STKEY_UI_BUTTON) && (value == 1))) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
            }
        }

        @Override
        public void onAOAPStatus(int key, int value) {}

        @Override
        public void onIAPStatus(int key, int value) {}

        @Override
        public void onExternalSoundState(int state) {}
    }
}
