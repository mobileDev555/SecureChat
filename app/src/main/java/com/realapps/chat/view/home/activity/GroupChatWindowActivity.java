package com.realapps.chat.view.home.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.koushikdutta.ion.Ion;
import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.SendMessageOfflineService;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.parser.PublicKeysParser;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.interfaces.AttachmentDialogResponse;
import com.realapps.chat.interfaces.ChatWindowFunctionListener;
import com.realapps.chat.interfaces.DestructTimeDialogResponse;
import com.realapps.chat.interfaces.GroupUpdateListener;
import com.realapps.chat.interfaces.UnlockListener;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.model.GroupMemberEntity;
import com.realapps.chat.model.VaultEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.Cryptography;
import com.realapps.chat.utils.DateTimeUtils;
import com.realapps.chat.utils.DbConstants;
import com.realapps.chat.utils.FileLog;
import com.realapps.chat.utils.FileUtils;
import com.realapps.chat.utils.NetworkUtils;
import com.realapps.chat.utils.NotificationUtils;
import com.realapps.chat.utils.SocketUtils;
import com.realapps.chat.view.dialoges.DeleteMessageDialog;
import com.realapps.chat.view.dialoges.DialogAttachment;
import com.realapps.chat.view.dialoges.DialogCamera;
import com.realapps.chat.view.dialoges.DialogDestructTimer;
import com.realapps.chat.view.dialoges.DialogMediaPlayer;
import com.realapps.chat.view.dialoges.DialogUnlock;
import com.realapps.chat.view.home.VaultFileSaveUtils;
import com.realapps.chat.view.home.adapters.ChatWindowAdapter;
import com.realapps.chat.view.home.adapters.GroupChatWindowAdapter;
import com.realapps.chat.view.home.fragment.FragmentChats;
import com.realapps.chat.view.home.fragment.FragmentGroupChat;
import com.realapps.chat.view.lock.Lock;
import com.zxy.tiny.Tiny;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;


public class GroupChatWindowActivity extends AppCompatActivity implements GroupChatWindowAdapter.onItemClickListner, ChatWindowFunctionListener, TextView.OnEditorActionListener, View.OnTouchListener, GroupUpdateListener, UnlockListener {


    private static final String TAG = GroupChatWindowActivity.class.getSimpleName();
    private static final int REQUEST_CONTACT_SELECT = 101;
    private static final int REQUEST_VAULT_ITEM_SELECT = 102;
    public static ChatWindowFunctionListener chatWindowFunctionListener;
    public static GroupUpdateListener groupUpdateListener;
    public static UnlockListener unlockListener;
    private static boolean SELECT_MODE = false;
    private static boolean isRunning = true;
    private static boolean isRunningUnsentMessages = true;
    private static boolean isRunningUnsentMediaMessages = true;
    private final int TWO_SECONDS = 2000;
    public ArrayList<GroupMemberEntity> groupMemberList;
    //Float
    float x1, y1, x2, y2, dx, dy;
    //Long
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    ArrayList<ContactEntity> contacts;
    ArrayList<VaultEntity> vaultListEntities;
    String audioPath = "";
    Context mContext;
    Activity mActivity;
    @BindView(R.id.recycler_messages)
    RecyclerView mRecycler;
    @BindView(R.id.txt_message)
    EditText txtMessage;
    ArrayList<String> photoPaths = new ArrayList<>();
    Handler backGroundHandler = new Handler();
    Handler backGroundHandlerUnsentMessages = new Handler();
    Handler backGroundHandlerUnsentMediaMessages = new Handler();
    @BindView(R.id.txt_group_name)
    TextView txtGroupName;
    @BindView(R.id.btn_send_message)
    ImageButton btnSendMessage;
    @BindView(R.id.lyr_text_message)
    LinearLayout lyrTextMessage;
    @BindView(R.id.txt_recording_time)
    TextView txtRecordingTime;
    @BindView(R.id.txt_slide_to_cancel)
    TextView txtSlideToCancel;
    @BindView(R.id.lyr_recording)
    LinearLayout lyrRecording;
    @BindView(R.id.lyr_bottom)
    LinearLayout lyrBottom;
    @BindView(R.id.lyr_hide)
    LinearLayout lyrHide;
    @BindView(R.id.img_camera)
    ImageButton lyrCamera;
    DialogMediaPlayer dialogMediaPlayer;
    LinearLayoutManager mLayoutManager;
    int imageItemPosition = -1;
    //to check if message can be revised or not
    private boolean isRevised = false;
    private ChatMessageEntity revisedChatMessageEntity;
    private String originaltext = "";
    //    @BindView(R.id.main_lyr)
//    RelativeLayout rlmainlyr;
    private long startTime = 0L;
    private int mCount = 0;
    private boolean isMultipleContact = false;
    private boolean isMultipleVaultItem = false;
    private MediaRecorder recorder = null;
    private Timer timer;
    private Toolbar toolbar;
    private GroupChatWindowAdapter mAdapter;
    private ChatMessageEntity chatMessageEntityVault;
    private ArrayList<ChatMessageEntity> messageList;
    private ChatListEntity chatListEntity;
    private DbHelper dbHelper;
    private boolean isPaused = false;

    private Bitmap.Config mConfig = Bitmap.Config.ARGB_8888;
    private String fileName = "";
    Handler handler;

    boolean flag = false;
    boolean isforward = false;

