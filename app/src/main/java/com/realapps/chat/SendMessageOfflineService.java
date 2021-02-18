package com.realapps.chat;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.koushikdutta.ion.Ion;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.parser.PublicKeysParser;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.model.GroupMemberEntity;
import com.realapps.chat.model.PublicKeyEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.Cryptography;
import com.realapps.chat.utils.DateTimeUtils;
import com.realapps.chat.utils.NetworkUtils;
import com.realapps.chat.utils.SocketUtils;
import com.realapps.chat.view.home.adapters.ChatWindowAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



/**
 * Created by Hari Choudhary on 8/23/2019 at 11:07 AM .
 * Core techies
 * hari@coretechies.org
 */
public class SendMessageOfflineService extends Service {

    private final int TWO_SECONDS = 2000;
    Handler mHandler;
    boolean flag = false;
    int countLoop;
    int position;
    boolean isProcessRunning;
    private DbHelper dbHelper;
    private ChatWindowAdapter mAdapter;
    private String fileName = "";
    private ArrayList<ChatListEntity> chatList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        isProcessRunning = false;


        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        mHandler = new Handler();
        startRepeatingTask();


        return START_STICKY;
    }

    void startRepeatingTask() {
        countLoop=0;
        getUnsendMessages();
    }

    void stopRepeatingTask() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
       stopRepeatingTask();
    }

    public synchronized void getUnsendMessages() {
        if (!isProcessRunning) {

            isProcessRunning = true;
            if (NetworkUtils.isNetworkConnected(getApplicationContext())) {
                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                    Log.e("service2", "Running");
                    dbHelper = new DbHelper(getApplicationContext());
                    chatList = dbHelper.getChatListFromUndelivered();
                    if (chatList != null && chatList.size() > 0) {
                        //   for (int j = 0; j < chatList.size(); j++) {
                        if (chatList.get(0).getChatType() == AppConstants.SINGLE_CHAT_TYPE) {
                            singleChatUnsentMessages(chatList.get(0));
                        }
                        else {
                            groupChatUnsentMessages(chatList.get(0));
                        }
                    } else {
                        Log.e("service", "Stop");
                        stopSelf();
                    }
                    //  }
                } else {
                    isProcessRunning = false;
                }
            } else {
                isProcessRunning = false;
            }
        }


        reExecute();

    }

    public void reExecute(){
        if (!isProcessRunning) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        getUnsendMessages();
                    } finally {

                    }
                }
            }, 3000);
        }
    }
    public synchronized void singleChatUnsentMessages(ChatListEntity chatListEntity) {

        if(AppConstants.chatId!=0 && ((AppConstants.chatId == chatListEntity.getId()) && AppConstants.chatType == chatListEntity.getChatType())){
            isProcessRunning = false;
            reExecute();
        } else {
            ChatMessageEntity chatMessageEntity = dbHelper.getUndeliveredMessageList(chatListEntity.getId(), chatListEntity.getChatType());
            if (chatMessageEntity != null) {
                countLoop = 0;
                        if (chatMessageEntity.getMessageStatus() == AppConstants.MESSAGE_NOT_SENT_STATUS) {
                    if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_IMAGE && chatMessageEntity.getImagePath().length() == 0) {
                        sendMultimediaMessageOffline(chatListEntity, chatMessageEntity, chatMessageEntity.getMessageMimeType());

                    } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_NOTE && chatMessageEntity.getFilePath().length() == 0) {
                        sendMultimediaMessageOffline(chatListEntity, chatMessageEntity, chatMessageEntity.getMessageMimeType());


                    } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_AUDIO && chatMessageEntity.getAudioPath().length() == 0) {
                        sendMultimediaMessageOffline(chatListEntity, chatMessageEntity, chatMessageEntity.getMessageMimeType());


                    } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_CONTACT && chatMessageEntity.getContactPath().length() == 0) {
                        sendMultimediaMessageOffline(chatListEntity, chatMessageEntity, chatMessageEntity.getMessageMimeType());

                    } else {
                        chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
                        String messageBurnTime = DateTimeUtils.getMessageDestructionTimeByBurnTime(getApplicationContext(), chatMessageEntity.getMessageBurnTime());
                        chatMessageEntity.setMessageBurnTimeStamp(messageBurnTime);
                        SocketUtils.sendNewMessageToSocket(getApplicationContext(), chatListEntity, chatMessageEntity);
                        dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
                        dbHelper.updateMessageBurnDate(chatMessageEntity.getMessageId(), messageBurnTime);
                        singleChatUnsentMessages(chatListEntity);

                    }

                } else if (chatMessageEntity.getMessageStatus() == AppConstants.MESSAGE_IN_PROGRESS_STATUS) {
                    sendMultimediaMessageToSocketOfflineDirect(chatListEntity, chatMessageEntity);

                }

            } else {
                isProcessRunning = false;
                reExecute();
            }
        }


    }

    private synchronized void sendMultimediaMessageOffline(ChatListEntity chatListEntity,ChatMessageEntity chatMessageEntity, int fileMimeType) {

        try {
            if (dbHelper.checkPublicKeysOfUser(chatListEntity.getUserDbId())) {
                String encryptedFilePath = "";
                if (fileMimeType == AppConstants.MIME_TYPE_AUDIO) {
                    encryptedFilePath = Cryptography.encryptFile(getApplicationContext(), chatMessageEntity.getMessage(), chatListEntity.getUserDbId(), chatListEntity.getEccId(), AppConstants.MIME_TYPE_AUDIO);
                } else if (fileMimeType == AppConstants.MIME_TYPE_CONTACT) {
                    encryptedFilePath = Cryptography.encryptFile(getApplicationContext(), chatMessageEntity.getMessage(), chatListEntity.getUserDbId(), chatListEntity.getEccId(), AppConstants.MIME_TYPE_CONTACT);
                } else if (fileMimeType == AppConstants.MIME_TYPE_IMAGE) {
                    encryptedFilePath = Cryptography.encryptFile(getApplicationContext(), chatMessageEntity.getMessage(), chatListEntity.getUserDbId(), chatListEntity.getEccId(), AppConstants.MIME_TYPE_IMAGE);
                } else if (fileMimeType == AppConstants.MIME_TYPE_NOTE) {
                    encryptedFilePath = Cryptography.encryptFile(getApplicationContext(), chatMessageEntity.getMessage(), chatListEntity.getUserDbId(), chatListEntity.getEccId(), AppConstants.MIME_TYPE_NOTE);
                } else if (fileMimeType == AppConstants.MIME_TYPE_VIDEO) {
                    encryptedFilePath = Cryptography.encryptFile(getApplicationContext(), chatMessageEntity.getMessage(), chatListEntity.getUserDbId(), chatListEntity.getEccId(), AppConstants.MIME_TYPE_VIDEO);
                }
                if (encryptedFilePath.length() > 0)
                    sendFilesToServerAndSocketOffline(chatListEntity,chatMessageEntity,encryptedFilePath, "POST", ApiEndPoints.URL_UPLOADING_MULTIMEDIA_SINGLE, UUID.randomUUID().toString(), fileMimeType);

            } else {
                if (NetworkUtils.isNetworkConnected(getApplicationContext())) {
                    searchPublicKeys(chatListEntity,chatMessageEntity, fileMimeType);

                } else {


                }
            }
        } catch (Exception e) {
            singleChatUnsentMessages(chatListEntity);
        }

    }


    /////////////////////////////////////////////////////////////////GroupMsg/////////////////////////////////////////////////////////////






    private synchronized void sendMultimediaMessageOfflineForGroup(ChatListEntity chatListEntity,ChatMessageEntity chatMessageEntity, int fileMimeType, ArrayList<GroupMemberEntity> groupMemberList) {

        try {
            if (checkGroupMembersKey(groupMemberList) ) {
                String encryptedFilePath = "";
                if (fileMimeType == AppConstants.MIME_TYPE_AUDIO) {
                    encryptedFilePath = Cryptography.encryptFileGroup(getApplicationContext(), chatMessageEntity.getMessage(), groupMemberList, AppConstants.MIME_TYPE_AUDIO);
                } else if (fileMimeType == AppConstants.MIME_TYPE_CONTACT) {
                    encryptedFilePath = Cryptography.encryptFileGroup(getApplicationContext(), chatMessageEntity.getMessage(), groupMemberList, AppConstants.MIME_TYPE_CONTACT);
                } else if (fileMimeType == AppConstants.MIME_TYPE_IMAGE) {
                    encryptedFilePath = Cryptography.encryptFileGroup(getApplicationContext(), chatMessageEntity.getMessage(), groupMemberList, AppConstants.MIME_TYPE_IMAGE);
                } else if (fileMimeType == AppConstants.MIME_TYPE_NOTE) {
                    encryptedFilePath = Cryptography.encryptFileGroup(getApplicationContext(), chatMessageEntity.getMessage(), groupMemberList, AppConstants.MIME_TYPE_NOTE);
                } else if (fileMimeType == AppConstants.MIME_TYPE_VIDEO) {
                    encryptedFilePath = Cryptography.encryptFileGroup(getApplicationContext(), chatMessageEntity.getMessage(), groupMemberList, AppConstants.MIME_TYPE_VIDEO);
                }
                if (encryptedFilePath.length() > 0) {
                    sendFilesToServerAndSocketOfflineGroup(chatListEntity, chatMessageEntity,encryptedFilePath, "POST", ApiEndPoints.URL_UPLOADING_MULTIMEDIA_GROUP, UUID.randomUUID().toString(), fileMimeType,groupMemberList);

                }
            } else {
                if (NetworkUtils.isNetworkConnected(getApplicationContext())) {
                    searchPublicKeysForGroup(chatListEntity,chatMessageEntity, fileMimeType,groupMemberList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            groupChatUnsentMessages(chatListEntity);
        }

    }
///////////////////////////////////SearchPublicKeysForGroup//////////////////////////////////////////////
private  boolean checkGroupMembersKey(ArrayList<GroupMemberEntity> groupMemberList) {
    boolean keysFound = false;
    for (int i = 0; i < groupMemberList.size(); i++) {
        synchronized (this) {
            if (!(dbHelper.checkPublicKeysOfUser(groupMemberList.get(i).getUserDbId()))) {
                keysFound = false;
                break;
            } else {
                keysFound = true;
            }

        }
    }
    return keysFound;
}

    public synchronized void searchPublicKeysForGroup(ChatListEntity chatListEntity, ChatMessageEntity chatMessageEntity, int fileMimeType, ArrayList<GroupMemberEntity> groupMemberList) {
        AndroidNetworking.post(ApiEndPoints.URL_FETCH_GROUP_ECC_KEYS)
                .addJSONObjectBody(getRawData(chatListEntity.getUserDbId()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

//                        if (mProgressDialoge != null) {
//                            if (mProgressDialoge.isShowing())
//                                mProgressDialoge.dismiss();
//                        }

                        try {
                            new PublicKeysParser().parseJson(getApplicationContext(), response.toString(), groupMemberList);

                            if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                                chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
                                SocketUtils.sendGroupMessageToSocket("SendMsgFromService",getApplicationContext(), chatListEntity, chatMessageEntity, groupMemberList);
                            } else {
                                chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                            }
                            chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(getApplicationContext(), chatListEntity.getBurnTime()));

                            dbHelper.insertChatMessage(chatMessageEntity);

                          //  setMessageHintDestructionTime();


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {

                        chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_NOT_SENT_STATUS);
                        chatMessageEntity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(getApplicationContext(), chatListEntity.getBurnTime()));

                        dbHelper.insertChatMessage(chatMessageEntity);



                      //  setMessageHintDestructionTime();
                    }
                });
    }


