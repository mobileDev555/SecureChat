package com.realapps.chat.view.login.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.realapps.chat.R;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.ui.api.ISipService;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.service.SipService;
import com.realapps.chat.ui.utils.Log;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.KeyboardUtils;
import com.realapps.chat.utils.NetworkUtils;
import com.realapps.chat.view.home.activity.HomeActivity;
import com.realapps.chat.view.login.activity.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import android.content.pm.PackageManager;
import android.widget.Switch;
import android.widget.TextView;

import static android.view.WindowManager.*;

/**
 * Created by Prashant Sharma on 3/15/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class FragmentLicenceKey extends Fragment {

    public static final int PREMISSION_CONTACT_REQUEST_CODE = 1;
    private static final int REQUEST_OVERLAY_PERMISSION = 1000;

    @BindView(R.id.edt_licence_key)
    EditText edtLicenceKey;
    @BindView(R.id.btn_activate)
    Button btnActivate;
    Unbinder unbinder;
    private Context mContext;
    private Activity mActivity;
    private ProgressDialog mProgressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_licence_key, container, false);
        mActivity = getActivity();
        mContext = getContext();
        unbinder = ButterKnife.bind(this, view);
        CommonUtils.hideTextSuggestion(edtLicenceKey);

        return view;
    }



    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
            } else {
//                requestPermission();
                ((LoginActivity) getActivity()).updateScreen(new FragmentPermission(), "", false);
            }
        } else {
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private boolean checkPermission() {
        boolean isChecked1 = false;
        boolean isChecked2 = false;
        boolean isChecked3 = false;
        int read_contact = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS);
        int write_contact = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CONTACTS);
        int read_phone = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE);
        int record_audio = ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO);
        int modify_audio = ContextCompat.checkSelfPermission(mContext, Manifest.permission.MODIFY_AUDIO_SETTINGS);
        int read_storage = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);
        int write_storage = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int config_sip = ContextCompat.checkSelfPermission(mContext, SipManager.PERMISSION_CONFIGURE_SIP);
        int use_sip = ContextCompat.checkSelfPermission(mContext, SipManager.PERMISSION_USE_SIP);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if(Settings.canDrawOverlays(mContext)) isChecked1 = true;//appear on top of app

            if(Settings.System.canWrite(getContext())) isChecked2 = true;//change system settings
        }

        //main permissions
        if (read_contact == PackageManager.PERMISSION_GRANTED && write_contact == PackageManager.PERMISSION_GRANTED
                    && read_phone == PackageManager.PERMISSION_GRANTED && record_audio == PackageManager.PERMISSION_GRANTED
                    && modify_audio == PackageManager.PERMISSION_GRANTED && read_storage == PackageManager.PERMISSION_GRANTED
                    && write_storage == PackageManager.PERMISSION_GRANTED && config_sip == PackageManager.PERMISSION_GRANTED
                    && use_sip == PackageManager.PERMISSION_GRANTED) {
                isChecked3 = true;
        }

        if (isChecked1 && isChecked2 && isChecked3) return true;
        else return false;
    }

    @OnClick(R.id.btn_activate)
    public void onViewClicked() {
        if (CommonUtils.hasText(mContext, edtLicenceKey)) {
            if (NetworkUtils.isNetworkConnected(mContext)) {
                KeyboardUtils.hideSoftInput(mActivity);
                mProgressDialog = CommonUtils.showLoadingDialog(mContext);
                licenceKey();
                licenceKey_call();
            } else {
                CommonUtils.showErrorMsg(mContext, getString(R.string.no_internet_connection));
            }
        }
    }

    public void licenceKey() {
        AndroidNetworking.post(ApiEndPoints.checkLicence)
                .addBodyParameter("licence_key", edtLicenceKey.getText().toString().trim())
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();

                        try {
                            JSONObject rootObject = new JSONObject(response.toString());

                            if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                if (rootObject.getString("licence_key_status").equalsIgnoreCase("active")) {
                                    String keyId = rootObject.getString("result_data");
                                    User_settings.setLicenceKeyID(mContext, keyId);

                                    FragmentPassword screenName = new FragmentPassword();
                                    Bundle argsCompose = new Bundle();
                                    argsCompose.putString(AppConstants.keyId, keyId);
                                    screenName.setArguments(argsCompose);
                                    ((LoginActivity) mActivity).updateScreen(screenName, "", false);
                                } else {
                                    CommonUtils.showInfoMsg(mContext, "This licence key is already in use.");
                                }

                            } else {
                                CommonUtils.showInfoMsg(mContext, rootObject.getString("msg"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        System.out.println("API Error : " + error.getErrorDetail());
                        CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again));
                    }
                });
    }


    public void licenceKey_call() {
        AndroidNetworking.post(ApiEndPoints.check_licence_call)
                .addBodyParameter("licence_key", edtLicenceKey.getText().toString().trim())
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();

                        try {
                            JSONObject rootObject = new JSONObject(response.toString());

                            if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                if (rootObject.getString("licence_key_status").equalsIgnoreCase("active")) {
                                    String keyId = rootObject.getString("result_data");
                                    User_settings.setLicenceKeyID(mContext, keyId);

                                    User_settings.setLicenseActivate(mContext, true);
                                    User_settings.setUserId(mContext, keyId);
                                    FragmentPassword screenName = new FragmentPassword();
                                    Bundle argsCompose = new Bundle();
                                    argsCompose.putString(AppConstants.keyId, keyId);
                                    screenName.setArguments(argsCompose);
                                    ((LoginActivity) getActivity()).updateScreen(screenName, "", false);
                                } else {
                                    CommonUtils.showInfoMsg(mContext, "This licence key is already in use.");
                                }

                            } else {
                                CommonUtils.showInfoMsg(mContext, rootObject.getString("msg"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        System.out.println("API Error : " + error.getErrorDetail());
                        CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again));
                    }
                });
    }

    private void requestPermission() {
        AppConstants.onpermission = true;

        if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.READ_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.WRITE_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.READ_PHONE_STATE)
                || ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.RECORD_AUDIO)
                || ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.MODIFY_AUDIO_SETTINGS)
                || ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(mActivity, SipManager.PERMISSION_CONFIGURE_SIP)
                || ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(mActivity, SipManager.PERMISSION_USE_SIP)) {

            new AlertDialog.Builder(new ContextThemeWrapper(mActivity, android.R.style.Theme_Light_NoTitleBar))
                    .setTitle("Permission Access")
                    .setMessage("You must have to allow all permission from setting of application?")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // continue with delete
                        try {
                            //Open the specific App Info page:
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + mActivity.getApplicationContext().getPackageName()));
                            startActivity(intent);

                        } catch (ActivityNotFoundException e) {
                            //e.printStackTrace();

                            //Open the generic Apps page:
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                            startActivity(intent);

                        }
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {
                        // do nothing
                        if (checkPermission()) {

                        } else {
                            requestPermission();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .show().setCanceledOnTouchOutside(false);

        } else {
            ActivityCompat.requestPermissions(mActivity,
                    new String[]{
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.MODIFY_AUDIO_SETTINGS,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            SipManager.PERMISSION_CONFIGURE_SIP,
                            SipManager.PERMISSION_USE_SIP
                    }, PREMISSION_CONTACT_REQUEST_CODE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(mContext)){
                // ask for setting
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + mActivity.getApplicationContext().getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            }

            if (!isAccessGranted()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:" + mActivity.getApplicationContext().getPackageName()));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e("======error", e.getMessage());
                }
            }


            if(!Settings.System.canWrite(getContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + mActivity.getApplicationContext().getPackageName()));
                startActivity(intent);
            }
        }
    }

//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        boolean permission_st = false;
//        switch (requestCode) {
//            case PREMISSION_CONTACT_REQUEST_CODE:
//                if (grantResults.length > 0) {
//                    // Contacts permission
//                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    } else {
//                    }
//                    // Phone permission
//                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                        permission_st = true;
//                    } else {
//                        permission_st = false;
//                    }
//                    // Microphone permission
//                    if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
//                        permission_st = true;
//                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access Microphone.", Toast.LENGTH_LONG).show();
//                    } else {
//                        permission_st = false;
//                        //Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access Microphone.", Toast.LENGTH_LONG).show();
//                    }
//                    // Storage permission
//                    if (grantResults[3] == PackageManager.PERMISSION_GRANTED) {
//                        permission_st = true;
//                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access Storage.", Toast.LENGTH_LONG).show();
//                    } else {
//                        permission_st = false;
//                        //Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access Storage.", Toast.LENGTH_LONG).show();
//                    }
//                    //Full permission
//                    if (grantResults[4] == PackageManager.PERMISSION_GRANTED) {
//                        permission_st = true;
//                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you have full access to ASTPP Dialer Plus.", Toast.LENGTH_LONG).show();
//                    } else {
//                        permission_st = false;
//                        //Toast.makeText(getApplicationContext(), "Permission Denied, You cannot have full access to ASTPPDialer Plus.", Toast.LENGTH_LONG).show();
//                    }
//
//                    if (!permission_st) {
//                        new AlertDialog.Builder(mActivity)
//                                .setTitle("Permission Access")
//                                .setMessage("You must have to allow all permission from setting of application?")
//                                .setCancelable(false)
//                                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
//                                    // continue with delete
//                                    try {
//                                        //Open the specific App Info page:
//                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                        intent.setData(Uri.parse("package:" + mContext.getPackageName()));
//                                        startActivity(intent);
//
//                                    } catch (ActivityNotFoundException e) {
//                                        //e.printStackTrace();
//
//                                        //Open the generic Apps page:
//                                        Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
//                                        startActivity(intent);
//
//                                    }
//                                })
//                                .setNegativeButton(android.R.string.no, (dialog, which) -> {
//                                    // do nothing
//                                    if (checkPermission()) {
//
//                                    } else {
//                                        requestPermission();
//                                    }
//                                })
//                                .setIconAttribute(android.R.attr.alertDialogIcon)
//                                .show().setCanceledOnTouchOutside(false);
//
//                    } else {
//                        System.out.println("Not Call.");
//                    }
//
//                }
//                break;
//            case REQUEST_OVERLAY_PERMISSION:
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (Settings.canDrawOverlays(mContext)) {
//                        // permission granted...
//                    }else{
//                        // permission not granted...
//                    }
//                }
//                break;
//        }
//    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean isAccessGranted() {
        try {
            PackageManager packageManager =getActivity(). getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getActivity().getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getActivity().getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}