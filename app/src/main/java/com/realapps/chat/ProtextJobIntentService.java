package com.realapps.chat;

import android.annotation.SuppressLint;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.koushikdutta.ion.Ion;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.model.PublicKeyEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.Cryptography;
import com.realapps.chat.utils.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;


public class ProtextJobIntentService extends JobIntentService {

    private static final String TAG = "JobService";
    public static final String CHAT_ID = "chat_id";
    public static final String CHAT_TYPE = "chat_type";
    public static final String USER_ID = "user_id";
    public static final String USER_ECC = "user_ecc";
    public static final String USER_NAME = "user_name";
    public static final int SHOW_RESULT = 123;
    DbHelper dbHelper = null;
    static Context mContext;
    /**
     * Result receiver object to send results
     */
    private ResultReceiver mResultReceiver;
    /**
     * Unique job ID for this service.
     */
    static final int DOWNLOAD_JOB_ID = 1000;
    /**
     * Actions download
     */
    private static final String ACTION_DOWNLOAD = "action.DOWNLOAD_DATA";

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, int chatId, int chatType, ChatListEntity chatListEntity){//}, ServiceResultReceiver workerResultReceiver) {
        mContext = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        Intent intent = new Intent(context, JobService.class);
        intent.putExtra(CHAT_ID, chatId);
        intent.putExtra(CHAT_TYPE,chatType);
        intent.putExtra(USER_ID,chatListEntity.getUserDbId());
        intent.putExtra(USER_ECC,chatListEntity.getEccId());
        intent.putExtra(USER_NAME,chatListEntity.getName());
        intent.setAction(ACTION_DOWNLOAD);

            enqueueWork(context, JobService.class, DOWNLOAD_JOB_ID, intent);
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        dbHelper = new DbHelper(mContext);
        Log.d(TAG, "onHandleWork() called with: intent = [" + intent + "]");
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_DOWNLOAD:
                    startBackGroundThreadForMediaMessages(intent.getIntExtra(CHAT_ID,0),intent.getIntExtra(CHAT_TYPE,0),intent.getIntExtra(USER_ID,0),intent.getStringExtra(USER_ECC),intent.getStringExtra(USER_NAME));
                    break;
            }
        }
    }

    private void sendMultimediaMessageOffline(String messageId,String filePath, int fileMimeType,int userId,String userEcc,int chatId,int chatType,String userName) {


        try {
            if (dbHelper.checkPublicKeysOfUser(userId)) {
                String encryptedFilePath = "";
                if (fileMimeType == AppConstants.MIME_TYPE_AUDIO) {
                    encryptedFilePath = Cryptography.encryptFile(mContext, filePath, userId, userEcc, AppConstants.MIME_TYPE_AUDIO);
                } else if (fileMimeType == AppConstants.MIME_TYPE_CONTACT) {
                    encryptedFilePath = Cryptography.encryptFile(mContext, filePath, userId, userEcc, AppConstants.MIME_TYPE_CONTACT);
                } else if (fileMimeType == AppConstants.MIME_TYPE_IMAGE) {
                    encryptedFilePath = Cryptography.encryptFile(mContext, filePath, userId, userEcc, AppConstants.MIME_TYPE_IMAGE);
                } else if (fileMimeType == AppConstants.MIME_TYPE_NOTE) {
                    encryptedFilePath = Cryptography.encryptFile(mContext, filePath, userId, userEcc, AppConstants.MIME_TYPE_NOTE);
                } else if (fileMimeType == AppConstants.MIME_TYPE_VIDEO) {
                    encryptedFilePath = Cryptography.encryptFile(mContext, filePath, userId, userEcc, AppConstants.MIME_TYPE_VIDEO);
                }
                if (encryptedFilePath.length() > 0)
                    sendFilesToServerAndSocketOffline(messageId,encryptedFilePath, "POST", ApiEndPoints.URL_UPLOADING_MULTIMEDIA_SINGLE, UUID.randomUUID().toString(), fileMimeType,userId);

            } else {
                if (NetworkUtils.isNetworkConnected(mContext)) {
                    searchPublicKeys(userId,userEcc,chatType,userName);
                } else {
                    //  CommonUtils.showErrorMsg(mContext, getString(R.string.no_internet_connection));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendFilesToServerAndSocketOffline(String messageId,String encryptedFilePath, String httpMethod, String serverUrl, String uploadId, int fileMimeType,int userId) {
        String fileName = "";
        if (NetworkUtils.isNetworkConnected(mContext)) {
            try {
                Ion.with(mContext)
                        .load(serverUrl)
                        .setMultipartParameter("json_data", getMultimediaJSONParameter(fileMimeType,userId))
                        .setMultipartFile("user_files", new File(encryptedFilePath))
                        .asJsonObject()
                        .setCallback((e, result) -> {


                            if (e != null) {
                                e.printStackTrace();

                            } else {
                                try {
                                    JSONObject rootObject = new JSONObject(result.toString());
                                    String url = rootObject.getString("url");
                                    if (fileMimeType == AppConstants.MIME_TYPE_IMAGE) {
                                        url = url + AppConstants.EXTRA_HIND + fileName;
                                    }
                                    sendMultimediaMessageToSocketOffline(messageId,fileMimeType, encryptedFilePath, url);
                                } catch (JSONException ex) {
                                    ex.printStackTrace();

                                }
                            }


                        });


            } catch (Exception exc) {
                Log.e(TAG, "onDone: " + "fail");
                exc.printStackTrace();

            }
        }

    }

    private void sendMultimediaMessageToSocketOffline(String messageId,int mimeType, String filePath, String fileUrl) {



        Log.e("image","send");
    }

    public void startBackGroundThreadForMediaMessages(int chatId,int chatType,int userId,String userEcc,String userName) {

        if (NetworkUtils.isNetworkConnected(mContext)) {
            if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {
                ArrayList<ChatMessageEntity> messageList = new ArrayList<>();

                messageList = dbHelper.getMessageList(chatId,chatType);
                if (messageList.size() > 0) {
                    for (int i = 0; i < messageList.size(); i++) {
                        if (messageList.get(i).getMessageStatus() == AppConstants.MESSAGE_NOT_SENT_STATUS) {
                            if (messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_IMAGE && messageList.get(i).getImagePath().length() == 0) {
                                sendMultimediaMessageOffline(messageList.get(i).getMessageId(), messageList.get(i).getMessage(), messageList.get(i).getMessageMimeType(),userId,userEcc,chatId,chatType,userName);
                                break;
                            } else if (messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_VIDEO && messageList.get(i).getVideoPath().length() == 0) {
                                sendMultimediaMessageOffline(messageList.get(i).getMessageId(), messageList.get(i).getMessage(), messageList.get(i).getMessageMimeType(),userId,userEcc,chatId,chatType,userName);
                                break;
                            } else if (messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_AUDIO && messageList.get(i).getVideoPath().length() == 0) {
                                sendMultimediaMessageOffline(messageList.get(i).getMessageId(), messageList.get(i).getMessage(), messageList.get(i).getMessageMimeType(),userId,userEcc,chatId,chatType,userName);
                                break;
                            } else if (messageList.get(i).getMessageMimeType() == AppConstants.MIME_TYPE_CONTACT && messageList.get(i).getContactPath().length() == 0) {
                                sendMultimediaMessageOffline(messageList.get(i).getMessageId(), messageList.get(i).getMessage(), messageList.get(i).getMessageMimeType(),userId,userEcc,chatId,chatType,userName);
                                break;
                            }
                        }
                    }
                }
            }
        }

    }
    public void searchPublicKeys(int userId,String ecc,int chatType,String name) {
        AndroidNetworking.post(ApiEndPoints.URL_FETCH_ECC_KEYS)
                .addBodyParameter("email", CommonUtils.getUserEmail(ecc))
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
                                keyEntity.setUserDbId(userId);
                                keyEntity.setEccId(ecc);
                                keyEntity.setUserType(chatType);
                                keyEntity.setEccPublicKey(publicKey);
                                keyEntity.setName(name);
                                dbHelper.insertPublicKey(keyEntity);

                                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen()) {

                                } else {

                                }


                            } else {
                                CommonUtils.showInfoMsg(mContext, rootObject.getString("msg"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {

                    }
                });


    }
    public String getMultimediaJSONParameter(int mimeType,int userId) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("sender_id", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObj.put("receiver_id", userId);
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
