package com.realapps.chat.view.dialoges;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.interfaces.AppUnlockDialogResponse;
import com.realapps.chat.interfaces.DeleteItemsResponse;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.SocketUtils;
import com.realapps.chat.view.home.activity.HomeActivity;


import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Modified by Marius on 13.03.2020
 */


public class DialogLastAttempt extends Dialog implements View.OnClickListener {

    private DbHelper db;
    ArrayList<ContactEntity> contactList;
    private boolean started = false;
    private Handler handler = new Handler();
    private Timer timer;
    boolean isDeleted = false;
    ProgressDialog progressDialog;

    boolean resultOfComparison;
    EditText title_edit;
    Button btn_ok, btn_cancel;
    TextView lbl_title, txt_comment, txt_error;
    LinearLayout lyr_clear_contacts;
    Switch switch_clear_contacts;
    ProgressBar progressBar;

    AppUnlockDialogResponse dialogResponse = null;
    Context mContext;
    String title, stringA, stringB;

    public DialogLastAttempt(Context mContext, String title, AppUnlockDialogResponse dialogResponse) {
        super(mContext);
        this.dialogResponse = dialogResponse;
        this.mContext = mContext;
        this.title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.dialog_last_attemp);
        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        setCancelable(false);
        wlp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        wlp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        wlp.windowAnimations = R.style.dialog_animation;
        wlp.dimAmount = 0.9f;
        window.setAttributes(wlp);

        stringA = mContext.getString(R.string.ShadowSecureWipeData);//wipe local database
        stringB = mContext.getString(R.string.ShadowSecureTypeIn);//original

        db = new DbHelper(mContext);
        contactList = getSortedContactList(db.getContactList());
        progressBar = new ProgressBar(mContext);

        initView();

    }

    private void initView() {
        btn_ok = findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(this);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);
        lbl_title = findViewById(R.id.lbl_title);
        lbl_title.setText(title);
        title_edit = findViewById(R.id.last_attempt_check);
        txt_comment = findViewById(R.id.txt_comment);
        lyr_clear_contacts = findViewById(R.id.lyr_clear_contacts);
        switch_clear_contacts = findViewById(R.id.switch_clear_contacts);
        txt_error = findViewById(R.id.last_attempt_check_wrong);

        if(title.equals(stringA)) {
            lyr_clear_contacts.setVisibility(View.VISIBLE);
            title_edit.setHint(mContext.getString(R.string.wipe_edit_hint));
            txt_comment.setText(mContext.getString(R.string.wipe_comment));
        }

        title_edit.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                txt_error.setVisibility(View.INVISIBLE);
                return false;
            }
        });
        title_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txt_error.setVisibility(View.INVISIBLE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                String inputed_text = title_edit.getText().toString();

                if(inputed_text.trim().equals(stringA)) {//==== wipe data
                    wipeDataFromLocalDB();

                } else if(inputed_text.trim().equals(stringB)) {//====
                    dismiss();

                } else {
                   txt_error.setVisibility(View.VISIBLE);

                }
                break;

            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (!isDeleted) {
                CommonUtils.showInfoMsg(mContext, mContext.getString(R.string.please_try_again));
                if(progressDialog.isShowing()) progressDialog.dismiss();
                stop();
            }
        }
    };
    public void start() {
        if(timer != null) {
            return;
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }
    public void stop() {
        timer.cancel();
        timer = null;
    }

    private void wipeDataFromLocalDB() {
        dismiss();
        progressDialog = CommonUtils.showLoadingDialog(mContext);
        boolean isClearContacts = switch_clear_contacts.isChecked();

        //==== delete the chat list
        db.deleteAllChatList();
        db.deleteAllMessageListEntity();
        db.deleteAllGroupChatMembers();
        HomeActivity.setMenuCounter(R.id.nav_chat, db.getTotalUnreadMessages());

        //==== if switch is on, delete the contacts too.
        if(isClearContacts) {
            int count = contactList.size();
            if (count > 0) {
                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {

                    for (int i = 0; i < contactList.size(); i++) {
                        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
//                            SocketUtils.sendRemoveContactToSocket(mContext, contactList.get(i));
                            deleteContactFromDb(i);
                            isDeleted = true;
                        } else {
                            isDeleted = false;
                            break;
                        }
                    }

                }
            }
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(progressDialog.isShowing()) progressDialog.dismiss();
                if(isDeleted) CommonUtils.showInfoMsg(mContext, mContext.getString(R.string.wipe_completed));
                else CommonUtils.showInfoMsg(mContext, mContext.getString(R.string.wipe_completed));
            }
        }, 3000);
    }

    private void deleteContactFromDb(int position) {
        db.deleteContact(contactList.get(position).getUserDbId());
        db.deletePublicKey(contactList.get(position).getUserDbId());
    }

    private ArrayList<ContactEntity> getSortedContactList(ArrayList<ContactEntity> actualContactList) {

        ArrayList<ContactEntity> tempContactList = new ArrayList<>();
        ArrayList<ContactEntity> requestContactList = new ArrayList<>();
        ArrayList<ContactEntity> pendingContactList = new ArrayList<>();
        ArrayList<ContactEntity> acceptedContactList = new ArrayList<>();
        if (actualContactList.size() > 0) {
            for (int i = 0; i < actualContactList.size(); i++) {
                if (actualContactList.get(i).getBlockStatus().equalsIgnoreCase(String.valueOf(SocketUtils.request))) {
                    requestContactList.add(actualContactList.get(i));
                }

                if (actualContactList.get(i).getBlockStatus().equalsIgnoreCase(String.valueOf(SocketUtils.accepted))) {
                    acceptedContactList.add(actualContactList.get(i));
                }

                if (actualContactList.get(i).getBlockStatus().equalsIgnoreCase(String.valueOf(SocketUtils.pending))) {
                    pendingContactList.add(actualContactList.get(i));
                }
            }
            tempContactList.addAll(requestContactList);
            tempContactList.addAll(acceptedContactList);
            tempContactList.addAll(pendingContactList);

            return tempContactList;
        }

        return actualContactList;
    }

}