////////////////////////////////////////////////////////////////////////////////////





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


//////////////////////////////////////////////////////////////////////////////////////////


    public List<String> getMembersECCID(int groupId) {
        ArrayList<GroupMemberEntity> mList = dbHelper.getGroupMemberList(groupId);
        List<String> eccId = new ArrayList<>();
        int size = mList.size();
        for (int i = 0; i < size; i++) {
            if (!dbHelper.checkPublicKeysOfUser(mList.get(i).getUserDbId())) {
                eccId.add(mList.get(i).getEccId());
            }
        }
        return eccId;
    }



///////////////////////////////////////////////////////////////////////////////////////////



    public synchronized void searchPublicKeys(ChatListEntity chatListEntity,ChatMessageEntity chatMessageEntity, int fileMimeType) {
        AndroidNetworking.post(ApiEndPoints.URL_FETCH_ECC_KEYS)
                .addBodyParameter("email", CommonUtils.getUserEmail(chatListEntity.getEccId()))
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
                                keyEntity.setUserDbId(chatListEntity.getUserDbId());
                                keyEntity.setEccId(chatListEntity.getEccId());
                                keyEntity.setUserType(chatListEntity.getChatType());
                                keyEntity.setEccPublicKey(publicKey);
                                keyEntity.setName(chatListEntity.getName());
                                dbHelper.insertPublicKey(keyEntity);
                                sendMultimediaMessageOffline(chatListEntity,chatMessageEntity, fileMimeType);



                            } else {
                               // CommonUtils.showInfoMsg(mContext, rootObject.getString("msg"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        singleChatUnsentMessages(chatListEntity);
                      //  CommonUtils.showInfoMsg(mContext, getString(R.string.user_s_public_key_not_found));

                    }
                });


    }
    public synchronized void sendFilesToServerAndSocketOffline(ChatListEntity chatListEntity,ChatMessageEntity chatMessageEntity,String encryptedFilePath, String httpMethod, String serverUrl, String uploadId, int fileMimeType) {
        if (NetworkUtils.isNetworkConnected(getApplicationContext())) {
            try {
                Ion.with(getApplicationContext())
                        .load(serverUrl)
                        .setMultipartParameter("json_data", getMultimediaJSONParameter(chatListEntity,fileMimeType))
                        .setMultipartFile("user_files", new File(encryptedFilePath))
                        .asJsonObject()
                        .setCallback((e, result) -> {


                            if (e != null) {
                                e.printStackTrace();
                                flag=false;

                            } else {
                                try {
                                    JSONObject rootObject = new JSONObject(result.toString());
                                    String url = rootObject.getString("url");
                                    if (fileMimeType == AppConstants.MIME_TYPE_IMAGE) {
                                        url = url + AppConstants.EXTRA_HIND + fileName;
                                    }
                                    sendMultimediaMessageToSocketOffline(chatListEntity,chatMessageEntity,fileMimeType, encryptedFilePath, url);
                                } catch (JSONException ex) {
                                    ex.printStackTrace();

                                }
                            }


                        });


            } catch (Exception exc) {
                Log.e("SendMessageOfflService", "onDone: " + "fail");
                exc.printStackTrace();

            }
        } else {
            singleChatUnsentMessages(chatListEntity);
        }

    }
    public String getMultimediaJSONParameter(ChatListEntity chatListEntity,int mimeType) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("sender_id", Integer.parseInt(User_settings.getUserId(getApplicationContext())));
            jsonObj.put("receiver_id", chatListEntity.getUserDbId());
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
    private synchronized void sendMultimediaMessageToSocketOffline(ChatListEntity chatListEntity,ChatMessageEntity chatMessageEntity,int mimeType, String filePath, String fileUrl) {




                chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
                String messageBurnTime = DateTimeUtils.getMessageDestructionTimeByBurnTime(getApplicationContext(), chatMessageEntity.getMessageBurnTime());
                chatMessageEntity.setMessageBurnTimeStamp(messageBurnTime);
                chatMessageEntity.setMessage(fileUrl);
                if (mimeType == AppConstants.MIME_TYPE_AUDIO) {
                    chatMessageEntity.setAudioPath(filePath);
                    dbHelper.updateMessageFilePathByMessageId(chatMessageEntity.getMessageId(), filePath,mimeType);
                } else if (mimeType == AppConstants.MIME_TYPE_VIDEO) {
                    chatMessageEntity.setVideoPath(filePath);
                    dbHelper.updateMessageFilePathByMessageId(chatMessageEntity.getMessageId(), filePath,mimeType);
                } else if (mimeType == AppConstants.MIME_TYPE_NOTE) {
                    chatMessageEntity.setFilePath(filePath);
                    dbHelper.updateMessageFilePathByMessageId(chatMessageEntity.getMessageId(), filePath,mimeType);
                } else if (mimeType == AppConstants.MIME_TYPE_IMAGE) {
                    chatMessageEntity.setImagePath(filePath);
                    dbHelper.updateMessageFilePathByMessageId(chatMessageEntity.getMessageId(), filePath,mimeType);
                } else if (mimeType == AppConstants.MIME_TYPE_CONTACT) {
                    chatMessageEntity.setContactPath(filePath);
                    dbHelper.updateMessageFilePathByMessageId(chatMessageEntity.getMessageId(), filePath,mimeType);
                }

                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                    SocketUtils.sendNewMessageToSocket(getApplicationContext(), chatListEntity, chatMessageEntity);
                    dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_SENT_STATUS);
                    dbHelper.updateMessageBurnDate(chatMessageEntity.getMessageId(), messageBurnTime);
                } else {
                    dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_IN_PROGRESS_STATUS);

                }

        singleChatUnsentMessages(chatListEntity);




    }

    private synchronized void sendMultimediaMessageToSocketOfflineDirect(ChatListEntity chatListEntity,ChatMessageEntity chatMessageEntity) {



            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
            String messageBurnTime = DateTimeUtils.getMessageDestructionTimeByBurnTime(getApplicationContext()
                    , chatMessageEntity.getMessageBurnTime());
            chatMessageEntity.setMessageBurnTimeStamp(messageBurnTime);


            if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                SocketUtils.sendNewMessageToSocket(getApplicationContext(), chatListEntity, chatMessageEntity);
                dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_SENT_STATUS);
                dbHelper.updateMessageBurnDate(chatMessageEntity.getMessageId(), messageBurnTime);


            }
        singleChatUnsentMessages(chatListEntity);

    }
