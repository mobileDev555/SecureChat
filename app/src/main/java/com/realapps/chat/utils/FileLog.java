package com.realapps.chat.utils;

import android.util.Log;

/**
 * Created by Prashant Sharma on 1/5/2018.
 */

public class FileLog {

    private static boolean doPrint = true;

    public static void e(String TAG, String msg) {
        if (doPrint) {
            if (TAG.contains(":")) {
                Log.e(TAG, msg);
            } else {
                Log.e(TAG, " : " + msg);
            }
        }
    }

    public static void d(String TAG, String msg) {
        if (doPrint) {
            if (TAG.contains(":")) {
                Log.d(TAG, msg);
            } else {
                Log.d(TAG, " : " + msg);
            }
        }
    }

    public static void w(String TAG, String msg) {
        if (doPrint) {
            if (TAG.contains(":")) {
                Log.w(TAG, msg);
            } else {
                Log.w(TAG, " : " + msg);
            }
        }
    }

    public static void sout(String msg) {
        if (doPrint) {
            System.out.println(msg);
        }
    }

    public static void sout(int msg) {
        if (doPrint) {
            System.out.println(String.valueOf(msg));
        }
    }

    public static void i(String TAG, String msg) {
        if (doPrint) {
            Log.i(TAG, " : " + msg);
        }
    }
}
