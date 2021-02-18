package com.realapps.chat.view.home.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.view.login.activity.LoginActivity;

/**
 * Created by Hari Choudhary on 3/26/2019 at 4:21 PM .
 * Core techies
 * hari@coretechies.org
 */
public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isTaskRoot()) {
            Intent intent = getPackageManager()
                    .getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
            startActivity(intent);
        } else {
            if (User_settings.getLastActivity(this).contains(LockScreenActivity.class.getSimpleName()) || User_settings.getLastActivity(this).contains(LoginActivity.class.getSimpleName())) {

            } else {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        }
        finish();

    }


}
