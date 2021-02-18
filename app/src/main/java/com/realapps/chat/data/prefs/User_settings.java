package com.realapps.chat.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.realapps.chat.utils.FileLog;

public class User_settings {

    private static String PREF_LOGIN_PREFERENCES = "login_status_preference";

    //USER PROFILE DATA
    private static String PREF_SCREEN_NAME = "screenName";
    private static String PREF_ECC_ID = "eccID";
    private static String PREF_REMOTE_STATUS = "PREF_REMOTE_STATUS";
    private static String PREF_APP_STATUS = "PREF_APP_STATUS";
    private static String PREF_ENCRYPT_MSG = "PREF_ENCRYPT_MSG";
    private static String PREF_ENCRYPT_GROUP_MSG = "PREF_ENCRYPT_GROUP_MSG";
    private static String PREF_DEFAULT_MSG_TIME = "12";
    private static String PREF_USER_ID = "userId";
    private static String PREF_LICENCE_KEY_ID = "licenceKeyID";
    private static String PREF_APP_PASSWORD = "appPassword";
    private static String PREF_DURESS_PASSWORD = "duressPassword";
    private static String PREF_SINGLE_BURN_TIME = "single_burn_time";
    private static String PREF_SUBSCRIPTION_DATE = "subscription_date";
    //USER Login DATA
    private static String PREF_USER_EMAIL = "user_email";
    private static String PREF_USER_PASSWORD = "user_passwod";
    private static String PREF_LOGIN_STATUS = "login_status";
    private static String PREF_ATTACH_DIR = "attachdir";
    private static String PREF_DECODE_DIR = "decodedir";
    public static String PREF_HOST_POP = "popHost";
    public static String PREF_HOST_SMTP = "hostSMTP";
    public static String PREF_SHOW_DIALOG = "showDialog";
    //Services
    public static String PREF_SER_UPDATE_EMAIL = "serUpdateEmail";
    private static String PREF_USER_PGP_MODE = "user_pgp_mode";
    private static String PREF_USER_PGP_Password = "pgp_password";
    private static String PREF_USER_PGP_EMAIL = "user_pgp_mail";
    private static String PREF_MAIL_STATUS = "mail_status";
    private static String PREF_SUPPORT_ECC_ID = "support_ecc_id";
    private static String PREF_FIREBASE_TOKEN = "firebaseToken";
    private static String PREF_END_DATE_license_KEY = "endDateLicenseKey";
    private static String PREF_SUPPORT_DB_ID = "supportDbId";
    private static String PREF_INTERNET_CONNECTION_DATE = "InternetConDate";
    private static String PREF_LAST_LOGIN_TIME = "lastLoginTime";
    private static String PREF_KEYS_PATH = "keysPath";
    private static String PREF_USER_AT_CHAT_WINDOW = "userAtChatWindow";
    private static String PREF_BLOCK_MEMBER = "block_member";
    private static String MAX_PASSWORD_ATTEMPT = "max_pwd_attempts";
    private static String ENTER_KEY_SEND = "enter_key_send";
    private static String TEMP_ATTEMPT = "temp_attempt";
    private static String RINGTONE_SELECTOR = "rington";
    private static String NOTIFY_SOUND_SELECTOR = "notify_sound";
    private static String FONT_SIZE = "font";
    private static String LOCK_TIME = "lock_time";
    private static String PREF_LANGUAGE = "language";
    private static String PREF_USER_STATUS = "user_status";
    private static String PREF_SUBSCRIBED = "subscribed";
    private static String PREF_NORMAL_USER = "normal_user";
    private static String PREF_ISbACKGROUND = "background";
    private static String VERSIONCODE = "verCode";
    private static String PREF_LAST_ACTIVITY = "last_activity";
    private static String LAST_CLEAR_CACHE_DATE = "last_cache_date";

    // // Added by Marius 09 - 03 - 2020 from call

    public static String Licence_activate = "Licence_activate";

