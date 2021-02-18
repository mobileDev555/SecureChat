package com.realapps.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.view.ExitActivity;

/**
 * Created by Hari Choudhary on 7/25/2019 at 5:18 PM .
 * Core techies
 * hari@coretechies.org
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if(User_settings.isBackground(context))
        ExitActivity.exitApplication(context);


    }
}
