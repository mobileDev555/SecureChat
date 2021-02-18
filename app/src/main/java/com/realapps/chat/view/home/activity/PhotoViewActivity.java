package com.realapps.chat.view.home.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.github.chrisbanes.photoview.PhotoView;
import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.model.VaultEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DateTimeUtils;
import com.realapps.chat.utils.DbConstants;
import com.realapps.chat.view.dialoges.DialogUnlock;
import com.realapps.chat.view.home.VaultFileSaveUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class PhotoViewActivity extends AppCompatActivity {

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }

        }
    };
    private PhotoView mContentView;
    String imagePath;
    boolean isFromVault;
    private Toolbar toolbar;
    ChatListEntity chatListEntity;
    String MessageId;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_photo_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);
        if (getIntent().getExtras() != null) {
            imagePath = getIntent().getExtras().getString(AppConstants.EXTRA_IMAGE_PATH);
            mContentView.setImageURI(Uri.parse(imagePath));
            isFromVault = getIntent().getExtras().getBoolean(AppConstants.EXTRA_FROM_VAULT);
            chatListEntity = (ChatListEntity) getIntent().getSerializableExtra(AppConstants.EXTRA_CHAT_LIST_ITEM);
            MessageId = getIntent().getExtras().getString(AppConstants.EXTRA_MESSAGE_ID);
            if (getIntent().getExtras().containsKey("file_name")) {
                fileName = getIntent().getExtras().getString("file_name");
            }
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //set Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (!isFromVault)
            getMenuInflater().inflate(R.menu.save_menu, menu);
        else
            getMenuInflater().inflate(R.menu.share_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_to_vault:
                try {
                    DbHelper dbHelper = new DbHelper(this);
                    VaultFileSaveUtils fileSaveUtils = new VaultFileSaveUtils(dbHelper, AppConstants.ITEM_TYPE_PICTURE);
                    fileName = fileSaveUtils.getFileName(fileName);
                    VaultEntity vaultEntity = new VaultEntity();
                    vaultEntity.setMimeType(AppConstants.ITEM_TYPE_PICTURE);
                    vaultEntity.setName(fileName);
                    vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));
                    vaultEntity.setImage(copy(new File(imagePath), new File(CommonUtils.getImageDirectory(this) + fileName + ".jpg")));
                    vaultEntity.setEccId(chatListEntity.getEccId());
                    vaultEntity.setDateTimeStamp(DateTimeUtils.getCurrentDateTime());
                    dbHelper.deleteVaultItembyPath(DbConstants.KEY_FILE_PATH, imagePath);
                    dbHelper.insertVaultItem(vaultEntity);
                    CommonUtils.showInfoMsg(this, "Saved Successfully");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.action_share:

                share();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void share() {
        Intent i = new Intent(this, ForwardMessageActivity.class);
        i.putExtra(AppConstants.EXTRA_MESSAGE_LIST, getMessage());
        i.putExtra(AppConstants.IS_ENCRYPTED, false);
        i.putExtra("name", fileName);
        startActivity(i);

    }


    private ArrayList<ChatMessageEntity> getMessage() {
        ArrayList<ChatMessageEntity> chatMessageEntityArrayList = new ArrayList<>();
        ChatMessageEntity message = new ChatMessageEntity();
        message.setMessageMimeType(AppConstants.MIME_TYPE_IMAGE);
        message.setImagePath(imagePath);
        message.setFileName(fileName);
        chatMessageEntityArrayList.add(message);
        return chatMessageEntityArrayList;
    }

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

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppConstants.isbackground = true;
        HomeActivity.runnable = new Runnable() {
            @Override
            public void run() {
                if (AppConstants.isbackground) {
                    Log.e("Tag", "onPause: " + "background-016");
                    CommonUtils.lockDialog(PhotoViewActivity.this);
                } else {
                    Log.e("Tag", "onPause: " + "forground-016");
                }


            }
        };
        HomeActivity.lockHandler.postDelayed(HomeActivity.runnable, User_settings.getLockTime(PhotoViewActivity.this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        HomeActivity.lockHandler.removeCallbacks(HomeActivity.runnable);
        if (AppConstants.lockscreen) {
            CommonUtils.checkDialog(PhotoViewActivity.this);
        }
        AppConstants.isbackground = false;
        if (AppConstants.lockscreen) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    DialogUnlock.onShowKeyboard.showKeyboard();
                }
            }, 500);

        }
    }
}