///////////////////////////////////////////////////////////GroupChatOffline///////////////////////////////////////////////////////////////////////

    public synchronized void groupChatUnsentMessages(ChatListEntity chatListEntity) {

        ChatMessageEntity  groupMessageListoff = dbHelper.getUndeliveredMessageList(chatListEntity.getId(), chatListEntity.getChatType());

        ArrayList<GroupMemberEntity> groupMemberList = dbHelper.getGroupMemberList(chatListEntity.getUserDbId());




        if (groupMessageListoff!=null) {
            countLoop=0;



                if (groupMessageListoff.getMessageStatus() == AppConstants.MESSAGE_NOT_SENT_STATUS) {
                    if (groupMessageListoff.getMessageMimeType() == AppConstants.MIME_TYPE_IMAGE && groupMessageListoff.getImagePath().length() == 0) {


                        sendMultimediaMessageOfflineForGroup(chatListEntity,groupMessageListoff, groupMessageListoff.getMessageMimeType(),groupMemberList);



                    } else if (groupMessageListoff .getMessageMimeType() == AppConstants.MIME_TYPE_NOTE &&groupMessageListoff.getFilePath().length() == 0) {

                        sendMultimediaMessageOfflineForGroup(chatListEntity,groupMessageListoff, groupMessageListoff.getMessageMimeType(),groupMemberList);

                    } else if (groupMessageListoff.getMessageMimeType() == AppConstants.MIME_TYPE_AUDIO && groupMessageListoff.getAudioPath().length() == 0) {

                        sendMultimediaMessageOfflineForGroup(chatListEntity,groupMessageListoff, groupMessageListoff.getMessageMimeType(),groupMemberList);

                    } else if (groupMessageListoff .getMessageMimeType() == AppConstants.MIME_TYPE_CONTACT && groupMessageListoff .getContactPath().length() == 0) {

                        sendMultimediaMessageOfflineForGroup(chatListEntity,groupMessageListoff, groupMessageListoff.getMessageMimeType(),groupMemberList);


                    } else {

                        groupMessageListoff.setMessageStatus(AppConstants.MESSAGE_SENT_STATUS);
                        String messageBurnTime = DateTimeUtils.getMessageDestructionTimeByBurnTime(getApplicationContext(), groupMessageListoff.getMessageBurnTime());
                        groupMessageListoff.setMessageBurnTimeStamp(messageBurnTime);
                        groupMemberList = dbHelper.getGroupMemberList(chatListEntity.getUserDbId());
                        SocketUtils.sendGroupMessageToSocket( "SendMsgFromService",getApplicationContext(), chatListEntity, groupMessageListoff, groupMemberList);
                        dbHelper.updateMessageStatusByMessageId(groupMessageListoff.getMessageId(), AppConstants.MESSAGE_SENT_STATUS);
                        dbHelper.updateMessageBurnDate(groupMessageListoff.getMessageId(), messageBurnTime);
                        //setAdapter();
                        groupChatUnsentMessages(chatListEntity);

                    }
                }

                else if (groupMessageListoff.getMessageStatus() == AppConstants.MESSAGE_IN_PROGRESS_STATUS) {
                    sendMultimediaMessageToSocketOfflineDirectGroup(chatListEntity,groupMessageListoff,groupMemberList);
                }

        } else {
            isProcessRunning=false;
            reExecute();

        }


    }

    public synchronized void sendFilesToServerAndSocketOfflineGroup(ChatListEntity chatListEntity,ChatMessageEntity chatMessageEntity,String encryptedFilePath, String httpMethod, String serverUrl, String uploadId, int fileMimeType,ArrayList<GroupMemberEntity> groupMemberList) {
        try {

            Ion.with(getApplicationContext())
                    .load(serverUrl)
                    .setMultipartParameter("json_data", getMultimediaJSONParameterGroup(chatListEntity,fileMimeType))
                    .setMultipartFile("user_files", new File(encryptedFilePath))
                    .asJsonObject()
                    .setCallback((e, result) -> {
//                        if (mProgressDialoge != null) {
//                            if (mProgressDialoge.isShowing())
//                                mProgressDialoge.dismiss();
//                        }
                        if (e != null) {
                            //  CommonUtils.showErrorMsg(mContext, getString(R.string.something_went_wrong));

                        } else {
                            try {
                                JSONObject rootObject = new JSONObject(result.toString());


                                String url = rootObject.getString("url");
                                if (fileMimeType == AppConstants.MIME_TYPE_IMAGE) {
                                    url = url + AppConstants.EXTRA_HIND + fileName;
                                }
                                sendMultimediaMessageToSocketOfflineGroup(chatListEntity,chatMessageEntity,fileMimeType, encryptedFilePath, url,groupMemberList);

                            } catch (JSONException ex) {
                                ex.printStackTrace();

                                // CommonUtils.showErrorMsg(mContext, getString(R.string.file_not_sent));
                            }
                        }

                    });


        } catch (Exception exc) {
            Log.e("SendMessageSer", "onDone: " + "fail");
            groupChatUnsentMessages(chatListEntity);
            //`  CommonUtils.showErrorMsg(mContext, getString(R.string.file_not_sent));
        }
    }
    private synchronized void sendMultimediaMessageToSocketOfflineGroup(ChatListEntity chatListEntity,ChatMessageEntity chatMessageEntity,int mimeType, String filePath, String fileUrl,ArrayList<GroupMemberEntity> groupMemberList) {




            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
            String messageBurnTime = DateTimeUtils.getMessageDestructionTimeByBurnTime(getApplicationContext(), chatMessageEntity.getMessageBurnTime());
            chatMessageEntity.setMessageBurnTimeStamp(messageBurnTime);
            chatMessageEntity.setMessage(fileUrl);
            if (mimeType == AppConstants.MIME_TYPE_AUDIO) {
                chatMessageEntity.setAudioPath(filePath);
                dbHelper.updateMessageFilePathByMessageId(chatMessageEntity.getMessageId(), filePath,mimeType);
            } else if (mimeType == AppConstants.MIME_TYPE_VIDEO) {
                chatMessageEntity.setVideoPath(filePath);
                dbHelper.updateMessageFilePathByMessageId(chatMessageEntity.getMessageId(), filePath,mimeType);
            } else if (mimeType == AppConstants.MIME_TYPE_NOTE) {
                chatMessageEntity.setFilePath(filePath);
                dbHelper.updateMessageFilePathByMessageId(chatMessageEntity.getMessageId(), filePath,mimeType);
            } else if (mimeType == AppConstants.MIME_TYPE_IMAGE) {
                chatMessageEntity.setImagePath(filePath);
                dbHelper.updateMessageFilePathByMessageId(chatMessageEntity.getMessageId(), filePath,mimeType);
            } else if (mimeType == AppConstants.MIME_TYPE_CONTACT) {
                chatMessageEntity.setContactPath(filePath);
                dbHelper.updateMessageFilePathByMessageId(chatMessageEntity.getMessageId(), filePath,mimeType);
            }

            if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                SocketUtils.sendGroupMessageToSocket("SendMsgFromService",getApplicationContext(), chatListEntity, chatMessageEntity, groupMemberList);
                dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_SENT_STATUS);
                dbHelper.updateMessageBurnDate(chatMessageEntity.getMessageId(), messageBurnTime);
                //setAdapter();


            } else {
                dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_IN_PROGRESS_STATUS);

            }
        groupChatUnsentMessages(chatListEntity);


    }
    private synchronized void sendMultimediaMessageToSocketOfflineDirectGroup(ChatListEntity chatListEntity,ChatMessageEntity chatMessageEntity,ArrayList<GroupMemberEntity> groupMemberList) {



            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS);
            String messageBurnTime = DateTimeUtils.getMessageDestructionTimeByBurnTime(getApplicationContext(), chatMessageEntity.getMessageBurnTime());
            chatMessageEntity.setMessageBurnTimeStamp(messageBurnTime);


            if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                SocketUtils.sendGroupMessageToSocket("SendMsgFromService",getApplicationContext(), chatListEntity, chatMessageEntity, groupMemberList);
                dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_SENT_STATUS);
                dbHelper.updateMessageBurnDate(chatMessageEntity.getMessageId(), messageBurnTime);


            }


        groupChatUnsentMessages(chatListEntity);

    }

    public String getMultimediaJSONParameterGroup(ChatListEntity chatListEntity,int mimeType) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("sender_id", Integer.parseInt(User_settings.getUserId(getApplicationContext())));
            jsonObj.put("group_id", chatListEntity.getUserDbId());
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








}
