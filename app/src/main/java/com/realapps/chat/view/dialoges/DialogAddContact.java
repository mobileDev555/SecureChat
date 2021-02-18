package com.realapps.chat.view.dialoges;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.interfaces.AddContactDialogResponse;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.model.PublicKeyEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.NetworkUtils;
import com.realapps.chat.utils.SocketUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Prashant Kumar Sharma on 3/23/2017.
 */

public class DialogAddContact extends Dialog {
    Context mContext;
    AddContactDialogResponse dialogResponseListener;
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
    ContactEntity entity;
    DbHelper db;
    private Unbinder unbinder;
    private ProgressDialog mProgressDialoge;
    private ArrayList<ContactEntity> contactList;
    public DialogAddContact(Context mContext, ArrayList<ContactEntity> contactList, AddContactDialogResponse dialogResponseListener) {
        super(mContext);
        this.mContext = mContext;
        this.contactList = contactList;
        this.dialogResponseListener = dialogResponseListener;
        db = new DbHelper(mContext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.dialog_add_contact);
        Window window = getWindow();
        unbinder = ButterKnife.bind(this);
        WindowManager.LayoutParams wlp = window.getAttributes();
        setCancelable(true);
        wlp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        wlp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        wlp.windowAnimations = R.style.dialog_animation;
        wlp.dimAmount = 0.9f;
        window.setAttributes(wlp);
        setOnDismissListener(dialogInterface -> dialogResponseListener.onClose());
        CommonUtils.hideTextSuggestion(txtEccId);
        txtEccId.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
    }


    @OnClick({R.id.btn_save, R.id.txtcancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                if (txtEccId.getText().toString().toCharArray().length < 3) {
                    Toast.makeText(mContext, mContext.getString(R.string.please_enter_valid_ecc_id), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!db.checkContact(txtEccId.getText().toString().trim())) {
                    if (txtEccId.getText().toString().trim().equalsIgnoreCase(User_settings.getECCID(mContext))) {
                        Toast.makeText(mContext, mContext.getString(R.string.cannot_add_own_ecc_id_in_contacts), Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        if (validate()) {
                            if (NetworkUtils.isNetworkConnected(mContext)) {
                                if (!db.checkContactName(txtAlias.getText().toString().trim())) {
                                    mProgressDialoge = CommonUtils.showLoadingDialog(mContext);
                                    addContact();
                                    dismiss();
                                } else {
                                    Toast.makeText(mContext, mContext.getString(R.string.contact_name_should_be_unique), Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(mContext, mContext.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                                dismiss();
                            }

                        }
                    }
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.contact_is_added_already), Toast.LENGTH_SHORT).show();
                    dismiss();
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


    public void addContact() {
        AndroidNetworking.post(ApiEndPoints.get_user_details)
                .addBodyParameter("id", txtEccId.getText().toString().trim())
                .addBodyParameter("type", AppConstants.typeEcc)
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
                                JSONObject childObj = rootObject.getJSONObject("result_data");
                                entity = new ContactEntity();
                                entity.setUserDbId(childObj.getInt("user_id"));
                                entity.setEccId(childObj.optString("Id"));
                                entity.setUserType(childObj.getInt("Type"));
                                entity.setEccPublicKey(childObj.optString("ecc_key"));
                                entity.setName(txtAlias.getText().toString().trim());
                                entity.setBlockStatus(String.valueOf(SocketUtils.pending));
                                PublicKeyEntity keyEntity = new PublicKeyEntity();
                                keyEntity.setUserDbId(childObj.getInt("user_id"));
                                keyEntity.setEccId(childObj.optString("Id"));
                                keyEntity.setUserType(childObj.getInt("Type"));
                                keyEntity.setEccPublicKey(childObj.optString("ecc_key"));
                                keyEntity.setName(txtAlias.getText().toString().trim());
                                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                                    db.insertContactList(entity);
                                    db.insertPublicKey(keyEntity);
                                    SocketUtils.sendAddContactRequestToSocket(mContext, entity);
                                    dialogResponseListener.onAddContact(entity);
                                } else {
                                    CommonUtils.showInfoMsg(mContext, mContext.getString(R.string.please_try_again));
                                }
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
                        CommonUtils.showInfoMsg(mContext, mContext.getString(R.string.please_try_again));

                    }
                });
    }

}

