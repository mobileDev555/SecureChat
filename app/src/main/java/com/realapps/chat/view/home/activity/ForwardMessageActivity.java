package com.realapps.chat.view.home.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.koushikdutta.ion.Ion;
import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.parser.PublicKeysParser;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.model.GroupMemberEntity;
import com.realapps.chat.model.PublicKeyEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.Cryptography;
import com.realapps.chat.utils.DateTimeUtils;
import com.realapps.chat.utils.NetworkUtils;
import com.realapps.chat.utils.SocketUtils;
import com.realapps.chat.view.custom.MyDividerItemDecoration;
import com.realapps.chat.view.home.VaultFileSaveUtils;
import com.realapps.chat.view.home.adapters.ForwardMessageAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ForwardMessageActivity extends AppCompatActivity implements ForwardMessageAdapter.onItemClickListner {

    private static final String TAG = ForwardMessageActivity.class.getSimpleName();
    public ArrayList<GroupMemberEntity> groupMemberList;
    @BindView(R.id.recycler_chat)
    RecyclerView mRecyclerView;
    ArrayList<ContactEntity> contacts;
    ForwardMessageAdapter mAdapter;
    Context mContext;
    Activity mActivity;
    DbHelper db;
    Bundle bundle;
    int mCount = 0;
    int mGroupCount = 0;
    ArrayList<ChatMessageEntity> chatMessageEntityArrayList;
    @BindView(R.id.fab)
    ImageView fab;
    ContactEntity contactEntity;
    private boolean clickEvent = true;
    private boolean encrypted;
    String fileName;
    public boolean isEncrypted() {
        return encrypted;
    }
    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_select_contact);
        mContext = ForwardMessageActivity.this;
        mActivity = ForwardMessageActivity.this;
        ButterKnife.bind(mActivity);


        bundle = getIntent().getExtras();
        if (bundle != null) {
            chatMessageEntityArrayList = (ArrayList<ChatMessageEntity>) bundle.get(AppConstants.EXTRA_MESSAGE_LIST);
            setEncrypted(bundle.getBoolean(AppConstants.IS_ENCRYPTED));
            if (bundle.containsKey("name"))
                fileName = bundle.getString("name");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //set Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        if (!isEncrypted())
            toolbar.setTitle(getString(R.string.share_to));
        else
            toolbar.setTitle(getString(R.string.forward_to));

        db = new DbHelper(mContext);
        contacts = new ArrayList<>();
        contacts = db.getAcceptedContactList();
        addGroupToList();
        initViews();
        setAdapter();
    }

    private void addGroupToList() {
        ArrayList<ChatListEntity> groupList = db.getGroupChatList();
        if (groupList.size() > 0) {
            for (int i = 0; i < groupList.size(); i++) {
                if (db.getMember(groupList.get(i).getUserDbId(), User_settings.getECCID(mContext)) > 0) {
                    ContactEntity entity = new ContactEntity();
                    entity.setIsGroup(AppConstants.GROUP_CHAT_TYPE);
                    entity.setUserDbId(groupList.get(i).getUserDbId());
                    entity.setName(groupList.get(i).getName());
                    contacts.add(entity);
                }
            }
        }
    }


    private void initViews() {
        fab.setVisibility(View.GONE);
        ForwardMessageAdapter.checkLists = new boolean[contacts.size()];
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(mContext, DividerItemDecoration.VERTICAL, 0));
    }


    private void setAdapter() {
        mAdapter = new ForwardMessageAdapter(mContext, contacts, this);
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


    @Override
    public void onItemClick(ContactEntity contactEntity, int position) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(mContext, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(mContext);
        }
        builder.setTitle("Forward");
        if(contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE)
            builder.setMessage("are you sure that you want to forward this message to this contact : ("+contactEntity.getEccId()+")");
        else
            builder.setMessage("are you sure that you want to forward this message to this group : ("+contactEntity.getName()+")");
        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
            clickRow(contactEntity);
            dialogInterface.cancel();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();



    }
    public void clickRow(ContactEntity contactEntity){
        if (clickEvent) {
            clickEvent = false;
  this.contactEntity = contactEntity;
            if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                if (isMyFriend(contactEntity)) {
                    sendMessageOffline(mCount);
                } else {
                    clickEvent = true;
                    CommonUtils.showInfoMsg(mContext, getString(R.string.s_not_in_your_friend_list_to_send_message_you_have_to_add_this_contact_to_in_you_contact_list, contactEntity.getEccId()));
                }
            } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                if (isGroupMember(contactEntity.getUserDbId(), User_settings.getECCID(this))) {
                    groupMemberList = db.getGroupMemberList(contactEntity.getUserDbId());
                    sendMessageOffline(mCount);
                } else {
                    clickEvent = true;
                    CommonUtils.showInfoMsg(this, getString(R.string.you_are_no_longer_member_of_the_group));
                }

            }
        } else {
            clickEvent = true;
        }
    }

    private boolean isMyFriend(ContactEntity contactEntity) {
        return db.checkUserHasFriend(contactEntity.getEccId());
    }

    private boolean isGroupMember(int userDbId, String eccid) {
        return db.checkGroupMember(userDbId, eccid);
    }

    private void sendMessage(int mCount) {

        if (chatMessageEntityArrayList.size() > mCount) {
            ChatMessageEntity chatMessageEntity = chatMessageEntityArrayList.get(mCount);
            if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_TEXT) {
                if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                    sendTextMessage(contactEntity, chatMessageEntity.getMessage());
                } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                    sendTextMessageGroup(contactEntity, chatMessageEntity.getMessage());
                }
            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_AUDIO) {
              fileName = new VaultFileSaveUtils(db, AppConstants.ITEM_TYPE_PICTURE).getFileName(chatMessageEntity.getAudioPath().substring(chatMessageEntity.getAudioPath().lastIndexOf("/") + 1));

                String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getAudioPath());
                try {
                    decryptedFilePath = copy(new File(decryptedFilePath),  new File(CommonUtils.getAudioDirectory(this) + fileName + ".aac"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                    sendMultimediaMessage(chatMessageEntity, contactEntity, decryptedFilePath, AppConstants.MIME_TYPE_AUDIO);
                } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                    sendMultimediaMessage(chatMessageEntity, decryptedFilePath, AppConstants.MIME_TYPE_AUDIO);
                }
            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_CONTACT) {
                String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getContactPath());
                File baseDir = new File(CommonUtils.getKeyBasePath(mContext) + "Contacts");
                if (!baseDir.exists())
                    baseDir.mkdirs();
                File vcfFile = new File(baseDir + File.separator + "Contact " + System.currentTimeMillis() + ".vcf");
                decryptedFilePath = new File(decryptedFilePath).getAbsolutePath();
                try {
                    decryptedFilePath = copy(new File(decryptedFilePath), vcfFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                    sendMultimediaMessage(chatMessageEntity, contactEntity, decryptedFilePath, AppConstants.MIME_TYPE_CONTACT);
                } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                    sendMultimediaMessage(chatMessageEntity, decryptedFilePath, AppConstants.MIME_TYPE_CONTACT);
                }


            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_IMAGE) {
                fileName = new VaultFileSaveUtils(db, AppConstants.ITEM_TYPE_PICTURE).getFileName(chatMessageEntity.getImagePath().substring(chatMessageEntity.getImagePath().lastIndexOf("/") + 1));

                String file;
                if (isEncrypted()) {
                    file = Cryptography.decryptFile(mContext, chatMessageEntity.getImagePath());
                    try {
                        file = copy(new File(file), new File(CommonUtils.getImageDirectory(this) + fileName + ".jpg"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    file = chatMessageEntity.getImagePath();
                }
                if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                    sendMultimediaMessage(chatMessageEntity, contactEntity, file, AppConstants.MIME_TYPE_IMAGE);
                } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                    sendMultimediaMessage(chatMessageEntity, file, AppConstants.MIME_TYPE_IMAGE);
                }

            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_NOTE) {
                String file;
                if (isEncrypted()) {
                    file = Cryptography.decryptFile(mContext, chatMessageEntity.getFilePath());
                    String text = readEncodedFile(file);
                    if (text.contains("!@#$%^")) {
                        fileName = text.substring(0, text.indexOf("!"));
                    } else {
                        fileName = "Note " + System.currentTimeMillis();
                }
                    VaultFileSaveUtils fileSaveUtils = new VaultFileSaveUtils(db, AppConstants.ITEM_TYPE_NOTES);
                    fileName = fileSaveUtils.getFileName(fileName);
                    try {
                        file = copy(new File(file), new File(CommonUtils.getNotesDirectory(this), fileName + ".txt"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    file = chatMessageEntity.getFilePath();
                }
                if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                    sendMultimediaMessage(chatMessageEntity, contactEntity, file, AppConstants.MIME_TYPE_NOTE);
                } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                    sendMultimediaMessage(chatMessageEntity, file, AppConstants.MIME_TYPE_NOTE);
                }
            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_VIDEO) {

            }

        } else {
            finish();
        }

    }

    private String getAudioFilename() {
        File audioFile = new File(CommonUtils.getKeyBasePath(mContext) + "Audio");
        if (!audioFile.exists()) {
            audioFile.mkdirs();
        }

        return (audioFile.getAbsolutePath() + "/" + System.currentTimeMillis() + ".aac");
    }

    public String readEncodedFile(String path) {
        File file = new File(path);

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return text.toString();
    }

    @Override
    public void onItemLongPress(ContactEntity contactEntity, int position) {

    }
    private void sendTextMessage(ContactEntity contactEntity, String messageText) {

        ChatListEntity chatListEntity;

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
            chatListEntity = chatEntity;
        } else {
            chatListEntity = db.getChatEntity(contactEntity.getEccId());
        }

        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setSenderId(Integer.parseInt(User_settings.getUserId(mContext)));
        chatMessageEntity.setMessage(messageText);
        chatMessageEntity.setMessageMimeType(AppConstants.MIME_TYPE_TEXT);
        chatMessageEntity.setMessageId(String.valueOf(System.currentTimeMillis()));
        chatMessageEntity.setChatId(chatListEntity.getId());
        chatMessageEntity.setReceiverId(chatListEntity.getUserDbId());
        chatMessageEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
        chatMessageEntity.setMessageBurnTime(chatListEntity.getBurnTime());
        chatMessageEntity.setChatUserDbId(chatMessageEntity.getChatUserDbId());
        if (NetworkUtils.isNetworkConnected(mContext)) {
            if (db.checkPublicKeysOfUser(chatListEntity.getUserDbId())) {
                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
                    chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
                    SocketUtils.sendNewMessageToSocket(mContext, chatListEntity, chatMessageEntity);
                } else {
                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                    chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
                }
                db.insertChatMessage(chatMessageEntity);

            } else {
                searchPublicKeys(contactEntity);
            }
        } else {
            CommonUtils.showErrorMsg(mContext, getString(R.string.no_internet_connection));
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
            chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
            db.insertChatMessage(chatMessageEntity);
        }
        db.updateChatListTimeStamp(chatListEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());
        mCount++;
        sendMessage(mCount);
    }

    private void sendTextMessageGroup(ContactEntity contactEntity, String messageText) {
        ChatListEntity chatListEntity = db.getChatEntity(contactEntity.getUserDbId());
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setSenderId(Integer.parseInt(User_settings.getUserId(mContext)));
        chatMessageEntity.setMessage(messageText);
        chatMessageEntity.setMessageMimeType(AppConstants.MIME_TYPE_TEXT);
        chatMessageEntity.setMessageId(String.valueOf(System.currentTimeMillis()));
        chatMessageEntity.setChatId(chatListEntity.getId());
        chatMessageEntity.setReceiverId(chatListEntity.getUserDbId());
        chatMessageEntity.setName(User_settings.getScreenName(mContext));
        chatMessageEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
        chatMessageEntity.setMessageBurnTime(chatListEntity.getBurnTime());
        chatMessageEntity.setChatUserDbId(chatMessageEntity.getChatUserDbId());
        chatMessageEntity.setChatType(AppConstants.GROUP_CHAT_TYPE);
        chatMessageEntity.setMessageType(AppConstants.MESSAGE_TYPE_FROM);
        chatMessageEntity.setEddId(User_settings.getECCID(mContext));
        if (NetworkUtils.isNetworkConnected(mContext)) {
            if (checkGroupMembersKey()) {
                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
                    SocketUtils.sendGroupMessageToSocket("",mContext, chatListEntity, chatMessageEntity, groupMemberList);
                } else {
                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                }
                chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
                db.insertChatMessage(chatMessageEntity);
            } else {
                searchPublicKeys(chatMessageEntity, contactEntity);
            }
        } else {
            CommonUtils.showErrorMsg(mContext, getString(R.string.no_internet_connection));
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
            chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
            db.insertChatMessage(chatMessageEntity);
        }
        db.updateChatListTimeStamp(chatListEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());
        mCount++;
        sendMessage(mCount);
    }


    private void sendMultimediaMessage(ChatMessageEntity chatMessageEntity, ContactEntity contactEntity, String filePath, int fileMimeType) {
        try {
            if (db.checkPublicKeysOfUser(contactEntity.getUserDbId())) {
                String encryptedFilePath = "";
                if (fileMimeType == AppConstants.MIME_TYPE_AUDIO) {
                    encryptedFilePath = Cryptography.encryptFile(mContext, filePath, contactEntity.getUserDbId(), contactEntity.getEccId(), AppConstants.MIME_TYPE_AUDIO);
                } else if (fileMimeType == AppConstants.MIME_TYPE_CONTACT) {
                    encryptedFilePath = Cryptography.encryptFile(mContext, filePath, contactEntity.getUserDbId(), contactEntity.getEccId(), AppConstants.MIME_TYPE_CONTACT);
                } else if (fileMimeType == AppConstants.MIME_TYPE_IMAGE) {
                    encryptedFilePath = Cryptography.encryptFile(mContext, filePath, contactEntity.getUserDbId(), contactEntity.getEccId(), AppConstants.MIME_TYPE_IMAGE);
                } else if (fileMimeType == AppConstants.MIME_TYPE_NOTE) {
                    encryptedFilePath = Cryptography.encryptFile(mContext, filePath, contactEntity.getUserDbId(), contactEntity.getEccId(), AppConstants.MIME_TYPE_NOTE);
                } else if (fileMimeType == AppConstants.MIME_TYPE_VIDEO) {
                    encryptedFilePath = Cryptography.encryptFile(mContext, filePath, contactEntity.getUserDbId(), contactEntity.getEccId(), AppConstants.MIME_TYPE_VIDEO);
                }
                if (encryptedFilePath.length() > 0)
                    sendFilesToServerAndSocket(chatMessageEntity, contactEntity, encryptedFilePath, "POST", ApiEndPoints.URL_UPLOADING_MULTIMEDIA_SINGLE, "1234156", fileMimeType);
            } else {
                if (NetworkUtils.isNetworkConnected(mContext)) {
                    searchPublicKeys(contactEntity);
                } else {
                    CommonUtils.showErrorMsg(mContext, getString(R.string.no_internet_connection));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendMultimediaMessage(ChatMessageEntity chatMessageEntity, String filePath, int fileMimeType) {
        try {
            if (checkGroupMembersKey()) {
                String encryptedFilePath = "";
                if (fileMimeType == AppConstants.MIME_TYPE_AUDIO) {
                    encryptedFilePath = Cryptography.encryptFileGroup(mContext, filePath, groupMemberList, AppConstants.MIME_TYPE_AUDIO);
                } else if (fileMimeType == AppConstants.MIME_TYPE_CONTACT) {
                    encryptedFilePath = Cryptography.encryptFileGroup(mContext, filePath, groupMemberList, AppConstants.MIME_TYPE_CONTACT);
                } else if (fileMimeType == AppConstants.MIME_TYPE_IMAGE) {
                    encryptedFilePath = Cryptography.encryptFileGroup(mContext, filePath, groupMemberList, AppConstants.MIME_TYPE_IMAGE);
                } else if (fileMimeType == AppConstants.MIME_TYPE_NOTE) {
                    encryptedFilePath = Cryptography.encryptFileGroup(mContext, filePath, groupMemberList, AppConstants.MIME_TYPE_NOTE);
                } else if (fileMimeType == AppConstants.MIME_TYPE_VIDEO) {
                    encryptedFilePath = Cryptography.encryptFileGroup(mContext, filePath, groupMemberList, AppConstants.MIME_TYPE_VIDEO);
                }
                if (encryptedFilePath.length() > 0)
                    sendFilesToServerAndSocket(chatMessageEntity, encryptedFilePath, "POST", ApiEndPoints.URL_UPLOADING_MULTIMEDIA_GROUP, UUID.randomUUID().toString(), fileMimeType);
            } else {
                if (NetworkUtils.isNetworkConnected(mContext)) {
                    searchPublicKeys();
                } else {
                    CommonUtils.showErrorMsg(mContext, getString(R.string.no_internet_connection));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void searchPublicKeys() {
        AndroidNetworking.post(ApiEndPoints.URL_FETCH_GROUP_ECC_KEYS)
                .addJSONObjectBody(getRawData(contactEntity.getUserDbId()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            new PublicKeysParser().parseJson(mActivity, response.toString(), groupMemberList);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        CommonUtils.showInfoMsg(mContext, getString(R.string.user_s_public_key_not_found));
                    }
                });
    }

    public void sendFilesToServerAndSocket(ChatMessageEntity chatMessageEntity, String encryptedFilePath, String httpMethod, String serverUrl, String uploadId, int fileMimeType) {
        try {

            Ion.with(mContext)
                    .load(serverUrl)
                    .setMultipartParameter("json_data", getMultimediaJSONParameter(fileMimeType))
                    .setMultipartFile("user_files", new File(encryptedFilePath))
                    .asJsonObject()
                    .setCallback((e, result) -> {
                        try {
                            JSONObject rootObject = new JSONObject(result.toString());

                            String url = rootObject.getString("url");
                            if (fileMimeType == AppConstants.MIME_TYPE_IMAGE && !isEncrypted()) {
                                url = url + AppConstants.EXTRA_HIND + chatMessageEntity.getFileName();
                            }
                            sendMultimediaMessageToSocket(fileMimeType, encryptedFilePath, url);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                            CommonUtils.showErrorMsg(mContext, getString(R.string.file_not_sent));
                            mCount++;
                            sendMessage(mCount);
                        }
                    });


        } catch (Exception exc) {
            Log.e(TAG, "onDone: " + "fail");
            CommonUtils.showErrorMsg(mContext, getString(R.string.file_not_sent));
            mCount++;
            sendMessage(mCount);
        }
    }

    public String getMultimediaJSONParameter(int mimeType) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("sender_id", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObj.put("group_id", contactEntity.getUserDbId());
            if (mimeType == AppConstants.MIME_TYPE_AUDIO) {
                jsonObj.put("mime_type", "Audio");
            } else if (mimeType == AppConstants.MIME_TYPE_CONTACT) {
                jsonObj.put("mime_type", "Image");
            } else if (mimeType == AppConstants.MIME_TYPE_IMAGE) {
                jsonObj.put("mime_type", "Image");
            } else if (mimeType == AppConstants.MIME_TYPE_NOTE) {
                jsonObj.put("mime_type", "Image");
            } else if (mimeType == AppConstants.MIME_TYPE_VIDEO) {
                jsonObj.put("mime_type", " Video");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObj.toString();
    }

    private void sendMultimediaMessageToSocket(int mimeType, String filePath, String fileUrl) {
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setChatId(db.getChatEntity(contactEntity.getUserDbId()).getId());
        chatMessageEntity.setName(User_settings.getScreenName(mContext));
        chatMessageEntity.setMessageType(AppConstants.MESSAGE_TYPE_FROM);
        chatMessageEntity.setSenderId(Integer.parseInt(User_settings.getUserId(mContext)));
        chatMessageEntity.setMessageMimeType(mimeType);
        chatMessageEntity.setMessageId(String.valueOf(System.currentTimeMillis()));
        chatMessageEntity.setReceiverId(contactEntity.getUserDbId());
        chatMessageEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
        chatMessageEntity.setMessageBurnTime(db.getChatEntity(contactEntity.getUserDbId()).getBurnTime());
        chatMessageEntity.setChatUserDbId(chatMessageEntity.getChatUserDbId());
        chatMessageEntity.setChatType(AppConstants.GROUP_CHAT_TYPE);
        chatMessageEntity.setMessage(fileUrl);
        chatMessageEntity.setEddId(User_settings.getECCID(mContext));
        chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, db.getChatEntity(contactEntity.getUserDbId()).getBurnTime()));
        if (mimeType == AppConstants.MIME_TYPE_AUDIO) {
            chatMessageEntity.setAudioPath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_VIDEO) {
            chatMessageEntity.setVideoPath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_NOTE) {
            chatMessageEntity.setFilePath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_IMAGE) {
            chatMessageEntity.setImagePath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_CONTACT) {
            chatMessageEntity.setContactPath(filePath);
        }
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
            SocketUtils.sendGroupMessageToSocket("",mContext, db.getChatEntity(contactEntity.getUserDbId()), chatMessageEntity, groupMemberList);
        } else {
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
        }

        db.insertChatMessage(chatMessageEntity);
        db.updateChatListTimeStamp(contactEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());
        mCount++;
        sendMessage(mCount);

    }

    public void sendFilesToServerAndSocket(ChatMessageEntity chatMessageEntity, ContactEntity contactEntity, String encryptedFilePath, String httpMethod, String serverUrl, String uploadId, int fileMimeType) {
        try {

            Ion.with(mContext)
                    .load(serverUrl)
                    .setMultipartParameter("json_data", getMultimediaJSONParameter(fileMimeType, contactEntity.getUserDbId()))
                    .setMultipartFile("user_files", new File(encryptedFilePath))
                    .asJsonObject()
                    .setCallback((e, result) -> {
                        if (e != null) {
                            Toast.makeText(mContext, getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            JSONObject rootObject = new JSONObject(result.toString());
                            String url = rootObject.getString("url");
                            if (fileMimeType == AppConstants.MIME_TYPE_IMAGE && !isEncrypted()) {
                                url = url + AppConstants.EXTRA_HIND + chatMessageEntity.getFileName();
                            }
                            sendMultimediaMessageToSocket(contactEntity, fileMimeType, encryptedFilePath, url);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                            mCount++;
                            sendMessage(mCount);
                        }
                    });


        } catch (Exception exc) {
            Log.e(TAG, "onDone: " + "fail");
            mCount++;
            sendMessage(mCount);
        }
    }


    public void searchPublicKeys(ContactEntity contactEntity) {
        AndroidNetworking.post(ApiEndPoints.URL_FETCH_ECC_KEYS)
                .addBodyParameter("email", CommonUtils.getUserEmail(contactEntity.getEccId()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(response.toString());

                            if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                String publicKey = rootObject.getString("result_data");
                                PublicKeyEntity keyEntity = new PublicKeyEntity();
                                keyEntity.setUserDbId(contactEntity.getUserDbId());
                                keyEntity.setEccId(contactEntity.getEccId());
                                keyEntity.setUserType(AppConstants.SINGLE_CHAT_TYPE);
                                keyEntity.setEccPublicKey(publicKey);
                                keyEntity.setName(contactEntity.getName());
                                db.insertPublicKey(keyEntity);
                            } else {
                                CommonUtils.showInfoMsg(mContext, rootObject.getString("msg"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        CommonUtils.showInfoMsg(mContext, getString(R.string.user_s_public_key_not_found));

                    }
                });


    }


    private void sendMultimediaMessageToSocket(ContactEntity contactEntity, int mimeType, String
            filePath, String fileUrl) {
        ChatListEntity chatListEntity;
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
            chatListEntity = chatEntity;
        } else {
            chatListEntity = db.getChatEntity(contactEntity.getEccId());
        }
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setSenderId(Integer.parseInt(User_settings.getUserId(mContext)));
        chatMessageEntity.setMessageMimeType(mimeType);
        chatMessageEntity.setMessageId(String.valueOf(System.currentTimeMillis()));
        chatMessageEntity.setChatId(chatListEntity.getId());
        chatMessageEntity.setReceiverId(contactEntity.getUserDbId());
        chatMessageEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
        chatMessageEntity.setMessageBurnTime(chatListEntity.getBurnTime());
        chatMessageEntity.setChatUserDbId(chatMessageEntity.getChatUserDbId());
        chatMessageEntity.setMessage(fileUrl);
        String newFilePath = filePath;
        if (mimeType == AppConstants.MIME_TYPE_AUDIO) {
            chatMessageEntity.setAudioPath(newFilePath);
        } else if (mimeType == AppConstants.MIME_TYPE_VIDEO) {
            chatMessageEntity.setVideoPath(newFilePath);
        } else if (mimeType == AppConstants.MIME_TYPE_NOTE) {
            chatMessageEntity.setFilePath(newFilePath);
        } else if (mimeType == AppConstants.MIME_TYPE_IMAGE) {
            chatMessageEntity.setImagePath(newFilePath);
        } else if (mimeType == AppConstants.MIME_TYPE_CONTACT) {
            chatMessageEntity.setContactPath(newFilePath);
        }
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
            chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
            SocketUtils.sendNewMessageToSocket(mContext, chatListEntity, chatMessageEntity);
        } else {
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
            chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
        }

        db.insertChatMessage(chatMessageEntity);
        db.updateChatListTimeStamp(chatListEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());
        mCount++;
        sendMessage(mCount);
    }


    public String getMultimediaJSONParameter(int mimeType, int userDbId) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("sender_id", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObj.put("receiver_id", userDbId);
            if (mimeType == AppConstants.MIME_TYPE_AUDIO) {
                jsonObj.put("mime_type", "Audio");
            } else if (mimeType == AppConstants.MIME_TYPE_CONTACT) {
                jsonObj.put("mime_type", "Image");
            } else if (mimeType == AppConstants.MIME_TYPE_IMAGE) {
                jsonObj.put("mime_type", "Image");
            } else if (mimeType == AppConstants.MIME_TYPE_NOTE) {
                jsonObj.put("mime_type", "Image");
            } else if (mimeType == AppConstants.MIME_TYPE_VIDEO) {
                jsonObj.put("mime_type", " Video");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObj.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onPause() {
        super.onPause();
        AppConstants.isbackground = true;
        if (AppConstants.lockscreen) {
            CommonUtils.checkDialog(mActivity);
        }
        HomeActivity.runnable = new Runnable() {
            @Override
            public void run() {
                if (AppConstants.isbackground) {
                    Log.e("Tag", "onPause: " + "background-010");
                    CommonUtils.lockDialog(mActivity);

                } else {
                    Log.e("Tag", "onPause: " + "forground-010");
                }


            }
        };
        HomeActivity.lockHandler.postDelayed(HomeActivity.runnable, User_settings.getLockTime(mContext));
    }

    @Override
    protected void onResume() {
        super.onResume();
        HomeActivity.lockHandler.removeCallbacks(HomeActivity.runnable);
        AppConstants.isbackground = false;

    }

    private boolean checkGroupMembersKey() {
        boolean keysFound = false;
        for (int i = 0; i < groupMemberList.size(); i++) {
            synchronized (this) {
                if (!(db.checkPublicKeysOfUser(groupMemberList.get(i).getUserDbId()))) {
                    keysFound = false;
                    break;
                } else {
                    keysFound = true;
                }

            }
        }
        return keysFound;
    }

    public void searchPublicKeys(ChatMessageEntity chatMessageEntity, ContactEntity
            contactEntity) {
        AndroidNetworking.post(ApiEndPoints.URL_FETCH_GROUP_ECC_KEYS)
                .addJSONObjectBody(getRawData(contactEntity.getUserDbId()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            new PublicKeysParser().parseJson(mActivity, response.toString(), groupMemberList);

                            if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                                chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
                                SocketUtils.sendGroupMessageToSocket("",mContext, db.getChatEntity(contactEntity.getUserDbId()), chatMessageEntity, groupMemberList);
                            } else {
                                chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                            }
                            chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, db.getChatEntity(contactEntity.getUserDbId()).getBurnTime()));
                            db.insertChatMessage(chatMessageEntity);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        CommonUtils.showInfoMsg(mContext, getString(R.string.user_s_public_key_not_found));
                        chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                        chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, db.getChatEntity(contactEntity.getUserDbId()).getBurnTime()));
                        db.insertChatMessage(chatMessageEntity);
                    }
                });
    }

    public JSONObject getRawData(int groupID) {
        List<String> eccId = getMembersECCID(groupID);
        JSONObject jsonObject = new JSONObject();
        try {
            String json;

            JSONArray eccIdArray = new JSONArray();
            if (eccId.size() > 0) {
                for (int i = 0; i < eccId.size(); i++) {
                    JSONObject singleECCIdObject = new JSONObject();
                    String eccID = eccId.get(i);
                    singleECCIdObject.put("ecc_id", eccID);
                    eccIdArray.put(singleECCIdObject);
                }
            }
            jsonObject.put("ecc_data", eccIdArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public List<String> getMembersECCID(int groupId) {
        ArrayList<GroupMemberEntity> mList = db.getGroupMemberList(groupId);
        List<String> eccId = new ArrayList<>();
        int size = mList.size();
        for (int i = 0; i < size; i++) {
            if (!db.checkPublicKeysOfUser(mList.get(i).getUserDbId())) {
                eccId.add(mList.get(i).getEccId());
            }
        }
        return eccId;
    }

    public static String copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
        return dst.getAbsolutePath();
    }

    private void sendTextMessageOffline(ContactEntity contactEntity, String messageText) {

        ChatListEntity chatListEntity;

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

            chatListEntity = chatEntity;

        } else {
            chatListEntity = db.getChatEntity(contactEntity.getEccId());
        }

        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setSenderId(Integer.parseInt(User_settings.getUserId(mContext)));
        chatMessageEntity.setMessage(messageText);
        chatMessageEntity.setMessageMimeType(AppConstants.MIME_TYPE_TEXT);
        chatMessageEntity.setMessageId(String.valueOf(System.currentTimeMillis()));
        chatMessageEntity.setChatId(chatListEntity.getId());
        chatMessageEntity.setReceiverId(chatListEntity.getUserDbId());
        chatMessageEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
        chatMessageEntity.setMessageBurnTime(chatListEntity.getBurnTime());
        chatMessageEntity.setChatUserDbId(chatMessageEntity.getChatUserDbId());
        if (NetworkUtils.isNetworkConnected(mContext)) {
            if (db.checkPublicKeysOfUser(chatListEntity.getUserDbId())) {
                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
                    chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
                    SocketUtils.sendNewMessageToSocket(mContext, chatListEntity, chatMessageEntity);
                } else {
                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                    chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
                }
                db.insertChatMessage(chatMessageEntity);

            } else {
               // mProgressDialoge = CommonUtils.showLoadingDialog(mContext);
                searchPublicKeys(contactEntity);
            }
        } else {
           // CommonUtils.showErrorMsg(mContext, getString(R.string.no_internet_connection));
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
            chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));

            db.insertChatMessage(chatMessageEntity);

        }

        db.updateChatListTimeStamp(chatListEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());
        mCount++;
        sendMessageOffline(mCount);
    }

    private void sendTextMessageGroupOffline(ContactEntity contactEntity, String messageText) {

        ChatListEntity chatListEntity = db.getChatEntity(contactEntity.getUserDbId());
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setSenderId(Integer.parseInt(User_settings.getUserId(mContext)));
        chatMessageEntity.setMessage(messageText);
        chatMessageEntity.setMessageMimeType(AppConstants.MIME_TYPE_TEXT);
        chatMessageEntity.setMessageId(String.valueOf(System.currentTimeMillis()));
        chatMessageEntity.setChatId(chatListEntity.getId());
        chatMessageEntity.setReceiverId(chatListEntity.getUserDbId());
        chatMessageEntity.setName(User_settings.getScreenName(mContext));
        chatMessageEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
        chatMessageEntity.setMessageBurnTime(chatListEntity.getBurnTime());
        chatMessageEntity.setChatUserDbId(chatMessageEntity.getChatUserDbId());
        chatMessageEntity.setChatType(AppConstants.GROUP_CHAT_TYPE);
        chatMessageEntity.setMessageType(AppConstants.MESSAGE_TYPE_FROM);
        chatMessageEntity.setEddId(User_settings.getECCID(mContext));
        if (NetworkUtils.isNetworkConnected(mContext)) {
            if (checkGroupMembersKey()) {
                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
                    SocketUtils.sendGroupMessageToSocket("",mContext, chatListEntity, chatMessageEntity, groupMemberList);
                } else {
                    chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                }
                chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
                db.insertChatMessage(chatMessageEntity);
            } else {
                searchPublicKeys(chatMessageEntity, contactEntity);
            }
        } else {

            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
            chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
            db.insertChatMessage(chatMessageEntity);
        }
        db.updateChatListTimeStamp(chatListEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());
        mCount++;
        sendMessageOffline(mCount);
    }
    private void sendMessageOffline(int mCount) {

        if (chatMessageEntityArrayList.size() > mCount) {
            ChatMessageEntity chatMessageEntity = chatMessageEntityArrayList.get(mCount);
            if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_TEXT) {
                if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                    sendTextMessageOffline(contactEntity, chatMessageEntity.getMessage());
                } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                    sendTextMessageGroupOffline(contactEntity, chatMessageEntity.getMessage());
                }
            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_AUDIO) {
                if (chatMessageEntity.getAudioPath().length() > 0){
                    fileName = new VaultFileSaveUtils(db, AppConstants.ITEM_TYPE_PICTURE).getFileName(chatMessageEntity.getAudioPath().substring(chatMessageEntity.getAudioPath().lastIndexOf("/") + 1));

                    String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getAudioPath());
                    try {
                        decryptedFilePath = copy(new File(decryptedFilePath),  new File(CommonUtils.getAudioDirectory(this) + fileName + ".aac"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                        sendMultimediaMessageToSocketOffline(contactEntity, AppConstants.MIME_TYPE_AUDIO, "", decryptedFilePath);
                    } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                        sendMultimediaMessageToSocketOfflineGroup(AppConstants.MIME_TYPE_AUDIO, "", decryptedFilePath);
                    }

            } else {
                    if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                        sendMultimediaMessageToSocketOffline(contactEntity, AppConstants.MIME_TYPE_AUDIO, "", chatMessageEntity.getMessage());
                    } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                        sendMultimediaMessageToSocketOfflineGroup(AppConstants.MIME_TYPE_AUDIO, "", chatMessageEntity.getMessage());

                    }
            }


            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_CONTACT) {
                if (chatMessageEntity.getContactPath().length() > 0) {
                    String decryptedFilePath = Cryptography.decryptFile(mContext, chatMessageEntity.getContactPath());
                    File baseDir = new File(CommonUtils.getKeyBasePath(mContext) + "Contacts");
                    if (!baseDir.exists())
                        baseDir.mkdirs();
                    File vcfFile = new File(baseDir + File.separator + "Contact " + System.currentTimeMillis() + ".vcf");
                    decryptedFilePath = new File(decryptedFilePath).getAbsolutePath();
                    try {
                        decryptedFilePath = copy(new File(decryptedFilePath), vcfFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                           sendMultimediaMessageToSocketOffline(contactEntity, AppConstants.MIME_TYPE_CONTACT, "", chatMessageEntity.getContactPath());
                    } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                           sendMultimediaMessageToSocketOfflineGroup(AppConstants.MIME_TYPE_CONTACT, "", chatMessageEntity.getContactPath());

                    }
                } else {
                    if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                             sendMultimediaMessageToSocketOffline(contactEntity, AppConstants.MIME_TYPE_CONTACT, "", chatMessageEntity.getMessage());
                    } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                             sendMultimediaMessageToSocketOfflineGroup(AppConstants.MIME_TYPE_CONTACT, "", chatMessageEntity.getMessage());
                    }
                }


            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_IMAGE) {
                if (chatMessageEntity.getImagePath().length() > 0) {
                    String fname =  chatMessageEntity.getImagePath();
                    fileName = new VaultFileSaveUtils(db, AppConstants.ITEM_TYPE_PICTURE).getFileName(fname.substring(fname.lastIndexOf("/") + 1));
                    String file;
                    if (isEncrypted()) {
                        file = Cryptography.decryptFile(mContext, fname);
                        try {
                            file = copy(new File(file), new File(CommonUtils.getImageDirectory(this) + fileName + ".jpg"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        file = fname;
                    }

                    if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                        sendMultimediaMessageToSocketOffline(contactEntity, AppConstants.MIME_TYPE_IMAGE, "", file);

                    } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                        sendMultimediaMessageToSocketOfflineGroup(AppConstants.MIME_TYPE_IMAGE, "", file);
                    }
                } else {
                    if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                        sendMultimediaMessageToSocketOffline(contactEntity, AppConstants.MIME_TYPE_IMAGE, "", chatMessageEntity.getMessage());

                    } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                        sendMultimediaMessageToSocketOfflineGroup(AppConstants.MIME_TYPE_IMAGE, "", chatMessageEntity.getMessage());
                    }
                }
            } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_NOTE) {
                if (chatMessageEntity.getFilePath().length() > 0) {
                    String fname = chatMessageEntity.getFilePath();

                    String file;
                    if (isEncrypted()) {
                        file = Cryptography.decryptFile(mContext, fname);
                        String text = readEncodedFile(file);
                        if (text.contains("!@#$%^")) {
                            fileName = text.substring(0, text.indexOf("!"));
                        } else {
                            fileName = "Note " + System.currentTimeMillis();
                        }
                        VaultFileSaveUtils fileSaveUtils = new VaultFileSaveUtils(db, AppConstants.ITEM_TYPE_NOTES);
                        fileName = fileSaveUtils.getFileName(fileName);
                        try {
                            file = copy(new File(file), new File(CommonUtils.getNotesDirectory(this), fileName + ".txt"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        file = fname;
                    }

                    if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                        sendMultimediaMessageToSocketOffline(contactEntity, AppConstants.MIME_TYPE_NOTE, "", file);
                    } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                        sendMultimediaMessageToSocketOfflineGroup(AppConstants.MIME_TYPE_NOTE, "", file);
                    }
                } else {

                    String file  = chatMessageEntity.getMessage();
                    if (contactEntity.getIsGroup() == AppConstants.SINGLE_CHAT_TYPE) {
                        sendMultimediaMessageToSocketOffline(contactEntity, AppConstants.MIME_TYPE_NOTE, "", file);
                    } else if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                        sendMultimediaMessageToSocketOfflineGroup(AppConstants.MIME_TYPE_NOTE, "", file);
                    }
                }
            }
        } else {
            finish();
        }

    }
    private void sendMultimediaMessageToSocketOffline(ContactEntity contactEntity, int mimeType, String filePath, String fileUrl) {
        ChatListEntity chatListEntity;
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
            chatListEntity = chatEntity;
        } else {
            chatListEntity = db.getChatEntity(contactEntity.getEccId());
        }
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setSenderId(Integer.parseInt(User_settings.getUserId(mContext)));
        chatMessageEntity.setMessageMimeType(mimeType);
        chatMessageEntity.setMessageId(String.valueOf(System.currentTimeMillis()));
        chatMessageEntity.setChatId(chatListEntity.getId());
        chatMessageEntity.setReceiverId(contactEntity.getUserDbId());
        chatMessageEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
        chatMessageEntity.setMessageBurnTime(chatListEntity.getBurnTime());
        chatMessageEntity.setChatUserDbId(chatMessageEntity.getChatUserDbId());
        chatMessageEntity.setMessage(fileUrl);
        String newFilePath = filePath;
        if (mimeType == AppConstants.MIME_TYPE_AUDIO) {
            chatMessageEntity.setAudioPath(newFilePath);
        } else if (mimeType == AppConstants.MIME_TYPE_VIDEO) {
            chatMessageEntity.setVideoPath(newFilePath);
        } else if (mimeType == AppConstants.MIME_TYPE_NOTE) {
            chatMessageEntity.setFilePath(newFilePath);
        } else if (mimeType == AppConstants.MIME_TYPE_IMAGE) {
            chatMessageEntity.setImagePath(newFilePath);
        } else if (mimeType == AppConstants.MIME_TYPE_CONTACT) {
            chatMessageEntity.setContactPath(newFilePath);
        }
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
            chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, chatListEntity.getBurnTime()));
        db.insertChatMessage(chatMessageEntity);
        db.updateChatListTimeStamp(chatListEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());
        mCount++;
        sendMessageOffline(mCount);
    }

    private void sendMultimediaMessageToSocketOfflineGroup(int mimeType, String filePath, String fileUrl) {
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setChatId(db.getChatEntity(contactEntity.getUserDbId()).getId());
        chatMessageEntity.setName(User_settings.getScreenName(mContext));
        chatMessageEntity.setMessageType(AppConstants.MESSAGE_TYPE_FROM);
        chatMessageEntity.setSenderId(Integer.parseInt(User_settings.getUserId(mContext)));
        chatMessageEntity.setMessageMimeType(mimeType);
        chatMessageEntity.setMessageId(String.valueOf(System.currentTimeMillis()));
        chatMessageEntity.setReceiverId(contactEntity.getUserDbId());
        chatMessageEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
        chatMessageEntity.setMessageBurnTime(db.getChatEntity(contactEntity.getUserDbId()).getBurnTime());
        chatMessageEntity.setChatUserDbId(chatMessageEntity.getChatUserDbId());
        chatMessageEntity.setChatType(AppConstants.GROUP_CHAT_TYPE);
        chatMessageEntity.setMessage(fileUrl);
        chatMessageEntity.setEddId(User_settings.getECCID(mContext));
        chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, db.getChatEntity(contactEntity.getUserDbId()).getBurnTime()));
        if (mimeType == AppConstants.MIME_TYPE_AUDIO) {
            chatMessageEntity.setAudioPath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_VIDEO) {
            chatMessageEntity.setVideoPath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_NOTE) {
            chatMessageEntity.setFilePath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_IMAGE) {
            chatMessageEntity.setImagePath(filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_CONTACT) {
            chatMessageEntity.setContactPath(filePath);
        }
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
        db.insertChatMessage(chatMessageEntity);
        db.updateChatListTimeStamp(contactEntity.getUserDbId(), DateTimeUtils.getCurrentDateTimeString());
        mCount++;
        sendMessageOffline(mCount);

    }

}
