package com.realapps.chat.view.home.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.realapps.chat.R;
import com.realapps.chat.RealAppsChat;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.parser.PublicKeysParser;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.interfaces.AppUnlockDialogResponse;
import com.realapps.chat.interfaces.ResponseSound;
import com.realapps.chat.interfaces.ScreenNameChangeDialogResponse;
import com.realapps.chat.ui.utils.Log;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DbConstants;
import com.realapps.chat.utils.KeyboardUtils;
import com.realapps.chat.view.dialoges.DialogChangeScreenName;
import com.realapps.chat.view.dialoges.DialogLastAttempt;
import com.realapps.chat.view.home.activity.HomeActivity;
import com.realapps.chat.view.lock.Lock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Prashant Sharma on 3/15/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class FragmentSettings extends Fragment implements ResponseSound {


    public static ResponseSound responseSound;
    int fontid;
    int lockid;
    int langId;
    String lan;

    @BindView(R.id.lyr_profile_settings)
    LinearLayout lyrProfileSettings;
    //Added by Marius to be continued

    //@BindView(R.id.remaining_days)
   // LinearLayout remaining_days;

    @BindView(R.id.txt_ecc_id)
    TextView txtEccId;

    @BindView(R.id.txt_days_remaining)
    TextView txtDaysRemaining;

    @BindView(R.id.txt_pwd_attemps)
    TextView txtPwdAttemps;

    @BindView(R.id.lyr_pwd_attempts)
    LinearLayout lyrPwdAttempts;

    @BindView(R.id.seek_bar_pwd_attempts)
    SeekBar seekBarPwdAttempts;

    @BindView(R.id.txt_save_pwd_attemps)
    TextView txtSavePwdAttemps;

    @BindView(R.id.lyr_pwd_attmepts_seekbar)
    LinearLayout lyrPwdAttmeptsSeekbar;

    @BindView(R.id.mainLinear)
    LinearLayout mainLinear;

    @BindView(R.id.lyr_profile_child)
    LinearLayout lyrProfileChild;

    @BindView(R.id.lyr_account_settings)
    LinearLayout lyrAccountSettings;

    @BindView(R.id.txt_account_password)
    TextView txtAccountPassword;

    @BindView(R.id.lyr_account_password)
    LinearLayout lyrAccountPassword;

    @BindView(R.id.txt_current_pwd)
    EditText txtCurrentPwd;

    @BindView(R.id.txt_new_pwd)
    EditText txtNewPwd;

    @BindView(R.id.txt_confirm_pwd)
    EditText txtConfirmPwd;

    @BindView(R.id.btn_save_account_pwd)
    Button btnSaveAccountPwd;

    @BindView(R.id.lyr_account_password_child)
    LinearLayout lyrAccountPasswordChild;

    @BindView(R.id.txt_duress_password)
    TextView txtDuressPassword;

    @BindView(R.id.lyr_duress_password)
    LinearLayout lyrDuressPassword;

    @BindView(R.id.txt_duress_pwd)
    EditText txtDuressPwd;

    @BindView(R.id.btn_save_duress_pwd)
    Button btnSaveDuressPwd;

    @BindView(R.id.lyr_duress_password_child)
    LinearLayout lyrDuressPasswordChild;

    @BindView(R.id.txt_ecc_public_key)
    TextView txtEccPublicKey;

    @BindView(R.id.lyr_eec_key)
    LinearLayout lyrEecKey;

    @BindView(R.id.txt_app_version)
    TextView txtAppVersion;

    @BindView(R.id.txt_data_wipe)
    TextView txtDataWipe;

    Unbinder unbinder;

    @BindView(R.id.lyr_child_account_settings)
    LinearLayout lyrChildAccountSettings;

    String maxAttempts;

    @BindView(R.id.img_profile_expand)
    ImageView imgProfileExpand;

    @BindView(R.id.img_maxpass_expand)
    ImageView imgMaxpassExpand;

    @BindView(R.id.img_account_expand)
    ImageView imgAccountExpand;

    @BindView(R.id.img_actpass_expand)
    ImageView imgActpassExpand;

    @BindView(R.id.img_duress_expand)
    ImageView imgDuressExpand;
    @BindView(R.id.checkbox)
    CheckBox checkbox;
    @BindView(R.id.lyr_enter_send_key)
    LinearLayout lyrEnterSendKey;
    @BindView(R.id.txt_ringtone)
    TextView txtRingtone;
    @BindView(R.id.txt_notify_sound)
    TextView txtNotifySound;
    @BindView(R.id.txt_name)
    TextView txtName;
    @BindView(R.id.txt_font)
    TextView txtFont;
    @BindView(R.id.txt_lock_time)
    TextView txtLockTime;
    @BindView(R.id.txt_language)
    TextView txtLanguage;
    @BindView(R.id.lyr_lang)
    LinearLayout lyrLang;
    @BindView(R.id.scrollView)
    NestedScrollView scrollView;
    @BindView(R.id.txt_clear_cache)
    TextView txtClearCache;
    @BindView(R.id.txt_lock)
    TextView txtLock;

    DbHelper db;
    private ProgressDialog progressDialog;


    private Context mContext;
    private Activity mActivity;

    private String blockCharacterSet = ".1234567890";

    private InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mContext = getContext();
        mActivity = getActivity();
        responseSound = this;
        unbinder = ButterKnife.bind(this, view);
        db = new DbHelper(mContext);
        setData();


        seekBarPwdAttempts.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txtPwdAttemps.setText(getString(R.string.d_attempts, i + 3));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        checkbox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                checkbox.setChecked(true);
                User_settings.setEnterKeySend(mContext, AppConstants.YES);
            } else {
                checkbox.setChecked(false);
                User_settings.setEnterKeySend(mContext, AppConstants.NO);
            }
        });


        scrollToTop();

        return view;
    }

    private void scrollToTop() {
        scrollView.smoothScrollTo(0, scrollView.getBottom());
    }

    private void setDaysRemainingLicense(){
        int totalDays = (int) CommonUtils.getTotalDays(User_settings.getEndDateLicense(mContext));
        txtDaysRemaining.setText(totalDays+"");

    }
    private void setData() {
        scrollView.setFocusableInTouchMode(true);
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);


        //   txtDuressPwd.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(6)});
        setDaysRemainingLicense();
        if (User_settings.getLockTime(mContext) == AppConstants.lockTime) {
            txtLockTime.setText(getString(R.string.immediately));
        } else if (User_settings.getLockTime(mContext) == AppConstants.lockTime1minute) {
            txtLockTime.setText("1 minute");
        } else if (User_settings.getLockTime(mContext) == AppConstants.lockTime2minute) {
            txtLockTime.setText("2 minute");
        } else {
            txtLockTime.setText("3 minute");
        }

        if (User_settings.getfont(mContext) == AppConstants.smallFont) {
            txtFont.setText(getString(R.string.small));
        } else if (User_settings.getfont(mContext) == AppConstants.mediumFont) {
            txtFont.setText(getString(R.string.medium));
        } else {
            txtFont.setText(getString(R.string.large));
        }
        // seekBarPwdAttempts.setProgress(Integer.parseInt(new User_settings().getMaxPasswordAttempt(mContext)));

