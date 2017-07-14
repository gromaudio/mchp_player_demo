package com.gromaudio.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;


public final class Logger {

    private static final String TAG = "DashLinQ";
	private static boolean debug = true;

	@NonNull
	private static String changeTag(String tag) {
		tag = tag == null ? TAG : ( TAG + " @ " + tag);
		return tag;
	}

	@NonNull
	private static String validateMessage(@Nullable String msg) {
		return msg == null ? "" : msg;
	}

	public static void d(String tag, String message) {
		if (debug && message != null)
			Log.d(changeTag(tag), validateMessage(message));
	}


	public static void i(String tag, String message) {
		if (debug && message != null)
			Log.i(changeTag(tag), validateMessage(message));
	}

	public static void v(String tag, String message) {
		if (debug && message != null)
			Log.v(changeTag(tag), validateMessage(message));
	}
	//--------------------------------------------------------------------------
	public static void e(String msg) {
		Log.e(changeTag(null), getLocation() + msg);
	}

	static void e(String tag, String message) {
		Log.e(changeTag(tag), validateMessage(message));
	}

	public static void e(String tag, String message, Throwable err) {
		Log.e(changeTag(tag), validateMessage(message), err);
	}

	@NonNull
	private static String getLocation() {
		final String className = Logger.class.getName();
		final StackTraceElement[] traces = Thread.currentThread().getStackTrace();
		boolean found = false;
		for (final StackTraceElement trace : traces) {
			try {
				if (found) {
					if (!trace.getClassName().startsWith(className)) {
						Class<?> clazz = Class.forName(trace.getClassName());
						return "[" + getClassName(clazz) + ":" + trace.getMethodName() + ":" + trace.getLineNumber() + "]: ";
					}
				} else if (trace.getClassName().startsWith(className)) {
					found = true;
				}
			} catch (ClassNotFoundException ignored) {}
		}
		return "[]: ";
	}

	private static String getClassName(Class<?> clazz) {
		if (clazz != null) {
			if (!TextUtils.isEmpty(clazz.getSimpleName())) {
				return clazz.getSimpleName();
			}

			return getClassName(clazz.getEnclosingClass());
		}

		return "";
	}
}