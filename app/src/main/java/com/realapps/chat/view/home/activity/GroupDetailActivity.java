package com.realapps.chat.view.home.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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
import com.realapps.chat.interfaces.GroupUpdateListener;
import com.realapps.chat.interfaces.ScreenNameChangeDialogResponse;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.model.GroupMemberEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DateTimeUtils;
import com.realapps.chat.utils.KeyboardUtils;
import com.realapps.chat.utils.NetworkUtils;
import com.realapps.chat.utils.SocketUtils;
import com.realapps.chat.view.custom.MyDividerItemDecoration;
import com.realapps.chat.view.dialoges.DialogChangeGroupName;
import com.realapps.chat.view.dialoges.DialogUnlock;
import com.realapps.chat.view.home.adapters.GroupDetailAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupDetailActivity extends AppCompatActivity implements GroupDetailAdapter.OnItemListeners, GroupUpdateListener {
    private static final int REQUEST_CONTACT_SELECT = 10003;
    public static GroupUpdateListener groupUpdateListener;
    @BindView(R.id.recycler_chat)
    RecyclerView mRecyclerView;
    GroupDetailAdapter mAdapter;
    ArrayList<GroupMemberEntity> groupMemberList;
    Context mContext;
    Activity mActivity;
    DbHelper db;
    Bundle bundle;
    @BindView(R.id.fab)
    ImageView fab;
    int groupId;
    int groupServerId;
    String groupName, eccId;
    Toolbar toolbar;
    @BindView(R.id.btn_leave_group)
    Button btnLeaveGroup;
    private ProgressDialog mProgressDialoge;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_select_contact);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mActivity = this;
        mContext = this;
        ButterKnife.bind(mActivity);
        db = new DbHelper(mContext);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //set Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        fab.setVisibility(View.GONE);
        bundle = getIntent().getExtras();
        if (bundle != null) {
            groupId = getIntent().getExtras().getInt(AppConstants.EXTRA_GROUP_ID);
            groupName = getIntent().getExtras().getString(AppConstants.EXTRA_GROUP_NAME);
            eccId = getIntent().getExtras().getString("ecc_id");
        }
        groupMemberList = new ArrayList<>();
        groupMemberList = db.getGroupMemberList(groupId);
        toolbar.setTitle(groupName);
        initViews();
        setAdapter();
    }
    private void initViews() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(mContext, DividerItemDecoration.VERTICAL, 0));
        if (!isImGroupAdmin() && isGroupMember()) {
            btnLeaveGroup.setVisibility(View.VISIBLE);
        } else {
            btnLeaveGroup.setVisibility(View.GONE);
        }
        groupName = db.getGroupChatEntity(groupId).getName();
        toolbar.setTitle(groupName);
    }
    private void setAdapter() {
        groupMemberList = db.getGroupMemberList(groupId);
        mAdapter = new GroupDetailAdapter(mContext, groupMemberList, GroupDetailActivity.this);
        mRecyclerView.setAdapter(mAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isGroupMember()) {
            if (isImGroupAdmin())
                getMenuInflater().inflate(R.menu.group_info_menu, menu);
            else if (isGroupMember())
                getMenuInflater().inflate(R.menu.group_info_menu_not_member, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private boolean isGroupMember() {
        boolean isMember = false;
        for (GroupMemberEntity memberEntity : groupMemberList) {
            if (memberEntity.getEccId().equalsIgnoreCase(User_settings.getECCID(mContext)))
                isMember = true;
        }
        return isMember;
    }

    public boolean isImGroupAdmin() {
        boolean isAdmin = false;
        for (GroupMemberEntity memberEntity : groupMemberList) {
            if (memberEntity.getMemberType() == AppConstants.GROUP_ADMIN && memberEntity.getEccId().equalsIgnoreCase(User_settings.getECCID(mContext)))
                isAdmin = true;
        }
        return isAdmin;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_member:
                Intent intent = new Intent(mContext, AddMemberActivity.class);
                intent.putExtra(AppConstants.EXTRA_GROUP_MEMBER, groupMemberList);
                startActivityForResult(intent, REQUEST_CONTACT_SELECT);
                break;
            case R.id.action_edit_group_name:
                new DialogChangeGroupName(mContext, groupName, new ScreenNameChangeDialogResponse() {
                    @Override
                    public void onChangeName(String name) {
                        KeyboardUtils.hideSoftInput(mActivity);
                        if (isGroupMember())
                            changeNameApi(name);
                        else
                            CommonUtils.showInfoMsg(mContext, getString(R.string.you_are_no_longer_member_of_the_group));
                    }
                    @Override
                    public void onClose() {
                    }
                }).show();
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
                    Log.e("Tag", "onPause: " + "background-008");
                    CommonUtils.lockDialog(mActivity);
                } else {
                    Log.e("Tag", "onPause: " + "forground-008");
                }
            }
        };
        HomeActivity.lockHandler.postDelayed(HomeActivity.runnable, User_settings.getLockTime(mContext));
        groupUpdateListener = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onResume() {
        super.onResume();
        HomeActivity.lockHandler.removeCallbacks(HomeActivity.runnable);
        if (AppConstants.lockscreen) {
            CommonUtils.checkDialog(mActivity);
        }
        groupUpdateListener = this;
        AppConstants.isbackground = false;
        if (AppConstants.lockscreen) {
            new Handler().postDelayed(() -> DialogUnlock.onShowKeyboard.showKeyboard(), 500);
        }
    }
    @Override
    public void onLongClickListeners(GroupMemberEntity memberEntity, int position) {
    }

    @Override
    public void onClickMore(View view, GroupMemberEntity memberEntity, int position) {
        PopupMenu popup = new PopupMenu(mContext, view);
        popup.inflate(R.menu.more_option_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_send_message:
                    sendMessage(memberEntity);
                    break;
                case R.id.action_remove_member:
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(getString(R.string.remove_member));
                    builder.setMessage(getString(R.string.are_you_sure_to_remove_1_s_from_group_2_s, memberEntity.getName(), groupName));
                    builder.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        removeMember(memberEntity, position);
                    });
                    builder.setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                    });
                    builder.show();
                    break;
            }
            return false;
        });
        popup.show();
    }
    private void removeMember(GroupMemberEntity memberEntity, int position) {
        mProgressDialoge = CommonUtils.showLoadingDialog(mContext);
        AndroidNetworking.post(ApiEndPoints.END_POINT_REMOVE_GROUP_MEMBER)
                .addBodyParameter("json_data", getDeleteParameter(memberEntity))
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
                                boolean status = SocketUtils.sendRemoveMemberToSocket(mContext, groupId, groupName, memberEntity);
                                if (status) {
                                    db.deleteGroupMember(groupId, memberEntity.getEccId());
                                    setAdapter();
                                } else
                                    CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again_later));
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
    private String getDeleteParameter(GroupMemberEntity memberEntity) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("group_id", groupId);
            JSONArray member = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("ecc_id", memberEntity.getEccId());
            object.put("member_type", 0);
            member.put(object);
            jsonObject.put("member", member);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
    private void sendMessage(GroupMemberEntity memberEntity) {
        if ((!db.checkUserHaveChatList(memberEntity.getEccId()))) {
            ChatListEntity chatEntity = new ChatListEntity();
            chatEntity.setUserDbId(memberEntity.getUserDbId());
            chatEntity.setEccId(memberEntity.getEccId());
            chatEntity.setName(memberEntity.getName());
            chatEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
            chatEntity.setBurnTime(42);
            chatEntity.setChatType(AppConstants.SINGLE_CHAT_TYPE);
            int id = (int) db.insertChatList(chatEntity);
            chatEntity.setId(id);
            Intent intent = new Intent(mContext, ChatWindowActivity.class);
            intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatEntity);
            startActivity(intent);
        } else {
            ChatListEntity Entity = db.getChatEntity(memberEntity.getEccId());
            Intent intent = new Intent(mContext, ChatWindowActivity.class);
            intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, Entity);
            startActivity(intent);
        }
    }

    @Override
    public void onNameChange() {
        this.runOnUiThread(() -> {
            groupName = db.getGroupChatEntity(groupId).getName();
            if (toolbar != null)
                toolbar.setTitle(groupName);
        });

    }
    @Override
    public void onMemberAdd() {
        mActivity.runOnUiThread(() -> {
            groupMemberList = db.getGroupMemberList(groupId);
            setAdapter();
            invalidateOptionsMenu();
            initViews();
        });
    }
    @Override
    public void onMemberRemove(String eccId, int userId, int gId) {
        mActivity.runOnUiThread(() -> {
            if (gId == groupId) {
                int pos = getMemberPosition(eccId);
                if (pos != -1) {
                    mAdapter.notifyItemRemoved(pos);
                    groupMemberList.remove(pos);
                }
                invalidateOptionsMenu();
                initViews();
            }
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONTACT_SELECT:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<ContactEntity> contacts = (ArrayList<ContactEntity>) data.getSerializableExtra(AppConstants.EXTRA_SELECTED_CONTACT);
                    addMembers(contacts);
                }
                break;
        }
    }

    private void addMembers(ArrayList<ContactEntity> contacts) {
        mProgressDialoge = CommonUtils.showLoadingDialog(mContext);
        AndroidNetworking.post(ApiEndPoints.END_POINT_ADD_GROUP_MEMBER)
                .addBodyParameter("json_data", getAddMemberParameter(contacts))
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
                                boolean status = SocketUtils.sendAddMemberToSocket(mContext, groupId, groupName, contacts);
                                if (status) {
                                    for (ContactEntity row : contacts) {
                                        GroupMemberEntity entity = new GroupMemberEntity();
                                        entity.setChatId(groupId);
                                        entity.setEccId(row.getEccId());
                                        entity.setEccPublicKey(row.getEccPublicKey());
                                        entity.setMemberType(AppConstants.GroupMember);
                                        entity.setName(row.getName());
                                        entity.setUserDbId(row.getUserDbId());
                                        if (!db.checkGroupMember(groupId, row.getEccId())) {
                                            db.insertGroupMember(entity);
                                            groupMemberList.add(entity);
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }
                                } else
                                    CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again_later));
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

    private void changeNameApi(String name) {
        mProgressDialoge = CommonUtils.showLoadingDialog(mContext);

        AndroidNetworking.post(ApiEndPoints.END_POINT_UPDATE_GROUP_NAME)
                .addBodyParameter("group_id", String.valueOf(groupId))
                .addBodyParameter("group_name", name)
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

                                db.updateGroupChatListName(groupId, name);
                                String eccId = db.getEccId(groupId);
                                db.updateVaultItemName(name, eccId);
                                groupName = name;
                                CommonUtils.showInfoMsg(mContext, getString(R.string.group_name_changed_successfully));
                                toolbar.setTitle(name);
                                SocketUtils.sendUpdatedGroupNameToSocket(mContext, groupId, name);
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
                        System.out.println("API Error : " + error.getErrorDetail());
                        CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again));

                    }
                });
    }
    private String getAddMemberParameter(ArrayList<ContactEntity> addedContact) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("group_id", groupId);
            JSONArray member = new JSONArray();
            for (int i = 0; i < addedContact.size(); i++) {
                ContactEntity entity = addedContact.get(i);
                JSONObject object = new JSONObject();
                object.put("ecc_id", entity.getEccId().toUpperCase());
                object.put("member_type", 0);
                member.put(object);
            }
            jsonObject.put("member", member);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    @OnClick(R.id.btn_leave_group)
    public void onViewClicked() {
        if (NetworkUtils.isNetworkConnected(mContext)) {
            if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                mProgressDialoge = CommonUtils.showLoadingDialog(mContext);
                AndroidNetworking.post(ApiEndPoints.END_POINT_REMOVE_GROUP_MEMBER)
                        .addBodyParameter("json_data", getRawData().toString())
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
                                        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                                            db.deleteGroupMember(groupId, User_settings.getECCID(mContext));
                                            int pos = getUserIndex(User_settings.getECCID(mContext));
                                            groupMemberList.remove(pos);
                                            mAdapter.notifyItemRemoved(pos);
                                            SocketUtils.sendLeaveGroupToSocket(mContext, groupId);
                                            btnLeaveGroup.setVisibility(View.GONE);
                                            invalidateOptionsMenu();
                                        } else {
                                            CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again_later));
                                        }
                                    } else {
                                        CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again_later));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onError(ANError error) {
                                if (mProgressDialoge.isShowing())
                                    mProgressDialoge.dismiss();
                                CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again_later));

                            }
                        });
            }
        } else {
            CommonUtils.showInfoMsg(mContext, getString(R.string.no_internet_connection_please_try_again_later));
        }
    }

    private int getUserIndex(String eccid) {
        int i = -1;
        for (int j = 0; j < groupMemberList.size(); j++) {
            if (groupMemberList.get(j).getEccId().equalsIgnoreCase(eccid)) {
                i = j;
                break;
            }
        }
        return i;
    }
    public JSONObject getRawData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("group_id", groupId);
            JSONArray jsonArray = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("ecc_id", User_settings.getECCID(mContext));
            object.put("member_type", 0);
            jsonArray.put(object);
            jsonObject.put("member", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
