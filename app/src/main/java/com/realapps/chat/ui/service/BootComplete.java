package com.realapps.chat.ui.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.utils.CustomDistribution;
import com.realapps.chat.ui.utils.PreferencesProviderWrapper;
import com.realapps.chat.ui.utils.PreferencesWrapper;
import com.realapps.chat.view.home.activity.HomeActivity;

/**
 * Created by Saif Ahmed
 */
public class BootComplete extends BroadcastReceiver {

    PreferencesProviderWrapper prefProviderWrapper;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            Thread t = new Thread("StartSip") {
                public void run() {
                    Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
                    // Optional, but here we bundle so just ensure we are using csipsimple package
                    serviceIntent.setPackage(context.getPackageName());
                    serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(context, HomeActivity.class));
                    context.startService(serviceIntent);
                    postStartSipService(context);
                }
            };
            t.start();
        }
    }
    private void postStartSipService(Context context) {
        System.out.println("Post Start Sip Service Home");
        // If we have never set fast settings
        prefProviderWrapper = new PreferencesProviderWrapper(context);
        if (CustomDistribution.showFirstSettingScreen()) {

            if (!prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false)) {
                Intent prefsIntent = new Intent(SipManager.ACTION_UI_PREFS_FAST);
                prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(prefsIntent);
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
}
