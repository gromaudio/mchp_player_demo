#!/bin/bash

adb root
adb remount

#Copy files
adb shell mkdir /system/app/SimplifiedMediaPlayer
adb push ./system/app/SimplifiedMediaPlayer/SimplifiedMediaPlayer.apk /system/app/SimplifiedMediaPlayer/
adb push ./system/bin/base_daemon /system/bin/
adb push ./system/bin/aoap_streaming /system/bin/

adb reboot
