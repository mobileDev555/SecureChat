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
import com.realapps.chat.interfaces.VaultItemNameChangeDialogResponse;
import com.realapps.chat.model.VaultEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Prashant Kumar Sharma on 3/23/2017.
 */

public class DialogChangeVaultItemName extends Dialog {

    Context mContext;
    VaultItemNameChangeDialogResponse dialogResponseListener;
    @BindView(R.id.txttitle)
    TextView txttitle;
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
    private VaultEntity vaultEntity;
    public DialogChangeVaultItemName(Context mContext, VaultEntity vaultEntity, VaultItemNameChangeDialogResponse dialogResponseListener) {
        super(mContext);
        this.mContext = mContext;
        this.vaultEntity = vaultEntity;
        this.dialogResponseListener = dialogResponseListener;
        db = new DbHelper(mContext);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.dialog_change_vault_item_name);
        Window window = getWindow();
        unbinder = ButterKnife.bind(this);
        WindowManager.LayoutParams wlp = window.getAttributes();
        setCancelable(true);
        wlp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        wlp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        wlp.windowAnimations = R.style.dialog_animation;
        window.setAttributes(wlp);
        setOnDismissListener(dialogInterface -> dialogResponseListener.onClose());
        CommonUtils.hideTextSuggestion(txtAlias);
    }

    @OnClick({R.id.btn_save, R.id.txtcancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                if (validate()) {
                    if (txtAlias.getText().toString().trim().length() < 50) {
                        if (vaultEntity.getName().equals(txtAlias.getText().toString().trim())) {
                            ChangeScreenName(vaultEntity.getName());
                        } else {
                            reNameFile(txtAlias.getText().toString().trim());
                        }
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.name_should_be_less_then_50_characters), Toast.LENGTH_SHORT).show();
                    }

                }

                break;
            case R.id.txtcancel:
                dismiss();
                dialogResponseListener.onClose();
                break;
        }
    }

    int count = 2;
    private void reNameFile(String trim) {
        if (isFileExist(trim)) {
            if (trim.endsWith(")")) {
                reNameFile(addSufToName(trim));
            } else {
                reNameFile(trim + "(" + count + ")");
            }
            count = count + 1;
        } else {
            ChangeScreenName(trim);
            count = 2;
        }
    }

    private String addSufToName(String trim) {
        int lastIndexOfOpenBre = trim.lastIndexOf("(");
        int lastIndexOfCloseBre = trim.lastIndexOf(")");
        CharSequence charSequence = trim.subSequence(lastIndexOfOpenBre + 1, lastIndexOfCloseBre);
        int lastCount = Integer.parseInt(charSequence.toString());
        return trim.replace(charSequence, String.valueOf(lastCount + 1));
    }

    private boolean validate() {
        if (!CommonUtils.hasTextdialog(mContext, txtAlias, mContext.getString(R.string.alias_field_is_required))) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isFileExist(String name) {
        if (vaultEntity.getMimeType() == AppConstants.ITEM_TYPE_NOTES && db.checkPersonalNoteName(name)) {
            return true;
        } else if (vaultEntity.getMimeType() == AppConstants.ITEM_TYPE_PICTURE && db.checkImageName(name)) {
            return true;
        } else {
            return false;
        }
    }
    private void ChangeScreenName(String trim) {
        db.updateVaultItemName(trim, vaultEntity.getId());
        dismiss();
        dialogResponseListener.onChangeName();
    }


}

