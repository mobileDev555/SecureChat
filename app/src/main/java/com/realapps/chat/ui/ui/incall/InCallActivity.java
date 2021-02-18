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


package com.realapps.chat.ui.ui.incall;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.ui.api.GlobalClass;
import com.realapps.chat.ui.api.ISipService;
import com.realapps.chat.ui.api.MediaState;
import com.realapps.chat.ui.api.SipCallSession;
import com.realapps.chat.ui.api.SipCallSession.StatusCode;
import com.realapps.chat.ui.api.SipConfigManager;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.api.SipProfile;
import com.realapps.chat.ui.helper.PrefManager;
import com.realapps.chat.ui.service.SipService;
import com.realapps.chat.ui.ui.incall.CallProximityManager.ProximityDirector;
import com.realapps.chat.ui.ui.incall.DtmfDialogFragment.OnDtmfListener;
import com.realapps.chat.ui.ui.incall.locker.IOnLeftRightChoice;
import com.realapps.chat.ui.ui.incall.locker.ScreenLocker;
import com.realapps.chat.ui.ui.view.java.AddContactForConference;
import com.realapps.chat.ui.utils.DialingFeedback;
import com.realapps.chat.ui.utils.Log;
import com.realapps.chat.ui.utils.PreferencesProviderWrapper;
import com.realapps.chat.ui.utils.keyguard.KeyguardWrapper;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.view.home.activity.ChatWindowActivity;
import com.realapps.chat.view.home.activity.HomeActivity;

