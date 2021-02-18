package com.realapps.chat.notification;


import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.realapps.chat.services.MessageClass;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.FileLog;


public class MyFCMClass extends FirebaseMessagingService {

    private final String TAG = "JSA-FCM";
    Runnable runnable;
    Handler handler;
    public static int elapseTime = 0;
//    @Override
//    public void handleIntent(Intent intent) {
//       // super.handleIntent(intent);
//        AppConstants.messageClass = new MessageClass(getApplicationContext());
//        handler = new Handler(Looper.getMainLooper());
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                runnable = this;
//                if (!AppConstants.isAppActive) {
//                    FileLog.e("Queue Time :", String.valueOf(elapseTime));
//                    elapseTime++;
//                    if (elapseTime < 120) {
//                        AppConstants.messageClass = new MessageClass(getApplicationContext());
//                        handler.postDelayed(runnable, 1000);
//                    }
//                    if (elapseTime == 120) {
//                        elapseTime = 0;
//                        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
//                            AppConstants.mWebSocketClient.close();
//                            AppConstants.mWebSocketClient = null;
//                        }
//                        AppConstants.messageClass = null;
//                        FileLog.e("Queue Time :", "Socket Disconnected.");
//                    }
//                }
//
//            }
//        }, 1000);
//    }
}