package com.gromaudio.simplifiedmediaplayer.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.gromaudio.simplifiedmediaplayer.R;
import com.gromaudio.simplifiedmediaplayer.ui.customElements.StatusBar;
import com.gromaudio.simplifiedmediaplayer.ui.fragments.BaseFragment;
import com.gromaudio.simplifiedmediaplayer.ui.fragments.LauncherFragment;
import com.gromaudio.utils.Logger;
import com.gromaudio.utils.Utils;

import static com.gromaudio.simplifiedmediaplayer.ui.customElements.StatusBar.BLUETOOTH_STATE.STATE_CONNECTED;
import static com.gromaudio.simplifiedmediaplayer.ui.customElements.StatusBar.BLUETOOTH_STATE.STATE_CONNECTING;
import static com.gromaudio.simplifiedmediaplayer.ui.customElements.StatusBar.BLUETOOTH_STATE.STATE_OFF;
import static com.gromaudio.simplifiedmediaplayer.ui.customElements.StatusBar.BLUETOOTH_STATE.STATE_ON;
import static com.gromaudio.simplifiedmediaplayer.ui.customElements.StatusBar.WIFI_STATE.CONNECTED;
import static com.gromaudio.simplifiedmediaplayer.ui.customElements.StatusBar.WIFI_STATE.CONNECTING;
import static com.gromaudio.simplifiedmediaplayer.ui.customElements.StatusBar.WIFI_STATE.DISCONNECT;

public class MainActivity extends ActionBarActivity {

    private StatusBar mStatusBar;

    private Handler mHandler = new Handler();
    private boolean mIsRegisterReceiverWifiAndBluetoothReceiver;


    private BroadcastReceiver mTickReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                mStatusBar.updateTime();
            }
        }
    };

    private final BroadcastReceiver mWifiAndBluetoothReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action){
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                        case BluetoothAdapter.STATE_OFF:
                            mStatusBar.setBluetoothState(STATE_OFF);
                            break;
                        case BluetoothAdapter.STATE_ON:
                            mStatusBar.setBluetoothState(STATE_ON);
                            break;
                        case BluetoothAdapter.STATE_CONNECTED:
                            mStatusBar.setBluetoothState(STATE_CONNECTED);
                            break;
                        case BluetoothAdapter.STATE_CONNECTING:
                            mStatusBar.setBluetoothState(STATE_CONNECTING);
                            break;
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    mStatusBar.setBluetoothState(STATE_CONNECTED);
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    mStatusBar.setBluetoothState(STATE_ON);
                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    updateWifiStatus();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_main);

        mStatusBar = (StatusBar) findViewById(R.id.status_bar);
        mStatusBar.updateBluetoothState();
        updateWifi();
        registerReceiverWifiAndBluetoothReceiver();

        BaseFragment fragment = null;
        if (savedState != null) {
            fragment = getTheLatestFragmentInBackstack();
        }
        if (fragment == null) {
            showFragment(LauncherFragment.newInstance());
        }

        final FragmentManager fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(new OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int backCount = fm.getBackStackEntryCount();
                if (backCount == 1){
                    mStatusBar.setOnBackPressetListener(null);
                    final BaseFragment baseFragment = getTheLatestFragmentInBackstack();
                    if (baseFragment != null) {
                        baseFragment.onResumeFragment();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        unregisterReceiverWifiAndBluetoothReceiver();
        stopUpdateWifiStatus();

        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();


        try {
            registerReceiver(mTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        } catch (Throwable e) {
            Logger.e(e.getMessage());
        }
        mStatusBar.updateTime();
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            unregisterReceiver(mTickReceiver);
        } catch (Throwable e) {
            Logger.e(e.getMessage());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            Utils.configureActivityFullScreenMode(this, true);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (mStatusBar != null) {
            mStatusBar.setTitle(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    @Override
    public void onBackPressed() {
        final FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStack();
        } else {
            finish();
        }
    }

    private void unregisterReceiverWifiAndBluetoothReceiver() {
        if (mIsRegisterReceiverWifiAndBluetoothReceiver) {
            unregisterReceiver(mWifiAndBluetoothReceiver);
            mIsRegisterReceiverWifiAndBluetoothReceiver = false;
        }
    }

    private void registerReceiverWifiAndBluetoothReceiver() {
        if (!mIsRegisterReceiverWifiAndBluetoothReceiver) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            registerReceiver(mWifiAndBluetoothReceiver, filter);
            mIsRegisterReceiverWifiAndBluetoothReceiver = true;
        }
    }

    private void updateWifiStatus() {
        final Context context = getApplicationContext();
        final ConnectivityManager conMngr = (ConnectivityManager)
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo info =
            conMngr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isConnected = info != null && info.isConnected();
        boolean isConnecting = info != null && info.isConnectedOrConnecting();

        if (isConnected){
            final WifiManager wifiManager =
                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final int signalLevel = WifiManager.calculateSignalLevel(
                wifiManager.getConnectionInfo().getRssi(), 4
            );
            mStatusBar.setWifiState(CONNECTED, signalLevel);
        } else if (isConnecting) {
            mStatusBar.setWifiState(CONNECTING, 0);
        } else {
            mStatusBar.setWifiState(DISCONNECT, 0);
        }
    }

    private void updateWifi() {
        updateWifiStatus();
        stopUpdateWifiStatus();
        final int UPDATE_INTERVAL = 30000;
        mHandler.postDelayed(mUpdateWifiStatusRunnable, UPDATE_INTERVAL);
    }

    private void stopUpdateWifiStatus() {
        mHandler.removeCallbacks(mUpdateWifiStatusRunnable);
    }

    private Runnable mUpdateWifiStatusRunnable = new Runnable() {
        private static final int UPDATE_INTERVAL = 30000;

        @Override
        public void run() {
            try {
                updateWifiStatus();
            } finally {
                mHandler.postDelayed(mUpdateWifiStatusRunnable, UPDATE_INTERVAL);
            }
        }
    };

    public void showFragment(@NonNull BaseFragment fragment) {
        final FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            mStatusBar.setOnBackPressetListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
        final FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        ft.add(R.id.fragmentContainer, fragment, fragment.getClass().getSimpleName());
        ft.addToBackStack(fragment.getClass().getSimpleName());
        ft.commitAllowingStateLoss();
    }

    @Nullable
    private BaseFragment getTheLatestFragmentInBackstack() {
        final FragmentManager fm = getSupportFragmentManager();
        final int index = fm.getBackStackEntryCount() - 1;
        final BackStackEntry backEntry = fm.getBackStackEntryAt(index);
        final String tag = backEntry.getName();
        return  (BaseFragment) fm.findFragmentByTag(tag);
    }
}