package com.realapps.chat.view.login.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.realapps.chat.R;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.utils.Log;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.KeyboardUtils;
import com.realapps.chat.view.login.activity.LoginActivity;

import java.util.ArrayList;
import java.util.Objects;

public class FragmentPermission extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {


    private Context mContext;
    private Activity mActivity;
    private Switch switch_call_logs, switch_camera, switch_contacts, switch_microphone, switch_phone, switch_storage, switch_full_access;
    private ImageView back_img;
    private LinearLayout linear_top, linear_system;
    private TextView txt_top, txt_system;

    public boolean p_call_log, p_camera, p_contacts, p_microphone, p_phone, p_storage, p_full_access, p_system, p_top;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.permission_dialog, container, false);
        mActivity = getActivity();
        mContext = getContext();
        KeyboardUtils.hideSoftInput(mActivity);
        initView(view);
        initPermissionState();
        return view;
    }

    private void initPermissionState() {
        int read_contact = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS);
        int write_contact = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CONTACTS);
        int read_phone = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE);
        int record_audio = ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO);
        int modify_audio = ContextCompat.checkSelfPermission(mContext, Manifest.permission.MODIFY_AUDIO_SETTINGS);
        int read_storage = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);
        int write_storage = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int camera = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
        int call_logs = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CALL_LOG);
        int config_sip = ContextCompat.checkSelfPermission(mContext, SipManager.PERMISSION_CONFIGURE_SIP);
        int use_sip = ContextCompat.checkSelfPermission(mContext, SipManager.PERMISSION_USE_SIP);

        if(read_contact == PackageManager.PERMISSION_GRANTED && write_contact == PackageManager.PERMISSION_GRANTED) {
            switch_contacts.setChecked(true);
            p_contacts = true;
        }
        if(read_phone == PackageManager.PERMISSION_GRANTED) {
            switch_phone.setChecked(true);
            p_phone = true;
        }
        if(camera == PackageManager.PERMISSION_GRANTED) {
            switch_camera.setChecked(true);
            p_camera = true;
        }
        if(call_logs == PackageManager.PERMISSION_GRANTED) {
            switch_call_logs.setChecked(true);
            p_call_log = true;
        }
        if(record_audio == PackageManager.PERMISSION_GRANTED && modify_audio == PackageManager.PERMISSION_GRANTED) {
            switch_microphone.setChecked(true);
            p_microphone = true;
        }
        if(read_storage == PackageManager.PERMISSION_GRANTED && write_storage == PackageManager.PERMISSION_GRANTED) {
            switch_storage.setChecked(true);
            p_storage = true;
        }
        if(config_sip == PackageManager.PERMISSION_GRANTED && use_sip == PackageManager.PERMISSION_GRANTED) {
            switch_full_access.setChecked(true);
            p_full_access = true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(mContext)) {
                txt_top.setText("Allowed");
            } else {
                txt_top.setText("not Allowed");
            }

            if(Settings.System.canWrite(getContext())) {
                txt_system.setText("Allowed");
            } else {
                txt_system.setText("not Allowed");
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initView(View view) {
        switch_call_logs = view.findViewById(R.id.switch_call_logs);
        switch_camera = view.findViewById(R.id.switch_camera);
        switch_contacts = view.findViewById(R.id.switch_contacts);
        switch_microphone = view.findViewById(R.id.switch_microphone);
        switch_phone = view.findViewById(R.id.switch_phone);
        switch_storage = view.findViewById(R.id.switch_storage);
        switch_full_access = view.findViewById(R.id.switch_full_access);

        switch_call_logs.setOnCheckedChangeListener(this);
        switch_camera.setOnCheckedChangeListener(this);
        switch_contacts.setOnCheckedChangeListener(this);
        switch_microphone.setOnCheckedChangeListener(this);
        switch_phone.setOnCheckedChangeListener(this);
        switch_storage.setOnCheckedChangeListener(this);
        switch_full_access.setOnCheckedChangeListener(this);

        txt_system = view.findViewById(R.id.txt_system);
        txt_top = view.findViewById(R.id.txt_top);
        back_img = view.findViewById(R.id.back_img);
        linear_system = view.findViewById(R.id.linear_system);
        linear_top = view.findViewById(R.id.linear_top);
        back_img.setOnClickListener(this);
        linear_system.setOnClickListener(this);
        linear_top.setOnClickListener(this);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        switch(id) {
            case R.id.switch_call_logs:
                checkCallLogsPermission(isChecked, 1);
                break;
            case R.id.switch_camera:
                checkCameraPermission(isChecked, 2);
                break;
            case R.id.switch_contacts:
                checkContactsPermission(isChecked, 3);
                break;
            case R.id.switch_microphone:
                checkAudioPermission(isChecked, 4);
                break;
            case R.id.switch_phone:
                checkPhonePermission(isChecked, 5);
                break;
            case R.id.switch_storage:
                checkStoragePermission(isChecked, 6);
                break;
            case R.id.switch_full_access:
                checkSipPermission(isChecked, 7);
                break;
        }
    }

    //========1
    private void checkCallLogsPermission(boolean isChecked, int code) {
        if(isChecked) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.WRITE_CALL_LOG,
                            Manifest.permission.READ_CALL_LOG
                    }, code);
        } else {
            if (p_call_log) switch_call_logs.setChecked(true);
            CommonUtils.showErrorMsg(mContext, "You have to allow permission");
        }
    }
    //========2
    private void checkCameraPermission(boolean isChecked, int code) {
        if(isChecked) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.CAMERA
                    }, code);
        } else {
            if (p_camera) switch_camera.setChecked(true);
            CommonUtils.showErrorMsg(mContext, "You have to allow permission");
        }
    }
    //========3
    private void checkContactsPermission(boolean isChecked, int code) {
        if(isChecked) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS
                    }, code);
        } else {
            if (p_contacts) switch_contacts.setChecked(true);
            CommonUtils.showErrorMsg(mContext, "You have to allow permission");
        }
    }
    //========4
    private void checkAudioPermission(boolean isChecked, int code) {
        if(isChecked) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.MODIFY_AUDIO_SETTINGS
                    }, code);
        } else {
            if (p_microphone) switch_microphone.setChecked(true);
            CommonUtils.showErrorMsg(mContext, "You have to allow permission");
        }
    }
    //========5
    private void checkPhonePermission(boolean isChecked, int code) {
        if(isChecked) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.READ_PHONE_STATE
                    }, code);
        } else {
            if (p_phone) switch_phone.setChecked(true);
            CommonUtils.showErrorMsg(mContext, "You have to allow permission");
        }
    }
    //========6
    private void checkStoragePermission(boolean isChecked, int code) {
        if(isChecked) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, code);
        } else {
            if (p_storage) switch_storage.setChecked(true);
            CommonUtils.showErrorMsg(mContext, "You have to allow permission");
        }
    }
    //========7
    private void checkSipPermission(boolean isChecked, int code) {
        if(isChecked) {
            requestPermissions(
                    new String[]{
                            SipManager.PERMISSION_CONFIGURE_SIP,
                            SipManager.PERMISSION_USE_SIP
                    }, code);

        } else {
            if (p_full_access) switch_storage.setChecked(true);
            CommonUtils.showErrorMsg(mContext, "You have to allow permission");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        switch_call_logs.setChecked(true);
                        p_call_log = true;
                    } else {
                        switch_call_logs.setChecked(false);
                    }
                }
                return;
            case 2:
                if (grantResults.length > 0) {
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        switch_camera.setChecked(true);
                        p_camera = true;
                    } else {
                        switch_camera.setChecked(false);
                    }
                }
                return;
            case 3:
                if (grantResults.length > 0) {
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        switch_contacts.setChecked(true);
                        p_contacts = true;
                    } else {
                        switch_contacts.setChecked(false);
                    }
                }
                return;
            case 4:
                if (grantResults.length > 0) {
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        switch_microphone.setChecked(true);
                        p_microphone = true;
                    } else {
                        switch_microphone.setChecked(false);
                    }
                }
                return;
            case 5:
                if (grantResults.length > 0) {
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        switch_phone.setChecked(true);
                        p_phone = true;
                    } else {
                        switch_phone.setChecked(false);
                    }
                }
                return;
            case 6:
                if (grantResults.length > 0) {
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        switch_storage.setChecked(true);
                        p_storage = true;
                    } else {
                        switch_storage.setChecked(false);
                    }
                }
                return;
            case 7:
                if (grantResults.length > 0) {
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        switch_full_access.setChecked(true);
                        p_full_access = true;
                    } else {
                        switch_full_access.setChecked(false);
                    }
                }
                return;
        }
    }



    private void checkSystemPermission(int code) {//========11
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + mActivity.getApplicationContext().getPackageName()));
            startActivityForResult(intent, code);
        }
    }

    private void checkTopPermission(int code) {//========12
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + mActivity.getApplicationContext().getPackageName()));
            startActivityForResult(intent, code);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(mContext)) {
                    txt_top.setText("Allowed");
                } else {
                    txt_top.setText("not Allowed");
                }
            }
        }
        if (requestCode == 11) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(Settings.System.canWrite(getContext())) {
                    txt_system.setText("Allowed");
                } else {
                    txt_system.setText("not Allowed");
                }
            }
        }

    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back_img:
                ((LoginActivity) getActivity()).updateScreen(new FragmentLicenceKey(), "", false);
                break;
            case R.id.linear_system:
                checkSystemPermission(11);
                break;
            case R.id.linear_top:
                checkTopPermission(12);
                break;
        }
    }
}