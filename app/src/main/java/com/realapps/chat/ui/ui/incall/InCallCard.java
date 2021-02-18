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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.view.menu.MenuBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.github.romychab.slidetounlock.ISlideChangeListener;
import com.github.romychab.slidetounlock.ISlideListener;
import com.github.romychab.slidetounlock.SlideLayout;
import com.github.romychab.slidetounlock.renderers.TranslateRenderer;
import com.github.romychab.slidetounlock.sliders.HorizontalSlider;
import com.google.gson.Gson;
import com.pkmmte.view.CircularImageView;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.model.EccContactConferenceList;
import com.realapps.chat.ui.api.GlobalClass;
import com.realapps.chat.ui.api.SipCallSession;
import com.realapps.chat.ui.api.SipConfigManager;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.api.SipProfile;
import com.realapps.chat.ui.api.SipUri;
import com.realapps.chat.ui.helper.PrefManager;
import com.realapps.chat.ui.models.CallerInfo;
import com.realapps.chat.ui.service.SipService;
import com.realapps.chat.ui.service.SwitchOffReceiver;
import com.realapps.chat.ui.ui.view.java.conferencecall.ManageConferenceCall;
import com.realapps.chat.ui.utils.ContactsAsyncHelper;
import com.realapps.chat.ui.utils.ExtraPlugins;
import com.realapps.chat.ui.utils.Log;
import com.realapps.chat.ui.utils.PreferencesProviderWrapper;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.KeyboardUtils;

