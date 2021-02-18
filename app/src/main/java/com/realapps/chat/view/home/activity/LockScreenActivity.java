package com.realapps.chat.view.home.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.realapps.chat.R;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.view.login.fragment.FragmentLock;


public class LockScreenActivity extends AppCompatActivity {


    @Override
    protected void onStart() {
        super.onStart();
        AppConstants.lockscreen = true;
        overridePendingTransition(0, 0);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("=====lockscreen destroy", "ok");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Log.e("=====lockscreen start", "ok");
        showLockFragment();
    }

    private void showLockFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frg_container, new FragmentLock())
                .commitAllowingStateLoss();
    }


    @Override
    protected void onResume() {
        super.onResume();
        User_settings.setLastActivity(this, LockScreenActivity.class.getSimpleName());
        clearNotification();
    }

    @Override
    public void onBackPressed() {

    }

    private void clearNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null)
            mNotificationManager.cancelAll();
    }
}
