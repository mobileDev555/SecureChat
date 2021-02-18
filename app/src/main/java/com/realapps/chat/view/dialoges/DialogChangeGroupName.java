package com.realapps.chat.view.dialoges;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.interfaces.ScreenNameChangeDialogResponse;
import com.realapps.chat.utils.CommonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Prashant Sharma on 3/23/2017.
 * Core techies
 * prashant@coretechies.org
 */

public class DialogChangeGroupName extends Dialog {
    Context mContext;
    ScreenNameChangeDialogResponse dialogResponseListener;
    String screenName;
    @BindView(R.id.txt_screen_name)
    EditText txtScreenName;
    private Unbinder unbinder;

    public DialogChangeGroupName(Context mContext, String screenName, ScreenNameChangeDialogResponse dialogResponseListener) {
        super(mContext);
        this.mContext = mContext;
        this.screenName = screenName;
        this.dialogResponseListener = dialogResponseListener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.dialog_change_group_name);
        Window window = getWindow();
        unbinder = ButterKnife.bind(this);
        WindowManager.LayoutParams wlp = window.getAttributes();
        setCancelable(true);
        wlp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        wlp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        wlp.windowAnimations = R.style.dialog_animation;
        window.setAttributes(wlp);
        txtScreenName.setText(screenName);
        txtScreenName.setSelection(screenName.length());
        setOnDismissListener(dialogInterface -> dialogResponseListener.onClose());


    }


    private boolean validate() {
        if (!CommonUtils.hasTextdialog(mContext, txtScreenName, mContext.getString(R.string.screen_name_is_required))) {
            return false;
        } else
            return true;
    }


    @OnClick({R.id.btn_save, R.id.txtcancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                if (validate()) {
                    if (txtScreenName.getText().toString().trim().length() < 25) {
                        dialogResponseListener.onChangeName(txtScreenName.getText().toString().trim());
                        dismiss();
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.group_name_should_be_less_then_25_characters), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.txtcancel:
                dismiss();
                break;
        }
    }
}

