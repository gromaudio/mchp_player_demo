package com.gromaudio.simplifiedmediaplayer.ui.customElements;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.gromaudio.simplifiedmediaplayer.R;
import com.gromaudio.utils.Utils;

import java.util.Date;

public class StatusBar extends FrameLayout {
    //private final String mTag = "StatusBar";
    public final static String IS_BLUETOOTH_CONNECTED = "is_bluetooth_connected";

    public enum BLUETOOTH_STATE {STATE_CONNECTED, STATE_CONNECTING, STATE_ON, STATE_OFF}
    public enum WIFI_STATE {CONNECTED, CONNECTING, DISCONNECT}


    protected ImageView mBluetoothView;
    protected ImageView mWifiView;
    protected FrameLayout mBackButton;
    protected TextView mStatusBarTitle;
    protected TextView mClockTextView;

    public StatusBar(Context context) {
        super(context);
        initView();
    }

    public StatusBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public StatusBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.activity_main_status_bar_view, this);

        mBluetoothView = (ImageView) findViewById(R.id.bluetooth);
        mWifiView = (ImageView) findViewById(R.id.wifi);
        mBackButton = (FrameLayout) findViewById(R.id.backButton);
        mStatusBarTitle = (TextView) findViewById(R.id.statusBarTitle);
        mClockTextView = (TextView) findViewById(R.id.rightClockView);
    }


    public void updateTime() {
        if (mClockTextView.getVisibility() != VISIBLE) {
            mClockTextView.setVisibility(VISIBLE);
        }
        if (DateFormat.is24HourFormat(getContext())) {
            final String timeString = Utils.getDate(new Date().getTime());
            mClockTextView.setText(timeString);
        } else {
            final String timeString = Utils.getDateShort(new Date().getTime());
            mClockTextView.setText(timeString);
        }
    }

    public void setWifiState(WIFI_STATE wifiState, int signalLevel) {
        switch (wifiState) {
            case CONNECTED:
                mWifiView.setVisibility(View.VISIBLE);
                switch (signalLevel) {
                    case 0:
                        mWifiView.setBackgroundResource(R.drawable.ic_wifi);
                        break;
                    case 1:
                        mWifiView.setBackgroundResource(R.drawable.ic_wifi_1);
                        break;
                    case 2:
                        mWifiView.setBackgroundResource(R.drawable.ic_wifi_2);
                        break;
                    case 3:
                        mWifiView.setBackgroundResource(R.drawable.ic_wifi_3);
                        break;
                }
                break;
            case CONNECTING:
                mWifiView.setVisibility(View.VISIBLE);
                mWifiView.setBackgroundResource(R.drawable.ic_wifi_connecting);
                AnimationDrawable animation = (AnimationDrawable)mWifiView.getBackground();
                animation.start();
                break;
            case DISCONNECT:
                mWifiView.setVisibility(View.GONE);
                break;
        }
    }

    public void setBluetoothState(BLUETOOTH_STATE bluetoothState) {
        switch (bluetoothState) {
            case STATE_ON:
                mBluetoothView.setVisibility(View.VISIBLE);
                mBluetoothView.setImageResource(R.drawable.ic_bluetooth);
                break;
            case STATE_OFF:
                mBluetoothView.setVisibility(View.GONE);
                break;
            case STATE_CONNECTED:
                mBluetoothView.setVisibility(View.VISIBLE);
                mBluetoothView.setImageResource(R.drawable.ic_bluetooth_connected);
                break;
            case STATE_CONNECTING:
                mBluetoothView.setVisibility(View.VISIBLE);
                mBluetoothView.setImageResource(R.drawable.ic_bluetooth_searching_1);
                break;
        }
    }

    public void updateBluetoothState() {
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            mBluetoothView.setVisibility(View.GONE);
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothView.setVisibility(View.VISIBLE);
                mBluetoothView.setImageResource(R.drawable.ic_bluetooth);
                boolean isConnected = PreferenceManager
                    .getDefaultSharedPreferences(getContext())
                    .getBoolean(IS_BLUETOOTH_CONNECTED, false);
                if (isConnected) {
                    mBluetoothView.setImageResource(R.drawable.ic_bluetooth_connected);
                }
            } else if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothView.setVisibility(View.VISIBLE);
                mBluetoothView.setImageResource(R.drawable.ic_bluetooth_searching_1);
            } else
                mBluetoothView.setVisibility(View.GONE);
        }
    }

    public void setTitle(@Nullable CharSequence title) {
        mStatusBarTitle.setText(title);
        setVisibilityView(mStatusBarTitle, title != null);
    }

    public void setOnBackPressetListener(OnClickListener listener) {
        setVisibilityView(mBackButton, listener != null);
        mBackButton.setOnClickListener(listener);
    }

    private static void setVisibilityView(View view, boolean isVisible) {
        if (isVisible) {
            if (view.getVisibility() != VISIBLE) {
                view.setVisibility(VISIBLE);
            }
        } else {
            if (view.getVisibility() != GONE) {
                view.setVisibility(GONE);
            }
        }
    }
}
