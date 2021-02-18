package com.realapps.chat.view.dialoges;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.interfaces.AppUnlockDialogResponse;
import com.realapps.chat.interfaces.onShowKeyboard;
import com.realapps.chat.ui.utils.Log;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.KeyboardUtils;
import com.realapps.chat.utils.NotificationUtils;
import com.realapps.chat.view.login.fragment.Delete_Data;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import android.os.Build.*;


/**
 * Created by Prashant Kumar Sharma on 3/23/2017.
 */

public class DialogUnlock extends Dialog implements onShowKeyboard {
    public static AppUnlockDialogResponse appUnlockDialogResponse;
    public static onShowKeyboard onShowKeyboard;
    Context mContext;
    @BindView(R.id.img_logo)
    ImageView imgLogo;
    @BindView(R.id.logo_lyr)
    LinearLayout logoLyr;
    @BindView(R.id.edt_password)
    EditText edtPassword;
    @BindView(R.id.btn_cancel)
    Button btnCancel;
    @BindView(R.id.btn_save)
    Button btnSave;
    Unbinder unbinder;
    int tmp_attempt = 0;
    @BindView(R.id.main_lyr)
    RelativeLayout mainLyr;
    String data = "aici";
    public DialogUnlock(Context mContext, AppUnlockDialogResponse appUnlockDialogResponse) {
        super(mContext, R.style.full_screen_dialog);
        this.mContext = mContext;
        this.appUnlockDialogResponse = appUnlockDialogResponse;
        onShowKeyboard = this::showKeyboard;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.fragment_lock);
        unbinder = ButterKnife.bind(this);
        KeyboardUtils.toggleSoftInput(mContext);
        Window window = getWindow();
        mainLyr.setClickable(false);
        WindowManager.LayoutParams wlp = window.getAttributes();
        setCancelable(false);
        wlp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        wlp.height = LinearLayout.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);
        int messageCount = new DbHelper(mContext).getTotalUnreadMessages();
        NotificationUtils.showBadge(mContext, messageCount);
    }


    private boolean validate() {
        tmp_attempt = User_settings.getTempAttempt(mContext);
        int totalAttempt = User_settings.getMaxPasswordAttempt(mContext);

        if (!CommonUtils.hasTextdialog(mContext, edtPassword, mContext.getString(R.string.app_unlock_password_field_is_required))) {
            return false;
        } else if (CommonUtils.duressPass(mContext, User_settings.getDuressPassword(mContext), edtPassword)) {
            new CommonUtils().resetAll(mContext);
            return false;
        } else if (!CommonUtils.compareTextDialog(mContext, edtPassword.getText().toString(), User_settings.getAppPassword(mContext), mContext.getString(R.string.password_isn_t_correct_try_again))) {
            tmp_attempt++;
            User_settings.setTempAttempt(mContext, tmp_attempt);
            String msg = mContext.getString(R.string.password_isn_t_correct_please_try_again_incorrect_attempt_1_d_2_d, tmp_attempt, totalAttempt);

            if (tmp_attempt == totalAttempt - 1) {
                msg += mContext.getString(R.string.you_are_on_your_last_attempt_if_incorrect_your_data_will_be_security_wiped);
            }

            try {
                if (tmp_attempt <= totalAttempt - 2)
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                else if (tmp_attempt == totalAttempt - 1) {
                    DialogLastAttempt dialogLastAttempt =

                            new DialogLastAttempt(mContext, mContext.getString(R.string.you_are_on_the_last_attemp_if_incorrect_your_data_will_be_security_wiped), new AppUnlockDialogResponse() {

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
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (tmp_attempt == totalAttempt) {
                Log.v(data, "Clear");
               // clearPreferences();
                //new CommonUtils().resetAll(mContext);
                //new Delete_Data().getInstance();
                //new Delete_Data().clearApplicationData();
            }
            return false;
        } else {
            return true;
        }
    }
   /* private void clearPreferences() {

        try {
            Log.v(data, "Clear");
            // clearing app data
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear com.realapps.chat");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    @OnClick({R.id.btn_cancel, R.id.btn_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                KeyboardUtils.hideKeyboard(mContext);
                AppConstants.lockscreen = false;
                appUnlockDialogResponse.cancel();
                dismiss();
                break;
            case R.id.btn_save:

                if (validate()) {
                    KeyboardUtils.hideKeyboard(mContext);
                    AppConstants.lockscreen = false;
                    User_settings.setTempAttempt(mContext, 0);
                    appUnlockDialogResponse.appUnlock();
                    dismiss();
                }
                break;
        }
    }

    @Override
    public void showKeyboard() {
        KeyboardUtils.showSoftInput(edtPassword, mContext);
    }
}

