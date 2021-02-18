package com.realapps.chat.model;

import java.io.Serializable;

/**
 * Created by Prashant Sharma on 3/15/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class ChatListEntity implements Serializable {

    private int blockStatus = 0;
    private int snoozeStatus = 0;
    private String messageTimeStamp = "";
    private int chatType = 0;
    private boolean isSelected = false;
    private String snoozeTimeStamp = "";
    private String lastMessageTime = "";
    private int lastMessageStatus = 0;
    private int lastMessageType = 0;
    private int id = 0;
    private int userDbId = 0;
    private String eccId = "";
    private String name = "";
    private String createdDateTime = "";
    private int burnTime = 0;
    private boolean isFriend;

    public ChatListEntity() {
    }

    public ChatListEntity(int id, int userDbId, String eccId, String name) {
        this.id = id;
        this.eccId = eccId;
        this.name = name;
    }
    public String getEccId() {
        return eccId;
    }

    public void setEccId(String eccId) {
        this.eccId = eccId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBlockStatus() {
        return blockStatus;
    }

    public void setBlockStatus(int blockStatus) {
        this.blockStatus = blockStatus;
    }

    public int getSnoozeStatus() {
        return snoozeStatus;
    }

    public void setSnoozeStatus(int snoozeStatus) {
        this.snoozeStatus = snoozeStatus;
    }

    public String getMessageTimeStamp() {
        return messageTimeStamp;
    }

    public void setMessageTimeStamp(String messageTimeStamp) {
        this.messageTimeStamp = messageTimeStamp;
    }

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public String getSnoozeTimeStamp() {
        return snoozeTimeStamp;
    }

    public void setSnoozeTimeStamp(String snoozeTimeStamp) {
        this.snoozeTimeStamp = snoozeTimeStamp;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
    public int getLastMessageStatus() {
        return lastMessageStatus;
    }

    public void setLastMessageStatus(int lastMessageStatus) {
        this.lastMessageStatus = lastMessageStatus;
    }
    public int getLastMessageType() {
        return lastMessageType;
    }

    public void setLastMessageType(int lastMessageType) {
        this.lastMessageType = lastMessageType;
    }

    public int getUserDbId() {
        return userDbId;
    }

    public void setUserDbId(int userDbId) {
        this.userDbId = userDbId;
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(String createdDateTime) {
        this.createdDateTime = createdDateTime;
    }
    public int getBurnTime() {
        return burnTime;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = burnTime;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
