package com.realapps.chat.view.home.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
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
import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.SendMessageOfflineService;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.parser.PublicKeysParser;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.model.VaultEntity;
import com.realapps.chat.services.CopyExistingFileService;
import com.realapps.chat.services.MessageClass;
import com.realapps.chat.services.SchedulerEventReceiver;
import com.realapps.chat.ui.api.GlobalClass;
import com.realapps.chat.ui.api.ISipService;
import com.realapps.chat.ui.api.SipConfigManager;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.helper.PrefManager;
import com.realapps.chat.ui.service.SipService;
import com.realapps.chat.ui.service.SwitchOffReceiver;
import com.realapps.chat.ui.utils.CustomDistribution;
import com.realapps.chat.ui.utils.PreferencesProviderWrapper;
import com.realapps.chat.ui.utils.PreferencesWrapper;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DbConstants;
import com.realapps.chat.utils.FileLog;
import com.realapps.chat.utils.MessagesUtils;
import com.realapps.chat.utils.NotificationUtils;
import com.realapps.chat.utils.SocketUtils;
import com.realapps.chat.view.dialoges.DialogUnlock;
import com.realapps.chat.view.home.adapters.CallAdapter;
import com.realapps.chat.view.home.adapters.ChatAdapter;
import com.realapps.chat.view.home.adapters.ContactAdapter;
import com.realapps.chat.view.home.fragment.FragmentCall;
import com.realapps.chat.view.home.fragment.FragmentChats;
import com.realapps.chat.view.home.fragment.FragmentContacts;
import com.realapps.chat.view.home.fragment.FragmentGroupChat;
import com.realapps.chat.view.home.fragment.FragmentSettings;
import com.realapps.chat.view.home.fragment.FragmentVault;
import com.realapps.chat.view.lock.Lock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    //CALLING
    private PreferencesProviderWrapper prefProviderWrapper;
    public static final int PREMISSION_CONTACT_REQUEST_CODE = 1;

    private static final String TAG = "HomeActivity";

    public static boolean CHAT_DELETE_MODE = false;
    public static boolean GROUP_CHAT_DELETE_MODE = false;
    public static boolean CALL_DELETE_MODE = false;
    public static boolean CONTACT_DELETE_MODE = false;
    public static Handler lockHandler = new Handler();
    public static Runnable runnable;
    public static boolean unLocked = true;
    static NavigationView navigationView;
    private static boolean isRunning = true;
    private final int TWO_SECONDS = 2000;
    Handler backGroundHandler = new Handler();
    Context mContext;
    Activity mActivity;
    ArrayList<String> photoPaths = new ArrayList<>();
    DbHelper db;
    @BindView(R.id.img_lock)
    ImageView imgLock;
    View blackBackground;
    private Boolean exit = false;
    private ImageView fab;
    private ProgressDialog progressDialog;
    private boolean isAlreadyRunning = false;
    private boolean isBind = false;

    public static void deActivateDeleteMode() {
        HomeActivity.CHAT_DELETE_MODE = false;
        HomeActivity.CALL_DELETE_MODE = false;
        ChatAdapter.DELETE_MODE = false;
        CallAdapter.DELETE_MODE = false;
        CONTACT_DELETE_MODE = false;
        ContactAdapter.DELETE_MODE = false;
    }

    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

            NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

            if (SwitchOffReceiver.switchOff != null)
                SwitchOffReceiver.switchOff.onSwitchOffPhone();

            stopSipService();
            startSipService();
            // do application-specific task(s) based on the current network state, such
            // as enabling queuing of HTTP requests when currentNetworkInfo is connected etc.

        }
    };

    public void startSipService() {
        System.out.println("Start Sip Service");
        Thread t = new Thread("StartSip") {
            public void run() {
                Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
                // Optional, but here we bundle so just ensure we are using csipsimple package
                serviceIntent.setPackage(HomeActivity.this.getPackageName());
                serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(HomeActivity.this, HomeActivity.class));
                startService(serviceIntent);
                postStartSipService();
            };
        };
        t.start();
    }

    private void postStartSipService() {
        System.out.println("Post Start Sip Service Home");
        // If we have never set fast settings
        if (CustomDistribution.showFirstSettingScreen()) {
            if (!prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false)) {
                Intent prefsIntent = new Intent(SipManager.ACTION_UI_PREFS_FAST);
                prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(prefsIntent);
                return;
            }
        } else {
            boolean doFirstParams = !prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false);
            prefProviderWrapper.setPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, true);
            if (doFirstParams) {
                System.out.println("Post Start Sip Service elseif");
                prefProviderWrapper.resetAllDefaultValues();
            }
        }

    }

    //In Case : navigation open drawer
    public static void setMenuCounter(@IdRes int itemId, int count) {
        LinearLayout view = (LinearLayout) navigationView.getMenu().findItem(itemId).getActionView();
        TextView textMenuCounter = view.findViewById(R.id.txt_counter);
        textMenuCounter.setText(count > 0 ? String.valueOf(count) : null);

        if (count > 0) {
            textMenuCounter.setVisibility(View.VISIBLE);
            if (count > 99) {
                textMenuCounter.setTextSize(8);
                textMenuCounter.setText(Html.fromHtml("99<sup>+</sup>"));
            } else {
                textMenuCounter.setTextSize(12);
                textMenuCounter.setText(String.valueOf(count));
            }
        } else {
            textMenuCounter.setVisibility(View.GONE);
        }

    }

    //In Case : Bottom navigation
    public void setMenuBadgeCounter(int count_chat, int count_contact) {

        if (count_chat > 0) {
            textMenuCounter1.setText(count_chat > 0 ? String.valueOf(count_chat) : "");

            badgeView1.setVisibility(View.VISIBLE);

            if (count_chat > 99) {
                textMenuCounter1.setTextSize(8);
                textMenuCounter1.setText(Html.fromHtml("99<sup>+</sup>"));
            } else {
                textMenuCounter1.setTextSize(12);
                textMenuCounter1.setText(String.valueOf(count_chat));
            }
        } else {
            badgeView1.setVisibility(View.GONE);
        }

        if (count_contact > 0) {
            textMenuCounter2.setText(count_contact > 0 ? String.valueOf(count_contact) : "");

            badgeView2.setVisibility(View.VISIBLE);

            if (count_contact > 99) {
                textMenuCounter2.setTextSize(8);
                textMenuCounter2.setText(Html.fromHtml("99<sup>+</sup>"));
            } else {
                textMenuCounter2.setTextSize(12);
                textMenuCounter2.setText(String.valueOf(count_contact));
            }
        } else {
            badgeView2.setVisibility(View.GONE);
        }
    }

    BottomNavigationView navigation;
    BottomNavigationMenuView menuView1, menuView2;
    View badgeView1, badgeView2;
    BottomNavigationItemView view_chat;
    BottomNavigationItemView view_contact;
    TextView textMenuCounter1, textMenuCounter2;

    @SuppressLint("SourceLockedOrientationActivity")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_home);
        LinearLayout rootView = (LinearLayout) findViewById(R.id.drawer_layout);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();

                if (heightDiff > 400) {
                    Log.e("MyActivity", "keyboard opened");
                    navigation.setVisibility(View.GONE);
                } else {
                    Log.e("MyActivity", "keyboard closed");
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 5s = 5000ms
                            navigation.setVisibility(View.VISIBLE);
                        }
                    }, 80);

                }
            }
        });

        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        serviceIntent.setPackage(this.getPackageName());
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        isBind = true;

        ButterKnife.bind(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mActivity = this;
        mContext = this;
        db = new DbHelper(mContext);
        AppConstants.isAppActive = true;
        prefProviderWrapper = new PreferencesProviderWrapper(this);
        Bundle bundle = getIntent().getExtras();

        clearNotification();

        if (bundle != null) {

            Log.e(TAG, "EXTRA_FROM_NOTIFICATION: " + getIntent().getBooleanExtra(AppConstants.EXTRA_FROM_NOTIFICATION, false));
            Log.e(TAG, "getLastActivity: " + User_settings.getLastActivity(this).contains(LockScreenActivity.class.getSimpleName()));

            if (getIntent().getBooleanExtra(AppConstants.EXTRA_FROM_NOTIFICATION, false)
                    || User_settings.getLastActivity(this).contains(LockScreenActivity.class.getSimpleName())) {
                AppConstants.lockscreen = false;
                // CommonUtils.lockDialog(mActivity);
                Lock.getInstance(this).lockApplication();
                NotificationUtils.showBadge(mContext, 0);
            }
        } else {
            if (User_settings.getLastActivity(this).contains(LockScreenActivity.class.getSimpleName())) {
                AppConstants.lockscreen = false;
                // CommonUtils.lockDialog(mActivity);
                Lock.getInstance(this).lockApplication();
                NotificationUtils.showBadge(mContext, 0);
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

        /*LinearLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                mActivity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();*/

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(getApplicationContext(), SchedulerEventReceiver.class); // explicit
        PendingIntent intentExecuted = PendingIntent.getBroadcast(getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, 3);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), intentExecuted);

        registerReceivers();

        IntentFilter makesipFilter = new IntentFilter(SipManager.ACTION_SIP_bROADCAST);
        registerReceiver(msipReceiver, makesipFilter);


        //First Screen
        navigationView.getMenu().getItem(0).setChecked(true);
        updateScreen(new FragmentChats(), getString(R.string.fragment_chat), false);

        deleteUnreadMessages();


        /*drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                setMenuCounter(R.id.nav_chat, db.getTotalUnreadMessages());
                setMenuCounter(R.id.nav_contact, db.getTotalFirendrequest());
                setMenuCounter(R.id.nav_group_chat, db.getTotalUnreadGMessages());
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });*/


        navigation = (BottomNavigationView) findViewById(R.id.navigation);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //=====component declare for showing bottom chat/contact badge
        menuView1 = (BottomNavigationMenuView) navigation.getChildAt(0);
        view_chat = (BottomNavigationItemView) menuView1.getChildAt(0);
        badgeView1 = LayoutInflater.from(HomeActivity.this).inflate(R.layout.menu_badge, menuView1, false);
        textMenuCounter1 = badgeView1.findViewById(R.id.txt_menu_badge);
        view_chat.addView(badgeView1);

        menuView2 = (BottomNavigationMenuView) navigation.getChildAt(0);
        view_contact = (BottomNavigationItemView) menuView2.getChildAt(1);
        badgeView2 = LayoutInflater.from(HomeActivity.this).inflate(R.layout.menu_badge_contact, menuView2, false);
        textMenuCounter2 = badgeView2.findViewById(R.id.txt_menu_badge_contact);
        view_contact.addView(badgeView2);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int id = item.getItemId();
            item.setChecked(true);
            switch (id) {
                case R.id.nav_chat:
                    if (!(getCurrentFragment() instanceof FragmentChats)) {
                        deActivateDeleteMode();
                        fab.setVisibility(View.VISIBLE);
                        imgLock.setVisibility(View.VISIBLE);
                        //fab.setImageResource(R.drawable.ic_menu_chat);
                        updateScreen(new FragmentChats(), getString(R.string.fragment_chat), false);
                    }

                    break;
           /* case R.id.nav_group_chat:
                if (!(getCurrentFragment() instanceof FragmentGroupChat)) {
                    deActivateDeleteMode();
                    fab.setVisibility(View.VISIBLE);
                    fab.setImageResource(R.drawable.ic_menu_chat);
                    updateScreen(new FragmentGroupChat(), getString(R.string.fragment_group_chat), false);
                }

                break;*/


                case R.id.nav_contact:
                    if (!(getCurrentFragment() instanceof FragmentContacts)) {
                        fab.setVisibility(View.GONE);
                        imgLock.setVisibility(View.GONE);
                        deActivateDeleteMode();
                        updateScreen(new FragmentContacts(), getString(R.string.fragment_contact), false);
                    }

                    break;
                case R.id.nav_vault:
                    if (!(getCurrentFragment() instanceof FragmentVault)) {
                        fab.setVisibility(View.GONE);

                        //   fab.setImageResource(R.drawable.ic_menu_add);
                        imgLock.setVisibility(View.GONE);
                        deActivateDeleteMode();
                        updateScreen(new FragmentVault(), getString(R.string.fragment_vault), false);

                    }

                    break;
                case R.id.nav_setting:
                    if (!(getCurrentFragment() instanceof FragmentSettings)) {
                        fab.setVisibility(View.GONE);
                        imgLock.setVisibility(View.GONE);
                        deActivateDeleteMode();
                        updateScreen(new FragmentSettings(), getString(R.string.fragment_setting), false);
                    }
                    break;

            /*case R.id.lock:
                Lock.getInstance(this).lockApplication();
                break;
            case R.id.ic_clear_cache:

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(getString(R.string.clear_cache));
                builder.setMessage("Are you sure to clear cache.");
                builder.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> clearCache());
                builder.setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                });
                builder.show();

                break;*/

            }
            return false;
        }
    };

    private void BackGroundService() {
        Intent intent = new Intent(this,SendMessageOfflineService.class);
        startService(intent);
    }





    private void clearNotification() {
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    private void deleteUnreadMessages() {
        List<Integer> chatId = db.getChatListChatId();
        ArrayList<ChatMessageEntity> msgList = db.getUnreadMessageList();
        for (int i = 0; i < msgList.size(); i++) {
            if (!chatId.contains(msgList.get(i).getChatId()))
                db.deleteUnreadMessage(msgList.get(i).getChatId());
        }
    }


    @Override
    public void onBackPressed() {
        /*DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else*/
        if (CHAT_DELETE_MODE) {
            FragmentChats.deactivateDeleteModeLister.onDeActive();
        } else if (GROUP_CHAT_DELETE_MODE) {
            FragmentGroupChat.deactivateDeleteModeLister.onDeActive();
        } else if (CALL_DELETE_MODE) {
            FragmentCall.deactivateDeleteModeLister.onDeActive();
        } else if (CONTACT_DELETE_MODE) {
            FragmentContacts.deactivateDeleteModeLister.onDeActive();
        } else {
            if (exit) {
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                finish();
                System.exit(0);
            } else {
                Toast.makeText(mContext, getString(R.string.press_back_again_to_close_real_apps_chat), Toast.LENGTH_LONG).show();
                exit = true;
                new Handler().postDelayed(() -> exit = false, 3 * 1000);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        navigation.getMenu().findItem(id).setChecked(true);
        switch (id) {
            case R.id.nav_chat:
                if (!(getCurrentFragment() instanceof FragmentChats)) {
                    deActivateDeleteMode();
                    fab.setVisibility(View.VISIBLE);
                    imgLock.setVisibility(View.VISIBLE);
                    fab.setImageResource(R.drawable.ic_menu_chat);
                    updateScreen(new FragmentChats(), getString(R.string.fragment_chat), false);
                }

                break;
           /* case R.id.nav_group_chat:
                if (!(getCurrentFragment() instanceof FragmentGroupChat)) {
                    deActivateDeleteMode();
                    fab.setVisibility(View.VISIBLE);
                    fab.setImageResource(R.drawable.ic_menu_chat);
                    updateScreen(new FragmentGroupChat(), getString(R.string.fragment_group_chat), false);
                }

                break;*/


            case R.id.nav_contact:
                if (!(getCurrentFragment() instanceof FragmentContacts)) {
                    fab.setVisibility(View.GONE);
                    imgLock.setVisibility(View.GONE);
                    deActivateDeleteMode();
                    updateScreen(new FragmentContacts(), getString(R.string.fragment_contact), false);
                }

                break;
            case R.id.nav_vault:
                if (!(getCurrentFragment() instanceof FragmentVault)) {
                    fab.setVisibility(View.GONE);
                    fab.setImageResource(R.drawable.ic_menu_add);
                    imgLock.setVisibility(View.GONE);
                    deActivateDeleteMode();
                    updateScreen(new FragmentVault(), getString(R.string.fragment_vault), false);
                }

                break;
            case R.id.nav_setting:
                if (!(getCurrentFragment() instanceof FragmentSettings)) {
                    fab.setVisibility(View.GONE);
                    imgLock.setVisibility(View.GONE);
                    deActivateDeleteMode();
                    updateScreen(new FragmentSettings(), getString(R.string.fragment_setting), false);
                }
                break;

            /*case R.id.lock:
                Lock.getInstance(this).lockApplication();
                break;
            case R.id.ic_clear_cache:

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(getString(R.string.clear_cache));
                builder.setMessage("Are you sure to clear cache.");
                builder.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> clearCache());
                builder.setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                });
                builder.show();

                break;*/

        }

        /*DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);*/
        return true;
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

    public void updateScreen(Fragment fragment, String title, boolean animStatus) {

        FragmentManager manager = getSupportFragmentManager();

        FragmentTransaction ft = manager.beginTransaction();
        String backStateName = fragment.getClass().getName();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        setToolbarTitle(title);
        if (title.equalsIgnoreCase(getString(R.string.fragment_chat))) {
            navigationView.getMenu().getItem(0).setChecked(true);
        }
        if (animStatus)
            ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        ft.replace(R.id.container, fragment);
        ft.addToBackStack(backStateName);
        ft.commit();
    }

    public void fabButtonGone() {
        if (fab.getVisibility() == View.VISIBLE)
            fab.setVisibility(View.GONE);
    }

    public void fabButtonVisible() {
        if (fab.getVisibility() == View.GONE)
            fab.setVisibility(View.VISIBLE);
    }

    private void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    Fragment getCurrentFragment() {
        FragmentManager manager = getSupportFragmentManager();
        return manager.findFragmentById(R.id.container);
    }

    public void fabButton(Fragment fragment, View view) {
        view.setEnabled(false);
        if (fragment instanceof FragmentChats) {
            if (db.getContactList().size() > 0) {
                startActivity(new Intent(mContext, SelectContactActivity.class));
            } else {
                CommonUtils.showInfoMsg(mContext, getString(R.string.add_contacts_first));
            }
        } else if (fragment instanceof FragmentGroupChat) {
            if (db.getContactList().size() > 0) {
                startActivity(new Intent(mContext, SelectContactActivity.class));
            } else {
                CommonUtils.showInfoMsg(mContext, getString(R.string.add_contacts_first));
            }
        }

        new Handler().postDelayed(() -> {
            if (view != null)
                view.setEnabled(true);
        }, 1000);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if (User_settings.getUserActiveStatus(mContext)) {
                    if (User_settings.getInventryStatus(mContext)) {
                        if (User_settings.getSubscriptionStatus(mContext)) {
                            fabButton(getCurrentFragment(), view);
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

    public void startBackGroundThread(final Context mContext) {
        FileLog.e("SOCKET ", "START");
        Log.e("====>SOCKET ", "START");
        isRunning = true;
        backGroundHandler.postDelayed(new Runnable() {
            public void run() {
                if (isRunning) {
                    if (AppConstants.messageClass != null) AppConstants.messageClass = null;
                    AppConstants.messageClass = new MessageClass(mContext);
                    if (!isAlreadyRunning)
                        deleteChatListInBackground();

                    //===== showing badge in bottom chat/contact item
                    Log.e("=============", ""+db.getTotalUnreadMessages()+"/"+db.getTotalFirendrequest());
                    setMenuBadgeCounter(db.getTotalUnreadMessages(), db.getTotalFirendrequest());

                    backGroundHandler.postDelayed(this, TWO_SECONDS);

                }

            }
        }, 3);
    }


    private void deleteListHaveZeroMessage() {
        if (mContext != null) {
            DbHelper dbHelper = new DbHelper(mContext);
            final ArrayList<ChatListEntity> chatList = dbHelper.getChatList();

            if (chatList.size() > 0 && !HomeActivity.CHAT_DELETE_MODE) {
                for (ChatListEntity entity : chatList) {
                    if (AppConstants.openedChatID != entity.getId() && MessagesUtils.NewMessageUserDbId != entity.getUserDbId()) {
                        if (dbHelper.getMessageCount(entity.getId(), entity.getChatType()) > 0) {


                        } else {
                            if (entity.getChatType() != AppConstants.GROUP_CHAT_TYPE) {
                                dbHelper.deleteChatList(DbConstants.KEY_ID, entity.getId());

                                if (FragmentChats.refreshChatListListener != null) {
                                    FragmentChats.refreshChatListListener.onRefresh();


                                }

                            }

                        }
                    }

                }
                isAlreadyRunning = false;
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    public void deleteChatListInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                isAlreadyRunning = true;
                deleteListHaveZeroMessage();
                return null;
            }
        }.execute();
    }

    public void stopBackgroundThread() {
        FileLog.e("SOCKET ", "STOP");
        if (AppConstants.mWebSocketClient != null)
            AppConstants.mWebSocketClient.close();
        backGroundHandler.removeCallbacksAndMessages(null);
        isRunning = false;
    }

    public void checkStorageAndCameraPermission() {
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


    public void checkCameraPermission() {

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
                                    .labelConfirm(R.string.save)
                                    .start(AppConstants.CAMERA_RQ);                    // Starts the camera activity, the result will be sent back to the current Activity
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

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
                .enableCameraSupport(true)
                .pickPhoto((Activity) mContext);
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

                        if (FragmentVault.addVaultItemResponse != null)
                            FragmentVault.addVaultItemResponse.onImageAddResponse(photoPaths.get(0));
                    } else {
                        CommonUtils.showErrorMsg(mContext, getString(R.string.no_pictures_selected));
                    }

                }
                break;
            case AppConstants.REQUEST_CODE_PERSONAL_NOTE:

                if (resultCode == Activity.RESULT_OK && data != null) {

                    if (FragmentVault.addVaultItemResponse != null)
                        FragmentVault.addVaultItemResponse.onAddPersonalNote(data.getStringExtra(AppConstants.EXTRA_PERSONAL_NOTE_FILE_NAME), data.getStringExtra(AppConstants.EXTRA_PERSONAL_NOTE_FILE_PATH));
                }
                break;

            //========== Ringtone Processing =======
            case AppConstants.REQUEST_CODE_RINGTONE:
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                AppConstants.onpermission = false;
                if (uri != null) {
                    RingtoneManager.setActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE, uri);
                    User_settings.setRingtoneSelector(mContext, uri.toString());
                    Log.e("===========", uri.toString());

                    Ringtone r = RingtoneManager.getRingtone(mContext, uri);
                    if (FragmentSettings.responseSound != null)
                        FragmentSettings.responseSound.onChangeSound(r.getTitle(mContext));
                }
                break;

            //========== Notification Sound Processing =======
            case AppConstants.REQUEST_CODE_NOTIFY_SOUND:
                Uri uri_s = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                AppConstants.onpermission = false;
                if (uri_s != null) {
                    RingtoneManager.setActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION, uri_s);
                    User_settings.setNotifySoundSelector(mContext, uri_s.toString());
                    Log.e("===========notify-sound", uri_s.toString());

                    Ringtone r = RingtoneManager.getRingtone(mContext, uri_s);
                    if (FragmentSettings.responseSound != null)
                        FragmentSettings.responseSound.onChangeNotifySound(r.getTitle(mContext));
                }
                break;
            case AppConstants.CAMERA_RQ:
                if (resultCode == RESULT_OK && data.getExtras() != null) {
                    String tempFilePathImg = data.getDataString().substring(7);
                    if (FragmentVault.addVaultItemResponse != null)
                        FragmentVault.addVaultItemResponse.onImageAddResponse(tempFilePathImg);
                } else if (data != null) {
                    Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
                    e.printStackTrace();
                }
                break;

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

//        stopSipService();
//        try {
//            if(isBind) {
//                unbindService(connection);
//            }
//        } catch (IllegalArgumentException e) {
//            Log.e("=========home pause", e.getMessage());
//        }

        if (AppConstants.onpermission) {
            AppConstants.onpermission = false;
            AppConstants.isbackground = false;
        } else {
            AppConstants.isbackground = true;
        }
        runnable = () -> {
            if (!AppConstants.onpermission) {
                if (AppConstants.isbackground) {
                    Log.e("Tag", "onPause: " + "background-HomeActivity");
                    CommonUtils.lockDialog(mActivity);
                    AppConstants.isAppActive = false;
                } else {
                    Log.e("Tag", "onPause: " + "forground-HomeActivity");
                }
            }

        };
        Log.e("=========user id", ""+User_settings.getUserId(mContext));
        lockHandler.postDelayed(runnable, User_settings.getLockTime(mContext));
    }


    private void deleteVisibleFile() {
        ArrayList<VaultEntity> images = db.getTinyImages();
        if (images.size() > 0)
            CopyExistingFileService.startActionCopy(getApplicationContext(), images);
        File tinyDir = new File("/storage/emulated/0/Android/data/com.realapps.chat/tiny/");
        if (tinyDir.exists()) {
            new Handler().postDelayed(() -> CopyExistingFileService.deleteTinyDirectory(getApplicationContext()), 5000);
        }

    }

    private void registerReceivers() {
        registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public BroadcastReceiver msipReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            GlobalClass gc = GlobalClass.getInstance();
            String accountStatus = gc.getAccountStatus();
            int statusCode = gc.getAccountStatusCode();
            boolean isOnline = gc.isOnline();


            if (isOnline) {
                if (accountStatus != null && accountStatus.length() != 0) {
                    if (accountStatus.equals("SipConnecting")) {
                        stopSipService();
                        startSipService();
                    } else if (accountStatus.equals("SipDisconnecting")) {
                        stopSipService();
                        startSipService();
                    }
                }
            } else {
                if (accountStatus != null && accountStatus.length() != 0) {
                    if (accountStatus.equals("SipConnecting")) {

                        stopSipService();
                        startSipService();

                    } else if (accountStatus.equals("SipDisconnecting")) {
                        stopSipService();
                        startSipService();
                    }
                }

            }

        }
    };

    private void stopSipService() {
        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        // Optional, but here we bundle so just ensure we are using csipsimple package
        serviceIntent.setPackage(HomeActivity.this.getPackageName());
        serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(HomeActivity.this, HomeActivity.class));
        stopService(serviceIntent);
    }

    public ISipService service;
    public ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ISipService.Stub.asInterface(arg1);
            Log.e("Inside Connection", "//"+service);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("=====home destroy", "ok");
        if(mConnReceiver != null) {
            unregisterReceiver(mConnReceiver);
        }
        if(msipReceiver != null) {
            unregisterReceiver(msipReceiver);
        }
        if(isBind) {
            unbindService(connection);
        }

        if (CommonUtils.dialogUnlock != null && CommonUtils.dialogUnlock.isShowing()) {
            CommonUtils.dialogUnlock.dismiss();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onResume() {
        super.onResume();
        fab.setEnabled(true);

        User_settings.setLastActivity(this, HomeActivity.class.getSimpleName());
        clearNotification();
        AppConstants.isbackground = false;
        if (User_settings.isUserLogin(mContext)) {
            startBackGroundThread(mContext);
        }
        CommonUtils.checkStatus(mContext);
        lockHandler.removeCallbacks(runnable);
        if (AppConstants.lockscreen) {
            CommonUtils.checkDialog(mActivity);
        } else if (AppConstants.onpermission) {
            AppConstants.onpermission = false;
            CommonUtils.checkDialog(mActivity);
        }
        Log.e("Tag", "onResume: " + "forground-HomeActivity");
        Log.e("Lockscreen", String.valueOf(AppConstants.lockscreen));
        if (AppConstants.lockscreen) {
            new Handler().postDelayed(() -> DialogUnlock.onShowKeyboard.showKeyboard(), 500);
        }
        if( User_settings.getVERSIONCODE(getApplicationContext())!=BuildConfig.VERSION_CODE) {
            String device_model = getDeviceName();
            if (device_model == null)
                device_model = "Unknown";
            UpdateAppInfo(device_model);
        }

        if(!CommonUtils.isMyServiceRunning(mContext,SendMessageOfflineService.class)){
            BackGroundService();
        }

        PrefManager prefManager = new PrefManager(this);
        if(prefManager.getCallFromChat()){
            if(prefManager.getTypeFrag().equals("contacts")) {
                updateScreen(new FragmentContacts(), getString(R.string.fragment_contact), false);
            } else if(prefManager.getTypeFrag().equals("chat")) {
                updateScreen(new FragmentChats(), getString(R.string.fragment_chat), false);
            }
        }
    }

    @OnClick(R.id.img_lock)
    public void onViewClicked() {
        Lock.getInstance(this).lockApplication();
    }


    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }
    private boolean isMyServiceRunning(Class serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void requestPermission() {
        AppConstants.onpermission = true;
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.MODIFY_AUDIO_SETTINGS) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, SipManager.PERMISSION_CONFIGURE_SIP)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, SipManager.PERMISSION_USE_SIP)) {

            if (isMyServiceRunning(SipService.class)) {
                stopSipService();
            }

            new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Light_NoTitleBar))

                    .setTitle("Permission Access")
                    .setMessage("You must have to allow all permission from setting of application?")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // continue with delete
                        try {
                            //Open the specific App Info page:
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                            startActivity(intent);

                        } catch (ActivityNotFoundException e) {
                            //e.printStackTrace();

                            //Open the generic Apps page:
                            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                            startActivity(intent);

                        }
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {
                        // do nothing
                        if (checkPermission()) {
                            startSipService();
                        } else {
                            requestPermission();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .show().setCanceledOnTouchOutside(false);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, SipManager.PERMISSION_CONFIGURE_SIP, SipManager.PERMISSION_USE_SIP}, PREMISSION_CONTACT_REQUEST_CODE);
        }
    }

    private void UpdateAppInfo(String device_model) {
        AndroidNetworking.post(ApiEndPoints.update_app_info)
                .addBodyParameter("user_id",User_settings.getUserId(mContext))
                .addBodyParameter("app_type", "2").
                addBodyParameter("device_model",device_model)
                .addBodyParameter("build_version", BuildConfig.VERSION_NAME)
                .addBodyParameter("build_version_code", String.valueOf(BuildConfig.VERSION_CODE))
                .addBodyParameter("os_version",String.valueOf(Build.VERSION.SDK_INT) )
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.e("Device Infomation","Successfully Sent");
                        try {
                            JSONObject rootObject = new JSONObject(response.toString());
                            if (rootObject.getInt("status")==1) {
                                User_settings.setVERSIONCODE(mContext,BuildConfig.VERSION_CODE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.e("Device Infomation","Not Successfully Sent");

                    }
                });
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        boolean permission_st = false;
        switch (requestCode) {
            case PREMISSION_CONTACT_REQUEST_CODE:
                if (grantResults.length > 0) {
                    // Contacts permission
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access Contacts.", Toast.LENGTH_LONG).show();

                    } else {
                        //Toast.makeText(getApplicationContext(),"Until you grant the permission, we canot display the contacts.",Toast.LENGTH_LONG).show();
                        //Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access Contacts.", Toast.LENGTH_LONG).show();
                    }
                    // Phone permission
                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        permission_st = true;
                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access Phone.", Toast.LENGTH_LONG).show();
                    } else {
                        permission_st = false;
                        //Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access Phone.", Toast.LENGTH_LONG).show();
                    }
                    // Microphone permission
                    if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                        permission_st = true;
                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access Microphone.", Toast.LENGTH_LONG).show();
                    } else {
                        permission_st = false;
                        //Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access Microphone.", Toast.LENGTH_LONG).show();
                    }
                    // Storage permission
                    if (grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                        permission_st = true;
                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access Storage.", Toast.LENGTH_LONG).show();
                    } else {
                        permission_st = false;
                        //Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access Storage.", Toast.LENGTH_LONG).show();
                    }
                    //Full permission
                    if (grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                        permission_st = true;
                        //Toast.makeText(getApplicationContext(), "Permission Granted, Now you have full access to ASTPP Dialer Plus.", Toast.LENGTH_LONG).show();
                    } else {
                        permission_st = false;
                        //Toast.makeText(getApplicationContext(), "Permission Denied, You cannot have full access to ASTPPDialer Plus.", Toast.LENGTH_LONG).show();
                    }

                    if (!permission_st) {
                        new AlertDialog.Builder(this)
                                .setTitle("Permission Access")
                                .setMessage("You must have to allow all permission from setting of application?")
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                    // continue with delete
                                    try {
                                        //Open the specific App Info page:
                                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                        startActivity(intent);

                                    } catch (ActivityNotFoundException e) {
                                        //e.printStackTrace();

                                        //Open the generic Apps page:
                                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                        startActivity(intent);

                                    }
                                })
                                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                                    // do nothing
                                    if (checkPermission()) {
                                        startSipService();
                                    } else {
                                        requestPermission();
                                    }
                                })
                                .setIconAttribute(android.R.attr.alertDialogIcon)
                                .show().setCanceledOnTouchOutside(false);

                    } else {
                        System.out.println("Not Call.");
                    }

                }
                break;
        }
    }

    private boolean checkPermission() {
        int read_contact = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS);
        int write_contact = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_CONTACTS);
        int read_phone = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
        int record_audio = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        int modify_audio = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.MODIFY_AUDIO_SETTINGS);
        int read_storage = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int write_storage = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int config_sip = ContextCompat.checkSelfPermission(getApplicationContext(), SipManager.PERMISSION_CONFIGURE_SIP);
        int use_sip = ContextCompat.checkSelfPermission(getApplicationContext(), SipManager.PERMISSION_USE_SIP);


        if (read_contact == PackageManager.PERMISSION_GRANTED && write_contact == PackageManager.PERMISSION_GRANTED && read_phone == PackageManager.PERMISSION_GRANTED && record_audio == PackageManager.PERMISSION_GRANTED && modify_audio == PackageManager.PERMISSION_GRANTED && read_storage == PackageManager.PERMISSION_GRANTED && write_storage == PackageManager.PERMISSION_GRANTED && config_sip == PackageManager.PERMISSION_GRANTED && use_sip == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
}