//        //======= Ringtone Name
//        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//        Ringtone r = RingtoneManager.getRingtone(mContext, uri);
//        String ringtone_title = r.getTitle(mContext);
//        ringtone_title = ringtone_title.replace("Default", "");
//        ringtone_title = ringtone_title.replace("(", "");
//        ringtone_title = ringtone_title.replace(")", "");
//        txtRingtone.setText(ringtone_title);
//
//        //======= Notification Sound Name
//        Uri uri_notify= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        Ringtone r_notify = RingtoneManager.getRingtone(mContext, uri_notify);
//        String notify_sound_title = r_notify.getTitle(mContext);
//        notify_sound_title = notify_sound_title.replace("Default", "");
//        notify_sound_title = notify_sound_title.replace("(", "");
//        notify_sound_title = notify_sound_title.replace(")", "");
//        txtNotifySound.setText(notify_sound_title);

        txtEccId.setText(User_settings.getECCID(mContext));
        txtPwdAttemps.setText(getString(R.string.d_attempts, User_settings.getMaxPasswordAttempt(mContext)));
        txtEccPublicKey.setText(CommonUtils.getEccPublicKey(mContext));
        txtAppVersion.setText(CommonUtils.getAppVersion(mContext));
        seekBarPwdAttempts.setProgress(User_settings.getMaxPasswordAttempt(mContext) - 3);
        txtName.setText(User_settings.getScreenName(mContext));

        if (User_settings.getEnterKeySend(mContext) == AppConstants.YES) {
            checkbox.setChecked(true);
        } else {
            checkbox.setChecked(false);
        }

        if (User_settings.getLanguage(mContext) != null && User_settings.getLanguage(mContext).length() > 0) {
            txtLanguage.setText(CommonUtils.getLanguageStr(User_settings.getLanguage(mContext)));
        } else {
            txtLanguage.setText(getString(R.string.default_word));
        }

        txtClearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(getString(R.string.clear_cache));
                builder.setMessage("Are you sure to clear cache.");
                builder.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> clearCache());
                builder.setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                });
                builder.show();
            }
        });

        txtLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Lock.getInstance(getContext()).lockApplication();
            }
        });
    }
    public JSONObject getRawData() {
        List<String> eccId = db.getECCid();

        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray eccIdArray = new JSONArray();
            if (eccId.size() > 0) {
                for (int i = 0; i < eccId.size(); i++) {
                    JSONObject singleECCIdObject = new JSONObject();
                    String eccID = eccId.get(i);
                    singleECCIdObject.put("ecc_id", eccID);
                    eccIdArray.put(singleECCIdObject);
                }
            }
            jsonObject.put("ecc_data", eccIdArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void clearCache() {
        progressDialog = CommonUtils.showLoadingDialog(mContext);

        AndroidNetworking.post(ApiEndPoints.URL_FETCH_GROUP_ECC_KEYS)
                .addJSONObjectBody(getRawData())
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (progressDialog != null) {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }

                        try {
                            new PublicKeysParser().parseJsonKey(mContext, response.toString());

                            CommonUtils.showInfoMsg(mContext, getString(R.string.cache_cleared_successfully));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        if (progressDialog != null) {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                        CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again));
                    }
                });

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void lockTimeDialogue() {
        if (User_settings.getLockTime(mContext) == AppConstants.lockTime) {
            lockid = 0;
        } else if (User_settings.getLockTime(mContext) == AppConstants.lockTime1minute) {
            lockid = 1;
        } else if (User_settings.getLockTime(mContext) == AppConstants.lockTime2minute) {
            lockid = 2;
        } else {
            lockid = 3;
        }
        final CharSequence lockTime[] = new CharSequence[]{getString(R.string.immediately), "1 Minute", "2 Minute", "3 Minute"};
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(mContext, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(mContext);
        }

        builder.setTitle(getString(R.string.lock_time_colon));
        builder.setSingleChoiceItems(lockTime, lockid, (dialogInterface, i) -> lockid = i);
        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
            switch (lockid) {
                case 0:
                    txtLockTime.setText(R.string.immediately);
                    User_settings.setLockTime(mContext, AppConstants.lockTime);
                    break;
                case 1:
                    txtLockTime.setText("1 Minute");
                    User_settings.setLockTime(mContext, AppConstants.lockTime1minute);
                    break;
                case 2:
                    txtLockTime.setText("2 Minute");
                    User_settings.setLockTime(mContext, AppConstants.lockTime2minute);
                    break;
                case 3:
                    txtLockTime.setText("3 Minute");
                    User_settings.setLockTime(mContext, AppConstants.lockTime3minute);
                    break;
            }

            dialogInterface.cancel();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

    }

    @OnClick({R.id.change_phone_password, R.id.txt_name, R.id.lyr_profile_settings, R.id.lyr_pwd_attempts, R.id.txt_save_pwd_attemps, R.id.lyr_pwd_attmepts_seekbar,
            R.id.lyr_account_settings, R.id.lyr_account_password, R.id.btn_save_account_pwd, R.id.lyr_duress_password, R.id.btn_save_duress_pwd,
            R.id.lyr_duress_password_child, R.id.lyr_enter_send_key, R.id.lyr_ringtone_selector, R.id.lyr_notify_sound_selector, R.id.lyr_font_selector, R.id.lyr_lock_time, R.id.lyr_lang,
            R.id.txt_current_pwd, R.id.txt_new_pwd, R.id.txt_confirm_pwd, R.id.txt_duress_pwd, R.id.lyr_data_wipe})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.change_phone_password:
            Intent change = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
            startActivity(change);
            case R.id.txt_current_pwd:
                txtCurrentPwd.setFocusable(true);
                break;
            case R.id.txt_name:
                changeName();
                break;
            case R.id.txt_new_pwd:
                break;
            case R.id.txt_confirm_pwd:
                break;
            case R.id.txt_duress_pwd:
                break;
            case R.id.lyr_lock_time:
                lockTimeDialogue();
                break;
            case R.id.lyr_profile_settings:
                visibleProfileSettings();
                break;
            case R.id.lyr_pwd_attempts:
                hideShowPasswordAttemptsLayout();
                break;
            case R.id.txt_save_pwd_attemps:
                User_settings.setMaxPasswordAttempt(mContext, (seekBarPwdAttempts.getProgress()) + 3);
                setData();
                CommonUtils.showInfoMsg(mContext, getString(R.string.account_updated_successfully));
                break;
            case R.id.lyr_pwd_attmepts_seekbar:
                break;
            case R.id.lyr_account_settings:
                hideShowAccountSettingsLayout();
                break;
            case R.id.lyr_account_password:
                hideShowAccountPasswordLayout();
                break;
            case R.id.btn_save_account_pwd:
                if (validate()) {
                    User_settings.setAppPassword(mContext, txtNewPwd.getText().toString());
                    CommonUtils.showInfoMsg(mContext, getString(R.string.account_updated_successfully));
                    resetAccount();
                    hideShowAccountPasswordLayout();
                }

                break;
            case R.id.lyr_duress_password:
                hideShowDuressPasswordLayout();
                break;
            case R.id.btn_save_duress_pwd:
                if ((duressValidity())) {
                    User_settings.setDuressPassword(mContext, txtDuressPwd.getText().toString());
                    CommonUtils.showInfoMsg(mContext, getString(R.string.account_updated_successfully));
                    txtDuressPwd.setText("");
                    hideShowDuressPasswordLayout();
                }

                break;
            case R.id.lyr_duress_password_child:
                break;
            case R.id.lyr_enter_send_key:
                setSendEnterMessage();
                break;
            case R.id.lyr_ringtone_selector:
                setRingtone();
                break;
            case R.id.lyr_notify_sound_selector:
                setNotifySound();
                break;
            case R.id.lyr_font_selector:
                setFont();
                break;

            case R.id.lyr_lang:
                setLanguage();
                break;
            case R.id.lyr_data_wipe:
                clearLocalDatabase();
                break;
        }
    }

    private void clearLocalDatabase() {
            DialogLastAttempt dialogLastAttempt =
                    new DialogLastAttempt(mContext, mContext.getString(R.string.ShadowSecureWipeData), new AppUnlockDialogResponse() {
                        @Override
                        public void appUnlock() {
                        }
                        @Override
                        public void cancel() {
                        }
                    });
            dialogLastAttempt.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialogLastAttempt.show();
    }

    private void setLanguage() {
        lan = User_settings.getLanguage(mContext);
        if (lan != null && lan.length() > 0) {
            if (lan.equalsIgnoreCase("en")) {
                langId = 0;
            } else if (lan.equalsIgnoreCase("nl")) {
                langId = 1;
            } else if (lan.equalsIgnoreCase("es")) {
                langId = 2;
            } else if (lan.equalsIgnoreCase("tr")) {
                langId = 3;
            } else if (lan.equalsIgnoreCase("de")) {
                langId = 4;
            } else if (lan.equalsIgnoreCase("fr")) {
                langId = 5;
            } else if (lan.equalsIgnoreCase("it")) {
                langId = 6;
            } else if (lan.equalsIgnoreCase("pl")) {
                langId = 7;
            } else if (lan.equalsIgnoreCase("sv")) {
                langId = 8;
            } else if (lan.equalsIgnoreCase("ru")) {
                langId = 9;
            } else if (lan.equalsIgnoreCase("pt")) {
                langId = 10;
            } else if (lan.equalsIgnoreCase("zh")) {
                langId = 11;
            }
        }
        final CharSequence langs[] = new CharSequence[]{"ENGLISH", "NEDERLANDS", "ESPANOL", "TURKCE", "DEUTSCHE", "FRANCAISE", "ITALIANO", "POLSKI", "SVENSKA", "RYSKA", "PORTUGUES", "MANDARIN"};

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(mContext, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(mContext);
        }


        builder.setTitle(getString(R.string.change_language_colon));
        builder.setSingleChoiceItems(langs, langId, (dialogInterface, i) -> langId = i);
        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
            switch (langId) {
                case 0:
                    txtLanguage.setText("ENGLISH");
                    break;
                case 1:
                    txtLanguage.setText("NEDERLANDS");
                    break;
                case 2:
                    txtLanguage.setText("ESPANOL");
                    break;
                case 3:
                    txtLanguage.setText("TURKCE");
                    break;
                case 4:
                    txtLanguage.setText("DEUTSCHE");
                    break;
                case 5:
                    txtLanguage.setText("FRANCAISE");
                    break;
                case 6:
                    txtLanguage.setText("ITALIANO");
                    break;
                case 7:
                    txtLanguage.setText("POLSKI");
                    break;
                case 8:
                    txtLanguage.setText("SVENSKA");
                    break;
                case 9:
                    txtLanguage.setText("RYSKA");
                    break;
                case 10:
                    txtLanguage.setText("PORTUGUES");
                    break;
                case 11:
                    txtLanguage.setText("MANDARIN");
                    break;
            }
            User_settings.setlanguage(mContext, CommonUtils.getLanguageCode(langId));
            ((RealAppsChat) getActivity().getApplication()).changeLanguage();
            refresh();
            dialogInterface.cancel();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void refresh() {

        Intent intent = new Intent(getContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }


    private void setFont() {
        fontid = User_settings.getfont(mContext);
        final CharSequence fonts[] = new CharSequence[]{getString(R.string.small), getString(R.string.medium), getString(R.string.large)};

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(mContext, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(mContext);
        }


        builder.setTitle(getString(R.string.font_size_colon));
        builder.setSingleChoiceItems(fonts, fontid, (dialogInterface, i) -> fontid = i);
        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
            switch (fontid) {
                case 0:
                    txtFont.setText(getString(R.string.small));
                    break;
                case 1:
                    txtFont.setText(getString(R.string.medium));
                    break;
                case 2:
                    txtFont.setText(getString(R.string.large));
                    break;
            }
            User_settings.setFontSize(mContext, fontid);
            dialogInterface.cancel();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void setRingtone() {
        Log.e("============ringtone-1", User_settings.getRingtoneSelector(mContext));
        AppConstants.onpermission = true;
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.ringtone));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(User_settings.getRingtoneSelector(mContext)));
        mActivity.startActivityForResult(intent, AppConstants.REQUEST_CODE_RINGTONE);
    }

    private void setNotifySound() {
        AppConstants.onpermission = true;
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.notify));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(User_settings.getNotifySoundSelector(mContext)));
        mActivity.startActivityForResult(intent, AppConstants.REQUEST_CODE_NOTIFY_SOUND);
    }

    private void setSendEnterMessage() {
        if (User_settings.getEnterKeySend(mContext) == AppConstants.YES) {
            checkbox.setChecked(false);
            User_settings.setEnterKeySend(mContext, AppConstants.NO);
        } else {
            checkbox.setChecked(true);
            User_settings.setEnterKeySend(mContext, AppConstants.YES);
        }

    }

    private boolean duressValidity() {
        if (!(CommonUtils.hasText(mContext, txtDuressPwd, getString(R.string.duress_password_field_is_required)))) {
            return false;
        } else if (!(CommonUtils.checkPasswordLength(mContext, txtDuressPwd, R.string.pass_length_msg))) {
            return false;
        } else if (!(CommonUtils.isPassword(mContext, txtDuressPwd, true))) {
            return false;
        } else if (!(CommonUtils.comparedurText(mContext, User_settings.getAppPassword(mContext), txtDuressPwd.getText().toString().trim(), getString(R.string.duress_password_should_be_different_from_application_password)))) {
            return false;
        } else {
            return true;
        }
    }

    private void resetAccount() {
        txtCurrentPwd.setText("");
        txtConfirmPwd.setText((""));
        txtNewPwd.setText("");
    }

    private boolean validate() {
        if (!(CommonUtils.hasText(mContext, txtCurrentPwd, getString(R.string.current_password_field_is_required)))) {
            return false;
        } else if (!(CommonUtils.compareText(mContext, User_settings.getAppPassword(mContext), txtCurrentPwd.getText().toString().trim(), getString(R.string.incorrect_current_password_please_try_again)))) {
            return false;
        } else if (!(CommonUtils.sameText(mContext, txtCurrentPwd, txtNewPwd, getString(R.string.new_password_should_be_different_from_old_password)))) {
            return false;
        } else if (!(CommonUtils.hasText(mContext, txtNewPwd, getString(R.string.new_password_field_is_required)))) {
            return false;
        } else if (!(CommonUtils.checkPasswordLength(mContext, txtNewPwd, R.string.pass_length_msg))) {
            return false;
        } else if (!(CommonUtils.isPassword(mContext, txtNewPwd, true))) {
            return false;
        } else if (!(CommonUtils.hasText(mContext, txtConfirmPwd, getString(R.string.confirm_password_field_is_required)))) {
            return false;
        } else if (!CommonUtils.compareText(mContext, txtNewPwd, txtConfirmPwd, getString(R.string.password_and_confirm_password_does_not_match))) {
            return false;
        } else if (!(CommonUtils.comparedurText(mContext, User_settings.getDuressPassword(mContext), txtNewPwd.getText().toString().trim(), getString(R.string.duress_password_should_be_different_from_application_password)))) {
            return false;
        } else {
            return true;
        }
    }


    private void hideShowAccountPasswordLayout() {
        if (lyrAccountPasswordChild.getVisibility() == View.GONE) {
            imgActpassExpand.setImageResource(R.drawable.ic_expand_green);
            lyrAccountPasswordChild.setVisibility(View.VISIBLE);
        } else {
            imgActpassExpand.setImageResource(R.drawable.img_chevron_right_green);
            lyrAccountPasswordChild.setVisibility(View.GONE);
        }
    }


    private void visibleProfileSettings() {
        if (lyrProfileChild.getVisibility() == View.GONE) {
            lyrProfileChild.setVisibility(View.VISIBLE);
            imgProfileExpand.setImageResource(R.drawable.ic_expand_white);
            lyrProfileSettings.setBackgroundColor(Color.parseColor("#27ba96"));
        } else {
            lyrProfileChild.setVisibility(View.GONE);
            imgProfileExpand.setImageResource(R.drawable.img_chevron_right_white);
            lyrProfileSettings.setBackgroundColor(Color.parseColor("#2c2d31"));
        }
    }

    private void hideShowPasswordAttemptsLayout() {
        if (lyrPwdAttmeptsSeekbar.getVisibility() == View.GONE) {
            imgMaxpassExpand.setImageResource(R.drawable.ic_expand_green);
            lyrPwdAttmeptsSeekbar.setVisibility(View.VISIBLE);
        } else {
            imgMaxpassExpand.setImageResource(R.drawable.img_chevron_right_green);
            lyrPwdAttmeptsSeekbar.setVisibility(View.GONE);
        }
    }

    private void hideShowAccountSettingsLayout() {
        if (lyrChildAccountSettings.getVisibility() == View.GONE) {
            imgAccountExpand.setImageResource(R.drawable.ic_expand_white);
            lyrChildAccountSettings.setVisibility(View.VISIBLE);
            lyrAccountSettings.setBackgroundColor(Color.parseColor("#27ba96"));
        } else {
            imgAccountExpand.setImageResource(R.drawable.img_chevron_right_white);
            lyrChildAccountSettings.setVisibility(View.GONE);
            lyrAccountSettings.setBackgroundColor(Color.parseColor("#2c2d31"));
        }
    }

    private void hideShowDuressPasswordLayout() {
        if (lyrDuressPasswordChild.getVisibility() == View.GONE) {
            imgDuressExpand.setImageResource(R.drawable.ic_expand_green);
            lyrDuressPasswordChild.setVisibility(View.VISIBLE);
        } else {
            imgDuressExpand.setImageResource(R.drawable.img_chevron_right_green);
            lyrDuressPasswordChild.setVisibility(View.GONE);
        }

    }


    @Override
    public void onChangeSound(String name) {
        txtRingtone.setText(name);
    }

    @Override
    public void onChangeNotifySound(String name) {
        txtNotifySound.setText(name);
    }


    @OnClick(R.id.lyr_lang)
    public void onViewClicked() {
    }


    private void changeName() {
        new DialogChangeScreenName(mContext, User_settings.getScreenName(mContext), new ScreenNameChangeDialogResponse() {
            @Override
            public void onChangeName(String name) {
                KeyboardUtils.hideSoftInput(mActivity);
                changeNameApi(name);
            }

            @Override
            public void onClose() {

            }
        }).show();
    }

    private void changeNameApi(String name) {
        ProgressDialog mProgressDialoge = CommonUtils.showLoadingDialog(mContext);

        AndroidNetworking.post(ApiEndPoints.END_POINT_UPDATE_USER_DETAIL)
                .addBodyParameter("screen_name", name)
                .addBodyParameter("user_id", User_settings.getUserId(mContext))
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
                                User_settings.setScreenName(mContext, name);
                                txtName.setText(User_settings.getScreenName(mContext));
                                new DbHelper(mContext).updateGroupMember(DbConstants.KEY_NAME, name, DbConstants.KEY_ECC_ID, User_settings.getECCID(mContext));
                                CommonUtils.showInfoMsg(mContext, "Screen name Changed Successfully");

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
                        System.out.println("API Error : " + error.getErrorDetail());
                        CommonUtils.showInfoMsg(mContext, "Please try again.");
                    }

                });
    }

    //===== From "content://settings/system/ringtone" To "content://media/internal/audio/media/47"
    public Uri uriMap(Uri uri) {
        Uri mediaUri = uri;
        if(uri.getAuthority().equals(Settings.AUTHORITY)) {
            Cursor c = null;
            try {
                c = mContext.getContentResolver().query(uri,new String[]{
                        Settings.NameValueTable.VALUE},null,null,null);
                if(c != null && c.moveToFirst()) {
                    String val = c.getString(0);
                    mediaUri = Uri.parse(val);
                }
            } catch (Exception e) {
            }finally {
                c.close();
            }
        }
        Log.e("====", uri + "->" + mediaUri);
        return mediaUri;
    }
    @Override
    public void onResume() {
        super.onResume();
        scrollView.setVisibility(View.VISIBLE);

        //======= Ringtone Name
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        User_settings.setRingtoneSelector(mContext, uriMap(uri).toString());

        Ringtone r = RingtoneManager.getRingtone(mContext, uri);
        String ringtone_title = r.getTitle(mContext);
        ringtone_title = ringtone_title.replace("Default", "");
        ringtone_title = ringtone_title.replace("(", "");
        ringtone_title = ringtone_title.replace(")", "");
        txtRingtone.setText(ringtone_title);

        //======= Notification Sound Name
        Uri uri_notify= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        User_settings.setNotifySoundSelector(mContext, uriMap(uri_notify).toString());

        Ringtone r_notify = RingtoneManager.getRingtone(mContext, uri_notify);
        String notify_sound_title = r_notify.getTitle(mContext);
        notify_sound_title = notify_sound_title.replace("Default", "");
        notify_sound_title = notify_sound_title.replace("(", "");
        notify_sound_title = notify_sound_title.replace(")", "");
        txtNotifySound.setText(notify_sound_title);
    }

    @Override
    public void onPause() {
        super.onPause();
        scrollView.setVisibility(View.GONE);
    }

}
