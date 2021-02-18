package com.realapps.chat.utils;

import android.content.Context;
import android.util.Log;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.network.DownloadFileFromURL;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.model.GroupMemberEntity;
import com.realapps.chat.model.SocketRequestEntity;
import com.realapps.chat.view.home.activity.ChatWindowActivity;
import com.realapps.chat.view.home.activity.GroupChatWindowActivity;
import com.realapps.chat.view.home.activity.GroupDetailActivity;
import com.realapps.chat.view.home.fragment.FragmentChats;
import com.realapps.chat.view.home.fragment.FragmentContacts;
import com.realapps.chat.view.home.fragment.FragmentGroupChat;
import com.realapps.chat.view.home.fragment.FragmentVault;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
/**
 * Created by Prashant Sharma on 3/19/2018.
 */
public class MessagesUtils {
    public static int NewMessageUserDbId = -1;
    public static synchronized void saveSingleChatMessage(final Context mContext, final JSONObject rootObject) {
        DbHelper db = new DbHelper(mContext);
        ChatMessageEntity entity = new ChatMessageEntity();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
        String date = df.format(Calendar.getInstance().getTime());
        try {
            int senderDbId = rootObject.getInt("sendFrom");
            int receiverDbId = rootObject.getInt("sendTo");
            int notificationStatus = rootObject.getInt("playSound");
            String messageText = rootObject.getString("msgTex");
            String name = rootObject.getString("screenName");
            String eccId = rootObject.getString("eccId");
            int messageBurnTime = rootObject.getInt("msgBurn");
            int messageMimeType = rootObject.getInt("mimeType");
            int chatType = rootObject.getInt("mchatType");
            String messageId = rootObject.getString("messageId");
            int messsageStatus = rootObject.getInt("messsagestatus");
            date = DateTimeUtils.UTCDateTimeToLocalTime(rootObject.getString("MsgDateTime"));
            int chatListId = 0;
            if (db.checkMessageId(messageId, senderDbId)) {
                SocketUtils.sendAcknowledgementToSocket(rootObject, AppConstants.MESSAGE_STATUS_DELIVERED);
                return;
            } else if (db.checkParentMessageId(messageId, senderDbId)) {
                return;
            } else if (!db.checkUserHasFriend(eccId)) {
                return;
            } else {
                if ((!db.checkUserHaveChatList(eccId))) {
                    ChatListEntity chatEntity = new ChatListEntity();
                    chatEntity.setUserDbId(senderDbId);
                    chatEntity.setEccId(eccId);
                    chatEntity.setName(name);
                    chatEntity.setMessageTimeStamp(date);
                    chatEntity.setBurnTime(42);
                    chatEntity.setChatType(chatType);
                    NewMessageUserDbId = senderDbId;
                    chatListId = (int) db.insertChatList(chatEntity);
                    FileLog.e("ChatWindow_DbId ", String.valueOf(chatListId));
                    FileLog.i("Chat", "Single chat created successfully");
                }
                ChatListEntity chatListEntity = null;
                if (chatType == 0) {
                    chatListEntity = db.getChatEntity(senderDbId);
                    if (chatListId <= 0)
                        chatListId = chatListEntity.getId();
                    FileLog.e("ChatWindow DbId ", String.valueOf(chatListId));
                }
                entity.setChatId(chatListId);
                entity.setChatUserDbId(senderDbId);
                entity.setSenderId(senderDbId);
                entity.setReceiverId(receiverDbId);
                entity.setMessageType(AppConstants.MESSAGE_TYPE_FROM);
                entity.setMessageTimeStamp(date);
                entity.setEddId(eccId);
                entity.setMessageBurnTime(messageBurnTime);
                entity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, messageBurnTime));
                entity.setMessageMimeType(messageMimeType);
                entity.setMessageStatus(AppConstants.MESSAGE_UNREAD_STATUS);
                entity.setPlaySound(notificationStatus);
                entity.setMessageId(messageId);
                entity.setReply("false");
                if (chatType == 0) {
                    if (messageMimeType == AppConstants.MIME_TYPE_TEXT) {
                        entity.setMessage(Cryptography.decryptText(mContext, messageText));
                        int decrypt_db_id = (int) db.insertChatMessage(entity);
                        db.updateChatListTimeStamp(chatListEntity.getUserDbId(), entity.getMessageTimeStamp());
                        if (decrypt_db_id > 0) {
                            if (ChatWindowActivity.chatWindowFunctionListener != null && AppConstants.openedChatID == chatListEntity.getId()) {
                                ChatWindowActivity.chatWindowFunctionListener.onNewMessage(entity);
                            } else {
                                SocketUtils.sendAcknowledgementToSocket(rootObject, AppConstants.MESSAGE_STATUS_DELIVERED);
                                if (chatListEntity.getSnoozeStatus() == AppConstants.NOTIFICATION_SNOOZE_NO) {
                                    NotificationUtils.showNotification(mContext, chatListEntity, AppConstants.NOTIFICATION_ID, mContext.getResources().getString(R.string.title_message_notification), db.getTotalUnreadMessages());
                                }
                            }
                        }
                        if (FragmentChats.refreshChatListListener != null) {
                            FragmentChats.refreshChatListListener.onRefresh();
                        }
                        if (FragmentGroupChat.refreshChatListListener != null) {
                            FragmentGroupChat.refreshChatListListener.onRefresh();
                        }
                    } else {
                        if (db.checkMessageId(messageId, senderDbId)) {
                            return;
                        }
                        new DownloadFileFromURL(mContext, chatListEntity, entity).execute(messageText);
                    }
                }
            }
            NewMessageUserDbId = -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static synchronized void updateSingleChatMessage(final Context mContext, final JSONObject rootObject) {
        DbHelper db = new DbHelper(mContext);
        ChatMessageEntity entity = new ChatMessageEntity();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
        String date = df.format(Calendar.getInstance().getTime());
        try {
            int senderDbId = rootObject.getInt("sendFrom");
            int receiverDbId = rootObject.getInt("sendTo");
            int notificationStatus = rootObject.getInt("playSound");
            String messageText = rootObject.getString("msgTex");
            String name = rootObject.getString("screenName");
            String eccId = rootObject.getString("eccId");
            int messageBurnTime = rootObject.getInt("msgBurn");
            int messageMimeType = rootObject.getInt("mimeType");
            int chatType = rootObject.getInt("mchatType");
            String messageId = rootObject.getString("messageId");
            String parentMessageId = rootObject.getString("parentMessageID");
            int messsageStatus = rootObject.getInt("messagestatus");
            date = DateTimeUtils.UTCDateTimeToLocalTime(rootObject.getString("MsgDateTime"));
            int chatListId = 0;
            if (db.checkMessageId(messageId, senderDbId)) {
                return;
            } else if (!db.checkMessageId(parentMessageId, senderDbId)) {
                return;
            } else if (!db.checkUserHasFriend(eccId)) {
                return;
            } else {
                if ((!db.checkUserHaveChatList(eccId)))
                    return;
                ChatListEntity chatListEntity = null;
                if (chatType == 0) {
                    chatListEntity = db.getChatEntity(senderDbId);
                    if (chatListId <= 0)
                        chatListId = chatListEntity.getId();
                    FileLog.e("ChatWindow DbId ", String.valueOf(chatListId));
                }
                entity.setChatId(chatListId);
                entity.setChatUserDbId(senderDbId);
                entity.setSenderId(senderDbId);
                entity.setReceiverId(receiverDbId);
                entity.setMessageType(AppConstants.MESSAGE_TYPE_FROM);
                entity.setEditedMessageTimeStamp(date);
                entity.setMessageBurnTime(messageBurnTime);
                entity.setMessageBurnTimeStamp(DateTimeUtils.getMessageDestructionTimeByBurnTime(mContext, messageBurnTime));
                entity.setMessageMimeType(messageMimeType);
                entity.setEddId(eccId);
                entity.setMessageStatus(AppConstants.MESSAGE_UNREAD_STATUS);
                entity.setPlaySound(notificationStatus);
                entity.setMessageId(messageId);
                entity.setParentMessageId(parentMessageId);
                entity.setIsRevised(AppConstants.revised);
                entity.setReply("false");
                if (chatType == 0) {
                    if (messageMimeType == AppConstants.MIME_TYPE_TEXT) {
                        entity.setMessage(Cryptography.decryptText(mContext, messageText));
                        db.updateParentMessageIDByMessageId(entity.getParentMessageId(), entity.getParentMessageId());
                        db.updateMessageIDByParentMessageId(entity.getMessageId(), entity.getParentMessageId());
                        db.updateMessageBurnDate(entity.getMessageId(), entity.getMessageBurnTimeStamp());
                        db.updateEditedMessageTimeStamp(entity.getMessageId(), entity.getEditedMessageTimeStamp());
                        db.updateIsRevised(entity.getMessageId(), AppConstants.revised);
                        db.updateMessageTextByMessageId(entity.getMessageId(), entity.getMessage());
                        if (ChatWindowActivity.chatWindowFunctionListener != null && AppConstants.openedChatID == chatListEntity.getId()) {
                            ChatWindowActivity.chatWindowFunctionListener.onNewMessage(entity);
                        } else {
                            db.updateMessageStatusByMessageId(entity.getMessageId(), AppConstants.MESSAGE_UNREAD_STATUS);
                            SocketUtils.sendAcknowledgementToSocket(rootObject, AppConstants.MESSAGE_STATUS_DELIVERED);
                        }
                        if (FragmentChats.refreshChatListListener != null) {
                            FragmentChats.refreshChatListListener.onRefresh();
                        }
                        if (FragmentGroupChat.refreshChatListListener != null) {
                            FragmentGroupChat.refreshChatListListener.onRefresh();
                        }
                    }
                }
            }
            NewMessageUserDbId = -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void messageDelivered(Context mContext, JSONObject rootObject) {
        DbHelper dbHelper = new DbHelper(mContext);
        try {
            String msgId = rootObject.getString("messageId");
            int status = rootObject.getInt("messsagestatus");
            int sendFrom = rootObject.getInt("sendFrom");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static synchronized void decryptNewMessages(Context mContext, ChatMessageEntity chatMessageEntity) {
        DbHelper dbHelper = new DbHelper(mContext);
        chatMessageEntity.setCurrentMessageStatus(AppConstants.MESSAGE_UNREAD_STATUS);
        dbHelper.updateMessage(chatMessageEntity.getMessageId(), "messageBurnTime", AppConstants.MESSAGE_UNREAD_STATUS);
        dbHelper.updateMessageStatusByMessageId(chatMessageEntity.getMessageId(), AppConstants.MESSAGE_UNREAD_STATUS);
        if (FragmentChats.refreshChatListListener != null) {
            FragmentChats.refreshChatListListener.onRefresh();
        }
    }
    public static void updateGroupDetails(Context mContext, JSONObject rootObject) {
        DbHelper db = new DbHelper(mContext);
        int burnTime = 42;
        DateFormat df = new SimpleDateFormat("h:mm a", Locale.ROOT);
        String date = df.format(Calendar.getInstance().getTime());
        String gID = null;
        String name = null;
        try {
            gID = rootObject.getString("groupId");
            name = rootObject.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ChatListEntity entity = new ChatListEntity();
        entity.setUserDbId(Integer.parseInt(gID));
        entity.setName(name);
        entity.setEccId(String.valueOf(System.currentTimeMillis()));
        entity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
        entity.setBurnTime(burnTime);
        entity.setSnoozeStatus(AppConstants.NOTIFICATION_SNOOZE_NO);
        entity.setChatType(AppConstants.GROUP_CHAT_TYPE);
        long gId = db.insertChatList(entity);
        try {
            name = rootObject.getString("screenName");
            String eccId = rootObject.getString("eccId");
            int groupAdminUserId = rootObject.getInt("creatorId");
            int groupId = rootObject.getInt("groupId");
            GroupMemberEntity adminEntity = new GroupMemberEntity();
            adminEntity.setUserDbId(groupAdminUserId);
            adminEntity.setName(name);
            adminEntity.setEccId(eccId);
            adminEntity.setMemberType(AppConstants.GROUP_ADMIN);
            adminEntity.setChatId(groupId);
            if (!db.checkGroupMember(groupId, eccId))
                db.insertGroupMember(adminEntity);
            JSONArray memberArray = rootObject.getJSONArray("members");
            for (int i = 0; i < memberArray.length(); i++) {
                JSONObject dataObject = memberArray.getJSONObject(i);
                GroupMemberEntity memberEntity = new GroupMemberEntity();
                memberEntity.setUserDbId(dataObject.getInt("userId"));
                memberEntity.setName(dataObject.getString("screenName"));
                memberEntity.setEccId(dataObject.getString("eccId"));
                memberEntity.setMemberType(AppConstants.GROUP_MEMBER);
                memberEntity.setChatId(groupId);
                if (!db.checkGroupMember(groupId, memberEntity.getEccId()))
                    db.insertGroupMember(memberEntity);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (FragmentChats.refreshChatListListener != null)
            FragmentChats.refreshChatListListener.onRefresh();

        if (FragmentGroupChat.refreshChatListListener != null) {
            FragmentGroupChat.refreshChatListListener.onRefresh();
        }
    }

    public static synchronized void saveGroupMessage(Context mContext, JSONObject rootObject) {
        try {
            DbHelper db = new DbHelper(mContext);
            DateFormat df = new SimpleDateFormat("h:mm a", Locale.ROOT);
            String date = DateTimeUtils.UTCDateTimeToLocalTime(rootObject.getString("MsgDateTime"));
            int groupId = rootObject.getInt("groupId");
            int senderId = rootObject.getInt("senderId");
            int notificationStatus = rootObject.getInt("playSound");
            String eccId = rootObject.getString("eccId");
            String screenName = rootObject.getString("screenName");
            String msgText = rootObject.getString("msgTex");
            int burnTime = rootObject.getInt("msgBurn");
            int mimeType = rootObject.getInt("mimeType");
            String messageId = rootObject.getString("messageId");
            if (db.checkMessageId(messageId)) {
                SocketUtils.sendAcknowledgementToSocketForGroup(rootObject.put("sendFrom", User_settings.getUserId(mContext)), AppConstants.MESSAGE_STATUS_DELIVERED);
                return;
            }
            Log.e("saveGroupMessage: ", messageId);
            ChatListEntity chatListEntity = db.getGroupChatEntity(groupId);
            ChatMessageEntity entity = new ChatMessageEntity();
            entity.setSenderId(senderId);
            entity.setChatUserDbId(groupId);
            entity.setMessageId(messageId);
            entity.setMessageType(AppConstants.MESSAGE_TYPE_FROM);
            entity.setMessageTimeStamp(date);
            entity.setName(screenName);
            entity.setMessageBurnTime(burnTime);
            entity.setEddId(eccId);
            entity.setMessageMimeType(mimeType);
            entity.setMessageStatus(AppConstants.MESSAGE_UNREAD_STATUS);
            entity.setChatType(AppConstants.GROUP_CHAT_TYPE);
            entity.setChatId(chatListEntity.getId());
            entity.setPlaySound(notificationStatus);
            if (mimeType == AppConstants.MIME_TYPE_TEXT) {
                entity.setMessage(Cryptography.decryptText(mContext, msgText));
                db.updateChatListTimeStamp(groupId, date);
                int db_id = (int) db.insertChatMessage(entity);
                if (db_id > 0) {
                    if (GroupChatWindowActivity.chatWindowFunctionListener != null && AppConstants.openedChatID == chatListEntity.getId()) {
                        GroupChatWindowActivity.chatWindowFunctionListener.onNewMessage(entity);
                    } else {
                        if (chatListEntity.getSnoozeStatus() == AppConstants.NOTIFICATION_SNOOZE_NO) {
                            NotificationUtils.showNotification(mContext, chatListEntity, AppConstants.NOTIFICATION_ID, mContext.getResources().getString(R.string.title_message_notification), db.getTotalUnreadMessages());
                        }
                    }
                }
                if (FragmentChats.refreshChatListListener != null) {
                    FragmentChats.refreshChatListListener.onRefresh();
                }
                if (FragmentGroupChat.refreshChatListListener != null) {
                    FragmentGroupChat.refreshChatListListener.onRefresh();
                }
            } else {
                if (db.checkMessageId(messageId, entity.getChatUserDbId())) {
                    return;
                }
                new DownloadFileFromURL(mContext, chatListEntity, entity).execute(msgText);
            }
            SocketUtils.sendAcknowledgementToSocketForGroup(rootObject.put("sendFrom", User_settings.getUserId(mContext)), AppConstants.MESSAGE_STATUS_DELIVERED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void updateGroupMessage(Context mContext, JSONObject rootObject) {
        try {
            DbHelper db = new DbHelper(mContext);
            DateFormat df = new SimpleDateFormat("h:mm a", Locale.ROOT);
            String date = DateTimeUtils.UTCDateTimeToLocalTime(rootObject.getString("msgDateTime"));
            int groupId = rootObject.getInt("groupId");
            int senderId = rootObject.getInt("senderId");
            int notificationStatus = rootObject.getInt("playSound");
            String eccId = rootObject.getString("eccId");
            String screenName = rootObject.getString("screenName");
            String msgText = rootObject.getString("msgTex");
            int burnTime = rootObject.getInt("msgBurn");
            int mimeType = rootObject.getInt("mimeType");
            String messageId = rootObject.getString("messageId");
            String parentMesageId = rootObject.getString("parentMessageID");
            if (db.checkMessageId(messageId)) {
                return;
            } else if (!db.checkMessageId(parentMesageId)) {
                return;
            }
            ChatListEntity chatListEntity = db.getGroupChatEntity(groupId);
            ChatMessageEntity entity = new ChatMessageEntity();
            entity.setSenderId(senderId);
            entity.setChatUserDbId(groupId);
            entity.setMessageId(messageId);
            entity.setParentMessageId(parentMesageId);
            entity.setMessageType(AppConstants.MESSAGE_TYPE_FROM);
            entity.setEditedMessageTimeStamp(date);
            entity.setName(screenName);
            entity.setMessageBurnTime(burnTime);
            entity.setEddId(eccId);
            entity.setMessageMimeType(mimeType);
            entity.setMessageStatus(AppConstants.MESSAGE_UNREAD_STATUS);
            entity.setChatType(AppConstants.GROUP_CHAT_TYPE);
            entity.setChatId(chatListEntity.getId());
            entity.setPlaySound(notificationStatus);
            if (mimeType == AppConstants.MIME_TYPE_TEXT) {
                entity.setMessage(Cryptography.decryptText(mContext, msgText));
                db.updateParentMessageIDByMessageId(entity.getParentMessageId(), entity.getParentMessageId());
                db.updateMessageIDByParentMessageId(entity.getMessageId(), entity.getParentMessageId());
                db.updateMessageBurnDate(entity.getMessageId(), entity.getMessageBurnTimeStamp());
                db.updateEditedMessageTimeStamp(entity.getMessageId(), entity.getEditedMessageTimeStamp());
                db.updateIsRevised(entity.getMessageId(), AppConstants.revised);
                db.updateMessageTextByMessageId(entity.getMessageId(), entity.getMessage());
                if (GroupChatWindowActivity.chatWindowFunctionListener != null && AppConstants.openedChatID == chatListEntity.getId()) {
                    GroupChatWindowActivity.chatWindowFunctionListener.onNewMessage(entity);
                }
                if (FragmentChats.refreshChatListListener != null) {
                    FragmentChats.refreshChatListListener.onRefresh();
                }
                if (FragmentGroupChat.refreshChatListListener != null) {
                    FragmentGroupChat.refreshChatListListener.onRefresh();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void friendRequestResponse(Context mContext, JSONObject rootObject) {
        DbHelper db = new DbHelper(mContext);
        try {
            int status = SocketUtils.request;
            String eccId = rootObject.getString("eccId");
            if (db.checkContact(rootObject.getInt("sendFrom"))) {
                db.updateContactEntity(DbConstants.KEY_BLOCK_STATUS, status, DbConstants.KEY_USER_DB_ID, rootObject.getInt("sendFrom"));
            } else {
                ContactEntity entity = new ContactEntity();
                entity.setUserDbId(rootObject.getInt("sendFrom"));
                entity.setEccId(rootObject.getString("eccId"));
                entity.setUserType(0);
                entity.setName(rootObject.getString("screenName"));
                entity.setBlockStatus(String.valueOf(SocketUtils.request));
                db.insertContactList(entity);
            }
            if (FragmentContacts.socketContactResponse != null)
                FragmentContacts.socketContactResponse.onSocketResponse();
            //insert socket request to database
            SocketRequestEntity entity = new SocketRequestEntity();
            entity.setRequestType(rootObject.getInt("response"));
            entity.setUniqueId(rootObject.getString("messageId"));
            db.insertSocketRequest(entity);
            SocketUtils.sendAcknowledgementMessage(Integer.parseInt(User_settings.getUserId(mContext)), rootObject.getString("messageId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void friendRequestAccepted(Context mContext, JSONObject rootObject) {
        DbHelper db = new DbHelper(mContext);
        try {
            int status = SocketUtils.request;
            if (rootObject.getInt("status") == 0) {
                db.deleteContact(rootObject.getString("eccId"));
                if (FragmentContacts.socketContactResponse != null)
                    FragmentContacts.socketContactResponse.onSocketResponse();
                return;
            } else if (rootObject.getInt("status") == 1) {
                status = SocketUtils.accepted;
            }
            db.updateContactEntity(DbConstants.KEY_BLOCK_STATUS, status, DbConstants.KEY_USER_DB_ID, rootObject.getInt("sendFrom"));
            //insert socket request to database
             SocketRequestEntity entity = new SocketRequestEntity();
            entity.setRequestType(rootObject.getInt("response"));
            entity.setUniqueId(rootObject.getString("messageId"));
            db.insertSocketRequest(entity);
            if (FragmentContacts.socketContactResponse != null){
                FragmentContacts.socketContactResponse.onSocketResponse();}
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void removeGroupMember(Context mContext, JSONObject rootObject) {

        try {
            int groupId = rootObject.getInt("groupId");

            JSONArray members = rootObject.getJSONArray("members");

            JSONObject jsonObject = members.getJSONObject(0);
            int userId = jsonObject.getInt("userId");
            String eccId = jsonObject.getString("eccId");

            new DbHelper(mContext).deleteGroupMember(groupId, eccId);
            if (GroupDetailActivity.groupUpdateListener != null){
                GroupDetailActivity.groupUpdateListener.onMemberRemove(eccId, userId, groupId);}
            else if (GroupChatWindowActivity.groupUpdateListener != null){
                GroupChatWindowActivity.groupUpdateListener.onMemberRemove(eccId, userId, groupId);}

            if (FragmentChats.refreshChatListListener != null) {
                FragmentChats.refreshChatListListener.onRefresh();
            }
            if (FragmentGroupChat.refreshChatListListener != null) {
                FragmentGroupChat.refreshChatListListener.onRefresh();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void addGroupMember(Context mContext, JSONObject rootObject) {
        try {
            int groupId = rootObject.getInt("groupId");
            DbHelper dbHelper = new DbHelper(mContext);
            if (!dbHelper.checkUserHaveChatList(groupId)) {
                ChatListEntity chatListEntity = new ChatListEntity();
                chatListEntity.setBurnTime(42);
                chatListEntity.setName("fetching group details...");
                chatListEntity.setUserDbId(groupId);
                chatListEntity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
                chatListEntity.setChatType(AppConstants.GROUP_CHAT_TYPE);
                chatListEntity.setEccId(DateTimeUtils.getCurrentTimeMilliseconds());
                dbHelper.insertChatList(chatListEntity);
                if (FragmentChats.refreshChatListListener != null){
                    FragmentChats.refreshChatListListener.onRefresh();}
                if (FragmentGroupChat.refreshChatListListener != null) {
                    FragmentGroupChat.refreshChatListListener.onRefresh();
                }
                getGroupInformation(mContext, groupId, dbHelper);
            } else {
                getGroupInformation(mContext, groupId, dbHelper);
                JSONArray members = rootObject.getJSONArray("members");
                for (int i = 0; i < members.length(); i++) {
                    JSONObject jsonObject = members.getJSONObject(i);
                    int userId = jsonObject.getInt("userId");
                    String eccId = jsonObject.getString("eccId");
                    String screenName = jsonObject.getString("screenName");
                    String ecc_key = jsonObject.getString("ecc_key");
                    if (!dbHelper.checkGroupMember(groupId, eccId)) {
                        GroupMemberEntity entity = new GroupMemberEntity();
                        entity.setChatId(groupId);
                        entity.setEccId(eccId);
                        entity.setEccPublicKey(ecc_key);
                        entity.setMemberType(AppConstants.GroupMember);
                        entity.setName(screenName);
                        entity.setUserDbId(userId);
                        dbHelper.insertGroupMember(entity);
                    }
                }
                if (GroupDetailActivity.groupUpdateListener != null)
                    GroupDetailActivity.groupUpdateListener.onMemberAdd();
                else if (GroupChatWindowActivity.groupUpdateListener != null)
                    GroupChatWindowActivity.groupUpdateListener.onMemberAdd();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private static void getGroupInformation(Context mContext, int groupId, DbHelper dbHelper) {
        AndroidNetworking.post(ApiEndPoints.END_POINT_GET_GROUP_DETAIL)
                .addBodyParameter("group_id", String.valueOf(groupId))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(response.toString());
                            Log.e("onResponse: ", rootObject.toString());
                            if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                JSONObject result = rootObject.getJSONObject("result");
                                String groupName = result.getString("Name");
                                JSONArray members = result.getJSONArray("Members array");
                                dbHelper.deleteGroupMember(groupId);
                                for (int i = 0; i < members.length(); i++) {
                                    JSONObject memberObject = members.getJSONObject(i);
                                    String ecc_id = memberObject.getString("ecc_id");
                                    String screen_name = memberObject.getString("screen_name");
                                    String member_type = memberObject.getString("member_type");
                                    String ecc_public_key = memberObject.getString("member_ecc_key");
                                    int member_user_id = memberObject.getInt("user_id");
                                    GroupMemberEntity entity = new GroupMemberEntity();
                                    entity.setChatId(groupId);
                                    entity.setEccId(ecc_id);
                                    entity.setEccPublicKey(ecc_public_key);
                                    if (member_type.equalsIgnoreCase("1"))
                                        entity.setMemberType(AppConstants.GroupAdmin);
                                    else
                                        entity.setMemberType(AppConstants.GroupMember);
                                    entity.setName(screen_name);
                                    entity.setUserDbId(member_user_id);
                                    dbHelper.insertGroupMember(entity);
                                    dbHelper.updateGroupList(groupName, groupId);
                                    if (FragmentChats.refreshChatListListener != null)
                                        FragmentChats.refreshChatListListener.onRefresh();
                                    if (FragmentGroupChat.refreshChatListListener != null) {
                                        FragmentGroupChat.refreshChatListListener.onRefresh();
                                    }
                                    if (GroupDetailActivity.groupUpdateListener != null)
                                        GroupDetailActivity.groupUpdateListener.onMemberAdd();
                                    else if (GroupChatWindowActivity.groupUpdateListener != null)
                                        GroupChatWindowActivity.groupUpdateListener.onMemberAdd();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e("onError: ", error.getErrorBody());
                    }
                });
    }
    public static void removeContacts(Context mContext, JSONObject rootObject) {
        DbHelper db = new DbHelper(mContext);
        try {
            String eccid = rootObject.getString("eccId");
            db.deleteContact(eccid);
            db.deletePublicKey(eccid);
            //insert socket request to database
            SocketRequestEntity entity = new SocketRequestEntity();
            entity.setRequestType(rootObject.getInt("response"));
            entity.setUniqueId(rootObject.getString("messageId"));
            db.insertSocketRequest(entity);
            if (FragmentContacts.socketContactResponse != null)
                FragmentContacts.socketContactResponse.onSocketResponse();
            if (ChatWindowActivity.friendRequestResponse != null)
                ChatWindowActivity.friendRequestResponse.onUnFriend(eccid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void changeGroupName(Context mContext, JSONObject rootObject) {
        DbHelper db = new DbHelper(mContext);
        try {
            String updatedGroupName = rootObject.getString("updatedname");
            int groupId = rootObject.getInt("groupId");
            AndroidNetworking.post(ApiEndPoints.END_POINT_GET_GROUP_DETAIL)
                    .addBodyParameter("group_id", String.valueOf(groupId))
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject rootObject = new JSONObject(response.toString());
                                Log.e("onResponse: ", rootObject.toString());
                                if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                    JSONObject result = rootObject.getJSONObject("result");
                                    String groupName = result.getString("Name");
                                    db.updateGroupChatListName(groupId, groupName);
                                    String eccId = db.getEccId(groupId);
                                    db.updateVaultItemName(groupName, eccId);
                                    if (FragmentChats.refreshChatListListener != null){
                                        FragmentChats.refreshChatListListener.onRefresh();}
                                    if (FragmentGroupChat.refreshChatListListener != null) {
                                        FragmentGroupChat.refreshChatListListener.onRefresh();
                                    }
                                    if (FragmentVault.addVaultItemResponse != null){
                                        FragmentVault.addVaultItemResponse.onChanged();}
                                    if (GroupDetailActivity.groupUpdateListener != null){
                                        GroupDetailActivity.groupUpdateListener.onNameChange();}
                                    else if (GroupChatWindowActivity.groupUpdateListener != null){
                                        GroupChatWindowActivity.groupUpdateListener.onNameChange();}
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onError(ANError error) {
                            Log.e("onError: ", error.getErrorBody());
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void createGroupAgain(Context mContext, JSONObject rootObjectMain) {
        DbHelper dbHelper = new DbHelper(mContext);
        String groupId;
        try {
            groupId = rootObjectMain.getString("groupId");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        AndroidNetworking.post(ApiEndPoints.END_POINT_GET_GROUP_DETAIL)
                .addBodyParameter("group_id", groupId)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(response.toString());
                            Log.e("onResponse: ", rootObject.toString());
                            if (rootObject.getString("status").equalsIgnoreCase("1")) {

                                JSONObject result = rootObject.getJSONObject("result");
                                String groupName = result.getString("Name");
                                long gId = -1;
                                ChatListEntity entity = new ChatListEntity();
                                entity.setUserDbId(Integer.parseInt(groupId));
                                entity.setName(groupName);
                                entity.setEccId(String.valueOf(System.currentTimeMillis()));
                                entity.setMessageTimeStamp(DateTimeUtils.getCurrentDateTime());
                                entity.setBurnTime(45);
                                entity.setSnoozeStatus(AppConstants.NOTIFICATION_SNOOZE_NO);
                                entity.setChatType(AppConstants.GROUP_CHAT_TYPE);
                                if (dbHelper.checkIfGroupExist(Integer.parseInt(groupId))) {
                                    dbHelper.updateGroupChatListName(Integer.parseInt(groupId), groupName);
                                } else
                                    dbHelper.insertChatList(entity);
                                JSONArray members = result.getJSONArray("Members array");
                                for (int i = 0; i < members.length(); i++) {
                                    JSONObject memberObject = members.getJSONObject(i);
                                    String ecc_id = memberObject.getString("ecc_id");
                                    String screen_name = memberObject.getString("screen_name");
                                    String member_type = memberObject.getString("member_type");
                                    String ecc_public_key = memberObject.getString("member_ecc_key");
                                    int member_user_id = memberObject.getInt("user_id");
                                    GroupMemberEntity entity1 = new GroupMemberEntity();
                                    entity1.setChatId(Integer.parseInt(groupId));
                                    entity1.setEccId(ecc_id);
                                    entity1.setEccPublicKey(ecc_public_key);
                                    if (member_type.equalsIgnoreCase("1"))
                                        entity1.setMemberType(AppConstants.GroupAdmin);
                                    else
                                        entity1.setMemberType(AppConstants.GroupMember);
                                    entity1.setName(screen_name);
                                    entity1.setUserDbId(member_user_id);
                                    if (!dbHelper.checkGroupMember(Integer.parseInt(groupId), ecc_id))
                                        dbHelper.insertGroupMember(entity1);
                                }
                                if (dbHelper.checkIfGroupExist(Integer.parseInt(groupId)))
                                    saveGroupMessage(mContext, rootObjectMain);
                                if (FragmentChats.refreshChatListListener != null){
                                    FragmentChats.refreshChatListListener.onRefresh();}
                                if (FragmentGroupChat.refreshChatListListener != null) {
                                    FragmentGroupChat.refreshChatListListener.onRefresh();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e("onError: ", error.getErrorBody());
                    }
                });
    }
    public static void deleteMessagesForSingleChat(Context context, JSONObject rootObject) {
        DbHelper dbHelper = new DbHelper(context);
        JSONArray messageIds;
        try {
            messageIds = rootObject.getJSONArray("message_id");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        try {
            for (int i = 0; i < messageIds.length(); i++) {
                dbHelper.updateMimeTime(messageIds.getString(i), AppConstants.MIME_TYPE_DELETE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (ChatWindowActivity.chatWindowFunctionListener != null)
            ChatWindowActivity.chatWindowFunctionListener.onDeleteMessage(null);
    }
    public static void deleteMessagesForGroupChat(Context context, JSONObject rootObject) {
        DbHelper dbHelper = new DbHelper(context);
        JSONArray messageIds;
        try {
            messageIds = rootObject.getJSONArray("message_id");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        try {
            for (int i = 0; i < messageIds.length(); i++) {
                dbHelper.updateMimeTime(messageIds.getString(i), AppConstants.MIME_TYPE_DELETE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (GroupChatWindowActivity.chatWindowFunctionListener != null)
            GroupChatWindowActivity.chatWindowFunctionListener.onDeleteMessage(null);
    }
}
