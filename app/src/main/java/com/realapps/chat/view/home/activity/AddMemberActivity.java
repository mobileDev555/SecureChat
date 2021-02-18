package com.realapps.chat.view.home.activity;

import android.app.Activity;
import android.app.SearchManager;
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
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.model.GroupMemberEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.view.custom.MyDividerItemDecoration;
import com.realapps.chat.view.dialoges.DialogUnlock;
import com.realapps.chat.view.home.adapters.SelectContactAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddMemberActivity extends AppCompatActivity implements SelectContactAdapter.onItemClickListener {

    @BindView(R.id.recycler_chat)
    RecyclerView mRecyclerView;
    Context mContext;
    Activity mActivity;
    ArrayList<ContactEntity> contacts;
    ArrayList<GroupMemberEntity> groupMember;
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
        toolbar.setTitle("Select Contacts");
        db = new DbHelper(mContext);
        contacts = new ArrayList<>();
        // contacts = db.getContactList();
        contacts = db.getAcceptedContactList();
        initViews();
        setAdapter();

        bundle = getIntent().getExtras();
        if (bundle != null) {
            groupMember = (ArrayList<GroupMemberEntity>) bundle.getSerializable(AppConstants.EXTRA_GROUP_MEMBER);
            setSelected();
        }
    }

    private void setSelected() {

        for (int i = 0; i < groupMember.size(); i++) {
            for (int j = 0; j < contacts.size(); j++) {
                if (groupMember.get(i).getEccId().equalsIgnoreCase(contacts.get(j).getEccId())) {
                    contacts.get(j).setSelected(true);
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
        Intent intent = new Intent();
        intent.putExtra(AppConstants.EXTRA_SELECTED_CONTACT, getSelectedContact());
        setResult(RESULT_OK, intent);
        finish();
    }

    private ArrayList<ContactEntity> getSelectedContact() {
        ArrayList<ContactEntity> selectedContacts = new ArrayList<>();
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).isSelected() && !isAlreayMember(contacts.get(i)))
                selectedContacts.add(contacts.get(i));
        }
        return selectedContacts;
    }


    private void toggleSelection(ContactEntity contactEntity, int position) {
        contacts.get(contacts.indexOf(contactEntity)).setSelected(!contacts.get(contacts.indexOf(contactEntity)).isSelected());
        mAdapter.notifyItemChanged(position);
    }


    @Override
    public void onItemClick(ContactEntity contactEntity, int position) {
        if (!isAlreayMember(contactEntity)) {
            toggleSelection(contactEntity, position);
            mAdapter.notifyItemChanged(position);
        }
    }

    private boolean isAlreayMember(ContactEntity contactEntity) {
        boolean isMember = false;

        for (GroupMemberEntity memberEntity : groupMember) {
            if (memberEntity.getEccId().equalsIgnoreCase(contactEntity.getEccId()))
                isMember = true;
        }

        return isMember;
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
                    Log.e("Tag", "onPause: " + "background-005");
                    CommonUtils.lockDialog(mActivity);
                } else {
                    Log.e("Tag", "onPause: " + "forground-005");
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
