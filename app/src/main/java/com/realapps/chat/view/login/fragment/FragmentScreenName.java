package com.realapps.chat.view.login.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


import java.io.Console;
import java.util.Timer;
import java.util.TimerTask;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.ui.db.DBProvider;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.api.SipProfile;
import com.realapps.chat.ui.api.SipUri;
import com.realapps.chat.ui.models.Filter;
import com.realapps.chat.ui.ui.view.kotlin.model.URLCollection;
import com.realapps.chat.ui.utils.PreferencesWrapper;
import com.realapps.chat.ui.wizards.WizardIface;
import com.realapps.chat.ui.wizards.WizardUtils;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.KeyboardUtils;
import com.realapps.chat.utils.NetworkUtils;
import com.realapps.chat.view.home.activity.HomeActivity;
import com.realapps.chat.view.login.activity.LoginActivity;
import com.realapps.chat.ui.helper.PrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Prashant Sharma on 3/15/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class FragmentScreenName extends Fragment {

    private static final String TAG = "FragmentScreenName";
    //Himadri
    String EccID_new;
    private String wizardId = "";
    protected SipProfile account = null;
    private WizardIface wizard = null;
    private static final String THIS_FILE = "Register wizard";


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
    String password;
    String username;
    private Context mContext;
    private Activity mActivity;
    private ProgressDialog mProgressDialoge;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_name, container, false);
        mContext = getActivity();
        mActivity = getActivity();
        unbinder = ButterKnife.bind(this, view);
        Bundle bundle = getArguments();
        if (bundle != null) {
            keyId = bundle.getString(AppConstants.keyId);
            password = bundle.getString("Password");
            username = edtScreenName.getText().toString().trim();
        }

        CommonUtils.hideTextSuggestion(edtScreenName);


        Intent intent = getActivity().getIntent();
        long accountId = intent.getLongExtra(SipProfile.FIELD_ID, SipProfile.INVALID_ID);
        setWizardId(getActivity().getIntent().getStringExtra(SipProfile.FIELD_WIZARD));

        account = SipProfile.getProfileFromDbId(getActivity(), accountId, DBProvider.ACCOUNT_FULL_PROJECTION);
        return view;
    }

    private boolean setWizardId(String wId) {

        WizardUtils.WizardInfo wizardInfo = WizardUtils.getWizardClass(wId);
        if (wizardInfo == null) {
            if (!wizardId.equals(WizardUtils.BASIC_WIZARD_TAG)) //EXPERT_WIZARD_TAG
            {
                return setWizardId(WizardUtils.BASIC_WIZARD_TAG); //EXPERT_WIZARD_TAG
            }
            return false;
        }
        try {
            wizard = (WizardIface) wizardInfo.classObject.newInstance();
        } catch (IllegalAccessException e) {
            Log.e(THIS_FILE, "Can't access wizard class", e);
            if (!wizardId.equals(WizardUtils.EXPERT_WIZARD_TAG)) {
                return setWizardId(WizardUtils.EXPERT_WIZARD_TAG);
            }
            return false;
        } catch (java.lang.InstantiationException e) {
            Log.e(THIS_FILE, "Can't access wizard class", e);
            if (!wizardId.equals(WizardUtils.EXPERT_WIZARD_TAG)) {
                return setWizardId(WizardUtils.EXPERT_WIZARD_TAG);
            }
            return false;
        }
        wizardId = wId;
        return true;
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
                mActivity.onBackPressed();
                break;
            case R.id.btn_save:
                if (NetworkUtils.isNetworkConnected(mContext)) {
                    if (validate()) {
                        KeyboardUtils.hideSoftInput(mActivity);
                        mProgressDialoge = CommonUtils.showLoadingDialog(mContext);
                        //
                        Log.e("Before","call");
                        CheckNamePassword_call();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 5s = 5000ms
                                CheckNamePassword();
                            }
                        }, 5000);




                        // setData();

                    }
                } else {
                    CommonUtils.showErrorMsg(mContext, getString(R.string.no_internet_connection));
                }
                break;
        }
    }

    private void CheckNamePassword() {
        AndroidNetworking.post(ApiEndPoints.add_user_details)
                .addBodyParameter("screen_name", username)
                .addBodyParameter("user_id", keyId)
                .addBodyParameter("device_id", User_settings.getFirebaseToken(mContext))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mProgressDialoge.isShowing())
                            mProgressDialoge.dismiss();

                        try {
                            JSONObject rootObject = new JSONObject(response.toString());
                            if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                JSONObject resultObject = rootObject.getJSONObject("result_data");
                                String keyId = resultObject.getString("UserId");
                                String EccID = resultObject.getString("EccID");



                                String supportEccId = resultObject.optString("support_ecc_id");

                                User_settings.setSupportEccId(mContext, supportEccId);
                                User_settings.savePreferences(User_settings.PREF_HOST_POP, BuildConfig.BASE_URL, mContext);
                                User_settings.savePreferences(User_settings.PREF_HOST_SMTP, BuildConfig.BASE_URL, mContext);


                                User_settings.setUserId(mContext, keyId);
                                User_settings.setECCID(mContext, EccID);
                                User_settings.setScreenName(mContext, username);
                                User_settings.setAppPassword(mContext, password);
                                User_settings.setDuressPassword(mContext, "");
                                User_settings.setTempAttempt(mContext, 0);
                                File attachDir = new File(mContext.getFilesDir().getAbsolutePath() + File.separator + "Shadow_Secure_App");
                                if (!attachDir.exists())
                                    attachDir.mkdirs();

                                User_settings.setAttachDir(mContext, attachDir.getAbsolutePath());
                                User_settings.savePreferences(User_settings.PREF_SHOW_DIALOG, true, mContext);
                                // setDefaultVault();
                                User_settings.setMaxPasswordAttempt(mContext, AppConstants.MAX_PWD_ATTEMPT);
                                User_settings.setEnterKeySend(mContext, AppConstants.NO);
                                //======== notification sound that we set up in the settins page
                                Uri uri = Uri.parse(User_settings.getNotifySoundSelector(mContext));
                                com.realapps.chat.ui.utils.Log.e("=========notify_sound-1", uri.toString());
//                                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                User_settings.setRingtoneSelector(mContext, uri.toString());
                                User_settings.setFontSize(mContext, AppConstants.mediumFont);
                                User_settings.setLockTime(mContext, AppConstants.lockTime);
                                AppConstants.lockscreen = false;
                                AppConstants.onpermission = false;
                                FragmentKeyGeneration keyGenerationFirst = new FragmentKeyGeneration();
                                Bundle argsCompose = new Bundle();
                                argsCompose.putBoolean("loginStatus", true);
                                keyGenerationFirst.setArguments(argsCompose);
                                ((LoginActivity) getActivity()).updateScreen(keyGenerationFirst, "", false);

                            } else {
                                CommonUtils.showInfoMsg(mContext, rootObject.getString("msg"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        if (mProgressDialoge.isShowing())
                            mProgressDialoge.dismiss();
                        CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again));

                    }
                });
    }




    // Added by Marius on 10-03-2020 from call app

    private void CheckNamePassword_call() {
        username = edtScreenName.getText().toString().trim();
        AndroidNetworking.post(ApiEndPoints.add_user_details_call)

                .addBodyParameter("screen_name", username)
                .addBodyParameter("user_id", User_settings.getUserId(mContext))
                .addBodyParameter("device_id", User_settings.getFirebaseToken(mContext))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        android.util.Log.e("CheckNamePassword_call", String.valueOf(response));

                        try {
                            JSONObject rootObject = new JSONObject(response.toString());
                            if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                JSONObject resultObject = rootObject.getJSONObject("result_data");
                                if (resultObject.getString("user_type").equalsIgnoreCase("U")) {


                                    String keyId = resultObject.getString("UserId");
                                    String EccID = resultObject.getString("EccID");

                                    String pgpEmail = resultObject.optString("pgp_email");
                                    String pgpPwd = resultObject.optString("pgp_password");
                                    String supportEccId = resultObject.optString("support_ecc_id");

                                    //saveAndFinish();...
                                    PrefManager prefManager = new PrefManager(getContext());
                                    prefManager.setEccId(EccID);
                                    prefManager.setScreenName(edtScreenName.getText().toString());
                                    //Himadri
                                    //Login API calling for getting token details
                                    EccID_new = EccID;


                                    User_settings.setUserId(mContext, keyId);
                                    User_settings.setECCID(mContext, EccID);
                                    getLoginToken(keyId, EccID, pgpEmail, pgpPwd, supportEccId, response,username);

                               /* User_settings.setSupportEccId(mContext, supportEccId);
                                User_settings.savePreferences(User_settings.PREF_HOST_POP, AppConstants.host_name, mContext);
                                User_settings.savePreferences(User_settings.PREF_HOST_SMTP, AppConstants.host_name, mContext);

                                User_settings.setUserPgpMail(mContext, pgpEmail);
                                User_settings.setUserPgpPassword(mContext, pgpPwd);
                                User_settings.setUserId(mContext, keyId);
                                User_settings.setECCID(mContext, EccID);
                                User_settings.setScreenName(mContext, edtScreenName.getText().toString().trim());
                                User_settings.setAppPassword(mContext, edtPassword.getText().toString().trim());
                                User_settings.setDuressPassword(mContext, "");
                                User_settings.setTempAttempt(mContext, 0);
                                File attachDir = new File(mContext.getFilesDir().getAbsolutePath() + File.separator + "SecretApp");
                                if (!attachDir.exists())
                                    attachDir.mkdirs();

                                User_settings.setAttachDir(mContext, attachDir.getAbsolutePath());
                                User_settings.savePreferences(User_settings.PREF_SHOW_DIALOG, true, mContext);
                                // setDefaultVault();

                                User_settings.setMaxPasswordAttempt(mContext, AppConstants.MAX_PWD_ATTEMPT);
                                User_settings.setEnterKeySend(mContext, AppConstants.NO);
                                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                User_settings.setRingtoneSelector(mContext, uri.toString());
                                User_settings.setFontSize(mContext, AppConstants.mediumFont);
                                User_settings.setLockTime(mContext, AppConstants.lockTime);
                                AppConstants.lockscreen = false;
                                AppConstants.onpermission = false;
                                FragmentKeyGeneration keyGenerationFirst = new FragmentKeyGeneration();
                                Bundle argsCompose = new Bundle();
                                argsCompose.putBoolean("loginStatus", true);
                                keyGenerationFirst.setArguments(argsCompose);
                                ((LoginActivity) getActivity()).updateScreen(keyGenerationFirst, "", false);*/
                                } else {
                                    if (mProgressDialoge.isShowing())
                                        mProgressDialoge.dismiss();
                                    CommonUtils.showInfoMsg(mContext, "Inventory user can't use application, Please contact for support.");
                                }
                            } else {
                                if (mProgressDialoge.isShowing())
                                    mProgressDialoge.dismiss();
                                CommonUtils.showInfoMsg(mContext, rootObject.getString("msg"));
                            }

                        } catch (JSONException e) {
                            if (mProgressDialoge.isShowing())
                                mProgressDialoge.dismiss();
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        if (mProgressDialoge.isShowing())
                            mProgressDialoge.dismiss();
                        CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again));

                    }
                });
    }



    private boolean validate() {
        if (!(CommonUtils.hasText(mContext, edtScreenName, mContext.getString(R.string.screen_name_is_required)))) {
            return false;
        } else {
            return true;
        }
    }


    /*private boolean validate() {
        if (!(CommonUtils.hasText(mContext, edtScreenName, mContext.getString(R.string.screen_name_is_required)))) {
            return false;
        } else if (!(CommonUtils.hasText(mContext, edtPassword, getString(R.string.password_field_is_required)))) {
            return false;
        } else if (!(CommonUtils.checkPasswordLength(mContext, edtPassword, R.string.pass_length_msg))) {
            return false;
        } else if (!(CommonUtils.isPassword(mContext, edtPassword, true))) {
            return false;
        } else if (!(CommonUtils.hasText(mContext, edtConfrmPassword, getString(R.string.confirm_password_field_is_required)))) {
            return false;
        } else if (!CommonUtils.compareText(mContext, edtPassword, edtConfrmPassword, getString(R.string.password_and_confirm_password_does_not_match))) {
            return false;
        } else {
            return true;
        }
    }*/


    void getLoginToken(String uId, final String eccID, final String pgpEmail, final String pgpPwd, final String supportEccId, JSONObject object, String username) {

        String fToken = FirebaseInstanceId.getInstance().getToken();
        if (fToken==null)
            fToken="";
        android.util.Log.e(TAG, "getLoginToken: " + fToken);
        Log.e("===========Login-API-1", EccID_new+"/"+fToken);
        Log.e("===========Login-API-2", ApiEndPoints.END_POINT_GET_CALL_LOGIN_TOKEN);
        AndroidNetworking.post(ApiEndPoints.END_POINT_GET_CALL_LOGIN_TOKEN)
                .addBodyParameter("request", "login")
                .addBodyParameter("username", EccID_new)
                .addBodyParameter("password", EccID_new)
                .addBodyParameter("devicetoken", fToken)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        android.util.Log.e("getLoginToken", String.valueOf(response));
                        try {
                            JSONObject rootObject = new JSONObject(response.toString());
                            if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                JSONObject data = rootObject.getJSONObject("response");
                                String token = data.getString("token");


                                User_settings.setTOKEN(mContext, token);
                                User_settings.setSupportEccId(mContext, supportEccId);
                              /*  User_settings.savePreferences(User_settings.PREF_HOST_POP, AppConstants.host_name, mContext);
                                User_settings.savePreferences(User_settings.PREF_HOST_SMTP, AppConstants.host_name, mContext);
*/
                                User_settings.setUserPgpMail(mContext, pgpEmail);
                                User_settings.setUserPgpPassword(mContext, pgpPwd);
                                User_settings.setUserId(mContext, keyId);
                                User_settings.setECCID(mContext, eccID);

                                //test used to be edtScreenName.getText().toString().trim()
                                User_settings.setScreenName(mContext, FragmentScreenName.this.username);
                                User_settings.setAppPassword(mContext, password);
                                User_settings.setDuressPassword(mContext, "");
                                User_settings.setTempAttempt(mContext, 0);
                                File attachDir = new File(mContext.getFilesDir().getAbsolutePath() + File.separator + "Shadow_Secure_App");
                                if (!attachDir.exists())
                                    attachDir.mkdirs();

                                User_settings.setAttachDir(mContext, attachDir.getAbsolutePath());
                                User_settings.savePreferences(User_settings.PREF_SHOW_DIALOG, true, mContext);
                                // setDefaultVault();

                                User_settings.setMaxPasswordAttempt(mContext, AppConstants.MAX_PWD_ATTEMPT);
                                User_settings.setEnterKeySend(mContext, AppConstants.NO);
                                //======== notification sound that we set up in the settins page
                                Uri uri = Uri.parse(User_settings.getNotifySoundSelector(mContext));

                                com.realapps.chat.ui.utils.Log.e("=========notify_sound-2", uri.toString());
//                                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                User_settings.setRingtoneSelector(mContext, uri.toString());
                                User_settings.setFontSize(mContext, AppConstants.mediumFont);
                                User_settings.setLockTime(mContext, AppConstants.lockTime);
                                AppConstants.lockscreen = false;
                                AppConstants.onpermission = false;

                                saveAndFinish();


                                //Himadri
                                //Function for setting up callerid via API calling
                                // EccID_new = EccID;
                                //test was edtScreenName.getText().toString().trim()
                                setCallerId(token, FragmentScreenName.this.username, EccID_new);


                                // TODO: 11/2/2018 No Need key Generation process for Protext Call
                               /* FragmentKeyGeneration keyGenerationFirst = new FragmentKeyGeneration();
                                Bundle argsCompose = new Bundle();
                                argsCompose.putBoolean("loginStatus", true);
                                keyGenerationFirst.setArguments(argsCompose);
                                ((LoginActivity) getActivity()).updateScreen(keyGenerationFirst, "", false);*/


                                User_settings.setLoginStatus(mContext, true);
                                startActivity(new Intent(getActivity(), HomeActivity.class));

                            }
                            else {
                                registerUser(response, eccID, pgpEmail, pgpPwd, supportEccId, object,username);
                            }

                        } catch (JSONException e) {
                            if (mProgressDialoge.isShowing())
                                mProgressDialoge.dismiss();
                            e.printStackTrace();
                            Log.e("========get_tocken_1", "error-"+e.getMessage());
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        if (mProgressDialoge.isShowing())
                            mProgressDialoge.dismiss();
                        Log.e("========get_tocken_2", "error-"+error.getMessage());
                    }
                });
    }


    //Himadri
    //For calling Login API and getting token
    void setCallerId(String token, String callerName, final String eccID) {
        AndroidNetworking.post(ApiEndPoints.END_POINT_GET_CALL_LOGIN_TOKEN)
                .addBodyParameter("request", "add_callerid")
                .addBodyParameter("token", token)
                .addBodyParameter("display_name", callerName)
                .addBodyParameter("display_number", EccID_new)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mProgressDialoge.isShowing())
                            mProgressDialoge.dismiss();

                        try {
                            JSONObject rootObject = new JSONObject(response.toString());
                            Log.d("CallerId response: ", rootObject.toString());


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        if (mProgressDialoge.isShowing())
                            mProgressDialoge.dismiss();


                    }
                });
    }


    public SipProfile newBuildAccount(SipProfile account) {
        Log.d(THIS_FILE, "begin of save ....");
        account.display_name = EccID_new;//"9393939393";
        account.username = EccID_new;//"9393939393";

        // check for the port if included
        if (URLCollection.obj.SERVER_SIP_IP_WITH_PORT.contains(":")) {
            String[] serverParts = URLCollection.obj.SERVER_SIP_IP_WITH_PORT.split(":");
            account.acc_id = "<sip:" + SipUri.encodeUser(EccID_new) + "@" + serverParts[0].trim() + ":" + serverParts[1].trim() + ">";
        } else {
            //String[] serverParts = serverIp.getText().toString().split(":");
            account.acc_id = "<sip:" + SipUri.encodeUser(EccID_new) + "@" + URLCollection.obj.SERVER_SIP_IP_WITH_PORT.trim() + ">";
        }
        String regUri = "sip:" + URLCollection.obj.SERVER_SIP_IP_WITH_PORT;
        account.reg_uri = regUri;

        //For SRTP/TLS
        account.realm = "*";
        account.realm = "*";
        account.username = EccID_new;//"9393939393";
        account.data = EccID_new;//"9393939393";
        account.scheme = SipProfile.CRED_SCHEME_DIGEST;
        account.datatype = SipProfile.CRED_DATA_PLAIN_PASSWD;
        account.initial_auth = false;
        account.auth_algo = "";
        //By default auto transport
        account.transport = SipProfile.TRANSPORT_TLS;
        account.default_uri_scheme = "sip";
        account.ipv6_media_use = 0;
        account.publish_enabled = 0;
        account.reg_timeout = 900;
        account.reg_delay_before_refresh = -1;
        account.contact_rewrite_method = 2;
        account.allow_contact_rewrite = false;
        account.try_clean_registers = 1;
        account.proxies = new String[]{regUri};
        account.use_srtp = 2;
        account.use_zrtp = -1;
        account.rtp_public_addr = "";
        account.rtp_bound_addr = "";
        account.rtp_enable_qos = -1;
        account.rtp_qos_dscp = -1;
        account.vm_nbr = "";
        account.mwi_enabled = true;
        account.vid_in_auto_show = -1;
        account.vid_out_auto_transmit = -1;
        account.use_rfc5626 = true;
        account.rfc5626_instance_id = "<urn:uuid:ab614474-4ad0-4c25-8e98-307ebdbcc3de>";
        account.rfc5626_reg_id = "";
        account.allow_sdp_nat_rewrite = false;
        account.sip_stun_use = -1;
        account.media_stun_use = -1;
        account.ice_cfg_use = -1;
        account.ice_cfg_enable = 0;
        account.turn_cfg_use = -1;
        account.turn_cfg_enable = 0;
        account.turn_cfg_server = "";
        account.turn_cfg_user = "";
        account.turn_cfg_password = "";

        return account;
    }

    private void applyNewAccountDefault(SipProfile account) {
        if (account.use_rfc5626) {
            if (TextUtils.isEmpty(account.rfc5626_instance_id)) {
                String autoInstanceId = (UUID.randomUUID()).toString();
                account.rfc5626_instance_id = "<urn:uuid:" + autoInstanceId + ">";
            }
        }
    }

    public void saveAndFinish() {
        saveAccount();
    }

    private void saveAccount() {
        saveAccount(wizardId);
    }

    private void saveAccount(String wizardId) {
        boolean needRestart = false;

        PreferencesWrapper sh_Pref = new PreferencesWrapper(getActivity());
        account = newBuildAccount(account);//buildAccount(account);
        account.wizard = wizardId;
        if (account.id == SipProfile.INVALID_ID) {
            // This account does not exists yet
            //Toast.makeText(ctx, "acc Id"+account.id, Toast.LENGTH_LONG).show();
            sh_Pref.startEditing();
            wizard.setDefaultParams(sh_Pref);
            sh_Pref.endEditing();
            applyNewAccountDefault(account);
            Uri uri = getActivity().getContentResolver().insert(SipProfile.ACCOUNT_URI, account.getDbContentValues());

            // After insert, add filters for this wizard
            account.id = ContentUris.parseId(uri);
            List<Filter> filters = wizard.getDefaultFilters(account);
            if (filters != null) {
                for (Filter filter : filters) {
                    // Ensure the correct id if not done by the wizard
                    filter.account = (int) account.id;
                    getActivity().getContentResolver().insert(SipManager.FILTER_URI, filter.getDbContentValues());
                }
            }
            // Check if we have to restart
            needRestart = wizard.needRestart();

        } else {
            // TODO : should not be done there but if not we should add an
            // option to re-apply default params
            //Toast.makeText(ctx, "Else "+account.id, Toast.LENGTH_LONG).show();
            sh_Pref.startEditing();
            wizard.setDefaultParams(sh_Pref);
            sh_Pref.endEditing();
            getActivity().getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, account.id), account.getDbContentValues(), null, null);
        }

        // Mainly if global preferences were changed, we have to restart sip stack
        if (needRestart) {
            Intent intent = new Intent(SipManager.ACTION_SIP_REQUEST_RESTART);
            getActivity().sendBroadcast(intent);
        }

        FragmentScreenName screenName = new FragmentScreenName();
        Bundle argsCompose = new Bundle();
        argsCompose.putString(AppConstants.keyId, keyId);
        screenName.setArguments(argsCompose);
        ((LoginActivity) getActivity()).updateScreen(screenName, "", true);
    }


    /*private boolean validate() {
        if (!(CommonUtils.hasText(mContext, edtScreenName, mContext.getString(R.string.screen_name_is_required)))) {
            return false;
        } else if (!(CommonUtils.hasText(mContext, edtPassword, getString(R.string.password_field_is_required)))) {
            return false;
        } else if (!(CommonUtils.isPassword(mContext, edtPassword, true))) {
            return false;
        } else if (!(CommonUtils.hasText(mContext, edtConfrmPassword, getString(R.string.confirm_password_field_is_required)))) {
            return false;
        } else if (!CommonUtils.compareText(mContext, edtPassword, edtConfrmPassword, getString(R.string.password_and_confirm_password_does_not_match))) {
            return false;
        } else {
            return true;
        }
    }*/

