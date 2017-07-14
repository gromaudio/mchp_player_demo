package com.gromaudio.utils;


import android.content.Context;
import android.support.annotation.Nullable;

import com.gromaudio.simplifiedmediaplayer.R;

import java.util.Formatter;
import java.util.Locale;

public class TimeUtils {

    /**
     * Convert milliseconds to time string
     */
    public static StringBuffer makeTimeString(@Nullable Context context, long msecs) {
        if (context == null) {
            return new StringBuffer();
        }
        long secs = msecs / 1000;
        String durationformat =
            context.getString(secs < 3600 ?
                R.string.durationformatshort :
                R.string.durationformatlong);

        StringBuilder formatBuilder = new StringBuilder();
        formatBuilder.setLength(0);

        final Object[] timeArgs = new Object[5];
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = (secs / 60) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;

        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
        StringBuffer result = new StringBuffer(formatter.format(durationformat, timeArgs).toString());
        formatter.close();
        return result;
    }

}
