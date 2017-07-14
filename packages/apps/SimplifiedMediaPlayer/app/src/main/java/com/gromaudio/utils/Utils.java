package com.gromaudio.utils;


import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {




    @NonNull
    public static Bitmap getBitmapFromResources(@NonNull Context context,
                                                @DrawableRes int drawableRes) {
        final Resources res = context.getResources();
        final BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opts.inScaled = false;
        Bitmap bitmap = getBitmapFromResources(res, drawableRes, opts);
        if (bitmap == null) {
            bitmap = getBitmapFromResources(res, drawableRes, null);
            if (bitmap == null) {
                // Single color bitmap will be created of 1x1 pixel
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            }
        }
        return bitmap;
    }


    @Nullable
    private static Bitmap getBitmapFromResources(@NonNull Resources res,
                                                 @DrawableRes int drawableRes,
                                                 BitmapFactory.Options opts) {
        try {
            return BitmapFactory.decodeResource(res, drawableRes, opts);
        } catch (Throwable e) {
            Logger.e("getBitmapFromResources", e.getMessage(), e);
            return null;
        }
    }


    public static void setViewBackground(View view, Drawable background) {
        view.setBackground(background);
    }

    public static boolean isRTL(Context context) {
        Configuration config = context.getResources().getConfiguration();
        return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public static int resolveColor(Context context, @AttrRes int color) {
        TypedArray a = context.obtainStyledAttributes(new int[]{color});
        int resId = a.getColor(0, 0);
        a.recycle();
        return resId;
    }

    public static int convertDpToPx(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static void configureActivityFullScreenMode(@NonNull Activity activity,
                                                       boolean isFullScreen) {
        final Window window = activity.getWindow();
        if (window != null) {
            final View decorView = window.getDecorView();
            if (decorView != null) {
                try {
                    if (isFullScreen) {
                        hideSystemUI(decorView);
                    } else {
                        showSystemUI(decorView);
                    }
                } catch (Throwable e) {
                    Logger.e(Utils.class.getSimpleName(), e.getMessage(), e);
                }
            }
        }
    }

    private static void hideSystemUI(final View decorView) {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private static void showSystemUI(final View decorView) {
        if(decorView!=null){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			/*decorView.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);*/
        }
    }


    /**
     * Return formatted date with TimeZone
     * @param milliseconds milliseconds
     * @return string
     */
    public static String getDate(long milliseconds) {
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.setTimeInMillis(milliseconds);

        StringBuilder result = new StringBuilder();
        appendTwoDigits(result, cal.get(Calendar.HOUR_OF_DAY));
        result.append(':');
        appendTwoDigits(result, cal.get(Calendar.MINUTE));
        return result.toString();
    }

    public static String getDateShort(long milliseconds) {
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.setTimeInMillis(milliseconds);

        StringBuilder result = new StringBuilder();
        result.append(cal.get(Calendar.HOUR));
        result.append(':');
        appendTwoDigits(result, cal.get(Calendar.MINUTE));
        result.append( (cal.get(Calendar.AM_PM)== Calendar.AM) ? "AM" : "PM" );
        return result.toString();
    }

    private static void appendTwoDigits(StringBuilder sb, int n) {
        if (n < 10) {
            sb.append('0');
        }
        sb.append(n);
    }
}
