package com.realapps.chat.view.home.fragment;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.interfaces.AddContactDialogResponse;
import com.realapps.chat.interfaces.ContactNameChangeDialogResponse;
import com.realapps.chat.interfaces.DeactivateDeleteModeLister;
import com.realapps.chat.interfaces.DeleteItemsResponse;
import com.realapps.chat.interfaces.ScreenNameChangeDialogResponse;
import com.realapps.chat.interfaces.SocketContactResponse;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.services.MessageClass;
import com.realapps.chat.services.SchedulerEventReceiver;
import com.realapps.chat.ui.api.GlobalClass;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.api.SipProfile;
import com.realapps.chat.ui.helper.PrefManager;
import com.realapps.chat.ui.service.SipService;
import com.realapps.chat.ui.utils.CallHandlerPlugin;
import com.realapps.chat.ui.utils.CustomDistribution;
import com.realapps.chat.ui.utils.PreferencesProviderWrapper;
import com.realapps.chat.ui.utils.PreferencesWrapper;
import com.realapps.chat.ui.widgets.DialerCallBar;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DateTimeUtils;
import com.realapps.chat.utils.DbConstants;
import com.realapps.chat.utils.KeyboardUtils;
import com.realapps.chat.utils.SocketUtils;
import com.realapps.chat.view.custom.MyDividerItemDecoration;
import com.realapps.chat.view.dialoges.DeleteDialog;
import com.realapps.chat.view.dialoges.DialogAddContact;
import com.realapps.chat.view.dialoges.DialogChangeContactName;
import com.realapps.chat.view.dialoges.DialogChangeName;
import com.realapps.chat.view.dialoges.DialogChangeScreenName;
import com.realapps.chat.view.home.activity.CallActivity;
import com.realapps.chat.view.home.activity.ChatWindowActivity;
import com.realapps.chat.view.home.activity.GroupChatWindowActivity;
import com.realapps.chat.view.home.activity.HomeActivity;
import com.realapps.chat.view.home.adapters.ContactAdapter;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Prashant Sharma on 3/15/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class FragmentContacts extends Fragment implements ContactAdapter.onItemClickListner, DeactivateDeleteModeLister, SocketContactResponse, DialerCallBar.OnDialActionListener {
    public static DeactivateDeleteModeLister deactivateDeleteModeLister;
    public static SocketContactResponse socketContactResponse;

    @BindView(R.id.recycler_contacts)
    RecyclerView mRecyclerView;
    @BindView(R.id.linear_delete)
    LinearLayout lyrDelete;
    @BindView(R.id.txt_selected_count)
    TextView txtSelectedCount;
    @BindView(R.id.fab2_contacts)
    ImageView fab2;

    Unbinder unbinder;
    ArrayList<ContactEntity> contactList;
    ContactAdapter mAdapter;
    DbHelper db;
    int deleteCount;
    @BindView(R.id.delete_all_txt)
    TextView txtDeleteAll;
    private Context mContext;
    private Activity mActivity;

    private PreferencesProviderWrapper prefProviderWrapper;

    //calling
    // private ISipService service;
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
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        deactivateDeleteModeLister = this;
        socketContactResponse = this;
        mContext = getContext();
        mActivity = getActivity();
        prefProviderWrapper = new PreferencesProviderWrapper(getActivity());
        unbinder = ButterKnife.bind(this, view);

        db = new DbHelper(mContext);

        initViews();

        setAdapter();

        gc = GlobalClass.getInstance();
        prefManager = new PrefManager(getContext());

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
        if (mRecyclerView != null) {
            contactList = getSortedContactList(db.getContactList());
            mAdapter = new ContactAdapter(mContext, contactList, FragmentContacts.this, mRecyclerView);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    private void notifyList() {
        if (mRecyclerView == null)
            return;

        ArrayList<ContactEntity> actualContactList = db.getContactList();
        contactList = getUpdatedContactList(actualContactList, contactList);
        contactList = getSortedContactList(contactList);
        mAdapter = new ContactAdapter(mContext, contactList, FragmentContacts.this, mRecyclerView);
        mRecyclerView.swapAdapter(mAdapter, false);
    }

    private ArrayList<ContactEntity> getUpdatedContactList(ArrayList<ContactEntity> actualContactList, ArrayList<ContactEntity> contactlist) {
        for (ContactEntity contactEntity : actualContactList) {
            for (ContactEntity entity : contactlist) {
                if (!TextUtils.isEmpty(contactEntity.getEccId()) && !TextUtils.isEmpty(entity.getEccId()) && contactEntity.getEccId().equalsIgnoreCase(entity.getEccId()) && entity.isSelected())
                    contactEntity.setSelected(true);
            }

        }
        return actualContactList;
    }


    public int selectedItemCount() {
        deleteCount = 0;
        for (int i = 0; i < contactList.size(); i++) {
            if (contactList.get(i).isSelected()) {
                deleteCount++;
            }
        }
        return deleteCount;
    }

    public String selectedItemName() {
        String contactName = "";
        for (int i = 0; i < contactList.size(); i++) {
            if (contactList.get(i).isSelected()) {
                contactName = contactList.get(i).getName();
            }
        }
        return contactName;
    }

    public String selectedItemDB_ID() {
        String contactName = "";
        for (int i = 0; i < contactList.size(); i++) {
            if (contactList.get(i).isSelected()) {
                contactName = contactList.get(i).getName();
            }
        }
        return contactName;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.contact_menu, menu);


        SearchManager searchManager = (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (contactList.size() > 0)
                    mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (contactList.size() > 0)
                    mAdapter.getFilter().filter(query);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_new_message:
                if (User_settings.getUserActiveStatus(mContext))
                {
                    if (User_settings.getInventryStatus(mContext)) {
                        if (User_settings.getSubscriptionStatus(mContext)) {
                            new DialogAddContact(mContext, contactList, new AddContactDialogResponse() {
                                @Override
                                public void onAddContact(ContactEntity entity) {
                                    contactList.add(entity);
                                    mAdapter.updateCheckList();
                                    mAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onClose() {

                                }
                            }).show();
                        } else {
                            CommonUtils.showInfoMsg(mContext, "Your subscription has been expired. Please renew.");
                        }
                    } else {
                        CommonUtils.showInfoMsg(mContext, "You do not have any plan yet.");
                    }
                } else {
                    CommonUtils.showInfoMsg(mContext, "Your account has been temporarily suspended. Please try later!");
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onItemClick(ContactEntity contactEntity, int position) {
        if (HomeActivity.CONTACT_DELETE_MODE) {
            toggleSelection(getPosition(contactEntity));
        }
    }

    @Override
    public void onItemLongPress(ContactEntity contactEntity, int position) {
        activeDeleteMode();
        toggleSelection(getPosition(contactEntity));
    }

    @Override
    public void onItemSendMessage(ContactEntity contactEntity, int position, String accept_state) {

        if ((!db.checkUserHaveChatList(contactEntity.getEccId()))) {
            ChatListEntity chatEntity = new ChatListEntity();
            chatEntity.setUserDbId(contactEntity.getUserDbId());
            chatEntity.setEccId(contactEntity.getEccId());
            chatEntity.setName(contactEntity.getName());
            chatEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
            chatEntity.setBurnTime(42);
            chatEntity.setChatType(AppConstants.SINGLE_CHAT_TYPE);

            int id = (int) db.insertChatList(chatEntity);
            chatEntity.setId(id);

            if(accept_state.equalsIgnoreCase(String.valueOf(SocketUtils.accepted))) {
                Intent intent = new Intent(mContext, ChatWindowActivity.class);
                intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatEntity);
                intent.putExtra(AppConstants.TYPE_FRAGMENT, "contacts");
                startActivity(intent);
            }
        } else {
            if(accept_state.equalsIgnoreCase(String.valueOf(SocketUtils.accepted))) {
                ChatListEntity Entity = db.getChatEntity(contactEntity.getEccId());
                Intent intent = new Intent(mContext, ChatWindowActivity.class);
                intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, Entity);
                intent.putExtra(AppConstants.TYPE_FRAGMENT, "contacts");
                startActivity(intent);
            }
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
    public void onCall(ContactEntity contactEntity, int position) {

        Log.e("Encrypted call", ""+ contactEntity.getEccId().toUpperCase());
        //Toast.makeText(mContext, "Encrypted call" + contactList.get((Integer) v.getTag()).getEccId().toUpperCase(), Toast.LENGTH_SHORT).show();*/
        dialNum = contactEntity.getEccId().toUpperCase();
        //placeCall();
        PrefManager prefManager = new PrefManager(getContext());
        Log.e("Encrypted call => ",  ""+ isCallPlaced);
        Log.e("Encrypted call ==> ", ""+ prefManager.get_is_ongoing_call_flag());
        if (isCallPlaced) {
            if (!prefManager.get_is_ongoing_call_flag()) {
                prefManager.set_is_ongoing_call_flag(true);
                placeCall();
            }
        }
        //prefManager.setCallingNum(dialNum);
                /*CallDialog callDialog = new CallDialog(mContext, new DialogResponseListener() {
                    @Override
                    public void onDialogResponse(boolean isResponseOk, Object response) {

                    }
                });
                callDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                callDialog.show();*/
     /*   if ((!db.checkUserHaveChatList(contactEntity.getEccId()))) {
            ChatListEntity chatEntity = new ChatListEntity();
            chatEntity.setUserDbId(contactEntity.getUserDbId());
            chatEntity.setEccId(contactEntity.getEccId());
            chatEntity.setName(contactEntity.getName());
            chatEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
            chatEntity.setBurnTime(42);
            chatEntity.setChatType(AppConstants.SINGLE_CHAT_TYPE);

            int id = (int) db.insertChatList(chatEntity);
            chatEntity.setId(id);

            Intent intent = new Intent(mContext, CallActivity.class);
            intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatEntity);
            startActivity(intent);


        } else {
            ChatListEntity Entity = db.getChatEntity(contactEntity.getEccId().toUpperCase());
            Intent intent = new Intent(mContext, CallActivity.class);
            intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, Entity);
            startActivity(intent);
        }*/
    }

    @Override
    public void onChat(ContactEntity contactEntity, int position) {
        if ((!db.checkUserHaveChatList(contactEntity.getEccId()))) {
            ChatListEntity chatEntity = new ChatListEntity();
            chatEntity.setUserDbId(contactEntity.getUserDbId());
            chatEntity.setEccId(contactEntity.getEccId());
            chatEntity.setName(contactEntity.getName());
            chatEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
            chatEntity.setBurnTime(42);
            chatEntity.setChatType(AppConstants.SINGLE_CHAT_TYPE);

            int id = (int) db.insertChatList(chatEntity);
            chatEntity.setId(id);

            Intent intent = new Intent(mContext, CallActivity.class);
            intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatEntity);
            startActivity(intent);


        } else {
            ChatListEntity Entity = db.getChatEntity(contactEntity.getEccId().toUpperCase());
            Intent intent = new Intent(mContext, CallActivity.class);
            intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, Entity);
            startActivity(intent);
        }
    }

    @Override
    public void onOpeningChat(ContactEntity contactEntity, int position, String accept_state) {
        if (HomeActivity.CHAT_DELETE_MODE) {
            toggleSelection(getPosition(contactEntity));
        } else {
//            if ((!db.checkUserHaveChatList(contactEntity.getEccId()))) {
//                ChatListEntity chatEntity = new ChatListEntity();
//                chatEntity.setUserDbId(contactEntity.getUserDbId());
//                chatEntity.setEccId(contactEntity.getEccId());
//                chatEntity.setName(contactEntity.getName());
//                chatEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
//                chatEntity.setBurnTime(42);
//                chatEntity.setChatType(AppConstants.SINGLE_CHAT_TYPE);
//
//                int id = (int) db.insertChatList(chatEntity);
//                chatEntity.setId(id);
//
//                if (accept_state.equalsIgnoreCase(String.valueOf(SocketUtils.accepted))) {
//                    Intent intent = new Intent(mContext, ChatWindowActivity.class);
//                    intent.putExtra("contactEntity", contactEntity);
//                    intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatEntity);
//                    intent.putExtra(AppConstants.TYPE_FRAGMENT, "contacts");
//                    startActivity(intent);
//                } else if (accept_state.equalsIgnoreCase(String.valueOf(SocketUtils.request))) {
//                    CommonUtils.showInfoMsg(mContext, "You have to accept the request first.");
//                } else if (accept_state.equalsIgnoreCase(String.valueOf(SocketUtils.pending))) {
//                    CommonUtils.showInfoMsg(mContext, "You are in pending state yet.");
//                }
//            } else {
                if (accept_state.equalsIgnoreCase(String.valueOf(SocketUtils.accepted))) {
                    ChatListEntity Entity = db.getChatEntity(contactEntity.getEccId());
                    Intent intent = new Intent(mContext, ChatWindowActivity.class);
                    intent.putExtra("contactEntity", contactEntity);
                    intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, Entity);
                    intent.putExtra(AppConstants.TYPE_FRAGMENT, "contacts");
                    startActivity(intent);
                } else if (accept_state.equalsIgnoreCase(String.valueOf(SocketUtils.request))) {
                    CommonUtils.showInfoMsg(mContext, "You have to accept the request first.");
                } else if (accept_state.equalsIgnoreCase(String.valueOf(SocketUtils.pending))) {
                    CommonUtils.showInfoMsg(mContext, "You are in pending state yet.");
                }
//            }
        }
    }

    @Override
    public void onSlideItemDelete(ContactEntity contactEntity, int position) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
            SocketUtils.sendRemoveContactToSocket(mContext, contactEntity);
            deleteContactFromDb(position);
            notifyList();
        } else {
            CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again));
        }
    }

    @Override
    public void onChangeScreenName(ContactEntity contactEntity, int position) {
        new DialogChangeName(mContext, contactEntity, new ContactNameChangeDialogResponse() {
            @Override
            public void onChangeContactName() {
                notifyList();
            }

            @Override
            public void onClose() {

            }
        }).show();
    }

    @Override
    public void onRejectRequest(ContactEntity contactEntity, int position) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
            SocketUtils.sendResponseContactRequestToSocket(mContext, contactEntity, 0);
            CommonUtils.clearNotification(mContext);
            db.deleteContact(contactEntity.getEccId());
            HomeActivity.setMenuCounter(R.id.nav_contact, new DbHelper(mContext).getTotalFirendrequest());
            notifyList();
        } else {
            CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again));
        }

    }

    @Override
    public void onAcceptRequest(ContactEntity contactEntity, int position) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
            SocketUtils.sendResponseContactRequestToSocket(mContext, contactEntity, 1);
            CommonUtils.clearNotification(mContext);
            db.updateContactEntity(DbConstants.KEY_BLOCK_STATUS, SocketUtils.accepted, DbConstants.KEY_USER_DB_ID, contactEntity.getUserDbId());

            HomeActivity.setMenuCounter(R.id.nav_contact, new DbHelper(mContext).getTotalFirendrequest());
