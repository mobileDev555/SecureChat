package com.realapps.chat.view.home.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.interfaces.DeactivateDeleteModeLister;
import com.realapps.chat.interfaces.DeleteItemsResponse;
import com.realapps.chat.interfaces.RefreshChatListListener;
import com.realapps.chat.interfaces.SearchResultResponse;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.DbConstants;
import com.realapps.chat.view.custom.MyDividerItemDecoration;
import com.realapps.chat.view.dialoges.DeleteDialog;
import com.realapps.chat.view.home.activity.HomeActivity;
import com.realapps.chat.view.home.activity.IncommingCallActivity;
import com.realapps.chat.view.home.adapters.CallAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Prashant Sharma on 3/15/2018.
 * Core techies
 * prashant@coretechies.org
 */


public class FragmentCall extends Fragment implements CallAdapter.onItemClickListner, DeactivateDeleteModeLister, RefreshChatListListener, SearchResultResponse {
    public static DeactivateDeleteModeLister deactivateDeleteModeLister;

    public static RefreshChatListListener refreshChatListListener;
    public static SearchResultResponse searchResultResponse;
    @BindView(R.id.recycler_chat)
    RecyclerView mRecyclerView;
    @BindView(R.id.lyr_delete)
    LinearLayout lyrDelete;
    Unbinder unbinder;
    int deleteCount;
    private Context mContext;
    private Activity mActivity;
    private ArrayList<ChatListEntity> chatList;
    private CallAdapter mAdapter;
    private DbHelper db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call, container, false);
        deactivateDeleteModeLister = this;
        refreshChatListListener = this;
        searchResultResponse = this;
        mContext = getContext();
        mActivity = getActivity();
        db = new DbHelper(mContext);
        unbinder = ButterKnife.bind(this, view);

        initViews();

        setAdapter();


        return view;
    }


    private void initViews() {
        deleteCount = 0;
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(mContext, DividerItemDecoration.VERTICAL, 0));
    }


    private void setAdapter() {
        chatList = db.getChatList();
        mAdapter = new CallAdapter(mContext, chatList, FragmentCall.this);
        mRecyclerView.setAdapter(mAdapter);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.chat_menu, menu);

        SearchManager searchManager = (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();

        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (chatList.size() > 0)
                    mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (chatList.size() > 0)
                    mAdapter.getFilter().filter(query);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Override
    public void onItemClick(ChatListEntity contactEntity, int position) {
        if (HomeActivity.CALL_DELETE_MODE) {
            toggleSelection(getPostion(contactEntity));
        } else {
            if (contactEntity.getChatType() == AppConstants.SINGLE_CHAT_TYPE) {
                Intent i = new Intent(mContext, IncommingCallActivity.class);
                i.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, contactEntity);
                startActivity(i);
            }

        }
    }

    private void setSeletion() {
        for (int i = 0; i < chatList.size(); i++) {
            chatList.get(i).setSelected(false);
        }
    }

    private int getPostion(ChatListEntity contactEntity) {
        int position = -1;
        for (int i = 0; i < chatList.size(); i++) {
            if (contactEntity.getEccId() == chatList.get(i).getEccId()) {
                position = i;
                break;
            }
        }
        return position;
    }

    @Override
    public void onItemLongPress(ChatListEntity contactEntity, int position) {
        activeDeleteMode(getPostion(contactEntity));
        toggleSelection(getPostion(contactEntity));
    }

    @Override
    public void onDeleteItem(ChatListEntity contactEntity, int position) {
        db.deleteChatList(DbConstants.KEY_ID, contactEntity.getId());
        db.deleteMessageListEntity(DbConstants.KEY_CHAT_ID, contactEntity.getId());
        if (contactEntity.getChatType() == AppConstants.GROUP_CHAT_TYPE) {
            db.deleteGroupMembers(contactEntity.getUserDbId());
        }
        chatList.remove(contactEntity);
        setAdapter();
    }

    public int selectedItemCount() {
        for (int i = 0; i < chatList.size(); i++) {
            if (chatList.get(i).isSelected()) {
                deleteCount++;
            }
        }
        return deleteCount;
    }


    private void deactivateDeleteMode() {
        HomeActivity.CALL_DELETE_MODE = false;
        CallAdapter.DELETE_MODE = false;
        lyrDelete.setVisibility(View.GONE);
        setSeletion();
        // CallAdapter.checkLists = new boolean[chatList.size()];
        mAdapter.notifyDataSetChanged();
    }


    private void toggleSelection(int position) {
        if (chatList.get(position).isSelected()) {
            chatList.get(position).setSelected(false);
        } else {
            chatList.get(position).setSelected(true);
        }
        //   CallAdapter.checkLists[position] = !CallAdapter.checkLists[position];
        mAdapter.notifyDataSetChanged();
    }

    private void activeDeleteMode(int position) {
        HomeActivity.CALL_DELETE_MODE = true;
        CallAdapter.DELETE_MODE = true;

        lyrDelete.setVisibility(View.VISIBLE);
        ((HomeActivity) getActivity()).fabButtonGone();

        // CallAdapter.checkLists[position] = !CallAdapter.checkLists[position];
        mAdapter.notifyDataSetChanged();
    }

    @OnClick({R.id.txt_delete, R.id.txt_delete_all})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_delete:
                int count = selectedItemCount();
                deleteCount = 0;
                if (count > 0) {
                    DeleteSelectedItem(count);
                }
                break;
            case R.id.txt_delete_all:
                deleteAllItem();
                break;
        }
    }

    public void DeleteSelectedItem(int count) {
        new DeleteDialog(mContext, getResources().getString(R.string.app_name), getString(R.string.delete_selected_d_chat, count), new DeleteItemsResponse() {
            @Override
            public void onDelete(boolean delete) {
                deleteListItem();
                chatList = db.getChatList();
                setAdapter();
                deactivateDeleteMode();
            }

            @Override
            public void onClose() {
                deactivateDeleteMode();
            }
        }).show();


    }

    public void deleteAllItem() {

        new DeleteDialog(mContext, getResources().getString(R.string.app_name), getString(R.string.delete_all_chats), new DeleteItemsResponse() {
            @Override
            public void onDelete(boolean delete) {
                DeleteAllListItem();
                setAdapter();
                deactivateDeleteMode();
            }

            @Override
            public void onClose() {
                deactivateDeleteMode();
            }
        }).show();
    }

    private void DeleteAllListItem() {
        chatList.clear();
        db.deleteAllChatList();
        db.deleteAllMessageListEntity();
        db.deleteAllGroupChatMembers();
    }

    private void deleteListItem() {
        //Iterator itr = chatList.iterator();

        for (int i = 0; i < chatList.size(); i++) {
            // itr.next();
            if (chatList.get(i).isSelected()) {
                if (i >= 0) {
                    db.deleteChatList(DbConstants.KEY_ID, chatList.get(i).getId());
                    db.deleteMessageListEntity(DbConstants.KEY_CHAT_ID, chatList.get(i).getId());
                    if (chatList.get(i).getChatType() == AppConstants.GROUP_CHAT_TYPE) {
                        db.deleteGroupMembers(chatList.get(i).getUserDbId());
                    }
                    // itr.remove();

                }
            }

        }
    }

    @Override
    public void onDeActive() {
        deactivateDeleteMode();
    }


    @Override
    public void onRefresh() {
        mActivity.runOnUiThread(() -> {
            HomeActivity.setMenuCounter(R.id.nav_chat, new DbHelper(mContext).getTotalUnreadMessages());
            if (mAdapter != null && mRecyclerView != null) {
                setAdapter();

            }
        });

    }

    @Override
    public void onQueryTextSubmit(String query) {
        if (chatList.size() > 0)
            mAdapter.getFilter().filter(query);

    }

    @Override
    public void onQueryTextChange(String query) {
        if (chatList.size() > 0)
            mAdapter.getFilter().filter(query);

    }
}
