package com.realapps.chat.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.ui.utils.Log;
import com.realapps.chat.view.NotificationActivity;
import com.realapps.chat.view.home.activity.HomeActivity;
import com.realapps.chat.view.home.activity.LockScreenActivity;

import java.util.ArrayList;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by Prashant Sharma on 3/20/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class NotificationUtils {

    static String CHANNEL_ID = "Channel_2";
    static String CHANNEL_Messages = "Channel_Messages_01";
    static String CHANNEL_Request = "Channel_request";
    static String CHANNEL_Name = "Protext";
    public static void showNotification(Context mContext, ChatListEntity entity, int notify_id, String title, int msg) {
        //======== notification sound that we set up in the settins page
        Uri uri = Uri.parse(User_settings.getNotifySoundSelector(mContext));
        Log.e("=========notify_sound-4", uri.toString());
//        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("Channel_2", "Protext", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mNotificationManager.createNotificationChannel(channel);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(mContext.getApplicationContext(), "Channel_2");
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, getIntent(mContext, new DbHelper(mContext).getTotalUnreadMessageList()), PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle("Shadow Secure Chat");
            mBuilder.setColor(Color.parseColor("#27ba96"));
            mBuilder.setContentText(mContext.getString(R.string.new_messages_s, msg));
            mBuilder.setStyle(bigText);
            mBuilder.setSound(uri);
            mBuilder.setLights(Color.GREEN, 3000, 3000);
            mBuilder.setPriority(NotificationManager.IMPORTANCE_MAX);
            mNotificationManager.notify(0, mBuilder.build());
            showBadge(mContext, mBuilder.build(), msg);
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle("Shadow Secure Chat")
                    .setContentText(mContext.getString(R.string.new_messages_s, msg))
                    .setColor(Color.parseColor("#27ba96"))
                    .setChannelId(CHANNEL_ID);

            if (entity.getSnoozeStatus() == AppConstants.NOTIFICATION_SNOOZE_NO) {
                builder.setSound(uri);
            }

            builder.setAutoCancel(true);
            if (Build.VERSION.SDK_INT >= 21)
                builder.setVibrate(new long[1000]);
            else
                builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

            builder.setLights(Color.GREEN, 3000, 3000);
            PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, getIntent(mContext, new DbHelper(mContext).getTotalUnreadMessageList()), PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);
            Notification note = builder.build();
            note.flags |= Notification.FLAG_AUTO_CANCEL;
            note.defaults |= Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;
            // Add as notification
            NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(notify_id, note);
            showBadge(mContext, builder.build(), msg);
        }
    }

    public static int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_notification : R.mipmap.ic_launcher;
    }

    public static void showNotification(Context mContext, int notify_id, String title, String msg) {
        //======== notification sound that we set up in the settins page
        Uri uri = Uri.parse(User_settings.getNotifySoundSelector(mContext));
        Log.e("=========notify_sound-5", uri.toString());
//        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(mContext.getApplicationContext(), "Channel_2");
            Intent ii = new Intent(mContext.getApplicationContext(), HomeActivity.class);
            ii.putExtra(AppConstants.EXTRA_FROM_NOTIFICATION, true);
            ii.putExtra(AppConstants.EXTRA_ACTIVITY_TYPE, -1);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, ii, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
            bigText.bigText("Shadow Secure Chat");
            bigText.setBigContentTitle(msg);
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setColor(Color.parseColor("#27ba96"));
            mBuilder.setContentTitle("");
            mBuilder.setContentText("");
            mBuilder.setPriority(Notification.PRIORITY_MAX);
            mBuilder.setStyle(bigText);
            mBuilder.setSound(uri);
            NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("Channel_2", "Protext", NotificationManager.IMPORTANCE_HIGH);
                    channel.enableLights(true);
                    channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                    channel.setLightColor(Color.GREEN);
                    mNotificationManager.createNotificationChannel(channel);
            }
            mNotificationManager.notify(0, mBuilder.build());
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle("Shadow Secure Chat")
                    .setContentText(msg)
                    .setColor(Color.parseColor("#27ba96"))
                    .setChannelId(CHANNEL_ID)
                    .setSound(uri)
                    .setAutoCancel(true)
                    .setLights(Color.GREEN, 3000, 3000);

            if (Build.VERSION.SDK_INT >= 21)
                builder.setVibrate(new long[1000]);
            else
                builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

            Intent notificationIntent = new Intent(mContext.getApplicationContext(), HomeActivity.class);
            notificationIntent.putExtra(AppConstants.EXTRA_FROM_NOTIFICATION, true);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            notificationIntent.putExtra(AppConstants.EXTRA_ACTIVITY_TYPE, -1);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


            PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            Notification note = builder.build();
            note.flags |= Notification.FLAG_AUTO_CANCEL;
            note.defaults |= Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;
            // Add as notification
            NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(notify_id, note);
        }
    }

    public static void showBadge(Context mContext, Notification notification, int count) {
        String deviceName = android.os.Build.MODEL;
        String deviceMan = android.os.Build.MANUFACTURER;
        if (deviceMan.equalsIgnoreCase("Xiaomi"))
            ShortcutBadger.applyNotification(mContext, notification, count);
        else
            ShortcutBadger.applyCount(mContext, count);
    }

    public static void showBadge(Context mContext, int count) {
        ShortcutBadger.applyCount(mContext, count);
    }

    private static Intent getIntent(Context context, ArrayList<ChatMessageEntity> messages) {
        Intent intent = new Intent(context.getApplicationContext(), NotificationActivity.class);
        return intent;
    }

    private static boolean canShowLock(Context context) {
        if (User_settings.isBackground(context))
            return true;
        else
            return ActivityUtils.getCurrentActivity(context).contains(LockScreenActivity.class.getSimpleName());

    }

    private static ChatMessageEntity getMessageEntity(ArrayList<ChatMessageEntity> messages) {
        ChatMessageEntity message = null;
        int chatId = messages.get(0).getChatId();

        for (ChatMessageEntity entity : messages) {
            if (chatId == entity.getChatId())
                message = entity;
            else {
                message = null;
                break;
            }
        }
        return message;
    }

}