    private static String TOKEN = "Token";
    //getVersionCode
    public static int getVERSIONCODE(Context mContext) {
        return loadSavedPreferencesVersion(VERSIONCODE, mContext);
    }
    //setVersionCode
    public static void setVERSIONCODE(Context mContext, int versionCode) {
        savePreferencesVersion(VERSIONCODE, versionCode, mContext);
    }
    public static int loadSavedPreferencesVersion(String key, Context cntxt) {
        SharedPreferences sp = cntxt.getSharedPreferences(PREF_LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        int cbValue = sp.getInt(key, 0);
        return cbValue;
    }

    public static void savePreferencesVersion(String key, int value, Context cntxt) {
        SharedPreferences sp = cntxt.getSharedPreferences(PREF_LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        Editor edit = sp.edit();
        edit.putInt(key, value);
        edit.commit();
    }
    public static String getLastClearDate(Context mContext) {
        return loadSavedPreferences_string(LAST_CLEAR_CACHE_DATE, mContext);
    }

    public static void setLastClearDate(Context mContext, String last_cache_date) {
        Log.e("setLastActivity: ", last_cache_date);
        savePreferences(LAST_CLEAR_CACHE_DATE, last_cache_date, mContext);
    }

    public static String getLastActivity(Context mContext) {
        return loadSavedPreferences_string(PREF_LAST_ACTIVITY, mContext);
    }

    public static void setLastActivity(Context mContext, String pass) {
        Log.e("setLastActivity: ", pass);
        savePreferences(PREF_LAST_ACTIVITY, pass, mContext);
    }

    public static boolean getInventryStatus(Context mContext) {
        return loadSavedPreferences(PREF_NORMAL_USER, mContext);
    }

    public static void setInventrystatus(Context mContext, boolean isFirstTime) {
        savePreferences(PREF_NORMAL_USER, isFirstTime, mContext);
    }

    public static boolean isBackground(Context mContext) {
        return loadSavedPreferences(PREF_ISbACKGROUND, mContext);
    }

    public static void setBackgroundApp(Context mContext, boolean isFirstTime) {
        savePreferences(PREF_ISbACKGROUND, isFirstTime, mContext);
    }

    public static boolean getSubscriptionStatus(Context mContext) {
        return loadSavedPreferences(PREF_SUBSCRIBED, mContext);
    }

    public static void setSubscriptionStatus(Context mContext, boolean isFirstTime) {
        savePreferences(PREF_SUBSCRIBED, isFirstTime, mContext);
    }

    public static boolean getUserActiveStatus(Context mContext) {
        return loadSavedPreferences(PREF_USER_STATUS, mContext);
    }

    public static void setUserActiveStatus(Context mContext, boolean isFirstTime) {
        savePreferences(PREF_USER_STATUS, isFirstTime, mContext);
    }


    //CHECK APP PASSWORD STATUS
    public static boolean isAppPassword(Context mContext) {
        boolean check = false;

        try {
            FileLog.e("TAG", "isUserLogedIn: " + (getAppPassword(mContext).length() > 0));
            check = (getAppPassword(mContext).length() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return check;
    }

    //CHECK DURESS PASSWORD STATUS
    public static boolean isDuressPassword(Context mContext) {
        boolean check = false;

        try {
            FileLog.e("TAG", "isUserLogedIn: " + (getDuressPassword(mContext).length() > 0));
            check = (getDuressPassword(mContext).length() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return check;
    }

    //CHECK USER LOGIN STATUS
    public static boolean isUserLogin(Context mContext) {
        boolean check = false;

        try {
//            FileLog.e("TAG-1", "isUserLogedIn: " + getLoginStatus(mContext));
            check = getLoginStatus(mContext); //(getUserEmail(mContext).length() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return check;
    }


    //USER PROFILE setter AND getter AFTER LOGIN OR OTP VERIFIED
    public static int getEnterKeySend(Context mContext) {
        return loadSavedPreferences_int(ENTER_KEY_SEND, mContext);
    }

    public static void setEnterKeySend(Context mContext, int pass) {
        savePreferences(ENTER_KEY_SEND, pass, mContext);
    }

    public static int getLockTime(Context mContext) {
        return loadSavedPreferences_int(LOCK_TIME, mContext);
    }

    public static void setLockTime(Context mContext, int pass) {
        savePreferences(LOCK_TIME, pass, mContext);
    }

    public static String getRingtoneSelector(Context mContext) {
        return loadSavedPreferences_string(RINGTONE_SELECTOR, mContext);
    }

    public static void setRingtoneSelector(Context mContext, String pass) {
        savePreferences(RINGTONE_SELECTOR, pass, mContext);
    }

    public static String getNotifySoundSelector(Context mContext) {
        return loadSavedPreferences_string(NOTIFY_SOUND_SELECTOR, mContext);
    }

    public static void setNotifySoundSelector(Context mContext, String pass) {
        savePreferences(NOTIFY_SOUND_SELECTOR, pass, mContext);
    }

    public static int getfont(Context mContext) {
        return loadSavedPreferences_int(FONT_SIZE, mContext);
    }

    public static void setFontSize(Context mContext, int pass) {
        savePreferences(FONT_SIZE, pass, mContext);
    }


    public static String getBlockMemberListStatus(Context mContext) {
        return loadSavedPreferences_string(PREF_BLOCK_MEMBER, mContext);
    }

    public static void setBlockMemberListStatus(Context mContext, String pass) {
        savePreferences(PREF_BLOCK_MEMBER, pass, mContext);
    }

    public static int getTempAttempt(Context mContext) {
        return loadSavedPreferences_int(TEMP_ATTEMPT, mContext);
    }

    public static void setTempAttempt(Context mContext, int temp_attempt) {
        savePreferences(TEMP_ATTEMPT, temp_attempt, mContext);
    }

    //USER PROFILE setter AND getter AFTER LOGIN OR OTP VERIFIED
    public static String getAppPassword(Context mContext) {
        return loadSavedPreferences_string(PREF_APP_PASSWORD, mContext);
    }

    public static void setAppPassword(Context mContext, String pass) {
        savePreferences(PREF_APP_PASSWORD, pass, mContext);
    }

    public static String getUserStatus(Context mContext) {
        return loadSavedPreferences_string(PREF_USER_AT_CHAT_WINDOW, mContext);
    }

    public static void setUserStatus(Context mContext, String pass) {
        savePreferences(PREF_USER_AT_CHAT_WINDOW, pass, mContext);
    }

    public static String getLastLoginTime(Context mContext) {
        return loadSavedPreferences_string(PREF_LAST_LOGIN_TIME, mContext);
    }

    public static void setLastLoginTime(Context mContext, String pass) {
        savePreferences(PREF_LAST_LOGIN_TIME, pass, mContext);
    }

    public static int getMaxPasswordAttempt(Context mContext) {
        return loadSavedPreferences_int(MAX_PASSWORD_ATTEMPT, mContext);
    }

    public static void setMaxPasswordAttempt(Context mContext, int attempts) {
        savePreferences(MAX_PASSWORD_ATTEMPT, attempts, mContext);
    }

    public static String getTOKEN(Context context) {
        return loadSavedPreferences_string(TOKEN, context);
    }

    public static void setTOKEN(Context context, String balance) {
        savePreferences(TOKEN, balance, context);
    }

    public static String getSupportEccId(Context mContext) {
        return loadSavedPreferences_string(PREF_SUPPORT_ECC_ID, mContext);
    }

    public static void setSupportEccId(Context mContext, String pass) {
        savePreferences(PREF_SUPPORT_ECC_ID, pass, mContext);
    }

    public static String getSubscriptionDate(Context mContext) {
        return loadSavedPreferences_string(PREF_SUBSCRIPTION_DATE, mContext);
    }


    public static void setSubscriptionDate(Context mContext, String pass) {
        savePreferences(PREF_SUBSCRIPTION_DATE, pass, mContext);
    }

    public static String getKeysPath(Context mContext) {
        return loadSavedPreferences_string(PREF_KEYS_PATH, mContext);
    }


    public static void setKeysPath(Context mContext, String path) {
        savePreferences(PREF_KEYS_PATH, path, mContext);
    }

    public static String getSupportDbId(Context mContext) {
        return loadSavedPreferences_string(PREF_SUPPORT_DB_ID, mContext);
    }


    public static void setSupportDbId(Context mContext, String pass) {
        savePreferences(PREF_SUPPORT_DB_ID, pass, mContext);
    }

    public static String getInternetConDate(Context mContext) {
        return loadSavedPreferences_string(PREF_INTERNET_CONNECTION_DATE, mContext);
    }

    public static void setInternetConDate(Context mContext, String pass) {
        savePreferences(PREF_INTERNET_CONNECTION_DATE, pass, mContext);
    }

    public static String getUserPgpMail(Context mContext) {
        return loadSavedPreferences_string(PREF_USER_PGP_EMAIL, mContext);
    }


    public static void setUserPgpMail(Context mContext, String email) {
        savePreferences(PREF_USER_PGP_EMAIL, email, mContext);
    }

    public static String getDuressPassword(Context mContext) {
        return loadSavedPreferences_string(PREF_DURESS_PASSWORD, mContext);
    }

    public static void setDuressPassword(Context mContext, String pass) {
        savePreferences(PREF_DURESS_PASSWORD, pass, mContext);
    }

    public static String getScreenName(Context mContext) {
        return loadSavedPreferences_string(PREF_SCREEN_NAME, mContext);
    }

    public static void setScreenName(Context mContext, String screenName) {
        savePreferences(PREF_SCREEN_NAME, screenName, mContext);
    }

    public static String getECCID(Context mContext) {
        return loadSavedPreferences_string(PREF_ECC_ID, mContext);
    }

    public static void setECCID(Context mContext, String ECCID) {
        savePreferences(PREF_ECC_ID, ECCID, mContext);
    }

    public static String getDefault_msg_tym(Context mContext) {
        return loadSavedPreferences_string(PREF_DEFAULT_MSG_TIME, mContext);
    }

    public static void setDefault_msg_tym(Context mContext, String default_msg_tym) {
        savePreferences(PREF_DEFAULT_MSG_TIME, default_msg_tym, mContext);
    }

    public static String getUserId(Context mContext) {
        return loadSavedPreferences_string(PREF_USER_ID, mContext);
    }

    public static void setUserId(Context mContext, String UserId) {
        savePreferences(PREF_USER_ID, UserId, mContext);
    }

    // Added by Marius 09 - 03 - 2020 from call

    public static boolean isLicenseActivated(Context mContext) {
        return loadSavedPreferences(Licence_activate, mContext);
    }

    public static void setLicenseActivate(Context mContext, boolean isFirstTime) {
        savePreferences(Licence_activate, isFirstTime, mContext);
    }
    //////////

    public static String getLicenceKeyID(Context mContext) {
        return loadSavedPreferences_string(PREF_LICENCE_KEY_ID, mContext);
    }

    public static void setLicenceKeyID(Context mContext, String licenceKeyID) {
        savePreferences(PREF_LICENCE_KEY_ID, licenceKeyID, mContext);
    }

    public static boolean getLoginStatus(Context mContext) {
        return loadSavedPreferences(PREF_LOGIN_STATUS, mContext);
    }

    public static boolean getMailStatus(Context mContext) {
        return loadSavedPreferences(PREF_MAIL_STATUS, mContext);
    }

    public static void setMailStatus(Context mContext, boolean mailStatus) {
        savePreferences(PREF_MAIL_STATUS, mailStatus, mContext);
    }

    public static boolean getRemoteStatus(Context mContext) {
        return loadSavedPreferences(PREF_REMOTE_STATUS, mContext);
    }

    public static void setRemoteStatus(Context mContext, boolean remoteStatus) {
        savePreferences(PREF_REMOTE_STATUS, remoteStatus, mContext);
    }

    public static boolean getAppStatus(Context mContext) {
        return loadSavedPreferences(PREF_APP_STATUS, mContext);
    }

    public static void setAppStatus(Context mContext, boolean appStatus) {
        savePreferences(PREF_APP_STATUS, appStatus, mContext);
    }

    public static boolean getEncryptMsg(Context mContext) {
        return loadSavedPreferences(PREF_ENCRYPT_MSG, mContext);
    }

    public static void setEncryptMsg(Context mContext, boolean encryptMsg) {
        savePreferences(PREF_ENCRYPT_MSG, encryptMsg, mContext);
    }

    public static boolean getEncryptGroupMsg(Context mContext) {
        return loadSavedPreferences(PREF_ENCRYPT_GROUP_MSG, mContext);
    }

    public static void setEncryptGroupMsg(Context mContext, boolean encryptGroupMsg) {
        savePreferences(PREF_ENCRYPT_GROUP_MSG, encryptGroupMsg, mContext);
    }

    public static void setLoginStatus(Context mContext, boolean loginStatus) {
        savePreferences(PREF_LOGIN_STATUS, loginStatus, mContext);
    }

    public static String getAttachDir(Context mContext) {
        return loadSavedPreferences_string(PREF_ATTACH_DIR, mContext);
    }

    public static void setAttachDir(Context mContext, String attachDir) {
        savePreferences(PREF_ATTACH_DIR, attachDir, mContext);
    }

    public static String getDecodeDir(Context mContext) {
        return loadSavedPreferences_string(PREF_DECODE_DIR, mContext);
    }

    public static void setDecodeDir(Context mContext, String decodeDir) {
        savePreferences(PREF_DECODE_DIR, decodeDir, mContext);
    }

    public static void savePreferences(String key, boolean value, Context cntxt) {
        SharedPreferences sp = cntxt.getSharedPreferences(PREF_LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public static boolean loadSavedPreferences(String key, Context cntxt) {
        SharedPreferences sp = cntxt.getSharedPreferences(PREF_LOGIN_PREFERENCES, Context.MODE_PRIVATE);

//        FileLog.d("value", "" + sp.getBoolean(key, false));
        boolean cbValue = sp.getBoolean(key, false);

        return cbValue;
    }

    public static void removePreferences(String key, Context cntxt) {
        SharedPreferences sp = cntxt.getSharedPreferences(PREF_LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        sp.edit().remove(key).apply();
    }

    public static void savePreferences(String key, String value, Context cntxt) {

        SharedPreferences sp = cntxt.getSharedPreferences(PREF_LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public static int loadSavedPreferences_int(String key, Context cntxt) {
        SharedPreferences sp = cntxt.getSharedPreferences(PREF_LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        int cbValue = sp.getInt(key, 0);
        return cbValue;
    }

    public static void savePreferences(String key, int value, Context cntxt) {
        SharedPreferences sp = cntxt.getSharedPreferences(PREF_LOGIN_PREFERENCES, Context.MODE_PRIVATE);
        Editor edit = sp.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    public static String loadSavedPreferences_string(String key, Context cntxt) {
        String cbValue = null;
        try {
            SharedPreferences sp = cntxt.getSharedPreferences(PREF_LOGIN_PREFERENCES, Context.MODE_PRIVATE);
            cbValue = sp.getString(key, "");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cbValue;
    }


    public static int getUserPgpMode(Context mContext) {
        return loadSavedPreferences_int(PREF_USER_PGP_MODE, mContext);
    }

    public static void setUserPgpMode(Context mContext, int USER_PGP_MODE) {
        savePreferences(PREF_USER_PGP_MODE, USER_PGP_MODE, mContext);
    }

    public static String getUserPgpPassword(Context mContext) {
        return loadSavedPreferences_string(PREF_USER_PGP_Password, mContext);
    }

    public static void setUserPgpPassword(Context mContext, String USER_PGP_Password) {
        savePreferences(PREF_USER_PGP_Password, USER_PGP_Password, mContext);
    }


    public static void logout(Context cntxt) {
        removePreferences(PREF_APP_PASSWORD, cntxt);
        removePreferences(PREF_DURESS_PASSWORD, cntxt);
        removePreferences(PREF_USER_PASSWORD, cntxt);
        removePreferences(PREF_LOGIN_STATUS, cntxt);
        removePreferences(PREF_USER_ID, cntxt);
        removePreferences(PREF_SCREEN_NAME, cntxt);
        removePreferences(PREF_ATTACH_DIR, cntxt);
        removePreferences(PREF_DECODE_DIR, cntxt);
        removePreferences(PREF_HOST_POP, cntxt);
        removePreferences(PREF_HOST_SMTP, cntxt);
    }


    public static int getSingleBurnTime(Context mContext) {
        return loadSavedPreferences_int(PREF_SINGLE_BURN_TIME, mContext);
    }

    public static void setSingleBurnTime(Context mContext, int SINGLE_BURN_TIME) {
        savePreferences(PREF_SINGLE_BURN_TIME, SINGLE_BURN_TIME, mContext);
    }

    public static String getUserPassword(Context mContext) {
        return loadSavedPreferences_string(PREF_USER_PASSWORD, mContext);
    }

    public static void setUserPassword(Context mContext, String userPassword) {
        savePreferences(PREF_USER_PASSWORD, userPassword, mContext);
    }

    public static String getUserEmail(Context mContext) {
        return loadSavedPreferences_string(PREF_USER_EMAIL, mContext);
    }

    public static void setUserEmail(Context mContext, String userEmail) {
        savePreferences(PREF_USER_EMAIL, userEmail, mContext);
    }

    public static String getFirebaseToken(Context mContext) {
        return loadSavedPreferences_string(PREF_FIREBASE_TOKEN, mContext);
    }

    public static void setFirebaseToken(Context mContext, String firebaseToken) {
        savePreferences(PREF_FIREBASE_TOKEN, firebaseToken, mContext);
    }

    public static String getLanguage(Context context) {
        return loadSavedPreferences_string(PREF_LANGUAGE, context);
    }

    public static void setlanguage(Context context, String cusId) {
        savePreferences(PREF_LANGUAGE, cusId, context);
    }

    public static void setEndDateLicense(Context context, String subValid) {
        savePreferences(PREF_END_DATE_license_KEY, subValid, context);
    }

    public static String getEndDateLicense(Context context) {
        return loadSavedPreferences_string(PREF_END_DATE_license_KEY, context);
    }
}
