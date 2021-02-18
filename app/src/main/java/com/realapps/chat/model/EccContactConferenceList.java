package com.realapps.chat.model;

/**
 * Created by inextrix on 26/3/18.
 */

public class EccContactConferenceList {

    int keyId, dbId, contactType;
    String eccId;
    String screenName;


    public EccContactConferenceList() {
        this.eccId = "";
        this.screenName = "";

    }

    public int getContactType() {
        return contactType;
    }

    public void setContactType(int contactType) {
        this.contactType = contactType;
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public String getEccId() {
        return eccId;
    }

    public void setEccId(String eccId) {
        this.eccId = eccId;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }
}
