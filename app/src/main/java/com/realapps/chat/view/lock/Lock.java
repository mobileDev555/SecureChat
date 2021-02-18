/*
 *
 *  Copyright (C) 2019 CORE TECHIES INDIA PRIVATE LIMITED
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://coretechies.com/license/apache-v2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 *
 */

package com.realapps.chat.view.lock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.realapps.chat.AlarmReceiver;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.utils.ActivityUtils;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.view.ExitActivity;
import com.realapps.chat.view.home.activity.HomeActivity;
import com.realapps.chat.view.home.activity.LockScreenActivity;
import com.realapps.chat.view.login.activity.LoginActivity;

import java.util.Objects;

/**
 * Created by Hari Choudhary on 1/28/2019 at 11:03 AM .
 * Core techies
 * hari@coretechies.org
 */

public class Lock implements Handler.Callback {

    private static final int LOCK_AFTER_DELAY = 0x66;
    private static Context mContext;
    private static Lock instance;
    private static Handler appKillingHandler;
    private BroadcastReceiver mWakeUpReceiver;
    private boolean isAppBackgrounded = false;
    private Handler autoLockHandler;
    private boolean lock = false;
    private boolean isAppExited = false;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    public static Lock getInstance(Context context) {

        mContext = context;
        if (instance == null)
            instance = new Lock();
        return instance;
    }

    private boolean canLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    private boolean isAppBackgrounded() {
        return isAppBackgrounded;
    }

    private void setIsAppBackgrounded(boolean isAppBackgrounded) {
        this.isAppBackgrounded = isAppBackgrounded;
    }

    public void enableLock() {

        if (isAppExited)
            return;

        registerWakeUpReceiver();

        showLockScreen(true);

        setIsAppBackgrounded(false);

        if (alarmMgr!= null && alarmIntent!=null) {
            alarmMgr.cancel(alarmIntent);
        }
    }


    public void disableLock() {
        setIsAppBackgrounded(true);
        unRegisterWakeUpReceiver();
        setLockAfterDelay();
        killAppAfter5Minutes();
//        alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(mContext, AlarmReceiver.class);
//        alarmIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
//        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, 9000000,alarmIntent);
    }

    private void setLockAfterDelay() {
        Log.e("handleMessage: ", "start");
        autoLockHandler = new Handler(this);
        autoLockHandler.removeMessages(LOCK_AFTER_DELAY);
        autoLockHandler.sendEmptyMessageDelayed(LOCK_AFTER_DELAY, User_settings.getLockTime(mContext));
    }


    private void registerWakeUpReceiver() {
        mWakeUpReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Objects.requireNonNull(intent.getAction()).equals(Intent.ACTION_SCREEN_OFF)) {
                    handleActionOnScreenLock();
                    setLockAfterDelay();
                    if (appKillingHandler != null)
                        killAppAfter5Minutes();
                } else if (Objects.requireNonNull(intent.getAction()).equals(Intent.ACTION_SCREEN_ON)) {
                    showLockScreen(false);
                    removeAppKillingHandler();
                }
                else
                {
                    showLockScreen(false);
                    removeAppKillingHandler();
                }
            }
        };

        IntentFilter lockFilter = new IntentFilter();
        lockFilter.addAction(Intent.ACTION_SCREEN_OFF);
        lockFilter.addAction(Intent.ACTION_USER_PRESENT);
        lockFilter.addAction(Intent.ACTION_SCREEN_ON);

        mContext.registerReceiver(mWakeUpReceiver, lockFilter);
    }

    public void removeAppKillingHandler() {
        if (appKillingHandler != null) {
            appKillingHandler.removeCallbacksAndMessages(null);
        }
    }

    private void killAppAfter5Minutes() {
        appKillingHandler = new Handler();
        appKillingHandler.postDelayed(() -> {
            if (AppConstants.lockscreen){
            isAppExited = true;
            if (appKillingHandler != null)
                appKillingHandler.removeCallbacksAndMessages(null);
            appKillingHandler = null;
            ExitActivity.exitApplication(mContext);
        }
        }, 60 * 1000 * 3);
    }

    private void unRegisterWakeUpReceiver() {
        try {
            mContext.unregisterReceiver(mWakeUpReceiver);
        } catch (IllegalArgumentException e) {
            mWakeUpReceiver = null;
        }
    }

    private void showLockScreen(boolean checkBackground) {

        removeLockAfterDelay();

        if (!canShowLockScreen()) {
            return;
        }

        if (checkBackground)
            if (!isAppBackgrounded()) {
                return;
            }

        if (!User_settings.isUserLogin(mContext))
            return;

        if (canLock()) {
            handleActionOnScreenLock();
            launchLockActivity();
        }
    }


    private void handleActionOnScreenLock() {
        setLock(false);
        AppConstants.lockscreen = true;
        new HomeActivity().stopBackgroundThread();
    }

    private void removeLockAfterDelay() {
        if (autoLockHandler != null) {
            autoLockHandler.removeMessages(LOCK_AFTER_DELAY);
            autoLockHandler.removeCallbacksAndMessages(null);
            autoLockHandler = null;
            Log.e("handleMessage: ", "stoped");
        }

        System.gc();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case LOCK_AFTER_DELAY:
                setLock(true);
                Log.e("handleMessage: ", "called");
                return false;
        }
        return false;
    }


    private boolean canShowLockScreen() {
        if (ActivityUtils.getCurrentActivity(mContext).contains(LockScreenActivity.class.getSimpleName())) {
            return false;
        } else if (ActivityUtils.getCurrentActivity(mContext).contains(LoginActivity.class.getSimpleName())) {
            return false;
        } else {
            return true;
        }


    }


    public void lockApplication() {
        Log.e("=====lockapp", "ok");
        setLock(false);
        AppConstants.lockscreen = true;
//        new HomeActivity().stopBackgroundThread();
        removeLockAfterDelay();
        launchLockActivity();
        killAppAfter5Minutes();
    }

    private void launchLockActivity() {
        Intent i = new Intent(mContext, LockScreenActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mContext.startActivity(i);
    }

}

