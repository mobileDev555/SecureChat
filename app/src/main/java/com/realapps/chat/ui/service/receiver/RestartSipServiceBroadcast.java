package com.realapps.chat.ui.service.receiver;

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
 * Created by inextrix on 27/10/17.
 */

public class RestartSipServiceBroadcast extends BroadcastReceiver {

    Context ctx;
    private PreferencesProviderWrapper prefProviderWrapper;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.ctx= context;
        prefProviderWrapper = new PreferencesProviderWrapper(context);
        if(intent.getAction().equals(SipManager.INTENT_SIP_SERVICE_RESTART)){
            startSipService();
        }

    }

    private void startSipService() {
        Thread t = new Thread("StartSip") {
            public void run() {
                Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
                // Optional, but here we bundle so just ensure we are using csipsimple package
                serviceIntent.setPackage(ctx.getPackageName());
                serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(ctx, HomeActivity.class));
                ctx.startService(serviceIntent);
                postStartSipService();
            }

            ;
        };
        t.start();
    }

    private void postStartSipService() {
        // If we have never set fast settings
        if (CustomDistribution.showFirstSettingScreen()) {
            if (!prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false)) {
                Intent prefsIntent = new Intent(SipManager.ACTION_UI_PREFS_FAST);
                prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(prefsIntent);
                return;
            }
        } else {
            boolean doFirstParams = !prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false);
            prefProviderWrapper.setPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, true);
            if (doFirstParams) {
                prefProviderWrapper.resetAllDefaultValues();
            }
        }
    }
}
