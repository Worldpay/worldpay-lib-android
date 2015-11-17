package com.worldpay;

import android.util.Log;

/**
 * Debugging utility.
 */
final class DebugLogger {
    public static String tag = WorldPay.TAG;

    public static boolean debug = false;

    public static void setDebug(boolean debug) {
        DebugLogger.debug = debug;
    }

    public static void setTag(String tag) {
        DebugLogger.tag = tag;
    }

    public static void d(String str) {
        if (debug)
            Log.d(tag, str);
    }

    public static void e(Exception e) {
        if (debug) {
            Log.d(tag, "Error", e);
            System.out.println("--------------------");
            e.printStackTrace();
        }
    }

}
