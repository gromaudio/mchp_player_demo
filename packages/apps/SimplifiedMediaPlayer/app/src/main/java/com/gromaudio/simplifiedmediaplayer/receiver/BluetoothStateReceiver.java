package com.gromaudio.simplifiedmediaplayer.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.gromaudio.simplifiedmediaplayer.ui.customElements.StatusBar;

public class BluetoothStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        switch (action) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                    case BluetoothAdapter.STATE_OFF:
                        editor.putBoolean(StatusBar.IS_BLUETOOTH_CONNECTED, false).apply();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        editor.putBoolean(StatusBar.IS_BLUETOOTH_CONNECTED, false).apply();
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        editor.putBoolean(StatusBar.IS_BLUETOOTH_CONNECTED, true).apply();
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        editor.putBoolean(StatusBar.IS_BLUETOOTH_CONNECTED, false).apply();
                        break;
                }
                break;
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                editor.putBoolean(StatusBar.IS_BLUETOOTH_CONNECTED, true).apply();
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                editor.putBoolean(StatusBar.IS_BLUETOOTH_CONNECTED, false).apply();
                break;
        }
    }
}
