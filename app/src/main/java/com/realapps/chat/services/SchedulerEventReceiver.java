package com.realapps.chat.services;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.service.SipService;
import com.realapps.chat.ui.utils.CustomDistribution;
import com.realapps.chat.ui.utils.Log;
import com.realapps.chat.ui.utils.PreferencesProviderWrapper;
import com.realapps.chat.ui.utils.PreferencesWrapper;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Saif Ahmed
 */

public class SchedulerEventReceiver extends BroadcastReceiver {

    private static final String APP_TAG = "com.realapps.chat";
    Context ctx;
    private PreferencesProviderWrapper prefProviderWrapper;

    @SuppressLint("NewApi")
    @Override
    public void onReceive(final Context ctx, final Intent intent) {
        Log.e("Marius2","SchedulerEventReceiver");
        Log.d(APP_TAG, "SchedulerEventReceiver.onReceive() called");
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ctx, SchedulerEventReceiver.class); // explicit
        // intent
        PendingIntent intentExecuted = PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, 60);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), intentExecuted);
        this.ctx = ctx;
        prefProviderWrapper = new PreferencesProviderWrapper(ctx);
        if (isAppIsInBackground(ctx))
            if (!isMyServiceRunning(SipService.class))
                startSipService();
    }
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    private void startSipService() {
        System.out.println("Start Sip Service");
        Thread t = new Thread("StartSip") {
            public void run() {
                Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
                // Optional, but here we bundle so just ensure we are using csipsimple package
                serviceIntent.setPackage(ctx.getPackageName());
                serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(ctx, SchedulerEventReceiver.class));
                //startService(serviceIntent);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //startService(serviceIntent);

                    // stopSipService();
                    ctx.startForegroundService(new Intent(serviceIntent));//Crashing here.
                    postStartSipService();
                    Log.e("==========sip-101", "ok");
                } else {
                    //startForegroundService(new Intent(serviceIntent));
                    ctx.startService(serviceIntent);
                    postStartSipService();
                }

            }

            ;
        };
        t.start();
    }

    private void postStartSipService() {
        System.out.println("Post Start Sip Service");
        // If we have never set fast settings
        if (CustomDistribution.showFirstSettingScreen()) {
            if (!prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false)) {
                Intent prefsIntent = new Intent(SipManager.ACTION_UI_PREFS_FAST);
                prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(prefsIntent);
                System.out.println("Post Start Sip Service 1");
                return;
            }
        } else {
            boolean doFirstParams = !prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false);
            prefProviderWrapper.setPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, true);
            if (doFirstParams) {
                System.out.println("Post Start Sip Service elseif");
                prefProviderWrapper.resetAllDefaultValues();
            }
        }
    }

    private boolean isMyServiceRunning(Class serviceClass) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}