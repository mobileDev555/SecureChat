/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 * <p>
 * CSipSimple is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * If you own a pjsip commercial license you can also redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License
 * as an android library.
 * <p>
 * CSipSimple is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.realapps.chat.ui.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.CallLog;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.ui.api.GlobalClass;
import com.realapps.chat.ui.api.SipCallSession;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.api.SipMessage;
import com.realapps.chat.ui.api.SipProfile;
import com.realapps.chat.ui.api.SipProfileState;
import com.realapps.chat.ui.api.SipUri;
import com.realapps.chat.ui.models.CallerInfo;
import com.realapps.chat.ui.utils.Compatibility;
import com.realapps.chat.ui.utils.CustomDistribution;
import com.realapps.chat.ui.utils.Log;
import com.realapps.chat.ui.widgets.RegistrationNotification;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.DateTimeUtils;
import com.realapps.chat.utils.FileLog;
import com.realapps.chat.utils.NetworkUtils;
import com.realapps.chat.utils.NotificationUtils;
import com.realapps.chat.utils.SocketUtils;
import com.realapps.chat.view.home.activity.HomeActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class SipNotifications {

    private final NotificationManager notificationManager;
    private final Context context;
    private Builder inCallNotification;
    private NotificationCompat.Builder missedCallNotification;
    private Builder messageNotification;
    private Builder messageVoicemail;
    private boolean resolveContacts = true;
    String CHANNEL_ID = "my_channel_01";
    int count = 0;

    public static final int REGISTER_NOTIF_ID = 1;
    public static final int CALL_NOTIF_ID = REGISTER_NOTIF_ID + 1;
    public static final int CALLLOG_NOTIF_ID = REGISTER_NOTIF_ID + 2;
    public static final int MESSAGE_NOTIF_ID = REGISTER_NOTIF_ID + 3;
    public static final int VOICEMAIL_NOTIF_ID = REGISTER_NOTIF_ID + 4;

    private static boolean isInit = false;
    public static String TAG = "SipNotification";

    private void wakeUpLock() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if(isScreenOn==false) {
            @SuppressLint("InvalidWakeLockTag")
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(10000);
            @SuppressLint("InvalidWakeLockTag")
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");
            wl_cpu.acquire(10000);
        }
    }

    public SipNotifications(Context aContext) {
        context = aContext;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, aContext.getResources().getString(R.string.app_name), importance);

            mChannel.enableLights(true);
            mChannel.enableVibration(false);
            mChannel.setLightColor(Color.GREEN);
            mChannel.setVibrationPattern(new long[]{0, 0, 0, 0, 0});
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(mChannel);
        }

        if (!isInit) {
            cancelAll();
            cancelCalls();
            isInit = true;
        }

        if (!Compatibility.isCompatible(9)) {
            searchNotificationPrimaryText(aContext);
        }
    }

    private Integer notificationPrimaryTextColor = null;

    private static String TO_SEARCH = "Search";

    // Retrieve notification textColor with android < 2.3
    @SuppressWarnings("deprecation")
    private void searchNotificationPrimaryText(Context aContext) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Notification ntfL = new Notification.Builder(aContext)
                        .setContentIntent(null)
                        .setContentTitle(TO_SEARCH)
                        .setContentText("")
                        .build();
                LinearLayout group = new LinearLayout(aContext);
                ViewGroup event = (ViewGroup) ntfL.contentView.apply(aContext, group);
                recurseSearchNotificationPrimaryText(event);
                group.removeAllViews();
            } else {
                Notification ntf = new Notification();
                //Himadri
                try {
                    Method deprecatedMethod = ntf.getClass().getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
                    deprecatedMethod.invoke(ntf, aContext, TO_SEARCH, "", null);

                    //ntf.setLatestEventInfo(aContext, TO_SEARCH, "", null);
                    LinearLayout group = new LinearLayout(aContext);
                    ViewGroup event = (ViewGroup) ntf.contentView.apply(aContext, group);
                    recurseSearchNotificationPrimaryText(event);
                    group.removeAllViews();
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
                    Log.w(TAG, "Method not found", e);
                }


            }

        } catch (Exception e) {
            Log.e(THIS_FILE, "Can't retrieve the color", e);
        }
    }

    private boolean recurseSearchNotificationPrimaryText(ViewGroup gp) {
        final int count = gp.getChildCount();
        for (int i = 0; i < count; ++i) {
            if (gp.getChildAt(i) instanceof TextView) {
                final TextView text = (TextView) gp.getChildAt(i);
                final String szText = text.getText().toString();
                if (TO_SEARCH.equals(szText)) {
                    notificationPrimaryTextColor = text.getTextColors().getDefaultColor();
                    return true;
                }
            } else if (gp.getChildAt(i) instanceof ViewGroup) {
                if (recurseSearchNotificationPrimaryText((ViewGroup) gp.getChildAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }


    // Foreground api

    private static final Class<?>[] SET_FG_SIG = new Class[]{boolean.class};
    private static final Class<?>[] START_FG_SIG = new Class[]{int.class, Notification.class};
    private static final Class<?>[] STOP_FG_SIG = new Class[]{boolean.class};
    private static final String THIS_FILE = "Notifications";

    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

    private void invokeMethod(Method method, Object[] args) {
        try {
            method.invoke(context, args);
        } catch (InvocationTargetException e) {
            // Should not happen.
            Log.e(THIS_FILE, "Unable to invoke method", e);
        } catch (IllegalAccessException e) {
            // Should not happen.
            Log.e(THIS_FILE, "Unable to invoke method", e);
        }
    }

    /**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    private void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            invokeMethod(mStartForeground, mStartForegroundArgs);
            return;
        }

        // Fall back on the old API.
        mSetForegroundArgs[0] = Boolean.TRUE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
        notificationManager.notify(id, notification);
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    private void stopForegroundCompat(int id) {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            invokeMethod(mStopForeground, mStopForegroundArgs);
            return;
        }

        // Fall back on the old API. Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        notificationManager.cancel(id);
        mSetForegroundArgs[0] = Boolean.FALSE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
    }

    private boolean isServiceWrapper = false;

    public void onServiceCreate() {
        try {
            mStartForeground = context.getClass().getMethod("startForeground", START_FG_SIG);
            mStopForeground = context.getClass().getMethod("stopForeground", STOP_FG_SIG);
            isServiceWrapper = true;
            return;
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }
        try {
            mSetForeground = context.getClass().getMethod("setForeground", SET_FG_SIG);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("OS doesn't have Service.startForeground OR Service.setForeground!");
        }
        isServiceWrapper = true;
    }

    public void onServiceDestroy() {
        // Make sure our notification is gone.
        cancelAll();
        cancelCalls();
    }

    // Announces

    // Register
    public synchronized void notifyRegisteredAccounts(ArrayList<SipProfileState> activeAccountsInfos, boolean showNumbers) {
        if (!isServiceWrapper) {
            Log.e(THIS_FILE, "Trying to create a service notification from outside the service");
            return;
        }
        int icon = R.mipmap.ic_launcher;
        CharSequence tickerText = context.getString(R.string.service_ticker_registered_text);
        long when = System.currentTimeMillis();


        Builder nb = new Builder(context);
        nb.setSmallIcon(icon);
        nb.setTicker(tickerText);
        nb.setWhen(when);
        Intent notificationIntent = new Intent(SipManager.ACTION_SIP_DIALER);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RegistrationNotification contentView = new RegistrationNotification(context.getPackageName());
        contentView.clearRegistrations();
		/*if(!Compatibility.isCompatible(9)) {
		    contentView.setTextsColor(notificationPrimaryTextColor);
		}*/
        contentView.addAccountInfos(context, activeAccountsInfos);

        if (Compatibility.checkForVersion()) {
            contentView.setTextsColor(context.getResources().getColor(R.color.black));
        } else {
            contentView.setTextsColor(context.getResources().getColor(R.color.white));
        }

        // notification.setLatestEventInfo(context, contentTitle,
        // contentText, contentIntent);
        nb.setOngoing(true);
        nb.setOnlyAlertOnce(true);
        nb.setContentIntent(contentIntent);
        nb.setContent(contentView);

        Notification notification = nb.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        // We have to re-write content view because getNotification setLatestEventInfo implicitly
        notification.contentView = contentView;
        if (showNumbers) {
            // This only affects android 2.3 and lower
            notification.number = activeAccountsInfos.size();
        }
        startForegroundCompat(REGISTER_NOTIF_ID, notification);
        Log.e("==========sip-102", "ok");
    }

    private String formatRemoteContactString(String title, String accId) {//long accId) {
        System.out.println("Notificaiton::: " + String.valueOf(title) + "-" + accId);
        String formattedRemoteContact;
        if (accId.length() > 0) {
            formattedRemoteContact = String.valueOf(title) + ", Last missed call from " + accId;
        } else {
            formattedRemoteContact = String.valueOf(title);
        }
        if (resolveContacts) {
            CallerInfo callerInfo = CallerInfo.getCallerInfoFromSipUri(context, formattedRemoteContact);
            if (callerInfo != null && callerInfo.contactExists) {
                StringBuilder remoteInfo = new StringBuilder();
                remoteInfo.append(callerInfo.name);
              /*  remoteInfo.append(" <");
                remoteInfo.append(SipUri.getCanonicalSipContact(String.valueOf(title)));
                remoteInfo.append(">");*/
                formattedRemoteContact = remoteInfo.toString();
            }
        }
        return formattedRemoteContact;
    }

    private String formatRemoteContactString(String remoteContact) {
        String formattedRemoteContact = remoteContact;
        if (resolveContacts) {
            CallerInfo callerInfo = CallerInfo.getCallerInfoFromSipUri(context, formattedRemoteContact);
            if (callerInfo != null && callerInfo.contactExists) {
                StringBuilder remoteInfo = new StringBuilder();
                remoteInfo.append(callerInfo.name);
                remoteInfo.append(" <");
                remoteInfo.append(SipUri.getCanonicalSipContact(remoteContact));
                remoteInfo.append(">");
                formattedRemoteContact = remoteInfo.toString();
            }
        }
        return formattedRemoteContact;
    }

    private String formatNotificationTitle(int title, long accId) {
        StringBuilder notifTitle = new StringBuilder(context.getResources().getString(R.string.app_name));
        SipProfile acc = SipProfile.getProfileFromDbId(context, accId,
                new String[]{SipProfile.FIELD_DISPLAY_NAME});
       /* StringBuilder notifTitle = new StringBuilder(context.getText(title));
        SipProfile acc = SipProfile.getProfileFromDbId(context, accId,
                new String[] {SipProfile.FIELD_DISPLAY_NAME});
        if ((acc != null) && !TextUtils.isEmpty(acc.display_name)) {
            notifTitle.append(" - ");
            notifTitle.append(acc.display_name);
        }*/

        return notifTitle.toString();
    }

    // Calls
    public void showNotificationForCall(SipCallSession callInfo) {
        // This is the pending call notification
        // int icon = R.drawable.ic_incall_ongoing;
        @SuppressWarnings("deprecation")
        int icon = android.R.drawable.stat_sys_phone_call;
        CharSequence tickerText = context.getText(R.string.ongoing_call);
        long when = System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (inCallNotification == null) {
                inCallNotification = new Builder(context);
                inCallNotification.setSmallIcon(icon);
                inCallNotification.setTicker(tickerText);
                inCallNotification.setWhen(when);
                inCallNotification.setChannelId(CHANNEL_ID);
                inCallNotification.setOngoing(true);
            }
        } else {
            if (inCallNotification == null) {
                inCallNotification = new Builder(context);
                inCallNotification.setSmallIcon(icon);
                inCallNotification.setTicker(tickerText);
                inCallNotification.setWhen(when);
                inCallNotification.setOngoing(true);
            }
        }

        Intent notificationIntent = SipService.buildCallUiIntent(context, callInfo);
        notificationIntent.putExtra("from_notification",callInfo);
        /*Intent notificationIntent = new Intent(context,Home.class);*/
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        inCallNotification.setContentTitle(formatNotificationTitle(R.string.ongoing_call, callInfo.getAccId()));
        String data = context.getResources().getString(R.string.ongoing_call);
        //inCallNotification.setContentText(formatRemoteContactString(data, callInfo.getAccId()));
        Log.e("Call in progress acc id: ",  ""+ callInfo.getAccId());
        inCallNotification.setContentText(formatRemoteContactString(data, ""));
        inCallNotification.setContentIntent(contentIntent);

        Notification notification = inCallNotification.build();

        notification.flags |= Notification.FLAG_NO_CLEAR;
        notificationManager.notify(CALL_NOTIF_ID, notification);
    }


    public void showNotificationForMessage(SipMessage msg) {
        if (!CustomDistribution.supportMessaging()) {
            return;
        }
        // CharSequence tickerText = context.getText(R.string.instance_message);
        if (!msg.getFrom().equalsIgnoreCase(viewingRemoteFrom)) {
            String from = formatRemoteContactString(msg.getFullFrom());
            if (from.equalsIgnoreCase(msg.getFullFrom()) && !from.equals(msg.getDisplayName())) {
                from = msg.getDisplayName() + " " + from;
            }
            CharSequence tickerText = buildTickerMessage(context, from, msg.getBody());

            if (messageNotification == null) {
                messageNotification = new Builder(context);
                messageNotification.setSmallIcon(SipUri.isPhoneNumber(from) ? R.drawable.ic_chat_bubbles : android.R.drawable.stat_notify_chat);
                messageNotification.setTicker(tickerText);
                messageNotification.setWhen(System.currentTimeMillis());
                messageNotification.setDefaults(Notification.DEFAULT_ALL);
                messageNotification.setAutoCancel(true);
                messageNotification.setOnlyAlertOnce(true);
            }

            Intent notificationIntent = new Intent(SipManager.ACTION_SIP_MESSAGES);
            notificationIntent.setClass(context, HomeActivity.class); //siphome.java kunjan
            notificationIntent.putExtra(AppConstants.EXTRA_FROM_NOTIFICATION, true);
			/*notificationIntent.putExtra(SipMessage.FIELD_FROM, msg.getFrom());
			notificationIntent.putExtra(SipMessage.FIELD_BODY, msg.getBody());*/
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            messageNotification.setContentTitle(from);
            messageNotification.setContentText(msg.getBody());
            messageNotification.setContentIntent(contentIntent);

            notificationManager.notify(MESSAGE_NOTIF_ID, messageNotification.build());
        }
    }

    public void showNotificationForVoiceMail(SipProfile acc, int numberOfMessages) {
        if (messageVoicemail == null) {

            messageVoicemail = new Builder(context);
            messageVoicemail.setSmallIcon(android.R.drawable.stat_notify_voicemail);
            messageVoicemail.setTicker(context.getString(R.string.voice_mail));
            messageVoicemail.setWhen(System.currentTimeMillis());
            messageVoicemail.setDefaults(Notification.DEFAULT_ALL);
            messageVoicemail.setAutoCancel(true);
            messageVoicemail.setOnlyAlertOnce(true);
        }

        PendingIntent contentIntent = null;
        Intent notificationIntent;
        if (acc != null && !TextUtils.isEmpty(acc.vm_nbr) && acc.vm_nbr != "null") {
            notificationIntent = new Intent(Intent.ACTION_CALL);
            notificationIntent.setData(SipUri.forgeSipUri(SipManager.PROTOCOL_CSIP, acc.vm_nbr
                    + "@" + acc.getDefaultDomain()));
            notificationIntent.putExtra(SipProfile.FIELD_ACC_ID, acc.id);
        } else {
            notificationIntent = new Intent(SipManager.ACTION_SIP_DIALER);
        }
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        String messageText = "";
        if (acc != null) {
            messageText += acc.getProfileName();
            if (numberOfMessages > 0) {
                messageText += " : ";
            }
        }
        if (numberOfMessages > 0) {
            messageText += Integer.toString(numberOfMessages);
        }

        messageVoicemail.setContentTitle(context.getString(R.string.voice_mail));
        messageVoicemail.setContentText(messageText);
        if (contentIntent != null) {
            messageVoicemail.setContentIntent(contentIntent);
            notificationManager.notify(VOICEMAIL_NOTIF_ID, messageVoicemail.build());
        }
    }

    private static String viewingRemoteFrom = null;

    public void setViewingMessageFrom(String remoteFrom) {
        viewingRemoteFrom = remoteFrom;
    }

    protected static CharSequence buildTickerMessage(Context context, String address, String body) {
        String displayAddress = address;

        StringBuilder buf = new StringBuilder(displayAddress == null ? "" : displayAddress.replace('\n', ' ').replace('\r', ' '));
        buf.append(':').append(' ');

        int offset = buf.length();

        if (!TextUtils.isEmpty(body)) {
            body = body.replace('\n', ' ').replace('\r', ' ');
            buf.append(body);
        }

        SpannableString spanText = new SpannableString(buf.toString());
        spanText.setSpan(new StyleSpan(Typeface.BOLD), 0, offset, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spanText;
    }

    // Cancels
    public final void cancelRegisters() {
        if (!isServiceWrapper) {
            Log.e(THIS_FILE, "Trying to cancel a service notification from outside the service");
            return;
        }
        stopForegroundCompat(REGISTER_NOTIF_ID);
    }

    public final void cancelCalls() {
        notificationManager.cancel(CALL_NOTIF_ID);
    }

    public final void cancelMissedCalls() {
        notificationManager.cancel(CALLLOG_NOTIF_ID);
    }

    public final void cancelMessages() {
        notificationManager.cancel(MESSAGE_NOTIF_ID);
    }

    public final void cancelVoicemails() {
        notificationManager.cancel(VOICEMAIL_NOTIF_ID);
    }

    public final void cancelAll() {
        // Do not cancel calls notification since it's possible that there is
        // still an ongoing call.
        if (isServiceWrapper) {
            cancelRegisters();
        }
        cancelMessages();
        cancelMissedCalls();
        cancelVoicemails();
    }

    //Added missed call count for resolving issue no: TC_011
    public void showNotificationForMissedCall(ContentValues callLog) {
        wakeUpLock();

        GlobalClass gc = GlobalClass.getInstance();

        if (gc.getMissedCallCount(context) != null) {
            if (gc.getMissedCallCount(context).equals("0")) {
                count = 0;
                count++;
            } else {
                int count1 = Integer.parseInt(gc.getMissedCallCount(context));
                count = count1 + 1;
            }
        } else {
            count++;
        }
        gc.setMissedCallCount(context, String.valueOf(count));

        int icon = R.mipmap.ic_launcher_round;
        CharSequence tickerText = context.getText(R.string.missed_call);
        long when = System.currentTimeMillis();
        Uri uri = Uri.parse(User_settings.getNotifySoundSelector(context));
        Log.e("=========notify_sound-7", uri.toString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (missedCallNotification == null) {
                missedCallNotification = new NotificationCompat.Builder(context, "missed-call")
                    .setSmallIcon(icon)
                    .setTicker(tickerText)
                    .setWhen(when)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setChannelId(CHANNEL_ID)
                    .setSound(uri)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_ALL);
            }
        } else {
            if (missedCallNotification == null) {
                missedCallNotification = new Builder(context);
                missedCallNotification.setSmallIcon(icon);
                missedCallNotification.setTicker(tickerText);
                missedCallNotification.setWhen(when);
                missedCallNotification.setOnlyAlertOnce(true);
                missedCallNotification.setAutoCancel(true);
                missedCallNotification.setSound(uri);
                missedCallNotification.setDefaults(Notification.DEFAULT_ALL);
            }
        }

        Intent notificationIntent = new Intent(context, HomeActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.putExtra(AppConstants.EXTRA_FROM_NOTIFICATION, true);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        String remoteContact = callLog.getAsString(CallLog.Calls.NUMBER);//"Anushek" <sip:YDZ810@89.149.51.172>
        String eccId = remoteContact.substring(remoteContact.indexOf(":")+1, remoteContact.indexOf("@"));//YDZ810
        long accId = callLog.getAsLong(SipManager.CALLLOG_PROFILE_ID_FIELD);

        missedCallNotification.setContentTitle(formatNotificationTitle(R.string.missed_call, accId));
        String data;
        String last_missed_call = gc.getLastMissedCall(context);

        if (remoteContact.contains("<")) {
            String[] split = remoteContact.split("<");
            remoteContact = split[0];
        }
        Log.e("=======missed call:", ""+ remoteContact + "//" + last_missed_call);

        if (count == 0) {
            data = context.getResources().getString(R.string.missed_call);
        } else if (last_missed_call.equals(remoteContact) || last_missed_call.equals("")) {
            data = count + " " + context.getResources().getString(R.string.missed_call);
        } else {
            data = count + " " + context.getResources().getString(R.string.missed_call);
        }

        gc.setLastMissedCall(context, remoteContact);

        missedCallNotification.setContentText(formatRemoteContactString(data, remoteContact));
        missedCallNotification.setContentIntent(contentIntent);

        notificationManager.notify(CALLLOG_NOTIF_ID, missedCallNotification.build());

        //show badge with count in app icon
        NotificationUtils.showBadge(context, missedCallNotification.build(), count);

        //show missed call in chat page
        showMissedCall(eccId, remoteContact);

        //show badge with unread count in contacts/chat fragment
        showMessageCountBadge();
    }

    private void showMessageCountBadge() {
        DbHelper db = new DbHelper(context);
        int messageCount = db.getTotalUnreadMessages();
        NotificationUtils.showBadge(context, messageCount);
    }

    private void showMissedCall(String ecc_id, String remoteUser) {
        DbHelper db = new DbHelper(context);
        int index = 0;
        ArrayList<ContactEntity> contactEntities = getSortedContactList(db.getContactList());
        for(int i=0; i<contactEntities.size(); i++) {
            ContactEntity contact_i = contactEntities.get(i);
            String eccId = contact_i.getEccId();
            if(ecc_id.equals(eccId)) {
                index = i;
            }
        }

        ContactEntity contact_item = contactEntities.get(index);

        ChatListEntity chatListEntity;
        if ((!db.checkUserHaveChatList(contact_item.getEccId()))) {
            ChatListEntity chatEntity = new ChatListEntity();
            chatEntity.setUserDbId(contact_item.getUserDbId());
            chatEntity.setEccId(contact_item.getEccId());
            chatEntity.setName(contact_item.getName());
            chatEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
            chatEntity.setBurnTime(42);
            chatEntity.setChatType(AppConstants.SINGLE_CHAT_TYPE);
            int id = (int) db.insertChatList(chatEntity);
            chatEntity.setId(id);
            chatListEntity = chatEntity;
            Log.e("=========index-2", "id:"+id);
        } else {
            chatListEntity = db.getChatEntity(contact_item.getEccId());
        }

//        messageList.get(i).getSenderId() != Integer.valueOf(User_settings.getUserId(mContext))

        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setSenderId(Integer.parseInt(User_settings.getUserId(context)));
        chatMessageEntity.setMessage("Missed voice call");//remoteUser="A-ea4"
        chatMessageEntity.setMessageMimeType(AppConstants.MIME_TYPE_MISSED);
        chatMessageEntity.setMessageId(String.valueOf(System.currentTimeMillis()));
        chatMessageEntity.setChatId(chatListEntity.getId());
        chatMessageEntity.setReceiverId(chatListEntity.getUserDbId());
        chatMessageEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
        chatMessageEntity.setMessageBurnTime(42);
        chatMessageEntity.setChatUserDbId(chatMessageEntity.getChatUserDbId());
        chatMessageEntity.setEddId(chatListEntity.getEccId());

        if (NetworkUtils.isNetworkConnected(context)) {
            if (db.checkPublicKeysOfUser(chatListEntity.getUserDbId())) {
                chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_UNREAD_STATUS);
                chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(context, chatListEntity.getBurnTime()));
                db.insertChatMessage(chatMessageEntity);
            }
        }

    }

    private ArrayList<ContactEntity> getSortedContactList(ArrayList<ContactEntity> actualContactList) {

        ArrayList<ContactEntity> tempContactList = new ArrayList<>();
        ArrayList<ContactEntity> requestContactList = new ArrayList<>();
        ArrayList<ContactEntity> pendingContactList = new ArrayList<>();
        ArrayList<ContactEntity> acceptedContactList = new ArrayList<>();
        if (actualContactList.size() > 0) {
            for (int i = 0; i < actualContactList.size(); i++) {
                if (actualContactList.get(i).getBlockStatus().equalsIgnoreCase(String.valueOf(SocketUtils.request))) {
                    requestContactList.add(actualContactList.get(i));
                }

                if (actualContactList.get(i).getBlockStatus().equalsIgnoreCase(String.valueOf(SocketUtils.accepted))) {
                    acceptedContactList.add(actualContactList.get(i));
                }

                if (actualContactList.get(i).getBlockStatus().equalsIgnoreCase(String.valueOf(SocketUtils.pending))) {
                    pendingContactList.add(actualContactList.get(i));
                }
            }
            tempContactList.addAll(requestContactList);
            tempContactList.addAll(acceptedContactList);
            tempContactList.addAll(pendingContactList);

            return tempContactList;
        }

        return actualContactList;
    }
}
