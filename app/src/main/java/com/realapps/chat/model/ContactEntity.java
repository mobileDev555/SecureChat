package com.realapps.chat.model;

import com.realapps.chat.utils.AppConstants;

import java.io.Serializable;

/**
 * Created by Prashant Sharma on 3/15/2018.
 * Core techies
 * prashant@coretechies.org
 */

public class ContactEntity implements Serializable {

    private String id = "";
    private int userType = 0;
    private int userDbId = 0;
    private String eccId = "";
    private String name = "";
    private boolean isSelected = false;
    private String eccPublicKey = "";
    private String blockStatus = "";
    private int isGroup = AppConstants.SINGLE_CHAT_TYPE;

    public ContactEntity() {
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public int getUserDbId() {
        return userDbId;
    }

    public void setUserDbId(int userDbId) {
        this.userDbId = userDbId;
    }

    public String getEccId() {
        return eccId;
    }

    public void setEccId(String eccId) {
        this.eccId = eccId;
    }

    public String getEccPublicKey() {
        return eccPublicKey;
    }

    public void setEccPublicKey(String eccPublicKey) {
        this.eccPublicKey = eccPublicKey;
    }

    public String getBlockStatus() {
        return blockStatus;
    }

    public void setBlockStatus(String blockStatus) {
        this.blockStatus = blockStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(int isGroup) {
        this.isGroup = isGroup;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
