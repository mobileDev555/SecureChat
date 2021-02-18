package com.realapps.chat.utils;

import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.services.MessageClass;

import org.java_websocket.client.WebSocketClient;

import java.util.ArrayList;

/**
 * Created by Prashant Sharma on 08/01/17.
 * Core techies
 * prashant@coretechies.org
 */
public final class AppConstants {
    public static final String EXTRA_GROUP_MEMBER = "com.realapps.chat.utils:group_member";
    public static final String EXTRA_SELECTED_CONTACT = "com.realapps.chat.utils:selected_contact:";
    public static final String EXTRA_SELECTED_VAULTITES = "com.realapps.chat.utils:selected_vault:";
    public static final String EXTRA_ITEM_TYPE = "com.realapps.chat.utils:item_type:";
    public static final String EXTRA_CONTACT_ENTITY = "com.realapps.chat.contactentity";
    public static final String EXTRA_PERSONAL_NOTE_FILE_NAME = "com.realapps.chat.file.name";
    public static final String EXTRA_PERSONAL_NOTE_FILE_PATH = "com.realapps.chat.file.path";
    public static final String EXTRA_GROUP_ID = "com.realapps.chat.group.id";
    public static final String EXTRA_GROUP_NAME = "com.realapps.chat.group.name";
    public static final String EXTRA_SERVER_ID = "com.realapps.chat.group.serverid";
    public static final String EXTRA_VAULT_LIST_ITEM = "com.realapps.chat.utils:vault_list_item";
    public static final String EXTRA_IS_SHARE = "com.realapps.chat.utils:is_share";
    public static final String EXTRA_VAULT_MESSAGE = "com.realapps.chat.utils:vault_message";
    public static final String EXTRA_FROM_NOTIFICATION = "com.realapps.chat.utils:from_notification";
    public static final String EXTRA_ACTIVITY_TYPE = "com.realapps.chat.utils:activity_type";
    public static final String EXTRA_CHAT_ID = "com.realapps.chat.utils:chat_id";
    public static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";
    public static Boolean isbackground = false;
    //public static long lockTime = 500;
    public static int lockTime = 2;
    public static int lockTime1minute = 1000 * 60;
    public static int lockTime2minute = 1000 * 2 * 60;
    public static int lockTime3minute = 1000 * 3 * 60;
    public final static int CAMERA_RQ = 6969;
    //Extras Constant with Intent Object Passing.
    public static final String EXTRA_CHAT_LIST_ITEM = "extra_chat_list_item";
    public static final String TYPE_FRAGMENT = "type_fragment";
    public static final String EXTRA_MESSAGE_ID = "extra_message_id";
    // MIME TYPE - All messages MIME-TYPE
    public static final int MIME_TYPE_TEXT = 1;
    public static final int MIME_TYPE_IMAGE = 2;
    public static final int MIME_TYPE_AUDIO = 3;
    public static final int MIME_TYPE_VIDEO = 4;
    public static final int MIME_TYPE_CONTACT = 5;
    public static final int MIME_TYPE_NOTE = 6;
    public static final int MIME_TYPE_DELETE = 7;
    public static final int MIME_TYPE_MISSED = 8;
    //vault items type
    public static final int ITEM_TYPE_NOTES = 1;
    public static final int ITEM_TYPE_PICTURE = 2;
    public static final int ITEM_TYPE_CHATS = 3;
    //fontsize
    public static final int smallFont = 0;
    public static final int mediumFont = 1;
    public static final int largeFont = 2;
    //Block Status
    public static final int KEY_UNBLOCKED = 0;
    public static final int KEY_BLOCKED = 1;
    //Chat Type
    public static final int SINGLE_CHAT_TYPE = 0;
    public static final int GROUP_CHAT_TYPE = 1;
    //Message Type
    public static final int MESSAGE_TYPE_FROM = 1;
    public static final int MESSAGE_TYPE_TO = 2;
    //PersonalNotes Viw Type
    public static final int Personal_edit = 1;
    public static final int Personal_save = 2;
    public static final int Personal_view = 3;
    //Group Member Type
    public static final String Personal_note = "personalnote";
    public static final int GROUP_ADMIN = 1;
    public static final int GROUP_MEMBER = 0;
    //Message Status
    public static final int MESSAGE_UNREAD_STATUS = 0;
    public static final int MESSAGE_READ_STATUS = 1;
    public static final int MESSAGE_ENCRYPTED_STATUS = 2;
    public static final int MESSAGE_LOCK_STATUS = 3;
    public static final int MESSAGE_IN_PROGRESS_STATUS = 4;
    public static final int MESSAGE_NOT_SENT_STATUS = 5;
    public static final int MESSAGE_SENT_STATUS = 6;
    public static final int MESSAGE_SENT_IN_PROGRESS_STATUS = 7;
    public static final int MESSAGE_STATUS_DELIVERED = 2;
    public static boolean MESSAGE_IMG_FLAG = false;
    public static final int MESSAGE_STATUS_READ_BUT_UN_ACK = 8;
    //User Notification Snooze Status
    public static final int NOTIFICATION_SNOOZE_YES = 1;
    public static final int NOTIFICATION_SNOOZE_NO = 0;
    public static final int NOTIFICATION_SOUND_NO = 1;
    public static final int NOTIFICATION_SOUND_YES = 0;
    //Push Notification Constant
    public static final int NOTIFICATION_ID = 1;
    public static final int REQUEST_CODE_PERSONAL_NOTE = 14263;
    public static final int REQUEST_CODE_POWER_OPTIMIZATION = 34251;
    public static final int REQUEST_CODE_RINGTONE = 5;
    public static final int REQUEST_CODE_NOTIFY_SOUND = 50;
    //Time Text Constant
    public static final String SECONDS = " seconds";
    public static final String MINUTES = " minutes";
    public static final String HOURS = " hours";
    public static final String DAYS = " days";
    public static final String SECONDS_SMALL = " secs";
    public static final String MINUTES_SMALL = " mins";
    public static final String HOURS_SMALL = " hrs";
    public static final String DAYS_SMALL = " days";
    public static final int TIME_TEXT_TYPE_SMALL = 0;
    public static final int TIME_TEXT_TYPE_NORMAL = 1;
    //EXTRA AppConstant.
    public static final String EXTRA_IMAGE_PATH = "imagePath";
    //Temporary File Name.
    public static final String TEMP_FILE_NAME = "tempFile.";
    public static final String tempPassword = "sschat";
    public static final String EXTRA_FROM_VAULT = "from_vault";
    public static final int YES = 1;
    public static final int NO = 0;
    public static final java.lang.String EXTRA_FILE_PATH = "file_path";
    public static final java.lang.String EXTRA_MESSAGE_LIST = "extra_message_list";
    public static final java.lang.String IS_ENCRYPTED = "is_encrypted";
    public static final java.lang.String EXTRA_MIEM_TYPE = "mime_type";
    public static WebSocketClient mWebSocketClient;
    public static int LENGTH_6 = 6;
    public static int LENGTH_15 = 15;
    public static String keyId = "keyId";

