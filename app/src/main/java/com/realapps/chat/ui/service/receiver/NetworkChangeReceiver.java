package com.realapps.chat.ui.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.realapps.chat.ui.api.GlobalClass;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.utils.PreferencesProviderWrapper;

/**
 * Created by inextrix on 2/11/17.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    private boolean isConnected = false;
    private static final String LOG_TAG = "NetworkChangeReceiver";
    private PreferencesProviderWrapper prefProviderWrapper;

    @Override
    public void onReceive(Context context, Intent intent) {
        GlobalClass gc = GlobalClass.getInstance();
        try {

            //Reconnect TC_008 & TC_009: Sending network broadcast
            Log.e("====NetChangeReceiver", "" + isNetworkAvailable(context));
            if (isNetworkAvailable(context)) {
                gc.setIsOnline(true);
                gc.setAccountStatus("SipConnecting");
                Intent intent1 = new Intent();
                intent1.setAction(SipManager.ACTION_NETWORK_CONNECTED);
                context.sendBroadcast(intent1);


                //Himadri
                //For handling sip registration request on call notification
                Intent intent12 = new Intent();
                intent12.setAction(SipManager.ACTION_SIP_bROADCAST);
                context.sendBroadcast(intent12);

               /* prefProviderWrapper = new PreferencesProviderWrapper(context);

                if (isMyServiceRunning(SipService.class,context)) {
                    stopSipService(context);
                }
                startSipService(context);*/

            } else {
                gc.setIsOnline(false);
                gc.setAccountStatus("SipDisconnecting");
                Intent intent1 = new Intent();
                intent1.setAction(SipManager.ACTION_NETWORK_DISCONNECTED);
                context.sendBroadcast(intent1);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e("=====sip register", e.getMessage());
        }
    }


    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        if (!isConnected) {
                            isConnected = true;
                        }
                        return true;
                    }
                }
            }
        }
        isConnected = false;
        return false;
    }
}
