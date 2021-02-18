package com.realapps.chat.model;

import com.realapps.chat.utils.AppConstants;

import java.io.Serializable;

/**
 * Created by Prashant Sharma on 3/15/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class ChatMessageEntity implements Serializable {
    private int id = 0;
    private int chatId = 0;
    private int chatType = 0;
    private int chatUserDbId = 0;
    private String name = "";
    private String messageId = "";
    private int senderId = 0;
    private int receiverId = 0;
    private String message = "";
    private int messageType = 0;
    private int messageStatus = 0;
    private String messageTimeStamp = "";
    private String editedMessageTimeStamp = "";
    private int messageBurnTime = 0;
    private String messageBurnTimeStamp = "";
    private int messageMimeType = 0;
    private String imagePath = "";
    private String audioPath = "";
    private String contactPath = "";
    private String filePath = "";
    private String videoPath = "";
    private int currentMessageStatus = 0;
    private String reply = "";
    private int favourite = 0;
    private int pinned = 0;
    private int visibility = 0;
    private String messageEncryptionKey = "";
    private String messageIv = "";
    private String messageSharedSecretKey = "";
    private String eddId = "";
    private int playSound = 0;
    private int isRevised = AppConstants.nonRevised;
    private String parentMessageId = messageId;
    private String fileName = "";
    private boolean selected = false;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getPlaySound() {
        return playSound;
    }

    public void setPlaySound(int playSound) {
        this.playSound = playSound;
    }

    public ChatMessageEntity() {
    }

    public String getEddId() {
        return eddId;
    }

    public void setEddId(String eddId) {
        this.eddId = eddId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public int getChatUserDbId() {
        return chatUserDbId;
    }

    public void setChatUserDbId(int chatUserDbId) {
        this.chatUserDbId = chatUserDbId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(int messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getMessageTimeStamp() {
        return messageTimeStamp;
    }

    public void setMessageTimeStamp(String messageTimeStamp) {
        this.messageTimeStamp = messageTimeStamp;
    }

    public int getMessageBurnTime() {
        return messageBurnTime;
    }

    public void setMessageBurnTime(int messageBurnTime) {
        this.messageBurnTime = messageBurnTime;
    }

    public String getMessageBurnTimeStamp() {
        return messageBurnTimeStamp;
    }

    public void setMessageBurnTimeStamp(String messageBurnTimeStamp) {
        this.messageBurnTimeStamp = messageBurnTimeStamp;
    }

    public int getMessageMimeType() {
        return messageMimeType;
    }

    public void setMessageMimeType(int messageMimeType) {
        this.messageMimeType = messageMimeType;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getContactPath() {
        return contactPath;
    }

    public void setContactPath(String contactPath) {
        this.contactPath = contactPath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public int getCurrentMessageStatus() {
        return currentMessageStatus;
    }

    public void setCurrentMessageStatus(int currentMessageStatus) {
        this.currentMessageStatus = currentMessageStatus;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public int getFavourite() {
        return favourite;
    }

    public void setFavourite(int favourite) {
        this.favourite = favourite;
    }

    public int getPinned() {
        return pinned;
    }

    public void setPinned(int pinned) {
        this.pinned = pinned;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public String getMessageEncryptionKey() {
        return messageEncryptionKey;
    }

    public void setMessageEncryptionKey(String messageEncryptionKey) {
        this.messageEncryptionKey = messageEncryptionKey;
    }

    public String getMessageIv() {
        return messageIv;
    }

    public void setMessageIv(String messageIv) {
        this.messageIv = messageIv;
    }

    public String getMessageSharedSecretKey() {
        return messageSharedSecretKey;
    }

    public void setMessageSharedSecretKey(String messageSharedSecretKey) {
        this.messageSharedSecretKey = messageSharedSecretKey;
    }

    public int getIsRevised() {
        return isRevised;
    }

    public void setIsRevised(int isRevised) {
        this.isRevised = isRevised;
    }

    public String getParentMessageId() {
        return parentMessageId;
    }

    public void setParentMessageId(String parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    public String getEditedMessageTimeStamp() {
        return editedMessageTimeStamp;
    }

    public void setEditedMessageTimeStamp(String editedMessageTimeStamp) {
        this.editedMessageTimeStamp = editedMessageTimeStamp;
    }
}
