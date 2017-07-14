#!/bin/bash

adb root
adb shell setenforce 0 
adb shell base_daemon &

