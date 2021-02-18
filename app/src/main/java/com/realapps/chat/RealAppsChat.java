package com.realapps.chat;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.BuildConfig;
import com.androidnetworking.interceptors.HttpLoggingInterceptor;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.utils.AppLifecycleHandler;
import com.realapps.chat.utils.FileLog;
import com.realapps.chat.utils.LifeCycleDelegate;
import com.realapps.chat.utils.Utils;
import com.realapps.chat.view.lock.Lock;
import com.zxy.tiny.Tiny;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;

import io.fabric.sdk.android.Fabric;


/**
 * Created by Prashant Sharma on 1/31/2018.
 * Core techies
 * prashant@coretechies.org
 */


public class RealAppsChat extends Application implements LifeCycleDelegate {

    private FirebaseAnalytics mFirebaseAnalytics;
    private static final int EXEC_INTERVAL = 3 * 1000;
    Timer timer;
    Handler mHandler;
    private DbHelper dbHelper;
    private PendingIntent alarmIntent;
  //  Context context;
    private AlarmManager alarmMgr;
    private static final int REQUEST_CODE = 777;
    public static final long ALARM_INTERVAL =9000000;

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        mHandler = new Handler();
        Fabric.with(this, new Crashlytics());
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //Initialize Database.
        dbHelper = new DbHelper(getApplicationContext());
        Utils.init(RealAppsChat.this);
        AndroidNetworking.initialize(getApplicationContext());
        if (BuildConfig.DEBUG) {
            AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.BODY);
        }
        Tiny.getInstance().debug(true).init(this);
        takeLogs();
        AppLifecycleHandler lifeCycleHandler = new AppLifecycleHandler(this);
        registerLifecycleHandler(lifeCycleHandler);
        changeLanguage();
        deleteLogFileIfExit();
    }

    private void deleteLogFileIfExit() {
        File appDirectory = new File(Environment.getExternalStorageDirectory() + "/ShadowSecureChatApp");
        if (appDirectory.exists())
            deleteRecursive(appDirectory);
    }

    void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory() && fileOrDirectory.listFiles() != null && fileOrDirectory.listFiles().length > 0)
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();

    }

    private void takeLogs() {
        if (isExternalStorageWritable()) {

            File appDirectory = new File(Environment.getExternalStorageDirectory() + "/ShadowSecureChatApp");
            File logDirectory = new File(appDirectory + "/log");
            File logFile = new File(logDirectory, "logcat" + System.currentTimeMillis() + ".cabRoaster");

            // create app folder
            if (!appDirectory.exists()) {
                appDirectory.mkdir();
            }
            // create log folder
            if (!logDirectory.exists()) {
                logDirectory.mkdir();
            }
            // clear the previous logcat and then write the new one to the file
            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (isExternalStorageReadable()) {
            // only readable
        } else {
            // not accessible
        }
    }

    public DbHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = new DbHelper(getApplicationContext());
        }
        return dbHelper;
    }

    @Override
    public void onAppBackgrounded() {
        Log.e("RealAppsChat: ", "background");
        User_settings.setBackgroundApp(getApplicationContext(), true);
//        disableLock();

    }

    @Override
    public void onAppForegrounded() {
        //  timer.cancel();
        mHandler.removeCallbacksAndMessages(null);
        Log.e("RealAppsChat: ", "foreground");
        User_settings.setBackgroundApp(getApplicationContext(), false);
//        enableLock();
    }


    private void registerLifecycleHandler(AppLifecycleHandler lifeCycleHandler) {
        registerActivityLifecycleCallbacks(lifeCycleHandler);
        registerComponentCallbacks(lifeCycleHandler);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public void changeLanguage() {

        setApplicationLang();
        Locale myLocale = null;

        String lan = User_settings.getLanguage(getApplicationContext());

        if (lan != null && lan.length() > 0) {
            myLocale = new Locale(lan.toLowerCase());
            Locale.setDefault(myLocale);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                conf.setLocale(myLocale);
            } else {
                conf.locale = myLocale;
            }
            res.updateConfiguration(conf, dm);
        }

    }

    private void setApplicationLang() {
        String lang = Locale.getDefault().getDisplayLanguage();
        String country = Locale.getDefault().getCountry();
        String displayCountry = Locale.getDefault().getDisplayCountry();
        final CharSequence langs[] = new CharSequence[]{"ENGLISH", "NEDERLANDS", "ESPANOL", "TURKCE", "DEUTSCHE", "FRANCAISE", "ITALIANO", "POLSKI", "SVENSKA", "RYSKA", "PORTUGUES", "MANDARIN"};

    }

    private void enableLock() {
        Lock.getInstance(getApplicationContext()).enableLock();
    }

    private void disableLock() {
        Lock.getInstance(getApplicationContext()).disableLock();
    }

}