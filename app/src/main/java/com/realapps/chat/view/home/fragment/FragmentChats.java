package com.realapps.chat.view.home.fragment;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.realapps.chat.MessageSendService;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.interfaces.ContactNameChangeDialogResponse;
import com.realapps.chat.interfaces.DeactivateDeleteModeLister;
import com.realapps.chat.interfaces.DeleteItemsResponse;
import com.realapps.chat.interfaces.RefreshChatListListener;
import com.realapps.chat.interfaces.SearchResultResponse;
import com.realapps.chat.interfaces.SocketContactResponse;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.ui.api.GlobalClass;
import com.realapps.chat.ui.api.SipProfile;
import com.realapps.chat.ui.helper.PrefManager;
import com.realapps.chat.ui.service.SipService;
import com.realapps.chat.ui.utils.CallHandlerPlugin;
import com.realapps.chat.ui.widgets.DialerCallBar;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DbConstants;
import com.realapps.chat.utils.NetworkUtils;
import com.realapps.chat.utils.NotificationUtils;
import com.realapps.chat.view.custom.MyDividerItemDecoration;
import com.realapps.chat.view.dialoges.DeleteDialog;
import com.realapps.chat.view.dialoges.DialogChangeContactName;
import com.realapps.chat.view.dialoges.DialogChangeName;
import com.realapps.chat.view.home.activity.ChatWindowActivity;
import com.realapps.chat.view.home.activity.GroupChatWindowActivity;
import com.realapps.chat.view.home.activity.HomeActivity;
import com.realapps.chat.view.home.adapters.ChatAdapter;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Prashant Sharma on 3/15/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class FragmentChats extends Fragment implements ChatAdapter.onItemClickListner, DeactivateDeleteModeLister, RefreshChatListListener, SearchResultResponse, SocketContactResponse, DialerCallBar.OnDialActionListener {

    public static DeactivateDeleteModeLister deactivateDeleteModeLister;
    public static RefreshChatListListener refreshChatListListener;
    public static SearchResultResponse searchResultResponse;
    public static SocketContactResponse socketContactResponse;
    @BindView(R.id.recycler_chat)
    RecyclerView mRecyclerView;
    @BindView(R.id.lyr_delete)
    LinearLayout lyrDelete;
    @BindView(R.id.txt_selected_count)
    TextView txtSelectedCount;

    Unbinder unbinder;
    int deleteCount = 0;
    private Context mContext;
    private Activity mActivity;
    private ArrayList<ChatListEntity> chatList;
    private ChatAdapter mAdapter;
    private DbHelper db;
    private String TAG = FragmentChats.class.getSimpleName();

    GlobalClass gc;
    boolean isCallPlaced = true;
    String dialNum;
    PrefManager prefManager;
    Long accountToUse;

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
        socketContactResponse = this;
        mContext = getContext();
        mActivity = getActivity();
        db = new DbHelper(mContext);
        unbinder = ButterKnife.bind(this, view);

        initViews();
//        setAdapter();
        gc = GlobalClass.getInstance();
        prefManager = new PrefManager(getContext());

        if(NetworkUtils.isNetworkConnected(mContext)) {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int mm = cal.get(Calendar.MONTH) + 1;
            int dd = cal.get(Calendar.DAY_OF_MONTH);
            String cdate = year+"-"+("00"+mm).substring(("00"+mm).length()-2)+"-"+("00"+dd).substring(("00"+dd).length()-2);
            String odate = User_settings.getLastClearDate(mContext);
            if(odate.length()==0){
                User_settings.setLastClearDate(getActivity(), cdate);
            } else {
                if (cdate.compareTo(odate) > 0) {
                    if (!isMyServiceRunning(MessageSendService.class)) {
                        User_settings.setLastClearDate(getActivity(), cdate);
                        getActivity().startService(new Intent(getActivity(), MessageSendService.class));
                    }
                }
            }
        }

        return view;
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
        mAdapter = new ChatAdapter(mContext, chatList, FragmentChats.this, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.chat_menu, menu);

        SearchManager searchManager = (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
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
    public void onItemClick(ChatListEntity chatListEntity, int position) {

        if (HomeActivity.CHAT_DELETE_MODE) {
            toggleSelection(getPostion(chatListEntity));
            Log.e(TAG, "onItemClick: CHAT_DELETE_MODE");
        } else {
            if (chatListEntity.getChatType() == AppConstants.SINGLE_CHAT_TYPE) {

                Intent i = new Intent(mContext, ChatWindowActivity.class);
                i.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
                i.putExtra(AppConstants.TYPE_FRAGMENT, "chat");
                startActivity(i);
            } else if (chatListEntity.getChatType() == AppConstants.GROUP_CHAT_TYPE) {

                Intent i = new Intent(mContext, GroupChatWindowActivity.class);
                i.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
                i.putExtra(AppConstants.TYPE_FRAGMENT, "chat");
                startActivity(i);
            }

        }
    }

    private void setSeletion() {
        deleteCount = 0;
        selected_count = 0;
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
    public void onDeleteItem(ChatListEntity chatListEntity, int position) {
        db.deleteChatList(DbConstants.KEY_ID, chatListEntity.getId());
        db.deleteMessageListEntity(DbConstants.KEY_CHAT_ID, chatListEntity.getId());
        if (chatListEntity.getChatType() == AppConstants.GROUP_CHAT_TYPE) {
            db.deleteGroupMembers(chatListEntity.getUserDbId());
        }
        HomeActivity.setMenuCounter(R.id.nav_chat, db.getTotalUnreadMessages());
        chatList.remove(chatListEntity);
        setAdapter();
    }

    public int selectedItemCount() {
        deleteCount = 0;
        for (int i = 0; i < chatList.size(); i++) {
            if (chatList.get(i).isSelected()) {
                deleteCount++;
            }
        }
        return deleteCount;
    }

    public String selectedItemEccID() {
        String eccId = "";
        for (int i = 0; i < chatList.size(); i++) {
            if (chatList.get(i).isSelected()) {
                eccId = chatList.get(i).getEccId();
            }
        }
        return eccId;
    }

    public int selectedItemUserDBID() {
        int id = -1;
        for (int i = 0; i < chatList.size(); i++) {
            if (chatList.get(i).isSelected()) {
                id = chatList.get(i).getUserDbId();
            }
        }
        return id;
    }


    private void deactivateDeleteMode() {
        HomeActivity.CHAT_DELETE_MODE = false;
        ChatAdapter.DELETE_MODE = false;
        lyrDelete.setVisibility(View.GONE);
        ((HomeActivity) getActivity()).fabButtonVisible();
        setSeletion();
        // ChatAdapter.checkLists = new boolean[chatList.size()];
        mAdapter.notifyDataSetChanged();
    }


    int selected_count = 0;
    private void toggleSelection(int position) {
        if (chatList.get(position).isSelected()) {
            chatList.get(position).setSelected(false);
            selected_count = selected_count - 1;
        } else {
            chatList.get(position).setSelected(true);
            selected_count = selected_count + 1;
        }
        if (selectedItemCount() == 0) {
            deactivateDeleteMode();
        }
        txtSelectedCount.setText(String.valueOf(selected_count) + "  Selected");
        mAdapter.notifyDataSetChanged();
    }

    private void activeDeleteMode(int position) {
        HomeActivity.CHAT_DELETE_MODE = true;
        ChatAdapter.DELETE_MODE = true;

        lyrDelete.setVisibility(View.VISIBLE);
        ((HomeActivity) getActivity()).fabButtonGone();

        // ChatAdapter.checkLists[position] = !ChatAdapter.checkLists[position];
        mAdapter.notifyDataSetChanged();
    }

    @OnClick({R.id.txt_delete, R.id.txt_delete_all, R.id.txt_forward, R.id.txt_edit})
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
            case R.id.txt_forward:
                forwardToChat();
                break;
            case R.id.txt_edit:
                changeContactName();
                break;
        }
    }

    private void changeContactName() {
        int count = selectedItemCount();
        if (count == 1) {
//            new DialogChangeName()

            String eccId = selectedItemEccID();
            int userDBId = selectedItemUserDBID();

            new DialogChangeContactName(mContext, eccId, userDBId, new ContactNameChangeDialogResponse() {
                @Override
                public void onChangeContactName() {
                    notifyList();
                }

                @Override
                public void onClose() {

                }
            }).show();
        } else {
            CommonUtils.showInfoMsg(mContext, "You have to select one item");
        }
    }

    private void forwardToChat() {
        int count = selectedItemCount();
        if(count == 1) {
            int pos = 0;
            for (int i = 0; i < chatList.size(); i++) {
                if (chatList.get(i).isSelected()) {
                    pos = i;
                }
            }
            ChatListEntity chatListEntity = chatList.get(pos);
            if (chatListEntity.getChatType() == AppConstants.SINGLE_CHAT_TYPE) {

                Intent i = new Intent(mContext, ChatWindowActivity.class);
                i.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
                i.putExtra(AppConstants.TYPE_FRAGMENT, "chat");
                startActivity(i);

            } else if (chatListEntity.getChatType() == AppConstants.GROUP_CHAT_TYPE) {

                Intent i = new Intent(mContext, GroupChatWindowActivity.class);
                i.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
                i.putExtra(AppConstants.TYPE_FRAGMENT, "chat");
                startActivity(i);
            }

            deactivateDeleteMode();

        } else {
            CommonUtils.showInfoMsg(mContext, "You have to select only one item");
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
            }
        }).show();
    }

    private void DeleteAllListItem() {
        chatList.clear();
        db.deleteAllChatList();
        db.deleteAllMessageListEntity();
        db.deleteAllGroupChatMembers();
        HomeActivity.setMenuCounter(R.id.nav_chat, db.getTotalUnreadMessages());
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
                }
            }

        }
        HomeActivity.setMenuCounter(R.id.nav_chat, db.getTotalUnreadMessages());

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
                notifyList();
            }
        });

    }

    private void notifyList() {
        ArrayList<ChatListEntity> list = db.getChatList();
        chatList = getUpdatedList(list, chatList);
        mAdapter = new ChatAdapter(mContext, chatList, FragmentChats.this, mRecyclerView);
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
        Log.e("========resume", "fragment-chat");
        setAdapter();

        isCallPlaced = true;
        prefManager.set_is_ongoing_call_flag(false);
        prefManager.addContact(getActivity(), null);

        if(prefManager.getCallFromChat()){
            PrefManager prefManager = new PrefManager(mContext);
            prefManager.setCallFromChat(false);

            Log.e("======>2", ""+isMyServiceRunning(SipService.class));

            if (!isMyServiceRunning(SipService.class)) {
                new HomeActivity().startSipService();
            }
            if(AppConstants.mWebSocketClient.getConnection() == null || !AppConstants.mWebSocketClient.getConnection().isOpen()) {
                Log.e("====> websocket start", "ok");
                new HomeActivity().startBackGroundThread(mActivity);
            }

            callPerson();
            goToChatWindow();
        }

    }

    private void goToChatWindow() {
        Gson gson = new Gson();
        String json = prefManager.getChatEntry();
        ChatListEntity chatListEntity = gson.fromJson(json, ChatListEntity.class);

        if (chatListEntity.getChatType() == AppConstants.SINGLE_CHAT_TYPE) {

            Log.e(TAG, "onItemClick: SINGLE_CHAT_TYPE");
            Intent i = new Intent(mContext, ChatWindowActivity.class);
            i.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
            i.putExtra(AppConstants.TYPE_FRAGMENT, "chat");
            startActivity(i);
        } else if (chatListEntity.getChatType() == AppConstants.GROUP_CHAT_TYPE) {

            Log.e(TAG, "onItemClick: GROUP_CHAT_TYPE");
            Intent i = new Intent(mContext, GroupChatWindowActivity.class);
            i.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
            i.putExtra(AppConstants.TYPE_FRAGMENT, "chat");
            startActivity(i);
        }
    }

    private void callPerson() {
        PrefManager prefManager = new PrefManager(mContext);

        isCallPlaced = true; //??
        dialNum = prefManager.getEccIdToBeCalled().toUpperCase();
        prefManager.setEccIdToBeCalled("null");
        if (isCallPlaced) {
            if (!prefManager.get_is_ongoing_call_flag()) {
                prefManager.set_is_ongoing_call_flag(true);
                placeCall();
            }
        }
    }

    @Override
    public void placeCall() {
        AppConstants.isbackground = false;
        AppConstants.lockscreen = false;
        AppConstants.onpermission = true;
        placeCallWithOption(null);
    }

    @Override
    public void deleteAll() {
        Log.e("======chat del", "ok");
    }

    private void placeCallWithOption(Bundle b) {
        System.out.println("Encrypted call ==>service " + ((HomeActivity) getActivity()).service);
        if (((HomeActivity) getActivity()).service == null) {
            return;
        }

        String toCall = "";
        accountToUse = SipProfile.INVALID_ID;
        // Find account to use
        //accountChooserButton.getSelectedAccount();

        Uri toBeChanged = Uri.parse("content://com.shadowsecure.call.db/accounts");


        //Cursor c = getActivity().getContentResolver().query(toBeChanged, null, null, null, null);
//        Cursor c = getActivity().getContentResolver().query(SipProfile.ACCOUNT_URI, null, null, null, null);
//        System.out.println("Cursor count" + c.getCount());
//        c.moveToFirst();
//        SipProfile acc = new SipProfile(c);
//        if (acc == null) {
//            return;
//        }

        accountToUse = Long.parseLong("1");
        // Find number to dial
        String dataType = gc.checkNetworkType(getActivity());
        System.out.println("Encrypted Check network type: " + dataType);
        if (dataType != null && dataType.length() != 0) {
            toCall = dataType + dialNum;
        } else {
            toCall = dialNum;
        }
        /*toCall = dialNum;*/
        prefManager.setCallingNum("contact");
        gc.setIsCallRunning(true);

        System.out.println("Encrypted call ==>accountToUse " + accountToUse);
        // -- MAKE THE CALL --//
        if (accountToUse >= 0) {
            // It is a SIP account, try to call service for that
            try {
                isCallPlaced = false;

                ((HomeActivity) getActivity()).service.makeCallWithOptions(toCall, accountToUse.intValue(), b);
                //prefManager.setHistoryFetcher_AfterCall(true);

            } catch (RemoteException e) {
                android.util.Log.e(FragmentContacts.class.toString(), "Service can't be called to make the call");
            }
        } else if (accountToUse != SipProfile.INVALID_ID) {
            // It's an external account, find correct external account
            CallHandlerPlugin ch = new CallHandlerPlugin(getActivity());
            ch.loadFrom(accountToUse, toCall, new CallHandlerPlugin.OnLoadListener() {
                @Override
                public void onLoad(CallHandlerPlugin ch) {
                    placePluginCall(ch);
                }
            });
        } else {
            prefManager.set_is_ongoing_call_flag(false);
            System.out.println("Calling Flag : CAllhistory inside else: " + prefManager.get_is_ongoing_call_flag());

        }
    }

    private void placePluginCall(CallHandlerPlugin ch) {
        try {
            String nextExclude = ch.getNextExcludeTelNumber();
            if (((HomeActivity) getActivity()).service != null && nextExclude != null) {
                try {
                    ((HomeActivity) getActivity()).service.ignoreNextOutgoingCallFor(nextExclude);
                } catch (RemoteException e) {
                    android.util.Log.e(FragmentContacts.class.toString(), "Impossible to ignore next outgoing call", e);
                }
            }
            ch.getIntent().send();
        } catch (PendingIntent.CanceledException e) {
            android.util.Log.e(FragmentContacts.class.toString(), "Pending intent cancelled", e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mRecyclerView.setVisibility(View.GONE);
        deactivateDeleteMode();
    }

    @Override
    public void onSocketResponse() {
        mActivity.runOnUiThread(() -> {
            HomeActivity.setMenuCounter(R.id.nav_contact, new DbHelper(mContext).getTotalFirendrequest());
            notifyList();
        });

    }
}