    public static String pubECCKeyName = "pubECCKey51.asc";
    public static String privECCKeyName = "privECCKey51.asc";
    public static String KeyPairECCName = "keypairECC51.asc";
    public static String ECCDomain = " @realapps.ro";// changed fro @secret.com
    public static String KeyStoreECC = "myecc.keystore";
    public static String asciiHeader = "Shadow Secure Chat App";
    public static int ECCKeySize = 448;
    public static String typeEcc = "0";
    public static int GroupMember = 0;
    public static int GroupAdmin = 1;
    //settings
    public static int MAX_PWD_ATTEMPT = 5;
    public static String block = "block";
    public static String friend = "friend";
    public static String pending = "pending";
    public static int LENGTH_3 = 3;
    public static int LENGTH_5 = 5;
    public static int LENGTH_11 = 11;
    public static int LENGTH_16 = 16;
    public static int LENGTH_25 = 25;
    public static int LENGTH_32 = 32;
    public static int LENGTH_50 = 50;
    public static int LENGTH_10 = 10;
    public static int LENGTH_100 = 100;
    public static int LENGTH_500 = 500;
    public static boolean lockscreen = false;
    public static boolean onpermission = false;
    public static boolean isringtoneDialog = false;
    public static MessageClass messageClass;
    public static boolean isAppActive = false;
    static boolean isThreadRunning = false;
    public static String Format_yyyyMMdd = "yyyy-MM-dd";
    public static int openedChatID = -1;
    public static int revised = 1;
    public static int nonRevised = 0;
    public static String messId = "154986708845412";
    public static final String EXTRA_HIND = "!@#$";
    public static ArrayList<ChatListEntity> listChatIdAndType;
    public static int chatId;
    public static int chatType;

    public static final String Password_MSG = "Password field is required.";
    public static final String Repeat_Password_MSG = "Repeat password field is required.";
    public static final String CurrentPasswordNotMatch_MSG = "Password and repeat password does not match.";

    private AppConstants() {
        // This utility class is not publicly instantiable
    }
}
