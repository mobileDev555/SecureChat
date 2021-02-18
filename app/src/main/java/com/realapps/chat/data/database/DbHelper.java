package com.realapps.chat.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.model.GroupMemberEntity;
import com.realapps.chat.model.PublicKeyEntity;
import com.realapps.chat.model.SocketRequestEntity;
import com.realapps.chat.model.VaultEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.DateTimeUtils;
import com.realapps.chat.utils.DbConstants;
import com.realapps.chat.utils.SocketUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Prashant Kumar Sharma on 3/29/2017.
 */

public class DbHelper extends SQLiteOpenHelper {

    private String TAG = "DB Helper : ";

    private String CREATE_CHAT_TABLE = "CREATE TABLE IF NOT EXISTS " + DbConstants.TBL_CHAT_LIST + "("
            + DbConstants.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DbConstants.KEY_USER_DB_ID + " INTEGER,"
            + DbConstants.KEY_CREATED_DATE_TIME + " TEXT, "
            + DbConstants.KEY_NAME + " TEXT,"
            + DbConstants.KEY_ECC_ID + " TEXT,"
            + DbConstants.KEY_BLOCK_STATUS + " INTEGER,"
            + DbConstants.KEY_BURN_TIME + " INTEGER,"
            + DbConstants.KEY_SNOOZE_STATUS + " INTEGER,"
            + DbConstants.KEY_MESSAGE_TIME_STAMP + " TEXT, "
            + DbConstants.KEY_CHAT_TYPE + " INTEGER,"
            + DbConstants.KEY_SNOOZE_TIME_STAMP + " TEXT,"
            + DbConstants.KEY_LAST_MESSAGE_TIME + " TEXT,"
            + DbConstants.KEY_LAST_MESSAGE_STATUS + " INTEGER,"
            + DbConstants.KEY_LAST_MESSAGE_TYPE + " INTEGER,"
            + "UNIQUE(" + DbConstants.KEY_ECC_ID + "," + DbConstants.KEY_USER_DB_ID + ") ON CONFLICT IGNORE);";

    private String CREATE_MESSAGE_LIST_TABLE = "CREATE TABLE IF NOT EXISTS " + DbConstants.TBL_MESSAGE_LIST + "("
            + DbConstants.KEY_ID + " INTEGER PRIMARY KEY,"
            + DbConstants.KEY_CHAT_ID + " INTEGER,"
            + DbConstants.KEY_CHAT_TYPE + " INTEGER,"
            + DbConstants.KEY_CHAT_USER_DB_ID + " INTEGER,"
            + DbConstants.KEY_NAME + " TEXT,"
            + DbConstants.KEY_MESSAGE_ID + " TEXT,"
            + DbConstants.KEY_SENDER_ID + " INTEGER,"
            + DbConstants.KEY_RECEIVER_ID + " INTEGER,"
            + DbConstants.KEY_MESSAGE + " TEXT,"
            + DbConstants.KEY_MESSAGE_TYPE + " INTEGER,"
            + DbConstants.KEY_MESSAGE_STATUS + " INTEGER,"
            + DbConstants.KEY_MESSAGE_TIME_STAMP + " TEXT,"
            + DbConstants.KEY_MESSAGE_BURN_TIME + " INTEGER,"
            + DbConstants.KEY_MESSAGE_BURN_TIME_STAMP + " TEXT,"
            + DbConstants.KEY_MESSAGE_MIME_TYPE + " INTEGER,"
            + DbConstants.KEY_IMAGE_PATH + " TEXT,"
            + DbConstants.KEY_AUDIO_PATH + " TEXT,"
            + DbConstants.KEY_CONTACT_PATH + " TEXT,"
            + DbConstants.KEY_FILE_PATH + " TEXT,"
            + DbConstants.KEY_VIDEO_PATH + " TEXT,"
            + DbConstants.KEY_FILE_NAME + " TEXT,"
            + DbConstants.KEY_CURRENT_MESSAGE_STATUS + " INTEGER,"
            + DbConstants.KEY_REPLY + " TEXT,"
            + DbConstants.KEY_FAVOURITE + " INTEGER,"
            + DbConstants.KEY_PINNED + " INTEGER,"
            + DbConstants.KEY_VISIBILITY + " INTEGER,"
            + DbConstants.KEY_MESSAGE_ENCRYPTION_KEY + " TEXT,"
            + DbConstants.KEY_MESSAGE_IV + " TEXT,"
            + DbConstants.KEY_MESSAGE_SHARED_SECRET_KEY + " TEXT,"
            + DbConstants.KEY_PARENT_MESSAGE_ID + " TEXT,"
            + DbConstants.KEY_IS_REVISED + " INTEGER,"
            + DbConstants.KEY_EDITED_MESSAGE_TIME + " TEXT"
            + ")";

    private String CREATE_VAULT_MESSAGE_LIST_TABLE = "CREATE TABLE IF NOT EXISTS " + DbConstants.TBL_VAULT_MESSAGE_LIST + "("
            + DbConstants.KEY_ID + " INTEGER PRIMARY KEY,"
            + DbConstants.KEY_CHAT_ID + " INTEGER,"
            + DbConstants.KEY_CHAT_TYPE + " INTEGER,"
            + DbConstants.KEY_CHAT_USER_DB_ID + " INTEGER,"
            + DbConstants.KEY_NAME + " TEXT,"
            + DbConstants.KEY_MESSAGE_ID + " TEXT,"
            + DbConstants.KEY_SENDER_ID + " INTEGER,"
            + DbConstants.KEY_RECEIVER_ID + " INTEGER,"
            + DbConstants.KEY_MESSAGE + " TEXT,"
            + DbConstants.KEY_MESSAGE_TYPE + " INTEGER,"
            + DbConstants.KEY_MESSAGE_STATUS + " INTEGER,"
            + DbConstants.KEY_MESSAGE_TIME_STAMP + " TEXT,"
            + DbConstants.KEY_MESSAGE_BURN_TIME + " INTEGER,"
            + DbConstants.KEY_MESSAGE_BURN_TIME_STAMP + " TEXT,"
            + DbConstants.KEY_MESSAGE_MIME_TYPE + " INTEGER,"
            + DbConstants.KEY_IMAGE_PATH + " TEXT,"
            + DbConstants.KEY_AUDIO_PATH + " TEXT,"
            + DbConstants.KEY_CONTACT_PATH + " TEXT,"
            + DbConstants.KEY_FILE_PATH + " TEXT,"
            + DbConstants.KEY_VIDEO_PATH + " TEXT,"
            + DbConstants.KEY_CURRENT_MESSAGE_STATUS + " INTEGER,"
            + DbConstants.KEY_REPLY + " TEXT,"
            + DbConstants.KEY_FAVOURITE + " INTEGER,"
            + DbConstants.KEY_PINNED + " INTEGER,"
            + DbConstants.KEY_VISIBILITY + " INTEGER,"
            + DbConstants.KEY_MESSAGE_ENCRYPTION_KEY + " TEXT,"
            + DbConstants.KEY_MESSAGE_IV + " TEXT,"
            + DbConstants.KEY_MESSAGE_SHARED_SECRET_KEY + " TEXT"
            + ")";

    private String CREATE_CONTACT_TABLE = "CREATE TABLE IF NOT EXISTS " + DbConstants.TBL_CONTACT_LIST + "("
            + DbConstants.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DbConstants.KEY_USER_TYPE + " INTEGER,"
            + DbConstants.KEY_USER_DB_ID + " INTEGER,"
            + DbConstants.KEY_ECC_ID + " TEXT,"
            + DbConstants.KEY_NAME + " TEXT,"
            + DbConstants.KEY_ECC_PUBLIC_KEY + " TEXT,"
            + DbConstants.KEY_BLOCK_STATUS + " INTEGER,"
            + "UNIQUE(" + DbConstants.KEY_ECC_ID + ") ON CONFLICT IGNORE);";

    private String CREATE_GROUP_CONTACT_TABLE = "CREATE TABLE IF NOT EXISTS " + DbConstants.TBL_GROUP_CONTACT_LIST + "("
            + DbConstants.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DbConstants.KEY_CHAT_ID + " INTEGER,"
            + DbConstants.KEY_USER_DB_ID + " INTEGER,"
            + DbConstants.KEY_NAME + " TEXT,"
            + DbConstants.KEY_ECC_ID + " TEXT,"
            + DbConstants.KEY_MEMBER_TYPE + " INTEGER"
            + ")";

    private String CREATE_PUBLIC_KEY_TABLE = "CREATE TABLE IF NOT EXISTS " + DbConstants.TBL_PUBLIC_KEY_LIST + "("
            + DbConstants.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DbConstants.KEY_USER_DB_ID + " INTEGER,"
            + DbConstants.KEY_NAME + " TEXT,"
            + DbConstants.KEY_ECC_ID + " TEXT,"
            + DbConstants.KEY_USER_TYPE + " INTEGER,"
            + DbConstants.KEY_ECC_PUBLIC_KEY + " TEXT"
            + ")";

    private String CREATE_VAULT_ITEM_LIST_TABLE = "CREATE TABLE IF NOT EXISTS " + DbConstants.TBL_VAULT_ITEM_LIST + "("
            + DbConstants.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DbConstants.KEY_NAME + " TEXT,"
            + DbConstants.KEY_IMAGE_PATH + " TEXT,"
            + DbConstants.KEY_AUDIO_PATH + " TEXT,"
            + DbConstants.KEY_VIDEO_PATH + " TEXT,"
            + DbConstants.KEY_FILE_PATH + " TEXT,"
            + DbConstants.KEY_ECC_ID + " TEXT,"
            + DbConstants.KEY_ITEM_DATE_STAMP + " TEXT,"
            + DbConstants.KEY_DATE + " TEXT,"
            + DbConstants.KEY_TOTAL_ITEM + " INTEGER,"
            + DbConstants.KEY_PARENT_ID + " INTEGER,"
            + DbConstants.KEY_ITEM_TYPE + " INTEGER,"
            + DbConstants.KEY_ICON_TYPE + " INTEGER,"
            + DbConstants.KEY_MIME_TYPE + " INTEGER,"
            + DbConstants.KEY_USER_DB_ID + " INTEGER,"
            + DbConstants.KEY_CHAT_TYPE + " INTEGER,"
            + DbConstants.KEY_MESSAGE_ID + " TEXT"
            + ")";

    String ALTER_CREATE_VAULT_ITEM_LIST_TABLE = "ALTER TABLE " + DbConstants.TBL_VAULT_ITEM_LIST + " ADD COLUMN " + DbConstants.KEY_MESSAGE_ID + " TEXT";
    String ALTER_MESSAGE_LIST_TABLE_WITH_PARENT_MESSAGE_ID = "ALTER TABLE " + DbConstants.TBL_MESSAGE_LIST + " ADD COLUMN " + DbConstants.KEY_PARENT_MESSAGE_ID + " TEXT";
    String ALTER_MESSAGE_LIST_TABLE_WITH_REVISED = "ALTER TABLE " + DbConstants.TBL_MESSAGE_LIST + " ADD COLUMN " + DbConstants.KEY_IS_REVISED + " INTEGER";
    String ALTER_MESSAGE_LIST_TABLE_WITH_FILE_NAME = "ALTER TABLE " + DbConstants.TBL_MESSAGE_LIST + " ADD COLUMN " + DbConstants.KEY_FILE_NAME + " TEXT";
    String ALTER_MESSAGE_LIST_TABLE_WITH_REVISED_EDITED_TIMESTAMP = "ALTER TABLE " + DbConstants.TBL_MESSAGE_LIST + " ADD COLUMN " + DbConstants.KEY_EDITED_MESSAGE_TIME + " TEXT";


