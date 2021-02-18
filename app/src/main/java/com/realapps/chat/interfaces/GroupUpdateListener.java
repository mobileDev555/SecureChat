package com.realapps.chat.interfaces;

/**
 * Created by Prashant Sharma on 3/16/2018.
 * Core techies
 * prashant@coretechies.org
 */

public interface GroupUpdateListener {

    void onNameChange();

    void onMemberAdd();

    void onMemberRemove(String eccId, int userId,int groupid);

}