//    private boolean validate() {
//        if (!(CommonUtils.hasText(mContext, edtScreenName, mContext.getString(R.string.screen_name_is_required)))) {
//            return false;
//        } else {
//            return true;
//        }
//    }

    //  ********************************************************APIS***************************************************************

    private void registerUser(JSONObject jsonObject, String eccID, String pgpEmail, String pgpPwd, String supportEccId, JSONObject object, String username) {

        this.username = username;
        AndroidNetworking.post(ApiEndPoints.END_POINT_GET_CALL_LOGIN_TOKEN)
                .addBodyParameter("request", "signup")
                .addBodyParameter("username", EccID_new)
                .addBodyParameter("secret", EccID_new)
                .addBodyParameter("first_name", this.username)
                .addBodyParameter("last_name", EccID_new)
                .addBodyParameter("email", pgpEmail)
                .addBodyParameter("display_name", this.username)
                .addBodyParameter("display_number", EccID_new)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        android.util.Log.e("registerUser: ", response.toString());
                        try {
                            JSONObject rootObject = new JSONObject(response.toString());
                            if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                JSONObject data = rootObject.getJSONObject("response");
                                String token = data.getString("token");
                                creditBlance(token, eccID, pgpEmail, pgpPwd, supportEccId, object,username);
                            } else {
                                if (mProgressDialoge.isShowing())
                                    mProgressDialoge.dismiss();
                                CommonUtils.showInfoMsg(mContext, response.getString("msg"));
                            }

                        } catch (JSONException e) {
                            if (mProgressDialoge.isShowing())
                                mProgressDialoge.dismiss();
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        if (mProgressDialoge.isShowing())
                            mProgressDialoge.dismiss();


                    }
                });
    }


    private void creditBlance(String token, String eccID, String pgpEmail, String pgpPwd, String supportEccId, JSONObject object, String username) {
        AndroidNetworking.post(ApiEndPoints.END_POINT_GET_CALL_LOGIN_TOKEN)
                .addBodyParameter("request", "set_userbalance")
                .addBodyParameter("token", token)
                .addBodyParameter("balance", getBalanceDetail(object))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        android.util.Log.e("creditBlance: ", response.toString());
                        if (mProgressDialoge.isShowing())
                            mProgressDialoge.dismiss();

                        try {
                            JSONObject rootObject = new JSONObject(response.toString());
                            if (rootObject.getString("status").equalsIgnoreCase("1")) {

                                User_settings.setTOKEN(mContext, token);
                                User_settings.setSupportEccId(mContext, supportEccId);
                                /*User_settings.savePreferences(User_settings.PREF_HOST_POP, AppConstants.host_name, mContext);
                                User_settings.savePreferences(User_settings.PREF_HOST_SMTP, AppConstants.host_name, mContext);
*/
                                User_settings.setUserPgpMail(mContext, pgpEmail);
                                User_settings.setUserPgpPassword(mContext, pgpPwd);
                                User_settings.setUserId(mContext, keyId);
                                User_settings.setECCID(mContext, eccID);
                                User_settings.setScreenName(mContext, username);
                                User_settings.setAppPassword(mContext, password);
                                User_settings.setDuressPassword(mContext, "");
                                User_settings.setTempAttempt(mContext, 0);
                                File attachDir = new File(mContext.getFilesDir().getAbsolutePath() + File.separator + "Shadow_Secure_App");
                                if (!attachDir.exists())
                                    attachDir.mkdirs();

                                User_settings.setAttachDir(mContext, attachDir.getAbsolutePath());
                                User_settings.savePreferences(User_settings.PREF_SHOW_DIALOG, true, mContext);
                                // setDefaultVault();

                                User_settings.setMaxPasswordAttempt(mContext, AppConstants.MAX_PWD_ATTEMPT);
                                User_settings.setEnterKeySend(mContext, AppConstants.NO);
                                //======== notification sound that we set up in the settins page
                                Uri uri = Uri.parse(User_settings.getNotifySoundSelector(mContext));
                                Log.e("=========notify_sound-3", uri.toString());
//                                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                User_settings.setRingtoneSelector(mContext, uri.toString());
                                User_settings.setFontSize(mContext, AppConstants.mediumFont);
                                User_settings.setLockTime(mContext, AppConstants.lockTime);
                                AppConstants.lockscreen = false;
                                AppConstants.onpermission = false;

                                saveAndFinish();

                                //Himadri
                                //Function for setting up callerid via API calling
                                //EccID_new = EccID;
                                setCallerId(token, FragmentScreenName.this.username, EccID_new);


                                User_settings.setLoginStatus(mContext, true);
                                startActivity(new Intent(getActivity(), HomeActivity.class));

                            } else {
                                //  CommonUtils.showInfoMsg(mContext, rootObject.getString("msg"));
                                CommonUtils.showInfoMsg(mContext, response.getString("msg"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        if (mProgressDialoge.isShowing())
                            mProgressDialoge.dismiss();


                    }
                });
    }

    private String getBalanceDetail(JSONObject response) {
        String blance = "";
        try {
            JSONObject object = response.getJSONObject("result_data");

            if (object.getString("subscription_period").equalsIgnoreCase("3")) {
                blance = "60";
            } else if (object.getString("subscription_period").equalsIgnoreCase("6")) {
                blance = "120";
            } else {
                blance = "240";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return blance;
    }
}
