package com.gromaudio.simplifiedmediaplayer.models;


import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.gromaudio.simplifiedmediaplayer.players.PlayerMgr;

public class AppDetail {

    public enum STATE {NORMAL, DISABLED}

    @NonNull
    private String mAppName;
    private int mResIcon;
    @NonNull
    private STATE mState;
    private boolean mIsSelected;

    private PlayerMgr.PlayerType mPlayerType;

    public AppDetail(@NonNull String appName,
                     @DrawableRes int resIcon) {
        mAppName = appName;
        mResIcon = resIcon;
        mState = STATE.DISABLED;
    }

    public String getAppName() {
        return mAppName;
    }

    public int getResIcon() {
        return mResIcon;
    }

    public STATE getState() {
        return mState;
    }

    public void setState(STATE state) {
        mState = state;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    public PlayerMgr.PlayerType getPlayerType() {
        return mPlayerType;
    }

    public void setPlayerType(PlayerMgr.PlayerType playerType) {
        mPlayerType  = playerType;
    }
}
