package com.realapps.chat.view.dialoges;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.interfaces.ContactNameChangeDialogResponse;
import com.realapps.chat.interfaces.ScreenNameChangeDialogResponse;
import com.realapps.chat.ui.utils.Log;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DbConstants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DialogChangeContactName extends Dialog {
    Context mContext;
    ContactNameChangeDialogResponse dialogResponseListener;
    String eccId;
    String name;
    int userDBId;

    @BindView(R.id.txttitle)
    TextView txttitle;
    @BindView(R.id.txt_ecc_id)
    EditText txtEccId;
    @BindView(R.id.txt_alias)
    EditText txtAlias;
    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.txtcancel)
    TextView txtcancel;
    @BindView(R.id.lyr_account_password_child)
    LinearLayout lyrAccountPasswordChild;
    DbHelper db;
    private Unbinder unbinder;
    private ProgressDialog mProgressDialoge;

    public DialogChangeContactName(Context mContext, String eccId, int userDBId, ContactNameChangeDialogResponse dialogResponseListener) {
        super(mContext);
        this.mContext = mContext;
        this.eccId = eccId;
        this.name = name;
        this.userDBId = userDBId;
        this.dialogResponseListener = dialogResponseListener;
        db = new DbHelper(mContext);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.dialog_change_contact_name);
        Window window = getWindow();
        unbinder = ButterKnife.bind(this);
        WindowManager.LayoutParams wlp = window.getAttributes();
        setCancelable(true);
        wlp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        wlp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        wlp.windowAnimations = R.style.dialog_animation;
        window.setAttributes(wlp);

        txtEccId.setText(eccId);
        setOnDismissListener(dialogInterface -> dialogResponseListener.onClose());
        CommonUtils.hideTextSuggestion(txtEccId);
    }


    @OnClick({R.id.btn_save, R.id.txtcancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                if (validate()) {
                    if (txtAlias.getText().toString().trim().length() < 21) {

                        if (!db.checkContactName(txtAlias.getText().toString().trim())) {
                            ChangeScreenName();
                            dismiss();
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.nick_name_should_be_unique), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.alias_name_should_be_less_then_20_characters), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.txtcancel:
                dismiss();
                dialogResponseListener.onClose();
                break;
        }
    }

    private boolean validate() {
        if (!CommonUtils.hasTextdialog(mContext, txtEccId, mContext.getString(R.string.ecc_id_field_is_required))) {
            return false;
        } else if (!CommonUtils.hasTextdialog(mContext, txtAlias, mContext.getString(R.string.alias_field_is_required))) {
            return false;
        } else
            return true;
    }


    public void ChangeScreenName() {
        db.updateContactEntity(DbConstants.KEY_NAME, txtAlias.getText().toString().trim(), DbConstants.KEY_USER_DB_ID, userDBId);
        dismiss();
        dialogResponseListener.onChangeContactName();

    }
}


