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
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.FileLog;
import com.realapps.chat.utils.FileUtils;
import com.realapps.chat.utils.SocketUtils;
import com.realapps.chat.view.dialoges.DialogUnlock;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ezvcard.Ezvcard;
import ezvcard.VCard;

public class ContactDetailsActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txt_screenname)
    TextView txtScreenname;
    @BindView(R.id.txt_name)
    TextView txtName;
    @BindView(R.id.txt_ecc_id)
    TextView txtEccId;
    @BindView(R.id.txt_company)
    TextView txtCompany;
    DbHelper dbHelper;
    ContactEntity contactEntity;
    String decryptedFilePath = "";
    String eccID = "";
    String name = "";
    int userDbId;
    Activity mActivity;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_contact_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mActivity = this;
        mContext = this;
        ButterKnife.bind(mActivity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setTitle(getString(R.string.contact_details));
        dbHelper = new DbHelper(mContext);
        if (getIntent().getExtras() != null) {
            decryptedFilePath = getIntent().getExtras().getString(AppConstants.EXTRA_CONTACT_ENTITY);
        }
        initView();

    }

    private void initView() {
        try {
            File contactFile = new File(decryptedFilePath);
            if (contactFile.exists()) {
                String txt = FileUtils.readKeyString(decryptedFilePath);
                FileLog.i("contact ", "initViews: " + txt);

                VCard vcard = Ezvcard.parse(txt).first();
                eccID = vcard.getFormattedName().getValue();
                name = vcard.getStructuredName().getFamily();
                List<String> company = vcard.getOrganization().getValues();
                if (company.size() >= 0) {
                    userDbId = Integer.parseInt(company.get(0));
                }
            }
            txtName.setText(name);
            txtCompany.setText(eccID.toUpperCase());
            contactEntity = new ContactEntity();
            contactEntity.setEccId(eccID);
            contactEntity.setName(name);
            contactEntity.setUserDbId(userDbId);
            contactEntity.setUserType(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_contact:
                if (!eccID.equalsIgnoreCase(User_settings.getECCID(mContext))) {
                    if (dbHelper.checkContact(contactEntity.getUserDbId())) {
                        CommonUtils.showErrorMsg(mContext, getString(R.string.contact_already_exists));
                    } else {
                        contactEntity.setBlockStatus(String.valueOf(SocketUtils.pending));
                        dbHelper.insertContactList(contactEntity);

                        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                                AppConstants.mWebSocketClient.send(getParameters(contactEntity.getUserDbId()).toString());
                        }
                        CommonUtils.showInfoMsg(mContext, getString(R.string.contact_added_successfully));
                    }
                } else {
                    CommonUtils.showInfoMsg(mContext, getString(R.string.cannot_add_own_ecc_id_in_contacts));
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppConstants.isbackground = true;
        HomeActivity.runnable = new Runnable() {
            @Override
            public void run() {
                if (AppConstants.isbackground) {
                    Log.e("Tag", "onPause: " + "background-001");
                    CommonUtils.lockDialog(mActivity);
                } else {
                    Log.e("Tag", "onPause: " + "forground-001");
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

    public JSONObject getParameters(int userId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", SocketUtils.ReqFriendRequest);
            jsonObject.put("sendFrom", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("sendTo", userId);
            jsonObject.put("requeststatus", 0);
            jsonObject.put("messageId", String.valueOf(System.currentTimeMillis()));
            jsonObject.put("eccId", User_settings.getECCID(mContext));
            jsonObject.put("screenName", User_settings.getScreenName(mContext));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
