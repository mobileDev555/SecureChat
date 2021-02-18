package com.realapps.chat.utils;

import android.os.Handler;

/**
 * Created by Hari Choudhary on 9/6/2018.
 * Core techies
 * hari@coretechies.org
 */
public class MultiClickBlocker {
    private static final int DEFAULT_BLOCK_TIME = 1000;
    private static boolean mIsBlockClick;
    public static boolean block(int blockInMillis) {
        if (!mIsBlockClick) {
            mIsBlockClick = true;
            new Handler().postDelayed(() -> mIsBlockClick = false, blockInMillis);
            return false;
        }
        return true;
    }

    public static boolean block() {
        return block(DEFAULT_BLOCK_TIME);
    }
}
