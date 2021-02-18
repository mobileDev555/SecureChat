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
import com.realapps.chat.view.home.activity.ChatWindowActivity;
import com.realapps.chat.view.home.activity.GroupChatWindowActivity;
import com.realapps.chat.view.home.activity.HomeActivity;
import com.realapps.chat.view.home.adapters.ChatAdapter;

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

public class FragmentGroupChat extends Fragment implements ChatAdapter.onItemClickListner, DeactivateDeleteModeLister, RefreshChatListListener, SearchResultResponse {
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
    private ChatAdapter mAdapter;
    private DbHelper db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
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
        chatList = db.getGChatList();
        mAdapter = new ChatAdapter(mContext, chatList, FragmentGroupChat.this, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void notifyList() {
        ArrayList<ChatListEntity> list = db.getGChatList();
        chatList = getUpdatedList(list, chatList);
        mAdapter = new ChatAdapter(mContext, chatList, FragmentGroupChat.this, mRecyclerView);
        mRecyclerView.swapAdapter(mAdapter, false);
    }

    private ArrayList<ChatListEntity> getUpdatedList(ArrayList<ChatListEntity> list, ArrayList<ChatListEntity> chatList) {
        for (ChatListEntity listEntity : list) {
            for (ChatListEntity entity : chatList) {
                if (listEntity.getUserDbId() == entity.getUserDbId() && entity.isSelected())
                    listEntity.setSelected(true);
            }

        }
        return list;
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
        if (HomeActivity.GROUP_CHAT_DELETE_MODE) {
            toggleSelection(getPostion(contactEntity));
        } else {
            if (contactEntity.getChatType() == AppConstants.SINGLE_CHAT_TYPE) {
                Intent i = new Intent(mContext, ChatWindowActivity.class);
                i.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, contactEntity);
                startActivity(i);
            } else if (contactEntity.getChatType() == AppConstants.GROUP_CHAT_TYPE) {
                Intent i = new Intent(mContext, GroupChatWindowActivity.class);
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
       // HomeActivity.setMenuCounter(R.id.nav_group_chat, db.getTotalUnreadGMessages());
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
        HomeActivity.CHAT_DELETE_MODE = false;
        HomeActivity.GROUP_CHAT_DELETE_MODE = false;
        ChatAdapter.DELETE_MODE = false;
        lyrDelete.setVisibility(View.GONE);
        ((HomeActivity) getActivity()).fabButtonVisible();
        setSeletion();
        // ChatAdapter.checkLists = new boolean[chatList.size()];
        mAdapter.notifyDataSetChanged();
    }


    private void toggleSelection(int position) {
        if (chatList.get(position).isSelected()) {
            chatList.get(position).setSelected(false);
        } else {
            chatList.get(position).setSelected(true);
        }
        if (selectedItemCount() == 0) {
            deactivateDeleteMode();
        }
        mAdapter.notifyDataSetChanged();
    }

    private void activeDeleteMode(int position) {
        HomeActivity.GROUP_CHAT_DELETE_MODE = true;
        ChatAdapter.DELETE_MODE = true;

        lyrDelete.setVisibility(View.VISIBLE);
        ((HomeActivity) getActivity()).fabButtonGone();

        // ChatAdapter.checkLists[position] = !ChatAdapter.checkLists[position];
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
                chatList = db.getGChatList();
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
        db.deleteAllGroupsChatList();
        db.deleteAllGroupMessageListEntity();
        db.deleteAllGroupChatMembers();
        //HomeActivity.setMenuCounter(R.id.nav_group_chat, db.getTotalUnreadGMessages());
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
       // HomeActivity.setMenuCounter(R.id.nav_group_chat, db.getTotalUnreadGMessages());

    }

    @Override
    public void onDeActive() {
        deactivateDeleteMode();
    }


    @Override
    public void onRefresh() {
        mActivity.runOnUiThread(() -> {
            //HomeActivity.setMenuCounter(R.id.nav_group_chat, new DbHelper(mContext).getTotalUnreadGMessages());
            if (mAdapter != null && mRecyclerView != null) {
                notifyList();

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

    @Override
    public void onResume() {
        super.onResume();
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        mRecyclerView.setVisibility(View.GONE);
    }
}
