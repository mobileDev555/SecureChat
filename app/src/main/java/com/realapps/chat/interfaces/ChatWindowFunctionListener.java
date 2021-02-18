package com.realapps.chat.interfaces;

import com.realapps.chat.model.ChatMessageEntity;

/**
 * Created by Prashant Sharma on 3/16/2018.
 * Core techies
 * prashant@coretechies.org
 */

public interface ChatWindowFunctionListener {

    void onNewMessage(ChatMessageEntity chatMessageEntity);

    void onMessageAck(String messageId,int status);

    void onDeleteMessage(ChatMessageEntity chatMessageEntity);

    void onDeleteMessageByMessageId(String messageId);

}
