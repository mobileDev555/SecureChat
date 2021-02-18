package com.realapps.chat.view.login.fragment;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


// Added by Marius 09.03.2020 from call
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.support.v4.app.ActivityCompat;
import android.net.Uri;
import android.os.Build;
import android.content.pm.PackageManager;
import android.content.DialogInterface;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.Manifest;

import com.realapps.chat.R;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.interfaces.AppUnlockDialogResponse;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.KeyboardUtils;
import com.realapps.chat.utils.NotificationUtils;
import com.realapps.chat.view.dialoges.DialogLastAttempt;
import com.realapps.chat.view.home.activity.ChatWindowActivity;
import com.realapps.chat.view.home.activity.HomeActivity;

// Added by Marius 09.03.2020 from call 

import com.realapps.chat.R;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.interfaces.AppUnlockDialogResponse;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.service.SipService;
import com.realapps.chat.ui.utils.CustomDistribution;
import com.realapps.chat.ui.utils.PreferencesProviderWrapper;
import com.realapps.chat.ui.utils.PreferencesWrapper;
import com.realapps.chat.utils.Blocker;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.KeyboardUtils;
import com.realapps.chat.view.dialoges.DialogLastAttempt;
import com.realapps.chat.view.home.activity.HomeActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * MODIFIED BY MARIUS S on 13.03.2020
 *
 */

public class FragmentLock extends Fragment implements View.OnClickListener {
    private static final String TAG = FragmentLock.class.getSimpleName();

    @BindView(R.id.img_logo)
    ImageView imgLogo;
    @BindView(R.id.logo_lyr)
    LinearLayout logoLyr;
    @BindView(R.id.edt_password)
    EditText edtPassword;
    @BindView(R.id.btn_cancel)
    Button btnCancel;

    Unbinder unbinder;
    View view;
    int tmp_attempt = 0;
    private Context mContext;
    private Activity mActivity;

    Button btn_save;
// Added by Marius 09.03.2020 from call 

