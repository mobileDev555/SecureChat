package com.realapps.chat.services;

import android.content.Context;
import android.util.Log;

import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.notification.MyFCMClass;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.FileLog;
import com.realapps.chat.utils.MessagesUtils;
import com.realapps.chat.utils.NetworkUtils;
import com.realapps.chat.utils.NotificationUtils;
import com.realapps.chat.utils.SocketUtils;
import com.realapps.chat.view.home.activity.ChatWindowActivity;
import com.realapps.chat.view.home.activity.GroupChatWindowActivity;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;


/**
 * Created by Prashant Sharma on 3/29/2017.
 * Core techies
 * prashant@coretechies.org
 */

public class MessageClass {

    DbHelper db;
    int myId;
    Context mContext;
    boolean isConnected = false;
    boolean isConnecting = false;

    public MessageClass(Context mContext) {

        this.mContext = mContext;
        db = new DbHelper(mContext);

        if (!(User_settings.isUserLogin(mContext)))
            return;
        try {
            if (NetworkUtils.isNetworkConnected(mContext)) {
                myId = Integer.parseInt(User_settings.getUserId(mContext));
                if (AppConstants.mWebSocketClient != null) {
                    try {
                        Socket socket = AppConstants.mWebSocketClient.getSocket();
                        WebSocket connection = AppConstants.mWebSocketClient.getConnection();
                        if (socket != null && !socket.isInputShutdown() && !socket.isOutputShutdown() && !socket.isClosed() && socket.isConnected()) {
                            if (connection != null && connection.isOpen() && !connection.isClosed()) {
                                if (socket.getInputStream() != null && socket.getOutputStream() != null) {
                                    try {
                                        if (AppConstants.mWebSocketClient.getConnection() != null && AppConstants.mWebSocketClient.getConnection().isOpen()) {
//                                            FileLog.sout("PacketByte : " + String.valueOf(myId).getBytes().length);
//                                            FileLog.sout("MyId: " + String.valueOf(myId));
                                            AppConstants.mWebSocketClient.send(String.valueOf(myId).getBytes());
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        AppConstants.mWebSocketClient = null;
                                        connectWebSocket();
                                    }
                                } else {
                                    AppConstants.mWebSocketClient = null;
                                    connectWebSocket();
                                }
                            } else {
                                AppConstants.mWebSocketClient = null;
                                connectWebSocket();
                            }
                        } else {
                            AppConstants.mWebSocketClient = null;
                            connectWebSocket();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        AppConstants.mWebSocketClient = null;
                        connectWebSocket();
                    }
                } else {
                    connectWebSocket();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI(SocketUtils.SocketUrl + myId);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        AppConstants.mWebSocketClient = new WebSocketClient(uri, new Draft_6455()) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                FileLog.e("WebSocket", "Opened chat receiver");
                SocketUtils.sendDeviceTokenToSocket(mContext, 1);
                sendReadAckForNotSendMessages();
                isConnecting = false;
            }
            @Override
            public void onMessage(String message) {
                try {
                    int response;
                    if (!message.isEmpty()) {
                        JSONObject rootObject = new JSONObject(message);
                        response = rootObject.getInt("response");
                        if (response == SocketUtils.RespSingleMSg) {
                            MyFCMClass.elapseTime = 0;
                            rootObject.put("mchatType", AppConstants.SINGLE_CHAT_TYPE);
                            String db_id = rootObject.getString("eccId");
                            String support_db_id = User_settings.getSupportEccId(mContext);
                            if (CommonUtils.isSubscriptionValid(mContext)) {
                                MessagesUtils.saveSingleChatMessage(mContext, rootObject);
                            } else if (db_id.equalsIgnoreCase(support_db_id)) {
                                MessagesUtils.saveSingleChatMessage(mContext, rootObject);
                                SocketUtils.sendAcknowledgementToSocket(rootObject, AppConstants.MESSAGE_STATUS_DELIVERED);
                            }
                        } else if (response == SocketUtils.RespSingleRMSg) {
                            MyFCMClass.elapseTime = 0;
                            rootObject.put("mchatType", AppConstants.SINGLE_CHAT_TYPE);
                            MessagesUtils.updateSingleChatMessage(mContext, rootObject);
                        } else if (response == SocketUtils.RespAddGroup) {
                            MyFCMClass.elapseTime = 0;
                            rootObject.put("mchatType", AppConstants.GROUP_CHAT_TYPE);
                            if (CommonUtils.isSubscriptionValid(mContext)) {
                                MessagesUtils.updateGroupDetails(mContext, rootObject);
                            }
                        } else if (response == SocketUtils.RespMsgDelivered) {
                            MyFCMClass.elapseTime = 0;
                            synchronized (this) {
                                String msgId = rootObject.getString("messageId");
                                int status = rootObject.getInt("messsagestatus");
                                db.updateMessageStatusByMessageId(msgId, status);
                                if (ChatWindowActivity.chatWindowFunctionListener != null)
                                    ChatWindowActivity.chatWindowFunctionListener.onMessageAck(msgId, status);

                                if (GroupChatWindowActivity.chatWindowFunctionListener != null)
                                    GroupChatWindowActivity.chatWindowFunctionListener.onMessageAck(msgId, status);
                            }

                        } else if (response == SocketUtils.RespMsgSent) {
                            MyFCMClass.elapseTime = 0;
                            String msgId = rootObject.getString("messageId");
                            db.updateMessageStatusByMessageId(msgId, AppConstants.MESSAGE_SENT_STATUS);
                            if (ChatWindowActivity.chatWindowFunctionListener != null)
                                ChatWindowActivity.chatWindowFunctionListener.onMessageAck(msgId, AppConstants.MESSAGE_SENT_STATUS);
                            Log.e("onMessage: ", message);
                        } else if (response == 50) {
                            MyFCMClass.elapseTime = 0;
                            String msgId = rootObject.getString("messageId");
                            db.updateMessageStatusByMessageId(msgId, AppConstants.MESSAGE_SENT_STATUS);
                            if (ChatWindowActivity.chatWindowFunctionListener != null)
                                ChatWindowActivity.chatWindowFunctionListener.onMessageAck(msgId, AppConstants.MESSAGE_SENT_STATUS);
                            Log.e("onMessage: ", message);
                        } else if (response == SocketUtils.RespGetGroupMessage) {
                            if (CommonUtils.isSubscriptionValid(mContext)) {
                                int groupUserDbId = rootObject.getInt("groupId");
                                if (db.checkIfGroupExist(groupUserDbId)) {
                                    MessagesUtils.saveGroupMessage(mContext, rootObject);
                                } else {
                                    MessagesUtils.createGroupAgain(mContext, rootObject);
                                }

                            }
                        } else if (response == SocketUtils.RespGetRGroupMessage) {
                            if (CommonUtils.isSubscriptionValid(mContext)) {
                                int groupUserDbId = rootObject.getInt("groupId");
                                if (db.checkIfGroupExist(groupUserDbId)) {
                                    MessagesUtils.updateGroupMessage(mContext, rootObject);
                                }
                            }
                        } else if (response == SocketUtils.RespFriendRequest) {

                            synchronized (this) {
                                MyFCMClass.elapseTime = 0;

                                //check this request already get or not
                                if (db.isRequestExist(rootObject.getString("messageId")))
                                    return;

                                //============= sending request on Contacts
                                MessagesUtils.friendRequestResponse(mContext, rootObject);
                                NotificationUtils.showNotification(mContext, AppConstants.NOTIFICATION_ID, "", mContext.getString(R.string.you_have_one_new_friend_request));
                            }

                        } else if (response == SocketUtils.RespFriendRequestResponse) {
                            synchronized (this) {
                                MyFCMClass.elapseTime = 0;
                                //check this request already get or not
                                if (db.isRequestExist(rootObject.getString("messageId")))
                                    return;

                                MessagesUtils.friendRequestAccepted(mContext, rootObject);
                                if (rootObject.getInt("status") == 1) {
                                    NotificationUtils.showNotification(mContext, AppConstants.NOTIFICATION_ID, "", mContext.getString(R.string.friend_request_accepted_by_s, rootObject.getString("eccId")));
                                }
                            }


                        } else if (response == SocketUtils.RespRemoveMember) {
                            MyFCMClass.elapseTime = 0;
                            MessagesUtils.removeGroupMember(mContext, rootObject);
                        } else if (response == SocketUtils.RespLeaveGroup) {
                            MyFCMClass.elapseTime = 0;
                            MessagesUtils.removeGroupMember(mContext, rootObject);
                        } else if (response == SocketUtils.RespAddMember) {
                            MyFCMClass.elapseTime = 0;
                            MessagesUtils.addGroupMember(mContext, rootObject);
                        } else if (response == SocketUtils.RespDeleteConatact) {
                            MyFCMClass.elapseTime = 0;
                            synchronized (this) {
                                if (db.isRequestExist(rootObject.getString("messageId")))
                                    return;

                                MessagesUtils.removeContacts(mContext, rootObject);
                            }
                        } else if (response == SocketUtils.RespChangeGroupName) {
                            MyFCMClass.elapseTime = 0;
                            MessagesUtils.changeGroupName(mContext, rootObject);
                        }else if(response==62){
                            MyFCMClass.elapseTime = 0;
                            MessagesUtils.deleteMessagesForSingleChat(mContext, rootObject);
                        }else if(response==66){
                            MyFCMClass.elapseTime = 0;
                            MessagesUtils.deleteMessagesForGroupChat(mContext, rootObject);
                        }


                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    FileLog.e("====>WebSocket", "Error-0" + e.getMessage());
                    Log.e("====>WebSocket", "Error-0" + e.getMessage());
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                FileLog.e("====>WebSocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                FileLog.e("====>WebSocket", "Error " + e.getMessage());
                if (!isConnecting) {
                    AppConstants.mWebSocketClient = null;
                }
                e.printStackTrace();
            }

        };

        AppConstants.mWebSocketClient.connect();
        isConnected = true;
    }

    private void sendReadAckForNotSendMessages() {
        DbHelper dbHelper = new DbHelper(mContext);
        ArrayList<ChatMessageEntity> messageListNew;
        messageListNew = dbHelper.getReadUnSendAckMessages();
        for (int i = 0; i < messageListNew.size(); i++) {
            synchronized (this) {
                dbHelper.updateMessageStatusByMessageId(messageListNew.get(i).getMessageId(), AppConstants.MESSAGE_READ_STATUS);
                SocketUtils.sendREadAcknowledgementToSocket(mContext, messageListNew.get(i), AppConstants.MESSAGE_READ_STATUS);
            }
        }
    }


}
