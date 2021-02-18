package com.realapps.chat.view.home.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DateTimeUtils;
import com.realapps.chat.view.custom.MyDividerItemDecoration;
import com.realapps.chat.view.dialoges.DialogUnlock;
import com.realapps.chat.view.home.adapters.SelectContactAdapter;
import com.realapps.chat.view.home.fragment.FragmentChats;
import com.realapps.chat.view.home.fragment.FragmentGroupChat;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectContactActivity extends AppCompatActivity implements SelectContactAdapter.onItemClickListener {

    Context mContext;
    Activity mActivity;
    @BindView(R.id.recycler_chat)
    RecyclerView mRecyclerView;
    ArrayList<ContactEntity> contacts;
    ArrayList<ContactEntity> selectedContact;
    SelectContactAdapter mAdapter;
    DbHelper db;
    Bundle bundle;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //set Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setTitle(getString(R.string.select_contacts));
        db = new DbHelper(mContext);
        contacts = new ArrayList<>();
        contacts = db.getAcceptedContactList();

        initViews();
        setAdapter();

        bundle = getIntent().getExtras();
        if (bundle != null) {
            selectedContact = (ArrayList<ContactEntity>) bundle.getSerializable(AppConstants.EXTRA_SELECTED_CONTACT);
            setSelected();
        }


    }

    private void setSelected() {

        for (int i = 0; i < selectedContact.size(); i++) {
            for (int j = 0; j < contacts.size(); j++) {
                if (selectedContact.get(i).getEccId().equalsIgnoreCase(contacts.get(j).getEccId())) {
                    contacts.get(j).setSelected(false);
                    mAdapter.notifyItemChanged(j);
                }

            }
        }

    }

    private void initViews() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(mContext, DividerItemDecoration.VERTICAL, 0));
    }


    private void setAdapter() {
        mAdapter = new SelectContactAdapter(mContext, contacts, this);
        mRecyclerView.setAdapter(mAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_contact_menu, menu);
        SearchManager searchManager = (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(mActivity.getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (contacts.size() > 0)
                    mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (contacts.size() > 0)
                    mAdapter.getFilter().filter(query);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @OnClick(R.id.fab)
    public void onViewClicked() {
        ComponentName callingComponent = getCallingActivity();

        if (getSelectedContact().size() > 0)

            if (callingComponent == null) // result not expected
            {
                if (getSelectedContact().size() > 1) {
                    Intent intent = new Intent(mContext, GroupCreateActivity.class);
                    intent.putExtra(AppConstants.EXTRA_SELECTED_CONTACT, getSelectedContact());
                    startActivity(intent);
                    finish();
                } else {
                    ContactEntity contactEntity = getSelectedContact().get(0);
                    if ((!db.checkUserHaveChatList(contactEntity.getEccId()))) {
                        ChatListEntity chatEntity = new ChatListEntity();
                        chatEntity.setUserDbId(contactEntity.getUserDbId());
                        chatEntity.setEccId(contactEntity.getEccId());
                        chatEntity.setName(contactEntity.getName());
                        chatEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
                        chatEntity.setBurnTime(42);
                        chatEntity.setChatType(AppConstants.SINGLE_CHAT_TYPE);
                        int id = (int) db.insertChatList(chatEntity);
                        if (FragmentChats.refreshChatListListener != null) {
                            FragmentChats.refreshChatListListener.onRefresh();
                        }
                        if (FragmentGroupChat.refreshChatListListener != null) {
                            FragmentGroupChat.refreshChatListListener.onRefresh();
                        }
                        chatEntity.setId(id);
                        Intent intent = new Intent(mContext, ChatWindowActivity.class);
                        intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatEntity);
                        startActivity(intent);
                        finish();
                    } else {
                        if (FragmentChats.refreshChatListListener != null) {
                            FragmentChats.refreshChatListListener.onRefresh();
                        }
                        if (FragmentGroupChat.refreshChatListListener != null) {
                            FragmentGroupChat.refreshChatListListener.onRefresh();
                        }
                        ChatListEntity Entity = db.getChatEntity(contactEntity.getEccId());
                        Intent intent = new Intent(mContext, ChatWindowActivity.class);
                        intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, Entity);
                        startActivity(intent);
                        finish();
                    }
                }

            } else {
                if (callingComponent.getClassName().contains("GroupCreateActivity")) {
                    Intent intent = new Intent();
                    intent.putExtra(AppConstants.EXTRA_SELECTED_CONTACT, getSelectedContact());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(AppConstants.EXTRA_SELECTED_CONTACT, getSelectedContact());
                    setResult(RESULT_OK, intent);
                    finish();
                }

            }
    }

    private ArrayList<ContactEntity> getSelectedContact() {
        ArrayList<ContactEntity> selectedContacts = new ArrayList<>();
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).isSelected())
                selectedContacts.add(contacts.get(i));
        }
        return selectedContacts;
    }

    private int getPostion(ContactEntity contactEntity) {
        int position = -1;
        for (int i = 0; i < contacts.size(); i++) {
            if (contactEntity.getEccId() == contacts.get(i).getEccId()) {
                position = i;
                break;
            }
        }
        return position;
    }

    private void toggleSelection(int position) {
        if (contacts.get(position).isSelected()) {
            contacts.get(position).setSelected(false);
        } else {
            contacts.get(position).setSelected(true);
        }
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClick(ContactEntity contactEntity, int position) {
        toggleSelection(getPostion(contactEntity));
        mAdapter.notifyDataSetChanged();
    }
    @Override
    public void onItemLongPress(ContactEntity contactEntity, int position) {
    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppConstants.isbackground = true;
        HomeActivity.runnable = new Runnable() {
            @Override
            public void run() {
                if (AppConstants.isbackground) {
                    Log.e("Tag", "onPause: " + "background-002");
                    CommonUtils.lockDialog(mActivity);
                } else {
                    Log.e("Tag", "onPause: " + "forground-002");
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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    DialogUnlock.onShowKeyboard.showKeyboard();
                }
            }, 500);

        }
    }
}
