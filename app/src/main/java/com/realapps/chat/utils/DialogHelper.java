package com.realapps.chat.utils;

import android.app.Activity;
import android.support.v7.app.AlertDialog;

import com.realapps.chat.R;

import java.util.List;


public class DialogHelper {
    public static void showRationaleDialog(final PermissionUtils.OnRationaleListener.ShouldRequest shouldRequest) {
        Activity topActivity = getTopActivity();
        if (topActivity == null) return;
        new AlertDialog.Builder(topActivity)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(R.string.permission_rationale_message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> shouldRequest.again(true))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> shouldRequest.again(false))
                .setCancelable(false)
                .create()
                .show();

    }

    public static void showOpenAppSettingDialog() {
        Activity topActivity = getTopActivity();
        if (topActivity == null) return;
        new AlertDialog.Builder(topActivity)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(R.string.permission_denied_forever_message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> PermissionUtils.openAppSettings())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                .setCancelable(false)
                .create()
                .show();
    }

    public static Activity getTopActivity() {
        if (Utils.sTopActivityWeakRef != null) {
            Activity activity = Utils.sTopActivityWeakRef.get();
            if (activity != null) {
                return activity;
            }
        }
        List<Activity> activities = Utils.sActivityList;
        int size = activities.size();
        return size > 0 ? activities.get(size - 1) : null;
    }

}
