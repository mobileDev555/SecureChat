package com.realapps.chat.utils;

import android.os.Handler;

/**
 * Created by Saif Ahmed
 */
public class Blocker {
    private static final int DEFAULT_BLOCK_TIME = 1000;
    private boolean mIsBlockClick;

    /**
     * Block any event occurs in 1000 millisecond to prevent spam action
     * @return false if not in block state, otherwise return true.
     */
    public boolean block(int blockInMillis) {
        if (!mIsBlockClick) {
            mIsBlockClick = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsBlockClick = false;
                }
            }, blockInMillis);
            return false;
        }
        return true;
    }

    public boolean block() {
        return block(DEFAULT_BLOCK_TIME);
    }
}