    public static String copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
        return dst.getAbsolutePath();
    }

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_group_window);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        unlockListener = this;
        mContext = this;
        mActivity = this;
        ButterKnife.bind(mActivity);
        dbHelper = new DbHelper(mContext);
        if (getIntent().getExtras() != null) {
            chatListEntity = (ChatListEntity) getIntent().getSerializableExtra(AppConstants.EXTRA_CHAT_LIST_ITEM);
            AppConstants.chatId = chatListEntity.getId();
            AppConstants.chatType = chatListEntity.getChatType();
        }
        groupMemberList = dbHelper.getGroupMemberList(chatListEntity.getUserDbId());
        chatWindowFunctionListener = this;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //set Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        txtGroupName.setText(chatListEntity.getName());
        setAdapter();
        startBackGroundThread();
        startBackGroundThreadForUnsentMessages();
        if(!CommonUtils.isMyServiceRunning(mContext,SendMessageOfflineService.class)){
            BackGroundService();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        AppConstants.openedChatID = chatListEntity.getId();
        CommonUtils.checkStatus(mContext);
        if (chatWindowFunctionListener == null)
            chatWindowFunctionListener = this;

        HomeActivity.lockHandler.removeCallbacks(HomeActivity.runnable);
        if (AppConstants.lockscreen) {
            CommonUtils.checkDialog(mActivity);
        }
        groupUpdateListener = this;
        groupMemberList = dbHelper.getGroupMemberList(chatListEntity.getUserDbId());
        chatListEntity = dbHelper.getChatEntity(chatListEntity.getUserDbId());
        txtGroupName.setText(chatListEntity.getName());

        if (!AppConstants.lockscreen)
            setAdapter();

        initViews();
        AppConstants.isbackground = false;

        if (isPaused) {
            if(isforward){
                isforward=false;
                flag=false;
            }
            startBackGroundThread();
            startBackGroundThreadForUnsentMessages();
        }
        if (AppConstants.lockscreen) {
            new Handler().postDelayed(() -> DialogUnlock.onShowKeyboard.showKeyboard(), 500);
        }


        if (imageItemPosition != -1) {
            mRecycler.scrollToPosition(imageItemPosition);
            imageItemPosition = -1;
        }
    }

    @Override
    public void onUnlock() {
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEND && User_settings.getEnterKeySend(mContext) == AppConstants.YES && txtMessage.getText().toString().trim().length() > 0) {
            if (User_settings.getUserActiveStatus(mContext)) {
                if (User_settings.getInventryStatus(mContext)) {
                    if (User_settings.getSubscriptionStatus(mContext)) {
                        if (isRevised && revisedChatMessageEntity != null)
                            sendRevisedTextMessage(txtMessage.getText().toString().trim());
                        else
                            sendTextMessage(txtMessage.getText().toString().trim());
                    } else {
                        CommonUtils.showInfoMsg(mContext, "Your subscription has been expired. Please renew.");
                    }
                } else {
                    CommonUtils.showInfoMsg(mContext, "You do not have any plan yet.");
                }
            } else {
                CommonUtils.showInfoMsg(mContext, "Your account has been temporarily suspended. Please try later!");
            }
            return true;
        }
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void initViews() {
        if (isGroupMember()) {
            lyrBottom.setVisibility(View.VISIBLE);
            lyrHide.setVisibility(View.GONE);
        } else {
            lyrBottom.setVisibility(View.GONE);
            lyrHide.setVisibility(View.VISIBLE);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        setMessageHintDestructionTime();
        txtMessage.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        txtMessage.setOnEditorActionListener(this);

        if (User_settings.getEnterKeySend(mContext) == AppConstants.YES) {
            txtMessage.setImeOptions(EditorInfo.IME_ACTION_SEND);
        } else {
            txtMessage.setSingleLine(false);
            txtMessage.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        }


        if (User_settings.getfont(mContext) == AppConstants.smallFont) {
            txtMessage.setTextSize(15);
        } else if (User_settings.getfont(mContext) == AppConstants.mediumFont) {
            txtMessage.setTextSize(18);
        } else {
            txtMessage.setTextSize(22);
        }

        txtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (txtMessage.getText().toString().trim().length() > 0 && originaltext.equalsIgnoreCase(txtMessage.getText().toString()) && isRevised) {
                    btnSendMessage.setImageResource(R.drawable.ic_clear);
                    btnSendMessage.setOnTouchListener(null);
                } else if (txtMessage.getText().toString().trim().length() > 0) {
                    btnSendMessage.setImageResource(R.drawable.send_blue);
                    lyrCamera.setVisibility(View.GONE);
                    btnSendMessage.setOnTouchListener(null);
                } else {
                    btnSendMessage.setImageResource(R.drawable.ic_mic_white);
                    lyrCamera.setVisibility(View.VISIBLE);
                    btnSendMessage.setOnTouchListener(GroupChatWindowActivity.this);
                }
            }
        });

        btnSendMessage.setOnTouchListener(GroupChatWindowActivity.this);
    }

    private boolean isGroupMember() {
        boolean isMember = false;
        for (GroupMemberEntity memberEntity : groupMemberList) {
            if (memberEntity.getEccId().equalsIgnoreCase(User_settings.getECCID(mContext)))
                isMember = true;
        }
        return isMember;
    }

    private void setAdapter() {
        if (messageList != null) {
            notifyMessageList();
        } else {
            messageList = new ArrayList<>();
            messageList = dbHelper.getMessageList(chatListEntity.getId(), chatListEntity.getChatType());
            FileLog.e("GroupChatWindow DbId ", String.valueOf(chatListEntity.getId()));
            mAdapter = new GroupChatWindowAdapter(mContext, messageList, this);
            mRecycler.setAdapter(mAdapter);
            if (handler == null) {
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        handler.postDelayed(this, 1000);
                    }
                }, 1000);
            }
        }

        boolean notifyChatListItem = false;
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getMessageStatus() == AppConstants.MESSAGE_UNREAD_STATUS) {
                synchronized (this) {
                    notifyChatListItem = true;
                    dbHelper.updateMessageBurnDate(messageList.get(i).getMessageId(), DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, messageList.get(i).getMessageBurnTime()));
                    dbHelper.updateMessageStatusByMessageId(messageList.get(i).getMessageId(), AppConstants.MESSAGE_READ_STATUS);
                    showMessageCountBadge();
                }
            }
        }

        if (notifyChatListItem) {
            if (FragmentChats.refreshChatListListener != null) {
                FragmentChats.refreshChatListListener.onRefresh();
            }
            if (FragmentGroupChat.refreshChatListListener != null) {
                FragmentGroupChat.refreshChatListListener.onRefresh();
            }
        }
    }

    private void updateMessageStatus() {
        new AsyncTask<Void, Boolean, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean notifyChatListItem = false;
                for (int i = 0; i < messageList.size(); i++) {
                    if (messageList.get(i).getMessageStatus() == AppConstants.MESSAGE_UNREAD_STATUS) {
                        synchronized (this) {
                            notifyChatListItem = true;

                            dbHelper.updateMessageBurnDate(messageList.get(i).getMessageId(), DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, messageList.get(i).getMessageBurnTime()));
                            dbHelper.updateMessageStatusByMessageId(messageList.get(i).getMessageId(), AppConstants.MESSAGE_READ_STATUS);
                            showMessageCountBadge();
                        }
                    }

                }
                return notifyChatListItem;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean)
                    if (FragmentChats.refreshChatListListener != null) {
                        FragmentChats.refreshChatListListener.onRefresh();
                    }
            }
        }.execute();
    }

    private void refreshList() {

        messageList = dbHelper.getMessageList(chatListEntity.getId(), chatListEntity.getChatType());
        mAdapter.notifyDataSetChanged();

        boolean notifyChatListItem = false;
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getMessageStatus() == AppConstants.MESSAGE_UNREAD_STATUS) {
                synchronized (this) {
                    notifyChatListItem = true;
                    dbHelper.updateMessageBurnDate(messageList.get(i).getMessageId(), DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, messageList.get(i).getMessageBurnTime()));
                    dbHelper.updateMessageStatusByMessageId(messageList.get(i).getMessageId(), AppConstants.MESSAGE_READ_STATUS);
                    showMessageCountBadge();
                }
            }
        }

        if (notifyChatListItem) {
            if (FragmentChats.refreshChatListListener != null) {
                FragmentChats.refreshChatListListener.onRefresh();
            }
        }
    }

    @OnClick({R.id.img_camera, R.id.attachment, R.id.btn_send_message})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_camera:
                new DialogCamera(mContext, new AttachmentDialogResponse()
                {
                    @Override
                    public void onCameraResponse()
                    {
                        AppConstants.onpermission = true;
                        TedPermission.with(mContext)
                                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                .setPermissionListener(new PermissionListener() {
                                    @Override
                                    public void onPermissionGranted() {
                                        try {
                                            File saveFolder = new File(CommonUtils.getImageDirectory(mContext));

                                            new MaterialCamera(mActivity)                               // Constructor takes an Activity
                                                    .saveDir(saveFolder)                               // The folder recorded videos are saved to
                                                    .primaryColorAttr(R.attr.colorPrimary)             // The theme color used for the camera, defaults to colorPrimary of Activity in the constructor
                                                    .showPortraitWarning(true)                         // Whether or not a warning is displayed if the user presses record in portrait orientation
                                                    .defaultToFrontFacing(false)                       // Whether or not the camera will initially show the front facing camera
                                                    .iconFrontCamera(R.drawable.mcam_camera_front)     // Sets a custom icon for the button used to switch to the front camera
                                                    .iconRearCamera(R.drawable.mcam_camera_rear)       // Sets a custom icon for the button used to switch to the rear camera
                                                    .stillShot()
                                                    .labelConfirm(R.string.send)
                                                    .start(AppConstants.CAMERA_RQ);                    // Starts the camera activity, the result will be sent back to the current Activity
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        //File saveFolder = new File(CommonUtils.getImageDirectory(mContext));

                                    }

                                    @Override
                                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {

                                    }
                                })
                                .check();
                    }
                    @Override
                    public void onClose() {

                    }
                    @Override
                    public void onContactSelect() {
                        startActivityForResult(new Intent(mContext, SelectContactActivity.class), REQUEST_CONTACT_SELECT);
                    }
                    @Override
                    public void sharemsgfromvault() {
                        Intent intent = new Intent(GroupChatWindowActivity.this, ShareFromVaultActivity.class);
                        intent.putExtra(AppConstants.EXTRA_ITEM_TYPE, AppConstants.ITEM_TYPE_CHATS);
                        startActivityForResult(intent, REQUEST_VAULT_ITEM_SELECT);

                    }
                    @Override
                    public void onPersonalNoteSelect() {
                        Intent intent = new Intent(mContext, ShareFromVaultActivity.class);
                        intent.putExtra(AppConstants.EXTRA_ITEM_TYPE, AppConstants.ITEM_TYPE_NOTES);
                        startActivityForResult(intent, REQUEST_VAULT_ITEM_SELECT);
                    }
                    public void onImageSelect() {




                    }

                });

                break;

            case R.id.attachment:
                if (User_settings.getUserActiveStatus(mContext)) {
                    if (User_settings.getInventryStatus(mContext)) {
                        if (User_settings.getSubscriptionStatus(mContext)) {

                            if (isTextBoxExpanded())
                                collapseMessageTextBox();

                            new DialogAttachment(mContext, new AttachmentDialogResponse() {

                                @Override
                                public void onImageSelect() {
                                    if (isGroupMember()) {
                                        Intent intent2 = new Intent(mContext, ShareFromVaultActivity.class);
                                        intent2.putExtra(AppConstants.EXTRA_ITEM_TYPE, AppConstants.ITEM_TYPE_PICTURE);
                                        startActivityForResult(intent2, REQUEST_VAULT_ITEM_SELECT);
                                    } else {
                                        CommonUtils.showErrorMsg(mContext, getString(R.string.you_are_no_longer_member_of_the_group));
                                    }
                                }

                                @Override
                                public void onContactSelect() {
                                    startActivityForResult(new Intent(mContext, SelectContactActivity.class), REQUEST_CONTACT_SELECT);
                                }

                                @Override
                                public void onPersonalNoteSelect() {
                                    Intent intent = new Intent(mContext, ShareFromVaultActivity.class);
                                    intent.putExtra(AppConstants.EXTRA_ITEM_TYPE, AppConstants.ITEM_TYPE_NOTES);
                                    startActivityForResult(intent, REQUEST_VAULT_ITEM_SELECT);
                                }

                                @Override
                                public void onCameraResponse() {

                                    AppConstants.onpermission = true;
                                    TedPermission.with(mContext)
                                            .setDeniedMessage(getString(R.string.if_you_reject_permission_you_can_not_use_this_service_n_nplease_turn_on_permissions_at_setting_permission))
                                            .setPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            .setPermissionListener(new PermissionListener() {
                                                @Override
                                                public void onPermissionGranted() {
                                                    File saveFolder = new File(CommonUtils.getImageDirectory(mContext));
                                                    new MaterialCamera(mActivity)                               // Constructor takes an Activity
                                                            .saveDir(saveFolder)                               // The folder recorded videos are saved to
                                                            .primaryColorAttr(R.attr.colorPrimary)             // The theme color used for the camera, defaults to colorPrimary of Activity in the constructor
                                                            .showPortraitWarning(true)                         // Whether or not a warning is displayed if the user presses record in portrait orientation
                                                            .defaultToFrontFacing(false)                       // Whether or not the camera will initially show the front facing camera
                                                            .iconFrontCamera(R.drawable.mcam_camera_front)     // Sets a custom icon for the button used to switch to the front camera
                                                            .iconRearCamera(R.drawable.mcam_camera_rear)       // Sets a custom icon for the button used to switch to the rear camera
                                                            .stillShot()
                                                            .labelConfirm(R.string.send)
                                                            .start(AppConstants.CAMERA_RQ);                    // Starts the camera activity, the result will be sent back to the current Activity
                                                }

                                                @Override
                                                public void onPermissionDenied(ArrayList<String> deniedPermissions) {

                                                }
                                            })
                                            .check();


                                }
                                @Override
                                public void sharemsgfromvault() {
                                    Intent intent = new Intent(GroupChatWindowActivity.this, ShareFromVaultActivity.class);
                                    intent.putExtra(AppConstants.EXTRA_ITEM_TYPE, AppConstants.ITEM_TYPE_CHATS);
                                    startActivityForResult(intent, REQUEST_VAULT_ITEM_SELECT);

                                }
                                @Override
                                public void onClose() {

                                }
                            }).show();
                        } else {
                            CommonUtils.showInfoMsg(mContext, "Your subscription has been expired. Please renew.");
                        }
                    } else {
                        CommonUtils.showInfoMsg(mContext, "You do not have any plan yet.");
                    }
                } else {
                    CommonUtils.showInfoMsg(mContext, "Your account has been temporarily suspended. Please try later!");
                }
                break;
            case R.id.btn_send_message:
                if (User_settings.getUserActiveStatus(mContext)) {
                    if (User_settings.getInventryStatus(mContext)) {
                        if (User_settings.getSubscriptionStatus(mContext)) {
                            if (txtMessage.getText().toString().trim().length() > 0) {
                                if (isRevised && revisedChatMessageEntity != null)
                                    sendRevisedTextMessage(txtMessage.getText().toString().trim());
                                else
                                    sendTextMessage(txtMessage.getText().toString().trim());
                            }
                        } else {
                            CommonUtils.showInfoMsg(mContext, "Your subscription has been expired. Please renew.");
                        }
                    } else {
                        CommonUtils.showInfoMsg(mContext, "You do not have any plan yet.");
                    }
                } else {
                    CommonUtils.showInfoMsg(mContext, "Your account has been temporarily suspended. Please try later!");
                }

                break;
        }
    }

    private void sendTextMessage(String msg) {
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setSenderId(Integer.parseInt(User_settings.getUserId(mContext)));
        chatMessageEntity.setMessage(msg);
        chatMessageEntity.setMessageMimeType(AppConstants.MIME_TYPE_TEXT);
        chatMessageEntity.setMessageId(String.valueOf(System.currentTimeMillis()));
        chatMessageEntity.setChatId(chatListEntity.getId());
        chatMessageEntity.setReceiverId(chatListEntity.getUserDbId());
        chatMessageEntity.setName(User_settings.getScreenName(mContext));
        chatMessageEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
        chatMessageEntity.setMessageBurnTime(chatListEntity.getBurnTime());
        chatMessageEntity.setChatUserDbId(chatMessageEntity.getChatUserDbId());
        chatMessageEntity.setChatType(AppConstants.GROUP_CHAT_TYPE);
        chatMessageEntity.setMessageType(AppConstants.MESSAGE_TYPE_FROM);
        chatMessageEntity.setEddId(User_settings.getECCID(mContext));
        if (NetworkUtils.isNetworkConnected(mContext)) {
            if (checkGroupMembersKey()) {
                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
                    SocketUtils.sendGroupMessageToSocket("GroupChatWindow",mContext, chatListEntity, chatMessageEntity, groupMemberList);
                } else {
                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                }
                chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
                dbHelper.insertChatMessage(chatMessageEntity);
                notifyMessageList();

                txtMessage.setText("");
                setMessageHintDestructionTime();
            } else {
                searchPublicKeys(chatMessageEntity);
            }
        } else {

            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
            chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
            dbHelper.insertChatMessage(chatMessageEntity);
            notifyMessageList();
            txtMessage.setText("");
            setMessageHintDestructionTime();
        }
        dbHelper.updateChatListTimeStamp(chatListEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());
    }

    private void sendRevisedTextMessage(String messageText) {
        if (originaltext.equalsIgnoreCase(messageText)) {
            txtMessage.setText("");
            isRevised = false;
            btnSendMessage.setImageResource(R.drawable.ic_mic_white);
            return;
        } else if (!dbHelper.checkMessageId(revisedChatMessageEntity.getMessageId())) {
            CommonUtils.showInfoMsg(mContext, "No message found");
            txtMessage.setText("");
            isRevised = false;
            btnSendMessage.setImageResource(R.drawable.ic_mic_white);
            return;
        }
        ChatMessageEntity chatMessageEntity = revisedChatMessageEntity;
        chatMessageEntity.setMessage(messageText);

        if (NetworkUtils.isNetworkConnected(mContext)) {
            if (checkGroupMembersKey()) {
                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                    chatMessageEntity.setParentMessageId(chatMessageEntity.getMessageId());
                    chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
                    dbHelper.updateParentMessageIDByMessageId(chatMessageEntity.getMessageId(), chatMessageEntity.getParentMessageId());
                    chatMessageEntity.setMessageId(String.valueOf(System.currentTimeMillis()));
                    dbHelper.updateMessageIDByParentMessageId(chatMessageEntity.getMessageId(), chatMessageEntity.getParentMessageId());
                    chatMessageEntity.setEditedMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
                    dbHelper.updateMessageBurnDate(chatMessageEntity.getMessageId(), DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
                    dbHelper.updateEditedMessageTimeStamp(chatMessageEntity.getMessageId(), chatMessageEntity.getEditedMessageTimeStamp());
                    dbHelper.updateIsRevised(chatMessageEntity.getMessageId(), AppConstants.revised);
                    SocketUtils.sendRGroupMessageToSocket(mContext, chatListEntity, chatMessageEntity, groupMemberList);

                    dbHelper.updateMessageTextByMessageId(chatMessageEntity.getMessageId(), messageText);
                    notifyMessageList();
                    isRevised = false;
                    txtMessage.setText("");
                } }
        }
        dbHelper.updateChatListTimeStamp(chatListEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());
    }

    private void notifyMessageList() {
        ArrayList<ChatMessageEntity> list = dbHelper.getMessageList(chatListEntity.getId(), chatListEntity.getChatType());
        messageList = getMessageList(list, messageList);
        mAdapter = new GroupChatWindowAdapter(mContext, messageList, this, GroupChatWindowAdapter.SELECT_MODE);
        mRecycler.swapAdapter(mAdapter, false);
        mRecycler.smoothScrollToPosition(messageList.size());
    }

    private ArrayList<ChatMessageEntity> getMessageList(ArrayList<ChatMessageEntity> list, ArrayList<ChatMessageEntity> messageList) {
        for (ChatMessageEntity messageEntity : list) {
            for (ChatMessageEntity entity : messageList) {
                if (!TextUtils.isEmpty(messageEntity.getMessageId()) && !TextUtils.isEmpty(entity.getMessageId()) && messageEntity.getMessageId().equalsIgnoreCase(entity.getMessageId()) && entity.isSelected()) {
                    messageEntity.setSelected(true);
                }

            }

        }
        return list;
    }

    private boolean checkGroupMembersKey() {
        boolean keysFound = false;
        for (int i = 0; i < groupMemberList.size(); i++) {
            synchronized (this) {
                if (!(dbHelper.checkPublicKeysOfUser(groupMemberList.get(i).getUserDbId()))) {
                    keysFound = false;
                    break;
                } else {
                    keysFound = true;
                }

            }
        }
        return keysFound;
    }

    private void sendMultimediaMessage(String filePath, int fileMimeType) {
        sendMultimediaMessageToSocketOff(fileMimeType, "", filePath);
    }

    private boolean isTextMessage() {
        boolean isText = false;
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).isSelected() && messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_TEXT)
                isText = true;
        }
        return isText;
    }

    private boolean isMultimedia() {
        boolean isMultimedia = false;
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).isSelected() && messageList.get(i).getMessageMimeType() != AppConstants.MIME_TYPE_TEXT)
                isMultimedia = true;
        }
        return isMultimedia;
    }

    private boolean isMultimediaVault() {
        boolean isMultimedia = true;
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).isSelected())
                if (messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_AUDIO || messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_VIDEO)
                    isMultimedia = false;
        }
        return isMultimedia;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (SELECT_MODE)
            if (getSelectedMessageCount() == 1 && isTextMessage() && isRevised)
                getMenuInflater().inflate(R.menu.chat_window_select_mode_with_copy_and_edit, menu);
            else if (getSelectedMessageCount() > 0 && isTextMessage())
                getMenuInflater().inflate(R.menu.chat_window_select_mode_with_copy, menu);
            else if (getSelectedMessageCount() > 0 && !isMultimedia())
                getMenuInflater().inflate(R.menu.chat_window_select_mode, menu);
            else if (getSelectedMessageCount() < 2 && isMultimediaVault())
                getMenuInflater().inflate(R.menu.chat_window_select_mode, menu);
            else
                getMenuInflater().inflate(R.menu.chat_window_select_mode_not_save, menu);
        else
            getMenuInflater().inflate(R.menu.chat_window_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private boolean isMyMessage(ArrayList<ChatMessageEntity> selectedMessage) {
        return selectedMessage.get(0).getSenderId() == Integer.valueOf(User_settings.getUserId(this));
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_dec_time:
                new DialogDestructTimer(mContext, chatListEntity, new DestructTimeDialogResponse() {
                    @Override
                    public void onTimeChange(int time) {
                        dbHelper.updateChatListBurnTime(chatListEntity.getId(), time);
                        chatListEntity.setBurnTime(time);
                       // txtMessage.setHint(getString(R.string.your_message_will_delete_in_s, CommonUtils.getBurnTime(mContext, time, AppConstants.TIME_TEXT_TYPE_SMALL)));
                    }

                    @Override
                    public void onClose() {

                    }
                }).show();
                break;

            case R.id.action_group_info:
                Intent groupDetailActivityIntent = new Intent(mContext, GroupDetailActivity.class);
                groupDetailActivityIntent.putExtra(AppConstants.EXTRA_GROUP_NAME, chatListEntity.getName());
                groupDetailActivityIntent.putExtra(AppConstants.EXTRA_GROUP_ID, chatListEntity.getUserDbId());
                groupDetailActivityIntent.putExtra("ecc_id", chatListEntity.getEccId());
                startActivity(groupDetailActivityIntent);
                break;
            case R.id.action_copy:
                copyMessage();
                break;
            case R.id.action_lock:
                Lock.getInstance(this).lockApplication();
                break;
            case R.id.action_mute_notification:
                break;

            case R.id.action_share_from_vault:
                break;

            case R.id.action_share_personal_notes:
                if (isGroupMember()) {
                    if (User_settings.getUserActiveStatus(mContext)) {
                        if (User_settings.getInventryStatus(mContext)) {
                            if (User_settings.getSubscriptionStatus(mContext)) {
                                flag=false;
                                Intent intent = new Intent(mContext, ShareFromVaultActivity.class);
                                intent.putExtra(AppConstants.EXTRA_ITEM_TYPE, AppConstants.ITEM_TYPE_NOTES);
                                startActivityForResult(intent, REQUEST_VAULT_ITEM_SELECT);
                            } else {
                                CommonUtils.showInfoMsg(mContext, "Your subscription has been expired. Please renew.");
                            }
                        } else {
                            CommonUtils.showInfoMsg(mContext, "You do not have any plan yet.");
                        }
                    } else {
                        CommonUtils.showInfoMsg(mContext, "Your account has been temporarily suspended. Please try later!");
                    }
                } else {
                    CommonUtils.showErrorMsg(mContext, getString(R.string.you_are_no_longer_member_of_the_group));
                }

                break;

            case R.id.action_share_chat_message:
                if (isGroupMember()) {
                    if (User_settings.getUserActiveStatus(mContext)) {
                        if (User_settings.getInventryStatus(mContext)) {
                            if (User_settings.getSubscriptionStatus(mContext)) {
                                flag=false;
                                Intent intent1 = new Intent(mContext, ShareFromVaultActivity.class);
                                intent1.putExtra(AppConstants.EXTRA_ITEM_TYPE, AppConstants.ITEM_TYPE_CHATS);
                                startActivityForResult(intent1, REQUEST_VAULT_ITEM_SELECT);
                            } else {
                                CommonUtils.showInfoMsg(mContext, "Your subscription has been expired. Please renew.");
                            }
                        } else {
                            CommonUtils.showInfoMsg(mContext, "You do not have any plan yet.");
                        }
                    } else {
                        CommonUtils.showInfoMsg(mContext, "Your account has been temporarily suspended. Please try later!");
                    }
                } else {
                    CommonUtils.showErrorMsg(mContext, getString(R.string.you_are_no_longer_member_of_the_group));
                }

                break;

            case R.id.action_share_image:
                if (isGroupMember()) {
                    flag=false;
                    Intent intent2 = new Intent(mContext, ShareFromVaultActivity.class);
                    intent2.putExtra(AppConstants.EXTRA_ITEM_TYPE, AppConstants.ITEM_TYPE_PICTURE);
                    startActivityForResult(intent2, REQUEST_VAULT_ITEM_SELECT);
                } else {
                    CommonUtils.showErrorMsg(mContext, getString(R.string.you_are_no_longer_member_of_the_group));
                }

                break;

            case R.id.action_delete:
               /* if (getSelectedMessageCount() > 0) {
                    for (int i = 0; i < messageList.size(); i++) {
                        if (messageList.get(i).isSelected()) {
                            dbHelper.deleteMessageListEntity(DbConstants.KEY_MESSAGE_ID, messageList.get(i).getMessageId());
                        }
                    }
                    deActiveSelectMode();
                    setAdapter();
                } else {
                    CommonUtils.showErrorMsg(mContext, getString(R.string.messages_not_selected));
                }*/

                final ArrayList<ChatMessageEntity> selectedMessage = getSelectedMessage();
                if (selectedMessage.size() > 0) {

                    String msg;

                    if (selectedMessage.size() > 1) {
                        msg = "Delete " + selectedMessage.size() + " messages.";
                    } else {
                        msg = "Delete " + selectedMessage.size() + " message.";
                    }
                    new DeleteMessageDialog(this, msg, isAllMessageIsNew(selectedMessage), new DeleteMessageDialog.DeleteMessageListener() {
                        @Override
                        public void deleteForMe() {
                            for (ChatMessageEntity entity : selectedMessage) {
                                dbHelper.deleteMessageListEntity(DbConstants.KEY_MESSAGE_ID, entity.getMessageId());
                            }
                            deActiveSelectMode();
                        }

                        @Override
                        public void onCancel() {
                            deActiveSelectMode();
                        }

                        @Override
                        public void deleteForEveryOne() {
                            if (SocketUtils.sendDeleteMesageToSocketGroup(GroupChatWindowActivity.this, chatListEntity, selectedMessage)) {
                                for (ChatMessageEntity entity : selectedMessage) {
                                    dbHelper.updateMimeTime(entity.getMessageId(), AppConstants.MIME_TYPE_DELETE);
                                    deActiveSelectMode();
                                }
                            } else {
                                CommonUtils.showInfoMsg(GroupChatWindowActivity.this, getString(R.string.please_try_again));
                            }
                        }
                    }).show();
                }
                break;

            case R.id.action_save_chat:
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if (messageList.size() > 0)
                            saveAllChat();
                        else
                            CommonUtils.showInfoMsg(mContext, "No Messages");
                        return null;
                    }
                }.execute();
                break;
            case R.id.action_save_to_vault:
                if (chatMessageEntityVault.getMessageMimeType() == AppConstants.MIME_TYPE_TEXT) {
                    VaultEntity vaultEntity = new VaultEntity();
                    vaultEntity.setMimeType(AppConstants.ITEM_TYPE_CHATS);
                    vaultEntity.setName(chatListEntity.getName());
                    vaultEntity.setEccId(chatListEntity.getEccId());
                    vaultEntity.setChatType(1);
                    vaultEntity.setDateTimeStamp(DateTimeUtils.getCurrentDateTime());
                    vaultEntity.setDbId(chatListEntity.getId());
                    vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));
                    int id = (int) dbHelper.insertVaultItem(vaultEntity);
                    dbHelper.updateDbID(chatListEntity.getId(), id);

                    for (int i = 0; i < messageList.size(); i++) {
                        if (messageList.get(i).isSelected()) {
                            if (messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_TEXT) {
                                messageList.get(i).setChatId(id);
                                dbHelper.insertVaultMessage(messageList.get(i));
                            }

                        }
                    }
                } else if (chatMessageEntityVault.getMessageMimeType() == AppConstants.MIME_TYPE_IMAGE)
                    saveImageToVault(chatMessageEntityVault);

                else if (chatMessageEntityVault.getMessageMimeType() == AppConstants.MIME_TYPE_NOTE) {
                    saveNoteToVault(chatMessageEntityVault);
                }

                deActiveSelectMode();

                break;

            case R.id.action_forward:
                if (getSelectedMessageCount() > 0) {

                    if(!CommonUtils.isMyServiceRunning(mContext, SendMessageOfflineService.class)){
                        BackGroundService();
                    }

                    Intent i = new Intent(mContext, ForwardMessageActivity.class);
                    i.putExtra(AppConstants.EXTRA_MESSAGE_LIST, getSelectedMessage());
                    i.putExtra(AppConstants.IS_ENCRYPTED, true);
                    startActivity(i);
                    deActiveSelectMode();
                    isforward = true;
                }
                break;

            case R.id.action_edit:
                txtMessage.setText(getSelectedMessage().get(0).getMessage());
                originaltext = getSelectedMessage().get(0).getMessage();
                revisedChatMessageEntity = getSelectedMessage().get(0);
                deActiveSelectMode();
                btnSendMessage.setImageResource(R.drawable.ic_clear);
                btnSendMessage.setOnTouchListener(null);
                isRevised = true;
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean isAllMessageIsNew(ArrayList<ChatMessageEntity> selectedMessage) {
        boolean isIt = false;
        for (ChatMessageEntity entity : selectedMessage) {
            if (entity.getMessageMimeType() == AppConstants.MIME_TYPE_DELETE || entity.getSenderId() != Integer.parseInt(User_settings.getUserId(this))) {
                isIt = false;
                break;
            }
            Date messageTime = DateTimeUtils.getDateTimeFromString(entity.getMessageTimeStamp(), "yyyy-MM-dd HH:mm:ss");
            Date currentTime = DateTimeUtils.getDateTimeFromString(DateTimeUtils.getCurrentDate(), "yyyy-MM-dd HH:mm:ss");

            if (currentTime.getTime() - messageTime.getTime() < (1000 * 60 * 5)) {
                isIt = true;
            } else {
                isIt = false;
                break;
            }
        }
        return isIt;
    }

    private void copyMessage() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", getCopyText());
        clipboard.setPrimaryClip(clip);
        deActiveSelectMode();
        CommonUtils.showInfoMsg(mContext, getString(R.string.successfully_copied));
    }

    private String getCopyText() {
        String text = "";
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).isSelected() && messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_TEXT)
                text = text+ " "+ messageList.get(i).getMessage();
        }
        return text;
    }


    private void checkStorageAndCameraPermission() {
        AppConstants.onpermission = true;
        TedPermission.with(mContext)
                .setDeniedMessage(getString(R.string.if_you_reject_permission_you_can_not_use_this_service_n_nplease_turn_on_permissions_at_setting_permission))
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        CommonUtils.createTempFile();
                        takePhotos();
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {

                    }
                })
                .check();
    }
    ///////////////////////////////////////////////BackGroundService////////////////////////////


    private void BackGroundService() {
        Intent intent = new Intent(this,SendMessageOfflineService.class);
        startService(intent);
    }








    //////////////////////////////////////////////////////////////////////////////////////



    private void takePhotos() {
        AppConstants.isbackground = false;
        FilePickerBuilder.getInstance().setMaxCount(1)
                .setSelectedFiles(photoPaths)
                .setActivityTheme(R.style.AppTheme)
                .enableImagePicker(true)
                .enableVideoPicker(false)
                .enableCameraSupport(false)
                .pickPhoto(mActivity);
    }

    @Override
    public void onItemClick(ChatMessageEntity chatMessageEntity, int position) {
        if (SELECT_MODE) {
            chatMessageEntityVault = new ChatMessageEntity();
            chatMessageEntityVault = chatMessageEntity;
            toggleSelection(position);
            setMessageCount();
            invalidateOptionsMenu();
        } else {
            if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_AUDIO) {
                if(chatMessageEntity.getAudioPath().length()==0){
                    DialogMediaPlayer dialogMediaPlayer = new DialogMediaPlayer(mContext, chatMessageEntity.getMessage(), (isResponseOk, response) -> {
                    });
                    dialogMediaPlayer.show();
                } else {
                    String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getAudioPath());
                    if (decryptedFilePath.length() > 0) {
                        imageItemPosition = mLayoutManager.findLastVisibleItemPosition();
                        dialogMediaPlayer = new DialogMediaPlayer(mContext, decryptedFilePath, (isResponseOk, response) -> {
                        });
                        dialogMediaPlayer.show();
                    } else {
                        CommonUtils.showErrorMsg(mContext, getString(R.string.audio_cannot_be_decrypted_try_again));
                    }
                }
            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_CONTACT) {
                if(chatMessageEntity.getContactPath().length()==0){
                    Intent intent = new Intent(mContext, ContactDetailsActivity.class);
                    intent.putExtra(AppConstants.EXTRA_CONTACT_ENTITY, chatMessageEntity.getMessage());
                    startActivity(intent);
                } else {
                    String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getContactPath());
                    imageItemPosition = mLayoutManager.findLastVisibleItemPosition();
                    Intent intent = new Intent(mContext, ContactDetailsActivity.class);
                    intent.putExtra(AppConstants.EXTRA_CONTACT_ENTITY, decryptedFilePath);
                    startActivity(intent);
                }
            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_IMAGE) {
                if(chatMessageEntity.getImagePath().length()==0){
                    Intent intent = new Intent(mContext, PhotoViewActivity.class);
                    intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
                    intent.putExtra(AppConstants.EXTRA_FROM_VAULT, false);
                    intent.putExtra(AppConstants.EXTRA_MESSAGE_ID, chatMessageEntity.getMessageId());
                    intent.putExtra(AppConstants.EXTRA_IMAGE_PATH, chatMessageEntity.getMessage());
                    intent.putExtra("file_name", new File(chatMessageEntity.getMessage()).getName());
                    startActivity(intent);
                } else {
                    String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getImagePath());

                    if (decryptedFilePath.length() > 0) {
                        imageItemPosition = mLayoutManager.findLastVisibleItemPosition();
                        Intent intent = new Intent(mContext, PhotoViewActivity.class);
                        intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
                        intent.putExtra(AppConstants.EXTRA_FROM_VAULT, false);
                        intent.putExtra(AppConstants.EXTRA_MESSAGE_ID, chatMessageEntity.getMessageId());
                        intent.putExtra(AppConstants.EXTRA_IMAGE_PATH, decryptedFilePath);
                        intent.putExtra("file_name", new File(chatMessageEntity.getImagePath()).getName());
                        startActivity(intent);
                    } else {
                        CommonUtils.showErrorMsg(mContext, getString(R.string.picture_cannot_be_decrypted_try_again));
                    }
                }
            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_NOTE) {
                if(chatMessageEntity.getFilePath().length()==0){
                    Intent i = new Intent(mContext, PersonalActivityView.class);
                    i.putExtra(AppConstants.EXTRA_PERSONAL_NOTE_FILE_PATH, chatMessageEntity.getMessage());
                    i.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
                    i.putExtra(AppConstants.EXTRA_MESSAGE_ID, chatMessageEntity.getMessageId());

                    startActivity(i);
                } else {
                    String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getFilePath());
                    imageItemPosition = mLayoutManager.findLastVisibleItemPosition();
                    Intent i = new Intent(mContext, PersonalActivityView.class);
                    i.putExtra(AppConstants.EXTRA_PERSONAL_NOTE_FILE_PATH, decryptedFilePath);
                    i.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
                    i.putExtra(AppConstants.EXTRA_MESSAGE_ID, chatMessageEntity.getMessageId());
                    startActivity(i);
                }
            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_VIDEO) {

            }
        }

    }

    @Override
    public void onItemLongPress(ChatMessageEntity chatMessageEntity, int position) {
        chatMessageEntityVault = new ChatMessageEntity();
        chatMessageEntityVault = chatMessageEntity;
        if (chatMessageEntity.getSenderId() == Integer.valueOf(User_settings.getUserId(mContext)))
            isRevised = CommonUtils.getTimeDifference(chatMessageEntity.getMessageTimeStamp());
        else
            isRevised = false;
        activeSelectMode();
        toggleSelection(position);
        setMessageCount();

    }

    @Override
    public void onItemForward(ChatMessageEntity chatMessageEntity, int position) {
        /*Intent intent = new Intent(GroupChatWindowActivity.this, ForwardMessageActivity.class);
        if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_TEXT) {
            intent.putExtra(AppConstants.EXTRA_FILE_PATH, chatMessageEntity.getMessage());
            intent.putExtra(AppConstants.EXTRA_MIEM_TYPE, AppConstants.MIME_TYPE_TEXT);
            startActivity(intent);

        } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_AUDIO) {
            mProgressDialoge = CommonUtils.showLoadingDialog(mContext);
            String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getAudioPath());
            if (mProgressDialoge != null) {
                if (mProgressDialoge.isShowing())
                    mProgressDialoge.dismiss();
            }

            if (decryptedFilePath.length() > 0) {
                intent.putExtra(AppConstants.EXTRA_FILE_PATH, decryptedFilePath);
                intent.putExtra(AppConstants.EXTRA_MIEM_TYPE, AppConstants.MIME_TYPE_AUDIO);
                startActivity(intent);
            } else {
                CommonUtils.showErrorMsg(mContext, "Audio cannot be decrypted. Try again!");
            }

        } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_CONTACT) {
            mProgressDialoge = CommonUtils.showLoadingDialog(mContext);
            String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getContactPath());
            if (mProgressDialoge != null) {
                if (mProgressDialoge.isShowing())
                    mProgressDialoge.dismiss();
            }

            if (decryptedFilePath.length() > 0) {
                intent.putExtra(AppConstants.EXTRA_FILE_PATH, decryptedFilePath);
                intent.putExtra(AppConstants.EXTRA_MIEM_TYPE, AppConstants.MIME_TYPE_CONTACT);
                startActivity(intent);
            } else {
                CommonUtils.showErrorMsg(mContext, "Contact cannot be decrypted. Try again!");
            }

        } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_IMAGE) {
            mProgressDialoge = CommonUtils.showLoadingDialog(mContext);
            String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getImagePath());
            if (mProgressDialoge != null) {
                if (mProgressDialoge.isShowing())
                    mProgressDialoge.dismiss();
            }

            if (decryptedFilePath.length() > 0) {
                intent.putExtra(AppConstants.EXTRA_FILE_PATH, decryptedFilePath);
                intent.putExtra(AppConstants.EXTRA_MIEM_TYPE, AppConstants.MIME_TYPE_IMAGE);
                startActivity(intent);
            } else {
                CommonUtils.showErrorMsg(mContext, "Picture cannot be decrypted. Try again!");
            }


        } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_NOTE) {
            mProgressDialoge = CommonUtils.showLoadingDialog(mContext);
            String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getFilePath());
            if (mProgressDialoge != null) {
                if (mProgressDialoge.isShowing())
                    mProgressDialoge.dismiss();
            }

            if (decryptedFilePath.length() > 0) {
                intent.putExtra(AppConstants.EXTRA_FILE_PATH, decryptedFilePath);
                intent.putExtra(AppConstants.EXTRA_MIEM_TYPE, AppConstants.MIME_TYPE_NOTE);
                startActivity(intent);
            } else {
                CommonUtils.showErrorMsg(mContext, "Personal not cannot be decrypted. Try again!");
            }


        } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_VIDEO) {

        }*/

    }

    @Override
    public void onRetryMessage(ChatMessageEntity chatMessageEntity, int position) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
            String messageBurnTime = DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatMessageEntity.getMessageBurnTime());
            chatMessageEntity.setMessageBurnTimeStamp(messageBurnTime);
            SocketUtils.sendGroupMessageToSocket("GroupChatWindow",mContext, chatListEntity, chatMessageEntity, groupMemberList);
            dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_SENT_STATUS);
            dbHelper.updateMessageBurnDate(chatMessageEntity.getMessageId(), messageBurnTime);
            setAdapter();
        }
    }

    private void toggleSelection(int position) {
        GroupChatWindowAdapter.checkLists[position] = !GroupChatWindowAdapter.checkLists[position];
        messageList.get(position).setSelected(!messageList.get(position).isSelected());
        mAdapter.notifyItemChanged(position);

        if (getSelectedMessageCount() == 0)
            deActiveSelectMode();
    }

    private void activeSelectMode() {
        SELECT_MODE = true;
        GroupChatWindowAdapter.SELECT_MODE = true;
        mAdapter.notifyDataSetChanged();

        invalidateOptionsMenu();
    }

    private void deActiveSelectMode() {
        SELECT_MODE = false;
        isRevised = false;
        GroupChatWindowAdapter.SELECT_MODE = false;
        GroupChatWindowAdapter.checkLists = new boolean[messageList.size()];
        unSelectAll();
        setAdapter();
        invalidateOptionsMenu();
        txtGroupName.setText(chatListEntity.getName());

    }

    private void unSelectAll() {
        for (ChatMessageEntity messageEntity : messageList) {
            if (messageEntity.isSelected()) {
                messageEntity.setSelected(false);
            }
        }
    }

    private void setMessageCount() {
        if (getSelectedMessageCount() > 0)
            txtGroupName.setText(String.valueOf(getSelectedMessageCount()));
        else
            txtGroupName.setText(chatListEntity.getName());
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
        ArrayList<ChatMessageEntity> chatMessageEntityArrayList = new ArrayList<>();
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).isSelected())
                chatMessageEntityArrayList.add(messageList.get(i));
        }
        return chatMessageEntityArrayList;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA).size() > 0) {
                        if (photoPaths != null)
                            photoPaths.clear();
                        photoPaths = new ArrayList<>();
                        photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));


                        if (new File(photoPaths.get(0)).length() / 1024 > 301)
                            compressImage(photoPaths.get(0));
                        else
                            sendMultimediaMessage(photoPaths.get(0), AppConstants.MIME_TYPE_IMAGE);

                    } else {
                        CommonUtils.showErrorMsg(mContext, getString(R.string.no_media_selected));
                    }
                }
                break;

            case REQUEST_CONTACT_SELECT:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    contacts = (ArrayList<ContactEntity>) data.getSerializableExtra(AppConstants.EXTRA_SELECTED_CONTACT);

                    if (contacts.size() > 0) {
                        setMultipleContact(true);
                        mCount = 0;
                        sendMultipleContacts(mCount);
                    }

                }
                break;


            case REQUEST_VAULT_ITEM_SELECT:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    vaultListEntities = (ArrayList<VaultEntity>) data.getSerializableExtra(AppConstants.EXTRA_SELECTED_VAULTITES);

                    if (vaultListEntities.size() > 0) {
                        setMultipleVaultItem(true);
                        mCount = 0;
                        sendMultipleVaultItems(mCount);
                    }


                } else if (resultCode == 2 && data != null) {
                    ArrayList<ChatMessageEntity> selectedMessage = (ArrayList<ChatMessageEntity>) data.getSerializableExtra(AppConstants.EXTRA_VAULT_MESSAGE);
                    boolean allTextType = true;
                    if (selectedMessage.size() > 0) {
                        for (int i = 0; i < selectedMessage.size(); i++) {
                            if (!(selectedMessage.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_TEXT)) {
                                allTextType = false;
                            }
                        }
                        if (allTextType) {
                            for (int i = 0; i < selectedMessage.size(); i++) {
                                synchronized (this) {
                                    if (User_settings.getUserActiveStatus(mContext)) {
                                        if (User_settings.getInventryStatus(mContext)) {
                                            if (User_settings.getSubscriptionStatus(mContext)) {
                                                sendTextMessage(selectedMessage.get(i).getMessage());
                                            } else {
                                                CommonUtils.showInfoMsg(mContext, "Your subscription has been expired. Please renew.");
                                            }
                                        } else {
                                            CommonUtils.showInfoMsg(mContext, "You do not have any plan yet.");
                                        }
                                    } else {
                                        CommonUtils.showInfoMsg(mContext, "Your account has been temporarily suspended. Please try later!");
                                    }
                                }
                            }
                        }
                        setAdapter();
                    }

                }
                break;

            case AppConstants.CAMERA_RQ:
                if (resultCode == RESULT_OK && data.getExtras() != null) {
                    String tempFilePathImg = data.getDataString().substring(7);
                    fileName = new File(tempFilePathImg).getName();
                    if (new File(tempFilePathImg).length() / 1024 > 301)
                        compressImage(tempFilePathImg);
                    else
                        sendMultimediaMessage(tempFilePathImg, AppConstants.MIME_TYPE_IMAGE);
                } else if (data != null) {
                    Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
                    e.printStackTrace();
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void sendMultipleContacts(int mCount) {
        if (contacts.size() > mCount) {
            String contactPath = FileUtils.createContactFile(mContext, contacts.get(mCount));
            sendMultimediaMessage(contactPath, AppConstants.MIME_TYPE_CONTACT);
        } else {
            setMultipleContact(false);
            mCount = 0;
            contacts.clear();
        }
    }

    private void sendMultipleVaultItems(int mCount) {
        if (vaultListEntities.size() > mCount) {

            if (vaultListEntities.get(mCount).getMimeType() == AppConstants.ITEM_TYPE_NOTES) {
                sendMultimediaMessage(vaultListEntities.get(mCount).getNotes(), AppConstants.MIME_TYPE_NOTE);
            } else if (vaultListEntities.get(mCount).getMimeType() == AppConstants.ITEM_TYPE_PICTURE) {
                fileName = vaultListEntities.get(mCount).getName();
                sendMultimediaMessage(vaultListEntities.get(mCount).getImage(), AppConstants.MIME_TYPE_IMAGE);
            }

        } else {
            setMultipleVaultItem(false);
            mCount = 0;
            vaultListEntities.clear();
        }
    }

    private void saveAllChat() {
        VaultEntity vaultEntity = new VaultEntity();
        vaultEntity.setMimeType(AppConstants.ITEM_TYPE_CHATS);
        vaultEntity.setName(chatListEntity.getName());
        vaultEntity.setEccId(chatListEntity.getEccId());
        vaultEntity.setChatType(1);
        vaultEntity.setDateTimeStamp(DateTimeUtils.getCurrentDateTime());
        vaultEntity.setDbId(chatListEntity.getId());
        vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));

        int id = (int) dbHelper.insertVaultItem(vaultEntity);
        dbHelper.updateDbID(chatListEntity.getId(), id);

        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_TEXT) {
                messageList.get(i).setChatId(id);
                dbHelper.insertVaultMessage(messageList.get(i));
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (SELECT_MODE) {
            deActiveSelectMode();
        } else if (isTextBoxExpanded()) {
            collapseMessageTextBox();
        } else {
            if (FragmentChats.refreshChatListListener != null) {
                FragmentChats.refreshChatListListener.onRefresh();
            }
            if (FragmentGroupChat.refreshChatListListener != null) {
                FragmentGroupChat.refreshChatListListener.onRefresh();
            }
            AppConstants.openedChatID = -1;
            chatWindowFunctionListener = null;
            super.onBackPressed();
            mActivity.finish();
        }
    }

    private boolean isTextBoxExpanded() {
        return lyrBottom.getLayoutParams().height == LinearLayout.LayoutParams.MATCH_PARENT;
    }

    private boolean isLastVisible() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) mRecycler.getLayoutManager());
        int pos = layoutManager.findLastCompletelyVisibleItemPosition() + 1;
        int numItems = mRecycler.getAdapter().getItemCount();
        return (pos >= numItems);
    }


    @Override
    public void onNewMessage(ChatMessageEntity chatMessageEntity) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_TEXT) {
                        if (chatMessageEntity.getChatId() == chatListEntity.getId()) {
                            String messageBurnTimeStamp = DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatMessageEntity.getMessageBurnTime());
                            chatMessageEntity.setMessageBurnTimeStamp(messageBurnTimeStamp);
                            dbHelper.updateMessage(chatMessageEntity.getMessageId(), messageBurnTimeStamp, AppConstants.MESSAGE_READ_STATUS);
                            if (isLastVisible()) {
                                notifyMessageList();
                                mRecycler.smoothScrollToPosition(messageList.size());
                            } else {
                                notifyMessageList();
                            }

                            if (AppConstants.lockscreen) {
                                if (chatListEntity.getSnoozeStatus() == AppConstants.NOTIFICATION_SNOOZE_NO) {
                                    dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_UNREAD_STATUS);
                                    NotificationUtils.showNotification(mContext, chatListEntity, AppConstants.NOTIFICATION_ID, mContext.getResources().getString(R.string.title_message_notification), dbHelper.getTotalUnreadMessages());
                                }
                            } else {
                                dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_READ_STATUS);

                            }

                        } else {
                            if (chatListEntity.getSnoozeStatus() == AppConstants.NOTIFICATION_SNOOZE_NO) {

                                NotificationUtils.showNotification(mContext, chatListEntity, AppConstants.NOTIFICATION_ID, mContext.getResources().getString(R.string.title_message_notification), dbHelper.getTotalUnreadMessages());
                                playNewMessageSound();
                            }
                        }
                    } else {
                        if (chatMessageEntity.getChatId() == chatListEntity.getId()) {
                            if (AppConstants.lockscreen) {
                                if (chatListEntity.getSnoozeStatus() == AppConstants.NOTIFICATION_SNOOZE_NO) {
                                    NotificationUtils.showNotification(mContext, chatListEntity, AppConstants.NOTIFICATION_ID, mContext.getResources().getString(R.string.title_message_notification), dbHelper.getTotalUnreadMessages());
                                }
                            } else {
                                chatMessageEntity.setCurrentMessageStatus(AppConstants.MESSAGE_READ_STATUS);
                                String messageBurnTimeStamp = DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatMessageEntity.getMessageBurnTime());
                                chatMessageEntity.setMessageBurnTimeStamp(messageBurnTimeStamp);
                                dbHelper.updateMessage(chatMessageEntity.getMessageId(), messageBurnTimeStamp, AppConstants.MESSAGE_READ_STATUS);
                                setAdapter();
                                playNewMessageSound();
                            }
                        } else {
                            if (chatListEntity.getSnoozeStatus() == AppConstants.NOTIFICATION_SNOOZE_NO) {
                                NotificationUtils.showNotification(mContext, chatListEntity, AppConstants.NOTIFICATION_ID, mContext.getResources().getString(R.string.title_message_notification), dbHelper.getTotalUnreadMessages());

                            }
                        }

                    }
                    showMessageCountBadge();
                }
            }
        });

    }



    @Override
    public void onMessageAck(String messageId, int status) {
        int position = getPosition(messageId);

        if (position == -1)
            return;

        if (position > messageList.size()) {
            onMessageAck(messageId, status);
        }

        if (position <= messageList.size())
            this.runOnUiThread(() -> {
                if (position > messageList.size()) {
                    return;
                }
                messageList.get(position).setMessageStatus(status);
                mAdapter.notifyItemChanged(position);
            });

    }

    private int getPosition(String messageId) {
        int pos = -1;
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getMessageId().equalsIgnoreCase(messageId))
                pos = i;
        }
        return pos;
    }




    @Override
    public void onDeleteMessage(ChatMessageEntity chatMessageEntity) {
        this.runOnUiThread(this::notifyMessageList);
    }

    @Override
    public void onDeleteMessageByMessageId(String messageId) {
    }

    public void searchPublicKeys(ChatMessageEntity chatMessageEntity) {
        AndroidNetworking.post(ApiEndPoints.URL_FETCH_GROUP_ECC_KEYS)
                .addJSONObjectBody(getRawData(chatListEntity.getUserDbId()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                                                try {
                            new PublicKeysParser().parseJson(mActivity, response.toString(), groupMemberList);

                            if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                                chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
                                SocketUtils.sendGroupMessageToSocket("GroupChatWindow",mContext, chatListEntity, chatMessageEntity, groupMemberList);
                            } else {
                                chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                            }
                            chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));

                            dbHelper.insertChatMessage(chatMessageEntity);
                            notifyMessageList();
                            txtMessage.setText("");
                            setMessageHintDestructionTime();


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        CommonUtils.showInfoMsg(mContext, getString(R.string.user_s_public_key_not_found));
                        chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                        chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
                        dbHelper.insertChatMessage(chatMessageEntity);
                        notifyMessageList();
                        txtMessage.setText("");
                        setMessageHintDestructionTime();
                    }
                });
    }

    public void searchPublicKeys() {
        AndroidNetworking.post(ApiEndPoints.URL_FETCH_GROUP_ECC_KEYS)
                .addJSONObjectBody(getRawData(chatListEntity.getUserDbId()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            new PublicKeysParser().parseJson(mActivity, response.toString(), groupMemberList);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        CommonUtils.showInfoMsg(mContext, getString(R.string.user_s_public_key_not_found));
                    }
                });
    }

    public JSONObject getRawData(int groupID) {
        List<String> eccId = getMembersECCID(groupID);
        JSONObject jsonObject = new JSONObject();
        try {
            String json;

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

    public List<String> getMembersECCID(int groupId) {
        ArrayList<GroupMemberEntity> mList = dbHelper.getGroupMemberList(groupId);
        List<String> eccId = new ArrayList<>();
        int size = mList.size();
        for (int i = 0; i < size; i++) {
            if (!dbHelper.checkPublicKeysOfUser(mList.get(i).getUserDbId())) {
                eccId.add(mList.get(i).getEccId());
            }
        }
        return eccId;
    }

    private void setMessageHintDestructionTime() {
       // txtMessage.setHint(getString(R.string.your_message_will_delete_in_s, CommonUtils.getBurnTime(mContext, chatListEntity.getBurnTime(), AppConstants.TIME_TEXT_TYPE_SMALL)));
    }

    public void sendFilesToServerAndSocket(String encryptedFilePath, String httpMethod, String serverUrl, String uploadId, int fileMimeType) {
        try {

            Ion.with(mContext)
                    .load(serverUrl)
                    .setMultipartParameter("json_data", getMultimediaJSONParameter(fileMimeType))
                    .setMultipartFile("user_files", new File(encryptedFilePath))
                    .asJsonObject()
                    .setCallback((e, result) -> {
                        if (e != null) {
                        } else {
                            try {
                                JSONObject rootObject = new JSONObject(result.toString());

                                String url = rootObject.getString("url");
                                if (fileMimeType == AppConstants.MIME_TYPE_IMAGE) {
                                    url = url + AppConstants.EXTRA_HIND + fileName;
                                }
                                sendMultimediaMessageToSocket(fileMimeType, encryptedFilePath, url);
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                                CommonUtils.showErrorMsg(mContext, getString(R.string.file_not_sent));
                            }
                        }

                    });


        } catch (Exception exc) {
            Log.e(TAG, "onDone: " + "fail");
            CommonUtils.showErrorMsg(mContext, getString(R.string.file_not_sent));
        }
    }

    private void sendMultimediaMessageToSocket(int mimeType, String filePath, String fileUrl) {
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setSenderId(Integer.parseInt(User_settings.getUserId(mContext)));
        chatMessageEntity.setMessageMimeType(mimeType);
        chatMessageEntity.setMessageId(String.valueOf(System.currentTimeMillis()));
        chatMessageEntity.setChatId(chatListEntity.getId());
        chatMessageEntity.setReceiverId(chatListEntity.getUserDbId());
        chatMessageEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
        chatMessageEntity.setMessageBurnTime(chatListEntity.getBurnTime());
        chatMessageEntity.setChatUserDbId(chatMessageEntity.getChatUserDbId());
        chatMessageEntity.setChatType(AppConstants.GROUP_CHAT_TYPE);
        chatMessageEntity.setMessage(fileUrl);
        chatMessageEntity.setFileName(fileName);
        chatMessageEntity.setEddId(User_settings.getECCID(mContext));
        chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
        if (mimeType == AppConstants.MIME_TYPE_AUDIO) {
            chatMessageEntity.setAudioPath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_VIDEO) {
            chatMessageEntity.setVideoPath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_NOTE) {
            chatMessageEntity.setFilePath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_IMAGE) {
            chatMessageEntity.setImagePath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_CONTACT) {
            chatMessageEntity.setContactPath(filePath);
        }
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
            SocketUtils.sendGroupMessageToSocket("GroupChatWindow",mContext, chatListEntity, chatMessageEntity, groupMemberList);
        } else {
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
        }
        dbHelper.insertChatMessage(chatMessageEntity);
        notifyMessageList();
        txtMessage.setText("");
        dbHelper.updateChatListTimeStamp(chatListEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());

        if (isMultipleContact()) {
            mCount = mCount + 1;
            sendMultipleContacts(mCount);
        }


        if (isMultipleVaultItem()) {
            mCount = mCount + 1;
            sendMultipleVaultItems(mCount);
        }
        fileName = "";

    }

    private void sendMultimediaMessageToSocketOff(int mimeType, String filePath, String fileUrl) {
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setSenderId(Integer.parseInt(User_settings.getUserId(mContext)));
        chatMessageEntity.setMessageMimeType(mimeType);
        chatMessageEntity.setMessageId(String.valueOf(System.currentTimeMillis()));
        chatMessageEntity.setChatId(chatListEntity.getId());
        chatMessageEntity.setReceiverId(chatListEntity.getUserDbId());
        chatMessageEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
        chatMessageEntity.setMessageBurnTime(chatListEntity.getBurnTime());
        chatMessageEntity.setChatUserDbId(chatMessageEntity.getChatUserDbId());
        chatMessageEntity.setChatType(AppConstants.GROUP_CHAT_TYPE);
        chatMessageEntity.setMessage(fileUrl);
        chatMessageEntity.setFileName(fileName);
        chatMessageEntity.setEddId(User_settings.getECCID(mContext));
        chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
        if (mimeType == AppConstants.MIME_TYPE_AUDIO) {
            chatMessageEntity.setAudioPath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_VIDEO) {
            chatMessageEntity.setVideoPath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_NOTE) {
            chatMessageEntity.setFilePath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_IMAGE) {
            chatMessageEntity.setImagePath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_CONTACT) {
            chatMessageEntity.setContactPath(filePath);
        }
        chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);

        dbHelper.insertChatMessage(chatMessageEntity);

        notifyMessageList();
        txtMessage.setText("");
        dbHelper.updateChatListTimeStamp(chatListEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());

        if (isMultipleContact()) {
            mCount = mCount + 1;
            sendMultipleContacts(mCount);
        }


        if (isMultipleVaultItem()) {
            mCount = mCount + 1;
            sendMultipleVaultItems(mCount);
        }
        fileName = "";
        startBackGroundThreadForMediaMessages();
    }

    public String getMultimediaJSONParameter(int mimeType) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("sender_id", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObj.put("group_id", chatListEntity.getUserDbId());
            if (mimeType == AppConstants.MIME_TYPE_AUDIO) {
                jsonObj.put("mime_type", "Audio");
            } else if (mimeType == AppConstants.MIME_TYPE_CONTACT) {
                jsonObj.put("mime_type", "Image");
            } else if (mimeType == AppConstants.MIME_TYPE_IMAGE) {
                jsonObj.put("mime_type", "Image");
            } else if (mimeType == AppConstants.MIME_TYPE_NOTE) {
                jsonObj.put("mime_type", "Image");
            } else if (mimeType == AppConstants.MIME_TYPE_VIDEO) {
                jsonObj.put("mime_type", " Video");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObj.toString();
    }

    public void startBackGroundThread() {
        isRunning = true;
        backGroundHandler.postDelayed(new Runnable() {
            public void run() {
                if (isRunning) {
                    if (!SELECT_MODE) {
                        synchronized (this) {
                            if (dbHelper.checkMessageAge(chatListEntity.getId()) > 0) {
                                dbHelper.deleteOldMessage(chatListEntity.getId());
                                setAdapter();
                            }
                        }
                    }
                    backGroundHandler.postDelayed(this, TWO_SECONDS);
                }
            }
        }, 3);
    }

    public void stopBackgroundThread() {
        FileLog.e("Socket : ", "is stopped.");
        backGroundHandler.removeCallbacksAndMessages(null);
        isRunning = false;

    }

    public void startBackGroundThreadForUnsentMessages() {
        isRunningUnsentMessages = true;
        backGroundHandlerUnsentMessages.postDelayed(new Runnable() {
            public void run() {
                if (isRunningUnsentMessages) {
                    if (!SELECT_MODE) {
                        synchronized (this) {
                            if (NetworkUtils.isNetworkConnected(mContext)) {
                                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                                    if (messageList.size() > 0) {
                                        for (int i = 0; i < messageList.size(); i++) {

                                            if (messageList.get(i).getMessageStatus() == AppConstants.MESSAGE_NOT_SENT_STATUS) {
                                                if(messageList.get(i).getMessageMimeType()==AppConstants.MIME_TYPE_IMAGE && messageList.get(i).getImagePath().length()==0){

                                                } else if(messageList.get(i).getMessageMimeType()==AppConstants.MIME_TYPE_NOTE && messageList.get(i).getFilePath().length()==0){

                                                } else if(messageList.get(i).getMessageMimeType()==AppConstants.MIME_TYPE_AUDIO && messageList.get(i).getAudioPath().length()==0){

                                                } else if(messageList.get(i).getMessageMimeType()==AppConstants.MIME_TYPE_CONTACT && messageList.get(i).getContactPath().length()==0){

                                                } else {
                                                    ChatMessageEntity chatMessageEntity = messageList.get(i);
                                                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
                                                    String messageBurnTime = DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatMessageEntity.getMessageBurnTime());
                                                    chatMessageEntity.setMessageBurnTimeStamp(messageBurnTime);
                                                    SocketUtils.sendGroupMessageToSocket("GroupChatWindow",mContext, chatListEntity, chatMessageEntity, groupMemberList);
                                                    dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_SENT_STATUS);
                                                    dbHelper.updateMessageBurnDate(chatMessageEntity.getMessageId(), messageBurnTime);
                                                    messageList.get(i).setMessageStatus(AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
                                                    messageList.get(i).setMessageBurnTimeStamp(messageBurnTime);
                                                    mAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    backGroundHandlerUnsentMessages.postDelayed(this, TWO_SECONDS);
                }
                startBackGroundThreadForMediaMessages();
            }
        }, 3);
    }

    public void stopBackgroundThreadUnsentMessages() {
        backGroundHandlerUnsentMessages.removeCallbacksAndMessages(null);
        isRunningUnsentMessages = false;

    }

    @Override
    protected void onPause() {
        super.onPause();
        chatWindowFunctionListener = null;
        isPaused = true;
        stopBackgroundThread();
        stopBackgroundThreadUnsentMessages();
        stopBackGroundThreadForMediaMessages();
        if (AppConstants.onpermission) {
            AppConstants.onpermission = false;
            AppConstants.isbackground = false;
        } else {
            AppConstants.isbackground = true;
        }
        HomeActivity.runnable = new Runnable() {
            @Override
            public void run() {


                if (AppConstants.isbackground) {
                    Log.e("Tag", "onPause: " + "background-012");
                    if (dialogMediaPlayer != null && dialogMediaPlayer.isShowing()) {
                        dialogMediaPlayer.dismiss();
                    }
                    CommonUtils.lockDialog(mActivity);

                } else {
                    Log.e("Tag", "onPause: " + "forground-012");
                }


            }
        };
        HomeActivity.lockHandler.postDelayed(HomeActivity.runnable, User_settings.getLockTime(mContext));
       groupUpdateListener = null;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBackgroundThread();
        stopBackgroundThreadUnsentMessages();
        stopBackGroundThreadForMediaMessages();
        AppConstants.chatId = 0;
        AppConstants.chatType =-1;
        if (dialogMediaPlayer != null && dialogMediaPlayer.isShowing()) {
            dialogMediaPlayer.dismiss();
        }
    }

    public boolean isMultipleContact() {
        return isMultipleContact;
    }

    public void setMultipleContact(boolean multipleContact) {
        isMultipleContact = multipleContact;
    }

    public boolean isMultipleVaultItem() {
        return isMultipleVaultItem;
    }

    public void setMultipleVaultItem(boolean multipleVaultItem) {
        isMultipleVaultItem = multipleVaultItem;
    }


    //  ******************************************COMPRESS*************************

    @OnClick(R.id.txt_group_name)
    public void onViewClicked() {
        Intent i = new Intent(mContext, GroupDetailActivity.class);
        i.putExtra(AppConstants.EXTRA_GROUP_NAME, chatListEntity.getName());
        i.putExtra(AppConstants.EXTRA_GROUP_ID, chatListEntity.getUserDbId());
        i.putExtra("ecc_id", chatListEntity.getEccId());
        startActivity(i);
    }


    //******************************************Audio Recording***********************************

    private void compressImage(String file) {
        try {
            final InputStream is = new FileInputStream(file);
            File outfile = new File(CommonUtils.getImageDirectory(mContext), "tempCom.jpg");
            if (outfile.exists())
                outfile.delete();
            FileOutputStream fos = new FileOutputStream(outfile);
            byte[] buffer = new byte[4096];
            int len = -1;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            is.close();

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = mConfig;
            Bitmap originBitmap = BitmapFactory.decodeFile(outfile.getAbsolutePath(), options);


            String logMessage = "origin file size:" + Formatter.formatFileSize(mContext, outfile.length())
                    + "\nwidth:" + originBitmap.getWidth() + ",height:" + originBitmap.getHeight() + ",config:" + originBitmap.getConfig();
            Log.e(TAG, "Original File  : " + logMessage);

            Tiny.FileCompressOptions compressOptions = new Tiny.FileCompressOptions();
            compressOptions.config = mConfig;
            compressOptions.outfile = file;

            Tiny.getInstance().source(outfile).asFile().withOptions(compressOptions).compress((isSuccess, outfile1, t) -> {
                if (!isSuccess) {
                    Log.e("zxy", "error: " + t.getMessage());

                    return;
                }
                File file1 = new File(outfile1);
                String logMessage1 = "compress file size:" + Formatter.formatFileSize(mContext, file1.length())
                        + "\noutfile: " + outfile1;
                Log.e(TAG, "Compressed File  : " + logMessage1);

                sendMultimediaMessage(outfile1, AppConstants.MIME_TYPE_IMAGE);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (User_settings.getUserActiveStatus(mContext)) {
            if (User_settings.getInventryStatus(mContext)) {
                if (User_settings.getSubscriptionStatus(mContext)) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                        String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
                        if (CommonUtils.hasPermissions(mContext, PERMISSIONS)) {
                            if (isTextBoxExpanded()) {
                                collapseMessageTextBox();
                            }
                            showRecordingView();
                            x1 = motionEvent.getX();
                            y1 = motionEvent.getY();

                            startRecording();
                        } else {
                            askRecordingPermissions();
                        }
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP
                            || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                        if(recorder!=null) {
                            hideRecordingView();
                            x2 = motionEvent.getX();
                            y2 = motionEvent.getY();
                            dx = Math.abs(x2 - x1);
                            dy = Math.abs(y2 - y1);

                            if (dx > 80) {
                                stopRecording(false);
                            } else {
                                stopRecording(true);
                            }
                            txtMessage.requestFocus();
                        }
                    }
                } else {
                    CommonUtils.showInfoMsg(mContext, "Your subscription has been expired. Please renew.");
                }
            } else {
                CommonUtils.showInfoMsg(mContext, "You do not have any plan yet.");
            }
        } else {
            CommonUtils.showInfoMsg(mContext, "Your account has been temporarily suspended. Please try later!");
        }
        view.onTouchEvent(motionEvent);
        return true;
    }

    private void askRecordingPermissions() {
        AppConstants.onpermission = true;
        TedPermission.with(mContext)
                .setDeniedMessage(getString(R.string.if_you_reject_permission_you_can_not_use_this_service_n_nplease_turn_on_permissions_at_setting_permission))
                .setPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        deActiveSelectMode();
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                        deActiveSelectMode();
                    }
                })
                .check();
    }

    private void stopRecording(boolean b) {
        if (null != recorder) {

            if (timer != null) {
                timer.cancel();
            }

            if (txtRecordingTime.getText().toString().equals("00:00")) {
                try {
                    recorder.stop();
                    recorder.reset();
                    recorder.release();
                } catch (Exception e) {
                    e.printStackTrace();
                    recorder = null;
                    return;
                }
                return;
            }

            txtRecordingTime.setText("00:00");
            vibrate();
            try {
                recorder.stop();
                recorder.reset();
                recorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }

            recorder = null;
            if (b) {
                sendMultimediaMessage(audioPath, AppConstants.MIME_TYPE_AUDIO);
            } else {
                return;
            }


        }
    }

    private void startRecording() {
        audioPath = getFilename();
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioSamplingRate(16000);
        recorder.setAudioChannels(1);
        recorder.setOutputFile(audioPath);

        try {
            startTime = SystemClock.uptimeMillis();
            timer = new Timer();
            MyTimerTask myTimerTask = new MyTimerTask();
            timer.schedule(myTimerTask, 1000, 1000);
            vibrate();
            recorder.prepare();
            recorder.start();

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void vibrate() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFilename() {
        File audioFile = new File(CommonUtils.getKeyBasePath(mContext) + "Audio");
        if (!audioFile.exists()) {
            audioFile.mkdirs();
        }

        return (audioFile.getAbsolutePath() + "/" + System.currentTimeMillis() + ".aac");
    }

    private void toggleEditTextHeight() {
        if (lyrBottom.getLayoutParams().height == LinearLayout.LayoutParams.WRAP_CONTENT) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lyrBottom.setLayoutParams(layoutParams);
            txtMessage.setGravity(Gravity.START);
        } else {
            collapseMessageTextBox();
        }
    }

    private void collapseMessageTextBox() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lyrBottom.setLayoutParams(layoutParams);
        txtMessage.setGravity(Gravity.CENTER_VERTICAL);
    }

    private void hideRecordingView() {
        lyrTextMessage.setVisibility(View.VISIBLE);
        lyrRecording.setVisibility(View.GONE);
    }

    private void showRecordingView() {
        lyrTextMessage.setVisibility(View.GONE);
        lyrRecording.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNameChange() {
        this.runOnUiThread(() -> {
            chatListEntity = dbHelper.getGroupChatEntity(chatListEntity.getUserDbId());
            txtGroupName.setText(chatListEntity.getName());
        });

    }

    @Override
    public void onMemberAdd() {
        mActivity.runOnUiThread(() -> {
            onNameChange();
            groupMemberList = dbHelper.getGroupMemberList(chatListEntity.getUserDbId());
            initViews();
        });
    }

    @Override
    public void onMemberRemove(String eccId, int userId, int gId) {
        mActivity.runOnUiThread(() -> {
            groupMemberList = dbHelper.getGroupMemberList(chatListEntity.getUserDbId());
            initViews();
        });

    }

    private int getMemberPosition(String eccId) {
        int pos = -1;
        for (int i = 0; i < groupMemberList.size(); i++) {
            if (groupMemberList.get(i).getEccId().equalsIgnoreCase(eccId))
                pos = i;
        }
        return pos;
    }

    private void playNewMessageSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMessageCountBadge() {
        int messageCount = dbHelper.getTotalUnreadMessages();
        NotificationUtils.showBadge(mContext, messageCount);
    }

    private void saveImageToVault(ChatMessageEntity chatMessageEntity) {
        if(chatMessageEntity.getImagePath().length()==0){
            VaultFileSaveUtils fileSaveUtils = new VaultFileSaveUtils(dbHelper, AppConstants.ITEM_TYPE_PICTURE);
            try {
                String fileName = fileSaveUtils.getFileName( chatMessageEntity.getMessage().substring( chatMessageEntity.getMessage().lastIndexOf("/") + 1));
                Log.e(TAG, "saveImageToVault: " + fileName);
                DbHelper dbHelper = new DbHelper(this);
                VaultEntity vaultEntity = new VaultEntity();
                vaultEntity.setMimeType(AppConstants.ITEM_TYPE_PICTURE);
                vaultEntity.setName(fileName);
                vaultEntity.setImage(copy(new File(chatMessageEntity.getMessage()), new File(CommonUtils.getImageDirectory(this) + fileName + ".jpg")));
                vaultEntity.setEccId(chatListEntity.getEccId());
                vaultEntity.setDateTimeStamp(DateTimeUtils.getCurrentDateTime());
                vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));

                dbHelper.deleteVaultItembyPath(DbConstants.KEY_FILE_PATH, chatMessageEntity.getMessage());
                long id = dbHelper.insertVaultItem(vaultEntity);

                CommonUtils.showInfoMsg(this, "Saved Successfully");


            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getImagePath());
            VaultFileSaveUtils fileSaveUtils = new VaultFileSaveUtils(dbHelper, AppConstants.ITEM_TYPE_PICTURE);
            try {
                String fileName = fileSaveUtils.getFileName(chatMessageEntity.getImagePath().substring(chatMessageEntity.getImagePath().lastIndexOf("/") + 1));
                DbHelper dbHelper = new DbHelper(this);
                VaultEntity vaultEntity = new VaultEntity();
                vaultEntity.setMimeType(AppConstants.ITEM_TYPE_PICTURE);
                vaultEntity.setName(fileName);
                vaultEntity.setImage(copy(new File(decryptedFilePath), new File(CommonUtils.getImageDirectory(this) + fileName + ".jpg")));
                vaultEntity.setEccId(chatListEntity.getEccId());
                vaultEntity.setDateTimeStamp(DateTimeUtils.getCurrentDateTime());
                vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));

                dbHelper.deleteVaultItembyPath(DbConstants.KEY_FILE_PATH, decryptedFilePath);
                long id = dbHelper.insertVaultItem(vaultEntity);

                CommonUtils.showInfoMsg(this, "Saved Successfully");


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String readEncodedFile(String path) {
        File file = new File(path);

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return text.toString();
    }

    private void saveNoteToVault(ChatMessageEntity chatMessageEntity) {
        if(chatMessageEntity.getFilePath().length()==0){
            String fileName;
            String text = readEncodedFile(chatMessageEntity.getMessage());

            VaultFileSaveUtils fileSaveUtils = new VaultFileSaveUtils(dbHelper, AppConstants.ITEM_TYPE_NOTES);
            if (text.contains("!@#$%^")) {
                fileName = text.substring(0, text.indexOf("!"));
            } else {
                fileName = "Note " + System.currentTimeMillis();
            }

            fileName = fileSaveUtils.getFileName(fileName);
            try {


                DbHelper dbHelper = new DbHelper(this);
                VaultEntity vaultEntity = new VaultEntity();
                vaultEntity.setMimeType(AppConstants.ITEM_TYPE_NOTES);
                vaultEntity.setName(fileName);
                vaultEntity.setNotes(copy(new File(chatMessageEntity.getMessage()), new File(CommonUtils.getNotesDirectory(this), fileName + ".txt")));
                vaultEntity.setEccId(chatListEntity.getEccId());
                vaultEntity.setDateTimeStamp(DateTimeUtils.getCurrentDateTime());
                vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));
                dbHelper.deleteVaultItembyPath(DbConstants.KEY_FILE_PATH, chatMessageEntity.getMessage());
                long id = dbHelper.insertVaultItem(vaultEntity);
                CommonUtils.showInfoMsg(this, "Saved Successfully");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getFilePath());
            String fileName;
            String text = readEncodedFile(decryptedFilePath);
            VaultFileSaveUtils fileSaveUtils = new VaultFileSaveUtils(dbHelper, AppConstants.ITEM_TYPE_NOTES);
            if (text.contains("!@#$%^")) {
                fileName = text.substring(0, text.indexOf("!"));
            } else {
                fileName = "Note " + System.currentTimeMillis();
            }

            fileName = fileSaveUtils.getFileName(fileName);
            try {

                DbHelper dbHelper = new DbHelper(this);
                VaultEntity vaultEntity = new VaultEntity();
                vaultEntity.setMimeType(AppConstants.ITEM_TYPE_NOTES);
                vaultEntity.setName(fileName);
                vaultEntity.setNotes(copy(new File(decryptedFilePath), new File(CommonUtils.getNotesDirectory(this), fileName + ".txt")));
                vaultEntity.setEccId(chatListEntity.getEccId());
                vaultEntity.setDateTimeStamp(DateTimeUtils.getCurrentDateTime());
                vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));
                vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));

                dbHelper.deleteVaultItembyPath(DbConstants.KEY_FILE_PATH, decryptedFilePath);
                long id = dbHelper.insertVaultItem(vaultEntity);


                CommonUtils.showInfoMsg(this, "Saved Successfully");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            final String hms = String.format(Locale.ROOT,
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(updatedTime)
                            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                            .toHours(updatedTime)),
                    TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                            .toMinutes(updatedTime)));

            runOnUiThread(() -> {
                try {
                    if (txtRecordingTime != null) {
                        if(hms.equalsIgnoreCase("03:00")){
                            hideRecordingView();
                            stopRecording(true);
                        }
                        txtRecordingTime.setText(hms);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }
    }

    private void sendMultimediaMessageOffline(String messageId,String filePath, int fileMimeType) {

            try {
                if (checkGroupMembersKey()) {
                    String encryptedFilePath = "";
                    if (fileMimeType == AppConstants.MIME_TYPE_AUDIO) {
                        encryptedFilePath = Cryptography.encryptFileGroup(mContext, filePath, groupMemberList, AppConstants.MIME_TYPE_AUDIO);
                    } else if (fileMimeType == AppConstants.MIME_TYPE_CONTACT) {
                        encryptedFilePath = Cryptography.encryptFileGroup(mContext, filePath, groupMemberList, AppConstants.MIME_TYPE_CONTACT);
                    } else if (fileMimeType == AppConstants.MIME_TYPE_IMAGE) {
                        encryptedFilePath = Cryptography.encryptFileGroup(mContext, filePath, groupMemberList, AppConstants.MIME_TYPE_IMAGE);
                    } else if (fileMimeType == AppConstants.MIME_TYPE_NOTE) {
                        encryptedFilePath = Cryptography.encryptFileGroup(mContext, filePath, groupMemberList, AppConstants.MIME_TYPE_NOTE);
                    } else if (fileMimeType == AppConstants.MIME_TYPE_VIDEO) {
                       encryptedFilePath = Cryptography.encryptFileGroup(mContext, filePath, groupMemberList, AppConstants.MIME_TYPE_VIDEO);
                    }
                    if (encryptedFilePath.length() > 0) {
                       sendFilesToServerAndSocketOffline(messageId, encryptedFilePath, "POST", ApiEndPoints.URL_UPLOADING_MULTIMEDIA_GROUP, UUID.randomUUID().toString(), fileMimeType);
                    } else {
                        flag=false;
                        startBackGroundThreadForMediaMessages();
                    }
                } else {
                    if (NetworkUtils.isNetworkConnected(mContext)) {
                        searchPublicKeys(messageId,filePath,fileMimeType);
                    } else {
                        flag=false;
                        startBackGroundThreadForMediaMessages();

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

            }

    }
    public void sendFilesToServerAndSocketOffline(String messageId,String encryptedFilePath, String httpMethod, String serverUrl, String uploadId, int fileMimeType) {
        try {

            Ion.with(mContext)
                    .load(serverUrl)
                    .setMultipartParameter("json_data", getMultimediaJSONParameter(fileMimeType))
                    .setMultipartFile("user_files", new File(encryptedFilePath))
                    .asJsonObject()
                    .setCallback((e, result) -> {
                        if (e != null) {
                            flag=false;
                            startBackGroundThreadForMediaMessages();
                        } else {
                            try {
                                JSONObject rootObject = new JSONObject(result.toString());

                                String url = rootObject.getString("url");
                                if (fileMimeType == AppConstants.MIME_TYPE_IMAGE) {
                                    url = url + AppConstants.EXTRA_HIND + fileName;
                                }
                                sendMultimediaMessageToSocketOffline(messageId,fileMimeType, encryptedFilePath, url);
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        }

                    });


        } catch (Exception exc) {
            Log.e(TAG, "onDone: " + "fail");

        }
    }
    private void sendMultimediaMessageToSocketOffline(String messageId,int mimeType, String filePath, String fileUrl) {





        int messagePos=0;
        for(int i=0;i<messageList.size();i++){
            if(messageId.equals(messageList.get(i).getMessageId())){
                messagePos = i;
                break;
            }
        }
        if(messagePos>=0) {
            ChatMessageEntity chatMessageEntity = messageList.get(messagePos);
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
            String messageBurnTime = DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatMessageEntity.getMessageBurnTime());
            chatMessageEntity.setMessageBurnTimeStamp(messageBurnTime);
            chatMessageEntity.setMessage(fileUrl);
            if (mimeType == AppConstants.MIME_TYPE_AUDIO) {
                chatMessageEntity.setAudioPath(filePath);
            } else if (mimeType == AppConstants.MIME_TYPE_VIDEO) {
                chatMessageEntity.setVideoPath(filePath);
            } else if (mimeType == AppConstants.MIME_TYPE_NOTE) {
                chatMessageEntity.setFilePath(filePath);
            } else if (mimeType == AppConstants.MIME_TYPE_IMAGE) {
                chatMessageEntity.setImagePath(filePath);
            } else if (mimeType == AppConstants.MIME_TYPE_CONTACT) {
                chatMessageEntity.setContactPath(filePath);
            }

            if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                SocketUtils.sendGroupMessageToSocket("GroupChatWindow",mContext, chatListEntity, chatMessageEntity, groupMemberList);
                dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_SENT_STATUS);
                dbHelper.updateMessageBurnDate(chatMessageEntity.getMessageId(), messageBurnTime);
                messageList.get(messagePos).setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
                messageList.get(messagePos).setMessageBurnTimeStamp(messageBurnTime);
                mAdapter.notifyDataSetChanged();
            } else {
                dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_IN_PROGRESS_STATUS);
                messageList.get(messagePos).setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                messageList.get(messagePos).setMessageBurnTimeStamp(messageBurnTime);
                mAdapter.notifyDataSetChanged();
            }
        }
        flag=false;
        startBackGroundThreadForMediaMessages();


    }
    private void sendMultimediaMessageToSocketOfflineDirect(String messageId) {

        int messagePos=0;
        for(int i=0;i<messageList.size();i++){
            if(messageId.equals(messageList.get(i).getMessageId())){
                messagePos = i;
                break;
            }
        }
        if(messagePos>=0) {
            ChatMessageEntity chatMessageEntity = messageList.get(messagePos);
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
            String messageBurnTime = DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatMessageEntity.getMessageBurnTime());
            chatMessageEntity.setMessageBurnTimeStamp(messageBurnTime);


            if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                SocketUtils.sendGroupMessageToSocket("GroupChatWindow",mContext, chatListEntity, chatMessageEntity, groupMemberList);
                dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_SENT_STATUS);
                dbHelper.updateMessageBurnDate(chatMessageEntity.getMessageId(), messageBurnTime);
                messageList.get(messagePos).setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
                messageList.get(messagePos).setMessageBurnTimeStamp(messageBurnTime);
                mAdapter.notifyDataSetChanged();

            }
        }
        flag=false;
        startBackGroundThreadForMediaMessages();


    }

    public void startBackGroundThreadForMediaMessages() {
        if(!flag) {
            flag = true;
            isRunningUnsentMediaMessages = true;
            backGroundHandlerUnsentMediaMessages.post(new Runnable() {
                public void run() {
                    if (isRunningUnsentMediaMessages) {
                        if (!SELECT_MODE) {
                            synchronized (this) {
                                if (NetworkUtils.isNetworkConnected(mContext)) {
                                    if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                                        if (messageList.size() > 0) {
                                            boolean isbreak = false;

                                            for (int i = 0; i < messageList.size(); i++) {
                                                if (messageList.get(i).getMessageStatus() == AppConstants.MESSAGE_NOT_SENT_STATUS) {
                                                    if (messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_IMAGE && messageList.get(i).getImagePath().length() == 0) {
                                                        sendMultimediaMessageOffline(messageList.get(i).getMessageId(), messageList.get(i).getMessage(), messageList.get(i).getMessageMimeType());
                                                        isbreak = true;
                                                        break;
                                                    } else if (messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_NOTE && messageList.get(i).getFilePath().length() == 0) {
                                                        sendMultimediaMessageOffline(messageList.get(i).getMessageId(), messageList.get(i).getMessage(), messageList.get(i).getMessageMimeType());
                                                        isbreak = true;
                                                        break;
                                                    } else if (messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_AUDIO && messageList.get(i).getVideoPath().length() == 0) {
                                                        sendMultimediaMessageOffline(messageList.get(i).getMessageId(), messageList.get(i).getMessage(), messageList.get(i).getMessageMimeType());
                                                        isbreak = true;
                                                        break;
                                                    } else if (messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_CONTACT && messageList.get(i).getContactPath().length() == 0) {
                                                        sendMultimediaMessageOffline(messageList.get(i).getMessageId(), messageList.get(i).getMessage(), messageList.get(i).getMessageMimeType());
                                                        isbreak = true;
                                                        break;
                                                    }
                                                } else if (messageList.get(i).getMessageStatus() == AppConstants.MESSAGE_IN_PROGRESS_STATUS) {
                                                    sendMultimediaMessageToSocketOfflineDirect(messageList.get(i).getMessageId());
                                                    isbreak = true;
                                                    break;
                                                }
                                            }
                                            if (!isbreak)
                                                flag = false;
                                        } else {
                                            flag = false;
                                        }
                                    } else {
                                        flag = false;

                                    }
                                } else {
                                    flag = false;

                                }
                            }
                        }

                    } else {
                        flag = false;

                    }
                }
            });
        }
    }


    public void stopBackGroundThreadForMediaMessages() {
        backGroundHandlerUnsentMediaMessages.removeCallbacksAndMessages(null);
        isRunningUnsentMediaMessages = false;

    }
    public void searchPublicKeys(String messageId,String filePath,int fileMimeType) {
        AndroidNetworking.post(ApiEndPoints.URL_FETCH_GROUP_ECC_KEYS)
                .addJSONObjectBody(getRawData(chatListEntity.getUserDbId()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            new PublicKeysParser().parseJson(mActivity, response.toString(), groupMemberList);

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            flag=false;
                            startBackGroundThreadForMediaMessages();
                        }

                    }

                    @Override
                    public void onError(ANError error) {

                        CommonUtils.showInfoMsg(mContext, getString(R.string.user_s_public_key_not_found));
                    }
                });
    }



}

