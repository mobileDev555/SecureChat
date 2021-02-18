package com.realapps.chat.view.home.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.model.VaultEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.view.custom.MyDividerItemDecoration;
import com.realapps.chat.view.dialoges.DialogUnlock;
import com.realapps.chat.view.home.adapters.ShareFromVaultAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShareFromVaultActivity extends AppCompatActivity implements ShareFromVaultAdapter.onItemClickListener {

    private static final int REQUEST_MESSAGE_SELECT = 1001;
    Activity mActivity;
    Context mContext;
    @BindView(R.id.recycler_chat)
    RecyclerView mRecyclerView;
    ArrayList<VaultEntity> vaultListItems;
    ShareFromVaultAdapter mAdapter;
    DbHelper db;
    Bundle bundle;
    int itemType;
    @BindView(R.id.fab)
    ImageView fab;

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
        if (getIntent().getExtras() != null)
            itemType = getIntent().getIntExtra(AppConstants.EXTRA_ITEM_TYPE, -1);

        if (itemType == AppConstants.ITEM_TYPE_CHATS)
            toolbar.setTitle(getString(R.string.select_saved_message));
        else if (itemType == AppConstants.ITEM_TYPE_PICTURE)
            toolbar.setTitle(getString(R.string.select_picture));
        else if (itemType == AppConstants.ITEM_TYPE_NOTES)
            toolbar.setTitle(getString(R.string.select_personal_notes));

        db = new DbHelper(mContext);
        vaultListItems = db.getVaultEntityList(itemType);
        initViews();
        setAdapter();

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mAdapter.DELETE_MODE = false;
    }

    private void initViews() {
        if (itemType == AppConstants.ITEM_TYPE_CHATS) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(mContext, DividerItemDecoration.VERTICAL, 0));
    }

    private void setAdapter() {
        mAdapter = new ShareFromVaultAdapter(mContext, vaultListItems, this);
        mRecyclerView.setAdapter(mAdapter);
        if (itemType != AppConstants.ITEM_TYPE_CHATS) {
            mAdapter.DELETE_MODE = true;
            mAdapter.notifyDataSetChanged();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_contact_menu, menu);
        SearchManager searchManager = (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();

        /* searchView.setQueryHint("Search Speak");*/
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(mActivity.getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (vaultListItems.size() > 0)
                    mAdapter.getFilter().filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                if (vaultListItems.size() > 0)
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
        if (getSelecteditems().size() > 0)
            if (callingComponent.getClassName().contains("GroupCreateActivity")) {
                mAdapter.DELETE_MODE = false;
                Intent intent = new Intent(mContext, GroupCreateActivity.class);
                intent.putExtra(AppConstants.EXTRA_SELECTED_VAULTITES, getSelecteditems());
                startActivity(intent);
                finish();
            } else {

                mAdapter.DELETE_MODE = false;
                Intent intent = new Intent();
                intent.putExtra(AppConstants.EXTRA_SELECTED_VAULTITES, getSelecteditems());
                setResult(RESULT_OK, intent);
                finish();
            }

    }

    private ArrayList<VaultEntity> getSelecteditems() {
        ArrayList<VaultEntity> listEntities = new ArrayList<>();
        for (int i = 0; i < vaultListItems.size(); i++) {
            if (vaultListItems.get(i).getSelected())
                listEntities.add(vaultListItems.get(i));
        }
        return listEntities;
    }


    private void toggleSelection(int position) {
        if (vaultListItems.get(position).getSelected()) {
            vaultListItems.get(position).setSelected(false);
        } else {
            vaultListItems.get(position).setSelected(true);
        }
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClick(VaultEntity contactEntity, int position) {
        if (itemType != AppConstants.ITEM_TYPE_CHATS) {
            toggleSelection(getPostion(contactEntity));
            mAdapter.notifyDataSetChanged();
        } else {
            Intent intent = new Intent(mContext, VaultMessageWindowActivity.class);
            intent.putExtra(AppConstants.EXTRA_VAULT_LIST_ITEM, contactEntity);
            intent.putExtra(AppConstants.EXTRA_IS_SHARE, true);
            startActivityForResult(intent, REQUEST_MESSAGE_SELECT);
        }
    }

    private int getPostion(VaultEntity contactEntity) {
        int position = -1;
        for (int i = 0; i < vaultListItems.size(); i++) {
            if (contactEntity.getName() == vaultListItems.get(i).getName()) {
                position = i;
                break;
            }
        }
        return position;
    }


    @Override
    public void onItemLongPress(VaultEntity contactEntity, int position) {
    }

    @Override
    public void onDelete(VaultEntity contactEntity, int position) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_MESSAGE_SELECT:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<ChatMessageEntity> selectedMessage = (ArrayList<ChatMessageEntity>) data.getSerializableExtra(AppConstants.EXTRA_VAULT_MESSAGE);

                    ShareFromVaultAdapter.DELETE_MODE = false;
                    Intent intent = new Intent();
                    intent.putExtra(AppConstants.EXTRA_VAULT_MESSAGE, selectedMessage);
                    setResult(2, intent);
                    finish();

                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppConstants.isbackground = true;
        HomeActivity.runnable = () -> {
            if (AppConstants.isbackground) {
                Log.e("Tag", "onPause: " + "background-007");
                CommonUtils.lockDialog(mActivity);
            } else {
                Log.e("Tag", "onPause: " + "forground-007");
            }


        };
        HomeActivity.lockHandler.postDelayed(HomeActivity.runnable,User_settings.getLockTime(mContext));
    }

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
