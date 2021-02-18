package com.realapps.chat;

/**
 * Created by Hari Choudhary on 7/26/2019 at 2:46 PM .
 * Core techies
 * hari@coretechies.org
 */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.parser.PublicKeysParser;
import com.realapps.chat.model.ChatMessageEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Belal on 12/30/2016.
 */

public class MessageSendService extends Service {
    //creating a mediaplayer object
    DbHelper dbHelper = null;
    ArrayList<ChatMessageEntity> messageList = null;
    ChatMessageEntity chobj = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        clearCache();

        return START_STICKY;
    }
    private void clearCache() {


        AndroidNetworking.post(ApiEndPoints.URL_FETCH_GROUP_ECC_KEYS)
                .addJSONObjectBody(getRawData())
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            new PublicKeysParser().parseJsonKey(getApplicationContext(), response.toString());



                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally{
                            stopSelf();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        stopSelf();
                    }
                });

    }
    public JSONObject getRawData() {
        DbHelper  db = new DbHelper(getApplicationContext());
        List<String> eccId = db.getECCid();

        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray eccIdArray = new JSONArray();
            if (eccId.size() > 0) {
                for (int i = 0; i < eccId.size(); i++) {
                    JSONObject singleECCIdObject = new JSONObject();
                    String eccID = eccId.get(i);
                    singleECCIdObject.put("ecc_id", eccID);
                    eccIdArray.put(singleECCIdObject);
                }
            }
            jsonObject.put("ecc_data", eccIdArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}