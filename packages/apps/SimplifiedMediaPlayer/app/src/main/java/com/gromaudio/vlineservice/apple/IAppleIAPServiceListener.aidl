// IAppleIAPServiceListener.aidl
package com.gromaudio.vlineservice.apple;

// Declare any non-default types here with import statements

interface IAppleIAPServiceListener {
    void onMediaStateChanged(int playbackState, int randomState, int repeatState);
    void onTrackPositionChanged(int trackPosition, int trackLength);
    void onTrackChanged(String title, String album, String artist);
}
