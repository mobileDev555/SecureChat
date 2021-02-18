package com.realapps.chat;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.ui.api.GlobalClass;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.helper.PrefManager;
import com.realapps.chat.ui.utils.CustomDistribution;
import com.realapps.chat.ui.utils.Log;
import com.realapps.chat.ui.utils.PreferencesProviderWrapper;
import com.realapps.chat.ui.utils.PreferencesWrapper;

import java.util.List;

/**
 * Created by inextrix on 17/1/19.
 */

public class FirebaseBackgroundService extends WakefulBroadcastReceiver {
    private static final String TAG = "FirebaseService";
    boolean checkapp;
    private PreferencesProviderWrapper prefProviderWrapper;
    PrefManager pref;
    GlobalClass gc;
    Context ctx;
    String acc_token;
    DbHelper dbHelp;

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Notification received 0"+"Done");
        if (intent.getExtras() != null) {
            for (String key : intent.getExtras().keySet()) {
                Object value = intent.getExtras().get(key);
                //Log.e("FirebaseDataReceiver", "Key: " + key + " Value: " + value);
                if (key.equalsIgnoreCase("gcm.notification.body") && value != null) {
                    System.out.println("Notification received 1"+"Done");
                    prefProviderWrapper = new PreferencesProviderWrapper(context);
                    pref = new PrefManager(context);
                    gc = GlobalClass.getInstance();
                    ctx = context;
                    checkapp = isAppIsInBackground(context);
                    acc_token = pref.getToken();
                    dbHelp = new DbHelper(context);

                    if (checkapp == true) {

                       /* if (isMyServiceRunning(SipService.class,context)) {
                            // stopSipService();
                        } else {*/
                            startSipService();
                        //}


                    }


                }
            }
        }
    }

    private boolean isMyServiceRunning(Class serviceClass,Context ctx) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // Service monitoring stuff
    private void startSipService() {
        System.out.println("Start Sip Service"+"Service");
        System.out.println("Notification received 2"+"Done");
        Thread t = new Thread("StartSip") {
            public void run() {
                Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
                // Optional, but here we bundle so just ensure we are using csipsimple package
                serviceIntent.setPackage(ctx.getPackageName());
                serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(ctx, FirebaseBackgroundService.class));
                ctx.startService(serviceIntent);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //startService(serviceIntent);

                    // stopSipService();
//                    ctx.startForegroundService(new Intent(serviceIntent));//Crashing here.
                    postStartSipService();
                    Log.e("==========sip-104", "ok");
                } else {
                    //startForegroundService(new Intent(serviceIntent));
//                    ctx.startService(serviceIntent);
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
                System.out.println("Post Start Sip Service if if");
                return;
            }else{
                System.out.println("Post Start Sip Service if else");
            }
        } else {
            boolean doFirstParams = !prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false);
            prefProviderWrapper.setPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, true);
            if (doFirstParams) {
                prefProviderWrapper.resetAllDefaultValues();
                System.out.println("Post Start Sip Service else if");

            }else{
                System.out.println("Post Start Sip Service else else");
            }
        }
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
}
