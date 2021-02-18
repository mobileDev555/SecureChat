package com.realapps.chat.view.home.activity;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.model.VaultEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.Cryptography;
import com.realapps.chat.utils.FileLog;
import com.realapps.chat.view.dialoges.DialogUnlock;
import com.realapps.chat.view.home.adapters.ChatWindowAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class VaultMessageWindowActivity extends AppCompatActivity implements ChatWindowAdapter.onItemClickListner {
    private static final String TAG = VaultMessageWindowActivity.class.getSimpleName();

    private static boolean SELECT_MODE = false;

    Context mContext;
    Activity mActivity;
    @BindView(R.id.recycler_messages)
    RecyclerView mRecycler;
    @BindView(R.id.txt_message)
    EditText txtMessage;
    @BindView(R.id.lyr_bottom)
    LinearLayout lyrBottom;
    private Toolbar toolbar;
    private ChatWindowAdapter mAdapter;
    private ArrayList<ChatMessageEntity> messageList;
    private VaultEntity vaultListEntity;
    private DbHelper dbHelper;
    private ProgressDialog mProgressDialoge;
    private boolean isShare = false;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_chat_window);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mContext = this;
        mActivity = this;
        ButterKnife.bind(mActivity);
        dbHelper = new DbHelper(mContext);
        if (getIntent().getExtras() != null) {
            vaultListEntity = (VaultEntity) getIntent().getSerializableExtra(AppConstants.EXTRA_VAULT_LIST_ITEM);
            isShare = getIntent().getBooleanExtra(AppConstants.EXTRA_IS_SHARE, false);
        }
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //set Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (vaultListEntity.getChatType() == 0) {
            toolbar.setTitle(CommonUtils.getContactName(mContext, vaultListEntity.getEccId()));
        } else {
            toolbar.setTitle(vaultListEntity.getName().substring(0, 1).toUpperCase() + vaultListEntity.getName().substring(1));
        }
        initViews();
        setAdapter();
    }


    private void initViews() {
        CommonUtils.hideTextSuggestion(txtMessage);
        lyrBottom.setVisibility(View.GONE);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());
    }


    private void setAdapter() {
        messageList = new ArrayList<>();
        messageList = dbHelper.getVaultMessageList(vaultListEntity.getId());
        FileLog.e("ChatWindow DbId ", String.valueOf(vaultListEntity.getId()));
        mAdapter = new ChatWindowAdapter(mContext, messageList, this);
        mRecycler.setAdapter(mAdapter);


        if (isShare) {
            activeSelectMode();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isShare)
            getMenuInflater().inflate(R.menu.vault_message_window_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                Intent intent = new Intent(mContext, ForwardMessageActivity.class);
                intent.putExtra(AppConstants.EXTRA_MESSAGE_LIST, getSelectedMessage());
                intent.putExtra(AppConstants.IS_ENCRYPTED, false);
                startActivity(intent);
                deactiveSelectMode();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClick(View view, ChatMessageEntity chatMessageEntity, int position) {
        if (SELECT_MODE) {
            toggleSelection(position);
            setMessageCount();
        } else {

            if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_AUDIO) {

            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_CONTACT) {
                mProgressDialoge = CommonUtils.showLoadingDialog(mContext);
                String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getContactPath());
                if (mProgressDialoge != null) {
                    if (mProgressDialoge.isShowing())
                        mProgressDialoge.dismiss();
                }
                if (decryptedFilePath.length() > 0) {
                    Intent intent = new Intent(mContext, ContactDetailsActivity.class);
                    intent.putExtra(AppConstants.EXTRA_CONTACT_ENTITY, decryptedFilePath);
                    startActivity(intent);
                } else {
                    CommonUtils.showErrorMsg(mContext, getString(R.string.picture_cannot_be_decrypted_try_again));
                }

            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_IMAGE) {
                mProgressDialoge = CommonUtils.showLoadingDialog(mContext);
                String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getImagePath());
                if (mProgressDialoge != null) {
                    if (mProgressDialoge.isShowing())
                        mProgressDialoge.dismiss();
                }
                Intent intent = new Intent(mContext, PhotoViewActivity.class);
                intent.putExtra(AppConstants.EXTRA_IMAGE_PATH, decryptedFilePath);
                startActivity(intent);
            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_NOTE) {
                mProgressDialoge = CommonUtils.showLoadingDialog(mContext);
                String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getFilePath());
                if (mProgressDialoge != null) {
                    if (mProgressDialoge.isShowing())
                        mProgressDialoge.dismiss();
                }
                Intent i = new Intent(mContext, PersonalActivityView.class);
                i.putExtra(AppConstants.EXTRA_PERSONAL_NOTE_FILE_PATH, decryptedFilePath);
                i.putExtra(AppConstants.EXTRA_MESSAGE_ID, chatMessageEntity.getMessageId());
                startActivity(i);
            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_VIDEO) {

            }
        }


    }

    @Override
    public void onItemLongPress(ChatMessageEntity chatMessageEntity, int position) {

    }

    @Override
    public void onItemForward(ChatMessageEntity chatMessageEntity, int position) {

    }

    @Override
    public void onRetryMessage(ChatMessageEntity chatMessageEntity, int position) {

    }


    private void toggleSelection(int position) {
        messageList.get(position).setSelected(!messageList.get(position).isSelected());
        mAdapter.notifyItemChanged(position);
    }

    private void activeSelectMode() {
        SELECT_MODE = true;
        ChatWindowAdapter.SELECT_MODE = true;
        mAdapter.notifyDataSetChanged();
    }


    private void deactiveSelectMode() {
        SELECT_MODE = false;
        ChatWindowAdapter.SELECT_MODE = false;

        unSelectAllMessage();
        mAdapter.notifyDataSetChanged();
        toolbar.setTitle(vaultListEntity.getName());
    }

    private void unSelectAllMessage() {
        for (ChatMessageEntity messageEntity : messageList) {
            if (messageEntity.isSelected())
                messageEntity.setSelected(false);
        }
    }


    private void setMessageCount() {
        toolbar.setTitle(String.valueOf(getSelectedMessageCount()));
    }

    private int getSelectedMessageCount() {
        int mCount = 0;
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).isSelected())
                mCount++;
        }
        return mCount;
    }


    private ArrayList<ChatMessageEntity> getSelectedMessage() {
        ArrayList<ChatMessageEntity> selectedMessage = new ArrayList<>();
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).isSelected())
                selectedMessage.add(messageList.get(i));
        }
        return selectedMessage;
    }


    @Override
    public void onBackPressed() {
        mActivity.finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        AppConstants.isbackground = true;
        HomeActivity.runnable = new Runnable() {
            @Override
            public void run() {
                if (AppConstants.isbackground) {
                    Log.e("Tag", "onPause: " + "background-017");
                    CommonUtils.lockDialog(mActivity);
                } else {
                    Log.e("Tag", "onPause: " + "forground-017");
                }


            }
        };
        HomeActivity.lockHandler.postDelayed(HomeActivity.runnable, User_settings.getLockTime(mContext));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
        HomeActivity.lockHandler.removeCallbacks(HomeActivity.runnable);
        if (AppConstants.lockscreen) {
            CommonUtils.checkDialog(mActivity);
        }
        AppConstants.isbackground = false;
        if (AppConstants.lockscreen) {
            new Handler().postDelayed(() -> DialogUnlock.onShowKeyboard.showKeyboard(), 500);
        }

    }
}

