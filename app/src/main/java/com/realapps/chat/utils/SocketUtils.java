package com.realapps.chat.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.model.GroupMemberEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Prashant Sharma on 3/19/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class SocketUtils {
    //Socket URL Link
    public static String SocketUrl = BuildConfig.BASE_SOCKET;  //Live
    //public static String SocketUrl = "ws://apps.######.###/chatwithmongodb/";  //Live
    // public static String SocketUrl = "ws://192.95.33.149:8181/protext/chatwithmongodb/";   //Development
    //  public static String SocketUrl = "ws://192.95.33.149:8181/development/chatwithmongodb/";   //Development NEW

    //Friend Request
    public static int notAccepted = 0;
    public static int accepted = 1;
    public static int pending = 2;
    public static int request = 3;
    //Socket Request
    public static int ReqBlockList = 10;
    public static int ReqSingleChat = 1;
    public static int ReqSingleRChat = 20;
    public static int ReqAddGroup = 2;
    public static int ReqAddMember = 3;
    public static int ReqDeleteMember = 4;
    public static int ReqSendGroupMsg = 5;
    public static int ReqSendGroupRMsg = 5;
    public static int ReqSendRGroupRMsg = 21;
    public static int ReqBlockUser = 9;
    public static int ReqFriendRequest = 11;
    public static int ReqSendFriendReqResponse = 12;
    public static int ReqSendMessageAcknowledgement = 7;
    public static int ReqSendMessageAcknowledgementForGroup = 25;
    public static int ReqDeleteConatact = 15;
    public static int ReqChangeGroupName = 16;
    public static int ReqLeaveGroup = 18;
    public static int ReqAcknowledgement = 17;
    public static int ReqDeleteSingleChat = 23;
    public static int ReqDeleteGroupChat = 24;
    //Socket Response
    public static int RespMsgSent = 1;
    public static int RespMsgDelivered = 20;
    public static int RespBlockMembersList = 45;
    public static int RespFriendECCKeyChanged = 21;
    public static int RespAddMember = 22;
    public static int RespRemoveMember = 33;
    public static int RespLeaveGroup = 77;
    public static int RespBlockMember = 44;
    public static int RespSingleMSg = 2;
    public static int RespSingleRMSg = 52;
    public static int RespGroupCreated = 3;
    public static int RespReadMessage = 4;
    public static int RespDelMember = 6;
    public static int RespGetGroupMessage = 8;
    public static int RespGetRGroupMessage = 56;
    public static int RespAddGroup = 9;
    public static int RespFriendRequest = 111;
    public static int RespFriendRequestResponse = 112;
    public static int RespDeleteConatact = 131;
    public static int RespChangeGroupName = 99;

    @SuppressLint("LongLogTag")
    public static void sendDeviceTokenToSocket(Context mContext, int status) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", 14);
            jsonObject.put("userId", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("eccId", User_settings.getECCID(mContext));
            jsonObject.put("deviceToken", User_settings.getFirebaseToken(mContext));
            jsonObject.put("status", status);
            jsonObject.put("dtype", 1);

            Log.e("sendDeviceTokenToSocket: ", jsonObject.toString());
            if (AppConstants.mWebSocketClient != null) {
                if (AppConstants.mWebSocketClient.isOpen()) {
                    AppConstants.mWebSocketClient.send(jsonObject.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void sendREadAcknowledgementToSocket(Context mContext, ChatMessageEntity chatMessageEntity, int msgStatusDelivered) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen())
            AppConstants.mWebSocketClient.send(getMessageParameter(mContext, chatMessageEntity, msgStatusDelivered).toString());
        else {
            if (AppConstants.lockscreen)
                new DbHelper(mContext).updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_UNREAD_STATUS);
            else
                new DbHelper(mContext).updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_STATUS_READ_BUT_UN_ACK);
        }
    }

    public static void sendAcknowledgementMessage(Integer userId, String messageId) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen())
            AppConstants.mWebSocketClient.send(getGlobalAcknowledgement(userId, messageId).toString());
    }

    public static void sendLeaveGroupToSocket(Context mContext, Integer groupId) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen())
            AppConstants.mWebSocketClient.send(getLeaveGroupParameters(mContext, groupId).toString());
    }

    public static JSONObject getMessageParameter(Context mContext, ChatMessageEntity chatMessageEntity, int msgStatusDelivered) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", ReqSendMessageAcknowledgement);
            jsonObject.put("sendFrom", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("screenName", chatMessageEntity.getName());
            jsonObject.put("eccId", " ");
            jsonObject.put("sendTo", chatMessageEntity.getSenderId());
            jsonObject.put("messageId", chatMessageEntity.getMessageId());
            jsonObject.put("messagestatus", msgStatusDelivered);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject getGlobalAcknowledgement(Integer userId, String messageId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", ReqAcknowledgement);
            jsonObject.put("userId", userId);
            jsonObject.put("messageId", messageId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject getLeaveGroupParameters(Context mContext, Integer groupId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", ReqLeaveGroup);
            jsonObject.put("groupId", groupId);

            JSONArray jsonArray = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("userId", Integer.parseInt(User_settings.getUserId(mContext)));
            object.put("eccId", User_settings.getECCID(mContext));
            object.put("screenName", User_settings.getScreenName(mContext));
            object.put("type", 0);
            jsonArray.put(object);
            jsonObject.put("member", jsonArray);
            jsonObject.put("name", User_settings.getScreenName(mContext));
            jsonObject.put("creatorId", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("eccId", User_settings.getECCID(mContext));
            jsonObject.put("screenName", User_settings.getScreenName(mContext));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public synchronized static void sendAcknowledgementToSocket(JSONObject rootObject, int msgStatusDelivered) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen())
            AppConstants.mWebSocketClient.send(getMessageParameter(rootObject, msgStatusDelivered).toString());
    }

    public synchronized static void sendAcknowledgementToSocketForGroup(JSONObject rootObject, int msgStatusDelivered) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen())
            AppConstants.mWebSocketClient.send(getMessageParameterForGroup(rootObject, msgStatusDelivered).toString());
    }
    public static JSONObject getMessageParameterForGroup(JSONObject rootObject, int msgStatusDelivered) {
        JSONObject jsonObject = new JSONObject();
        try {
            int sendFrom = rootObject.getInt("sendFrom");
            String messageId = rootObject.getString("messageId");

            jsonObject.put("requestId", ReqSendMessageAcknowledgementForGroup);
            jsonObject.put("sendFrom", sendFrom);
            jsonObject.put("messageId", messageId);
            jsonObject.put("messagestatus", msgStatusDelivered);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject getMessageParameter(JSONObject rootObject, int msgStatusDelivered) {
        JSONObject jsonObject = new JSONObject();
        try {
            int sendFrom = rootObject.getInt("sendFrom");
            int sendTo = rootObject.getInt("sendTo");
            String screenName = rootObject.getString("screenName");
            String eccId = rootObject.getString("eccId");
            String messageId = rootObject.getString("messageId");
            jsonObject.put("requestId", ReqSendMessageAcknowledgement);
            jsonObject.put("sendFrom", sendTo);
            jsonObject.put("screenName", screenName);
            jsonObject.put("eccId", eccId);
            jsonObject.put("sendTo", sendFrom);
            jsonObject.put("messageId", messageId);
            jsonObject.put("messagestatus", msgStatusDelivered);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    public static void sendNewMessageToSocket(Context mContext, ChatListEntity chatListEntity, ChatMessageEntity chatMessageEntity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.getConnection().isOpen())
                    AppConstants.mWebSocketClient.send(getSendNewMessageParameter(mContext, chatListEntity, chatMessageEntity).toString());
                return null;
            }
        }.execute();
    }

    public static void sendGroupMessageToSocket(String fname , Context mContext, ChatListEntity chatListEntity, ChatMessageEntity chatMessageEntity, ArrayList<GroupMemberEntity> groupMemberList) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen())
                AppConstants.mWebSocketClient.send(getSendGroupMessageParameter(fname,mContext, chatListEntity, chatMessageEntity, groupMemberList).toString());
                return null;
            }
        }.execute();
    }

    public static void sendRGroupMessageToSocket(Context mContext, ChatListEntity chatListEntity, ChatMessageEntity chatMessageEntity, ArrayList<GroupMemberEntity> groupMemberList) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen())
                AppConstants.mWebSocketClient.send(getSendGroupRevisedMessageParameter(mContext, chatListEntity, chatMessageEntity, groupMemberList).toString());
                return null;
            }
        }.execute();
    }


    public static void sendRevisedMessageToSocket(Context mContext, ChatListEntity chatListEntity, ChatMessageEntity chatMessageEntity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen())
                AppConstants.mWebSocketClient.send(getSendRevisedMessageParameter(mContext, chatListEntity, chatMessageEntity).toString());
                return null;
            }
        }.execute();
    }

    public static void sendCreateGroupToSocket(Context mContext, ArrayList<GroupMemberEntity> groupMemberList, String groupName, int groupId) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen())
        AppConstants.mWebSocketClient.send(getCreateGroupJSON(mContext, groupMemberList, groupName, groupId).toString());
    }

    public static void sendAddContactRequestToSocket(Context mContext, ContactEntity entity) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen())
        AppConstants.mWebSocketClient.send(getAddContactRequestParameters(mContext, entity.getUserDbId()).toString());
    }

    public static void sendResponseContactRequestToSocket(Context mContext, ContactEntity entity, int status) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen())
        AppConstants.mWebSocketClient.send(getResponseContactRequestParameters(mContext, entity.getUserDbId(), status).toString());
    }


    public static boolean sendRemoveMemberToSocket(Context mContext, int groupId, String groupName, GroupMemberEntity memberEntity) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.getConnection().isOpen()) {
            AppConstants.mWebSocketClient.send(getResponseRemoveMember(mContext, groupId, groupName, memberEntity).toString());
            return true;
        } else
            return false;

    }

    public static boolean sendAddMemberToSocket(Context mContext, int groupId, String groupName, ArrayList<ContactEntity> contacts) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.getConnection().isOpen()) {
            AppConstants.mWebSocketClient.send(getResponseAddMember(mContext, groupId, groupName, contacts).toString());
            return true;
        } else
            return false;
    }

    public static void sendRemoveContactToSocket(Context mContext, ContactEntity contactEntity) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen())
        AppConstants.mWebSocketClient.send(getResponseRemoveContact(mContext, contactEntity).toString());
    }

    public static boolean sendDeleteMesageToSocket(Context mContext, ChatListEntity chatListEntity, List<ChatMessageEntity> messages) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.getConnection().isOpen()) {
            AppConstants.mWebSocketClient.send(getResponseDeleteMessage(mContext, chatListEntity, messages));
            return true;
        } else {
            return false;
        }
    }

    private static String getResponseDeleteMessage(Context mContext, ChatListEntity chatListEntity, List<ChatMessageEntity> messages) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", ReqDeleteSingleChat);
            jsonObject.put("sendFrom", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("sendTo", chatListEntity.getUserDbId());
            JSONArray messageIds = new JSONArray();
            for (ChatMessageEntity entity : messages) {
                messageIds.put(entity.getMessageId());
            }
            jsonObject.put("message_id", messageIds);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
    public static boolean sendDeleteMesageToSocketGroup(Context mContext, ChatListEntity chatListEntity, ArrayList<ChatMessageEntity> messages) {
        if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.getConnection().isOpen()) {
            AppConstants.mWebSocketClient.send(getResponseDeleteMessageGroup(mContext, chatListEntity, messages));
            return true;
        } else {
            return false;
        }
    }

    private static String getResponseDeleteMessageGroup(Context mContext, ChatListEntity chatListEntity, List<ChatMessageEntity> messages) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", ReqDeleteGroupChat);
            jsonObject.put("sendFrom", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("groupId", chatListEntity.getUserDbId());
            JSONArray messageIds = new JSONArray();
            for (ChatMessageEntity entity : messages) {
                messageIds.put(entity.getMessageId());
            }
            jsonObject.put("message_id", messageIds);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static JSONObject getSendNewMessageParameter(Context mContext, ChatListEntity chatListEntity, ChatMessageEntity chatMessageEntity) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", ReqSingleChat);
            jsonObject.put("sendFrom", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("screenName", User_settings.getScreenName(mContext));
            jsonObject.put("eccId", User_settings.getECCID(mContext));
            jsonObject.put("sendTo", chatListEntity.getUserDbId());
            jsonObject.put("MsgDateTime", DateTimeUtils.localDateTimeToUTC(chatMessageEntity.getMessageTimeStamp()));
            if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_TEXT) {
                jsonObject.put("msgTex", Cryptography.encryptText(mContext, chatListEntity.getUserDbId(), chatMessageEntity.getMessage()));
            } else {
                jsonObject.put("msgTex", chatMessageEntity.getMessage());
            }
            jsonObject.put("msgBurn", chatListEntity.getBurnTime());
            jsonObject.put("mimeType", chatMessageEntity.getMessageMimeType());
            jsonObject.put("messsagestatus", AppConstants.MESSAGE_SENT_STATUS);
            jsonObject.put("messageId", chatMessageEntity.getMessageId());
            jsonObject.put("reply", "false");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("SendNewMsgParameter:", jsonObject.toString());
        return jsonObject;
    }

    public static JSONObject getSendRevisedMessageParameter(Context mContext, ChatListEntity chatListEntity, ChatMessageEntity chatMessageEntity) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", ReqSingleRChat);
            jsonObject.put("sendFrom", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("screenName", User_settings.getScreenName(mContext));
            jsonObject.put("eccId", User_settings.getECCID(mContext));
            jsonObject.put("sendTo", chatListEntity.getUserDbId());
            jsonObject.put("MsgDateTime", DateTimeUtils.localDateTimeToUTC(chatMessageEntity.getEditedMessageTimeStamp()));
            if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_TEXT) {
                jsonObject.put("msgTex", Cryptography.encryptText(mContext, chatListEntity.getUserDbId(), chatMessageEntity.getMessage()));
            } else {
                jsonObject.put("msgTex", chatMessageEntity.getMessage());
            }
            jsonObject.put("msgBurn", chatListEntity.getBurnTime());
            jsonObject.put("mimeType", chatMessageEntity.getMessageMimeType());
            jsonObject.put("messsagestatus", AppConstants.MESSAGE_SENT_STATUS);
            jsonObject.put("messageId", chatMessageEntity.getMessageId());
            jsonObject.put("reply", "false");
            jsonObject.put("parentMessageID", chatMessageEntity.getParentMessageId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject getSendGroupMessageParameter(String fname,Context mContext, ChatListEntity chatListEntity, ChatMessageEntity chatMessageEntity, ArrayList<GroupMemberEntity> groupMemberList) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", ReqSendGroupMsg);
            jsonObject.put("senderId", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("groupId", Integer.parseInt(String.valueOf(chatListEntity.getUserDbId())));
            jsonObject.put("MsgDateTime", DateTimeUtils.localDateTimeToUTC(chatMessageEntity.getMessageTimeStamp()));
            if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_TEXT) {
                jsonObject.put("msgTex", Cryptography.encryptGroupText(mContext, groupMemberList, chatMessageEntity.getMessage()));
            } else {
                jsonObject.put("msgTex", chatMessageEntity.getMessage());
            }
            jsonObject.put("eccId", chatMessageEntity.getEddId());
            jsonObject.put("screenName", User_settings.getScreenName(mContext));
            jsonObject.put("msgBurn", chatListEntity.getBurnTime());
            jsonObject.put("mimeType", chatMessageEntity.getMessageMimeType());
            jsonObject.put("messageId", chatMessageEntity.getMessageId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CommonUtils.appendLog(fname+": :"+jsonObject.toString());

        return jsonObject;
    }

    public static JSONObject getSendGroupRevisedMessageParameter(Context mContext, ChatListEntity chatListEntity, ChatMessageEntity chatMessageEntity, ArrayList<GroupMemberEntity> groupMemberList) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", ReqSendRGroupRMsg);
            jsonObject.put("senderId", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("groupId", Integer.parseInt(String.valueOf(chatListEntity.getUserDbId())));
            jsonObject.put("MsgDateTime", DateTimeUtils.localDateTimeToUTC(chatMessageEntity.getEditedMessageTimeStamp()));
            if (chatMessageEntity.getMessageMimeType() == AppConstants.MIME_TYPE_TEXT) {
                jsonObject.put("msgTex", Cryptography.encryptGroupText(mContext, groupMemberList, chatMessageEntity.getMessage()));
            } else {
                jsonObject.put("msgTex", chatMessageEntity.getMessage());
            }
            jsonObject.put("eccId", chatMessageEntity.getEddId());
            jsonObject.put("screenName", User_settings.getScreenName(mContext));
            jsonObject.put("msgBurn", chatListEntity.getBurnTime());
            jsonObject.put("mimeType", chatMessageEntity.getMessageMimeType());
            jsonObject.put("messageId", chatMessageEntity.getMessageId());
            jsonObject.put("parentMessageID", chatMessageEntity.getParentMessageId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public static JSONObject getCreateGroupJSON(Context mContext, ArrayList<GroupMemberEntity> groupMemberList, String groupName, int groupId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", ReqAddGroup);
            jsonObject.put("creatorId", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("eccId", User_settings.getECCID(mContext).toUpperCase());
            jsonObject.put("screenName", User_settings.getScreenName(mContext));
            jsonObject.put("name", groupName);
            jsonObject.put("desciption", "");
            jsonObject.put("groupId", groupId);
            JSONArray member = new JSONArray();
            for (int i = 0; i < groupMemberList.size(); i++) {
                if (!(groupMemberList.get(i).getEccId().equalsIgnoreCase(User_settings.getECCID(mContext)))) {
                    GroupMemberEntity entity = groupMemberList.get(i);
                    JSONObject object = new JSONObject();
                    object.put("userId", entity.getUserDbId());
                    object.put("eccId", entity.getEccId().toUpperCase());
                    object.put("screenName", entity.getName());
                    member.put(object);
                }
            }
            jsonObject.put("member", member);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject getAddContactRequestParameters(Context mContext, int userId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", SocketUtils.ReqFriendRequest);
            jsonObject.put("sendFrom", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("sendTo", userId);
            jsonObject.put("requeststatus", 0);
            jsonObject.put("messageId", String.valueOf(System.currentTimeMillis()));
            jsonObject.put("eccId", User_settings.getECCID(mContext));
            jsonObject.put("screenName", User_settings.getScreenName(mContext));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject getResponseContactRequestParameters(Context mContext, int userId, int status) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", SocketUtils.ReqSendFriendReqResponse);
            jsonObject.put("sendFrom", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("sendTo", userId);
            jsonObject.put("requeststatus", 0);
            jsonObject.put("messageId", String.valueOf(System.currentTimeMillis()));
            jsonObject.put("eccId", User_settings.getECCID(mContext));
            jsonObject.put("screenName", User_settings.getScreenName(mContext));
            jsonObject.put("status", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private static JSONObject getResponseRemoveMember(Context mContext, int groupId, String groupName, GroupMemberEntity memberEntity) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", ReqDeleteMember);
            jsonObject.put("groupId", groupId);
            jsonObject.put("name", groupName);
            jsonObject.put("creatorId", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("eccId", User_settings.getECCID(mContext).toUpperCase());
            jsonObject.put("screenName", User_settings.getScreenName(mContext));

            JSONArray member = new JSONArray();
            JSONObject object = new JSONObject();

            object.put("userId", memberEntity.getUserDbId());
            object.put("eccId", memberEntity.getEccId());
            object.put("screenName", memberEntity.getName());
            member.put(object);

            jsonObject.put("member", member);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FileLog.sout(jsonObject.toString());
        return jsonObject;
    }


    private static JSONObject getResponseAddMember(Context mContext, int groupId, String groupName, ArrayList<ContactEntity> addedContact) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("requestId", ReqAddMember);
            jsonObject.put("creatorId", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("name", groupName);
            jsonObject.put("eccId", User_settings.getECCID(mContext).toUpperCase());
            jsonObject.put("screenName", User_settings.getScreenName(mContext));
            jsonObject.put("groupId", groupId);
            JSONArray member = new JSONArray();
            for (int i = 0; i < addedContact.size(); i++) {
                ContactEntity entity = addedContact.get(i);
                JSONObject object = new JSONObject();
                object.put("userId", entity.getUserDbId());
                object.put("eccId", entity.getEccId().toUpperCase());
                object.put("screenName", entity.getName());
                object.put("ecc_key", entity.getEccPublicKey());
                member.put(object);
            }
            jsonObject.put("member", member);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FileLog.sout(jsonObject.toString());
        return jsonObject;
    }

    private static JSONObject getResponseRemoveContact(Context mContext, ContactEntity contactEntity) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", SocketUtils.ReqDeleteConatact);
            jsonObject.put("sendFrom", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("sendTo", contactEntity.getUserDbId());
            jsonObject.put("requeststatus", 0);
            jsonObject.put("messageId", String.valueOf(System.currentTimeMillis()));
            jsonObject.put("eccId", User_settings.getECCID(mContext).toUpperCase());
            jsonObject.put("screenName", User_settings.getScreenName(mContext));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("removeContact", jsonObject.toString());

        return jsonObject;
    }

    public static void sendUpdatedGroupNameToSocket(Context mContext, int groupId, String groupName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", ReqChangeGroupName);
            jsonObject.put("groupId", groupId);
            jsonObject.put("name", groupName);
            jsonObject.put("updatorId", Integer.parseInt(User_settings.getUserId(mContext)));

            Log.e("sendUpdatedGroupNameSoc", jsonObject.toString());
            if (AppConstants.mWebSocketClient != null) {
                if (AppConstants.mWebSocketClient.isOpen()) {
                    AppConstants.mWebSocketClient.send(jsonObject.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
