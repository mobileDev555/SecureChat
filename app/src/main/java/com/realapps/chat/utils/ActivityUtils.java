/*
 *
 *  Copyright (C) 2019 CORE TECHIES INDIA PRIVATE LIMITED
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://coretechies.com/license/apache-v2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 *
 */

package com.realapps.chat.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

/**
 * Created by Hari Choudhary on 1/28/2019 at 11:25 AM .
 * Core techies
 * hari@coretechies.org
 */

public final class ActivityUtils {

    public static String getCurrentActivity(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        assert am != null;
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        return cn.getClassName();
    }

    public static ComponentName getCurrentActivityComponent(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        assert am != null;
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        return cn;
    }
}
