package com.realapps.chat.view.login.fragment;

import android.Manifest;
import android.app.Activity;
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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.realapps.chat.R;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.KeyboardUtils;
import com.realapps.chat.view.login.activity.LoginActivity;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Prashant Sharma on 3/15/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class FragmentSplash extends Fragment {


    private Context mContext;
    private Activity mActivity;
    private long splashTime = 1500;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spalsh, container, false);
        mActivity = getActivity();
        mContext = getContext();
        KeyboardUtils.hideSoftInput(mActivity);
        initView();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initView() {
        new Handler().postDelayed(() -> {
            if (getActivity() != null) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    String pkg = mContext.getPackageName();
                    PowerManager pm = mContext.getSystemService(PowerManager.class);

                    if (!pm.isIgnoringBatteryOptimizations(pkg)) {
                        Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:" + pkg));
                        Objects.requireNonNull(getActivity()).startActivityForResult(i, AppConstants.REQUEST_CODE_POWER_OPTIMIZATION);
                    }
//                    else {
//                        TedPermission.with(mContext)
//                                .setDeniedMessage(getString(R.string.if_you_reject_permission_you_can_not_use_this_service_n_nplease_turn_on_permissions_at_setting_permission))
//                                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                                .setPermissionListener(new PermissionListener() {
//                                    @Override
//                                    public void onPermissionGranted() {
//                                        try {
//                                            if (User_settings.getUserId(mContext).length() > 0)
//                                                ((LoginActivity) getActivity()).updateScreen(new FragmentLock(), "", false);
//                                            else
//                                                ((LoginActivity) getActivity()).updateScreen(new FragmentLicenceKey(), "", false);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
//                                        CommonUtils.showErrorMsg(mContext, getString(R.string.please_accept_the_permissions));
//                                    }
//                                })
//                                .check();
//                    }
                }
                if (User_settings.getUserId(mContext).length() > 0)
                    ((LoginActivity) getActivity()).updateScreen(new FragmentLock(), "", false);
                else
                    ((LoginActivity) getActivity()).updateScreen(new FragmentLicenceKey(), "", false);
            }


        }, splashTime/* 1.5 sec delay */);

    }

}
