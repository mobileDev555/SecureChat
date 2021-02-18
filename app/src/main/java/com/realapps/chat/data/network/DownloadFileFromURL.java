package com.realapps.chat.data.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.FileLog;
import com.realapps.chat.utils.FileUtils;
import com.realapps.chat.utils.NotificationUtils;
import com.realapps.chat.utils.SocketUtils;
import com.realapps.chat.view.home.activity.ChatWindowActivity;
import com.realapps.chat.view.home.activity.GroupChatWindowActivity;
import com.realapps.chat.view.home.fragment.FragmentChats;
import com.realapps.chat.view.home.fragment.FragmentGroupChat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Prashant Sharma on 3/23/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class DownloadFileFromURL extends AsyncTask<String, String, String> {
    private static final String TAG = "DownloadFileFromURL";
    Context mContext;
    int mimeType;
    int type;
    ChatListEntity chatListEntity;
    ChatMessageEntity chatMessageEntity;

    public DownloadFileFromURL(Context mContext, ChatListEntity chatListEntity, ChatMessageEntity chatMessageEntity) {
        this.mContext = mContext;
        this.chatListEntity = chatListEntity;
        this.mimeType = chatMessageEntity.getMessageMimeType();
        this.chatMessageEntity = chatMessageEntity;
        Log.e(TAG, "DownloadFileFromURL: " + chatMessageEntity.getMessageId());
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... f_url) {
        int count;
        File file = null;

        String fName = "";
        try {

            String urlPath = f_url[0];


            if (urlPath.contains(AppConstants.EXTRA_HIND)) {
                fName = urlPath.substring(urlPath.indexOf(AppConstants.EXTRA_HIND));
                urlPath = urlPath.replace(fName, "");
                fName = fName.replace(AppConstants.EXTRA_HIND, "") + ".jpg";
            }
            Log.e(TAG, "doInBackground: " + urlPath + "   " + chatMessageEntity.getMessageId());
            URL url = new URL(urlPath);

            URLConnection conection = url.openConnection();
            conection.connect();

            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            String root = "";
            // Output stream to write file
            if (type == 0) {
                if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_IMAGE) {
                    root = CommonUtils.getKeyBasePath(mContext) + "Images";
                } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_VIDEO) {
                    root = CommonUtils.getKeyBasePath(mContext) + "Videos";
                } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_AUDIO) {
                    root = CommonUtils.getKeyBasePath(mContext) + "Audio";
                } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_CONTACT) {
                    root = CommonUtils.getKeyBasePath(mContext) + "Contacts";
                } else if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_NOTE) {
                    root = CommonUtils.getKeyBasePath(mContext) + "Texts";
                }
            }
            if (!urlPath.contains(AppConstants.EXTRA_HIND))
                fName = urlPath.substring(urlPath.lastIndexOf('/') + 1);

            if (FileUtils.checkAndCreateFolder(root))
                file = new File(root, fName);

            OutputStream output = new FileOutputStream(file);

            byte[] data = new byte[1024];

            while ((count = input.read(data)) != -1) {

                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();

        } catch (Exception e) {
            FileLog.e("Error: ", e.getMessage());
            return "";
        }
        if (file != null)
            return file.getAbsolutePath();
        else
            return "";
    }

    @Override
    protected void onPostExecute(String file_url) {
        if (file_url.length() > 0) {

            Log.e(TAG, "onPostExecute: " + file_url + "  " + chatMessageEntity.getMessageId());
            DbHelper db = new DbHelper(mContext);
            if (db.checkMessageId(chatMessageEntity.getMessageId(), chatMessageEntity.getChatUserDbId())) {
                return;
            }
            chatMessageEntity.setMessageStatus(AppConstants.MESSAGE_UNREAD_STATUS);
            if (mimeType == AppConstants.MIME_TYPE_IMAGE) {
                chatMessageEntity.setImagePath(file_url);
                chatMessageEntity.setFileName(new File(file_url).getName());
                chatMessageEntity.setMessageMimeType(AppConstants.MIME_TYPE_IMAGE);
                db.insertChatMessage(chatMessageEntity);

            } else if (mimeType == AppConstants.MIME_TYPE_VIDEO) {
                chatMessageEntity.setVideoPath(file_url);
                chatMessageEntity.setMessageMimeType(AppConstants.MIME_TYPE_VIDEO);
                db.insertChatMessage(chatMessageEntity);

            } else if (mimeType == AppConstants.MIME_TYPE_AUDIO) {
                chatMessageEntity.setAudioPath(file_url);
                chatMessageEntity.setMessageMimeType(AppConstants.MIME_TYPE_AUDIO);
                db.insertChatMessage(chatMessageEntity);

            } else if (mimeType == AppConstants.MIME_TYPE_NOTE) {
                chatMessageEntity.setFilePath(file_url);
                chatMessageEntity.setMessageMimeType(AppConstants.MIME_TYPE_NOTE);
                db.insertChatMessage(chatMessageEntity);

            } else if (mimeType == AppConstants.MIME_TYPE_CONTACT) {
                chatMessageEntity.setContactPath(file_url);
                chatMessageEntity.setMessageMimeType(AppConstants.MIME_TYPE_CONTACT);
                db.insertChatMessage(chatMessageEntity);

            }
            db.updateChatListTimeStamp(chatListEntity.getUserDbId(), chatMessageEntity.getMessageTimeStamp());
            if (FragmentChats.refreshChatListListener != null)
                FragmentChats.refreshChatListListener.onRefresh();

            if (FragmentGroupChat.refreshChatListListener != null) {
                FragmentGroupChat.refreshChatListListener.onRefresh();
            }
            boolean isDelivered = false;

            if (ChatWindowActivity.chatWindowFunctionListener != null) {
                isDelivered = true;
                ChatWindowActivity.chatWindowFunctionListener.onNewMessage(chatMessageEntity);
            }
            if (GroupChatWindowActivity.chatWindowFunctionListener != null) {
                isDelivered = true;
                GroupChatWindowActivity.chatWindowFunctionListener.onNewMessage(chatMessageEntity);
            }
            if (!isDelivered) {
                JSONObject rootObject = new JSONObject();
                try {
                    rootObject.put("eccId", chatMessageEntity.getEddId());
                    rootObject.put("messageId", chatMessageEntity.getMessageId());
                    rootObject.put("screenName", chatMessageEntity.getName());
                    rootObject.put("sendFrom", chatMessageEntity.getSenderId());
                    rootObject.put("sendTo", chatMessageEntity.getReceiverId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                SocketUtils.sendAcknowledgementToSocket(rootObject, AppConstants.MESSAGE_STATUS_DELIVERED);
                if (chatListEntity.getSnoozeStatus() == AppConstants.NOTIFICATION_SNOOZE_NO) {
                    NotificationUtils.showNotification(mContext, chatListEntity, AppConstants.NOTIFICATION_ID, mContext.getResources().getString(R.string.title_message_notification), db.getTotalUnreadMessages());
                }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              showMessageCountBadge();
            }
        }
    }

    private void showMessageCountBadge() {
        int messageCount = new DbHelper(mContext).getTotalUnreadMessages();
        NotificationUtils.showBadge(mContext, messageCount);
    }
}
