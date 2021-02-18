/*
 * Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://mindorks.com/license/apache-v2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.realapps.chat.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.realapps.chat.view.home.activity.ChatWindowActivity;

/**
 * Created by Prashant Kumar Sharma on 27/01/17.
 */

public final class NetworkUtils {

    private static boolean isNetworkConnected = true;

    private NetworkUtils() {
        // This utility class is not publicly instantiable
    }

    public static boolean isNetworkConnected(Context mContext) {
      //  Log.e("NetworkClass", "CreateCheck...");
        try {
            if (!AppConstants.isThreadRunning) {
                new Thread(() -> {
                    AppConstants.isThreadRunning = true;
                  //  Log.e("NetworkClass", "checking...");
                    ConnectivityManager connectivityManager
                            = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    isNetworkConnected =  activeNetworkInfo != null && activeNetworkInfo.isConnected();
                    if (isNetworkConnected) {
                        if (ChatWindowActivity.networkReconnectedListener != null) {
                            ChatWindowActivity.networkReconnectedListener.onNetworkReconnected();
                        }
                    }
                }).start();
            }
            return isNetworkConnected;
        } catch (Exception e) {
            e.printStackTrace();
            return isNetworkConnected;
        }
    }
}