import org.webrtc.videoengine.ViERenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InCallCard extends FrameLayout implements OnClickListener, MenuBuilder.Callback, ISlideListener, SwitchOffReceiver.SwitchOff {

    private static final String THIS_FILE = "InCallCard";

    private SipCallSession callInfo;
    private String cachedRemoteUri = "";
    private int cachedInvState = SipCallSession.InvState.INVALID;
    private int cachedMediaState = SipCallSession.MediaState.ERROR;
    private boolean cachedCanRecord = false;
    private boolean cachedIsRecording = false;
    private boolean cachedIsHold = false;
    private boolean cachedVideo = false;
    private ImageView in_call_bg;


    private CircularImageView photo;
    private TextView remoteName, remoteSipAddress, callStatusText, callSecureText, txtRemainingSecs, txtCallCredit;
    private View viewForcallrate;
    private RelativeLayout endButton;
    private RelativeLayout decline, sentMessage;
    private ViewGroup callSecureBar;
    private Chronometer elapsedTime;
    LinearLayout chronometer, PTCCalling, bottomBalance;

    private SurfaceView renderView;
    private PreferencesProviderWrapper prefs;
    private ViewGroup endCallBar;
    private MenuBuilder btnMenuBuilder;
    boolean isConference = false;
    RelativeLayout rl1, rl2;
    private boolean hasVideo = false;
    private boolean canVideo = false;
    private boolean cachedZrtpVerified;
    private boolean cachedZrtpActive;
    private RelativeLayout inCallCardControls;
    InCallCheckableImageView xferButton, holdButton, recordButton, dtmfButton, speakerButton, muteButton, addcallButton;
    RelativeLayout llXfer, llHold, llRecord, llDTMF, llSpeaker, llMute, llAddCall;
    LinearLayout llSpe, llMu, llxf, reconnect_ll;
    private String split_text;
    private static TextDrawable drawable;
    static ArrayList<ContactEntity> contactList;
    ContactEntity entity;
    static String name = "";
    static String no_name = "Unknown";
    DbHelper db;
    PrefManager pref;
    ImageView nextIm;
    SlideLayout pickupCall;
    private TextView txt_text_sliding;
    //SlideToActView pickupCall;
    boolean callPicked = false;
    RelativeLayout callunloackPickbar, unlockCallEndRel, unlockMesSendRel, unlockCallRecevieRel;
    boolean isDeviceLocked;

    //private ActionMenuPresenter mActionMenuPresenter;

    private Map<String, ExtraPlugins.DynActivityPlugin> incallPlugins;
    private Context context;
    User_settings us = new User_settings();
    boolean supportMultipleCalls = false;

    TextView reconnect_txt;


    //Reconnect TC_008 & TC_009: Connected broadcast
    private BroadcastReceiver mConnected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("NetworkChangeReceiver::: receiver: in IncallCard " + intent.getAction());

            if (intent.getAction().equals(SipManager.ACTION_NETWORK_CONNECTED)) {
                reconnect_ll.setVisibility(GONE);
                chronometer.setVisibility(VISIBLE);
            }

        }
    };

    //Reconnect TC_008 & TC_009: Disconnected broadcast
    private BroadcastReceiver mDisConnected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("NetworkChangeReceiver::: receiver: in IncallCard " + intent.getAction());

            if (intent.getAction().equals(SipManager.ACTION_NETWORK_DISCONNECTED)) {
                reconnect_ll.setVisibility(VISIBLE);
                chronometer.setVisibility(GONE);
            }

        }
    };


    public InCallCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        //Fabric.with(context, new Crashlytics());
        KeyboardUtils.hideKeyboard(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        User_settings.setLastActivity(context, InCallActivity.class.getSimpleName());
        inflater.inflate(R.layout.in_call_card, this, true);
        SwitchOffReceiver.switchOff = this;
        pickupCall = (SlideLayout) findViewById(R.id.pickupcall);
        inCallCardControls = (RelativeLayout) findViewById(R.id.inCallCardControls);
        txt_text_sliding = (TextView) findViewById(R.id.txt_text_sliding);


        //inCallCardControls.setVisibility(GONE);
        prefs = new PreferencesProviderWrapper(context);
        canVideo = prefs.getPreferenceBooleanValue(SipConfigManager.USE_VIDEO);
        this.context = context;

        nextIm = (ImageView) findViewById(R.id.nextIm);
        supportMultipleCalls = SipConfigManager.getPreferenceBooleanValue(context, SipConfigManager.SUPPORT_MULTIPLE_CALLS, false);

        nextIm.setOnClickListener(this);

        initControllerView();

        incallPlugins = ExtraPlugins.getDynActivityPlugins(context, SipManager.ACTION_INCALL_PLUGIN);

        if (((InCallActivity) context).getIntent().getExtras() != null) {
            callInfo = ((InCallActivity) context).getIntent().getExtras().getParcelable("from_notification");
            if (callInfo != null && !callInfo.isIncoming()) {
                GlobalClass gc = GlobalClass.getInstance();
                bottomBalance.setVisibility(VISIBLE);
                viewForcallrate.setVisibility(VISIBLE);
                // txtRemainingSecs.setText("Call Credits : " + gc.getBalanceAPICall(context) + " Minutes");
                txtCallCredit.setText(getResources().getString(R.string.call_credit_1));
                txtRemainingSecs.setText(gc.getBalanceAPICall(context) + " Minutes");
            }

        }
        //Reconnect TC_008 & TC_009: Registering broadcast
        IntentFilter makesipFilter = new IntentFilter(SipManager.ACTION_NETWORK_DISCONNECTED);
        getContext().registerReceiver(mDisConnected, makesipFilter);
        IntentFilter makesipFilter2 = new IntentFilter(SipManager.ACTION_NETWORK_CONNECTED);
        getContext().registerReceiver(mConnected, makesipFilter2);

    }

    private void initControllerView() {
        in_call_bg = (ImageView) findViewById(R.id.in_call_bg);
        photo = (CircularImageView) findViewById(R.id.contact_photo);
        remoteName = (TextView) findViewById(R.id.contact_name_display_name);
        txtCallCredit = (TextView) findViewById(R.id.txtCallCredit);
        txtRemainingSecs = (TextView) findViewById(R.id.txtRemainingSecs);
        remoteSipAddress = (TextView) findViewById(R.id.contact_name_sip_address);
        elapsedTime = (Chronometer) findViewById(R.id.elapsedTime);
        chronometer = (LinearLayout) findViewById(R.id.chronometer);
        PTCCalling = (LinearLayout) findViewById(R.id.PTCCalling);
        bottomBalance = (LinearLayout) findViewById(R.id.ll);


        //Reconnect TC_008 & TC_009: Text binding with screen layout
        reconnect_txt = (TextView) findViewById(R.id.reconnect_txt);
        reconnect_ll = (LinearLayout) findViewById(R.id.reconnect_ll);

        callStatusText = (TextView) findViewById(R.id.call_status_text);
        callSecureBar = (ViewGroup) findViewById(R.id.call_secure_bar);
        callSecureText = (TextView) findViewById(R.id.call_secure_text);
        endCallBar = (ViewGroup) findViewById(R.id.end_call_bar);
        viewForcallrate = (View) findViewById(R.id.viewForcallrate);
        unlockCallRecevieRel = (RelativeLayout) findViewById(R.id.unlockCallRecevieRel);
        unlockCallEndRel = (RelativeLayout) findViewById(R.id.unlockCallEndRel);
        unlockMesSendRel = (RelativeLayout) findViewById(R.id.unlockMesSendRel);
        callunloackPickbar = (RelativeLayout) findViewById(R.id.callunloackPickbar);


        View btn;
        endButton = (RelativeLayout) findViewById(R.id.endButton);
        decline = (RelativeLayout) findViewById(R.id.img_decline);
        sentMessage = (RelativeLayout) findViewById(R.id.img_message);
        endButton.setOnClickListener(this);
        decline.setOnClickListener(this);
        sentMessage.setOnClickListener(this);

        unlockCallEndRel.setOnClickListener(this);
        unlockCallRecevieRel.setOnClickListener(this);
        unlockMesSendRel.setOnClickListener(this);


        initCallCardControls(false);
        GlobalClass gc = GlobalClass.getInstance();

        //System.out.println("InCallCard isDeviceLocked: " + gc.isDeviceLocked(context));
        isDeviceLocked = gc.isDeviceLocked(context);
        if (isDeviceLocked) {
            callunloackPickbar.setVisibility(GONE);
            pickupCall.setVisibility(VISIBLE);
            decline.setVisibility(VISIBLE);
            sentMessage.setVisibility(GONE);
        } else {
            callunloackPickbar.setVisibility(VISIBLE);
            pickupCall.setVisibility(GONE);
            decline.setVisibility(GONE);
            sentMessage.setVisibility(GONE);
        }


        pickupCall.setRenderer(new TranslateRenderer());
        pickupCall.setSlider(new HorizontalSlider());
        pickupCall.setChildId(R.id.slide_child);
        pickupCall.setThreshold(0.9f);

        pickupCall.addSlideChangeListener(new ISlideChangeListener() {
            @Override
            public void onSlideStart(SlideLayout slider) {
                ;
            }

            @Override
            public void onSlideChanged(SlideLayout slider, float percentage) {
                txt_text_sliding.setAlpha(1 - percentage);
            }

            @Override
            public void onSlideFinished(SlideLayout slider, boolean done) {
                onSlideDone(slider, done);
            }
        });

        //updateMenuView();
    }

    @Override
    public void onSlideDone(SlideLayout slider, boolean done) {
        if (done) {
            pickupCall.setVisibility(GONE);
            slider.setVisibility(GONE);
            sentMessage.setVisibility(GONE);
            decline.setVisibility(GONE);
            if (!callPicked) {
                dispatchTriggerEvent(IOnCallActionTrigger.TAKE_CALL);
                callPicked = true;
            }
        }
    }

    public void initCallCardControls(boolean isComingCallState) {

        xferButton = (InCallCheckableImageView) inCallCardControls.findViewById(R.id.xferCallButton);
        holdButton = (InCallCheckableImageView) inCallCardControls.findViewById(R.id.holdCallButton);
        recordButton = (InCallCheckableImageView) inCallCardControls.findViewById(R.id.recordCallButton);
        dtmfButton = (InCallCheckableImageView) inCallCardControls.findViewById(R.id.dtmfCallButton);
        speakerButton = (InCallCheckableImageView) inCallCardControls.findViewById(R.id.speaker);
        muteButton = (InCallCheckableImageView) inCallCardControls.findViewById(R.id.muteButton);
        addcallButton = (InCallCheckableImageView) inCallCardControls.findViewById(R.id.addCallButton);


        llXfer = (RelativeLayout) inCallCardControls.findViewById(R.id.llXferCallButton);
        llxf = (LinearLayout) inCallCardControls.findViewById(R.id.llxfer);

        //llHold = (RelativeLayout) inCallCardControls.findViewById(R.id.llholdCallButton);
        //llRecord = (RelativeLayout) inCallCardControls.findViewById(R.id.llRecordCall);
        //llDTMF = (RelativeLayout) inCallCardControls.findViewById(R.id.llDtmfCall);
        llSpeaker = (RelativeLayout) inCallCardControls.findViewById(R.id.llSpeaker);
        llSpe = (LinearLayout) inCallCardControls.findViewById(R.id.llSpe);
        llMute = (RelativeLayout) inCallCardControls.findViewById(R.id.llMute);
        llMu = (LinearLayout) inCallCardControls.findViewById(R.id.llmute1);
        //llAddCall = (RelativeLayout) inCallCardControls.findViewById(R.id.llAddCall);


        //muteButton.setBackgroundResource(R.drawable.new_mute_on);
        //dtmfButton.setBackgroundResource(R.drawable.new_white_keypad);
        //speakerButton.setBackgroundResource(R.drawable.new_speaker_off);
        //addcallButton.setBackgroundResource(R.drawable.new_call_transfer);
        xferButton.setBackgroundResource(R.drawable.ic_person);

        xferButton.setVisibility(GONE);
        dtmfButton.setVisibility(GONE);

        /*muteButton.setBackgroundResource(R.drawable.new_mute_icon);
        dtmfButton.setBackgroundResource(R.drawable.new_white_keypad);
        speakerButton.setBackgroundResource(R.drawable.new_speaker);
        addcallButton.setBackgroundResource(R.drawable.new_addcall_icon);
        xferButton.setBackgroundResource(R.drawable.new_call_transfer);*/

        if (!isComingCallState) {
            //llRecord.setBackground(getResources().getDrawable(R.drawable.plain_border_circle));
            recordButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_call_rec_stop));
            holdButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_call_hold));
            //llHold.setBackground(getResources().getDrawable(R.drawable.plain_border_circle));

            /*recordButton.setBackgroundResource(R.drawable.new_call_record_stop);
            holdButton.setBackgroundResource(R.drawable.new_call_hold);*/
        }

        xferButton.setChecked(false);
        holdButton.setChecked(false);
        recordButton.setChecked(false);
        dtmfButton.setChecked(false);
        speakerButton.setChecked(false);
        muteButton.setChecked(false);
        addcallButton.setChecked(false);

        xferButton.setOnClickListener(this);
        holdButton.setOnClickListener(this);
        recordButton.setOnClickListener(this);
        dtmfButton.setOnClickListener(this);
        speakerButton.setOnClickListener(this);
        muteButton.setOnClickListener(this);
        addcallButton.setOnClickListener(this);


    }

    private boolean added = false;


    public synchronized void setCallState(SipCallSession aCallInfo, boolean supportMultipleCalls) {
        callInfo = aCallInfo;
        pickupCall.setVisibility(GONE);
        sentMessage.setVisibility(GONE);
        decline.setVisibility(GONE);

        if (callInfo == null) {
            updateElapsedTimer();
            cachedInvState = SipCallSession.InvState.INVALID;
            cachedMediaState = SipCallSession.MediaState.ERROR;
            cachedCanRecord = false;
            cachedIsRecording = false;
            cachedIsHold = false;
            cachedVideo = false;
            cachedZrtpActive = false;
            cachedZrtpVerified = false;
            return;
        }


        if (callInfo.isBeforeConfirmed()) {
            if (!callInfo.isIncoming()) {
                pickupCall.setVisibility(GONE);
                User_settings us = new User_settings();
                GlobalClass gc = GlobalClass.getInstance();
                if (gc.getBalanceAPICall(context) != null) {
                    bottomBalance.setVisibility(VISIBLE);
                    viewForcallrate.setVisibility(VISIBLE);
                    // txtRemainingSecs.setText("Call Credits : " + gc.getBalanceAPICall(context) + " Minutes");
                    txtCallCredit.setText(getResources().getString(R.string.call_credit_1));
                    txtRemainingSecs.setText(gc.getBalanceAPICall(context) + " Minutes");
                } else {
                    viewForcallrate.setVisibility(GONE);
                    bottomBalance.setVisibility(GONE);
                }
                    /*User_settings us = new User_settings();
                    new Utility().showCustomCallCreditToast(context, "Call Credits : " + us.getBalace(context) + " Minutes");*/
            } else {
                bottomBalance.setVisibility(GONE);
            }
        }

        if (callInfo.isIncoming() || callInfo.isOngoing()) {

            PrefManager pref = new PrefManager(getContext());
            System.out.println("Preference value:::::: speaker:" + pref.getSpeaker() + " mute:" + pref.getMute());


            //Speaker background
            if (pref.getSpeaker().equals("on")) {
                // llSpe.setBackground(getResources().getDrawable(R.drawable.badge_text_bg));
                speakerButton.setImageDrawable(getResources().getDrawable(R.drawable.speakeron));
            } else {
                //  llSpe.setBackground(getResources().getDrawable(R.drawable.circular_gray));
                speakerButton.setImageDrawable(getResources().getDrawable(R.drawable.speaker));
            }

            //Mute background
            if (pref.getMute().equals("on")) {
                //  llMu.setBackground(getResources().getDrawable(R.drawable.badge_text_bg));
                muteButton.setImageDrawable(getResources().getDrawable(R.drawable.micon));

            } else {
                //  llMu.setBackground(getResources().getDrawable(R.drawable.circular_gray));
                muteButton.setImageDrawable(getResources().getDrawable(R.drawable.mic));
            }


            addcallButton.setImageDrawable(getResources().getDrawable(R.drawable.user));
            speakerButton.setImageDrawable(getResources().getDrawable(R.drawable.speaker));
            muteButton.setImageDrawable(getResources().getDrawable(R.drawable.mic));
            //  llxf.setBackground(getResources().getDrawable(R.drawable.circular_gray));

            long timer = elapsedTime.getBase();
            System.out.println("Current Timer:" + timer);
            //llMu.setBackground(getResources().getDrawable(R.drawable.white_border_circle));
            //muteButton.setImageResource(R.drawable.ic_mute_tint_white);
        }

        updateRemoteName();
        updateCallStateBar();
        //updateQuickActions();
        initCallCardControls(true);
        updateElapsedTimer();

        cachedInvState = callInfo.getCallState();
        cachedMediaState = callInfo.getMediaStatus();
        cachedCanRecord = callInfo.canRecord();
        cachedIsRecording = callInfo.isRecording();
        cachedIsHold = callInfo.isLocalHeld();
        cachedVideo = callInfo.mediaHasVideo();
        cachedZrtpActive = callInfo.getHasZrtp();
        cachedZrtpVerified = callInfo.isZrtpSASVerified();

        // VIDEO STUFF -- EXPERIMENTAL
        if (canVideo) {
            if (callInfo.getCallId() >= 0 && cachedVideo) {
                if (renderView == null) {
                    renderView = ViERenderer.CreateRenderer(getContext(), true);
                    photo.setVisibility(View.GONE);
                    RelativeLayout container = (RelativeLayout) findViewById(R.id.call_card_container);

                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    lp.addRule(RelativeLayout.ALIGN_LEFT, RelativeLayout.TRUE);
                    lp.addRule(RelativeLayout.ALIGN_RIGHT, RelativeLayout.TRUE);
                    lp.addRule(RelativeLayout.ALIGN_TOP, RelativeLayout.TRUE);
                    //  lp.addRule(RelativeLayout.ABOVE, R.id.call_action_bar);
                    renderView.setLayoutParams(lp);
                    container.addView(renderView, 0);

                    Log.d(THIS_FILE, "Render window added");
                    SipService.setVideoWindow(callInfo.getCallId(), renderView, false);

                    View v = findViewById(R.id.end_call_bar);
                    ViewGroup.LayoutParams lp2 = v.getLayoutParams();
                    lp2.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    v.setLayoutParams(lp2);
                }
                hasVideo = true;
            } else {
                if (renderView != null) {
                    renderView.setVisibility(View.GONE);
                    photo.setVisibility(View.VISIBLE);
                }
                hasVideo = false;
            }
        }
        if (onTriggerListener != null) {
            onTriggerListener.onDisplayVideo(hasVideo && canVideo);
        }

    }

    /* We accept height twice than width */
    private static float minRatio = 0.5f;
    /* We accept width 1/4 bigger than height */
    private static float maxRatio = 1.25f;

    private static float minButtonRation = 0.75f;


    private final Handler handler = new Handler();
    private final Runnable postLayout = new Runnable() {
        @Override
        public void run() {

            float w = getWidth();
            float h = getHeight();
            View v = findViewById(R.id.call_card_container);
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            v.setLayoutParams(lp);
            /*if (w > 0 && h > 0) {
                float currentRatio = w / h;
                float newWidth = w;
                float newHeight = h;
                Log.d(THIS_FILE, "Current ratio is " + currentRatio);
                if (currentRatio < minRatio) {
                    newHeight = w / minRatio;
                    int padding = (int) Math.floor((h - newHeight) / 2);
                    setPadding(0, padding, 0, padding);
                } else if (currentRatio > maxRatio) {
                    newWidth = h * maxRatio;
                    int padding = (int) Math.floor((w - newWidth) / 2);
                    setPadding(padding, 0, padding, 0);
                } else {
                    setPadding(0, 0, 0, 0);
                }
                View v = findViewById(R.id.end_call_bar);
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                if (currentRatio < minButtonRation && !hasVideo) {
                    lp.height = (int) ((1.0f - minButtonRation) * newHeight);
                } else {
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                v.setLayoutParams(lp);
                updateMenuView();
            }*/

        }
    };


  /*  @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        supportMultipleCalls = SipConfigManager.getPreferenceBooleanValue(context, SipConfigManager.SUPPORT_MULTIPLE_CALLS, false);

        DbHelper db = new DbHelper(context);
        List<ContactEntity> contactList = db.getContactList();
        List<EccContactConferenceList> conference_list = new ArrayList<>();
        PrefManager pref = new PrefManager(context);
        String conferenceNum = pref.getAddedContacts(context);
        String manageConferenceNum = pref.getManageConferenceContact(context);

        GlobalClass gc = GlobalClass.getInstance();
        //   String conference_flag = gc.getContact_flag(getContext());
        if (conferenceNum != null) {
            Gson gson = new Gson();
            String[] text = gson.fromJson(conferenceNum, String[].class);
            for (int j = 0; j < text.length; j++) {
                String num = text[j];
                for (int i = 0; i < contactList.size(); i++) {
                    ContactEntity entity = contactList.get(i);
                    EccContactConferenceList eccContactConferenceList = new EccContactConferenceList();

                    if (!entity.getEccId().toLowerCase().equals(num.toLowerCase())) {
                        String ecc_id = entity.getEccId();
                        String screen_name = CommonUtils.getContactName(context, entity.getEccId());
                        eccContactConferenceList.setEccId(ecc_id);
                        eccContactConferenceList.setScreenName(screen_name);
                        conference_list.add(eccContactConferenceList);
                    }
                }

            }
        }


        if (manageConferenceNum != null) {
            Gson gson = new Gson();
            String[] text = gson.fromJson(manageConferenceNum, String[].class);
            System.out.println("OnLayout Chanegd : " + text.length);
            *//*if(text.length >= 2){
                isConference = true;
            }else{
                isConference = false;
            }*//*

            if (text.length > 1) {
                if (supportMultipleCalls) {
                    nextIm.setVisibility(VISIBLE);
                    remoteName.setText("Conference Call");
                    isConference = true;
                    split_text = split_word(remoteName.getText().toString());
                    drawable = name_image(split_text);
                    photo.setImageDrawable(drawable);

                    remoteSipAddress.setText("");
                    remoteSipAddress.setVisibility(INVISIBLE);
                }

            } else {
                nextIm.setVisibility(GONE);
                if (text.length > 0) {
                    String callerId = text[0];

                    contactList = db.getContactList();
                    if (contactList.size() != 0) {
                        for (int i = 0; i < contactList.size(); i++) {
                            entity = contactList.get(i);
                            if (entity.getEccId() != null) {

                                if (isConference) {
                                    isConference = false;
                                    final String aRemoteUri = callInfo.getRemoteContact();
                                    if (aRemoteUri != null) {//&& !aRemoteUri.equalsIgnoreCase(cachedRemoteUri)) {
                                        cachedRemoteUri = aRemoteUri;
                                        SipUri.ParsedSipContactInfos uriInfos = SipUri.parseSipContact(cachedRemoteUri);
                                        String text1 = SipUri.getDisplayedSimpleContact(aRemoteUri);
                                        remoteName.setText(text1);
                                    } else {
                                        remoteName.setText(callerId);
                                    }
                                }
                            }
                        }
                    }
                    if(remoteName.getText().toString() != null ){
                        if(remoteName.getText().toString().length()>0){
                            split_text = split_word(remoteName.getText().toString());
                            drawable = name_image(split_text);
                            photo.setImageDrawable(drawable);
                        }else{
                            split_text = split_word(callerId.toUpperCase());
                            drawable = name_image(split_text);
                            photo.setImageDrawable(drawable);
                        }
                    }else{
                        split_text = split_word(callerId.toUpperCase());
                        drawable = name_image(split_text);
                        photo.setImageDrawable(drawable);
                    }

                   *//* name = CommonUtils.getContactName(context, callerId);
                    remoteName.setText(name);
                    split_text = split_word(remoteName.getText().toString());
                    drawable = name_image(split_text);
                    photo.setImageDrawable(drawable);*//*

                    remoteSipAddress.setVisibility(VISIBLE);
                    remoteSipAddress.setText(getResources().getString(R.string.ecc_id) + " : " + callerId.toUpperCase().toString());
                }
            }
        }

        if (supportMultipleCalls) {
            handler.postDelayed(postLayout, 100);
        }
        super.onLayout(changed, left, top, right, bottom);
    }*/

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            getContext().unregisterReceiver(mDisConnected);
            getContext().unregisterReceiver(mConnected);
        } catch (Exception e) {
            mDisConnected = null;
            mConnected = null;
            e.printStackTrace();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        supportMultipleCalls = SipConfigManager.getPreferenceBooleanValue(context, SipConfigManager.SUPPORT_MULTIPLE_CALLS, false);

        DbHelper db = new DbHelper(context);
        List<ContactEntity> contactList = db.getContactList();
        List<EccContactConferenceList> conference_list = new ArrayList<>();
        PrefManager pref = new PrefManager(context);
        String conferenceNum = pref.getAddedContacts(context);
        String manageConferenceNum = pref.getManageConferenceContact(context);

        GlobalClass gc = GlobalClass.getInstance();
        //   String conference_flag = gc.getContact_flag(getContext());
        if (conferenceNum != null) {
            Gson gson = new Gson();
            String[] text = gson.fromJson(conferenceNum, String[].class);
            for (int j = 0; j < text.length; j++) {
                String num = text[j];
                for (int i = 0; i < contactList.size(); i++) {
                    ContactEntity entity = contactList.get(i);
                    EccContactConferenceList eccContactConferenceList = new EccContactConferenceList();

                    if (!entity.getEccId().toLowerCase().equals(num.toLowerCase())) {
                        String ecc_id = entity.getEccId();
                        String screen_name = CommonUtils.getContactName(context, entity.getEccId());
                        eccContactConferenceList.setEccId(ecc_id);
                        eccContactConferenceList.setScreenName(screen_name);
                        conference_list.add(eccContactConferenceList);
                    }
                }

            }
        }


        if (manageConferenceNum != null) {
            Gson gson = new Gson();
            String[] text = gson.fromJson(manageConferenceNum, String[].class);
            System.out.println("OnLayout Chanegd : " + text.length);
            /*if(text.length >= 2){
                isConference = true;
            }else{
                isConference = false;
            }*/

            if (text.length > 1) {
                if (supportMultipleCalls) {
                    nextIm.setVisibility(VISIBLE);
                    remoteName.setText("Conference Call");
                    isConference = true;
                    split_text = split_word(remoteName.getText().toString());
                    drawable = name_image(split_text);
                    photo.setImageDrawable(drawable);

                    remoteSipAddress.setText("");
                    remoteSipAddress.setVisibility(INVISIBLE);
                }

            } /*else {
                nextIm.setVisibility(GONE);
                if (text.length > 0) {
                    String callerId = text[0];

                    contactList = db.getContactList();

                    name = CommonUtils.getContactName(context, callerId);
                    remoteName.setText(name);
                    split_text = split_word(remoteName.getText().toString());
                    drawable = name_image(split_text);
                    photo.setImageDrawable(drawable);

                    remoteSipAddress.setVisibility(VISIBLE);
                    remoteSipAddress.setText(getResources().getString(R.string.ecc_id) + " : " + callerId.toUpperCase().toString());
                }
            }*/
        }

        if (supportMultipleCalls) {
            handler.postDelayed(postLayout, 100);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.img_decline) {
            if (callInfo.isBeforeConfirmed() && callInfo.isIncoming()) {
                endCallBar.setVisibility(GONE);
                dispatchTriggerEvent(IOnCallActionTrigger.REJECT_CALL);
                callPicked = false;
            }

        } else if (id == R.id.img_message) {
            //callPicked = false;
            if (callInfo.isBeforeConfirmed() && callInfo.isIncoming()) {
                endCallBar.setVisibility(GONE);
                dispatchTriggerEvent(IOnCallActionTrigger.REJECT_CALL);
                callPicked = false;
            }

        } else if (id == R.id.endButton) {
            pref.addContact(context, null);

            if (supportMultipleCalls) {
                elapsedTime.stop();
                chronometer.setVisibility(VISIBLE);
                PTCCalling.setVisibility(GONE);

                if (callInfo.isBeforeConfirmed() && callInfo.isIncoming()) {
                    //inCallCardControls.setVisibility(View.GONE);
                    dispatchTriggerEvent(IOnCallActionTrigger.REJECT_CALL);

                } else if (!callInfo.isAfterEnded()) {
                    inCallCardControls.setVisibility(View.INVISIBLE);
                    endCallBar.setVisibility(GONE);
                    dispatchTriggerEvent(IOnCallActionTrigger.TERMINATE_CALL);
                }
            } else {
                if (callInfo.isBeforeConfirmed() && callInfo.isIncoming()) {
                    //inCallCardControls.setVisibility(View.GONE);
                    dispatchTriggerEvent(IOnCallActionTrigger.REJECT_CALL);

                    //getBalance();
                } else if (!callInfo.isAfterEnded()) {
                    inCallCardControls.setVisibility(View.INVISIBLE);
                    endCallBar.setVisibility(GONE);
                    dispatchTriggerEvent(IOnCallActionTrigger.TERMINATE_CALL);
                    //getBalance();
                }
            }
        } else if (id == R.id.unlockCallEndRel) {
            if (callInfo.isBeforeConfirmed() && callInfo.isIncoming()) {
                endCallBar.setVisibility(GONE);
                callPicked = false;
                dispatchTriggerEvent(IOnCallActionTrigger.REJECT_CALL);
            }
        } else if (id == R.id.unlockCallRecevieRel) {

            if (!callPicked) {
                dispatchTriggerEvent(IOnCallActionTrigger.TAKE_CALL);
                callPicked = true;
            }

        } else if (id == R.id.unlockMesSendRel) {
            //callPicked = false;
            if (callInfo.isBeforeConfirmed() && callInfo.isIncoming()) {
                endCallBar.setVisibility(GONE);
                callPicked = false;
                dispatchTriggerEvent(IOnCallActionTrigger.REJECT_CALL);
            }

        } else if (id == R.id.xferCallButton) {
            if (callInfo.isIncoming() || callInfo.isOngoing()) {
                dispatchTriggerEvent(IOnCallActionTrigger.XFER_CALL);
            }

        } else if (id == R.id.holdCallButton) {
            if (callInfo.isIncoming() || callInfo.isOngoing()) {
                holdButton.toggle();


                System.out.println("InCallActivity : holdbutton clicked " + callInfo.isLocalHeld() + " Id " + callInfo.getCallId());
                if (callInfo.isLocalHeld()) {
                    holdButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_call_hold));
                } else {
                    holdButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_call_resume));
                }

                dispatchTriggerEvent(IOnCallActionTrigger.TOGGLE_HOLD);
            }
        } else if (id == R.id.recordCallButton) {
            if (callInfo.isIncoming() || callInfo.isOngoing()) {

                recordButton.toggle();


                if (callInfo.isRecording()) {
                    recordButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_call_rec_stop));
                } else {
                    recordButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_call_rec_start));
                }
                dispatchTriggerEvent(callInfo.isRecording() ? IOnCallActionTrigger.STOP_RECORDING : IOnCallActionTrigger.START_RECORDING);
            }
        } else if (id == R.id.dtmfCallButton) {
            if (callInfo.isIncoming() || callInfo.isOngoing()) {

                dtmfButton.toggle();
                dispatchTriggerEvent(IOnCallActionTrigger.DTMF_DISPLAY);
            }
        } else if (id == R.id.speaker) {
            speakerButton.toggle();
            PrefManager pref = new PrefManager(getContext());
            if (callInfo.isIncoming() || callInfo.isOngoing()) {

                speakerButton.toggle();

                if (speakerButton.isChecked()) {
                    //speakerButton.setChecked(true);
                    dispatchTriggerEvent(IOnCallActionTrigger.SPEAKER_ON);
                    //speakerButton.setBackgroundResource(R.drawable.ic_speaker_tint_white);
                    // llSpe.setBackground(getResources().getDrawable(R.drawable.badge_text_bg));
                    speakerButton.setImageDrawable(getResources().getDrawable(R.drawable.speakeron));

                } else {
                    //speakerButton.setChecked(false);
                    dispatchTriggerEvent(IOnCallActionTrigger.SPEAKER_OFF);
                    //speakerButton.setBackgroundResource(R.drawable.ic_speaker_tint_white);
                    //llSpe.setBackground(getResources().getDrawable(R.drawable.plain_border_circle));
                    //  llSpe.setBackground(getResources().getDrawable(R.drawable.circular_gray));
                    speakerButton.setImageDrawable(getResources().getDrawable(R.drawable.speaker));
                }
            }

            String speakerevent = pref.getSpeaker();
            if (speakerevent.equals("off")) {
                pref.setSpeaker("on");
                dispatchTriggerEvent(IOnCallActionTrigger.SPEAKER_ON);
                // llSpe.setBackground(getResources().getDrawable(R.drawable.badge_text_bg));
                speakerButton.setImageDrawable(getResources().getDrawable(R.drawable.speakeron));
            } else {
                pref.setSpeaker("off");

                //speakerButton.setChecked(false);
                dispatchTriggerEvent(IOnCallActionTrigger.SPEAKER_OFF);
                //   llSpe.setBackground(getResources().getDrawable(R.drawable.circular_gray));
                speakerButton.setImageDrawable(getResources().getDrawable(R.drawable.speaker));
            }

        } else if (id == R.id.muteButton) {
            /*if (callInfo.isIncoming() || callInfo.isOngoing()) {

                muteButton.toggle();

                if (muteButton.isChecked()) {
                    dispatchTriggerEvent(IOnCallActionTrigger.MUTE_ON);
                    //muteButton.setBackgroundResource(R.drawable.ic_mute_tint_white);
                    llMu.setBackground(getResources().getDrawable(R.drawable.call_message_2));
                } else {
                    dispatchTriggerEvent(IOnCallActionTrigger.MUTE_OFF);
                    //muteButton.setBackgroundResource(R.drawable.ic_mute_tint_white);
                    //llMu.setBackground(getResources().getDrawable(R.drawable.plain_border_circle));
                    llMu.setBackground(getResources().getDrawable(R.drawable.white_border_circle));
                }
            }*/
            if (callInfo.isIncoming() || callInfo.isOngoing()) {
                muteButton.toggle();
            /*if (muteButton.isChecked()) {
                //muteButton.setChecked(true);
                dispatchTriggerEvent(IOnCallActionTrigger.MUTE_ON);
                muteButton.setBackgroundResource(R.drawable.new_mute_off);
                llMute.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }else {
                //muteButton.setChecked(false);
                dispatchTriggerEvent(IOnCallActionTrigger.MUTE_OFF);
                muteButton.setBackgroundResource(R.drawable.new_mute_on);
                llMute.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }*/

                // if (muteButton.isChecked()) {
                PrefManager pref = new PrefManager(getContext());

                //if (muteButton.isChecked()) {
                if (pref.getMute().equals("off")) {

                    //muteButton.setChecked(true);
                    pref.setMute("on");
                    dispatchTriggerEvent(IOnCallActionTrigger.MUTE_ON);
                    //muteButton.setImageResource(R.drawable.written_mute_icon);
                    //llMu.setBackground(getResources().getDrawable(R.drawable.user_badge));
                    muteButton.setImageDrawable(getResources().getDrawable(R.drawable.micon));
                } else {
                    //muteButton.setChecked(false);
                    pref.setMute("off");
                    dispatchTriggerEvent(IOnCallActionTrigger.MUTE_OFF);
                    //muteButton.setImageResource(R.drawable.written_unmute_icon);
                    //  llMu.setBackground(getResources().getDrawable(R.drawable.circular_gray));
                    muteButton.setImageDrawable(getResources().getDrawable(R.drawable.mic));
                }
            }
        } else if (id == R.id.addCallButton) {
            if (callInfo.isIncoming() || callInfo.isOngoing()) {
                //  llxf.setBackground(getResources().getDrawable(R.drawable.circular_gray));
                // addcallButton.setBackgroundResource(R.drawable.ic_profile_tint_white);
                // TODO: 12/14/2018 disable as the discuss with mahendra sir
                // dispatchTriggerEvent(IOnCallActionTrigger.ADD_CALL);
            }
        } else if (id == R.id.nextIm) {
            //System.out.println("ManageConferenceCall nextIm clicked " + callInfo.getCallId());
            String conferenceContact = pref.getManageConferenceContact(context).toString();
            Intent newIntent = new Intent(context, ManageConferenceCall.class);
            newIntent.putExtra("callId", callInfo.getCallId());
            newIntent.putExtra("contactArray", conferenceContact);
            context.startActivity(newIntent);
        }
    }


    /**
     * Bind the main visible view with data from call info
     */
    private void updateCallStateBar() {

        int stateText = -1;
        if (callInfo.isAfterEnded()) {
            stateText = R.string.call_state_disconnected;
            endCallBar.setVisibility(GONE);
            callPicked = false;

            inCallCardControls.setVisibility(View.GONE);
        } else if (callInfo.isLocalHeld() || callInfo.isRemoteHeld()) {
            stateText = R.string.on_hold;
            pickupCall.setVisibility(View.GONE);
            decline.setVisibility(GONE);
            sentMessage.setVisibility(GONE);
        } else if (callInfo.isBeforeConfirmed()) {
            if (callInfo.isIncoming()) {
                stateText = R.string.call_state_incoming;
                endCallBar.setVisibility(VISIBLE);
                System.out.println(THIS_FILE + " isDevice lock : " + isDeviceLocked);
                if (isDeviceLocked) {
                    callunloackPickbar.setVisibility(GONE);
                    if (callPicked) {
                        decline.setVisibility(INVISIBLE);
                        sentMessage.setVisibility(INVISIBLE);
                        pickupCall.setVisibility(INVISIBLE);
                    } else {
                        decline.setVisibility(VISIBLE);
                        sentMessage.setVisibility(GONE);
                        pickupCall.setVisibility(VISIBLE);
                    }
                } else {
                    decline.setVisibility(INVISIBLE);
                    sentMessage.setVisibility(INVISIBLE);
                    pickupCall.setVisibility(INVISIBLE);
                    callunloackPickbar.setVisibility(VISIBLE);

                    if (callPicked) {
                        callunloackPickbar.setVisibility(INVISIBLE);
                    } else {
                        callunloackPickbar.setVisibility(VISIBLE);
                    }
                }


                inCallCardControls.setVisibility(View.INVISIBLE);
            } else {
                stateText = R.string.call_state_calling;
                endCallBar.setVisibility(VISIBLE);
                callunloackPickbar.setVisibility(INVISIBLE);
                inCallCardControls.setVisibility(View.VISIBLE);
            }
        } else {
            callunloackPickbar.setVisibility(GONE);
            decline.setVisibility(GONE);
            sentMessage.setVisibility(GONE);
            pickupCall.setVisibility(GONE);
        }

        if ((callInfo.isBeforeConfirmed() && callInfo.isIncoming()) /* Before call is established we have the slider */ ||
                callInfo.isAfterEnded()  /*Once ended, just wait for the call finalization*/) {

            /*decline.setVisibility(VISIBLE);
            sentMessage.setVisibility(VISIBLE);
            pickupCall.setVisibility(VISIBLE);*/
            endButton.setVisibility(GONE);
            PTCCalling.setVisibility(GONE);
            chronometer.setVisibility(GONE);


        } else {
            endButton.setVisibility(VISIBLE);

        }


        if (stateText != -1) {
            chronometer.setVisibility(GONE);
            PTCCalling.setVisibility(VISIBLE);

            /*if(!(callInfo.isBeforeConfirmed() && callInfo.isIncoming())){
                PTCCalling.setVisibility(GONE);
            }else if(!callInfo.isIncoming()){
                PTCCalling.setVisibility(VISIBLE);

            }*/
            callStatusText.setText(stateText);
            setVisibleWithFade(callStatusText, true);

        } else {
            //inCallCardControls.setVisibility(View.VISIBLE);
            //setVisibleWithFade(callStatusText, false);
            //callStatusText.setVisibility(INVISIBLE);
            chronometer.setVisibility(VISIBLE);
            PTCCalling.setVisibility(GONE);
        }
        //callIcon.setContentDescription(CallsUtils.getStringCallState(callInfo, getContext()));

    }

    private void updateRemoteName() {

        final String aRemoteUri = callInfo.getRemoteContact();

        contactList = new ArrayList<>();
        db = new DbHelper(context);
        contactList = db.getContactList();

        pref = new PrefManager(context);

        //String new_called_number = pref.getCallingNum();

        // If not already set with the same value, just ignore it
        if (aRemoteUri != null && !aRemoteUri.equalsIgnoreCase(cachedRemoteUri)) {
            cachedRemoteUri = aRemoteUri;
            SipUri.ParsedSipContactInfos uriInfos = SipUri.parseSipContact(cachedRemoteUri);
            String text = SipUri.getDisplayedSimpleContact(aRemoteUri);
            StringBuffer statusTextBuffer = new StringBuffer();

           /* if (callInfo.isIncoming()) {
                remoteName.setText(text);
            } else {*/
            if (contactList.size() != 0) {
                for (int i = 0; i < contactList.size(); i++) {
                    entity = contactList.get(i);
                    if (entity.getEccId() != null) {

                        if (entity.getEccId().toLowerCase().equals(text.toLowerCase())) {
                            name = CommonUtils.getContactName(context, entity.getEccId());
                            if (name != null) {
                                remoteName.setText(name);
                            } else {
                                remoteName.setText(entity.getEccId().toUpperCase());
                            }


                            /*split_text = split_word(remoteName.getText().toString());
                            drawable = name_image(split_text);
                            photo.setImageDrawable(drawable);*/

                        }
                    }


                   /* if (remoteName.getText().toString().length() == 0) {
                        remoteName.setText(text.toUpperCase());
                    }*/
                }

            }

            //Himadri
            //To resolve, Text drawable not display proper at calling time. Once call pickup then it will work fine.

            if (remoteName.getText().toString() != null) {
                if (remoteName.getText().toString().length() > 0) {
                    split_text = split_word(remoteName.getText().toString());
                    drawable = name_image(split_text);
                    photo.setImageDrawable(drawable);
                } else {
                    split_text = split_word(text);
                    drawable = name_image(split_text);
                    photo.setImageDrawable(drawable);
                }

            } else {
                split_text = split_word(text);
                drawable = name_image(split_text);
                photo.setImageDrawable(drawable);
            }



            /*remoteName.setText(no_name);


            if (contactList.size() != 0) {
                for (int i = 0; i < contactList.size(); i++) {
                    entity = contactList.get(i);
                    if (entity.getEccId() != null) {

                        if (entity.getEccId().toLowerCase().equals(text.toLowerCase())) {
                            name = entity.getScreenName();
                            remoteName.setText(name);
                        }
                        *//*else{
                            remoteName.setText(no_name);
                        }*//*
                    }
                }
            }*/


            if (callInfo.getAccId() != SipProfile.INVALID_ID) {
                SipProfile acc = SipProfile.getProfileFromDbId(getContext(), callInfo.getAccId(),
                        new String[]{
                                SipProfile.FIELD_ID, SipProfile.FIELD_DISPLAY_NAME
                        });
                if (acc != null && acc.display_name != null) {
                    //statusTextBuffer.append(getResources().getString(R.string.ecc_id) + " : ");
                }
            } else {
                //statusTextBuffer.append(getResources().getString(R.string.ecc_id) + " : ");
            }


            String dialedNumber = uriInfos.userName;
            String remoteEnd = null;
            if (dialedNumber.contains("#")) {
                String[] remContact = dialedNumber.split("#");
                remoteEnd = remContact[1].toUpperCase();
            } else {
                remoteEnd = dialedNumber.toUpperCase();
            }
            statusTextBuffer.append(remoteEnd);

            if (!supportMultipleCalls) {
                GlobalClass gc = GlobalClass.getInstance();
                gc.setLastCalledNum(context, remoteEnd);
            }

            if (!dialedNumber.contains("#")) {
                name = CommonUtils.getContactName(context, dialedNumber);
                if (name != null) {
                    remoteName.setText(name);
                } else {
                    remoteName.setText(entity.getEccId().toUpperCase());
                }


                split_text = split_word(remoteName.getText().toString());
                drawable = name_image(split_text);
                photo.setImageDrawable(drawable);
            }


            remoteSipAddress.setText(getResources().getString(R.string.ecc_id) + " : " + statusTextBuffer.toString());
            Thread t = new Thread() {
                public void run() {
                    // Looks like a phone number so search the contact throw
                    // contacts
                    CallerInfo callerInfo = CallerInfo.getCallerInfoFromSipUri(getContext(),
                            cachedRemoteUri);
                    if (callerInfo != null && callerInfo.contactExists) {
                        LoadCallerInfoMessage lci = new LoadCallerInfoMessage(InCallCard.this, callerInfo);
/*                        userHandler.sendMessage(userHandler.obtainMessage(LOAD_CALLER_INFO,
                                lci));*/
                    }
                }

                ;
            };
            t.start();

        }

        // Useless to process that
        if (cachedInvState == callInfo.getCallState() &&
                cachedMediaState == callInfo.getMediaStatus()) {
            return;
        }
    }

    public String split_word(String label) {
        String x = label;

        String new_letter = "";
        String firstletter = x;

        try {
            //To remove special characters
            Pattern pt_1 = Pattern.compile("[^a-zA-Z0-9]");
            Matcher match_1 = pt_1.matcher(firstletter);

            if (match_1.find()) {
                while (match_1.find()) {
                    String s = match_1.group();
                    firstletter = firstletter.replaceAll("\\" + s, "");

                }
                //To get first character
                firstletter = firstletter.substring(0, 1);
                new_letter = firstletter;

            } else {
                //To get first character

                firstletter = x.substring(0, 1);
                new_letter = firstletter;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new_letter;
    }

    public TextDrawable name_image(String letter) {

        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int color1 = generator.getRandomColor();
        Typeface tf = Typeface.createFromAsset(context.getAssets(),
                "fonts/MavenPro-Bold.ttf");
        String newLatter = letter.toUpperCase();

        TextDrawable builder = TextDrawable.builder()
                .beginConfig()
                .withBorder(0)
                .width(230)  // width in px
                .height(230) // height in px
                .fontSize(90)
                .useFont(tf)
                .endConfig()
                .buildRoundRect(newLatter, Color.parseColor("#27ba96"), 230);

        return builder;
    }


    private void updateElapsedTimer() {
        System.out.println("updateElapsedTimer::" + "updateElapsedTimer");

        long elapsedMillis = SystemClock.elapsedRealtime() - elapsedTime.getBase();
        System.out.println("updateElapsedTimer::" + elapsedMillis);
        if (callInfo == null) {
            elapsedTime.stop();
            //elapsedTime.setVisibility(VISIBLE);
            chronometer.setVisibility(VISIBLE);
            PTCCalling.setVisibility(GONE);
            return;
        }

        elapsedTime.setBase(callInfo.getConnectStart());

        int sigSecureLevel = callInfo.getTransportSecureLevel();
        boolean isSecure = (callInfo.isMediaSecure() || sigSecureLevel > 0);
        //setVisibleWithFade(callSecureBar, isSecure);
        String secureMsg = "";
        if (isSecure) {
            List<String> secureTxtList = new ArrayList<String>();
            if (sigSecureLevel == SipCallSession.TRANSPORT_SECURE_TO_SERVER) {
                secureTxtList.add(getContext().getString(R.string.transport_secure_to_server));
            } else if (sigSecureLevel == SipCallSession.TRANSPORT_SECURE_FULL) {
                secureTxtList.add(getContext().getString(R.string.transport_secure_full));
            }
            if (callInfo.isMediaSecure()) {
                secureTxtList.add(callInfo.getMediaSecureInfo());
            }
            secureMsg = TextUtils.join("\r\n", secureTxtList);
        }
        callSecureText.setText(secureMsg);

        int state = callInfo.getCallState();
        switch (state) {
            case SipCallSession.InvState.INCOMING:
            case SipCallSession.InvState.CALLING:
            case SipCallSession.InvState.EARLY:
            case SipCallSession.InvState.CONNECTING:
                //elapsedTime.setVisibility(GONE);
                //elapsedTime.setVisibility(INVISIBLE);
                chronometer.setVisibility(GONE);
                //PTCCalling.setVisibility(VISIBLE);
                if (!callInfo.isIncoming()) {
                    PTCCalling.setVisibility(VISIBLE);
                } else {
                    PTCCalling.setVisibility(GONE);
                }
                if (state == SipCallSession.InvState.CALLING)
                    callStatusText.setText(R.string.call_state_connecting);
                if (state == SipCallSession.InvState.CONNECTING)
                    callStatusText.setText(R.string.call_state_calling);
                break;
            case SipCallSession.InvState.CONFIRMED:
                inCallCardControls.setVisibility(VISIBLE);
                Log.v(THIS_FILE, "we start the timer now ");
                if (callInfo.isLocalHeld()) {
                    elapsedTime.stop();
                    //elapsedTime.setVisibility(View.GONE);
                    //elapsedTime.setVisibility(INVISIBLE);
                    chronometer.setVisibility(GONE);
                    PTCCalling.setVisibility(VISIBLE);
                } else {
                    elapsedTime.start();
                    //elapsedTime.setVisibility(View.VISIBLE);
                    chronometer.setVisibility(VISIBLE);
                    PTCCalling.setVisibility(GONE);

                }
                break;
            case SipCallSession.InvState.NULL:
            case SipCallSession.InvState.DISCONNECTED:

                elapsedTime.stop();
                //elapsedTime.setVisibility(VISIBLE);
                chronometer.setVisibility(VISIBLE);
                PTCCalling.setVisibility(GONE);

                break;
            default:
                break;
        }

    }

    private static final int LOAD_CALLER_INFO = 0;

    @Override
    public void onSwitchOffPhone() {
        if (callInfo.isBeforeConfirmed() && callInfo.isIncoming()) {
            endCallBar.setVisibility(GONE);
            dispatchTriggerEvent(IOnCallActionTrigger.REJECT_CALL);
            callPicked = false;
        }
    }

    private class LoadCallerInfoMessage {
        LoadCallerInfoMessage(InCallCard callCard, CallerInfo ci) {
            callerInfo = ci;
            target = callCard;
        }

        CallerInfo callerInfo;
        InCallCard target;
    }

    private final static Handler userHandler = new ContactLoadedHandler();

    private static class ContactLoadedHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == LOAD_CALLER_INFO) {
                LoadCallerInfoMessage lci = (LoadCallerInfoMessage) msg.obj;
                if (lci.callerInfo != null && lci.callerInfo.contactContentUri != null) {
                    // Flag we'd like high res loading
                    lci.callerInfo.contactContentUri = lci.callerInfo.contactContentUri.buildUpon().appendQueryParameter(ContactsAsyncHelper.HIGH_RES_URI_PARAM, "1").build();
                }
                ContactsAsyncHelper.updateImageViewWithContactPhotoAsync(
                        lci.target.getContext(),
                        lci.target.photo,
                        lci.callerInfo,
                        R.drawable.ic_person,
                        drawable
                );
                //   lci.target.remoteName.setText(lci.callerInfo.name);
                lci.target.remoteName.setText(lci.callerInfo.name);
                /*if (name != null && name.length() != 0) {
                    lci.target.remoteName.setText(name);
                } else {
                    lci.target.remoteName.setText(no_name);
                }*/
                /*ContactsAsyncHelper.updateImageViewWithContactPhotoAsync(
                        lci.target.getContext(),
                        lci.target.in_call_bg,
                        lci.callerInfo,
                        R.drawable.incall_user_default,
                        drawable);*/
               /* lci.target.photo.setContentDescription(lci.callerInfo.name);
                lci.target.in_call_bg.setContentDescription(lci.callerInfo.name);*/
            }

        }
    }

    ;

    /*
    private OnBadgeTouchListener dragListener;
    public void setOnTouchListener(OnBadgeTouchListener l) {
        dragListener = l;
        super.setOnTouchListener(l);
    }
    */

    private IOnCallActionTrigger onTriggerListener;

    /*
     * Registers a callback to be invoked when the user triggers an event.
     * @param listener the OnTriggerListener to attach to this view
     */
    public void setOnTriggerListener(IOnCallActionTrigger listener) {
        onTriggerListener = listener;
    }


    private void dispatchTriggerEvent(int whichHandle) {
        if (onTriggerListener != null) {
            onTriggerListener.onTrigger(whichHandle, callInfo);
        }
    }


    public void terminate() {
        if (callInfo != null && renderView != null) {
            SipService.setVideoWindow(callInfo.getCallId(), null, false);
        }
    }


    private void setVisibleWithFade(View v, boolean in) {
        if (v.getVisibility() == View.VISIBLE && in) {
            // Already visible and ask to show, ignore
            return;
        }
        if (v.getVisibility() == View.GONE && !in) {
            // Already gone and ask to hide, ignore
            return;
        }

        Animation anim = AnimationUtils.loadAnimation(getContext(), in ? android.R.anim.fade_in : android.R.anim.fade_out);
        anim.setDuration(1000);
        v.startAnimation(anim);
        v.setVisibility(in ? View.VISIBLE : View.GONE);
    }


    @Override
    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.takeCallButton) {
            dispatchTriggerEvent(IOnCallActionTrigger.TAKE_CALL);
            return true;
        } else if (itemId == R.id.dontTakeCallButton) {
            dispatchTriggerEvent(IOnCallActionTrigger.DONT_TAKE_CALL);
            return true;
        }
        return false;
    }

    @Override
    public void onMenuModeChange(MenuBuilder menu) {
        // Nothing to do.
    }

}
