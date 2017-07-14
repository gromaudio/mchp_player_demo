package com.gromaudio.simplifiedmediaplayer;

import android.app.Application;

import com.gromaudio.utils.FontManager;


public class App extends Application {

    private static App sInstance;

    public static App get() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        FontManager.init(getAssets());
    }
}
