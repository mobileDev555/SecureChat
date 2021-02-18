package com.realapps.chat.view.home.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.interfaces.DeleteItemsResponse;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.FileLog;
import com.realapps.chat.utils.KeyboardUtils;
import com.realapps.chat.view.dialoges.DialogUnlock;
import com.realapps.chat.view.dialoges.DiscardDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PersonalActivity extends AppCompatActivity {

    public boolean VAULT_EDIT = false;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txt_title)
    EditText txtTitle;
    @BindView(R.id.txt_body)
    EditText txtBody;
    String filepath;
    int type;
    Context mContext;
    Activity mActivity;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_personal);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mContext = this;
        mActivity = this;
        ButterKnife.bind(mActivity);
        setSupportActionBar(toolbar);

        if (getIntent().getExtras() != null) {
            type = getIntent().getIntExtra(AppConstants.Personal_note, -1);
            if (type == AppConstants.Personal_save)
                filepath = getIntent().getStringExtra(AppConstants.EXTRA_PERSONAL_NOTE_FILE_PATH);

            if (getIntent().getExtras().containsKey("name"))
                fileName = getIntent().getExtras().getString("name");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> customOnBackPressed());
        toolbar.setTitle(getString(R.string.personal_notes));

        initViews();

        if (type != AppConstants.Personal_edit)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


    }

    private void initViews() {
        CommonUtils.hideTextSuggestion(txtTitle);
        if (type == AppConstants.Personal_edit) {
            txtBody.setEnabled(true);
            txtTitle.setEnabled(true);
        } else {
            String fileName = new File(filepath).getName();
            String text = readEncodedFile(filepath);
            if (text.contains("!@#$%^")) {
                String textBody = text.substring(text.indexOf("^") + 1);
                String textName = text.substring(0, text.indexOf("!"));
                txtBody.setText(textBody);
                txtTitle.setText(textName);
            } else {
                txtBody.setText(text);
                txtTitle.setText(fileName.substring(0, fileName.length() - 4));
            }
            txtBody.setEnabled(false);
            txtTitle.setEnabled(false);

        }

        txtTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkForSymbols(s.toString(), txtTitle);
            }
        });

    }

    private void checkForSymbols(String toString, EditText txtTitle) {
        if (!TextUtils.isEmpty(toString)) {
            if (toString.contains(";") || toString.contains(":") || toString.contains("?") || toString.contains("'") || toString.contains("|") || toString.contains("<") || toString.contains(">")) {
                txtTitle.setText("");
                CommonUtils.showErrorMsg(mContext, "A title can't contain following characters \n ; : ? ' | < > ");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (type == AppConstants.Personal_edit)
            getMenuInflater().inflate(R.menu.personal_notes_create, menu);
        else
            getMenuInflater().inflate(R.menu.personal_notes_view, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                DbHelper db = new DbHelper(mContext);
                if (VAULT_EDIT) {
                    VAULT_EDIT = false;
                    new File(filepath).delete();
                    db.deleteVaultItem(filepath);
                }
                if (txtBody.getText().toString().trim().length() > 0 && txtTitle.getText().toString().trim().length() > 0) {
                    if (txtTitle.getText().toString().trim().contains("/")) {
                        CommonUtils.showErrorMsg(mContext, "Title should not contain  / ");
                    } else {
                        if (!db.checkPersonalNote(txtTitle.getText().toString().trim())) {
                            String filePath = writeToFile(txtBody.getText().toString(), txtTitle.getText().toString());
                            Intent intent = new Intent();
                            intent.putExtra(AppConstants.EXTRA_PERSONAL_NOTE_FILE_NAME, txtTitle.getText().toString());
                            intent.putExtra(AppConstants.EXTRA_PERSONAL_NOTE_FILE_PATH, filePath);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            CommonUtils.showErrorMsg(mContext, getString(R.string.title_should_be_unique));
                        }
                    }

                } else {
                    CommonUtils.showErrorMsg(mContext, getString(R.string.title_or_body_cannot_be_left_empty));
                }
                break;
            case R.id.action_edit:
                type = AppConstants.Personal_edit;
                VAULT_EDIT = true;
                txtBody.setEnabled(true);
                txtTitle.setEnabled(true);
                invalidateOptionsMenu();
                KeyboardUtils.showSoftInput(txtTitle, mContext);
                txtTitle.setSelection(txtTitle.getText().toString().trim().length());
                break;
            case R.id.action_share:
                share();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void share() {
        Intent i = new Intent(mContext, ForwardMessageActivity.class);
        i.putExtra(AppConstants.EXTRA_MESSAGE_LIST, getMessage());
        i.putExtra(AppConstants.IS_ENCRYPTED, false);
        startActivity(i);
    }


    private ArrayList<ChatMessageEntity> getMessage() {
        ArrayList<ChatMessageEntity> chatMessageEntityArrayList = new ArrayList<>();
        ChatMessageEntity message = new ChatMessageEntity();
        message.setMessageMimeType(AppConstants.MIME_TYPE_NOTE);
        message.setFilePath(filepath);
        message.setFileName(fileName);
        chatMessageEntityArrayList.add(message);
        return chatMessageEntityArrayList;
    }


    private String writeToFile(String imgStr, String title) {
        File myFile = null;
        try {
            File basePath = new File(CommonUtils.getNotesDirectory(mContext));
            myFile = new File(basePath, title + ".txt");
            if (myFile.exists()) {
                CommonUtils.showInfoMsg(mContext, getString(R.string.file_already_exist_please_change_name));
            } else {
                myFile.createNewFile();
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
                outputStreamWriter.write(title + "!@#$%^" + imgStr);
                outputStreamWriter.close();
                outputStreamWriter.write(imgStr);
                outputStreamWriter.close();
            }

        } catch (IOException e) {
            FileLog.e("Exception", "File write failed: " + e.toString());
        }

        return myFile.getAbsolutePath();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppConstants.isbackground = true;
        HomeActivity.runnable = () -> {
            if (AppConstants.isbackground) {
                Log.e("Tag", "onPause: " + "background-013");
                CommonUtils.lockDialog(mActivity);
            } else {
                Log.e("Tag", "onPause: " + "forground-013");
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

    private void customOnBackPressed() {
        if (type == AppConstants.Personal_edit) {
            new DiscardDialog(mContext, getResources().getString(R.string.app_name), getString(R.string.do_you_want_to_discard_personal_notes), new DeleteItemsResponse() {
                @Override
                public void onDelete(boolean delete) {
                    onBackPressed();
                }

                @Override
                public void onClose() {

                }
            }).show();
        } else {
            onBackPressed();
        }

    }
}