    String data = "aici";
    Blocker blocker;
    private PreferencesProviderWrapper prefProviderWrapper;
    public static final int PREMISSION_CONTACT_REQUEST_CODE = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_lock, container, false);
        mActivity = getActivity();
        mContext = getContext();
        unbinder = ButterKnife.bind(this, view);

        btn_save = view.findViewById(R.id.btn_save);
        btn_save.setOnClickListener(this);
        blocker = new Blocker();
       edtPassword.requestFocusFromTouch();
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
       imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        prefProviderWrapper = new PreferencesProviderWrapper(getActivity());

        return view;
    }

    //Added by Marius on 03.09.2020 from call

    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                startSipService();
            } else {
                requestPermission();
            }
        } else {
            startSipService();
        }
    }
    private boolean checkPermission() {
        int read_contact = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_CONTACTS);
        int write_contact = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_CONTACTS);
        int read_phone = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
        int record_audio = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        int modify_audio = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.MODIFY_AUDIO_SETTINGS);
        int read_storage = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int write_storage = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int config_sip = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), SipManager.PERMISSION_CONFIGURE_SIP);
        int use_sip = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), SipManager.PERMISSION_USE_SIP);
        if (read_contact == PackageManager.PERMISSION_GRANTED && write_contact == PackageManager.PERMISSION_GRANTED && read_phone == PackageManager.PERMISSION_GRANTED && record_audio == PackageManager.PERMISSION_GRANTED && modify_audio == PackageManager.PERMISSION_GRANTED && read_storage == PackageManager.PERMISSION_GRANTED && write_storage == PackageManager.PERMISSION_GRANTED && config_sip == PackageManager.PERMISSION_GRANTED && use_sip == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_CONTACTS) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_PHONE_STATE) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.RECORD_AUDIO)
                || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.MODIFY_AUDIO_SETTINGS) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), SipManager.PERMISSION_CONFIGURE_SIP)
                || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), SipManager.PERMISSION_USE_SIP)) {
        /*try {
            //Open the specific App Info page:
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivity(intent);
        } catch ( ActivityNotFoundException e ) {
            //Open the generic Apps page:
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            startActivity(intent);
        }*/
            if (isMyServiceRunning(SipService.class)) {
                stopSipService();
            }
            new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Light_NoTitleBar))
                    .setTitle("Permission Access")
                    .setMessage("You must have to allow all permission from setting of application?")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            try {
                                //Open the specific App Info page:
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getActivity().getApplicationContext().getPackageName()));
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                //e.printStackTrace();
                                //Open the generic Apps page:
                                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                startActivity(intent);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                            if (checkPermission()) {
                                startSipService();
                                //Toast.makeText(getApplicationContext(),"Permission already granted.",Toast.LENGTH_LONG).show();
                            } else {
                                //Toast.makeText(getApplicationContext(),"Please request permission.",Toast.LENGTH_LONG).show();
                                requestPermission();
                            }
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .show().setCanceledOnTouchOutside(false);
            //Toast.makeText(getApplicationContext(),"Until you grant the permission, we canot prceed further.",Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, SipManager.PERMISSION_CONFIGURE_SIP, SipManager.PERMISSION_USE_SIP}, PREMISSION_CONTACT_REQUEST_CODE);
        }
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        boolean permission_st = false;
        switch (requestCode) {
            case PREMISSION_CONTACT_REQUEST_CODE:
                if (grantResults.length > 0) {
                    // Contacts permission
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access Contacts.", Toast.LENGTH_LONG).show();
                    } else {
                        //Toast.makeText(getApplicationContext(),"Until you grant the permission, we canot display the contacts.",Toast.LENGTH_LONG).show();
                        //Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access Contacts.", Toast.LENGTH_LONG).show();
                    }
                    // Phone permission
                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        permission_st = true;
                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access Phone.", Toast.LENGTH_LONG).show();
                    } else {
                        permission_st = false;
                        //Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access Phone.", Toast.LENGTH_LONG).show();
                    }
                    // Microphone permission
                    if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                        permission_st = true;
                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access Microphone.", Toast.LENGTH_LONG).show();
                    } else {
                        permission_st = false;
                        //Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access Microphone.", Toast.LENGTH_LONG).show();
                    }
                    // Storage permission
                    if (grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                        permission_st = true;
                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access Storage.", Toast.LENGTH_LONG).show();
                    } else {
                        permission_st = false;
                        //Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access Storage.", Toast.LENGTH_LONG).show();
                    }
                    //Full permission
                    if (grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                        permission_st = true;
                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you have full access to ASTPP Dialer Plus.", Toast.LENGTH_LONG).show();
                    } else {
                        permission_st = false;
                        //Toast.makeText(getApplicationContext(), "Permission Denied, You cannot have full access to ASTPPDialer Plus.", Toast.LENGTH_LONG).show();
                    }
                    if (!permission_st) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Permission Access")
                                .setMessage("You must have to allow all permission from setting of application?")
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue with delete
                                        try {
                                            //Open the specific App Info page:
                                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            intent.setData(Uri.parse("package:" + getActivity().getApplicationContext().getPackageName()));
                                            startActivity(intent);
                                        } catch (ActivityNotFoundException e) {
                                            //e.printStackTrace();
                                            //Open the generic Apps page:
                                            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                            startActivity(intent);
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                        if (checkPermission()) {
                                            startSipService();
                                        } else {
                                            requestPermission();
                                        }
                                    }
                                })
                                //.setIcon(android.R.drawable.ic_dialog_alert)
                                .setIconAttribute(android.R.attr.alertDialogIcon)
                                .show().setCanceledOnTouchOutside(false);
                    } else {
                        System.out.println("Not Call.");
                    }
                }
                break;
        }
    }
    // Service monitoring stuff
    private void startSipService() {
        System.out.println("Start Sip Service");
        Thread t = new Thread("StartSip") {
            public void run() {
                Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
                // Optional, but here we bundle so just ensure we are using csipsimple package
                serviceIntent.setPackage(getActivity().getPackageName());
                serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(getActivity(), HomeActivity.class));
                getActivity().startService(serviceIntent);
                postStartSipService();
            }
            ;
        };
        t.start();
    }
    private void postStartSipService() {
        System.out.println("Post Start Sip Service");
        // If we have never set fast settings
        if (CustomDistribution.showFirstSettingScreen()) {
            if (!prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false)) {
                Intent prefsIntent = new Intent(SipManager.ACTION_UI_PREFS_FAST);
                prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(prefsIntent);
                return;
            }
        } else {
            boolean doFirstParams = !prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false);
            prefProviderWrapper.setPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, true);
            if (doFirstParams) {
                System.out.println("Post Start Sip Service elseif");
                prefProviderWrapper.resetAllDefaultValues();
            }
        }
    }
    private void stopSipService() {
        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        // Optional, but here we bundle so just ensure we are using csipsimple package
        serviceIntent.setPackage(getActivity().getPackageName());
        serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(getActivity(), HomeActivity.class));
        getActivity().stopService(serviceIntent);
    }
    private boolean isMyServiceRunning(Class serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    //end of add



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Handler().postDelayed(() -> {
            EditText editText = view.findViewById(R.id.edt_password);
            editText.requestFocusFromTouch();
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }, 500);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

   // @OnClick({R.id.btn_cancel})

    @OnClick({R.id.btn_cancel, R.id.btn_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                User_settings.setBackgroundApp(getContext(), true);
                KeyboardUtils.hideSoftInput(mActivity);
                mActivity.finishAffinity();
                System.exit(0);
                break;


            case R.id.btn_save:
                if (validate() && !blocker.block(1000)) {
                    User_settings.setTempAttempt(mContext, 0);
                    startActivity(new Intent(mContext, HomeActivity.class));
                }

                break;
        }
    }

    private boolean validate() {
        tmp_attempt = User_settings.getTempAttempt(mContext);
        int totalAttempt = User_settings.getMaxPasswordAttempt(mContext);

        if (!CommonUtils.hasTextdialog(mContext, edtPassword, getString(R.string.app_unlock_password_field_is_required))) {
            return false;
        } else if (CommonUtils.duressPass(mContext, User_settings.getDuressPassword(mContext), edtPassword)) {
            clearPreferences();
           // new CommonUtils().resetAll(mContext);
            return false;
        } else if (!CommonUtils.compareTextDialog(mContext, edtPassword.getText().toString(), User_settings.getAppPassword(mContext), getString(R.string.password_isn_t_correct_try_again))) {
            tmp_attempt++;
            EditText editText = view.findViewById(R.id.edt_password);
            editText.getText().clear();
            User_settings.setTempAttempt(mContext, tmp_attempt);
            //String msg = "Password isn't correct. Please try again. Incorrect attempt (" + tmp_attempt + "/" + totalAttempt + "). ";
            String msg = getString(R.string.password_isn_t_correct_please_try_again_incorrect_attempt_1_d_2_d, tmp_attempt, totalAttempt);

            if (tmp_attempt == totalAttempt - 1) {
                msg += getString(R.string.you_are_on_your_last_attempt_if_incorrect_your_data_will_be_security_wiped);
            }

            try {
                if (tmp_attempt <= totalAttempt - 2)
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                else if (tmp_attempt == totalAttempt - 1) {
                    DialogLastAttempt dialogLastAttempt =

                            new DialogLastAttempt(mContext, getString(R.string.you_are_on_the_last_attemp_if_incorrect_your_data_will_be_security_wiped), new AppUnlockDialogResponse() {

                                @Override
                                public void appUnlock() {

                                }

                                @Override
                                public void cancel() {

                                }
                            });
                    dialogLastAttempt.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    Log.e(data, "Last Attempt");
                    dialogLastAttempt.show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            if (tmp_attempt == totalAttempt) {

                clearPreferences();
                //new CommonUtils().resetAll(mContext);
            }
            return false;
        } else {
            return true;
        }
    }
    private void clearPreferences() {

        try {
            Log.v(data, "Clear");
            // clearing app data
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear com.realapps.chat");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onPause() {
        KeyboardUtils.hideSoftInput(mActivity);
        super.onPause();
    }



    @Override
    public void onClick(View v) {
        if (validate()) {
            User_settings.setTempAttempt(mContext, 0);
            btn_save.setEnabled(false);
            btn_save.setClickable(false);
            btn_save.setOnClickListener(null);

            Log.e(TAG, "onClick: OK");
            if (getActivity().getClass().toString().contains("LockScreen")) {

                Log.e(TAG, "onClick: LockScreen");
                try {
                    KeyboardUtils.hideSoftInput(mActivity);
                    AppConstants.lockscreen = false;
                    new HomeActivity().startBackGroundThread(mActivity);
                    if (ChatWindowActivity.sendMessageAckToSocket != null) {
                        ChatWindowActivity.sendMessageAckToSocket.onSendAck();
                    }
                    NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                    NotificationUtils.showBadge(mActivity, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    //Lock.getInstance(getContext()).removeAppKillingHandler();
                    getActivity().finish();
                }


            } else {
                Log.e(TAG, "onClick: Splash");
                User_settings.setTempAttempt(mContext, 0);
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                getActivity().startActivity(intent);
            }


        }
    }
}
