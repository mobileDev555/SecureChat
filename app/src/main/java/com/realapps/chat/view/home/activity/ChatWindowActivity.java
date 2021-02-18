package com.realapps.chat.view.home.activity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.afollestad.materialcamera.MaterialCamera;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.koushikdutta.ion.Ion;
import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.SendMessageOfflineService;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.interfaces.AttachmentDialogResponse;
import com.realapps.chat.interfaces.ChatWindowFunctionListener;
import com.realapps.chat.interfaces.DestructTimeDialogResponse;
import com.realapps.chat.interfaces.FriendRequestResponse;
import com.realapps.chat.interfaces.NetworkReconnectedListener;
import com.realapps.chat.interfaces.SendMessageAckToSocket;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.model.PublicKeyEntity;
import com.realapps.chat.model.VaultEntity;
import com.realapps.chat.ui.api.GlobalClass;
import com.realapps.chat.ui.api.ISipService;
import com.realapps.chat.ui.api.SipCallSession;
import com.realapps.chat.ui.api.SipConfigManager;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.api.SipProfile;
import com.realapps.chat.ui.helper.PrefManager;
import com.realapps.chat.ui.service.SipService;
import com.realapps.chat.ui.service.SwitchOffReceiver;
import com.realapps.chat.ui.ui.incall.InCallActivity;
import com.realapps.chat.ui.utils.CallHandlerPlugin;
import com.realapps.chat.ui.utils.CustomDistribution;
import com.realapps.chat.ui.utils.PreferencesProviderWrapper;
import com.realapps.chat.ui.utils.PreferencesWrapper;
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
import com.realapps.chat.view.NotificationActivity;
import com.realapps.chat.view.dialoges.DeleteMessageDialog;
import com.realapps.chat.view.dialoges.DialogAttachment;
import com.realapps.chat.view.dialoges.DialogCamera;
import com.realapps.chat.view.dialoges.DialogDestructTimer;
import com.realapps.chat.view.dialoges.DialogMediaPlayer;
import com.realapps.chat.view.dialoges.DialogUnlock;
import com.realapps.chat.view.home.VaultFileSaveUtils;
import com.realapps.chat.view.home.adapters.ChatAdapter;
import com.realapps.chat.view.home.adapters.ChatWindowAdapter;
import com.realapps.chat.view.home.fragment.FragmentChats;
import com.realapps.chat.view.home.fragment.FragmentContacts;
import com.realapps.chat.view.home.fragment.FragmentGroupChat;
import com.realapps.chat.view.lock.Lock;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;
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
import android.app.Dialog;
import com.realapps.chat.interfaces.AttachmentDialogResponse;

public class ChatWindowActivity extends AppCompatActivity implements ChatWindowAdapter.onItemClickListner, ChatWindowFunctionListener, TextView.OnEditorActionListener, View.OnTouchListener, FriendRequestResponse, SendMessageAckToSocket, NetworkReconnectedListener {
    Context mContext;
    AttachmentDialogResponse dialogResponseListener;
    private static final String TAG = ChatWindowActivity.class.getSimpleName();
    private static final int REQUEST_CONTACT_SELECT = 101;
    private PreferencesProviderWrapper prefProviderWrapper;
    private static final int REQUEST_VAULT_ITEM_SELECT = 102;
    public static ChatWindowFunctionListener chatWindowFunctionListener;
    public static NetworkReconnectedListener networkReconnectedListener;
    public static SendMessageAckToSocket sendMessageAckToSocket;
    public static FriendRequestResponse friendRequestResponse;
    private static boolean SELECT_MODE = false;
    private static boolean isRunning = true;
    private static boolean isRunningUnsentMessages = true;
    private static boolean isRunningUnsentMediaMessages = true;
    private final int TWO_SECONDS = 2000;
    float x1, y1, x2, y2, dx, dy;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    ArrayList<ContactEntity> contacts;
    ArrayList<VaultEntity> vaultListEntities;
    String audioPath = "";

    Activity mActivity;
    @BindView(R.id.recycler_messages)
    RecyclerView mRecycler;
    @BindView(R.id.txt_message)
    EditText txtMessage;
    ArrayList<String> photoPaths = new ArrayList<>();
    Handler backGroundHandler = new Handler();
    Handler backGroundHandlerUnsentMessages = new Handler();
    Handler backGroundHandlerUnsentMediaMessages = new Handler();
    @BindView(R.id.btn_send_message)
    ImageButton btnSendMessage;
    @BindView(R.id.fab_go_call)
    FloatingActionButton btnGO;
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
    private boolean isMyFriend = false;
    private long startTime = 0L;
    private int mCount = 0;
    private boolean isMultipleContact = false;
    private boolean isMultipleVaultItem = false;
    private MediaRecorder recorder = null;
    private Timer timer;
    private Toolbar toolbar;
    private ChatWindowAdapter mAdapter;
    private ChatMessageEntity chatMessageEntityVault;
    private ArrayList<ChatMessageEntity> messageList;
    private ChatListEntity chatListEntity;
    private DbHelper dbHelper;
    private boolean isPaused = false;
    private ChatMessageEntity revisedChatMessageEntity;
    private String originaltext = "";
    private Bitmap.Config mConfig = Bitmap.Config.ARGB_8888;
    private String fileName = "";
    boolean flag = false;
    Handler handler;
    boolean isforward = false;
    PrefManager prefManager;
    boolean isCallPlaced = true;
    GlobalClass gc;

    private String type_fragment = "";
    private Handler customHandler = new Handler();

    private ContactEntity contactEntity;

