package com.realapps.chat.view.home.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.model.GroupMemberEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DateTimeUtils;
import com.realapps.chat.utils.FileLog;
import com.realapps.chat.utils.KeyboardUtils;
import com.realapps.chat.utils.NetworkUtils;
import com.realapps.chat.utils.SocketUtils;
import com.realapps.chat.view.custom.MyDividerItemDecoration;
import com.realapps.chat.view.dialoges.DialogUnlock;
import com.realapps.chat.view.home.adapters.CreateGroupAdapter;
import com.realapps.chat.view.home.fragment.FragmentChats;
import com.realapps.chat.view.home.fragment.FragmentGroupChat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupCreateActivity extends AppCompatActivity implements CreateGroupAdapter.onItemClickListner {

    private static final int REQUEST_CONTACT_SELECT = 101;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.edt_group_name)
    EditText edtGroupName;
    @BindView(R.id.recycler_chat)
    RecyclerView recyclerChat;
    @BindView(R.id.img_add)
    ImageView imgAdd;
    Bundle bundle;
    ArrayList<ContactEntity> selectedContacts = new ArrayList<>();
    CreateGroupAdapter mAdapter;
    DbHelper db;
    ArrayList<GroupMemberEntity> groupMemberList = new ArrayList<>();
    Context mContext;
    Activity mActivity;
    private ProgressDialog mProgressDialoge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_group);


            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        mActivity = this;
        mContext = this;
        ButterKnife.bind(mActivity);
        bundle = getIntent().getExtras();
        if (bundle != null) {
            selectedContacts = (ArrayList<ContactEntity>) bundle.getSerializable(AppConstants.EXTRA_SELECTED_CONTACT);
        }
        db = new DbHelper(mContext);
        initview();
        setAdapter();
    }

    private void setAdapter() {
        mAdapter = new CreateGroupAdapter(mContext, selectedContacts, this);
        recyclerChat.setAdapter(mAdapter);
    }

    private void initview() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setTitle(getString(R.string.create_group));
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        recyclerChat.setLayoutManager(mLayoutManager);
        recyclerChat.setItemAnimator(new DefaultItemAnimator());
        recyclerChat.addItemDecoration(new MyDividerItemDecoration(mContext, DividerItemDecoration.VERTICAL, 0));
       // CommonUtils.hideTextSuggestion(edtGroupName);

    }


    private void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    Fragment getCurrentFragment() {
        FragmentManager manager = getSupportFragmentManager();
        return manager.findFragmentById(R.id.container);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @OnClick(R.id.img_add)
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_add:
                Intent intent = new Intent(mContext, SelectContactActivity.class);
                intent.putExtra(AppConstants.EXTRA_SELECTED_CONTACT, selectedContacts);
                startActivityForResult(intent, REQUEST_CONTACT_SELECT);
                break;

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_CONTACT_SELECT:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<ContactEntity> contacts = (ArrayList<ContactEntity>) data.getSerializableExtra(AppConstants.EXTRA_SELECTED_CONTACT);
                    selectedContacts = contacts;
                    setAdapter();
                }
                break;


        }
    }


    @Override
    public void onItemClick(ContactEntity contactEntity, int position) {

    }

    @Override
    public void onItemRemove(ContactEntity contactEntity, int position) {
        selectedContacts.remove(contactEntity);
        mAdapter.notifyDataSetChanged();

    }

    @OnClick(R.id.btn_create_group)
    public void onViewClicked() {
        if (selectedContacts.size() > 0) {
            KeyboardUtils.hideSoftInput(mActivity);
            if (edtGroupName.getText().toString().equalsIgnoreCase("")) {
                CommonUtils.showInfoMsg(mContext, getString(R.string.group_name_field_is_required));
            } else {
                if (NetworkUtils.isNetworkConnected(mContext)) {
                    if (edtGroupName.getText().toString().trim().length() < 21) {
                        mProgressDialoge = CommonUtils.showLoadingDialog(mContext);
                        createGroupApi();
                    } else {
                        CommonUtils.showErrorMsg(mContext, getString(R.string.group_name_should_be_maximum_20_characters));
                    }

                } else {
                    CommonUtils.showErrorMsg(mContext, getString(R.string.no_internet_connection));
                }

            }
        } else
            CommonUtils.showInfoMsg(mContext, getString(R.string.please_select_at_least_1_contact_to_create_group));
    }

    private void createGroupApi() {
        AndroidNetworking.post(ApiEndPoints.create_group)
                .addBodyParameter("json_data", makeJsonData(edtGroupName.getText().toString().trim()))
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
                                JSONObject rootArray = rootObject.getJSONObject("result");
                                int group_id = rootArray.getInt("group_id");
                                insertGroupMember(group_id);
                                ChatListEntity chatListEntity = new ChatListEntity();
                                chatListEntity.setBurnTime(42);
                                chatListEntity.setName(edtGroupName.getText().toString().trim());
                                chatListEntity.setUserDbId(group_id);
                                chatListEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
                                chatListEntity.setChatType(AppConstants.GROUP_CHAT_TYPE);
                                chatListEntity.setEccId(DateTimeUtils.getCurrentTimeMilliseconds());
                                long id = db.insertChatList(chatListEntity);
                                chatListEntity.setId((int) id);


                                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                                    SocketUtils.sendCreateGroupToSocket(mContext, groupMemberList, edtGroupName.getText().toString().trim(), group_id);
                                }
                                if (FragmentChats.refreshChatListListener != null) {
                                    FragmentChats.refreshChatListListener.onRefresh();
                                }
                                if (FragmentGroupChat.refreshChatListListener != null) {
                                    FragmentGroupChat.refreshChatListListener.onRefresh();
                                }

                                Intent i = new Intent(mContext, GroupChatWindowActivity.class);
                                i.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
                                startActivity(i);

                                finish();

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
                        CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again));

                    }
                });
    }

    private void insertGroupMember(int group_id) {

        for (ContactEntity row : selectedContacts) {
            GroupMemberEntity entity = new GroupMemberEntity();
            entity.setChatId(group_id);
            entity.setEccId(row.getEccId());
            entity.setEccPublicKey(row.getEccPublicKey());
            entity.setMemberType(AppConstants.GroupMember);
            entity.setName(row.getName());
            entity.setUserDbId(row.getUserDbId());
            db.insertGroupMember(entity);
            groupMemberList.add(entity);
        }
        GroupMemberEntity entity = new GroupMemberEntity();
        entity.setChatId(group_id);
        entity.setEccId(User_settings.getECCID(mContext));
        entity.setEccPublicKey("");
        entity.setMemberType(AppConstants.GroupAdmin);
        entity.setName(User_settings.getScreenName(mContext));
        entity.setUserDbId(Integer.parseInt(User_settings.getUserId(mContext)));
        db.insertGroupMember(entity);
        groupMemberList.add(entity);

    }

    private String makeJsonData(String name) {
        String description = "this is for testing";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("desciption", description);
            JSONArray member = new JSONArray();
            for (int i = 0; i < selectedContacts.size(); i++) {
                ContactEntity entity = selectedContacts.get(i);
                JSONObject object = new JSONObject();
                object.put("ecc_id", entity.getEccId().toUpperCase());
                object.put("member_type", AppConstants.GroupMember);
                member.put(object);
            }
            JSONObject object = new JSONObject();
            object.put("ecc_id", User_settings.getECCID(mContext));
            object.put("member_type", AppConstants.GroupAdmin);

            member.put(object);


            jsonObject.put("member", member);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FileLog.sout(jsonObject.toString());
        return jsonObject.toString();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppConstants.isbackground = true;
        HomeActivity.runnable = new Runnable() {
            @Override
            public void run() {


                if (AppConstants.isbackground) {
                    Log.e("Tag", "onPause: " + "background-003");
                    CommonUtils.lockDialog(mActivity);

                } else {
                    Log.e("Tag", "onPause: " + "forground-003");
                }


            }
        };
        HomeActivity.lockHandler.postDelayed(HomeActivity.runnable, User_settings.getLockTime(mContext));
      /*  new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AppConstants.isbackground) {
                    Log.e("Tag", "onPause: " + "background");
                    CommonUtils.lockDialog(mActivity);
                } else {
                    Log.e("Tag", "onPause: " + "forground");
                }

            }
        }, User_settings.getLockTime(mContext));*/
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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    DialogUnlock.onShowKeyboard.showKeyboard();
                }
            }, 500);
            //DialogUnlock.onShowKeyboard.showKeyboard();
        }
    }
}
