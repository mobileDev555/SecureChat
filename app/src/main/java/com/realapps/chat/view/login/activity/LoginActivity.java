package com.realapps.chat.view.login.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.view.login.fragment.FragmentKeyGeneration;
import com.realapps.chat.view.login.fragment.FragmentSplash;

public class LoginActivity extends AppCompatActivity {
    Context mContext;
    Activity mActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_login);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        mContext = LoginActivity.this;

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }


        updateScreen(new FragmentSplash(), "", false);

        User_settings.setLastActivity(this, LoginActivity.class.getSimpleName());

    }

    public void updateScreen(Fragment fragment, String title, boolean animStatus) {
        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        String backStateName = fragment.getClass().getName();

        if (animStatus)
            ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        ft.replace(R.id.frg_container, fragment);
        ft.addToBackStack(backStateName);
        ft.commitAllowingStateLoss();
    }

    Fragment getCurrentFragment() {
        FragmentManager manager = getSupportFragmentManager();
        return manager.findFragmentById(R.id.frg_container);
    }

    @Override
    public void onBackPressed() {
        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
        int count = manager.getBackStackEntryCount();

        Fragment currFrag = getCurrentFragment();
        Log.d("count", "" + manager.getBackStackEntryCount());
        if (count == 2) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            finish();
            System.exit(0);
        } else if (currFrag instanceof FragmentKeyGeneration) {
            if (FragmentKeyGeneration.splashScreen) {
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                finish();
                System.exit(0);
            }
        } else {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            finish();
            System.exit(0);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
