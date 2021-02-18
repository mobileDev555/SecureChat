package com.realapps.chat.view.login.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.didisoft.pgp.PGPException;
import com.realapps.chat.R;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.NetworkUtils;
import com.realapps.chat.view.home.activity.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Prashant Sharma on 3/15/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class FragmentKeyGeneration extends Fragment {


    private static final String TAG = "FragmentKeyGeneration";
    public static boolean splashScreen = false;
    private Context mContext;
    private Activity mActivity;
    private boolean status = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_key_generation, container, false);
        mActivity = getActivity();
        mContext = getContext();
        Bundle bundle = getArguments();
        if (bundle != null) {
            status = bundle.getBoolean("loginStatus");
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        splashScreen = false;

        initView();
    }

    private void initView() {
        try {
            if (new CommonUtils().exportECCKey(mContext)) {
                if (NetworkUtils.isNetworkConnected(mContext)) {
                    uploadECCKey();
                } else {
                    CommonUtils.showErrorMsg(mContext, getString(R.string.no_internet_connection));
                }
            } else {
                splashScreen = true;
                CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again));
                mActivity.onBackPressed();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PGPException e) {
            e.printStackTrace();
        }
    }

    void uploadECCKey() {
        String basePath = CommonUtils.getKeyBasePath(mContext);
        Log.e(TAG, "uploadECCKey: " + basePath);
        String publicKey = CommonUtils.readKeyString(basePath + AppConstants.pubECCKeyName);
        Log.e(TAG, "uploadECCKey: " + publicKey);
        String email = CommonUtils.getUserEmail(User_settings.getECCID(mContext));
        keyGenerationApi(email, publicKey);

    }

    protected void keyGenerationApi(String email, String publicKey) {
        AndroidNetworking.post(ApiEndPoints.EccUpdate)
                .addBodyParameter("email", email)
                .addBodyParameter("ecc_key", publicKey)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject rootObject = new JSONObject(response.toString());

                            if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                User_settings.setLoginStatus(mContext, true);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        startActivity(new Intent(getActivity(), HomeActivity.class));
                                    }
                                }, 2000);

                            } else {
                                splashScreen = true;
                                CommonUtils.showErrorMsg(mContext, getString(R.string.please_try_again_later));
                                mActivity.onBackPressed();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        splashScreen = true;
                        CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again));
                        mActivity.onBackPressed();
                        return;
                    }
                });
    }
}
