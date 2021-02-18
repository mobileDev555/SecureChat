package com.realapps.chat.view.home.activity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.VaultEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DateTimeUtils;
import com.realapps.chat.utils.DbConstants;
import com.realapps.chat.view.dialoges.DialogUnlock;
import com.realapps.chat.view.home.VaultFileSaveUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PersonalActivityView extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.txt_body)
    TextView txtBody;
    String filepath;
    String fileName;
    Context mContext;
    Activity mActivity;
    ChatListEntity chatListEntity;
    String messageId;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_personal_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mContext = this;
        mActivity = this;
        ButterKnife.bind(mActivity);
        setSupportActionBar(toolbar);

        if (getIntent().getExtras() != null) {
            filepath = getIntent().getStringExtra(AppConstants.EXTRA_PERSONAL_NOTE_FILE_PATH);
            chatListEntity = (ChatListEntity) getIntent().getSerializableExtra(AppConstants.EXTRA_CHAT_LIST_ITEM);
            if (getIntent().getExtras().containsKey(AppConstants.EXTRA_MESSAGE_ID)) {
                messageId = getIntent().getStringExtra(AppConstants.EXTRA_MESSAGE_ID);
            }
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setTitle(getString(R.string.personal_notes));
        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_to_vault:
                try {
                    DbHelper db = new DbHelper(mContext);
                    VaultFileSaveUtils fileSaveUtils = new VaultFileSaveUtils(db, AppConstants.ITEM_TYPE_NOTES);
                    fileName = fileSaveUtils.getFileName(fileName);
                    DbHelper dbHelper = new DbHelper(mContext);
                    VaultEntity vaultEntity = new VaultEntity();
                    vaultEntity.setMimeType(AppConstants.ITEM_TYPE_NOTES);
                    vaultEntity.setName(fileName);
                    vaultEntity.setNotes(copy(new File(filepath), new File(CommonUtils.getNotesDirectory(mContext) + fileName + ".txt")));
                    vaultEntity.setEccId(chatListEntity.getEccId());
                    vaultEntity.setDateTimeStamp(DateTimeUtils.getCurrentDateTime());
                    vaultEntity.setMessageID(String.valueOf(System.currentTimeMillis()));
                    dbHelper.deleteVaultItembyPath(DbConstants.KEY_FILE_PATH, filepath);
                    dbHelper.insertVaultItem(vaultEntity);
                    CommonUtils.showInfoMsg(mContext, getString(R.string.saved_successfully));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {

        String text = readEncodedFile(filepath);
        if (text.contains("!@#$%^")) {
            String textBody = text.substring(text.indexOf("^") + 1);
            String textName = text.substring(0, text.indexOf("!"));
            txtBody.setText(textBody);
            txtTitle.setText(textName);
            fileName = textName;
        } else {
            txtBody.setText(text);
            txtTitle.setText(DateTimeUtils.getCurrentTimeMilliseconds());
            fileName = "Note " + System.currentTimeMillis();
        }
        txtBody.setEnabled(false);
        txtTitle.setEnabled(false);


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

    @Override
    protected void onPause() {
        super.onPause();
        AppConstants.isbackground = true;
        HomeActivity.runnable = new Runnable() {
            @Override
            public void run() {
                if (AppConstants.isbackground) {
                    Log.e("Tag", "onPause: " + "background-014");
                    CommonUtils.lockDialog(mActivity);
                } else {
                    Log.e("Tag", "onPause: " + "forground-014");
                }


            }
        };
        HomeActivity.lockHandler.postDelayed(HomeActivity.runnable, User_settings.getLockTime(mContext));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
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
