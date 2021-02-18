package com.realapps.chat.view.login.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.realapps.chat.R;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.KeyboardUtils;
import com.realapps.chat.view.login.activity.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentPassword extends Fragment {


    @BindView(R.id.edt_screen_name)
    EditText edtScreenName;
    @BindView(R.id.edt_password)
    EditText edtPassword;
    @BindView(R.id.edt_confrm_password)
    EditText edtConfrmPassword;
    @BindView(R.id.btn_cancel)
    Button btnCancel;
    @BindView(R.id.btn_save)
    Button btnSave;
    Unbinder unbinder;
    String keyId;
    private Context mContext;
    private Activity mActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen_name, null);
        mContext = getActivity();
        mActivity = getActivity();
        unbinder = ButterKnife.bind(this, view);
        Bundle bundle = getArguments();
        if (bundle != null) {
            keyId = bundle.getString(AppConstants.keyId);
        }
        CommonUtils.hideTextSuggestion(edtScreenName);
        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.btn_cancel, R.id.btn_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                getActivity().onBackPressed();
                break;
            case R.id.btn_save:
                if (validate()) {
                    KeyboardUtils.hideSoftInput(mActivity);
                    FragmentScreenName screenName = new FragmentScreenName();
                    Bundle argsCompose = new Bundle();
                    argsCompose.putString(AppConstants.keyId, keyId);
                    argsCompose.putString("Password", edtPassword.getText().toString().trim());
                    screenName.setArguments(argsCompose);
                    ((LoginActivity) getActivity()).updateScreen(screenName, "", false);
                }

                break;
        }
    }

    private boolean validate() {
        if (!(CommonUtils.hasText(mContext, edtPassword, AppConstants.Password_MSG))) {
            return false;
        } else if (!(CommonUtils.checkPasswordLength(mContext, edtPassword, R.string.pass_length_msg))) {
            return false;
        } else if (!(CommonUtils.isPassword(mContext, edtPassword, true))) {
            return false;
        } else if (!(CommonUtils.hasText(mContext, edtConfrmPassword, AppConstants.Repeat_Password_MSG))) {
            return false;
        } else
            return CommonUtils.compareText(mContext, edtPassword, edtConfrmPassword, AppConstants.CurrentPasswordNotMatch_MSG);
    }
}
