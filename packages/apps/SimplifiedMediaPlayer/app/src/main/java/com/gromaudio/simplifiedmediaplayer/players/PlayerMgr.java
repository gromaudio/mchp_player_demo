package com.gromaudio.simplifiedmediaplayer.players;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import java.util.List;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.os.ServiceManager;
import com.gromaudio.vlineservice.IBaseService;
import com.gromaudio.vlineservice.IBaseServiceListener;

/**
 * Created by Vitaly Kuznetsov <v.kuznetsov.work@gmail.com> on 28.06.17.
 */
public class PlayerMgr {

    private static final String TAG = "PlayerMgr";

    private static final String NATIVE_SERVICE_NAME = "demo.base_daemon";

    private AoapPlayer mAoapPlayer = new AoapPlayer();

    private AppleIAPPlayer mAppleIAPPlayer = new AppleIAPPlayer();

    private FilePlayer mFilePlayer = new FilePlayer();

    private USBStateReceiver mUSBStateReceiver = new USBStateReceiver();

    private StorageManager mStorageManager = null;

    private IBaseService mNativeService = null;

    private BaseServiceListener mBaseServiceListener = new BaseServiceListener();

    private Handler mHandler = new Handler();

    private String  mUSBPath = "";

    private boolean mAOAPConnected = false;
    
    private boolean mIAPConnected = false;

    private boolean mCarPlayConnected = false;

    private PlayerType mActivePlayerType = PlayerType.UNKNOWN_PLAYER;

    private IDemoPlayer mActivePlayer = null;

    private IPlayerMgrCallback mCallback = null;


    public enum PlayerType {
        UNKNOWN_PLAYER,
        AOAP_PLAYER,
        FILE_PLAYER,
        CARPLAY_PLAYER,
        ITUNES_PLAYER,
        AAUTO_PLAYER
    };

    public interface IPlayerMgrCallback {
        void onPlayerStateChanged(PlayerType player);
    };

    public boolean init(Context context, IPlayerMgrCallback callback) {
        mCallback = callback;
        closeCurrentPlayer();
        registerUSBReceiver(context);
        registerStorageManager(context);
        connectToNativeService();
        if (mNativeService==null) {
            return false;
        }
        updateCurrentNativeStates();
        return true;
    }

    public void close(Context context) {
        closeCurrentPlayer();
        unregisterUSBReceiver(context);
        unregisterStorageManager();
        disconnectFromNativeService();
    }

    public IDemoPlayer activatePlayer(PlayerType player) {
        if (player == mActivePlayerType) {
            return getActivePlayer();
        }
        if (!isPlayerEnabled(player)) {
            return null;
        }
        closeCurrentPlayer();
        mActivePlayerType = player;
        if (player == PlayerType.AOAP_PLAYER) {
            mAoapPlayer.init(null);
            mActivePlayer = mAoapPlayer;
            return mAoapPlayer;
        }
        else if (player == PlayerType.ITUNES_PLAYER) {
            mAppleIAPPlayer.init(null);
            mActivePlayer = mAppleIAPPlayer;
            return mAppleIAPPlayer;
        }
        else if (player == PlayerType.FILE_PLAYER) {
            mFilePlayer.init(mUSBPath);
            mActivePlayer = mFilePlayer;
            return mFilePlayer;
        }
        else if (player == PlayerType.CARPLAY_PLAYER) {
            mActivePlayerType = PlayerType.UNKNOWN_PLAYER;
            mActivePlayer = null;
        }
        return null;
    }

    private IDemoPlayer getActivePlayer() {
        return mActivePlayer;
    }

    public boolean isPlayerEnabled(PlayerType player) {
        if (player == PlayerType.AOAP_PLAYER) {
            return mAOAPConnected;
        }
        else if (player == PlayerType.ITUNES_PLAYER) {
            return mIAPConnected;
        }
        else if (player == PlayerType.CARPLAY_PLAYER) {
            return mCarPlayConnected;
        }
        else if (player == PlayerType.FILE_PLAYER) {
            return (mUSBPath!="");
        }
        return false;
    }


