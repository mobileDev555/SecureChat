package com.realapps.chat.model;

/**
 * Created by Hari Choudhary on 11/29/2018 at 11:55 AM .
 * Core techies
 * hari@coretechies.org
 */
public class SocketRequestEntity {
    private int id;
    private int requestType;
    private String uniqueId;
    public SocketRequestEntity() {
    }
    public SocketRequestEntity(int id, int requestType, String uniqueId) {
        this.id = id;
        this.requestType = requestType;
        this.uniqueId = uniqueId;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
