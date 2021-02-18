package com.realapps.chat.ui.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.realapps.chat.ui.ui.view.kotlin.model.URLCollection;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Ravi on 08/07/15.
 */
public class PrefManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = URLCollection.obj.PACKAGE_NAME + "_Preferences";

    // All Shared Preferences Keys
    private static final String KEY_IS_WAITING_FOR_SMS = "IsWaitingForSms";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_FrstNAME = "first_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_CONFERENCE_NO = "conference_no";
    private static final String KEY_DISPLAYBalance = "display_balance";
    private static final String KEY_LastNAME = "last_name";
    private static final String KEY_Number = "number";
    private static final String KEY_Currencycode = "currency_code";
    private static final String KEY_DeviceId = "device_id";
    private static final String KEY_ID = "id";
    private static final String KEY_COMPANYNAME = "company_name";
    public static final String KEY_PAYPAL_TAX = "paypal_tax";
    public static final String KEY_PAYPAL_STATUS = "paypal_status";
    public static final String KEY_PAYPAL_CLIENTID = "paypal_clientid";
    public static final String Key_ContactUs = "contact_us";

    public static final String KEY_REMEMBER_ME = "Remember_me";
    public static final String KEY_REMEMBER_UN = "username";
    public static final String KEY_REMEMBER_PASS = "pass";

    public static final String KEY_CONTACT_SYNC = "contact_sync";
    public static final String Key_OTPNUMBER = "otp_number";
    public static final String Key_sIGNUPNUMBER = "number";
    //public static final String Key_sIGNUPNUMBER = "number";
    SharedPreferences preferenceManager;

    public static final String CONFERENCE_CONTACT_NUMBER = "conference_contact_number";
    public static final String KEY_CALLING_NUM = "calling_num";
    public static final String KEY_ECC_ID = "ecc_id";
    public static final String KEY_SCREEN_NAME = "screen_name";
    public static final String KEY_IS_ONGOING_CALL = "ongoing_call";
    public static final String KEY_ONGOING_CALL_HOME = "ongoing_call_home2";

    public static final String KEY_CALL_FROM_CHAT = "call_from_chat";
    public static final String KEY_ECC_ID_TO_BE_CALLED = "ecc_id_to_be_called";

    public static final String KEY_SPEAKER = "speaker";
    public static final String KEY_MUTE = "mute";

    public static final String KEY_LOADING_CALL_LOG = "call_log_loader";
    public static final String KEY_HISTORY_FETCHER = "call_history_fetcher";

    public static final String KEY_NUMBER_ADD_CALL = "add_call_number";

    public static final String KEY_TYPE_FRAG = "frag_type";
    public static final String KEY_CHAT_ENTRY = "chat_entry";

    List<String> records;

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public PrefManager(Context context, String pref) {
        this._context = context;
        preferenceManager = (SharedPreferences) PreferenceManager.getDefaultSharedPreferences(_context);

        //editor = pref.edit();
    }


    public void setToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.commit();
    }

    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }


    public void setKey_ContactUs(String contactus) {
        editor.putString(Key_ContactUs, contactus);
        editor.commit();
    }

    public String getKey_ContactUs() {
        return pref.getString(Key_ContactUs, null);
    }


    public String getKeyPaypalTax() {
        return pref.getString(KEY_PAYPAL_TAX, null);
    }

    public void setKeyPaypalTax(String paypaltax) {
        editor.putString(KEY_PAYPAL_TAX, paypaltax);
        editor.commit();
    }

    public String getKeyPaypalClientid() {
        return pref.getString(KEY_PAYPAL_CLIENTID, null);
    }

    public void setKeyPaypalClientid(String paypalclientid) {
        editor.putString(KEY_PAYPAL_CLIENTID, paypalclientid);
        editor.commit();
    }

    public String getKeyPaypalStatus() {
        return pref.getString(KEY_PAYPAL_STATUS, null);
    }

    public void setKeyPaypalStatus(String paypalstatus) {
        editor.putString(KEY_PAYPAL_STATUS, paypalstatus);
        editor.commit();
    }


    public void setKEY_FrstNAME(String Firstname) {
        editor.putString(KEY_FrstNAME, Firstname);
        editor.commit();
    }

    public String getKEY_FrstNAME() {
        return pref.getString(KEY_FrstNAME, null);
    }


    public void setKEY_LastNAME(String Lastname) {
        editor.putString(KEY_LastNAME, Lastname);
        editor.commit();
    }

    public String getKEY_LastNAME() {
        return pref.getString(KEY_LastNAME, null);
    }

    public void setKEY_Number(String Number) {
        editor.putString(KEY_Number, Number);
        editor.commit();
    }

    public String getKEY_Number() {
        return pref.getString(KEY_Number, null);
    }


    public void setKeyPassword(String password) {
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }

    public String getKeyPassword() {
        return pref.getString(KEY_PASSWORD, null);
    }


    public void setLoggedIn(boolean setLogin) {
        editor.putBoolean(KEY_IS_LOGGED_IN, setLogin);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }


    public void clearSession() {
        editor.clear();
        editor.commit();
    }

    public void setSignUpCredential(String number, String firstname, String lastname, String password, String token, String email, String displaybalance, String id, String deviceid, String companyname) {
        editor.putString(KEY_Number, number);
        editor.putString(KEY_FrstNAME, firstname);
        editor.putString(KEY_LastNAME, lastname);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_DISPLAYBalance, displaybalance);
        editor.putString(KEY_ID, id);
        editor.putString(KEY_DeviceId, deviceid);
        editor.putString(KEY_COMPANYNAME, companyname);
        editor.commit();

    }

    public HashMap<String, String> getSignUpCredential() {
        HashMap<String, String> credential = new HashMap<>();
        credential.put("number", pref.getString(KEY_Number, null));
        credential.put("first_name", pref.getString(KEY_FrstNAME, null));
        credential.put("last_name", pref.getString(KEY_LastNAME, null));
        credential.put("password", pref.getString(KEY_PASSWORD, null));
        credential.put("token", pref.getString(KEY_TOKEN, null));
        credential.put("email", pref.getString(KEY_EMAIL, null));
        credential.put("display_balance", pref.getString(KEY_DISPLAYBalance, null));
        credential.put("id", pref.getString(KEY_ID, null));
        credential.put("device_id", pref.getString(KEY_DeviceId, null));
        credential.put("company_name", pref.getString(KEY_COMPANYNAME, null));
        return credential;
    }

    public void setUpdatedDate(String f_name, String l_name, String email, String number) {
        editor.putString(KEY_FrstNAME, f_name);
        editor.putString(KEY_LastNAME, l_name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_Number, number);
        editor.commit();
    }

    public void setRememberme(boolean rememberme, String username, String password) {
        editor.putBoolean(KEY_REMEMBER_ME, rememberme);
        editor.putString(KEY_REMEMBER_UN, username);
        editor.putString(KEY_REMEMBER_PASS, password);
        editor.commit();
    }

    public boolean getRememberme() {

        return pref.getBoolean(KEY_REMEMBER_ME, false);
    }

    public HashMap<String, String> getRememberData() {
        HashMap<String, String> credential = new HashMap<>();

        credential.put("username", pref.getString(KEY_REMEMBER_UN, null));
        credential.put("pass", pref.getString(KEY_REMEMBER_PASS, null));
        return credential;
    }


    public void set_contact_syc(boolean contact) {
        editor.putBoolean(KEY_CONTACT_SYNC, contact);
        editor.commit();

    }

    public boolean get_contact_sync() {
        return pref.getBoolean(KEY_CONTACT_SYNC, false);
    }


    public void setKEY_DeviceId(String deviceid) {
        editor.putString(KEY_DeviceId, deviceid);
        editor.commit();
    }

    public String getKEY_DeviceId() {
        return pref.getString(KEY_DeviceId, null);
    }


    public void setKey_sIGNUPNUMBER(String signupnumber) {
        editor.putString(Key_sIGNUPNUMBER, signupnumber);
        editor.commit();
    }

    public String getKey_sIGNUPNUMBER() {
        return pref.getString(Key_sIGNUPNUMBER, null);
    }


    public void setKey_OTPNUMBER(String otpnumber) {
        editor.putString(Key_OTPNUMBER, otpnumber);
        editor.commit();
    }

    public String getKey_OTPNUMBER() {
        return pref.getString(Key_OTPNUMBER, null);
    }


    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }


    public void setCallingNum(String number) {
        editor.putString(KEY_CALLING_NUM, number);
        editor.commit();
    }

    public String getCallingNum() {
        return pref.getString(KEY_CALLING_NUM, null);
    }


    public void setEccId(String number) {
        editor.putString(KEY_ECC_ID, number);
        editor.commit();
    }

    public String getEccId() {
        return pref.getString(KEY_ECC_ID, null);
    }


    public void setScreenName(String number) {
        editor.putString(KEY_SCREEN_NAME, number);
        editor.commit();
    }

    public String getScreenName() {
        return pref.getString(KEY_SCREEN_NAME, null);
    }


    public void setMultipleConference(ArrayList<String> number) {
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(number);
        editor.putString(KEY_NUMBER_ADD_CALL, jsonFavorites);
        editor.commit();
    }


    public ArrayList<String> getMultipleConfere() {
        try {
            String json = pref.getString(KEY_NUMBER_ADD_CALL, null);
            Gson gson = new Gson();
            records = new ArrayList<String>();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();

            String[] data = gson.fromJson(json, type);

            records = Arrays.asList(data);
            records = new ArrayList<String>(records);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (ArrayList<String>) records;
    }

    public void setConferenceContact(Context ctx, ArrayList<String> number) {

        //preferenceManager = PreferenceManager.getDefaultSharedPreferences(ctx);
        System.out.println("Onclick Hangup :=> " + number);
        if (number == null) {
            editor.putString(CONFERENCE_CONTACT_NUMBER, null);
        } else {
            Gson gson = new Gson();
            List<String> textList = new ArrayList<String>();
            textList.addAll(number);
            String jsonText = gson.toJson(textList);
            // SharedPreferences.Editor editor = preferenceManager.edit();

            editor.putString(CONFERENCE_CONTACT_NUMBER, jsonText);
        }
        editor.commit();
        editor.apply();
    }

    public String getConferenceContact(Context ctx) {
        //preferenceManager = PreferenceManager.getDefaultSharedPreferences(ctx);
        return pref.getString(CONFERENCE_CONTACT_NUMBER, "");

    }

    public void addContactAtHangup(Context ctx, ArrayList<String> number) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Editor editor = prefs.edit();

        Gson gson = new Gson();
        List<String> textList = new ArrayList<String>();
        if (number != null) {
            textList.addAll(number);
            String jsonText = gson.toJson(textList);
            editor.putString("add_conference_contact_at_hangup", jsonText);
        } else {
            editor.putString("add_conference_contact_at_hangup", null);
        }
        editor.commit();
        editor.apply();
    }

    public String getAddedContactsAtHangup(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("add_conference_contact_at_hangup", null);
    }

    public void addContact(Context ctx, ArrayList<String> number) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Editor editor = prefs.edit();

        Gson gson = new Gson();
        List<String> textList = new ArrayList<String>();
        if (number != null) {
            textList.addAll(number);
            String jsonText = gson.toJson(textList);
            editor.putString("add_conference_contact", jsonText);
        } else {
            editor.putString("add_conference_contact", null);
        }
        editor.commit();
        editor.apply();
    }

    public String getAddedContacts(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("add_conference_contact", null);
    }

    public void addMakeCallContact(Context ctx, ArrayList<String> number) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Editor editor = prefs.edit();

        Gson gson = new Gson();
        List<String> textList = new ArrayList<String>();
        if (number != null) {
            textList.addAll(number);
            String jsonText = gson.toJson(textList);
            editor.putString("add_conference_contact_at_make_call", jsonText);
        } else {
            editor.putString("add_conference_contact_at_make_call", null);
        }
        editor.commit();
        editor.apply();
    }

    public String getMakeCallContact(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("add_conference_contact_at_make_call", null);
    }

    public void manageConferenceContact(Context ctx, ArrayList<String> number) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Editor editor = prefs.edit();

        Gson gson = new Gson();
        List<String> textList1 = new ArrayList<String>();
        if (number != null) {
            textList1.addAll(number);
            System.out.println("Manage contact pref : " + textList1.size());
            String jsonText = gson.toJson(textList1);
            editor.putString("manage_conference_contact", jsonText);
        } else {
            editor.putString("manage_conference_contact", null);
        }
        editor.commit();
        editor.apply();
    }

    public String getManageConferenceContact(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString("manage_conference_contact", null);
    }

    /*public void setIsCallRunning(String isRunning){

        editor.putString("isCallRunning", isRunning);
        editor.commit();
        editor.apply();
    }

    public String getIsCallRunning(){
        return pref.getString("isCallRunning", null);
    }*/


    public void setCallerIdArray(ArrayList<Integer> id) {
        Gson gson = new Gson();
        List<Integer> textList = new ArrayList<Integer>();
        textList.addAll(id);
        String jsonText = gson.toJson(textList);
        editor.putString("caller_id_for_conference_call", jsonText);
        editor.commit();
        editor.apply();
    }

    public String getCallerIdArray() {
        return pref.getString("caller_id_for_conference_call", null);
    }


    public void set_is_ongoing_call_flag(Boolean flag) {
        editor.putBoolean(KEY_IS_ONGOING_CALL, flag);
        editor.commit();
    }

    public Boolean get_is_ongoing_call_flag() {
        return pref.getBoolean(KEY_IS_ONGOING_CALL, false);
    }

    public void setOngoingCallHomeButton(boolean flag) {
        editor.putBoolean(KEY_ONGOING_CALL_HOME, flag);
        editor.commit();
    }
    public Boolean getOngoingCallHomeButton() {
        return pref.getBoolean(KEY_ONGOING_CALL_HOME, false);
    }

    public void setCallFromChat(Boolean flag) {
        editor.putBoolean(KEY_CALL_FROM_CHAT, flag);
        editor.commit();
    }

    public Boolean getCallFromChat() {
        return pref.getBoolean(KEY_CALL_FROM_CHAT, false);
    }

    public void setTypeFrag(String type) {
        editor.putString(KEY_TYPE_FRAG, type);
        editor.commit();
    }
    public String getTypeFrag() {
        return pref.getString(KEY_TYPE_FRAG, "");
    }


    public void setChatEntry(String json) {
        editor.putString(KEY_CHAT_ENTRY, json);
        editor.commit();
    }
    public String getChatEntry() {
        return pref.getString(KEY_CHAT_ENTRY, "");
    }


    public void setSpeaker(String Speaker) {
        editor.putString(KEY_SPEAKER, Speaker);
        editor.commit();
    }

    public String getSpeaker() {
        return pref.getString(KEY_SPEAKER, "off");
    }


    public void setMute(String mute) {
        editor.putString(KEY_MUTE, mute);
        editor.commit();
    }

    public String getEccIdToBeCalled() {
        return pref.getString(KEY_ECC_ID_TO_BE_CALLED, "null");
    }


    public void setEccIdToBeCalled(String EccIdToBeCalled) {
        editor.putString(KEY_ECC_ID_TO_BE_CALLED, EccIdToBeCalled);
        editor.commit();
    }



    public String getMute() {
        return pref.getString(KEY_MUTE, "off");
    }

    public void setIsCallLogLoader(Boolean isCallLogLoader) {
        editor.putBoolean(KEY_LOADING_CALL_LOG, isCallLogLoader);
        editor.commit();
    }

    public Boolean IsCallLogLoader() {
        return pref.getBoolean(KEY_LOADING_CALL_LOG, false);
    }


    public void setHistoryFetcher_AfterCall(Boolean historyFetcher_afterCall) {
        editor.putBoolean(KEY_HISTORY_FETCHER, historyFetcher_afterCall);
        editor.commit();
    }

    public Boolean getHistoryFetcher_AfterCall() {

        return pref.getBoolean(KEY_HISTORY_FETCHER, false);
    }




}