    private void closeCurrentPlayer() {
        if (mActivePlayer != null) {
            ((IDemoPlayer.IDemoPlayerCtl) mActivePlayer).close();
            mActivePlayer = null;
        }
        mActivePlayerType = PlayerType.UNKNOWN_PLAYER;
    }

    private void registerUSBReceiver(Context context) {
        IntentFilter filters = new IntentFilter();
        filters.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filters.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filters.addAction(Intent.ACTION_MEDIA_REMOVED);
        filters.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filters.addDataScheme("file");
        context.registerReceiver(mUSBStateReceiver, filters);
    }

    private void unregisterUSBReceiver(Context context) {
        context.unregisterReceiver(mUSBStateReceiver);
    }

    private class USBStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "USBStateReceiver::onReceive("+intent+")");
            if (intent == null || intent.getAction() == null || intent.getDataString() == null) {
                return;
            }
            final String action = intent.getAction();
            String mediaPath = intent.getDataString().replaceAll("file://", "");
            Parcelable extra = intent.getParcelableExtra("storage_volume");
            final String sUUID = extractUUID(extra);
            switch (action) {
                case Intent.ACTION_MEDIA_MOUNTED:
                    if (!isInternalStorage(mediaPath)) {
                        mUSBPath = mediaPath;
                        onStateChanged(PlayerType.FILE_PLAYER);
                    }
                    break;

                case Intent.ACTION_MEDIA_UNMOUNTED:
                case Intent.ACTION_MEDIA_REMOVED:
                case Intent.ACTION_MEDIA_BAD_REMOVAL:
                    if ( mediaPath.equals(mUSBPath) ) {
                        mUSBPath = "";
                        onStateChanged(PlayerType.FILE_PLAYER);
                    }
                    break;
            }
        }
        private String extractUUID(Parcelable extra) {
            String sUUID = "unknown_storage_uuid";
            if (extra!=null) {
                String sExtra = extra.toString();
                int nIndex = sExtra.indexOf("mUuid=");
                if (nIndex >= 0) {
                    String sTmp = sExtra.substring(nIndex+6);
                    int nSpaceIndex = sTmp.indexOf(" ");
                    sUUID = (nSpaceIndex < 0) ? sTmp : sTmp.substring(0, nSpaceIndex);
                }
                Log.d(TAG, "UUID=" + sUUID + ";");
            }
            return sUUID;
        }
        //Possibly not correctly determine the internal storage.
        private boolean isInternalStorage(String mediaPath) {
            final String STORAGE_EMULATED = "/storage/emulated";
            final String STORAGE_INT_SD_VLINE = "/mnt/internal_sd";
            final String STORAGE_EXT_SD_VLINE = "/mnt/external_sd";
            return !TextUtils.isEmpty(mediaPath) &&
                    ( mediaPath.contains(STORAGE_EMULATED) ||
                            mediaPath.contains(STORAGE_INT_SD_VLINE) ||
                            mediaPath.contains(STORAGE_EXT_SD_VLINE)
                    );
        }
    }

    private void registerStorageManager(Context context) {
        mStorageManager = context.getSystemService(StorageManager.class);
        mStorageManager.registerListener(mStorageListener);
        refreshStorages();
    }

    private void unregisterStorageManager() {
        mStorageManager.unregisterListener(mStorageListener);
    }

    private final StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            Log.d(TAG, "onVolumeStateChanged: " + vol);
            if (vol.state == VolumeInfo.STATE_MOUNTED || 
                vol.state == VolumeInfo.STATE_MOUNTED_READ_ONLY ||
                vol.state == VolumeInfo.STATE_UNMOUNTED) 
            {
                if (isInteresting(vol)) {
                    refreshStorage(vol);
                }
            }
	}
    };

    private void refreshStorages() {
        Log.d(TAG, "refreshStorages();");
        final List<VolumeInfo> volumes = mStorageManager.getVolumes();
        for (VolumeInfo vol : volumes) {
            Log.d(TAG, "refreshStorages: check vol=" + vol.path);
            refreshStorage(vol);
        }
    }

    private void refreshStorage(VolumeInfo vol) {
        if (vol != null) {
            Log.d(TAG, "refreshStorage(v): " + vol.path);
            if (isInteresting(vol)) {
                Log.d(TAG, "refreshStorage: intresting vol=" + vol);
                switch (vol.getState()) {
                    case VolumeInfo.STATE_MOUNTED:
                    case VolumeInfo.STATE_MOUNTED_READ_ONLY:
                            mUSBPath = vol.path;
                            onStateChanged(PlayerType.FILE_PLAYER);
                            break;
                    case VolumeInfo.STATE_UNMOUNTED:
                            if (vol.path.equals(mUSBPath)) {
                                mUSBPath = "";
                                onStateChanged(PlayerType.FILE_PLAYER);
                            }
                            break;
                }
            }
        }
    }

    private boolean isInteresting(VolumeInfo vol) {
        String IMX_USB_MEDIA = "/mnt/media_rw/";
        if (TextUtils.isEmpty(vol.path) || !vol.path.contains(IMX_USB_MEDIA)) {
            return false;
        }

        switch(vol.getType()) {
            case VolumeInfo.TYPE_PUBLIC:
                return true;
            default:
                return false;
        }
    }

    private void connectToNativeService() {
        updateNativeServiceLink();
        if (mNativeService != null) {
            try {
                mNativeService.addListener(mBaseServiceListener);
            }catch (RemoteException ex) {
                Log.e(TAG, " addListener() ex: " + ex);
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

        @Override
        public void onAOAPStatus(int key, int value) {
            if (key == STKEY_ACTIVE) {
                mAOAPConnected = (value==1);
                onStateChanged(PlayerType.AOAP_PLAYER);
            }
        }

        @Override
        public void onCarPlayStatus(int key, int value) {
            if (key == STKEY_ACTIVE) {
                mCarPlayConnected = (value==1);
                onStateChanged(PlayerType.CARPLAY_PLAYER);
            }
        }

        @Override
        public void onIAPStatus(int key, int value) {
            if (key == STKEY_ACTIVE) {
                mIAPConnected = (value==1);
                onStateChanged(PlayerType.ITUNES_PLAYER);
            }
        }

        @Override
        public void onExternalSoundState(int state) {
            //-1 - Unknown state, 0 - Silent, 1 - Sound
            Log.e(TAG, " onExternalSoundState: " + state);
            //Ducking local (USB) player if it is playing.
            if (mActivePlayerType == PlayerType.FILE_PLAYER) {
                mFilePlayer.setDucking(state==1);
            }
        }
    }

    private void updateCurrentNativeStates() {
        try {
            int statusIAP = mNativeService.getIAPStatus();
            int statusAOAP = mNativeService.getAOAPStatus();
            int statusCarPlay = mNativeService.getCarPlayStatus();

            mAOAPConnected = (statusAOAP==1);
            onStateChanged(PlayerType.AOAP_PLAYER);

            mIAPConnected = (statusIAP==1);
            onStateChanged(PlayerType.ITUNES_PLAYER);

            mCarPlayConnected = (statusCarPlay==1);
            onStateChanged(PlayerType.CARPLAY_PLAYER);

        }catch (RemoteException ex) {
            Log.e(TAG, " getIAPStatus(), getAOAPStatus(), getCarPlayStatus() ex: " + ex);
        }
    }

    private void onStateChanged(final PlayerType type) {
        if (mCallback!=null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onPlayerStateChanged(type);
                    }
                }
            });
        }
    }

}