    public static String copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
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
    public boolean isMyFriend() {
        return dbHelper.checkUserHasFriend(chatListEntity.getEccId());
    }
    public void setMyFriend(boolean myFriend) {
        isMyFriend = myFriend;
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //gc = GlobalClass.getInstance();
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_chat_window);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mContext = this;
        mActivity = this;
        prefManager = new PrefManager(mContext);
        ButterKnife.bind(mActivity);
        dbHelper = new DbHelper(mContext);
        if (getIntent().getExtras() != null) {
            type_fragment = getIntent().getStringExtra(AppConstants.TYPE_FRAGMENT);

            chatListEntity = (ChatListEntity) getIntent().getSerializableExtra(AppConstants.EXTRA_CHAT_LIST_ITEM);

            contactEntity = (ContactEntity) getIntent().getSerializableExtra("contactEntity");
            AppConstants.chatId = chatListEntity.getId();
            AppConstants.chatType = chatListEntity.getChatType();
        }
        chatWindowFunctionListener = this;
        friendRequestResponse = this;
        networkReconnectedListener = this;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //set Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setTitle(CommonUtils.getContactName(mContext, chatListEntity.getEccId()));
        if (dbHelper.checkUserHasFriend(chatListEntity.getEccId())) {
            setMyFriend(true);
        } else {
            setMyFriend(false);
        }
        initViews();
        checkMessageStatus();
        startBackGroundThread();
        startBackGroundThreadForUnsentMessages();
        if(!CommonUtils.isMyServiceRunning(mContext,SendMessageOfflineService.class)){
            BackGroundService();
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEND && User_settings.getEnterKeySend(mContext) == AppConstants.YES && txtMessage.getText().toString().trim().length() > 0) {
            if (isMyFriend()) {
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
            } else {
                CommonUtils.showInfoMsg(mContext, chatListEntity.getEccId() + " not in your friend list, to send message you have to add this contact to in you contact list.");
            }
            return true;
        }
        return false;
    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
    private void initViews() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        lyrHide.setVisibility(View.GONE);
        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        txtMessage.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        //txtMessage.setHint(getString(R.string.your_message_will_delete_in_s, CommonUtils.getBurnTime(mContext, chatListEntity.getBurnTime(), AppConstants.TIME_TEXT_TYPE_SMALL)));
        txtMessage.setOnEditorActionListener(this);
        if (User_settings.getEnterKeySend(this) == AppConstants.YES) {
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
                    btnSendMessage.setOnTouchListener(ChatWindowActivity.this);
                }
            }
        });

        btnSendMessage.setOnTouchListener(this);
    }

    private void setAdapter() {
        if (messageList != null) {
            notifyMessageList();
        } else {
            messageList = new ArrayList<>();
            messageList = dbHelper.getMessageList(chatListEntity.getId(), chatListEntity.getChatType());
            FileLog.e("ChatWindow DbId ", String.valueOf(chatListEntity.getId()));
            mAdapter = new ChatWindowAdapter(mContext, messageList, this);
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
            synchronized (this) {
                if (messageList.get(i).getMessageStatus() == AppConstants.MESSAGE_UNREAD_STATUS
                        && messageList.get(i).getMessageStatus() != AppConstants.MESSAGE_READ_STATUS
                        && messageList.get(i).getSenderId() != Integer.valueOf(User_settings.getUserId(mContext))) {
                    notifyChatListItem = true;
                    Log.e("===========unread msg", "unread msg");
                    dbHelper.updateMessageBurnDate(messageList.get(i).getMessageId(), DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, messageList.get(i).getMessageBurnTime()));
                    dbHelper.updateMessageStatusByMessageId(messageList.get(i).getMessageId(), AppConstants.MESSAGE_READ_STATUS);
                    SocketUtils.sendREadAcknowledgementToSocket(mContext, messageList.get(i), AppConstants.MESSAGE_READ_STATUS);
                    showMessageCountBadge();

                }
                else if (messageList.get(i).getMessageStatus() == AppConstants.MESSAGE_UNREAD_STATUS
                        && messageList.get(i).getMessageStatus() != AppConstants.MESSAGE_READ_STATUS
                        && messageList.get(i).getSenderId() == Integer.valueOf(User_settings.getUserId(mContext))) {
                    notifyChatListItem = true;
                    Log.e("===========unread msg", "missed call");

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


    private void NotifyList() {
        messageList = dbHelper.getMessageList(chatListEntity.getId(), chatListEntity.getChatType());
        mAdapter.notifyDataSetChanged();
        boolean notifyChatListItem = false;
        for (int i = 0; i < messageList.size(); i++) {
            synchronized (this) {
                if (messageList.get(i).getMessageStatus() == AppConstants.MESSAGE_UNREAD_STATUS && messageList.get(i).getMessageStatus() != AppConstants.MESSAGE_READ_STATUS && messageList.get(i).getSenderId() != Integer.valueOf(User_settings.getUserId(mContext))) {
                    notifyChatListItem = true;

                    dbHelper.updateMessageBurnDate(messageList.get(i).getMessageId(), DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, messageList.get(i).getMessageBurnTime()));
                    dbHelper.updateMessageStatusByMessageId(messageList.get(i).getMessageId(), AppConstants.MESSAGE_READ_STATUS);
                    SocketUtils.sendREadAcknowledgementToSocket(mContext, messageList.get(i), AppConstants.MESSAGE_READ_STATUS);

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
                        Intent intent = new Intent(ChatWindowActivity.this, ShareFromVaultActivity.class);
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
                        if (isMyFriend()) {
                            if (User_settings.getUserActiveStatus(mContext)) {
                                if (User_settings.getInventryStatus(mContext)) {
                                    if (User_settings.getSubscriptionStatus(mContext)) {


                                        Intent intent = new Intent(mContext, ShareFromVaultActivity.class);
                                        intent.putExtra(AppConstants.EXTRA_ITEM_TYPE, AppConstants.ITEM_TYPE_PICTURE);
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
                            CommonUtils.showInfoMsg(mContext, chatListEntity.getEccId() + " not in your friend list, to send message you have to add this contact to in you contact list.");
                        }
                    }

                });

                break;

            case R.id.attachment:

                if (isMyFriend()) {
                    if (User_settings.getUserActiveStatus(mContext)) {
                        if (User_settings.getInventryStatus(mContext)) {
                            if (User_settings.getSubscriptionStatus(mContext)) {

                                if (isTextBoxExpanded())
                                    collapseMessageTextBox();

                                new DialogAttachment(mContext, new AttachmentDialogResponse() {
                                    @Override
                                    public void onImageSelect() {
                                        if (isMyFriend()) {
                                            if (User_settings.getUserActiveStatus(mContext)) {
                                                if (User_settings.getInventryStatus(mContext)) {
                                                    if (User_settings.getSubscriptionStatus(mContext)) {


                                                        Intent intent = new Intent(mContext, ShareFromVaultActivity.class);
                                                        intent.putExtra(AppConstants.EXTRA_ITEM_TYPE, AppConstants.ITEM_TYPE_PICTURE);
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
                                            CommonUtils.showInfoMsg(mContext, chatListEntity.getEccId() + " not in your friend list, to send message you have to add this contact to in you contact list.");
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
                                    public void sharemsgfromvault() {
                                        Intent intent = new Intent(ChatWindowActivity.this, ShareFromVaultActivity.class);
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
                } else {
                    CommonUtils.showInfoMsg(mContext, getString(R.string.s_not_in_your_friend_list_to_send_message_you_have_to_add_this_contact_to_in_you_contact_list, chatListEntity.getEccId()));
                }


                break;
            case R.id.btn_send_message:
                if (txtMessage.getText().toString().trim().length() > 0)
                    if (isMyFriend()) {
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
                    } else {
                        CommonUtils.showInfoMsg(mContext, getString(R.string.s_not_in_your_friend_list_to_send_message_you_have_to_add_this_contact_to_in_you_contact_list, chatListEntity.getEccId()));
                    }

                break;
        }
    }

    private void sendTextMessage(String messageText) {
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setSenderId(Integer.parseInt(User_settings.getUserId(mContext)));
        chatMessageEntity.setMessage(messageText);
        chatMessageEntity.setMessageMimeType(AppConstants.MIME_TYPE_TEXT);
        chatMessageEntity.setMessageId(String.valueOf(System.currentTimeMillis()));
        chatMessageEntity.setChatId(chatListEntity.getId());
        chatMessageEntity.setReceiverId(chatListEntity.getUserDbId());
        chatMessageEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
        chatMessageEntity.setMessageBurnTime(chatListEntity.getBurnTime());
        chatMessageEntity.setChatUserDbId(chatMessageEntity.getChatUserDbId());
        chatMessageEntity.setEddId(chatListEntity.getEccId());
        if (NetworkUtils.isNetworkConnected(mContext)) {
            if (dbHelper.checkPublicKeysOfUser(chatListEntity.getUserDbId())) {
                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
                    SocketUtils.sendNewMessageToSocket(mContext, chatListEntity, chatMessageEntity);
                    checkMessageStatus();
                } else {
                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                }
                chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
                dbHelper.insertChatMessage(chatMessageEntity);
                notifyMessageList();
               txtMessage.setText("");
            } else {
                searchPublicKeys(chatMessageEntity);
            }
        } else {
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
            chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
            dbHelper.insertChatMessage(chatMessageEntity);
            mRecycler.smoothScrollToPosition(messageList.size());

            notifyMessageList();
            txtMessage.setText("");
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
            if (dbHelper.checkPublicKeysOfUser(chatListEntity.getUserDbId())) {
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
                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
                    SocketUtils.sendRevisedMessageToSocket(mContext, chatListEntity, chatMessageEntity);
                    checkMessageStatus();
                    dbHelper.updateMessageTextByMessageId(chatMessageEntity.getMessageId(), messageText);
                    dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
                    notifyMessageList();
                    isRevised = false;

                    txtMessage.setText("");
                }
            }
        }
        dbHelper.updateChatListTimeStamp(chatListEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());


    }

    private void notifyMessageList() {
        ArrayList<ChatMessageEntity> list = dbHelper.getMessageList(chatListEntity.getId(), chatListEntity.getChatType());
        messageList = getMessageList(list, messageList);
        mAdapter = new ChatWindowAdapter(mContext, messageList, this, ChatWindowAdapter.SELECT_MODE);
        mRecycler.swapAdapter(mAdapter, false);
        // Call smooth scroll
        mRecycler.smoothScrollToPosition(mAdapter.getItemCount() - 1);
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
        chatMessageEntity.setMessage(fileUrl);
        chatMessageEntity.setEddId(chatListEntity.getEccId());
        chatMessageEntity.setFileName(fileName);
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
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
            chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
            SocketUtils.sendNewMessageToSocket(mContext, chatListEntity, chatMessageEntity);
            checkMessageStatus();
        } else {
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
            chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
        }

        dbHelper.insertChatMessage(chatMessageEntity);
        dbHelper.updateChatListTimeStamp(chatListEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());

        notifyMessageList();
        mRecycler.smoothScrollToPosition(messageList.size());

        txtMessage.setText("");

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
        chatMessageEntity.setMessage(fileUrl);
        chatMessageEntity.setEddId(chatListEntity.getEccId());
        chatMessageEntity.setFileName(fileName);
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
        chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));

        dbHelper.insertChatMessage(chatMessageEntity);
        dbHelper.updateChatListTimeStamp(chatListEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());

        notifyMessageList();
        mRecycler.smoothScrollToPosition(messageList.size());

        txtMessage.setText("");

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



    private void sendMultimediaMessage(String filePath, int fileMimeType) {

        sendMultimediaMessageToSocketOff(fileMimeType, "", filePath);
    }
    public void searchPublicKeys(String filePath, int fileMimeType) {
        AndroidNetworking.post(ApiEndPoints.URL_FETCH_ECC_KEYS)
                .addBodyParameter("email", CommonUtils.getUserEmail(chatListEntity.getEccId()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(response.toString());

                            if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                String publicKey = rootObject.getString("result_data");
                                PublicKeyEntity keyEntity = new PublicKeyEntity();
                                keyEntity.setUserDbId(chatListEntity.getUserDbId());
                                keyEntity.setEccId(chatListEntity.getEccId());
                                keyEntity.setUserType(chatListEntity.getChatType());
                                keyEntity.setEccPublicKey(publicKey);
                                keyEntity.setName(chatListEntity.getName());
                                dbHelper.insertPublicKey(keyEntity);
                                sendMultimediaMessage(filePath, fileMimeType);
                            } else {
                                CommonUtils.showInfoMsg(mContext, rootObject.getString("msg"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        CommonUtils.showInfoMsg(mContext, getString(R.string.user_s_public_key_not_found));

                    }
                });


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
        else {
            getMenuInflater().inflate(R.menu.single_chat_window_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    private boolean isMyMessage(ArrayList<ChatMessageEntity> selectedMessage) {
        return selectedMessage.get(0).getSenderId() == Integer.valueOf(User_settings.getUserId(this));
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_dec_time:
                new DialogDestructTimer(mContext, chatListEntity, new DestructTimeDialogResponse() {
                    @Override
                    public void onTimeChange(int time) {
                        dbHelper.updateChatListBurnTime(chatListEntity.getId(), time);
                        chatListEntity.setBurnTime(time);
                        txtMessage.setHint(getString(R.string.your_message_will_delete_in) + " " + CommonUtils.getBurnTime(mContext, time, AppConstants.TIME_TEXT_TYPE_SMALL));
                    }

                    @Override
                    public void onClose() {

                    }
                }).show();
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
                if (isMyFriend()) {
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
                    CommonUtils.showInfoMsg(mContext, chatListEntity.getEccId() + " not in your friend list, to send message you have to add this contact to in you contact list.");

                }
                break;

            case R.id.action_share_chat_message:
                if (isMyFriend()) {
                    if (User_settings.getUserActiveStatus(mContext)) {
                        if (User_settings.getInventryStatus(mContext)) {
                            if (User_settings.getSubscriptionStatus(mContext)) {
                                flag=false;
                                Intent intent = new Intent(mContext, ShareFromVaultActivity.class);
                                intent.putExtra(AppConstants.EXTRA_ITEM_TYPE, AppConstants.ITEM_TYPE_CHATS);
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
                    CommonUtils.showInfoMsg(mContext, chatListEntity.getEccId() + " not in your friend list, to send message you have to add this contact to in you contact list.");
                }
                break;
            case R.id.action_share_image:
                if (isMyFriend()) {
                    if (User_settings.getUserActiveStatus(mContext)) {
                        if (User_settings.getInventryStatus(mContext)) {
                            if (User_settings.getSubscriptionStatus(mContext)) {
                                flag=false;
                                Intent intent = new Intent(mContext, ShareFromVaultActivity.class);
                                intent.putExtra(AppConstants.EXTRA_ITEM_TYPE, AppConstants.ITEM_TYPE_PICTURE);
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
                    CommonUtils.showInfoMsg(mContext, chatListEntity.getEccId() + " not in your friend list, to send message you have to add this contact to in you contact list.");
                }
                break;
            case R.id.action_call:
                prefManager = new PrefManager(mContext);
                prefManager.setCallFromChat(true);
                prefManager.setEccIdToBeCalled(chatListEntity.getEccId());
                prefManager.setTypeFrag(type_fragment);

                Gson gson = new Gson();
                String json = gson.toJson(chatListEntity);
                prefManager.setChatEntry(json);
                this.finish();
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

            case R.id.action_delete:
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
                            if (SocketUtils.sendDeleteMesageToSocket(ChatWindowActivity.this, chatListEntity, selectedMessage)) {
                                for (ChatMessageEntity entity : selectedMessage) {
                                    dbHelper.updateMimeTime(entity.getMessageId(), AppConstants.MIME_TYPE_DELETE);
                                    deActiveSelectMode();
                                }
                            } else {
                                CommonUtils.showInfoMsg(ChatWindowActivity.this, getString(R.string.please_try_again));
                            }
                        }
                    }).show();
                }
                break;

            case R.id.action_save_to_vault:
                if (chatMessageEntityVault.getMessageMimeType() == AppConstants.MIME_TYPE_TEXT) {
                    VaultEntity vaultEntity = new VaultEntity();
                    vaultEntity.setMimeType(AppConstants.ITEM_TYPE_CHATS);
                    vaultEntity.setName(chatListEntity.getName());
                    vaultEntity.setEccId(chatListEntity.getEccId());
                    vaultEntity.setChatType(0);
                    vaultEntity.setDateTimeStamp(DateTimeUtils.getCurrentDateTime());
                    vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));
                    vaultEntity.setDbId(chatListEntity.getId());

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
//
//    private void callPerson() {
//        isCallPlaced = true; //??
//        String dialNum = this.contactEntity.getEccId().toUpperCase();
//        PrefManager prefManager = new PrefManager(mContext);
//        if (isCallPlaced) {
//            if (!prefManager.get_is_ongoing_call_flag()) {
//                prefManager.set_is_ongoing_call_flag(true);
//                placeCall(dialNum);
//            }
//        }
//    }
//
//    private void placeCall(String dialNum) {
//        AppConstants.isbackground = false;
//        AppConstants.lockscreen = false;
//        AppConstants.onpermission = true;
//        placeCallWithOption(dialNum);
//    }
//
//    private void placeCallWithOption(String dialNum) {
//
//        Long accountToUse;
//        //System.out.println(service);
////        System.out.println("Encrypted call ==>service " + ((HomeActivity) mActivity).service);
//        if ( ((ChatWindowActivity) mActivity).service == null) {
//            return;
//        }
//
//        String toCall = "";
//        accountToUse = SipProfile.INVALID_ID;
//        // Find account to use
//        //accountChooserButton.getSelectedAccount();
//
//        Uri toBeChanged = Uri.parse("content://com.shadowsecure.call.db/accounts");
//
//
//        //Cursor c = mActivity.getContentResolver().query(toBeChanged, null, null, null, null);
////        Cursor c = mActivity.getContentResolver().query(SipProfile.ACCOUNT_URI, null, null, null, null);
////        System.out.println("Cursor count" + c.getCount());
////        c.moveToFirst();
////        SipProfile acc = new SipProfile(c);
////        if (acc == null) {
////            return;
////        }
//
//        accountToUse = Long.parseLong("1");
//        // Find number to dial
//        String dataType = gc.checkNetworkType(mActivity);
//        System.out.println("Encrypted Check network type: " + dataType);
//        if (dataType != null && dataType.length() != 0) {
//            toCall = dataType + dialNum;
//        } else {
//            toCall = dialNum;
//        }
//        /*toCall = dialNum;*/
//        prefManager.setCallingNum("contact");
//        gc.setIsCallRunning(true);
//        /*ArrayList<String> toCallArr = new ArrayList<>();
//        toCallArr.add(dialNum);
//
//        PrefManager pref = new PrefManager(activity);
//        pref.addContact(toCallArr);*/
//
//        android.util.Log.d("acc Id" + accountToUse, "  Number " + toCall);
//        // Well we have now the fields, clear theses fields
//
//        System.out.println("Encrypted call ==>accountToUse " + accountToUse);
//        // -- MAKE THE CALL --//
//        if (accountToUse >= 0) {
//            // It is a SIP account, try to call service for that
//            try {
//                isCallPlaced = false;
//
//                service.makeCallWithOptions(toCall, accountToUse.intValue(), null);
//                //prefManager.setHistoryFetcher_AfterCall(true);
//
//            } catch (RemoteException e) {
//                android.util.Log.e(FragmentContacts.class.toString(), "Service can't be called to make the call");
//            }
//        } else if (accountToUse != SipProfile.INVALID_ID) {
//            // It's an external account, find correct external account
//            CallHandlerPlugin ch = new CallHandlerPlugin(mActivity);
//            ch.loadFrom(accountToUse, toCall, new CallHandlerPlugin.OnLoadListener() {
//                @Override
//                public void onLoad(CallHandlerPlugin ch) {
//                    placePluginCall(ch);
//                }
//            });
//        } else {
//            prefManager.set_is_ongoing_call_flag(false);
//            System.out.println("Calling Flag : CAllhistory inside else: " + prefManager.get_is_ongoing_call_flag());
//
//        }
//    }
//
//    private void placePluginCall(CallHandlerPlugin ch) {
//        try {
//            String nextExclude = ch.getNextExcludeTelNumber();
//            if (((HomeActivity) mActivity).service != null && nextExclude != null) {
//                try {
//                    ((HomeActivity) mActivity).service.ignoreNextOutgoingCallFor(nextExclude);
//                } catch (RemoteException e) {
//                    android.util.Log.e(FragmentContacts.class.toString(), "Impossible to ignore next outgoing call", e);
//                }
//            }
//            ch.getIntent().send();
//        } catch (PendingIntent.CanceledException e) {
//            android.util.Log.e(FragmentContacts.class.toString(), "Pending intent cancelled", e);
//        }
//    }

    private boolean isContainDeletedMessage() {
        boolean isIt = false;
        for (ChatMessageEntity entity : getSelectedMessage()) {
            if (entity.getMessageMimeType() == AppConstants.MIME_TYPE_DELETE) {
                isIt = true;
                break;
            }
        }
        return isIt;
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
    private void saveAllChat() {
        VaultEntity vaultEntity = new VaultEntity();
        vaultEntity.setMimeType(AppConstants.ITEM_TYPE_CHATS);
        vaultEntity.setName(chatListEntity.getName());
        vaultEntity.setEccId(chatListEntity.getEccId());
        vaultEntity.setChatType(0);
        vaultEntity.setDateTimeStamp(DateTimeUtils.getCurrentDateTime());
        vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));
        vaultEntity.setDbId(chatListEntity.getId());
        int id = (int) dbHelper.insertVaultItem(vaultEntity);
        dbHelper.updateDbID(chatListEntity.getId(), id);

        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_TEXT) {
                messageList.get(i).setChatId(id);
                dbHelper.insertVaultMessage(messageList.get(i));
            }
        }
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
                text = text +" " + messageList.get(i).getMessage();
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
    public void onItemClick(View view, ChatMessageEntity chatMessageEntity, int position) {
        /*  if (MultiClickBlocker.block())*/
        if (SELECT_MODE) {
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
                        DialogMediaPlayer dialogMediaPlayer = new DialogMediaPlayer(mContext, decryptedFilePath, (isResponseOk, response) -> {
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
                    intent.putExtra(AppConstants.EXTRA_IMAGE_PATH, chatMessageEntity.getMessage());
                    intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
                    intent.putExtra(AppConstants.EXTRA_MESSAGE_ID, chatMessageEntity.getMessageId());
                    intent.putExtra(AppConstants.EXTRA_FROM_VAULT, false);
                    intent.putExtra("file_name", new File(chatMessageEntity.getMessage()).getName());
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(mActivity, view, "photo");
                    startActivity(intent);
                } else {
                    String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getImagePath());
                    if (decryptedFilePath.length() > 0) {
                        imageItemPosition = mLayoutManager.findLastVisibleItemPosition();
                        Intent intent = new Intent(mContext, PhotoViewActivity.class);
                        intent.putExtra(AppConstants.EXTRA_IMAGE_PATH, decryptedFilePath);
                        intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
                        intent.putExtra(AppConstants.EXTRA_MESSAGE_ID, chatMessageEntity.getMessageId());
                        intent.putExtra(AppConstants.EXTRA_FROM_VAULT, false);
                        intent.putExtra("file_name", new File(chatMessageEntity.getImagePath()).getName());
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(mActivity, view, "photo");
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
    }

    @Override
    public void onRetryMessage(ChatMessageEntity chatMessageEntity, int position) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
            String messageBurnTime = DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatMessageEntity.getMessageBurnTime());
            chatMessageEntity.setMessageBurnTimeStamp(messageBurnTime);
            SocketUtils.sendNewMessageToSocket(mContext, chatListEntity, chatMessageEntity);
            dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
            dbHelper.updateMessageBurnDate(chatMessageEntity.getMessageId(), messageBurnTime);
            setAdapter();
            checkMessageStatus();
        }
    }

    private void toggleSelection(int position) {
        ChatWindowAdapter.checkLists[position] = !ChatWindowAdapter.checkLists[position];
        messageList.get(position).setSelected(!messageList.get(position).isSelected());
        mAdapter.notifyItemChanged(position);

        if (getSelectedMessageCount() == 0)
            deActiveSelectMode();
    }

    private void activeSelectMode() {
        SELECT_MODE = true;
        ChatWindowAdapter.SELECT_MODE = true;
        mAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
    }

    private void deActiveSelectMode() {
        SELECT_MODE = false;
        isRevised = false;
        ChatWindowAdapter.SELECT_MODE = false;
        ChatWindowAdapter.checkLists = new boolean[messageList.size()];
        unSelectAll();
        setAdapter();
        invalidateOptionsMenu();
        toolbar.setTitle(CommonUtils.getContactName(mContext, chatListEntity.getEccId()));
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
            toolbar.setTitle(String.valueOf(getSelectedMessageCount()));
        else
            toolbar.setTitle(CommonUtils.getContactName(mContext, chatListEntity.getEccId()));
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
                if (resultCode == Activity.RESULT_OK && data.getExtras() != null) {
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
                }
                break;
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

    @Override
    public void onBackPressed() {
        prefManager = new PrefManager(mContext);
        prefManager.setCallFromChat(false);
        prefManager.setEccIdToBeCalled("null");
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
            sendMessageAckToSocket = null;
            super.onBackPressed();
            this.finish();
        }
        stopBackgroundThread();
        stopBackgroundThreadUnsentMessages();
        stopBackGroundThreadForMediaMessages();
    }

    private boolean isTextBoxExpanded() {
        return lyrBottom.getLayoutParams().height == LinearLayout.LayoutParams.MATCH_PARENT;
    }
    public void startBackGroundThread() {
        isRunning = true;
        backGroundHandler.postDelayed(new Runnable() {
            public void run() {
                if (isRunning) {
                    if (!SELECT_MODE) {
                        synchronized (this) {
                            if (dbHelper.checkMessageAge(chatListEntity.getId()) > 0) {
                                ArrayList<String> oldMessageIdList = dbHelper.getOldMessageMessageId(chatListEntity.getId());
                                int loop = 0;
                                if (oldMessageIdList.size() > 0) {
                                    int deletedMessageLoop = 0;
                                    for (int i = 0; i < messageList.size(); i++) {
                                        FileLog.e("Message : ", messageList.get(loop).getMessage());
                                        boolean messageDeleted = false;
                                        for (int j = 0; j < oldMessageIdList.size(); j++) {
                                            if (messageList.get(loop).getMessageId().equals(oldMessageIdList.get(j))) {
                                                dbHelper.deleteMessageListEntity(DbConstants.KEY_ID, messageList.get(loop).getId());
                                                messageList.clear();
                                                messageList.addAll(dbHelper.getMessageList(chatListEntity.getId(), chatListEntity.getChatType()));
                                                deletedMessageLoop++;
                                                FileLog.e("Message ID : ", String.valueOf(deletedMessageLoop));
                                                messageDeleted = true;
                                                ChatAdapter.checkLists = new boolean[messageList.size()];
                                                FileLog.e("ListSize : ", String.valueOf(messageList.size()));
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        }
                                        if (!messageDeleted) {
                                            loop++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    backGroundHandler.postDelayed(this, TWO_SECONDS);
                }
            }
        }, 3);
    }

    public void stopBackgroundThread() {
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
                                                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
                                                    String messageBurnTime = DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatMessageEntity.getMessageBurnTime());
                                                    chatMessageEntity.setMessageBurnTimeStamp(messageBurnTime);
                                                    SocketUtils.sendNewMessageToSocket(mContext, chatListEntity, chatMessageEntity);
                                                    dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
                                                    dbHelper.updateMessageBurnDate(chatMessageEntity.getMessageId(), messageBurnTime);
                                                    //setAdapter();
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

    private void playNewMessageSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playCallingSound() {
        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            Ringtone ringtone = RingtoneManager.getRingtone(mContext, uri);
            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMessageCountBadge() {
        int messageCount = dbHelper.getTotalUnreadMessages();
        NotificationUtils.showBadge(mContext, messageCount);
    }


    @Override
    public void onNewMessage(ChatMessageEntity chatMessageEntity) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_TEXT) {
                        if (chatMessageEntity.getChatId() == chatListEntity.getId()) {
                            synchronized (this) {
                                chatMessageEntity.setCurrentMessageStatus(AppConstants.MESSAGE_READ_STATUS);
                                String messageBurnTimeStamp = DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatMessageEntity.getMessageBurnTime());
                                chatMessageEntity.setMessageBurnTimeStamp(messageBurnTimeStamp);
                                dbHelper.updateMessage(chatMessageEntity.getMessageId(), messageBurnTimeStamp, AppConstants.MESSAGE_READ_STATUS);
                                if (isLastVisible()) {
                                    notifyMessageList();
                                    mRecycler.smoothScrollToPosition(messageList.size());
                                } else {
                                    notifyMessageList();
                                }
                                if (!AppConstants.lockscreen) {
                                    dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_READ_STATUS);
                                    SocketUtils.sendREadAcknowledgementToSocket(mContext, chatMessageEntity, AppConstants.MESSAGE_READ_STATUS);
                                } else {
                                    SocketUtils.sendREadAcknowledgementToSocket(mContext, chatMessageEntity, AppConstants.MESSAGE_STATUS_DELIVERED);
                                    if (chatListEntity.getSnoozeStatus() == AppConstants.NOTIFICATION_SNOOZE_NO) {
                                        NotificationUtils.showNotification(mContext, chatListEntity, AppConstants.NOTIFICATION_ID, mContext.getResources().getString(R.string.title_message_notification), dbHelper.getTotalUnreadMessages());
                                        playNewMessageSound();
                                    }
                                }
                            }
                        } else {
                            SocketUtils.sendREadAcknowledgementToSocket(mContext, chatMessageEntity, AppConstants.MESSAGE_STATUS_DELIVERED);
                            if (chatListEntity.getSnoozeStatus() == AppConstants.NOTIFICATION_SNOOZE_NO) {
                                NotificationUtils.showNotification(mContext, chatListEntity, AppConstants.NOTIFICATION_ID, mContext.getResources().getString(R.string.title_message_notification), dbHelper.getTotalUnreadMessages());
                            }
                        }
                    } else {
                        if (chatMessageEntity.getChatId() == chatListEntity.getId()) {
                            if (AppConstants.lockscreen) {
                                SocketUtils.sendREadAcknowledgementToSocket(mContext, chatMessageEntity, AppConstants.MESSAGE_STATUS_DELIVERED);
                                if (chatListEntity.getSnoozeStatus() == AppConstants.NOTIFICATION_SNOOZE_NO) {
                                    NotificationUtils.showNotification(mContext, chatListEntity, AppConstants.NOTIFICATION_ID, mContext.getResources().getString(R.string.title_message_notification), dbHelper.getTotalUnreadMessages());
                                }
                            } else {
                                chatMessageEntity.setCurrentMessageStatus(AppConstants.MESSAGE_READ_STATUS);
                                String messageBurnTimeStamp = DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatMessageEntity.getMessageBurnTime());
                                chatMessageEntity.setMessageBurnTimeStamp(messageBurnTimeStamp);
                                dbHelper.updateMessage(chatMessageEntity.getMessageId(), messageBurnTimeStamp, AppConstants.MESSAGE_READ_STATUS);
                                if (isLastVisible()) {
                                    notifyMessageList();
                                    mRecycler.smoothScrollToPosition(messageList.size());
                                } else {
                                    notifyMessageList();
                                }
                                dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_READ_STATUS);
                                SocketUtils.sendREadAcknowledgementToSocket(ChatWindowActivity.this, chatMessageEntity, AppConstants.MESSAGE_READ_STATUS);
                                playNewMessageSound();
                            }
                        } else {
                            if (chatListEntity.getSnoozeStatus() == AppConstants.NOTIFICATION_SNOOZE_NO) {
                                NotificationUtils.showNotification(mContext, chatListEntity, AppConstants.NOTIFICATION_ID, mContext.getResources().getString(R.string.title_message_notification), dbHelper.getTotalUnreadMessages());
                            }
                            SocketUtils.sendREadAcknowledgementToSocket(mContext, chatMessageEntity, AppConstants.MESSAGE_STATUS_DELIVERED);
                        }

                    }
                    showMessageCountBadge();
                }
            }
        });

    }

    public void sendMessageAcknowledgement() {
        ArrayList<ChatMessageEntity> messageListNew;
        messageListNew = dbHelper.getUnreadMessageList(chatListEntity.getId(), chatListEntity.getChatType(), Integer.parseInt(User_settings.getUserId(mContext)));
        for (int i = 0; i < messageListNew.size(); i++) {
            synchronized (this) {
                if (messageListNew.get(i).getMessageStatus() == AppConstants.MESSAGE_UNREAD_STATUS && messageListNew.get(i).getMessageStatus() != AppConstants.MESSAGE_READ_STATUS && messageListNew.get(i).getSenderId() != Integer.valueOf(User_settings.getUserId(mContext))) {
                    dbHelper.updateMessageBurnDate(messageListNew.get(i).getMessageId(), DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, messageListNew.get(i).getMessageBurnTime()));
                    dbHelper.updateMessageStatusByMessageId(messageListNew.get(i).getMessageId(), AppConstants.MESSAGE_READ_STATUS);
                    SocketUtils.sendREadAcknowledgementToSocket(mContext, messageListNew.get(i), AppConstants.MESSAGE_READ_STATUS);

                }
            }
        }
        setAdapter();  //This function is works when we unlock the screen and comes to chat window.
    }

    public void checkMessageStatus() {
        ArrayList<ChatMessageEntity> messageListNew;
        messageListNew = dbHelper.getInProgressSentMessageList(chatListEntity.getId(), Integer.parseInt(User_settings.getUserId(mContext)));
        for (int i = 0; i < messageListNew.size(); i++) {
            synchronized (this) {
                dbHelper.updateMessageStatusByMessageId(messageListNew.get(i).getMessageId(), AppConstants.MESSAGE_NOT_SENT_STATUS);
            }
        }
        if (mAdapter != null)
            notifyMessageList();
        else
            setAdapter();
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
        AndroidNetworking.post(ApiEndPoints.URL_FETCH_ECC_KEYS)
                .addBodyParameter("email", CommonUtils.getUserEmail(chatListEntity.getEccId()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                         try {
                            JSONObject rootObject = new JSONObject(response.toString());
                            if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                String publicKey = rootObject.getString("result_data");
                                PublicKeyEntity keyEntity = new PublicKeyEntity();
                                keyEntity.setUserDbId(chatListEntity.getUserDbId());
                                keyEntity.setEccId(chatListEntity.getEccId());
                                keyEntity.setUserType(chatListEntity.getChatType());
                                keyEntity.setEccPublicKey(publicKey);
                                keyEntity.setName(chatListEntity.getName());
                                dbHelper.insertPublicKey(keyEntity);
                                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
                                    chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
                                    SocketUtils.sendNewMessageToSocket(mContext, chatListEntity, chatMessageEntity);
                                    checkMessageStatus();
                                } else {
                                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                                    chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
                                }
                                dbHelper.insertChatMessage(chatMessageEntity);
                                notifyMessageList();
                                txtMessage.setText("");

                            } else {
                                CommonUtils.showInfoMsg(mContext, rootObject.getString("msg"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                        public void onError(ANError error) {
                        CommonUtils.showInfoMsg(mContext, "User's Public Key Not Found !");
                        chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                        messageList.add(chatMessageEntity);
                        dbHelper.insertChatMessage(chatMessageEntity);
                        mAdapter.notifyItemInsert(messageList.size());
                        mRecycler.smoothScrollToPosition(messageList.size());
                        txtMessage.setText("");
                    }
                });


    }


    private void done(String encryptedFilePath, int fileMimeType) {
        AndroidNetworking.upload(ApiEndPoints.URL_UPLOADING_MULTIMEDIA_SINGLE)
                .addMultipartFile("user_files", new File(encryptedFilePath))
                .addMultipartParameter("json_data", getMultimediaJSONParameter(fileMimeType))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        FileLog.e("Encrypted FilePath : ", encryptedFilePath);
                    }

                    @Override
                    public void onError(ANError error) {
                        FileLog.e("Encrypted FilePath : ", error.getErrorDetail());
                    }
                });
    }

    public String getMultimediaJSONParameter(int mimeType) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("sender_id", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObj.put("receiver_id", chatListEntity.getUserDbId());
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

    public void sendFilesToServerAndSocket(String encryptedFilePath, String httpMethod, String serverUrl, String uploadId, int fileMimeType) {
        if (NetworkUtils.isNetworkConnected(mContext)) {
            try {
                Ion.with(mContext)
                        .load(serverUrl)
                        .setMultipartParameter("json_data", getMultimediaJSONParameter(fileMimeType))
                        .setMultipartFile("user_files", new File(encryptedFilePath))
                        .asJsonObject()
                        .setCallback((e, result) -> {
                            if (e != null) {
                                e.printStackTrace();
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
                                    CommonUtils.showErrorMsg(mContext, getString(R.string.please_try_again));
                                }
                            }


                        });


            } catch (Exception exc) {
                Log.e(TAG, "onDone: " + "fail");
                exc.printStackTrace();
                CommonUtils.showErrorMsg(mContext, getString(R.string.please_try_again));
            }
        } else {
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        sendMessageAckToSocket = null;
        chatWindowFunctionListener = null;
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
                    Log.e("Tag", "onPause: " + "background-ChatWindowActivity");
                    if (dialogMediaPlayer != null && dialogMediaPlayer.isShowing()) {
                        dialogMediaPlayer.dismiss();
                    }
                    CommonUtils.lockDialog(mActivity);
                } else {
                    Log.e("Tag", "onPause: " + "forground-011");
                }
            }
        };
        HomeActivity.lockHandler.postDelayed(HomeActivity.runnable, User_settings.getLockTime(mContext));
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

    @Override
    protected void onStart() {
        super.onStart();


    }
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            int a = SipConfigManager.getPreferenceIntegerValue(getApplicationContext(), SipConfigManager.CALLING, 0);
            if(a == 1) btnGO.setVisibility(View.VISIBLE);
            else btnGO.setVisibility(View.INVISIBLE);
            customHandler.postDelayed(this, 500);
        }

    };
    @Override
    protected void onResume() {
        super.onResume();
        //=========missed call counter :  format to 0
        gc = GlobalClass.getInstance();
        gc.setMissedCallCount(getApplicationContext(), "0");

        customHandler.postDelayed(updateTimerThread, 500);


        btnGO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatWindowActivity.this, InCallActivity.class));
            }
        });

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        CommonUtils.checkStatus(mContext);
        AppConstants.openedChatID = chatListEntity.getId();
        sendMessageAckToSocket = this;
        if (chatWindowFunctionListener == null)
            chatWindowFunctionListener = this;
        HomeActivity.lockHandler.removeCallbacks(HomeActivity.runnable);
        if (AppConstants.lockscreen) {
            CommonUtils.checkDialog(mActivity);
        }
        AppConstants.isbackground = false;
        if (!AppConstants.lockscreen && !NotificationActivity.fromNotification)
            setAdapter();
        if (isPaused) {
            if(isforward){
                isforward=false;
                flag=false;
            }
            startBackGroundThread();
            startBackGroundThreadForUnsentMessages();
            if(isforward){
                isforward=false;
                flag=false;
            }
        }

        if (AppConstants.lockscreen) {
            new Handler().postDelayed(() -> DialogUnlock.onShowKeyboard.showKeyboard(), 500);
        }
        if (imageItemPosition != -1) {
            mRecycler.scrollToPosition(imageItemPosition);
            imageItemPosition = -1;
        }
        NotificationActivity.fromNotification = false;
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
    //  ******************************************COMPRESS*************************

    public void setMultipleVaultItem(boolean multipleVaultItem) {
        isMultipleVaultItem = multipleVaultItem;
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
            Tiny.FileCompressOptions compressOptions = new Tiny.FileCompressOptions();
            compressOptions.config = mConfig;
            compressOptions.outfile = file;
            Tiny.getInstance().source(outfile).asFile().withOptions(compressOptions).compress(new FileCallback() {
                @Override
                public void callback(boolean isSuccess, String outfile, Throwable t) {
                    if (!isSuccess) {
                        return;
                    }
                    File file = new File(outfile);
                    String logMessage = "compress file size:" + Formatter.formatFileSize(mContext, file.length())
                            + "\noutfile: " + outfile;
                    sendMultimediaMessage(outfile, AppConstants.MIME_TYPE_IMAGE);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (isMyFriend()) {
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
        } else {
            CommonUtils.showInfoMsg(mContext, getString(R.string.s_not_in_your_friend_list_to_send_message_you_have_to_add_this_contact_to_in_you_contact_list, chatListEntity.getEccId()));
        }
        view.onTouchEvent(motionEvent);
        return true;
    }

    private void askRecordingPermissions() {
        AppConstants.isbackground = false;
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
        if (recorder != null) {

            if (timer != null) {
                timer.cancel();
            }

            if (txtRecordingTime.getText().toString().equals("00:00")) {
                try {
                    recorder = null;
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
    public void onUnFriend(String eccId) {
        if (eccId.equalsIgnoreCase(chatListEntity.getEccId())) {
            this.runOnUiThread(() -> {
                setMyFriend(false);
            });
        }
    }

    @Override
    public void onSendAck() {
        new Handler().postDelayed(this::sendMessageAcknowledgement, 3000);
    }

    private boolean isLastVisible() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) mRecycler.getLayoutManager());
        int pos = layoutManager.findLastCompletelyVisibleItemPosition() + 1;
        int numItems = mRecycler.getAdapter().getItemCount();
        return (pos >= numItems);
    }

    @Override
    public void onNetworkReconnected() {
        mActivity.runOnUiThread(() -> {
            Log.e("NetworkClass", "first");
            startBackGroundThreadForUnsentMessages();
        });
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
                Log.e(TAG, "saveImageToVault: " + fileName);
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
        }
        return text.toString();
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
            long lastsec = TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                    .toMinutes(updatedTime));

            runOnUiThread(() -> {
                try {
                    if (txtRecordingTime != null) {
                        //Todo uncomment for setting 1 min audio
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

    /******************************************************************
     *
     * file upload
     */
    private void sendMultimediaMessageOffline(String messageId,String filePath, int fileMimeType) {

            try {
                if (dbHelper.checkPublicKeysOfUser(chatListEntity.getUserDbId())) {
                    String encryptedFilePath = "";
                    if (fileMimeType == AppConstants.MIME_TYPE_AUDIO) {
                        encryptedFilePath = Cryptography.encryptFile(mContext, filePath, chatListEntity.getUserDbId(), chatListEntity.getEccId(), AppConstants.MIME_TYPE_AUDIO);
                    } else if (fileMimeType == AppConstants.MIME_TYPE_CONTACT) {
                        encryptedFilePath = Cryptography.encryptFile(mContext, filePath, chatListEntity.getUserDbId(), chatListEntity.getEccId(), AppConstants.MIME_TYPE_CONTACT);
                    } else if (fileMimeType == AppConstants.MIME_TYPE_IMAGE) {
                        encryptedFilePath = Cryptography.encryptFile(mContext, filePath, chatListEntity.getUserDbId(), chatListEntity.getEccId(), AppConstants.MIME_TYPE_IMAGE);
                    } else if (fileMimeType == AppConstants.MIME_TYPE_NOTE) {
                        encryptedFilePath = Cryptography.encryptFile(mContext, filePath, chatListEntity.getUserDbId(), chatListEntity.getEccId(), AppConstants.MIME_TYPE_NOTE);
                    } else if (fileMimeType == AppConstants.MIME_TYPE_VIDEO) {
                        encryptedFilePath = Cryptography.encryptFile(mContext, filePath, chatListEntity.getUserDbId(), chatListEntity.getEccId(), AppConstants.MIME_TYPE_VIDEO);
                    }
                    if (encryptedFilePath.length() > 0)
                        sendFilesToServerAndSocketOffline(messageId,encryptedFilePath, "POST", ApiEndPoints.URL_UPLOADING_MULTIMEDIA_SINGLE, UUID.randomUUID().toString(), fileMimeType);

                } else {
                    if (NetworkUtils.isNetworkConnected(mContext)) {
                        searchPublicKeys(messageId,filePath, fileMimeType);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    public void sendFilesToServerAndSocketOffline(String messageId,String encryptedFilePath, String httpMethod, String serverUrl, String uploadId, int fileMimeType) {
        if (NetworkUtils.isNetworkConnected(mContext)) {
            try {
                Ion.with(mContext)
                        .load(serverUrl)
                        .setMultipartParameter("json_data", getMultimediaJSONParameter(fileMimeType))
                        .setMultipartFile("user_files", new File(encryptedFilePath))
                        .asJsonObject()
                        .setCallback((e, result) -> {
                            if (e != null) {
                                e.printStackTrace();
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
                exc.printStackTrace();
            }
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
                SocketUtils.sendNewMessageToSocket(mContext, chatListEntity, chatMessageEntity);
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
                SocketUtils.sendNewMessageToSocket(mContext, chatListEntity, chatMessageEntity);
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

    public synchronized void startBackGroundThreadForMediaMessages() {
            if(!flag) {
                flag=true;
                isRunningUnsentMediaMessages = true;
                backGroundHandlerUnsentMediaMessages.post(new Runnable() {
                    public void run() {
                        if (isRunningUnsentMediaMessages) {
                            if (!SELECT_MODE) {
                                synchronized (this) {
                                    if (NetworkUtils.isNetworkConnected(mContext)) {
                                        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                                            if (messageList.size() > 0) {
                                                boolean isbreak=false;
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
                                                if(!isbreak)
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
    public void searchPublicKeys(String messageId,String filePath, int fileMimeType) {
        AndroidNetworking.post(ApiEndPoints.URL_FETCH_ECC_KEYS)
                .addBodyParameter("email", CommonUtils.getUserEmail(chatListEntity.getEccId()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(response.toString());

                            if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                String publicKey = rootObject.getString("result_data");
                                PublicKeyEntity keyEntity = new PublicKeyEntity();
                                keyEntity.setUserDbId(chatListEntity.getUserDbId());
                                keyEntity.setEccId(chatListEntity.getEccId());
                                keyEntity.setUserType(chatListEntity.getChatType());
                                keyEntity.setEccPublicKey(publicKey);
                                keyEntity.setName(chatListEntity.getName());
                                dbHelper.insertPublicKey(keyEntity);

                                sendMultimediaMessageOffline(messageId,filePath, fileMimeType);
                            } else {
                                CommonUtils.showInfoMsg(mContext, rootObject.getString("msg"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        CommonUtils.showInfoMsg(mContext, getString(R.string.user_s_public_key_not_found));
                    }
                });


    }

    private void BackGroundService() {
        Intent intent = new Intent(this, SendMessageOfflineService.class);
        startService(intent);
    }

}

