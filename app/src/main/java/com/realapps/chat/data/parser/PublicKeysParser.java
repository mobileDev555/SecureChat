package com.realapps.chat.data.parser;

import android.app.Activity;
import android.content.Context;

import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.model.GroupMemberEntity;
import com.realapps.chat.model.PublicKeyEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Prashant Kumar Sharma on 7/19/2017.
 */

public class PublicKeysParser {

    public ArrayList<PublicKeysParser> parseJson(Activity mActivity, String response, ArrayList<GroupMemberEntity> groupMemberList) {
        ArrayList<PublicKeysParser> contactList = new ArrayList<>();
        DbHelper db = new DbHelper(mActivity);
        try {
            JSONObject mainJSONObject = new JSONObject(response);
            if (mainJSONObject.getInt("status") == 1) {
                JSONObject rootObject = new JSONObject(response);
                JSONArray dataArray = rootObject.getJSONArray("result_exist");
                if (dataArray.length() > 0) {
                    for (int i = 0; i < dataArray.length(); i++) {
                        GroupMemberEntity entity = new GroupMemberEntity();
                        JSONObject dataObject = dataArray.getJSONObject(i);
                        PublicKeyEntity keyEntity = new PublicKeyEntity();
                        keyEntity.setEccId(dataObject.getString("Id"));
                        keyEntity.setEccPublicKey(dataObject.getString("ecc_key"));
                        for (int j = 0; j < groupMemberList.size(); j++) {
                            if (groupMemberList.get(j).getEccId().equalsIgnoreCase(dataObject.getString("Id"))) {
                                entity = groupMemberList.get(j);
                            }
                        }
                        keyEntity.setName(entity.getName());
                        keyEntity.setUserDbId(entity.getUserDbId());
                        keyEntity.setUserType(entity.getMemberType());
                        db.insertPublicKey(keyEntity);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return contactList;
    }

    public ArrayList<PublicKeysParser> parseJson(Context mContext, String response, ArrayList<GroupMemberEntity> groupMemberList) {
        ArrayList<PublicKeysParser> contactList = new ArrayList<>();
        DbHelper db = new DbHelper(mContext);
        try {
            JSONObject mainJSONObject = new JSONObject(response);
            if (mainJSONObject.getInt("status") == 1) {
                JSONObject rootObject = new JSONObject(response);
                JSONArray dataArray = rootObject.getJSONArray("result_exist");
                if (dataArray.length() > 0) {
                    for (int i = 0; i < dataArray.length(); i++) {
                        GroupMemberEntity entity = new GroupMemberEntity();
                        JSONObject dataObject = dataArray.getJSONObject(i);
                        PublicKeyEntity keyEntity = new PublicKeyEntity();
                        keyEntity.setEccId(dataObject.getString("Id"));
                        keyEntity.setEccPublicKey(dataObject.getString("ecc_key"));
                        for (int j = 0; j < groupMemberList.size(); j++) {
                            if (groupMemberList.get(j).getEccId().equalsIgnoreCase(dataObject.getString("Id"))) {
                                entity = groupMemberList.get(j);
                            }
                        }
                        keyEntity.setName(entity.getName());
                        keyEntity.setUserDbId(entity.getUserDbId());
                        keyEntity.setUserType(entity.getMemberType());
                        db.insertPublicKey(keyEntity);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return contactList;
    }


    public void parseJsonKey(Context mContext, String response) {

        DbHelper db = new DbHelper(mContext);
        try {
            JSONObject mainJSONObject = new JSONObject(response);
            if (mainJSONObject.getInt("status") == 1) {
                JSONObject rootObject = new JSONObject(response);
                JSONArray dataArray = rootObject.getJSONArray("result_exist");
                if (dataArray.length() > 0) {
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.getJSONObject(i);

                        String ecc_id = dataObject.getString("Id");
                        String ecc_key = dataObject.getString("ecc_key");

                        db.updatePublicKey(ecc_id, ecc_key);

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();

    }
}
