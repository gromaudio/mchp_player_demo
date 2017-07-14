# mchp_player_demo

Directory /system has a compiled binaries and .apk files.
These files must be uploaded on the board to /system/app and /system/bin directories.

1. Connect board to PC by USB-OTG cable.
2. Check adbd is running on the board:
    $ adb device
    This command must return the list of attached devices.
3. Run ./install_demo.sh script (will reboot board).
4. Wait until Android is loaded.

Next steps must be done always after rebooting:

5. Run ./run_demo.sh script.
6. Now you can lunch SimplifiedMediaPlayer App.

