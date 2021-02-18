package com.realapps.chat.ui.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Saif Ahmed
 */

public class SwitchOffReceiver extends BroadcastReceiver {

    public static SwitchOff switchOff;


    @Override
    public void onReceive(Context context, Intent intent) {
            if (switchOff!=null)
                switchOff.onSwitchOffPhone();
    }

    public interface SwitchOff{
        void onSwitchOffPhone();
    }

}
