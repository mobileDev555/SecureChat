package com.realapps.chat.model;

import java.io.Serializable;

/**
 * Created by WIN 10 on 3/18/2018.
 */

public class GroupMemberEntity implements Serializable {
    private int id = 0;
    private int chatId = 0;
    private int userDbId = 0;
    private String name = "";
    private String eccId = "";
    private int memberType = 0;
    private String eccPublicKey = "";

    public GroupMemberEntity() {
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

    public int getUserDbId() {
        return userDbId;
    }

    public void setUserDbId(int userDbId) {
        this.userDbId = userDbId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEccId() {
        return eccId;
    }

    public void setEccId(String eccId) {
        this.eccId = eccId;
    }

    public int getMemberType() {
        return memberType;
    }

    public void setMemberType(int memberType) {
        this.memberType = memberType;
    }

    public String getEccPublicKey() {
        return eccPublicKey;
    }

    public void setEccPublicKey(String eccPublicKey) {
        this.eccPublicKey = eccPublicKey;
    }
}