//            HomeActivity.setMenuBadgeCounter();
            notifyList();
        } else {
            CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again));
        }

    }

    private void deactivateDeleteMode() {
        HomeActivity.CONTACT_DELETE_MODE = false;
        ContactAdapter.DELETE_MODE = false;

        lyrDelete.setVisibility(View.GONE);
        ((HomeActivity) getActivity()).fabButtonGone();
        setSelection();
        mAdapter.notifyDataSetChanged();
    }

    private void setSelection() {
        deleteCount = 0;
        selected_count = 0;
        for (int i = 0; i < contactList.size(); i++) {
            contactList.get(i).setSelected(false);
        }

    }


    private int getPosition(ContactEntity contactEntity) {
        int position = -1;
        for (int i = 0; i < contactList.size(); i++) {
            if (contactEntity.getEccId().equalsIgnoreCase(contactList.get(i).getEccId())) {
                position = i;
                break;
            }
        }
        return position;
    }

    int selected_count = 0;
    private void toggleSelection(int position) {
        if (contactList.get(position).isSelected()) {
            contactList.get(position).setSelected(false);
            selected_count --;
        } else {
            contactList.get(position).setSelected(true);
            selected_count ++;
        }

        if (selectedItemCount() == 0) {
            deactivateDeleteMode();
        }

        txtSelectedCount.setText(String.valueOf(selected_count) + "  Selected");
        mAdapter.notifyDataSetChanged();
    }

    private void activeDeleteMode() {
        HomeActivity.CONTACT_DELETE_MODE = true;
        ContactAdapter.DELETE_MODE = true;

        lyrDelete.setVisibility(View.VISIBLE);
        ((HomeActivity) getActivity()).fabButtonGone();

        mAdapter.notifyDataSetChanged();
    }

    @OnClick({R.id.fab2_contacts,R.id.delete_txt, R.id.delete_all_txt, R.id.txt_forward})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fab2_contacts:
                if (User_settings.getUserActiveStatus(mContext)) {
                    if (User_settings.getInventryStatus(mContext)) {
                        if (User_settings.getSubscriptionStatus(mContext)) {
                            new DialogAddContact(mContext, contactList, new AddContactDialogResponse() {
                                @Override
                                public void onAddContact(ContactEntity entity) {
                                    contactList.add(entity);
                                    mAdapter.updateCheckList();
                                    mAdapter.notifyDataSetChanged();
                                }
                                @Override
                                public void onClose() {
                                }
                            }).show();
                        } else {
                            CommonUtils.showInfoMsg(mContext, "Your subscription has been expired. Please renew.");
                        }
                    } else {
                        CommonUtils.showInfoMsg(mContext, "You do not have any plan yet.");
                    }
                } else {
                    CommonUtils.showInfoMsg(mContext, "Your account has been temporarily suspended. Please try later!");
                }

                break;

            case R.id.delete_txt:
                int count = selectedItemCount();
                deleteCount = 0;
                if (count > 0) {
                    new DeleteDialog(mContext, getResources().getString(R.string.app_name), getString(R.string.delete_d_contact, count), new DeleteItemsResponse() {
                        @Override
                        public void onDelete(boolean delete) {
                            if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                                deleteListItem();
                                setAdapter();
                                deactivateDeleteMode();
                            } else {
                                CommonUtils.showInfoMsg(mContext, getString(R.string.please_try_again));
                            }
                        }

                        @Override
                        public void onClose() {
                        }
                    }).show();
                }
                break;

            case R.id.delete_all_txt:
                deleteAllContacts();
                break;

            case R.id.txt_forward:
                forwardToChat();
                break;
        }
    }

    private void forwardToChat() {
        int count = selectedItemCount();
        if (count == 1) {
            int pos = 0;
            for (int i = 0; i < contactList.size(); i++) {
                if (contactList.get(i).isSelected()) {
                    pos = i;
                }
            }

            ContactEntity contactEntity = contactList.get(pos);
            String accept_state = contactEntity.getBlockStatus();

            if ((!db.checkUserHaveChatList(contactEntity.getEccId()))) {
                ChatListEntity chatEntity = new ChatListEntity();
                chatEntity.setUserDbId(contactEntity.getUserDbId());
                chatEntity.setEccId(contactEntity.getEccId());
                chatEntity.setName(contactEntity.getName());
                chatEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
                chatEntity.setBurnTime(42);
                chatEntity.setChatType(AppConstants.SINGLE_CHAT_TYPE);

                int id = (int) db.insertChatList(chatEntity);
                chatEntity.setId(id);

                if (accept_state.equalsIgnoreCase(String.valueOf(SocketUtils.accepted))) {
                    Intent intent = new Intent(mContext, ChatWindowActivity.class);
                    intent.putExtra("contactEntity", contactEntity);
                    intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatEntity);
                    intent.putExtra(AppConstants.TYPE_FRAGMENT, "contacts");
                    startActivity(intent);
                    deactivateDeleteMode();

                } else if (accept_state.equalsIgnoreCase(String.valueOf(SocketUtils.request))) {
                    CommonUtils.showInfoMsg(mContext, "You have to accept the request first.");
                } else if (accept_state.equalsIgnoreCase(String.valueOf(SocketUtils.pending))) {
                    CommonUtils.showInfoMsg(mContext, "You are in pending state yet.");
                }
            } else {
                if (accept_state.equalsIgnoreCase(String.valueOf(SocketUtils.accepted))) {
                    ChatListEntity Entity = db.getChatEntity(contactEntity.getEccId());
                    Intent intent = new Intent(mContext, ChatWindowActivity.class);
                    intent.putExtra("contactEntity", contactEntity);
                    intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, Entity);
                    intent.putExtra(AppConstants.TYPE_FRAGMENT, "contacts");
                    startActivity(intent);
                    deactivateDeleteMode();

                } else if (accept_state.equalsIgnoreCase(String.valueOf(SocketUtils.request))) {
                    CommonUtils.showInfoMsg(mContext, "You have to accept the request first.");
                } else if (accept_state.equalsIgnoreCase(String.valueOf(SocketUtils.pending))) {
                    CommonUtils.showInfoMsg(mContext, "You are in pending state yet.");
                }
            }


        } else {
            CommonUtils.showInfoMsg(mContext, "You have to select only one item");
        }
    }

    public void deleteAllContacts() {
        new DeleteDialog(mContext, getResources().getString(R.string.app_name), getString(R.string.delete_all_contact), new DeleteItemsResponse() {
            @Override
            public void onDelete(boolean delete) {
                deleteAllListItem();
                setAdapter();
                deactivateDeleteMode();
            }

            @Override
            public void onClose() {
            }
        }).show();
    }

    private void deleteAllListItem() {
        for (int i = 0; i < contactList.size(); i++) {
            if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                SocketUtils.sendRemoveContactToSocket(mContext, contactList.get(i));
                deleteContactFromDb(i);
            }
        }
    }

    private void deleteListItem() {
        for (int i = 0; i < contactList.size(); i++) {
            if (contactList.get(i).isSelected()) {
                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                    SocketUtils.sendRemoveContactToSocket(mContext, contactList.get(i));
                    deleteContactFromDb(i);
                }
            }
        }
    }

    private void deleteContactFromDb(int position) {
        db.deleteContact(contactList.get(position).getUserDbId());
        db.deletePublicKey(contactList.get(position).getUserDbId());
    }


    @Override
    public void onDeActive() {
        deactivateDeleteMode();
    }

    @Override
    public void onSocketResponse() {
        mActivity.runOnUiThread(() -> {
            HomeActivity.setMenuCounter(R.id.nav_contact, new DbHelper(mContext).getTotalFirendrequest());
            notifyList();
        });

    }

    private ArrayList<ContactEntity> getSortedContactList(ArrayList<ContactEntity> actualContactList) {

        ArrayList<ContactEntity> tempContactList = new ArrayList<>();
        ArrayList<ContactEntity> requestContactList = new ArrayList<>();
        ArrayList<ContactEntity> pendingContactList = new ArrayList<>();
        ArrayList<ContactEntity> acceptedContactList = new ArrayList<>();
        if (actualContactList.size() > 0) {
            for (int i = 0; i < actualContactList.size(); i++) {
                if (actualContactList.get(i).getBlockStatus().equalsIgnoreCase(String.valueOf(SocketUtils.request))) {
                    requestContactList.add(actualContactList.get(i));
                }

                if (actualContactList.get(i).getBlockStatus().equalsIgnoreCase(String.valueOf(SocketUtils.accepted))) {
                    acceptedContactList.add(actualContactList.get(i));
                }

                if (actualContactList.get(i).getBlockStatus().equalsIgnoreCase(String.valueOf(SocketUtils.pending))) {
                    pendingContactList.add(actualContactList.get(i));
                }
            }
            tempContactList.addAll(requestContactList);
            tempContactList.addAll(acceptedContactList);
            tempContactList.addAll(pendingContactList);

            return tempContactList;
        }

        return actualContactList;
    }

    @Override
    public void onResume() {
        super.onResume();
        CommonUtils.checkStatus(mContext);
        mRecyclerView.setVisibility(View.VISIBLE);

        Log.e("====>", "fragment-contacts");

        isCallPlaced = true;
        prefManager.set_is_ongoing_call_flag(false);
        prefManager.addContact(getActivity(), null);

        if(prefManager.getCallFromChat()){
            PrefManager prefManager = new PrefManager(mContext);
            prefManager.setCallFromChat(false);

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

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private void stopSipService() {
        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        serviceIntent.setPackage(getActivity().getPackageName());
        serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(getActivity(), HomeActivity.class));
        getActivity().stopService(serviceIntent);
        Log.e("====>", "stop sip service");
    }
    private void goToChatWindow() {
        Gson gson = new Gson();
        String json = prefManager.getChatEntry();
        ChatListEntity contactEntity = gson.fromJson(json, ChatListEntity.class);

        if ((!db.checkUserHaveChatList(contactEntity.getEccId()))) {
            ChatListEntity chatEntity = new ChatListEntity();
            chatEntity.setUserDbId(contactEntity.getUserDbId());
            chatEntity.setEccId(contactEntity.getEccId());
            chatEntity.setName(contactEntity.getName());
            chatEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
            chatEntity.setBurnTime(42);
            chatEntity.setChatType(AppConstants.SINGLE_CHAT_TYPE);

            int id = (int) db.insertChatList(chatEntity);
            chatEntity.setId(id);

            Intent intent = new Intent(mContext, ChatWindowActivity.class);
            intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatEntity);
            intent.putExtra(AppConstants.TYPE_FRAGMENT, "contacts");
            startActivity(intent);


        } else {
            ChatListEntity Entity = db.getChatEntity(contactEntity.getEccId());
            Intent intent = new Intent(mContext, ChatWindowActivity.class);
            intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, Entity);
            intent.putExtra(AppConstants.TYPE_FRAGMENT, "contacts");
            startActivity(intent);
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

    }

    @Override
    public void onPause() {
        super.onPause();
        mRecyclerView.setVisibility(View.GONE);
    }

    private void placeCallWithOption(Bundle b) {
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
        if (dataType != null && dataType.length() != 0) {
            toCall = dataType + dialNum;
        } else {
            toCall = dialNum;
        }
        /*toCall = dialNum;*/
        prefManager.setCallingNum("contact");
        gc.setIsCallRunning(true);

        // -- MAKE THE CALL --//
        if (accountToUse >= 0) {
            // It is a SIP account, try to call service for that
            try {
                isCallPlaced = false;
                ((HomeActivity) getActivity()).service.makeCallWithOptions(toCall, accountToUse.intValue(), b);
                //prefManager.setHistoryFetcher_AfterCall(true);

            } catch (RemoteException e) {
                Log.e("====>real call error", "Service can't be called to make the call");
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
            Log.e("====>Calling Flag", ""+ prefManager.get_is_ongoing_call_flag());

        }
    }

    private void placePluginCall(CallHandlerPlugin ch) {
        try {
            String nextExclude = ch.getNextExcludeTelNumber();
            if (((HomeActivity) getActivity()).service != null && nextExclude != null) {
                try {
                    ((HomeActivity) getActivity()).service.ignoreNextOutgoingCallFor(nextExclude);
                } catch (RemoteException e) {
                    Log.e("====>", "Impossible to ignore next outgoing call"+e.getMessage());
                }
            }
            ch.getIntent().send();
        } catch (PendingIntent.CanceledException e) {
            Log.e("====>", "Pending intent cancelled"+e.getMessage());
        }
    }
}