    private String CREATE_SOCKET_REQUEST_TABLE = "CREATE TABLE IF NOT EXISTS " + DbConstants.TBL_SOCKET_REQUEST + "("
            + DbConstants.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DbConstants.KEY_REQUEST_TYPE + " INTEGER,"
            + DbConstants.KEY_MESSAGE_ID + " TEXT,"
            + "UNIQUE(" + DbConstants.KEY_MESSAGE_ID + ") ON CONFLICT IGNORE);";


    public DbHelper(Context mContext) {
        super(mContext, DbConstants.DATABASE_NAME, null, DbConstants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CHAT_TABLE);
        db.execSQL(CREATE_MESSAGE_LIST_TABLE);
        db.execSQL(CREATE_CONTACT_TABLE);
        db.execSQL(CREATE_GROUP_CONTACT_TABLE);
        db.execSQL(CREATE_PUBLIC_KEY_TABLE);
        db.execSQL(CREATE_VAULT_ITEM_LIST_TABLE);
        db.execSQL(CREATE_VAULT_MESSAGE_LIST_TABLE);
        db.execSQL(CREATE_SOCKET_REQUEST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        /**
         * create a new table CREATE_SOCKET_REQUEST_TABLE in database version 2
         */
        if (oldVersion < 2) {
            db.execSQL(CREATE_SOCKET_REQUEST_TABLE);
        }
        if (oldVersion < 3) {
            db.execSQL(ALTER_CREATE_VAULT_ITEM_LIST_TABLE);
        }

        if (oldVersion < 4) {
            db.execSQL(ALTER_MESSAGE_LIST_TABLE_WITH_PARENT_MESSAGE_ID);
            db.execSQL(ALTER_MESSAGE_LIST_TABLE_WITH_REVISED);
        }

        if (oldVersion < 5) {
            db.execSQL(ALTER_MESSAGE_LIST_TABLE_WITH_REVISED_EDITED_TIMESTAMP);
        }
        if (oldVersion < 6) {
            db.execSQL(ALTER_MESSAGE_LIST_TABLE_WITH_FILE_NAME);
        }
    }

    public void resetDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DbConstants.TBL_CHAT_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + DbConstants.TBL_MESSAGE_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + DbConstants.TBL_CONTACT_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + DbConstants.TBL_GROUP_CONTACT_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + DbConstants.TBL_PUBLIC_KEY_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + DbConstants.TBL_VAULT_ITEM_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + DbConstants.TBL_VAULT_MESSAGE_LIST);
        db.close();
        createDb();
    }

    public void createDb() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(CREATE_CHAT_TABLE);
        db.execSQL(CREATE_MESSAGE_LIST_TABLE);
        db.execSQL(CREATE_CONTACT_TABLE);
        db.execSQL(CREATE_GROUP_CONTACT_TABLE);
        db.execSQL(CREATE_PUBLIC_KEY_TABLE);
        db.execSQL(CREATE_VAULT_ITEM_LIST_TABLE);
        db.execSQL(CREATE_VAULT_MESSAGE_LIST_TABLE);
        db.close();
    }

    /**
     * Database INSERT QUERIES.
     * Insert data into database.
     */

    //Insert Chat List
    public long insertChatList(ChatListEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbConstants.KEY_USER_DB_ID, entity.getUserDbId());
        cv.put(DbConstants.KEY_CREATED_DATE_TIME, entity.getCreatedDateTime());
        cv.put(DbConstants.KEY_NAME, entity.getName());
        cv.put(DbConstants.KEY_ECC_ID, entity.getEccId().toUpperCase());
        cv.put(DbConstants.KEY_BLOCK_STATUS, entity.getBlockStatus());
        cv.put(DbConstants.KEY_BURN_TIME, entity.getBurnTime());
        cv.put(DbConstants.KEY_SNOOZE_STATUS, entity.getSnoozeStatus());
        cv.put(DbConstants.KEY_MESSAGE_TIME_STAMP, entity.getMessageTimeStamp());
        cv.put(DbConstants.KEY_CHAT_TYPE, entity.getChatType());
        cv.put(DbConstants.KEY_SNOOZE_TIME_STAMP, entity.getSnoozeTimeStamp());
        cv.put(DbConstants.KEY_LAST_MESSAGE_TIME, entity.getLastMessageTime());
        cv.put(DbConstants.KEY_LAST_MESSAGE_STATUS, entity.getLastMessageStatus());
        cv.put(DbConstants.KEY_LAST_MESSAGE_TYPE, entity.getLastMessageType());

        long id = db.insert(DbConstants.TBL_CHAT_LIST, null, cv);
        db.close();
        return id;
    }

    //Insert Chat List
    public long insertVaultMessage(ChatMessageEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbConstants.KEY_CHAT_ID, entity.getChatId());
        cv.put(DbConstants.KEY_CHAT_TYPE, entity.getChatType());
        cv.put(DbConstants.KEY_CHAT_USER_DB_ID, entity.getChatUserDbId());
        cv.put(DbConstants.KEY_NAME, entity.getName());
        cv.put(DbConstants.KEY_MESSAGE_ID, entity.getMessageId());
        cv.put(DbConstants.KEY_SENDER_ID, entity.getSenderId());
        cv.put(DbConstants.KEY_RECEIVER_ID, entity.getReceiverId());
        cv.put(DbConstants.KEY_MESSAGE, entity.getMessage());
        cv.put(DbConstants.KEY_MESSAGE_TYPE, entity.getMessageType());
        cv.put(DbConstants.KEY_MESSAGE_STATUS, entity.getMessageStatus());
        cv.put(DbConstants.KEY_MESSAGE_TIME_STAMP, entity.getMessageTimeStamp());
        cv.put(DbConstants.KEY_MESSAGE_BURN_TIME, entity.getMessageBurnTime());
        cv.put(DbConstants.KEY_MESSAGE_BURN_TIME_STAMP, entity.getMessageBurnTimeStamp());
        cv.put(DbConstants.KEY_MESSAGE_MIME_TYPE, entity.getMessageMimeType());
        cv.put(DbConstants.KEY_IMAGE_PATH, entity.getImagePath());
        cv.put(DbConstants.KEY_AUDIO_PATH, entity.getAudioPath());
        cv.put(DbConstants.KEY_CONTACT_PATH, entity.getContactPath());
        cv.put(DbConstants.KEY_FILE_PATH, entity.getFilePath());
        cv.put(DbConstants.KEY_VIDEO_PATH, entity.getVideoPath());
        cv.put(DbConstants.KEY_CURRENT_MESSAGE_STATUS, entity.getCurrentMessageStatus());
        cv.put(DbConstants.KEY_REPLY, entity.getEddId());
        cv.put(DbConstants.KEY_FAVOURITE, entity.getFavourite());
        cv.put(DbConstants.KEY_PINNED, entity.getPinned());
        cv.put(DbConstants.KEY_VISIBILITY, entity.getVisibility());
        cv.put(DbConstants.KEY_MESSAGE_ENCRYPTION_KEY, entity.getMessageEncryptionKey());
        cv.put(DbConstants.KEY_MESSAGE_IV, entity.getMessageIv());
        cv.put(DbConstants.KEY_MESSAGE_SHARED_SECRET_KEY, entity.getMessageSharedSecretKey());

        long id = db.insert(DbConstants.TBL_VAULT_MESSAGE_LIST, null, cv);
        db.close();
        return id;
    }


    //Insert Contact List
    public long insertContactList(ContactEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbConstants.KEY_USER_TYPE, entity.getUserType());
        cv.put(DbConstants.KEY_USER_DB_ID, entity.getUserDbId());
        cv.put(DbConstants.KEY_ECC_ID, entity.getEccId());
        cv.put(DbConstants.KEY_NAME, entity.getName());
        cv.put(DbConstants.KEY_ECC_PUBLIC_KEY, entity.getEccPublicKey());
        cv.put(DbConstants.KEY_BLOCK_STATUS, entity.getBlockStatus());
        long id = db.insert(DbConstants.TBL_CONTACT_LIST, null, cv);
        db.close();
        return id;
    }


    //Insert Socket request
    public long insertSocketRequest(SocketRequestEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbConstants.KEY_REQUEST_TYPE, entity.getRequestType());
        cv.put(DbConstants.KEY_MESSAGE_ID, entity.getUniqueId());
        long id = db.insert(DbConstants.TBL_SOCKET_REQUEST, null, cv);
        db.close();
        return id;
    }

    //Insert Group Contact Member
    public long insertGroupMember(GroupMemberEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbConstants.KEY_CHAT_ID, entity.getChatId());
        cv.put(DbConstants.KEY_USER_DB_ID, entity.getUserDbId());
        cv.put(DbConstants.KEY_NAME, entity.getName());
        cv.put(DbConstants.KEY_ECC_ID, entity.getEccId());
        cv.put(DbConstants.KEY_MEMBER_TYPE, entity.getMemberType());
        long id = db.insert(DbConstants.TBL_GROUP_CONTACT_LIST, null, cv);
        db.close();
        return id;
    }

    //Insert User Public Contact Member
    public long insertPublicKey(PublicKeyEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbConstants.KEY_USER_DB_ID, entity.getUserDbId());
        cv.put(DbConstants.KEY_NAME, entity.getName());
        cv.put(DbConstants.KEY_ECC_ID, entity.getEccId().toUpperCase());
        cv.put(DbConstants.KEY_USER_TYPE, entity.getUserType());
        cv.put(DbConstants.KEY_ECC_PUBLIC_KEY, entity.getEccPublicKey());
        long id = db.insert(DbConstants.TBL_PUBLIC_KEY_LIST, null, cv);
        db.close();
        return id;
    }

    //Insert User Public Contact Member
    public long insertVaultItem(VaultEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbConstants.KEY_NAME, entity.getName());
        cv.put(DbConstants.KEY_IMAGE_PATH, entity.getImage());
        cv.put(DbConstants.KEY_AUDIO_PATH, entity.getAudio());
        cv.put(DbConstants.KEY_VIDEO_PATH, entity.getVideo());
        cv.put(DbConstants.KEY_FILE_PATH, entity.getNotes());
        cv.put(DbConstants.KEY_ECC_ID, entity.getEccId());
        cv.put(DbConstants.KEY_ITEM_DATE_STAMP, entity.getDateTimeStamp());
        cv.put(DbConstants.KEY_DATE, entity.getDate());
        cv.put(DbConstants.KEY_TOTAL_ITEM, entity.getTotalItem());
        cv.put(DbConstants.KEY_PARENT_ID, entity.getParentId());
        cv.put(DbConstants.KEY_ITEM_TYPE, entity.getItemType());
        cv.put(DbConstants.KEY_ICON_TYPE, entity.getIconType());
        cv.put(DbConstants.KEY_MIME_TYPE, entity.getMimeType());
        cv.put(DbConstants.KEY_USER_DB_ID, entity.getDbId());
        cv.put(DbConstants.KEY_MESSAGE_ID, entity.getMessageID());
        cv.put(DbConstants.KEY_CHAT_TYPE, entity.getChatType());

        long id = db.insert(DbConstants.TBL_VAULT_ITEM_LIST, null, cv);
        db.close();
        return id;
    }

    public long updateDbID(int DbId, int newID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_USER_DB_ID, newID);
        long result = db.update(DbConstants.TBL_VAULT_ITEM_LIST, values, DbConstants.KEY_USER_DB_ID + "=" + DbId, null);
        closedb(db);
        return result;
    }

    //Insert Chat List
    public long insertChatMessage(ChatMessageEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbConstants.KEY_CHAT_ID, entity.getChatId());
        cv.put(DbConstants.KEY_CHAT_TYPE, entity.getChatType());
        cv.put(DbConstants.KEY_CHAT_USER_DB_ID, entity.getChatUserDbId());
        cv.put(DbConstants.KEY_NAME, entity.getName());
        cv.put(DbConstants.KEY_MESSAGE_ID, entity.getMessageId());
        //revised text
        cv.put(DbConstants.KEY_PARENT_MESSAGE_ID, entity.getParentMessageId());
        cv.put(DbConstants.KEY_IS_REVISED, entity.getIsRevised());
        cv.put(DbConstants.KEY_EDITED_MESSAGE_TIME, entity.getEditedMessageTimeStamp());

        cv.put(DbConstants.KEY_SENDER_ID, entity.getSenderId());
        cv.put(DbConstants.KEY_RECEIVER_ID, entity.getReceiverId());
        cv.put(DbConstants.KEY_MESSAGE, entity.getMessage());
        cv.put(DbConstants.KEY_MESSAGE_TYPE, entity.getMessageType());
        cv.put(DbConstants.KEY_MESSAGE_STATUS, entity.getMessageStatus());
        cv.put(DbConstants.KEY_MESSAGE_TIME_STAMP, entity.getMessageTimeStamp());
        cv.put(DbConstants.KEY_MESSAGE_BURN_TIME, entity.getMessageBurnTime());
        cv.put(DbConstants.KEY_MESSAGE_BURN_TIME_STAMP, entity.getMessageBurnTimeStamp());
        cv.put(DbConstants.KEY_MESSAGE_MIME_TYPE, entity.getMessageMimeType());
        cv.put(DbConstants.KEY_IMAGE_PATH, entity.getImagePath());
        cv.put(DbConstants.KEY_AUDIO_PATH, entity.getAudioPath());
        cv.put(DbConstants.KEY_CONTACT_PATH, entity.getContactPath());
        cv.put(DbConstants.KEY_FILE_PATH, entity.getFilePath());
        cv.put(DbConstants.KEY_VIDEO_PATH, entity.getVideoPath());
        cv.put(DbConstants.KEY_CURRENT_MESSAGE_STATUS, entity.getCurrentMessageStatus());
        cv.put(DbConstants.KEY_REPLY, entity.getEddId());  // we use this field for store ecc id
        cv.put(DbConstants.KEY_FAVOURITE, entity.getFavourite());
        cv.put(DbConstants.KEY_PINNED, entity.getPinned());
        cv.put(DbConstants.KEY_VISIBILITY, entity.getVisibility());
        cv.put(DbConstants.KEY_MESSAGE_ENCRYPTION_KEY, entity.getMessageEncryptionKey());
        cv.put(DbConstants.KEY_MESSAGE_IV, entity.getMessageIv());
        cv.put(DbConstants.KEY_MESSAGE_SHARED_SECRET_KEY, entity.getMessageSharedSecretKey());
        cv.put(DbConstants.KEY_FILE_NAME, entity.getFileName());

        long id = db.insert(DbConstants.TBL_MESSAGE_LIST, null, cv);
        db.close();
        return id;
    }

    /**
     * Database SELECT QUERIES.
     * Get data from database.
     */

    //Get Chat List Entity.
    public ChatListEntity getChatEntity(int senderUserDbId) {

        SQLiteDatabase db = getReadableDatabase();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_CHAT_LIST + " WHERE " + DbConstants.KEY_USER_DB_ID + " = " + senderUserDbId;
        Cursor co = db.rawQuery(sql_query, null);
        ChatListEntity entity = new ChatListEntity();
        if (co.moveToFirst()) {
            do {
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setCreatedDateTime(co.getString(co.getColumnIndex(DbConstants.KEY_CREATED_DATE_TIME)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setBlockStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_BLOCK_STATUS)));
                entity.setBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_BURN_TIME)));
                entity.setSnoozeStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_SNOOZE_STATUS)));
                entity.setMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_TIME_STAMP)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setSnoozeTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_SNOOZE_TIME_STAMP)));
                entity.setLastMessageTime(co.getString(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TIME)));
                entity.setLastMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_STATUS)));
                entity.setLastMessageType(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TYPE)));

            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return entity;
    }

    //Get Chat List Entity.
    public ChatListEntity getChatEntity(String senderEccId) {

        SQLiteDatabase db = getReadableDatabase();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_CHAT_LIST + " WHERE UPPER(" + DbConstants.KEY_ECC_ID + ") = '" + senderEccId.toUpperCase() + "' ";
        Cursor co = db.rawQuery(sql_query, null);
        ChatListEntity entity = new ChatListEntity();
        if (co.moveToFirst()) {
            do {
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setCreatedDateTime(co.getString(co.getColumnIndex(DbConstants.KEY_CREATED_DATE_TIME)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setBlockStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_BLOCK_STATUS)));
                entity.setBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_BURN_TIME)));
                entity.setSnoozeStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_SNOOZE_STATUS)));
                entity.setMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_TIME_STAMP)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setSnoozeTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_SNOOZE_TIME_STAMP)));
                entity.setLastMessageTime(co.getString(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TIME)));
                entity.setLastMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_STATUS)));
                entity.setLastMessageType(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TYPE)));

            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return entity;
    }

    //Get Chat List Entity.
    public ChatListEntity getGroupChatEntity(int groupUserDbId) {

        SQLiteDatabase db = getReadableDatabase();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_CHAT_LIST + " WHERE " + DbConstants.KEY_USER_DB_ID + " = " + groupUserDbId + " AND " + DbConstants.KEY_CHAT_TYPE + " = " + AppConstants.GROUP_CHAT_TYPE;
        Cursor co = db.rawQuery(sql_query, null);
        ChatListEntity entity = new ChatListEntity();
        if (co.moveToFirst()) {
            do {
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setCreatedDateTime(co.getString(co.getColumnIndex(DbConstants.KEY_CREATED_DATE_TIME)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setBlockStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_BLOCK_STATUS)));
                entity.setBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_BURN_TIME)));
                entity.setSnoozeStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_SNOOZE_STATUS)));
                entity.setMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_TIME_STAMP)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setSnoozeTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_SNOOZE_TIME_STAMP)));
                entity.setLastMessageTime(co.getString(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TIME)));
                entity.setLastMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_STATUS)));
                entity.setLastMessageType(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TYPE)));

            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return entity;
    }

    //Get Complete Chat List in DESC Order by DATE TIME.
    public ArrayList<ChatListEntity> getChatList() {

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ChatListEntity> chatList = new ArrayList<>();
        String sql_query = "SELECT ALL *  FROM " + DbConstants.TBL_CHAT_LIST + " GROUP BY " + DbConstants.KEY_USER_DB_ID + " ORDER BY " + "datetime(" + DbConstants.KEY_MESSAGE_TIME_STAMP + ")" + " DESC";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                ChatListEntity entity = new ChatListEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setCreatedDateTime(co.getString(co.getColumnIndex(DbConstants.KEY_CREATED_DATE_TIME)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setBlockStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_BLOCK_STATUS)));
                entity.setBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_BURN_TIME)));
                entity.setSnoozeStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_SNOOZE_STATUS)));
                entity.setMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_TIME_STAMP)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setSnoozeTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_SNOOZE_TIME_STAMP)));
                entity.setLastMessageTime(co.getString(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TIME)));
                entity.setLastMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_STATUS)));
                entity.setLastMessageType(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TYPE)));

                    chatList.add(entity);

            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return chatList;
    }


    public int getUndeliveredCount(int chatId, int chatType) {
        int count=0;
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ChatMessageEntity> msgList = new ArrayList<>();

        String sql_query = "SELECT ALL *  FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE "  + DbConstants.KEY_CHAT_TYPE + " = " + chatType + "  and  " + DbConstants.KEY_CHAT_ID + " = " + chatId + "  and  "+  DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_NOT_SENT_STATUS +" ORDER BY " + DbConstants.KEY_MESSAGE_TIME_STAMP + " ASC";
        Cursor co = db.rawQuery(sql_query, null);

        if (co.moveToFirst()) {
          count = co.getCount();
        }
        co.close();
        closedb(db);
        return count;
    }

    public ArrayList<ChatListEntity> getChatListFromUndelivered() {

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ChatListEntity> chatList = new ArrayList<>();
        String sql_query = "SELECT ALL *  FROM " + DbConstants.TBL_CHAT_LIST + " GROUP BY " + DbConstants.KEY_USER_DB_ID + " ORDER BY " + "datetime(" + DbConstants.KEY_MESSAGE_TIME_STAMP + ")" + " DESC";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                ChatListEntity entity = new ChatListEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setCreatedDateTime(co.getString(co.getColumnIndex(DbConstants.KEY_CREATED_DATE_TIME)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setBlockStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_BLOCK_STATUS)));
                entity.setBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_BURN_TIME)));
                entity.setSnoozeStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_SNOOZE_STATUS)));
                entity.setMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_TIME_STAMP)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setSnoozeTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_SNOOZE_TIME_STAMP)));
                entity.setLastMessageTime(co.getString(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TIME)));
                entity.setLastMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_STATUS)));
                entity.setLastMessageType(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TYPE)));

                if(getUndeliveredCount(entity.getId(),entity.getChatType())>0) {
                    chatList.add(entity);
                    break;
                }
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return chatList;
    }

    public ArrayList<ChatListEntity> getSChatList() {

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ChatListEntity> chatList = new ArrayList<>();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_CHAT_LIST + " WHERE " + DbConstants.KEY_CHAT_TYPE + " = " + AppConstants.SINGLE_CHAT_TYPE + " ORDER BY " + "datetime(" + DbConstants.KEY_MESSAGE_TIME_STAMP + ")" + " DESC";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                ChatListEntity entity = new ChatListEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setCreatedDateTime(co.getString(co.getColumnIndex(DbConstants.KEY_CREATED_DATE_TIME)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setBlockStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_BLOCK_STATUS)));
                entity.setBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_BURN_TIME)));
                entity.setSnoozeStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_SNOOZE_STATUS)));
                entity.setMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_TIME_STAMP)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setSnoozeTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_SNOOZE_TIME_STAMP)));
                entity.setLastMessageTime(co.getString(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TIME)));
                entity.setLastMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_STATUS)));
                entity.setLastMessageType(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TYPE)));

                chatList.add(entity);
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return chatList;
    }

    public ArrayList<ChatListEntity> getGChatList() {

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ChatListEntity> chatList = new ArrayList<>();
        String sql_query = "SELECT ALL *  FROM " + DbConstants.TBL_CHAT_LIST + " Group by " + DbConstants.KEY_USER_DB_ID + " HAVING " + DbConstants.KEY_CHAT_TYPE + " = " + AppConstants.GROUP_CHAT_TYPE + " ORDER BY " + "datetime(" + DbConstants.KEY_MESSAGE_TIME_STAMP + ")" + " DESC";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                ChatListEntity entity = new ChatListEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setCreatedDateTime(co.getString(co.getColumnIndex(DbConstants.KEY_CREATED_DATE_TIME)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setBlockStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_BLOCK_STATUS)));
                entity.setBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_BURN_TIME)));
                entity.setSnoozeStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_SNOOZE_STATUS)));
                entity.setMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_TIME_STAMP)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setSnoozeTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_SNOOZE_TIME_STAMP)));
                entity.setLastMessageTime(co.getString(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TIME)));
                entity.setLastMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_STATUS)));
                entity.setLastMessageType(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TYPE)));

                chatList.add(entity);
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return chatList;
    }

    public List<Integer> getChatListChatId() {

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Integer> chatList = new ArrayList<>();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_CHAT_LIST + " ORDER BY " + "datetime(" + DbConstants.KEY_MESSAGE_TIME_STAMP + ")" + " DESC";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                chatList.add(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return chatList;
    }

    public ArrayList<ChatMessageEntity> getUnreadMessageList() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ChatMessageEntity> msgList = new ArrayList<>();

        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_MESSAGE_LIST + " where " + DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_UNREAD_STATUS;
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                ChatMessageEntity entity = new ChatMessageEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setChatId(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_ID)));

                entity.setMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ID)));
                msgList.add(entity);
            } while (co.moveToNext());
        }
        co.close();
        closedb(db);
        return msgList;
    }

    public ArrayList<ChatListEntity> getGroupChatList() {

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ChatListEntity> chatList = new ArrayList<>();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_CHAT_LIST + " WHERE " + DbConstants.KEY_CHAT_TYPE + " = " + AppConstants.GROUP_CHAT_TYPE + " ORDER BY " + "datetime(" + DbConstants.KEY_MESSAGE_TIME_STAMP + ")" + " DESC";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                ChatListEntity entity = new ChatListEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setCreatedDateTime(co.getString(co.getColumnIndex(DbConstants.KEY_CREATED_DATE_TIME)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setBlockStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_BLOCK_STATUS)));
                entity.setBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_BURN_TIME)));
                entity.setSnoozeStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_SNOOZE_STATUS)));
                entity.setMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_TIME_STAMP)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setSnoozeTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_SNOOZE_TIME_STAMP)));
                entity.setLastMessageTime(co.getString(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TIME)));
                entity.setLastMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_STATUS)));
                entity.setLastMessageType(co.getInt(co.getColumnIndex(DbConstants.KEY_LAST_MESSAGE_TYPE)));

                chatList.add(entity);
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return chatList;
    }


    public int getTotalUnreadMessagesCount(int chatId) {
        //  String countQuery = "SELECT  *  FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE " + DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_UNREAD_STATUS + "  and  " + DbConstants.KEY_CHAT_ID + " = " + chatId + "  and  " + DbConstants.KEY_IS_REVISED + " != " + AppConstants.revised;
        String countQuery = "SELECT  *  FROM " + DbConstants.TBL_MESSAGE_LIST  +" GROUP BY " + DbConstants.KEY_MESSAGE_ID + " HAVING " + DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_UNREAD_STATUS  + " AND " + DbConstants.KEY_CHAT_ID + " = " + chatId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public int getMember(int chatId, String eccID) {
        String countQuery = "SELECT  *  FROM " + DbConstants.TBL_GROUP_CONTACT_LIST + " WHERE " + DbConstants.KEY_CHAT_ID + " = " + chatId + "  and UPPER(" + DbConstants.KEY_ECC_ID + ") = '" + eccID.toUpperCase() + "' ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    // Get Undeivered Message List of different users.
    public ChatMessageEntity getUndeliveredMessageList(int chatId, int chatType) {
        SQLiteDatabase db = getReadableDatabase();
        ChatMessageEntity entity = null;
        String sql_query = "SELECT ALL *  FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE "  + DbConstants.KEY_CHAT_TYPE + " = " + chatType + "  and  " + DbConstants.KEY_CHAT_ID + " = " + chatId + "  and  "+  DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_NOT_SENT_STATUS +" ORDER BY " + DbConstants.KEY_MESSAGE_TIME_STAMP + " ASC LIMIT 1";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
                entity = new ChatMessageEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setChatId(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_ID)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setChatUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_USER_DB_ID)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ID)));
                //Revised text
                entity.setParentMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_PARENT_MESSAGE_ID)));
                entity.setIsRevised(co.getInt(co.getColumnIndex(DbConstants.KEY_IS_REVISED)));
                entity.setEditedMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_EDITED_MESSAGE_TIME)));

                entity.setSenderId(co.getInt(co.getColumnIndex(DbConstants.KEY_SENDER_ID)));
                entity.setReceiverId(co.getInt(co.getColumnIndex(DbConstants.KEY_RECEIVER_ID)));
                entity.setMessage(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE)));
                entity.setMessageType(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_TYPE)));
                entity.setMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_STATUS)));
                entity.setMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_TIME_STAMP)));
                entity.setMessageBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_BURN_TIME)));
                entity.setMessageBurnTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_BURN_TIME_STAMP)));
                entity.setMessageMimeType(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_MIME_TYPE)));
                entity.setImagePath(co.getString(co.getColumnIndex(DbConstants.KEY_IMAGE_PATH)));
                entity.setAudioPath(co.getString(co.getColumnIndex(DbConstants.KEY_AUDIO_PATH)));
                entity.setContactPath(co.getString(co.getColumnIndex(DbConstants.KEY_CONTACT_PATH)));
                entity.setFilePath(co.getString(co.getColumnIndex(DbConstants.KEY_FILE_PATH)));
                entity.setVideoPath(co.getString(co.getColumnIndex(DbConstants.KEY_VIDEO_PATH)));
                entity.setCurrentMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_CURRENT_MESSAGE_STATUS)));
                entity.setEddId(co.getString(co.getColumnIndex(DbConstants.KEY_REPLY))); //using this for store ecc id
                entity.setFavourite(co.getInt(co.getColumnIndex(DbConstants.KEY_FAVOURITE)));
                entity.setPinned(co.getInt(co.getColumnIndex(DbConstants.KEY_PINNED)));
                entity.setVisibility(co.getInt(co.getColumnIndex(DbConstants.KEY_VISIBILITY)));
                entity.setMessageEncryptionKey(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ENCRYPTION_KEY)));
                entity.setMessageIv(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_IV)));
                entity.setMessageSharedSecretKey(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_SHARED_SECRET_KEY)));
                entity.setFileName(co.getString(co.getColumnIndex(DbConstants.KEY_FILE_NAME)));
        }
        co.close();
        closedb(db);
        return entity;
    }
    // Get Complete Message List of different users.
    public ArrayList<ChatMessageEntity> getMessageList(int chatId, int chatType) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ChatMessageEntity> msgList = new ArrayList<>();

        String sql_query = "SELECT ALL *  FROM " + DbConstants.TBL_MESSAGE_LIST + " GROUP BY " + DbConstants.KEY_MESSAGE_ID + " HAVING " + DbConstants.KEY_CHAT_TYPE + " = " + chatType + "  and  " + DbConstants.KEY_CHAT_ID + " = " + chatId + " ORDER BY " + DbConstants.KEY_MESSAGE_TIME_STAMP + " ASC";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                ChatMessageEntity entity = new ChatMessageEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setChatId(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_ID)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setChatUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_USER_DB_ID)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ID)));
                //Revised text
                entity.setParentMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_PARENT_MESSAGE_ID)));
                entity.setIsRevised(co.getInt(co.getColumnIndex(DbConstants.KEY_IS_REVISED)));
                entity.setEditedMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_EDITED_MESSAGE_TIME)));

                entity.setSenderId(co.getInt(co.getColumnIndex(DbConstants.KEY_SENDER_ID)));
                entity.setReceiverId(co.getInt(co.getColumnIndex(DbConstants.KEY_RECEIVER_ID)));
                entity.setMessage(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE)));
                entity.setMessageType(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_TYPE)));
                entity.setMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_STATUS)));
                entity.setMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_TIME_STAMP)));
                entity.setMessageBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_BURN_TIME)));
                entity.setMessageBurnTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_BURN_TIME_STAMP)));
                entity.setMessageMimeType(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_MIME_TYPE)));
                entity.setImagePath(co.getString(co.getColumnIndex(DbConstants.KEY_IMAGE_PATH)));
                entity.setAudioPath(co.getString(co.getColumnIndex(DbConstants.KEY_AUDIO_PATH)));
                entity.setContactPath(co.getString(co.getColumnIndex(DbConstants.KEY_CONTACT_PATH)));
                entity.setFilePath(co.getString(co.getColumnIndex(DbConstants.KEY_FILE_PATH)));
                entity.setVideoPath(co.getString(co.getColumnIndex(DbConstants.KEY_VIDEO_PATH)));
                entity.setCurrentMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_CURRENT_MESSAGE_STATUS)));
                entity.setEddId(co.getString(co.getColumnIndex(DbConstants.KEY_REPLY))); //using this for store ecc id
                entity.setFavourite(co.getInt(co.getColumnIndex(DbConstants.KEY_FAVOURITE)));
                entity.setPinned(co.getInt(co.getColumnIndex(DbConstants.KEY_PINNED)));
                entity.setVisibility(co.getInt(co.getColumnIndex(DbConstants.KEY_VISIBILITY)));
                entity.setMessageEncryptionKey(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ENCRYPTION_KEY)));
                entity.setMessageIv(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_IV)));
                entity.setMessageSharedSecretKey(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_SHARED_SECRET_KEY)));
                entity.setFileName(co.getString(co.getColumnIndex(DbConstants.KEY_FILE_NAME)));
                // Adding item to list
                msgList.add(entity);
            } while (co.moveToNext());
        }
        co.close();
        closedb(db);
        return msgList;
    }
    public ArrayList<ChatMessageEntity> getMessageListService() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ChatMessageEntity> msgList = new ArrayList<>();

        String sql_query = "SELECT ALL *  FROM " + DbConstants.TBL_MESSAGE_LIST + " GROUP BY " + DbConstants.KEY_MESSAGE_ID + " ORDER BY " + DbConstants.KEY_MESSAGE_TIME_STAMP + " ASC";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                ChatMessageEntity entity = new ChatMessageEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setChatId(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_ID)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setChatUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_USER_DB_ID)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ID)));
                //Revised text
                entity.setParentMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_PARENT_MESSAGE_ID)));
                entity.setIsRevised(co.getInt(co.getColumnIndex(DbConstants.KEY_IS_REVISED)));
                entity.setEditedMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_EDITED_MESSAGE_TIME)));

                entity.setSenderId(co.getInt(co.getColumnIndex(DbConstants.KEY_SENDER_ID)));
                entity.setReceiverId(co.getInt(co.getColumnIndex(DbConstants.KEY_RECEIVER_ID)));
                entity.setMessage(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE)));
                entity.setMessageType(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_TYPE)));
                entity.setMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_STATUS)));
                entity.setMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_TIME_STAMP)));
                entity.setMessageBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_BURN_TIME)));
                entity.setMessageBurnTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_BURN_TIME_STAMP)));
                entity.setMessageMimeType(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_MIME_TYPE)));
                entity.setImagePath(co.getString(co.getColumnIndex(DbConstants.KEY_IMAGE_PATH)));
                entity.setAudioPath(co.getString(co.getColumnIndex(DbConstants.KEY_AUDIO_PATH)));
                entity.setContactPath(co.getString(co.getColumnIndex(DbConstants.KEY_CONTACT_PATH)));
                entity.setFilePath(co.getString(co.getColumnIndex(DbConstants.KEY_FILE_PATH)));
                entity.setVideoPath(co.getString(co.getColumnIndex(DbConstants.KEY_VIDEO_PATH)));
                entity.setCurrentMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_CURRENT_MESSAGE_STATUS)));
                entity.setEddId(co.getString(co.getColumnIndex(DbConstants.KEY_REPLY))); //using this for store ecc id
                entity.setFavourite(co.getInt(co.getColumnIndex(DbConstants.KEY_FAVOURITE)));
                entity.setPinned(co.getInt(co.getColumnIndex(DbConstants.KEY_PINNED)));
                entity.setVisibility(co.getInt(co.getColumnIndex(DbConstants.KEY_VISIBILITY)));
                entity.setMessageEncryptionKey(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ENCRYPTION_KEY)));
                entity.setMessageIv(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_IV)));
                entity.setMessageSharedSecretKey(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_SHARED_SECRET_KEY)));
                entity.setFileName(co.getString(co.getColumnIndex(DbConstants.KEY_FILE_NAME)));
                // Adding item to list
                msgList.add(entity);
            } while (co.moveToNext());
        }
        co.close();
        closedb(db);
        return msgList;
    }

    // Get Complete Message List of different users.
    public ArrayList<ChatMessageEntity> getMessageListAll() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ChatMessageEntity> msgList = new ArrayList<>();

        String sql_query = "SELECT ALL *  FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE "+DbConstants.KEY_CURRENT_MESSAGE_STATUS +" = "+AppConstants.MESSAGE_NOT_SENT_STATUS+ " ORDER BY " + DbConstants.KEY_MESSAGE_TIME_STAMP + " ASC";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                ChatMessageEntity entity = new ChatMessageEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setChatId(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_ID)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setChatUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_USER_DB_ID)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ID)));
                //Revised text
                entity.setParentMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_PARENT_MESSAGE_ID)));
                entity.setIsRevised(co.getInt(co.getColumnIndex(DbConstants.KEY_IS_REVISED)));
                entity.setEditedMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_EDITED_MESSAGE_TIME)));

                entity.setSenderId(co.getInt(co.getColumnIndex(DbConstants.KEY_SENDER_ID)));
                entity.setReceiverId(co.getInt(co.getColumnIndex(DbConstants.KEY_RECEIVER_ID)));
                entity.setMessage(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE)));
                entity.setMessageType(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_TYPE)));
                entity.setMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_STATUS)));
                entity.setMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_TIME_STAMP)));
                entity.setMessageBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_BURN_TIME)));
                entity.setMessageBurnTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_BURN_TIME_STAMP)));
                entity.setMessageMimeType(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_MIME_TYPE)));
                entity.setImagePath(co.getString(co.getColumnIndex(DbConstants.KEY_IMAGE_PATH)));
                entity.setAudioPath(co.getString(co.getColumnIndex(DbConstants.KEY_AUDIO_PATH)));
                entity.setContactPath(co.getString(co.getColumnIndex(DbConstants.KEY_CONTACT_PATH)));
                entity.setFilePath(co.getString(co.getColumnIndex(DbConstants.KEY_FILE_PATH)));
                entity.setVideoPath(co.getString(co.getColumnIndex(DbConstants.KEY_VIDEO_PATH)));
                entity.setCurrentMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_CURRENT_MESSAGE_STATUS)));
                entity.setEddId(co.getString(co.getColumnIndex(DbConstants.KEY_REPLY))); //using this for store ecc id
                entity.setFavourite(co.getInt(co.getColumnIndex(DbConstants.KEY_FAVOURITE)));
                entity.setPinned(co.getInt(co.getColumnIndex(DbConstants.KEY_PINNED)));
                entity.setVisibility(co.getInt(co.getColumnIndex(DbConstants.KEY_VISIBILITY)));
                entity.setMessageEncryptionKey(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ENCRYPTION_KEY)));
                entity.setMessageIv(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_IV)));
                entity.setMessageSharedSecretKey(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_SHARED_SECRET_KEY)));
                entity.setFileName(co.getString(co.getColumnIndex(DbConstants.KEY_FILE_NAME)));
                // Adding item to list
                msgList.add(entity);
            } while (co.moveToNext());
        }
        co.close();
        closedb(db);
        return msgList;
    }

    public ArrayList<ChatMessageEntity> getUnreadMessageList(int chatId, int chatType, int myUserId) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ChatMessageEntity> msgList = new ArrayList<>();

        String sql_query = "SELECT * FROM " + DbConstants.TBL_MESSAGE_LIST + " where " + DbConstants.KEY_CHAT_TYPE + " = " + chatType + " and " + DbConstants.KEY_CHAT_ID + " = " + chatId + " AND " + " ( " + DbConstants.KEY_MESSAGE_STATUS + " != " + AppConstants.MESSAGE_READ_STATUS + " OR " + DbConstants.KEY_MESSAGE_STATUS + " = 0 " + " ) " + " ORDER BY " + DbConstants.KEY_ID + " ASC";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                ChatMessageEntity entity = new ChatMessageEntity();

                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ID)));
                entity.setSenderId(co.getInt(co.getColumnIndex(DbConstants.KEY_SENDER_ID)));
                entity.setMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_STATUS)));
                entity.setMessageBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_BURN_TIME)));
                // Adding item to list
                msgList.add(entity);
            } while (co.moveToNext());
        }
        co.close();
        closedb(db);
        return msgList;
    }

    public ArrayList<ChatMessageEntity> getInProgressSentMessageList(int chatId, int myUserId) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ChatMessageEntity> msgList = new ArrayList<>();
        String dateTime = DateTimeUtils.getCurrentDateTime();
        String sql_query = "SELECT " + DbConstants.KEY_MESSAGE_ID + ", Cast ((JulianDay('" + dateTime + "') - JulianDay(message_time_stamp)) * 24 * 60 * 60 As Integer) AS TimeDiff FROM " + DbConstants.TBL_MESSAGE_LIST + " where " + DbConstants.KEY_CHAT_ID + " = " + chatId + " AND " + DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_SENT_IN_PROGRESS_STATUS + " ORDER BY " + DbConstants.KEY_ID + " ASC";
        Cursor co = db.rawQuery(sql_query, null);

        if (co.moveToFirst()) {
            do {
                ChatMessageEntity entity = new ChatMessageEntity();

                entity.setMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ID)));
                int diff = co.getInt(co.getColumnIndex("TimeDiff"));
                // Adding item to list
                if (diff > 15)
                    msgList.add(entity);
            } while (co.moveToNext());
        }
        co.close();
        closedb(db);
        return msgList;
    }

    //Get Complete Contact List in.
    public ArrayList<ContactEntity> getContactList() {

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ContactEntity> chatList = new ArrayList<>();

        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_CONTACT_LIST + " ORDER BY LOWER(" + DbConstants.KEY_NAME + ") ASC";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                ContactEntity entity = new ContactEntity();
                entity.setId(co.getString(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setUserType(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_TYPE)));
                entity.setUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setEccPublicKey(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_PUBLIC_KEY)));
                entity.setBlockStatus(co.getString(co.getColumnIndex(DbConstants.KEY_BLOCK_STATUS)));
                chatList.add(entity);
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return chatList;
    }


    //Get Chat List Entity.
    public String getContactName(String eccId) {
        SQLiteDatabase db = getReadableDatabase();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_CONTACT_LIST + " WHERE UPPER(" + DbConstants.KEY_ECC_ID + ") = '" + eccId.toUpperCase() + "' ";
        Cursor co = db.rawQuery(sql_query, null);
        String name = "";
        if (co.moveToFirst()) {
            do {
                name = co.getString(co.getColumnIndex(DbConstants.KEY_NAME));
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return name;
    }

    public String getGroupName(String eccId) {
        SQLiteDatabase db = getReadableDatabase();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_CHAT_LIST + " WHERE " + DbConstants.KEY_USER_DB_ID + " = '" + eccId + "' ";
        Cursor co = db.rawQuery(sql_query, null);
        String name = "";
        if (co.moveToFirst()) {
            do {
                name = co.getString(co.getColumnIndex(DbConstants.KEY_NAME));
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return name;
    }

    public String getEccId(int dbId) {
        SQLiteDatabase db = getReadableDatabase();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_CHAT_LIST + " WHERE " + DbConstants.KEY_USER_DB_ID + "=" + dbId ;
        Cursor co = db.rawQuery(sql_query, null);
        String name = "";
        if (co.moveToFirst()) {
            do {
                name = co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID));
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return name;
    }

    //get Accepted request Contact list
    public ArrayList<ContactEntity> getAcceptedContactList() {

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ContactEntity> chatList = new ArrayList<>();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_CONTACT_LIST + " WHERE " + DbConstants.KEY_BLOCK_STATUS + " = " + SocketUtils.accepted + " ORDER BY " + DbConstants.KEY_NAME + "  COLLATE NOCASE ASC";

        // String sql_query = "SELECT  *  FROM " + DbConstants.TBL_CONTACT_LIST ;
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                ContactEntity entity = new ContactEntity();
                entity.setId(co.getString(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setUserType(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_TYPE)));
                entity.setUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setEccPublicKey(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_PUBLIC_KEY)));
                entity.setBlockStatus(co.getString(co.getColumnIndex(DbConstants.KEY_BLOCK_STATUS)));
                chatList.add(entity);
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return chatList;
    }

    //Get Complete Contact List in.
    public ArrayList<GroupMemberEntity> getGroupMemberList(int groupChatId) {

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<GroupMemberEntity> chatList = new ArrayList<>();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_GROUP_CONTACT_LIST + " WHERE " + DbConstants.KEY_CHAT_ID + " = " + groupChatId;
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                GroupMemberEntity entity = new GroupMemberEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setChatId(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_ID)));
                entity.setUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setMemberType(co.getInt(co.getColumnIndex(DbConstants.KEY_MEMBER_TYPE)));
                chatList.add(entity);
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return chatList;
    }

    //Get Contact Public Keys.
    public PublicKeyEntity getPublicKeys(int user_db_id) {

        SQLiteDatabase db = getReadableDatabase();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_PUBLIC_KEY_LIST + " WHERE " + DbConstants.KEY_USER_DB_ID + " = " + user_db_id;
        PublicKeyEntity entity = new PublicKeyEntity();
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                entity.setId(co.getString(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setUserType(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_TYPE)));
                entity.setUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setEccPublicKey(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_PUBLIC_KEY)));
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return entity;
    }

    //Get Contact Public Keys.
    public PublicKeyEntity getPublicKeys(String name) {
        SQLiteDatabase db = getReadableDatabase();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_PUBLIC_KEY_LIST + " WHERE " + DbConstants.KEY_NAME + " = '" + name + "'";
        PublicKeyEntity entity = new PublicKeyEntity();
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                entity.setId(co.getString(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setUserType(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_TYPE)));
                entity.setUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setEccPublicKey(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_PUBLIC_KEY)));
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return entity;
    }

    //Get Contact Public Keys.
    public PublicKeyEntity getPublicKeysByECCId(String eccId) {
        SQLiteDatabase db = getReadableDatabase();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_PUBLIC_KEY_LIST + " WHERE UPPER(" + DbConstants.KEY_ECC_ID + ") = '" + eccId + "'";
        PublicKeyEntity entity = new PublicKeyEntity();
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                entity.setId(co.getString(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setUserType(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_TYPE)));
                entity.setUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setEccPublicKey(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_PUBLIC_KEY)));
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return entity;
    }

    //Get Contact Public Keys.
    public ArrayList<String> getECCid() {
        ArrayList<String> eccid = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_PUBLIC_KEY_LIST + " ORDER BY " + DbConstants.KEY_ECC_ID + " ASC";
        PublicKeyEntity entity = new PublicKeyEntity();
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                eccid.add(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return eccid;
    }

    //Get All Vault Item using Items MIME-TYPE.
    public ArrayList<VaultEntity> getVaultEntityList(int itemMimeType) {

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<VaultEntity> vaultItemList = new ArrayList<>();
        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_VAULT_ITEM_LIST + " WHERE " + DbConstants.KEY_MIME_TYPE + " = " + itemMimeType + " ORDER BY " + "datetime(" + DbConstants.KEY_ITEM_DATE_STAMP + ")" + " DESC";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                VaultEntity entity = new VaultEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setImage(co.getString(co.getColumnIndex(DbConstants.KEY_IMAGE_PATH)));
                entity.setAudio(co.getString(co.getColumnIndex(DbConstants.KEY_AUDIO_PATH)));
                entity.setVideo(co.getString(co.getColumnIndex(DbConstants.KEY_VIDEO_PATH)));
                entity.setNotes(co.getString(co.getColumnIndex(DbConstants.KEY_FILE_PATH)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setDateTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_ITEM_DATE_STAMP)));
                entity.setDate(co.getString(co.getColumnIndex(DbConstants.KEY_DATE)));
                entity.setTotalItem(co.getInt(co.getColumnIndex(DbConstants.KEY_TOTAL_ITEM)));
                entity.setParentId(co.getInt(co.getColumnIndex(DbConstants.KEY_PARENT_ID)));
                entity.setItemType(co.getInt(co.getColumnIndex(DbConstants.KEY_ITEM_TYPE)));
                entity.setIconType(co.getInt(co.getColumnIndex(DbConstants.KEY_ICON_TYPE)));
                entity.setMimeType(co.getInt(co.getColumnIndex(DbConstants.KEY_MIME_TYPE)));
                entity.setDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setMessageID(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ID)));

                vaultItemList.add(entity);
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return vaultItemList;
    }  //Get All Vault Item using Items MIME-TYPE.


    public ArrayList<VaultEntity> getTinyImages() {
        String filter = "/tiny/tiny";

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<VaultEntity> vaultItemList = new ArrayList<>();
        Cursor co = db.query(true, DbConstants.TBL_VAULT_ITEM_LIST, null, DbConstants.KEY_IMAGE_PATH + " LIKE ?", new String[]{"%" + filter + "%"}, null, null, null, null);
        if (co.moveToFirst()) {
            do {
                VaultEntity entity = new VaultEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setImage(co.getString(co.getColumnIndex(DbConstants.KEY_IMAGE_PATH)));
                entity.setAudio(co.getString(co.getColumnIndex(DbConstants.KEY_AUDIO_PATH)));
                entity.setVideo(co.getString(co.getColumnIndex(DbConstants.KEY_VIDEO_PATH)));
                entity.setNotes(co.getString(co.getColumnIndex(DbConstants.KEY_FILE_PATH)));
                entity.setEccId(co.getString(co.getColumnIndex(DbConstants.KEY_ECC_ID)));
                entity.setDateTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_ITEM_DATE_STAMP)));
                entity.setDate(co.getString(co.getColumnIndex(DbConstants.KEY_DATE)));
                entity.setTotalItem(co.getInt(co.getColumnIndex(DbConstants.KEY_TOTAL_ITEM)));
                entity.setParentId(co.getInt(co.getColumnIndex(DbConstants.KEY_PARENT_ID)));
                entity.setItemType(co.getInt(co.getColumnIndex(DbConstants.KEY_ITEM_TYPE)));
                entity.setIconType(co.getInt(co.getColumnIndex(DbConstants.KEY_ICON_TYPE)));
                entity.setMimeType(co.getInt(co.getColumnIndex(DbConstants.KEY_MIME_TYPE)));
                entity.setDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_USER_DB_ID)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setMessageID(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ID)));

                vaultItemList.add(entity);
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return vaultItemList;
    }


    //Get Old Messages messageId list.
    public ArrayList<String> getOldMessageMessageId(int chatId) {
        ArrayList<String> oldMessageList = new ArrayList<>();
        String countQuery = "SELECT * FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE " + DbConstants.KEY_CHAT_ID + " = " + chatId + " AND " + DbConstants.KEY_MESSAGE_BURN_TIME_STAMP + " < '" + DateTimeUtils.getCurrentDateTime() + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor co = db.rawQuery(countQuery, null);
        if (co.moveToFirst()) {
            do {
                oldMessageList.add(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ID)));
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        closedb(db);
        return oldMessageList;
    }

    public long deleteMessageListEntity(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_VAULT_MESSAGE_LIST, DbConstants.KEY_CHAT_ID + " = " + userId, null);
        closedb(db);
        return result;
    }

    // Get Complete Vault Message List of different users.
    public ArrayList<ChatMessageEntity> getVaultMessageList(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ChatMessageEntity> msgList = new ArrayList<>();

        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_VAULT_MESSAGE_LIST + " where " + DbConstants.KEY_CHAT_ID + " = " + userId + " ORDER BY " + DbConstants.KEY_ID + " ASC";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                ChatMessageEntity entity = new ChatMessageEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setChatId(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_ID)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setChatUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_USER_DB_ID)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ID)));
                entity.setSenderId(co.getInt(co.getColumnIndex(DbConstants.KEY_SENDER_ID)));
                entity.setReceiverId(co.getInt(co.getColumnIndex(DbConstants.KEY_RECEIVER_ID)));
                entity.setMessage(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE)));
                entity.setMessageType(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_TYPE)));
                entity.setMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_STATUS)));
                entity.setMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_TIME_STAMP)));
                entity.setMessageBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_BURN_TIME)));
                entity.setMessageBurnTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_BURN_TIME_STAMP)));
                entity.setMessageMimeType(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_MIME_TYPE)));
                entity.setImagePath(co.getString(co.getColumnIndex(DbConstants.KEY_IMAGE_PATH)));
                entity.setAudioPath(co.getString(co.getColumnIndex(DbConstants.KEY_AUDIO_PATH)));
                entity.setContactPath(co.getString(co.getColumnIndex(DbConstants.KEY_CONTACT_PATH)));
                entity.setFilePath(co.getString(co.getColumnIndex(DbConstants.KEY_FILE_PATH)));
                entity.setVideoPath(co.getString(co.getColumnIndex(DbConstants.KEY_VIDEO_PATH)));
                entity.setCurrentMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_CURRENT_MESSAGE_STATUS)));
                entity.setEddId(co.getString(co.getColumnIndex(DbConstants.KEY_REPLY)));
                entity.setFavourite(co.getInt(co.getColumnIndex(DbConstants.KEY_FAVOURITE)));
                entity.setPinned(co.getInt(co.getColumnIndex(DbConstants.KEY_PINNED)));
                entity.setVisibility(co.getInt(co.getColumnIndex(DbConstants.KEY_VISIBILITY)));
                entity.setMessageEncryptionKey(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ENCRYPTION_KEY)));
                entity.setMessageIv(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_IV)));
                entity.setMessageSharedSecretKey(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_SHARED_SECRET_KEY)));
                // Adding item to list
                msgList.add(entity);
            } while (co.moveToNext());
        }
        co.close();
        closedb(db);
        return msgList;
    }

    /**
     * Database UPDATE Queries
     * Update the current data of database contents.
     */

    //Update Query - common for everyone.
    public long updateDb(String TableName, String fieldName, String entity, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(TableName, values, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long updateDb(String TableName, String fieldName, int entity, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(TableName, values, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long updateDb(String TableName, String fieldName, int entity, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(TableName, values, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    public long updateDb(String TableName, String fieldName, String entity, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(TableName, values, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }


    //Update Single Entity of TBL_CHAT_LIST

    public long updateChatListEntity(String fieldName, String entity, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_CHAT_LIST, values, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long updateChatListEntity(String fieldName, int entity, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_CHAT_LIST, values, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long updateChatListEntity(String fieldName, int entity, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_CHAT_LIST, values, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    public long updateChatListEntity(String fieldName, String entity, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_CHAT_LIST, values, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    public long updateGroupChatListName(int groupDbId, String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_NAME, groupName);
        long result = db.update(DbConstants.TBL_CHAT_LIST, values, DbConstants.KEY_USER_DB_ID + "=" + groupDbId + " AND " + DbConstants.KEY_CHAT_TYPE + " = " + AppConstants.GROUP_CHAT_TYPE, null);
        closedb(db);
        return result;
    }

    public long updateChatListTimeStamp(int groupDbId, String newDateTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_MESSAGE_TIME_STAMP, newDateTime);
        long result = db.update(DbConstants.TBL_CHAT_LIST, values, DbConstants.KEY_USER_DB_ID + "=" + groupDbId, null);
        closedb(db);
        return result;
    }

    public long updateChatListBurnTime(int id, int burnTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_BURN_TIME, burnTime);
        long result = db.update(DbConstants.TBL_CHAT_LIST, values, DbConstants.KEY_ID + "=" + id, null);
        closedb(db);
        return result;
    }


    public long updateGroupList(String name, int groupId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_NAME, name);
        long result = db.update(DbConstants.TBL_CHAT_LIST, values, DbConstants.KEY_USER_DB_ID + "=" + groupId, null);
        closedb(db);
        return result;
    }


    //Update Single Entity of TBL_MESSAGE_LIST

    public long updateMessageListEntity(String fieldName, String entity, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long updateMessageListEntity(String fieldName, int entity, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long updateMessageListEntity(String fieldName, int entity, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    public long updateMessageListEntity(String fieldName, String entity, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    public long updateMessage(String messageId, String msgBurnDateTime, int currentMessageStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_CURRENT_MESSAGE_STATUS, currentMessageStatus);
        values.put(DbConstants.KEY_MESSAGE_BURN_TIME_STAMP, msgBurnDateTime);
        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, DbConstants.KEY_MESSAGE_ID + "=" + messageId, null);
        closedb(db);
        return result;
    }

    public long updateMessageStatusByMessageId(String messageId, int messageStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_MESSAGE_STATUS, messageStatus);
        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, DbConstants.KEY_MESSAGE_ID + "=" + messageId, null);
        closedb(db);
        return result;
    }

    public long updateMessageFilePathByMessageId(String messageId, String filePath,int mimeType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (mimeType == AppConstants.MIME_TYPE_AUDIO) {
            values.put(DbConstants.KEY_AUDIO_PATH, filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_VIDEO) {
            values.put(DbConstants.KEY_VIDEO_PATH, filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_NOTE) {
            values.put(DbConstants.KEY_FILE_PATH, filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_IMAGE) {
            values.put(DbConstants.KEY_IMAGE_PATH, filePath);
        } else if (mimeType == AppConstants.MIME_TYPE_CONTACT) {
            values.put(DbConstants.KEY_CONTACT_PATH, filePath);
        }

        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, DbConstants.KEY_MESSAGE_ID + "=" + messageId, null);
        closedb(db);
        return result;
    }

    public long updateMessageTextByMessageId(String messageId, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_MESSAGE, message);
        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, DbConstants.KEY_MESSAGE_ID + "=" + messageId, null);
        closedb(db);
        return result;
    }

    public long updateParentMessageIDByMessageId(String messageId, String parentMessageID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_PARENT_MESSAGE_ID, parentMessageID);
        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, DbConstants.KEY_MESSAGE_ID + "=" + messageId, null);
        closedb(db);
        return result;
    }

    public long updateMessageIDByParentMessageId(String messageId, String parentMessageID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_MESSAGE_ID, messageId);
        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, DbConstants.KEY_PARENT_MESSAGE_ID + "=" + parentMessageID, null);
        closedb(db);
        return result;
    }

    public long updateMessageBurnDate(String messageId, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_MESSAGE_BURN_TIME_STAMP, date);
        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, DbConstants.KEY_MESSAGE_ID + "=" + messageId, null);
        closedb(db);
        return result;
    }

    public long updateIsRevised(String messageId, int isRevised) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_IS_REVISED, isRevised);
        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, DbConstants.KEY_MESSAGE_ID + "=" + messageId, null);
        closedb(db);
        return result;
    }

    public long updateMessageTimeStamp(String messageId, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_MESSAGE_TIME_STAMP, date);
        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, DbConstants.KEY_MESSAGE_ID + "=" + messageId, null);
        closedb(db);
        return result;
    }

    public long updateEditedMessageTimeStamp(String messageId, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_EDITED_MESSAGE_TIME, date);
        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, DbConstants.KEY_MESSAGE_ID + "=" + messageId, null);
        closedb(db);
        return result;
    }

    public long updateMimeTime(String messageId, int mimeType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_MESSAGE_MIME_TYPE, mimeType);
        long result = db.update(DbConstants.TBL_MESSAGE_LIST, values, DbConstants.KEY_MESSAGE_ID + "=" + messageId, null);
        closedb(db);
        return result;
    }
    //Update C
    //Update Contact Entity of TBL_CONTACT_LIST

    public long updateContactEntity(String fieldName, String entity, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_CONTACT_LIST, values, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long updateContactEntity(String fieldName, int entity, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_CONTACT_LIST, values, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long updateContactEntity(String fieldName, int entity, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_CONTACT_LIST, values, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    public long updateContactEntity(String fieldName, String entity, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_CONTACT_LIST, values, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    //Update Group Contact Entity of TBL_GROUP_CONTACT_LIST

    public long updateGroupMember(String fieldName, String entity, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_GROUP_CONTACT_LIST, values, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long updateGroupMember(String fieldName, int entity, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_GROUP_CONTACT_LIST, values, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long updateGroupMember(String fieldName, int entity, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_GROUP_CONTACT_LIST, values, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    public long updateGroupMember(String fieldName, String entity, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_GROUP_CONTACT_LIST, values, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    //Update Public Key Entity of TBL_PUBLIC_KEY_LIST

    public long updatePublicKey(String fieldName, String entity, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_PUBLIC_KEY_LIST, values, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long updatePublicKey(String fieldName, int entity, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_PUBLIC_KEY_LIST, values, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long updatePublicKey(String fieldName, int entity, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_PUBLIC_KEY_LIST, values, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    public long updatePublicKey(String fieldName, String entity, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_PUBLIC_KEY_LIST, values, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    public long updatePublicKey(String eccId, String ecc_public_key) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_ECC_PUBLIC_KEY, ecc_public_key);
        long result = db.update(DbConstants.TBL_PUBLIC_KEY_LIST, values, DbConstants.KEY_ECC_ID + " = '" + eccId + "'", null);
        closedb(db);
        return result;
    }

    public long updatePublicKey(int userDbId, String ecc_public_key) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_ECC_PUBLIC_KEY, ecc_public_key);
        long result = db.update(DbConstants.TBL_PUBLIC_KEY_LIST, values, DbConstants.KEY_USER_DB_ID + " = " + userDbId, null);
        closedb(db);
        return result;
    }

    //Update Vault Item Entity of TBL_VAULT_ITEM_LIST

    public long updateVaultItem(String fieldName, String entity, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_VAULT_ITEM_LIST, values, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long updateVaultItem(String fieldName, int entity, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_VAULT_ITEM_LIST, values, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long updateVaultItem(String fieldName, int entity, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_VAULT_ITEM_LIST, values, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    public long updateVaultItem(String fieldName, String entity, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fieldName, entity);
        long result = db.update(DbConstants.TBL_VAULT_ITEM_LIST, values, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    public long updateVaultItemName(String name, int localId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_NAME, name);
        long result = db.update(DbConstants.TBL_VAULT_ITEM_LIST, values, DbConstants.KEY_ID + "=" + localId, null);
        closedb(db);
        return result;
    }


    public long updateVaultItemName(String name, String eccId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.KEY_NAME, name);
        long result = db.update(DbConstants.TBL_VAULT_ITEM_LIST, values, DbConstants.KEY_ECC_ID + "= '" + eccId + "'", null);
        closedb(db);
        return result;
    }

    //Delete Query - common for everyone.
    public long deleteDb(String TableName, String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TableName, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long deleteDb(String TableName, String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TableName, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    //Delete Single Entity of TBL_CHAT_LIST

    public long deleteChatList(String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_CHAT_LIST, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long deleteChatList(String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_CHAT_LIST, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }
    //Delete All Chatlist

    public long deleteAllChatList() {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_CHAT_LIST, null, null);
        closedb(db);
        return result;
    }

    public long deleteAllGroupsChatList() {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_CHAT_LIST, DbConstants.KEY_CHAT_TYPE + "=" + AppConstants.GROUP_CHAT_TYPE, null);
        closedb(db);
        return result;
    }


    //Delete All TBL_MESSAGE_LIST


    public long deleteAllMessageListEntity() {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_MESSAGE_LIST, null, null);
        closedb(db);
        return result;
    }

    public long deleteAllGroupMessageListEntity() {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_MESSAGE_LIST, DbConstants.KEY_CHAT_TYPE + "=" + AppConstants.GROUP_CHAT_TYPE, null);
        closedb(db);
        return result;
    }

    //Delete Single Entity of TBL_MESSAGE_LIST

    public long deleteMessageListEntity(String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_MESSAGE_LIST, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }


    public long deleteMessageListEntity(String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_MESSAGE_LIST, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    //Delete All old messages from list.
    public long deleteOldMessage(int chatId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // long result = db.delete(DbConstants.TBL_MESSAGE_LIST, DbConstants.KEY_CHAT_ID + " = " + chatId + " AND " + DbConstants.KEY_MESSAGE_BURN_TIME_STAMP + " < '" + DateTimeUtils.getCurrentDateTime() + "'" + " AND " + "(" + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_SENT_STATUS + " OR " + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_READ_STATUS + " OR " + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_STATUS_DELIVERED +")", null);
        System.out.println(DbConstants.TBL_MESSAGE_LIST + DbConstants.KEY_MESSAGE_BURN_TIME_STAMP + " < '" + DateTimeUtils.getCurrentDateTime() + "'" + " AND " + "(" + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_SENT_STATUS + " OR " + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_READ_STATUS + " OR " + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_STATUS_DELIVERED + ")");
        long result = db.delete(DbConstants.TBL_MESSAGE_LIST, DbConstants.KEY_MESSAGE_BURN_TIME_STAMP + " < '" + DateTimeUtils.getCurrentDateTime() + "'" + " AND " + "(" + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_SENT_STATUS + " OR " + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_READ_STATUS + " OR " + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_STATUS_DELIVERED + ")", null);

        closedb(db);
        return result;
    }

    public long deleteUnreadMessage(int chatId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // long result = db.delete(DbConstants.TBL_MESSAGE_LIST, DbConstants.KEY_CHAT_ID + " = " + chatId + " AND " + DbConstants.KEY_MESSAGE_BURN_TIME_STAMP + " < '" + DateTimeUtils.getCurrentDateTime() + "'" + " AND " + "(" + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_SENT_STATUS + " OR " + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_READ_STATUS + " OR " + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_STATUS_DELIVERED +")", null);
        System.out.println(DbConstants.TBL_MESSAGE_LIST + DbConstants.KEY_MESSAGE_BURN_TIME_STAMP + " < '" + DateTimeUtils.getCurrentDateTime() + "'" + " AND " + "(" + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_SENT_STATUS + " OR " + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_READ_STATUS + " OR " + DbConstants.KEY_MESSAGE_STATUS + "=" + AppConstants.MESSAGE_STATUS_DELIVERED + ")");
        long result = db.delete(DbConstants.TBL_MESSAGE_LIST, DbConstants.KEY_CHAT_ID + " = " + chatId, null);
        closedb(db);
        return result;
    }

    //Delete Single Entity of TBL_CONTACT_LIST

    public long deleteContactEntity(String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_CONTACT_LIST, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long deleteContactEntity(String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_CONTACT_LIST, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    public long deleteContact(String eccId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_CONTACT_LIST, DbConstants.KEY_ECC_ID + "='" + eccId + "'", null);
        closedb(db);
        return result;
    }

    public long deleteContact(int userDbId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_CONTACT_LIST, DbConstants.KEY_USER_DB_ID + "=" + userDbId, null);
        closedb(db);
        return result;
    }
    //delete all contact list
    public long deleteAllContacts() {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_CONTACT_LIST, null, null);
        closedb(db);
        return result;
    }


    //Delete Group Member of TBL_GROUP_CONTACT_LIST
    public long deleteGroupMember(String whereField, String whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_GROUP_CONTACT_LIST, whereField + "='" + whereValue + "'", null);
        closedb(db);
        return result;
    }

    public long deleteGroupMember(String whereField, int whereValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_GROUP_CONTACT_LIST, whereField + "=" + whereValue, null);
        closedb(db);
        return result;
    }

    public long deleteGroupMember(int groupId, String eccId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_GROUP_CONTACT_LIST, DbConstants.KEY_CHAT_ID + "=" + groupId + " AND LOWER(" + DbConstants.KEY_ECC_ID + ") = '" + eccId.toLowerCase().trim() + "'", null);
        closedb(db);
        return result;
    }

    public long deleteGroupMember(int groupId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_GROUP_CONTACT_LIST, DbConstants.KEY_CHAT_ID + "=" + groupId , null);
        closedb(db);
        return result;
    }

    public long deleteGroupMembers(int groupId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_GROUP_CONTACT_LIST, DbConstants.KEY_CHAT_ID + "=" + groupId, null);
        closedb(db);
        return result;
    }

    //Deletes all group members from the chat list.
    public long deleteAllGroupChatMembers() {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_GROUP_CONTACT_LIST, null, null);
        closedb(db);
        return result;
    }

    //Delete Group Member of TBL_PUBLIC_KEY_LIST

    public long deletePublicKey(String eccId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_PUBLIC_KEY_LIST, DbConstants.KEY_ECC_ID + "='" + eccId + "'", null);
        closedb(db);
        return result;
    }

    public long deletePublicKey(int userDbId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_PUBLIC_KEY_LIST, DbConstants.KEY_USER_DB_ID + "=" + userDbId, null);
        closedb(db);
        return result;
    }
    //all public key delete
    public long deleteAllPublicKey() {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_PUBLIC_KEY_LIST, null, null);
        closedb(db);
        return result;
    }

    //Delete Vault Item
    public long deleteVaultItem(int keyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_VAULT_ITEM_LIST, DbConstants.KEY_ID + "=" + keyId, null);
        closedb(db);
        return result;
    }

    //Delete Vault Item
    public long deleteVaultItembyPath(String key, String vaule) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_VAULT_ITEM_LIST, key + "='" + vaule + "'", null);
        closedb(db);
        return result;
    }


    //Delete Vault Item
    public long deleteVaultItem(String vaule) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(DbConstants.TBL_VAULT_ITEM_LIST, DbConstants.KEY_FILE_PATH + "=" + " ?", new String[]{vaule});
        closedb(db);
        return result;
    }

    private void closedb(SQLiteDatabase db) {
        db.close();
    }

    //Check whether user have chat list available or not.
    public boolean checkUserHaveChatList(String eccId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_CHAT_LIST + " WHERE UPPER(" + DbConstants.KEY_ECC_ID + ") = '" + eccId.toUpperCase() + "' ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    public boolean checkUserHaveChatList(int userDbId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_CHAT_LIST + " WHERE " + DbConstants.KEY_USER_DB_ID + " = " + userDbId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }


    public boolean isRequestExist(String uniqueId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_SOCKET_REQUEST + " WHERE " + DbConstants.KEY_MESSAGE_ID + " = " + uniqueId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }


    public boolean checkUserHasFriend(String eccId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_CONTACT_LIST + " WHERE UPPER(" + DbConstants.KEY_ECC_ID + ") = '" + eccId.toUpperCase() + "' AND " + DbConstants.KEY_BLOCK_STATUS + " = " + 1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    public boolean checkUserisFriend(String eccId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_CONTACT_LIST + " WHERE UPPER(" + DbConstants.KEY_ECC_ID + ") = '" + eccId.toUpperCase() + "' ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    public int getTotalFirendrequest() {
        String countQuery = "SELECT  *  FROM " + DbConstants.TBL_CONTACT_LIST + " WHERE " + DbConstants.KEY_BLOCK_STATUS + " = " + SocketUtils.request;

        //String countQuery = "SELECT  *  FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE " + DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_UNREAD_STATUS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public int getTotalUnreadMessages() {
        // String countQuery = "SELECT  *  FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE " + DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_UNREAD_STATUS + " AND " + DbConstants.KEY_IS_REVISED + " != " + AppConstants.revised;
        //String countQuery = "SELECT  *  FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE " + DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_UNREAD_STATUS;
        String countQuery = "SELECT ALL * FROM " + DbConstants.TBL_MESSAGE_LIST + " GROUP BY " + DbConstants.KEY_MESSAGE_ID + " HAVING " + DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_UNREAD_STATUS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public int getTotalUnreadSMessages() {
        String countQuery = "SELECT  *  FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE " + DbConstants.KEY_CHAT_TYPE + " = " + AppConstants.SINGLE_CHAT_TYPE + " AND " + DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_UNREAD_STATUS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public int getTotalUnreadGMessages() {
        //String countQuery = "SELECT  *  FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE " + DbConstants.KEY_CHAT_TYPE + " = " + AppConstants.GROUP_CHAT_TYPE + " AND " + DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_UNREAD_STATUS;
        String countQuery = "SELECT  *  FROM " + DbConstants.TBL_MESSAGE_LIST +" GROUP BY " + DbConstants.KEY_MESSAGE_ID + " HAVING " + DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_UNREAD_STATUS  + " AND " + DbConstants.KEY_CHAT_TYPE + " = " + AppConstants.GROUP_CHAT_TYPE ;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }


    //Get total number of UnRead messages of chat list.
    public ArrayList<ChatMessageEntity> getTotalUnreadMessageList() {

        ArrayList<ChatMessageEntity> messages = new ArrayList<>();

        String countQuery = "SELECT  *  FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE " + DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_UNREAD_STATUS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor co = db.rawQuery(countQuery, null);
        if (co.moveToFirst()) {
            do {
                ChatMessageEntity entity = new ChatMessageEntity();
                entity.setId(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)));
                entity.setChatId(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_ID)));
                entity.setChatType(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_TYPE)));
                entity.setChatUserDbId(co.getInt(co.getColumnIndex(DbConstants.KEY_CHAT_USER_DB_ID)));
                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ID)));
                //revised text
                entity.setParentMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_PARENT_MESSAGE_ID)));
                entity.setIsRevised(co.getInt(co.getColumnIndex(DbConstants.KEY_IS_REVISED)));
                entity.setEditedMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_EDITED_MESSAGE_TIME)));
                entity.setSenderId(co.getInt(co.getColumnIndex(DbConstants.KEY_SENDER_ID)));
                entity.setReceiverId(co.getInt(co.getColumnIndex(DbConstants.KEY_RECEIVER_ID)));
                entity.setMessage(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE)));
                entity.setMessageType(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_TYPE)));
                entity.setMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_STATUS)));
                entity.setMessageTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_TIME_STAMP)));
                entity.setMessageBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_BURN_TIME)));
                entity.setMessageBurnTimeStamp(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_BURN_TIME_STAMP)));
                entity.setMessageMimeType(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_MIME_TYPE)));
                entity.setImagePath(co.getString(co.getColumnIndex(DbConstants.KEY_IMAGE_PATH)));
                entity.setAudioPath(co.getString(co.getColumnIndex(DbConstants.KEY_AUDIO_PATH)));
                entity.setContactPath(co.getString(co.getColumnIndex(DbConstants.KEY_CONTACT_PATH)));
                entity.setFilePath(co.getString(co.getColumnIndex(DbConstants.KEY_FILE_PATH)));
                entity.setVideoPath(co.getString(co.getColumnIndex(DbConstants.KEY_VIDEO_PATH)));
                entity.setCurrentMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_CURRENT_MESSAGE_STATUS)));
                entity.setEddId(co.getString(co.getColumnIndex(DbConstants.KEY_REPLY))); //using this for store ecc id
                entity.setFavourite(co.getInt(co.getColumnIndex(DbConstants.KEY_FAVOURITE)));
                entity.setPinned(co.getInt(co.getColumnIndex(DbConstants.KEY_PINNED)));
                entity.setVisibility(co.getInt(co.getColumnIndex(DbConstants.KEY_VISIBILITY)));
                entity.setMessageEncryptionKey(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ENCRYPTION_KEY)));
                entity.setMessageIv(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_IV)));
                entity.setMessageSharedSecretKey(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_SHARED_SECRET_KEY)));
                // Adding item to list
                messages.add(entity);
            } while (co.moveToNext());
        }
        co.close();
        db.close();
        return messages;
    }

    public int getTotalVaultItem(int itemtype) {
        String countQuery = "SELECT  *  FROM " + DbConstants.TBL_VAULT_ITEM_LIST + " WHERE " + DbConstants.KEY_MIME_TYPE + " = " + itemtype;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    //Check whether the public keys are present or not of any user.
    public boolean checkPublicKeysOfUser(String eccId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_PUBLIC_KEY_LIST + " WHERE UPPER(" + DbConstants.KEY_ECC_ID + ") = '" + eccId.toUpperCase() + "' ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }


    public boolean checkGroupMember(String eccId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_GROUP_CONTACT_LIST + " WHERE UPPER(" + DbConstants.KEY_ECC_ID + ") = '" + eccId.toUpperCase() + "' ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    public boolean checkGroupMember(int groupId, String eccId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_GROUP_CONTACT_LIST + " WHERE UPPER(" + DbConstants.KEY_ECC_ID + ") = '" + eccId.toUpperCase() + "' AND " + DbConstants.KEY_CHAT_ID + " = " + groupId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }


    public boolean checkPublicKeysOfUser(int userDbId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_PUBLIC_KEY_LIST + " WHERE " + DbConstants.KEY_USER_DB_ID + " = " + userDbId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    //Get Group KEY_ID from KEY_USER_DB_ID.
    public int getGroupId(int groupUserDbId) {
        int id = 0;
        String selectQuery = "SELECT  *  FROM " + DbConstants.TBL_CHAT_LIST + " WHERE " + DbConstants.KEY_USER_DB_ID + " = " + groupUserDbId + " AND " + DbConstants.KEY_CHAT_TYPE + " = " + AppConstants.GROUP_CHAT_TYPE;

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndex(DbConstants.KEY_ID));
            } else {
                id = 0;
            }
        }
        cursor.close();
        closedb(database);
        return id;
    }

    public boolean checkIfGroupExist(int groupUserDbId) {
        String countQuery = "SELECT  *  FROM " + DbConstants.TBL_CHAT_LIST + " WHERE " + DbConstants.KEY_USER_DB_ID + " = " + groupUserDbId + " AND " + DbConstants.KEY_CHAT_TYPE + " = " + AppConstants.GROUP_CHAT_TYPE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    //Check whether contact exists in Database or Not.
    public boolean checkContact(int userDbId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_CONTACT_LIST + " WHERE " + DbConstants.KEY_USER_DB_ID + " = " + userDbId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    //Check whether contact exists in Database or Not.
    public boolean checkContactName(String contactName) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_CONTACT_LIST + " WHERE " + DbConstants.KEY_NAME + " = '" + contactName + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    //Check whether contact exists in Database or Not.
    public boolean checkMessageId(String messageId, int senderUserId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE " + DbConstants.KEY_MESSAGE_ID + " = '" + messageId + "' AND " + DbConstants.KEY_SENDER_ID + " = " + senderUserId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    public boolean checkParentMessageId(String messageId, int senderUserId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE " + DbConstants.KEY_PARENT_MESSAGE_ID + " = '" + messageId + "' AND " + DbConstants.KEY_SENDER_ID + " = " + senderUserId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    public boolean checkMessageIdVault(String messageId, int senderUserId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_VAULT_MESSAGE_LIST + " WHERE " + DbConstants.KEY_MESSAGE_ID + " = '" + messageId + "' AND " + DbConstants.KEY_SENDER_ID + " = " + senderUserId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    //Check whether contact exists in Database or Not.
    public boolean checkMessageId(String messageId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE " + DbConstants.KEY_MESSAGE_ID + " = '" + messageId + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    public int getMessageStatus(String messageId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE " + DbConstants.KEY_MESSAGE_ID + " = '" + messageId + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int status = 2;
        if (cursor.moveToFirst()) {
            do {
                status = cursor.getInt(cursor.getColumnIndex(DbConstants.KEY_MESSAGE_STATUS));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return status;
    }

    public boolean checkContact(String eccId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_CONTACT_LIST + " WHERE UPPER(" + DbConstants.KEY_ECC_ID + ") = '" + eccId.toUpperCase() + "' ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    public boolean checkPersonalNote(String fileName) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_VAULT_ITEM_LIST + " WHERE " + DbConstants.KEY_NAME + " = " + "?" + " AND " + DbConstants.KEY_MIME_TYPE + "=" + " ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, new String[]{fileName, "1"});
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    public boolean checkPersonalNoteName(String fileName) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_VAULT_ITEM_LIST + " WHERE " + DbConstants.KEY_NAME + " = " + "?" + " AND " + DbConstants.KEY_MIME_TYPE + "=" + AppConstants.ITEM_TYPE_NOTES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, new String[]{fileName});
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    public boolean checkImageName(String fileName) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_VAULT_ITEM_LIST + " WHERE " + DbConstants.KEY_NAME + " = " + "?" + " AND " + DbConstants.KEY_MIME_TYPE + "=" + AppConstants.ITEM_TYPE_PICTURE;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(countQuery, new String[]{fileName});
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    public boolean checkImage(String messageID) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_VAULT_ITEM_LIST + " WHERE " + DbConstants.KEY_MESSAGE_ID + " = " + "?" + " AND " + DbConstants.KEY_MIME_TYPE + "=" + " ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, new String[]{messageID, "2"});
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt > 0;
    }

    //Check Whether messages are old or not.
    public int checkMessageAge(int chatId) {
        String countQuery = "SELECT * FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE " + DbConstants.KEY_CHAT_ID + " = " + chatId + " AND " + DbConstants.KEY_MESSAGE_BURN_TIME_STAMP + " < '" + DateTimeUtils.getCurrentDateTime() + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        closedb(db);
        return cnt;
    }


    //Get Complete Contact List in.
    public HashMap<Integer, String> getOldMessageArray(int chatId) {

        SQLiteDatabase db = getReadableDatabase();
        HashMap<Integer, String> multiMap = new HashMap<Integer, String>();
        String sql_query = "SELECT * FROM " + DbConstants.TBL_MESSAGE_LIST + " WHERE " + DbConstants.KEY_CHAT_ID + " = " + chatId + " AND " + DbConstants.KEY_MESSAGE_BURN_TIME_STAMP + " < '" + DateTimeUtils.getCurrentDateTime() + "'";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                multiMap.put(co.getInt(co.getColumnIndex(DbConstants.KEY_ID)), co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ID)));
            } while (co.moveToNext());
        }
        if (co != null)
            co.close();
        db.close();
        return multiMap;
    }

    public ArrayList<ChatMessageEntity> getReadUnSendAckMessages() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ChatMessageEntity> msgList = new ArrayList<>();

        String sql_query = "SELECT * FROM " + DbConstants.TBL_MESSAGE_LIST + " where " + DbConstants.KEY_MESSAGE_STATUS + " = " + AppConstants.MESSAGE_STATUS_READ_BUT_UN_ACK + " ORDER BY " + DbConstants.KEY_ID + " ASC";
        Cursor co = db.rawQuery(sql_query, null);
        if (co.moveToFirst()) {
            do {
                ChatMessageEntity entity = new ChatMessageEntity();

                entity.setName(co.getString(co.getColumnIndex(DbConstants.KEY_NAME)));
                entity.setMessageId(co.getString(co.getColumnIndex(DbConstants.KEY_MESSAGE_ID)));
                entity.setSenderId(co.getInt(co.getColumnIndex(DbConstants.KEY_SENDER_ID)));
                entity.setMessageStatus(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_STATUS)));
                entity.setMessageBurnTime(co.getInt(co.getColumnIndex(DbConstants.KEY_MESSAGE_BURN_TIME)));
                // Adding item to list
                msgList.add(entity);
            } while (co.moveToNext());
        }
        co.close();
        closedb(db);
        return msgList;
    }

    public int getMessageCount(int chatId, int chatType) {
        SQLiteDatabase db = getReadableDatabase();

        String sql_query = "SELECT  *  FROM " + DbConstants.TBL_MESSAGE_LIST + " where " + DbConstants.KEY_CHAT_TYPE + " = " + chatType + "  and  " + DbConstants.KEY_CHAT_ID + " = " + chatId + " ORDER BY " + DbConstants.KEY_MESSAGE_TIME_STAMP + " ASC";
        Cursor co = db.rawQuery(sql_query, null);

        int count = co.getCount();
        co.close();
        closedb(db);
        return count;
    }


}