import org.webrtc.videoengine.ViERenderer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class InCallActivity extends FragmentActivity implements IOnCallActionTrigger,
        IOnLeftRightChoice, ProximityDirector, OnDtmfListener, View.OnClickListener {

    private static final int QUIT_DELAY = 3000;
    private final static String THIS_FILE = "=====InCallActivity";

    private Object callMutex = new Object();
    String getAddedContacts;
    String allConferenceNum;
    private SipCallSession[] callsInfo = null;
    private MediaState lastMediaState;
    ArrayList<String> conference_data = new ArrayList<String>();
    ArrayList<String> contact_data = new ArrayList<String>();
    String isComingFrom;
    boolean isRunning = false;
    String removedContact;
    String isCallHangup;
    ArrayList<String> runningCallArray = new ArrayList<>();

    private ViewGroup mainFrame;
    private LinearLayout inCallControls;
    IOnCallActionTrigger onTriggerListener;
    private SipCallSession currentCall;
    InCallCheckableImageView speakerButton, muteButton, addCallButton, mediaSettingsButton;
    LinearLayout llSpeaker, llMute, llAddCall, llSettings;
    private boolean supportMultipleCalls = false;

    // Screen wake lock for incoming call
    private WakeLock wakeLock;
    // Screen wake lock for video
    private WakeLock videoWakeLock;

    private InCallInfoGrid activeCallsGrid;
    private Timer quitTimer;
    private ArrayList<Integer> caller_id;


    private DialingFeedback dialFeedback;
    private PowerManager powerManager;
    private PreferencesProviderWrapper prefsWrapper;
    PrefManager pref;

    private SurfaceView cameraPreview;
    private CallProximityManager proximityManager;
    private KeyguardWrapper keyguardManager;

    private boolean useAutoDetectSpeaker = false;
    private CallsAdapter activeCallsAdapter;
    private InCallInfoGrid heldCallsGrid;
    private CallsAdapter heldCallsAdapter;

    private final static int PICKUP_SIP_URI_XFER = 0;
    private final static int PICKUP_SIP_URI_NEW_CALL = 1;
    private static final String CALL_ID = "call_id";
    String supportMultiple;

    static InCallActivity instance;

    public static InCallActivity getInsance() {
        if (instance == null) {
            instance = new InCallActivity();
        }
        return instance;
    }

    @SuppressLint("InvalidWakeLockTag")
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Fabric.with(this, new Crashlytics());
        User_settings.setLastActivity(this, InCallActivity.class.getSimpleName());

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.in_call_main);

        SipCallSession initialSession = getIntent().getParcelableExtra(SipManager.EXTRA_CALL_INFO);
        caller_id = new ArrayList<>();
        synchronized (callMutex) {
            callsInfo = new SipCallSession[1];
            callsInfo[0] = initialSession;
        }


        bindService(new Intent(this, SipService.class), connection, Context.BIND_AUTO_CREATE);
        prefsWrapper = new PreferencesProviderWrapper(this);
        pref = new PrefManager(this);

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
                "com.csip.onIncomingCall");
        wakeLock.setReferenceCounted(false);

        takeKeyEvents(true);

        // Cache findViews
        mainFrame = (ViewGroup) findViewById(R.id.mainFrame);
        //inCallControls = (InCallControls) findViewById(R.id.inCallControls);
        inCallControls = (LinearLayout) findViewById(R.id.inCallControls);

        //inCallAnswerControls = (InCallAnswerControls) findViewById(R.id.inCallAnswerControls);
        activeCallsGrid = (InCallInfoGrid) findViewById(R.id.activeCallsGrid);
        heldCallsGrid = (InCallInfoGrid) findViewById(R.id.heldCallsGrid);

        // Bind
        attachVideoPreview();

        initCallControls();
        setOnTriggerListener(this);
        supportMultipleCalls = SipConfigManager.getPreferenceBooleanValue(this, SipConfigManager.SUPPORT_MULTIPLE_CALLS, false);

        //inCallAnswerControls.setOnTriggerListener(this);

        if (activeCallsAdapter == null) {
            activeCallsAdapter = new CallsAdapter(true);
        }
        activeCallsGrid.setAdapter(activeCallsAdapter);


        if (heldCallsAdapter == null) {
            heldCallsAdapter = new CallsAdapter(false);
        }
        heldCallsGrid.setAdapter(heldCallsAdapter);


        ScreenLocker lockOverlay = (ScreenLocker) findViewById(R.id.lockerOverlay);
        lockOverlay.setActivity(this);
        lockOverlay.setOnLeftRightListener(this);


        // Listen to media & sip events to update the UI
        registerReceiver(callStateReceiver, new IntentFilter(SipManager.ACTION_SIP_CALL_CHANGED));
        registerReceiver(callStateReceiver, new IntentFilter(SipManager.ACTION_SIP_MEDIA_CHANGED));
        registerReceiver(callStateReceiver, new IntentFilter(SipManager.ACTION_ZRTP_SHOW_SAS));

        proximityManager = new CallProximityManager(this, this, lockOverlay);
        keyguardManager = KeyguardWrapper.getKeyguardManager(this);

        dialFeedback = new DialingFeedback(this, true);

        if (prefsWrapper.getPreferenceBooleanValue(SipConfigManager.PREVENT_SCREEN_ROTATION)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (quitTimer == null) {
            quitTimer = new Timer("Quit-timer");
        }


        useAutoDetectSpeaker = prefsWrapper.getPreferenceBooleanValue(SipConfigManager.AUTO_DETECT_SPEAKER);

        //applyTheme();
        proximityManager.startTracking();

        setCallState(initialSession);

        /*Intent intent1 = new Intent();
        intent1.setAction(SipManager.ACTION_ADD_CONTACT);
        getApplicationContext().sendBroadcast(intent1);*/
    }


    public void initCallControls() {
        speakerButton = (InCallCheckableImageView) inCallControls.findViewById(R.id.speakerButton);
        muteButton = (InCallCheckableImageView) inCallControls.findViewById(R.id.muteButton);
        addCallButton = (InCallCheckableImageView) inCallControls.findViewById(R.id.addCallButton);
        mediaSettingsButton = (InCallCheckableImageView) inCallControls.findViewById(R.id.mediaSettingsButton);

        llSpeaker = (LinearLayout) inCallControls.findViewById(R.id.llSpeaker);
        llMute = (LinearLayout) inCallControls.findViewById(R.id.llMute);
        llAddCall = (LinearLayout) inCallControls.findViewById(R.id.llAddCall);
        llSettings = (LinearLayout) inCallControls.findViewById(R.id.llSettings);


        speakerButton.setChecked(false);
        muteButton.setChecked(false);

        llSpeaker.setOnClickListener(this);
        llMute.setOnClickListener(this);
        llAddCall.setOnClickListener(this);
        llSettings.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llSpeaker:
                speakerButton.toggle();
                if (speakerButton.isChecked()) {
                    dispatchTriggerEvent(IOnCallActionTrigger.SPEAKER_ON);
                    llSpeaker.setBackgroundColor(getResources().getColor(R.color.dial_edittext_bg));
                } else {
                    dispatchTriggerEvent(IOnCallActionTrigger.SPEAKER_OFF);
                    llSpeaker.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
                break;
            case R.id.llMute:
                muteButton.toggle();
                if (muteButton.isChecked()) {
                    dispatchTriggerEvent(IOnCallActionTrigger.MUTE_ON);
                    llMute.setBackgroundColor(getResources().getColor(R.color.dial_edittext_bg));
                } else {
                    dispatchTriggerEvent(IOnCallActionTrigger.MUTE_OFF);
                    llMute.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }

                break;
            case R.id.llAddCall:

                dispatchTriggerEvent(IOnCallActionTrigger.ADD_CALL);
                break;
            case R.id.llSettings:
                dispatchTriggerEvent(IOnCallActionTrigger.MEDIA_SETTINGS);
                break;
            default:
                break;
        }
    }

    public void setCallState(SipCallSession callInfo) {
        currentCall = callInfo;

        if (currentCall == null) {
            inCallControls.setVisibility(View.GONE);
            return;
        }

        int state = currentCall.getCallState();
        switch (state) {
            case SipCallSession.InvState.INCOMING:
                inCallControls.setVisibility(View.GONE);
                break;
            case SipCallSession.InvState.CALLING:
            case SipCallSession.InvState.CONNECTING:
                inCallControls.setVisibility(View.GONE);
                break;
            case SipCallSession.InvState.CONFIRMED:
                inCallControls.setVisibility(View.GONE);
                break;
            case SipCallSession.InvState.NULL:
            case SipCallSession.InvState.DISCONNECTED:
                inCallControls.setVisibility(View.GONE);
                break;
            case SipCallSession.InvState.EARLY:
            default:
                if (currentCall.isIncoming()) {
                    inCallControls.setVisibility(View.GONE);
                } else {
                    inCallControls.setVisibility(View.GONE);
                }
                break;
        }

    }


    /**
     * Registers a callback to be invoked when the user triggers an event.
     *
     * @param listener the OnTriggerListener to attach to this view
     */
    public void setOnTriggerListener(IOnCallActionTrigger listener) {
        onTriggerListener = listener;
    }

    private void dispatchTriggerEvent(int whichHandle) {
        if (onTriggerListener != null) {
            onTriggerListener.onTrigger(whichHandle, currentCall);
        }
    }

    private boolean callOngoing = false;


    public void setMediaState(MediaState mediaState) {
        lastMediaState = mediaState;

        // Update menu
        // BT
        boolean enabled, checked;
        if (lastMediaState == null) {
            enabled = callOngoing;
            checked = false;
        } else {
            enabled = callOngoing && lastMediaState.canBluetoothSco;
            checked = lastMediaState.isBluetoothScoOn;
        }
        /*btnMenuBuilder.findItem(R.id.bluetoothButton).setVisible(enabled).setChecked(checked);*/

        // Mic
        if (lastMediaState == null) {
            enabled = callOngoing;
            checked = false;
        } else {
            enabled = callOngoing && lastMediaState.canMicrophoneMute;
            checked = lastMediaState.isMicrophoneMute;
        }
        muteButton.setVisibility(View.VISIBLE);//.setChecked(checked);


        // Speaker
        Log.e(THIS_FILE, ">> Speaker1 " + lastMediaState);
        if (lastMediaState == null) {
            enabled = callOngoing;
            checked = false;
        } else {
            Log.e(THIS_FILE, ">> Speaker2 " + lastMediaState.isSpeakerphoneOn);
            enabled = callOngoing && lastMediaState.canSpeakerphoneOn;
            checked = lastMediaState.isSpeakerphoneOn;
        }
        speakerButton.setVisibility(View.VISIBLE);//.setChecked(checked);

        // Add call
        //Himadri
        //if (supportMultipleCalls && callOngoing)
        addCallButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
    @Override
    protected void onStart() {
        super.onStart();
        isRunning = true;
        keyguardManager.unlock();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Tag", "onResume: " + "resume-InCallActivity");
        /*
        endCallTargetRect = null;
        holdTargetRect = null;
        answerTargetRect = null;
        xferTargetRect = null;
        */
        dialFeedback.resume();
        runOnUiThread(new UpdateUIFromCallRunnable());

        //========= while call, if user click the home button, showing the FloatingActionButton
        SipConfigManager.setPreferenceIntegerValue(this, SipConfigManager.CALLING, 1);
    }
    @Override
    protected void onPause() {
        super.onPause();

        dialFeedback.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        isRunning = false;
        keyguardManager.lock();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SipConfigManager.setPreferenceIntegerValue(this, SipConfigManager.CALLING, 0);

        if (infoDialog != null) {
            infoDialog.dismiss();
        }

        if (quitTimer != null) {
            quitTimer.cancel();
            quitTimer.purge();
            quitTimer = null;
        }
        /*
        if (draggingTimer != null) {
            draggingTimer.cancel();
            draggingTimer.purge();
            draggingTimer = null;
        }
        */

        try {
            unbindService(connection);
        } catch (Exception e) {
            // Just ignore that
        }
        service = null;
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        proximityManager.stopTracking();
        proximityManager.release(0);
        try {
            unregisterReceiver(callStateReceiver);
        } catch (IllegalArgumentException e) {
            // That's the case if not registered (early quit)
        }

        if (activeCallsGrid != null) {
            activeCallsGrid.terminate();
        }

        detachVideoPreview();
        //handler.setActivityInstance(null);
    }

    @SuppressLint("InvalidWakeLockTag")
    @SuppressWarnings("deprecation")
    private void attachVideoPreview() {
        // Video stuff
        if (prefsWrapper.getPreferenceBooleanValue(SipConfigManager.USE_VIDEO)) {
            if (cameraPreview == null) {
                Log.e(THIS_FILE, "Create Local Renderer");
                cameraPreview = ViERenderer.CreateLocalRenderer(this);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(256, 256);
                //lp.leftMargin = 2;
                //lp.topMargin= 4;
                lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                cameraPreview.setVisibility(View.GONE);
                mainFrame.addView(cameraPreview, lp);
            } else {
                Log.e(THIS_FILE, "NO NEED TO Create Local Renderer");
            }

            if (videoWakeLock == null) {
                videoWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "com.csip.videoCall");
                videoWakeLock.setReferenceCounted(false);
            }
        }

        if (videoWakeLock != null && videoWakeLock.isHeld()) {
            videoWakeLock.release();
        }
    }

    private void detachVideoPreview() {
        if (mainFrame != null && cameraPreview != null) {
            mainFrame.removeView(cameraPreview);
        }
        if (videoWakeLock != null && videoWakeLock.isHeld()) {
            videoWakeLock.release();
        }
        if (cameraPreview != null) {
            cameraPreview = null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        // TODO : update UI
        Log.e(THIS_FILE, "New intent is launched");
        super.onNewIntent(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(THIS_FILE, "Configuration changed");
        if (cameraPreview != null && cameraPreview.getVisibility() == View.VISIBLE) {

            cameraPreview.setVisibility(View.GONE);
        }
        runOnUiThread(new UpdateUIFromCallRunnable());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        switch (requestCode) {
            case PICKUP_SIP_URI_XFER:
                if (resultCode == RESULT_OK && service != null) {

                    String callee = data.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    int callId = data.getIntExtra(CALL_ID, -1);
                    if (callId != -1) {
                        try {
                            service.xfer((int) callId, callee);
                        } catch (RemoteException e) {
                            // TODO : toaster
                        }
                    }
                }
                return;
            case PICKUP_SIP_URI_NEW_CALL:
                if (resultCode == RESULT_OK && service != null) {
                    String callee = data.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    getAddedContacts = data.getStringExtra("newAddedContact");
                    long accountId = data.getLongExtra(SipProfile.FIELD_ID,
                            SipProfile.INVALID_ID);

                    System.out.println("IncallActivity : PICKUP_SIP_URI_NEW_CALL : " + getAddedContacts);
                    String newNumber;

                    if (callee != null) {
                        if (callee.contains(",")) {
                            String[] jjCallee = callee.split(",");
                            for (int i = 0; i < jjCallee.length; i++) {
                                newNumber = jjCallee[i];
                                if (accountId != SipProfile.INVALID_ID) {
                                    try {
                                        service.makeCall(newNumber, (int) accountId);
                                    } catch (RemoteException e) {
                                        // TODO : toaster
                                    }
                                }
                            }
                        } else {
                            newNumber = callee;
                            if (accountId != SipProfile.INVALID_ID) {
                                try {
                                    service.makeCall(newNumber, (int) accountId);
                                } catch (RemoteException e) {
                                    // TODO : toaster
                                }
                            }
                        }
                    }
                }
                return;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private SipCallSession getActiveCallInfo() {
        SipCallSession currentCallInfo = null;
        if (callsInfo == null) {
            return null;
        }
        for (SipCallSession callInfo : callsInfo) {
            currentCallInfo = getPrioritaryCall(callInfo, currentCallInfo);
        }
        return currentCallInfo;
    }

    /**
     * Get the call with the higher priority comparing two calls
     *
     * @param call1 First call object to compare
     * @param call2 Second call object to compare
     * @return The call object with highest priority
     */
    private SipCallSession getPrioritaryCall(SipCallSession call1, SipCallSession call2) {
        // We prefer the not null
        if (call1 == null) {
            return call2;
        } else if (call2 == null) {
            return call1;
        }
        // We prefer the one not terminated
        if (call1.isAfterEnded()) {
            return call2;
        } else if (call2.isAfterEnded()) {
            return call1;
        }
        // We prefer the one not held
        if (call1.isLocalHeld()) {
            return call2;
        } else if (call2.isLocalHeld()) {
            return call1;
        }

        // We prefer the older call
        // to keep consistancy on what will be replied if new call arrives
        return (call1.getCallStart() > call2.getCallStart()) ? call2 : call1;
    }


    /**
     * Update the user interface from calls state.
     */
    private class UpdateUIFromCallRunnable implements Runnable {

        @Override
        public void run() {
            // Current call is the call emphasis by the UI.
            SipCallSession mainCallInfo = null;

            int mainsCalls = 0;
            int heldsCalls = 0;

            synchronized (callMutex) {
                if (callsInfo != null) {
                    for (SipCallSession callInfo : callsInfo) {
                        /*System.out.println(THIS_FILE +
                                "We have a call " + callInfo.getCallId() + " / " + callInfo.getCallState()
                                + "/" + callInfo.getMediaStatus());*/
                        caller_id.add(callInfo.getCallId());
                        PrefManager pref = new PrefManager(getApplicationContext());
                        pref.setCallerIdArray(caller_id);

                        Intent intent1 = new Intent();
                        intent1.setAction(SipManager.ACTION_FOR_CONFERENCE_CALLERID);
                        intent1.putIntegerArrayListExtra("caller_id_array", caller_id);
                        sendBroadcast(intent1);

                        if (!callInfo.isAfterEnded()) {
                            if (callInfo.isLocalHeld()) {
                                heldsCalls++;
                            } else {
                                mainsCalls++;
                            }
                        }
                        mainCallInfo = getPrioritaryCall(callInfo, mainCallInfo);
                    }
                }
            }


            // Update call control visibility - must be done before call cards
            // because badge avail size depends on that
            if ((mainsCalls + heldsCalls) >= 1) {

                // Update in call actions
                setCallState(mainCallInfo);
            } else {
                setCallState(null);
            }


            // heldCallsGrid.setVisibility((heldsCalls > 0) ? View.GONE : View.GONE);
            //heldCallsGrid.setVisibility((heldsCalls > 0)? View.VISIBLE : View.GONE);

            //Himadri : Conference call
            //Check support mulitple call flag and based on that handle ui

            supportMultiple = getIntent().getStringExtra(SipManager.EXTRA_CONFERENCE_CALL_SETUP);

            if (supportMultiple != null && supportMultiple.length() != 0) {
                if (supportMultiple.equals("false")) {
                    activeCallsAdapter.notifyDataSetChanged();
                    //heldCallsAdapter.notifyDataSetChanged();
                } else {

                    InCallCard vc = new InCallCard(InCallActivity.this, null);
                    vc.setCallState(mainCallInfo, true);
                    // vc.setOnTriggerListener(ManageConferenceCallAdapter.this);
                }
            }

            //findViewById(R.id.inCallContainer).requestLayout();

            if (mainCallInfo != null) {

                int state = mainCallInfo.getCallState();

                //int backgroundResId = R.drawable.bg_in_call_gradient_unidentified;

                // We manage wake lock
                switch (state) {
                    case SipCallSession.InvState.INCOMING:
                    case SipCallSession.InvState.EARLY:
                    case SipCallSession.InvState.CALLING:
                    case SipCallSession.InvState.CONNECTING:

                        //System.out.println(THIS_FILE + "Acquire wake up lock");
                        if (wakeLock != null && !wakeLock.isHeld()) {
                            wakeLock.acquire();
                        }
                        break;
                    case SipCallSession.InvState.CONFIRMED:
                        break;
                    case SipCallSession.InvState.NULL:
                    case SipCallSession.InvState.DISCONNECTED:
                        //System.out.println(THIS_FILE + "Active call session is disconnected or null wait for quit...");
                        // This will release locks
                        onDisplayVideo(false);
                        delayedQuit();
                        return;

                }
            }

            proximityManager.updateProximitySensorMode();

            if (heldsCalls + mainsCalls == 0) {
                delayedQuit();
            }
        }
    }

    @Override
    public void onDisplayVideo(boolean show) {
        runOnUiThread(new UpdateVideoPreviewRunnable(show));
    }

    /**
     * Update ui from media state.
     */
    private class UpdateUIFromMediaRunnable implements Runnable {
        @Override
        public void run() {
            setMediaState(lastMediaState);
            proximityManager.updateProximitySensorMode();
        }
    }

    private class UpdateVideoPreviewRunnable implements Runnable {
        private final boolean show;

        UpdateVideoPreviewRunnable(boolean show) {
            this.show = show;
        }

        @Override
        public void run() {
            // Update the camera preview visibility
            if (cameraPreview != null) {
                cameraPreview.setVisibility(show ? View.VISIBLE : View.GONE);
                if (show) {
                    if (videoWakeLock != null) {
                        videoWakeLock.acquire();
                    }
                    SipService.setVideoWindow(SipCallSession.INVALID_CALL_ID, cameraPreview, true);
                } else {
                    if (videoWakeLock != null && videoWakeLock.isHeld()) {
                        videoWakeLock.release();
                    }
                    SipService.setVideoWindow(SipCallSession.INVALID_CALL_ID, null, true);
                }
            } else {
                Log.w(THIS_FILE, "No camera preview available to be shown");
            }
        }
    }


    private synchronized void delayedQuit() {

        if (wakeLock != null && wakeLock.isHeld()) {
            Log.e(THIS_FILE, "Releasing wake up lock");
            wakeLock.release();
        }

        proximityManager.release(0);

        activeCallsGrid.setVisibility(View.VISIBLE);
        inCallControls.setVisibility(View.GONE);

        Log.e(THIS_FILE, "Start quit timer");
        if (quitTimer != null) {
            quitTimer.schedule(new QuitTimerTask(), QUIT_DELAY);
        } else {
            finish();
        }
    }

    private class QuitTimerTask extends TimerTask {
        @Override
        public void run() {
            finish();
        }
    }

    private void showDialpad(int callId) {
        DtmfDialogFragment newFragment = DtmfDialogFragment.newInstance(callId);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }


    @Override
    public void OnDtmf(int callId, int keyCode, int dialTone) {
        proximityManager.restartTimer();

        if (service != null) {
            if (callId != SipCallSession.INVALID_CALL_ID) {
                try {
                    service.sendDtmf(callId, keyCode);
                    dialFeedback.giveFeedback(dialTone);
                } catch (RemoteException e) {
                    Log.e(THIS_FILE, "Was not able to send dtmf tone", e);
                }
            }
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(THIS_FILE, "Key down : " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                //
                // Volume has been adjusted by the user.
                //
                int action = AudioManager.ADJUST_RAISE;
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    action = AudioManager.ADJUST_LOWER;
                }

                // Detect if ringing
                SipCallSession currentCallInfo = getActiveCallInfo();
                // If not any active call active
                if (currentCallInfo == null && serviceConnected) {
                    break;
                }

                if (service != null) {
                    try {
                        service.adjustVolume(currentCallInfo, action, AudioManager.FLAG_SHOW_UI);
                    } catch (RemoteException e) {
                        Log.e(THIS_FILE, "Can't adjust volume", e);
                    }
                }

                return true;
            case KeyEvent.KEYCODE_CALL:
            case KeyEvent.KEYCODE_ENDCALL:
                return true;
            case KeyEvent.KEYCODE_SEARCH:
                return true;
            //For blocking back button of mobile:: issue no: TC_012  && TC_09
            case KeyEvent.KEYCODE_BACK:
                return true;
            case KeyEvent.KEYCODE_HOME:
                Log.e(THIS_FILE, "keycode_home: " + "clicked");
                return true;
            default:
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.e(THIS_FILE, "Key up : " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_CALL:
            case KeyEvent.KEYCODE_SEARCH:
                return true;
            case KeyEvent.KEYCODE_ENDCALL:
                return true;//inCallAnswerControls.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_HOME:
                Log.e(THIS_FILE, "keycode_home:up " + "clicked");
                return true;

        }
        return super.onKeyUp(keyCode, event);
    }


    private BroadcastReceiver callStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Himadri : Conference call
            //Check support mulitple call flag and based on that handle ui

            supportMultiple = getIntent().getStringExtra(SipManager.EXTRA_CONFERENCE_CALL_SETUP);

            if (action.equals(SipManager.ACTION_SIP_CALL_CHANGED)) {
                if (service != null) {
                    try {
                        synchronized (callMutex) {
                            callsInfo = service.getCalls();

                            SipCallSession publicCallInfo = intent.getParcelableExtra(SipManager.EXTRA_CALL_INFO);
                            String remoteContact = publicCallInfo.getRemoteContact();
                            int call_state = publicCallInfo.getCallState();
                            GlobalClass gc = GlobalClass.getInstance();

                            System.out.println("OnLayout :: call_state : " + call_state);
                            if (call_state == 5) {

                                if (remoteContact.contains("@")) {
                                    String[] remortPart1 = remoteContact.split("@");
                                    String sipContact = remortPart1[0];
                                    if (sipContact.contains(":")) {
                                        String[] sipContact1 = sipContact.split(":");
                                        String newAddedContact = sipContact1[1];

                                        if (newAddedContact.contains("wifi%23")) {
                                            String[] contactWithouotWifi = newAddedContact.split("wifi%23");
                                            String contactWithouotWifi1 = contactWithouotWifi[1];

                                            conference_data.add(contactWithouotWifi1);
                                            pref.manageConferenceContact(context, conference_data);

                                        } else {
                                            conference_data.add(newAddedContact);
                                            pref.manageConferenceContact(context, conference_data);
                                        }

                                    }
                                }
                            }

                            if (call_state == 6) {
                                if (supportMultiple.equals("true")) {
                                    isCallHangup = "yes";

                                } else {
                                    isCallHangup = "no";
                                }

                                SipConfigManager.setPreferenceBooleanValue(context, SipConfigManager.SUPPORT_MULTIPLE_CALLS, false);
                                //supportMultiple = "false";
                                //System.out.println("OnLayout :: lastStatusCode : " + publicCallInfo.getLastStatusCode());
                                //System.out.println("OnLayout :: supportMultiple : " + supportMultiple);
                                if (remoteContact.contains("@")) {
                                    String[] remortPart1 = remoteContact.split("@");
                                    String sipContact = remortPart1[0];
                                    if (sipContact.contains(":")) {
                                        String[] sipContact1 = sipContact.split(":");
                                        String newAddedContact = sipContact1[1];

                                        if (newAddedContact.contains("wifi%23")) {
                                            String[] contactWithouotWifi = newAddedContact.split("wifi%23");
                                            String contactWithouotWifi1 = contactWithouotWifi[1];

                                            //removedContact = contactWithouotWifi1;
                                            //conference_data.remove(contactWithouotWifi1);


                                            if (supportMultiple.equals("true")) {
                                                int lastStatusCode = publicCallInfo.getLastStatusCode();

                                                if (lastStatusCode == 486) {

                                                    Toast.makeText(context, "This user is already available in conference", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    removedContact = contactWithouotWifi1;
                                                    conference_data.remove(contactWithouotWifi1);
                                                    pref.manageConferenceContact(context, conference_data);
                                                }
                                            } else {
                                                removedContact = contactWithouotWifi1;
                                                conference_data.remove(contactWithouotWifi1);
                                                pref.manageConferenceContact(context, conference_data);
                                            }


                                        } else {

                                            pref.manageConferenceContact(context, conference_data);
                                            // removedContact = newAddedContact;
                                            //pref.addContact(context.getApplicationContext(),conference_data);
                                            if (supportMultiple.equals("true")) {
                                                int lastStatusCode = publicCallInfo.getLastStatusCode();

                                                if (lastStatusCode == 486) {
                                                    Toast.makeText(context, "This user is already available in conference", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    removedContact = newAddedContact;
                                                    conference_data.remove(newAddedContact);
                                                    pref.manageConferenceContact(context, conference_data);
                                                }
                                            } else {
                                                removedContact = newAddedContact;
                                                conference_data.remove(newAddedContact);
                                                pref.manageConferenceContact(context, conference_data);
                                            }
                                        }


                                        String newData = getAddedContacts;//pref.getAddedContacts(context.getApplicationContext());


                                        if (newData != null) {
                                            Gson gson = new Gson();
                                            String[] text = gson.fromJson(newData, String[].class);
                                            for (int j = 0; j < text.length; j++) {
                                                String num = text[j];

                                                if (removedContact != null) {
                                                    //        System.out.println("OnLayout :: removedContact : " + removedContact);
                                                    if (num != removedContact) {
                                                        contact_data.add(removedContact);
                                                        //              System.out.println("OnLayout :: newData : " + removedContact);
                                                        pref.addContact(context.getApplicationContext(), contact_data);
                                                    }
                                                }
                                            }
                                        }
                                        //System.out.println("OnLayout :: addContactAtHangup : " + conference_data.size());
                                        pref.addContactAtHangup(context.getApplicationContext(), conference_data);
                                    }
                                }


                                boolean isAddParticpantActive = getSharedPreferences("OURINFO", MODE_PRIVATE).getBoolean("addParticipantActive", false);
                                boolean isManageConferenceActive = getSharedPreferences("OURINFO", MODE_PRIVATE).getBoolean("manageConferenceActive", false);

                                if (isAddParticpantActive) {
                                    Intent intent1 = new Intent();
                                    intent1.setAction(SipManager.ACTION_ADD_PARTICIPENT_STOP);
                                    //intent1.putExtra("removedContact","");
                                    sendBroadcast(intent1);
                                }

                                if (isManageConferenceActive) {
                                    Intent intent1 = new Intent();
                                    intent1.setAction(SipManager.ACTION_MANAGE_CONFERENCECALL_STOP);
                                    sendBroadcast(intent1);
                                }

                            }
                            runOnUiThread(new UpdateUIFromCallRunnable());
                            //Himadri : Conference call
                            //Check support mulitple call flag and based on that handle ui

                        }
                    } catch (RemoteException e) {
                        Log.e(THIS_FILE, "Not able to retrieve calls");
                    }
                }
            } else if (action.equals(SipManager.ACTION_SIP_MEDIA_CHANGED)) {
                if (service != null) {
                    MediaState mediaState;
                    try {
                        mediaState = service.getCurrentMediaState();
                        synchronized (callMutex) {
                            if (!mediaState.equals(lastMediaState)) {
                                lastMediaState = mediaState;
                                runOnUiThread(new UpdateUIFromMediaRunnable());
                            }
                        }
                    } catch (RemoteException e) {
                        Log.e(THIS_FILE, "Can't get the media state ", e);
                    }
                }
            } else if (action.equals(SipManager.ACTION_ZRTP_SHOW_SAS)) {
                SipCallSession callSession = intent.getParcelableExtra(SipManager.EXTRA_CALL_INFO);
                String sas = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                runOnUiThread(new ShowZRTPInfoRunnable(callSession, sas));
            }
        }
    };

    /**
     * Service binding
     */
    private boolean serviceConnected = false;
    private ISipService service;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ISipService.Stub.asInterface(arg1);
            try {
                // Log.d(THIS_FILE,
                // "Service started get real call info "+callInfo.getCallId());
                callsInfo = service.getCalls();
                serviceConnected = true;
                runOnUiThread(new UpdateUIFromCallRunnable());
                runOnUiThread(new UpdateUIFromMediaRunnable());
            } catch (RemoteException e) {
                Log.e(THIS_FILE, "Can't get back the call", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceConnected = false;
            callsInfo = null;
        }
    };
    private AlertDialog infoDialog;

    // private boolean showDetails = true;


    @Override
    public void onTrigger(int whichAction, final SipCallSession call) {

        Log.e("========inCall", ""+whichAction+"/"+TAKE_CALL);
        // Sanity check for actions requiring valid call id
        if (whichAction == TAKE_CALL || whichAction == REJECT_CALL || whichAction == DONT_TAKE_CALL ||
                whichAction == TERMINATE_CALL || whichAction == DETAILED_DISPLAY ||
                whichAction == TOGGLE_HOLD || whichAction == START_RECORDING ||
                whichAction == STOP_RECORDING || whichAction == DTMF_DISPLAY ||
                whichAction == XFER_CALL || whichAction == TRANSFER_CALL ||
                whichAction == START_VIDEO || whichAction == STOP_VIDEO) {
            // We check that current call is valid for any actions
            if (call == null) {
                Log.e(THIS_FILE, "Try to do an action on a null call !!!");
                return;
            }
            if (call.getCallId() == SipCallSession.INVALID_CALL_ID) {
                Log.e(THIS_FILE, "Try to do an action on an invalid call !!!");
                return;
            }
        }

        // Reset proximity sensor timer
        proximityManager.restartTimer();

        try {
            switch (whichAction) {
                case TAKE_CALL: {
                    if (service != null) {
                        Log.e("========accept call", "Answer call " + call.getCallId());

                        boolean shouldHoldOthers = false;

                        // Well actually we should be always before confirmed
                        if (call.isBeforeConfirmed()) {
                            shouldHoldOthers = true;
                        }

                        service.answer(call.getCallId(), SipCallSession.StatusCode.OK);

                        // if it's a ringing call, we assume that user wants to
                        // hold other calls
                        if (shouldHoldOthers && callsInfo != null) {
                            for (SipCallSession callInfo : callsInfo) {
                                // For each active and running call
                                if (SipCallSession.InvState.CONFIRMED == callInfo.getCallState()
                                        && !callInfo.isLocalHeld()
                                        && callInfo.getCallId() != call.getCallId()) {

                                    Log.e(THIS_FILE, "Hold call " + callInfo.getCallId());
                                    /*for(int i = 0; i < caller_id.size();i++){

                                    }*/
                                    service.hold(callInfo.getCallId());

                                }
                            }
                        }
                    }
                    break;
                }
                case DONT_TAKE_CALL: {
                    SipConfigManager.setPreferenceBooleanValue(this, SipConfigManager.SUPPORT_MULTIPLE_CALLS, false);
                    supportMultiple = "false";
                    if (service != null) {
                        useAutoDetectSpeaker = false;
                        //service.setSpeakerphoneOn((whichAction == SPEAKER_ON) ? true : false);
                        service.hangup(call.getCallId(), StatusCode.BUSY_HERE);
                    }
                    break;
                }
                case REJECT_CALL:
                case TERMINATE_CALL: {
                    Log.e("=========reject call-1", "ok");
                    SipConfigManager.setPreferenceBooleanValue(this, SipConfigManager.SUPPORT_MULTIPLE_CALLS, false);
                    supportMultiple = "false";
                    useAutoDetectSpeaker = false;
                    service.setSpeakerphoneOn((whichAction == SPEAKER_ON) ? true : false);

                    SipConfigManager.setPreferenceIntegerValue(this, SipConfigManager.CALLING, 0);

                    finish();

                    if (service != null) {
                        //caller_id.add(callInfo.getCallId());
                        for (int i = 0; i < caller_id.size(); i++) {
                            service.hangup(caller_id.get(i), 0);
                        }

                        caller_id.clear();

                        pref.setCallerIdArray(caller_id);
                        pref.setMute("off");
                        pref.setSpeaker("off");
                    }

                    /*SipConfigManager.setPreferenceBooleanValue(this, SipConfigManager.SUPPORT_MULTIPLE_CALLS, false);


                    if(supportMultiple.equals("true")){
                        supportMultiple = "false";
                        finish();
                        if (service != null) {

                            //caller_id.add(callInfo.getCallId());
                            for (int i = 0; i < caller_id.size(); i++) {
                                useAutoDetectSpeaker = false;
                                //service.setMicrophoneMute((whichAction == MUTE_ON) ? true : false);
                                service.setSpeakerphoneOn((whichAction == SPEAKER_ON) ? true : false);
                                service.hangup(caller_id.get(i), 0);

                            }
                            //service.hangup(call.getCallId(), 0);
                            caller_id.clear();

                            PrefManager pref = new PrefManager(getApplicationContext());
                            pref.setCallerIdArray(caller_id);

                            pref.setMute("off");
                            pref.setSpeaker("off");
                        }
                        pref.addContact(this, null);

                    }else{
                        supportMultiple = "false";
                        service.hangup(call.getCallId(), 0);
                    }*/


                    break;
                }
                case MUTE_ON:
                case MUTE_OFF: {
                    if (service != null) {
                        service.setMicrophoneMute((whichAction == MUTE_ON) ? true : false);
                    }
                    break;
                }
                case SPEAKER_ON:
                case SPEAKER_OFF: {
                    if (service != null) {
                        Log.e(THIS_FILE, "Manually switch to speaker");
                        useAutoDetectSpeaker = false;
                        service.setSpeakerphoneOn((whichAction == SPEAKER_ON) ? true : false);
                    }
                    break;
                }
                case BLUETOOTH_ON:
                case BLUETOOTH_OFF: {
                    if (service != null) {
                        service.setBluetoothOn((whichAction == BLUETOOTH_ON) ? true : false);
                    }
                    break;
                }
                case DTMF_DISPLAY: {
                    showDialpad(call.getCallId());
                    break;
                }
                case DETAILED_DISPLAY: {
                    if (service != null) {
                        if (infoDialog != null) {
                            infoDialog.dismiss();
                        }
                        String infos = service.showCallInfosDialog(call.getCallId());
                        String natType = service.getLocalNatType();
                        SpannableStringBuilder buf = new SpannableStringBuilder();
                        Builder builder = new Builder(this);

                        buf.append(infos);
                        if (!TextUtils.isEmpty(natType)) {
                            buf.append("\r\nLocal NAT type detected : ");
                            buf.append(natType);
                        }
                        TextAppearanceSpan textSmallSpan = new TextAppearanceSpan(this,
                                android.R.style.TextAppearance_Small);
                        buf.setSpan(textSmallSpan, 0, buf.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        infoDialog = builder.setIcon(android.R.drawable.ic_dialog_info)
                                .setMessage(buf)
                                .setNeutralButton(R.string.ok, null)
                                .create();
                        infoDialog.show();
                    }
                    break;
                }
                case TOGGLE_HOLD: {
                    boolean isHold = false;
                    /*supportMultipleCalls = SipConfigManager.getPreferenceBooleanValue(this, SipConfigManager.SUPPORT_MULTIPLE_CALLS, false);
                    if(supportMultipleCalls){

                        dispatchTriggerEvent(IOnCallActionTrigger.MUTE_ON);
                    }else{*/
                    if (service != null) {
                        if (call.getMediaStatus() == SipCallSession.MediaState.LOCAL_HOLD ||
                                call.getMediaStatus() == SipCallSession.MediaState.NONE) {
                            service.reinvite(call.getCallId(), true);
                            Toast.makeText(this, "" + getResources().getString(R.string.on_resume), Toast.LENGTH_SHORT).show();
                        } else {
                            service.hold(call.getCallId());
                        }

                    }
                    //}

                    break;
                }
                case MEDIA_SETTINGS: {
                    startActivity(new Intent(this, InCallMediaControl.class));
                    break;
                }
                case XFER_CALL: {
                    //Intent pickupIntent = new Intent(this, PickupSipUri.class);

                    Intent pickupIntent = new Intent(this, AddContactForConference.class);
                    startActivityForResult(pickupIntent, PICKUP_SIP_URI_XFER);
                    break;
                }
                case TRANSFER_CALL: {
                    final ArrayList<SipCallSession> remoteCalls = new ArrayList<SipCallSession>();
                    if (callsInfo != null) {
                        for (SipCallSession remoteCall : callsInfo) {
                            // Verify not current call
                            if (remoteCall.getCallId() != call.getCallId() && remoteCall.isOngoing()) {
                                remoteCalls.add(remoteCall);
                            }
                        }
                    }

                    if (remoteCalls.size() > 0) {
                        Builder builder = new Builder(this);
                        CharSequence[] simpleAdapter = new String[remoteCalls.size()];
                        for (int i = 0; i < remoteCalls.size(); i++) {
                            simpleAdapter[i] = remoteCalls.get(i).getRemoteContact();
                        }
                        builder.setSingleChoiceItems(simpleAdapter, -1, new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (service != null) {
                                    try {
                                        // 1 = PJSUA_XFER_NO_REQUIRE_REPLACES
                                        service.xferReplace(call.getCallId(), remoteCalls.get(which).getCallId(), 1);
                                    } catch (RemoteException e) {
                                        Log.e(THIS_FILE, "Was not able to call service method", e);
                                    }
                                }
                                dialog.dismiss();
                            }
                        })
                                .setCancelable(true)
                                .setNeutralButton(R.string.cancel, new Dialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }

                    break;
                }
                case ADD_CALL: {
                    /*if(wakeLock != null && wakeLock.isHeld()){
                        wakeLock.release();
                    }*/

                    SipConfigManager.setPreferenceBooleanValue(this, SipConfigManager.SUPPORT_MULTIPLE_CALLS, true);
                    service.sipStart();
                    /*String addedContactsArray = pref.getAddedContacts(this);
                    System.out.println("Add Contact: ==> "+ pref.getCallingNum());
*/

                    Intent addcallIntent = new Intent(this, AddContactForConference.class);
                    addcallIntent.putExtra("isCallHangup", isCallHangup);
                    startActivityForResult(addcallIntent, PICKUP_SIP_URI_NEW_CALL);
                    break;
                }
                case START_RECORDING: {
                    if (service != null) {
                        // TODO : add a tweaky setting for two channel recording in different files.
                        // Would just result here in two calls to start recording with different bitmask

                        //service.startRecording(call.getCallId(), SipManager.BITMASK_ALL);
                        //for (int i = 0; i < caller_id.size(); i++) {
                        service.startRecording(call.getCallId(), SipManager.BITMASK_ALL);
                        //}
                    }
                    break;
                }
                case STOP_RECORDING: {
                    if (service != null) {
                        // for (int i = 0; i < caller_id.size(); i++) {
                        service.stopRecording(call.getCallId());
                        // }
                        //service.stopRecording(call.getCallId());
                    }
                    break;
                }
                case START_VIDEO:
                case STOP_VIDEO: {
                    if (service != null) {
                        Bundle opts = new Bundle();
                        opts.putBoolean(SipCallSession.OPT_CALL_VIDEO, whichAction == START_VIDEO);
                        service.updateCallOptions(call.getCallId(), opts);
                    }
                    break;
                }
                case ZRTP_TRUST: {
                    if (service != null) {
                        service.zrtpSASVerified(call.getCallId());
                    }
                    break;
                }
                case ZRTP_REVOKE: {
                    if (service != null) {
                        service.zrtpSASRevoke(call.getCallId());
                    }
                    break;
                }
            }
        } catch (RemoteException e) {
            Log.e(THIS_FILE, "Was not able to call service method", e);
        }
    }


    @Override
    public void onLeftRightChoice(int whichHandle) {
        switch (whichHandle) {
            case LEFT_HANDLE:
                Log.e(THIS_FILE, "We unlock");
                proximityManager.release(0);
                proximityManager.restartTimer();
                break;
            case RIGHT_HANDLE:
                Log.e(THIS_FILE, "We clear the call");
                onTrigger(IOnCallActionTrigger.TERMINATE_CALL, getActiveCallInfo());
                proximityManager.release(0);
            default:
                break;
        }

    }

    private class ShowZRTPInfoRunnable implements Runnable, DialogInterface.OnClickListener {
        private String sasString;
        private SipCallSession callSession;

        public ShowZRTPInfoRunnable(SipCallSession call, String sas) {
            callSession = call;
            sasString = sas;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                Log.e(THIS_FILE, "ZRTP confirmed");
                if (service != null) {
                    try {
                        service.zrtpSASVerified(callSession.getCallId());
                    } catch (RemoteException e) {
                        Log.e(THIS_FILE, "Error while calling service", e);
                    }
                    dialog.dismiss();
                }
            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                dialog.dismiss();
            }
        }

        @Override
        public void run() {
            Builder builder = new Builder(InCallActivity.this);
            Resources r = getResources();
            builder.setTitle("ZRTP supported by remote party");
            builder.setMessage("Do you confirm the SAS : " + sasString);
            builder.setPositiveButton(r.getString(R.string.yes), this);
            builder.setNegativeButton(r.getString(R.string.no), this);

            AlertDialog backupDialog = builder.create();
            backupDialog.show();
        }
    }


    @Override
    public boolean shouldActivateProximity() {

        // TODO : missing headset & keyboard open
        if (lastMediaState != null) {
            if (lastMediaState.isBluetoothScoOn) {
                return false;
            }
            if (lastMediaState.isSpeakerphoneOn && !useAutoDetectSpeaker) {
                // Imediate reason to not enable proximity sensor
                return false;
            }
        }

        if (callsInfo == null) {
            return false;
        }

        boolean isValidCallState = true;
        int count = 0;
        for (SipCallSession callInfo : callsInfo) {
            if (callInfo.mediaHasVideo()) {
                return false;
            }
            if (!callInfo.isAfterEnded()) {
                int state = callInfo.getCallState();

                isValidCallState &= (
                        (state == SipCallSession.InvState.CONFIRMED) ||
                                (state == SipCallSession.InvState.CONNECTING) ||
                                (state == SipCallSession.InvState.CALLING) ||
                                (state == SipCallSession.InvState.EARLY && !callInfo.isIncoming())
                );
                count++;
            }
        }
        if (count == 0) {
            return false;
        }

        return isValidCallState;
    }

    @Override
    public void onProximityTrackingChanged(boolean acquired) {
        if (useAutoDetectSpeaker && service != null) {
            if (acquired) {
                if (lastMediaState == null || lastMediaState.isSpeakerphoneOn) {
                    try {
                        service.setSpeakerphoneOn(false);
                    } catch (RemoteException e) {
                        Log.e(THIS_FILE, "Can't run speaker change");
                    }
                }
            } else {
                if (lastMediaState == null || !lastMediaState.isSpeakerphoneOn) {
                    try {
                        service.setSpeakerphoneOn(true);
                    } catch (RemoteException e) {
                        Log.e(THIS_FILE, "Can't run speaker change");
                    }
                }
            }
        }
    }


    // Active call adapter
    private class CallsAdapter extends BaseAdapter {

        private boolean mActiveCalls;

        private SparseArray<Long> seenConnected = new SparseArray<Long>();

        public CallsAdapter(boolean notOnHold) {
            mActiveCalls = notOnHold;
        }

        private boolean isValidCallForAdapter(SipCallSession call) {
            boolean holdStateOk = false;
            if (mActiveCalls && !call.isLocalHeld()) {
                holdStateOk = true;
            }
            if (!mActiveCalls && call.isLocalHeld()) {
                holdStateOk = true;
            }

            if (holdStateOk) {
                long currentTime = System.currentTimeMillis();
                if (call.isAfterEnded()) {
                    // Only valid if we already seen this call in this adapter to be valid
                    if (hasNoMoreActiveCall() && seenConnected.get(call.getCallId(), currentTime + 2 * QUIT_DELAY) < currentTime + QUIT_DELAY) {
                        return true;
                    } else {
                        seenConnected.delete(call.getCallId());
                        return false;
                    }
                } else {
                    seenConnected.put(call.getCallId(), currentTime);
                    return true;
                }
            } else {
                long currentTime = System.currentTimeMillis();

                /*if(isComingFrom.equals("fromSipNotification")){
                    seenConnected.remove(call.getCallId());
                }else{*/
                seenConnected.put(call.getCallId(), currentTime);
                //}
                return true;
            }

            //return false;
        }

        private boolean hasNoMoreActiveCall() {
            synchronized (callMutex) {
                if (callsInfo == null) {
                    return true;
                }

                for (SipCallSession call : callsInfo) {
                    // As soon as we have one not after ended, we have at least active call
                    if (!call.isAfterEnded()) {
                        return false;
                    }
                }

            }
            return true;
        }

        @Override
        public int getCount() {
            int count = 0;
            synchronized (callMutex) {
                if (callsInfo == null) {
                    return 0;
                }

                for (SipCallSession call : callsInfo) {
                    if (isValidCallForAdapter(call)) {
                        count++;
                    }
                }
            }
            return count;
        }

        @Override
        public Object getItem(int position) {
            synchronized (callMutex) {
                if (callsInfo == null) {
                    return null;
                }
                int count = 0;
                for (SipCallSession call : callsInfo) {

                    if (isValidCallForAdapter(call)) {
                        if (count == position) {
                            return call;
                        }
                        count++;
                    }
                }

            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            SipCallSession call = (SipCallSession) getItem(position);
            if (call != null) {
                return call.getCallId();
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int count = 0;
            if (convertView == null) {
                count++;
                convertView = new InCallCard(InCallActivity.this, null);

            }

            if (convertView instanceof InCallCard) {
                InCallCard vc = (InCallCard) convertView;
                vc.setOnTriggerListener(InCallActivity.this);

                // TODO ---
                //badge.setOnTouchListener(new OnBadgeTouchListener(badge, call));
                //if (!supportMultipleCalls) {
                SipCallSession session = (SipCallSession) getItem(position);
                vc.setCallState(session, false);
                //}
            }


            return convertView;
        }

    }


}
