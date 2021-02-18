package com.realapps.chat.ui.api;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.realapps.chat.ui.db.BalanceProvider;
import com.realapps.chat.ui.db.ContactsProvider;
import com.realapps.chat.ui.db.HistoryStatusProvider;
import com.realapps.chat.ui.db.MissedCallCountProvider;
import com.realapps.chat.ui.ui.view.java.model.Settings;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by telafric on 20/7/17.
 */

public class GlobalClass {

    public static GlobalClass instance;
    private static final String CALL_STATE_PREF_NAME = "Call state Preference";
    public static String rechargeAmount;
    public static boolean isOnline;
    public static boolean isCallRunning;
    public static String accountStatus;
    public static int accountStatusCode;
    public static boolean isFocusable;
    public static boolean isAddContactOpen;
    public static String lastMissedCall;

    public static GlobalClass getInstance() {
        if (instance == null) {
            instance = new GlobalClass();
        }
        return instance;
    }

    public boolean isIsAddContactOpen() {
        return isAddContactOpen;
    }

    public void setIsAddContactOpen(boolean isAddContactOpen) {
        GlobalClass.isAddContactOpen = isAddContactOpen;
    }

    public boolean isFocusable() {
        return isFocusable;
    }


    // to check internet connection
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null
                && activeNetworkInfo.isConnectedOrConnecting();
    }

    // hide soft input kaypad
    public void hideKeypad(Activity act) {
        // Check if no view has focus:
        View view = act.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * to set the Formated Dialed number
     **/
    public String getFormatedDailedNumber(String dialedNumber) {
        String newDialedNumber = null;
        if(dialedNumber.contains("<")) {
            String[] dial = dialedNumber.split("<");
            String temNum = dial[1];
            if(temNum.contains(">")) {
                String[] splitedTemNum = temNum.split(">");
                newDialedNumber = splitedTemNum[0];
            }else{
                newDialedNumber = temNum;
            }
        }else if(dialedNumber.contains("#")){
            newDialedNumber =    dialedNumber.substring(dialedNumber.indexOf("#")+1);
        }

        else{
            newDialedNumber = dialedNumber;
        }
        return newDialedNumber;
    }

    public String getRechargeAmount(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        rechargeAmount = prefs.getString("current_recharge_amount", "");
        return rechargeAmount;
    }

    public void setRechargeAmount(Context ctx, String rechargeAmount) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("current_recharge_amount", rechargeAmount);
        System.out.println("current_recharge_record :: " + rechargeAmount);
        editor.commit();

        //GlobalClass.rechargeAmount = rechargeAmount;
    }

    public String split_word(String label) {
        String x = label;

        String new_letter;
        String lastletter = null;
        String firstletter = x;

        //To remove special characters
        Pattern pt_1 = Pattern.compile("[^a-zA-Z0-9]");
        Matcher match_1 = pt_1.matcher(firstletter);

        if(match_1.find()){
            while (match_1.find()) {
                String s = match_1.group();
                firstletter = firstletter.replaceAll("\\" + s, "");

            }
            //To get first character
            firstletter = firstletter.substring(0, 1);
            new_letter = firstletter;

        }else{
            //To get first character
            firstletter = x.substring(0, 1);
            new_letter = firstletter;

        }

        if (x.contains(" ")) {

            String lastword = x.substring(x.lastIndexOf(" "), x.length());

            //To remove special characters
            Pattern pt_2 = Pattern.compile("[^a-zA-Z0-9]");
            Matcher match_2 = pt_2.matcher(lastword);
            if(match_2.find()) {
                while (match_2.find()) {
                    String s = match_2.group();
                    lastword = lastword.replaceAll("\\" + s, "");
                }
                for (int i = 0; i < lastword.length(); i++) {

                    lastletter = lastword.substring(1, 2);
                }
            }else{
                for (int i = 0; i < lastword.length(); i++) {

                    lastletter = lastword.substring(1, 2);
                }
            }


            System.out.println("Initital string of last word" + lastletter);
            new_letter = firstletter.concat(lastletter);


        }
        return new_letter;
    }

    public TextDrawable name_image(String letter) {

        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int color1 = generator.getRandomColor();

        TextDrawable builder = TextDrawable.builder()
                .beginConfig()
                .withBorder(0)
                .width(150)  // width in px
                .height(150) // height in px
                .fontSize(64)
                .endConfig()
                .buildRoundRect(letter, Color.parseColor("#1F365C"), 150);

        return builder;
    }

    public SpannableStringBuilder getSpannableStringBuilder (String err_msg) {
        ForegroundColorSpan fgcspan = new ForegroundColorSpan(Color.RED);
        SpannableStringBuilder ssbuilder = new SpannableStringBuilder(err_msg);
        ssbuilder.setSpan(fgcspan, 0, err_msg.length(), 0);
        return ssbuilder;
    }

    public String md5Encode(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance(MD5);

            digest.update(s.getBytes());

            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    //To trim Volley error message and status code
    public String trimMessage(String json, String key) {
        String trimmedString;

        try {
            JSONObject obj = new JSONObject(json);
            trimmedString = obj.getString(key);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return trimmedString;
    }
    //To display Volley error message and status code
    public void  displayMessage(Context ctx,String toastString) {
        Toast.makeText(ctx, toastString, Toast.LENGTH_LONG).show();
    }

    public int gen() {
        Random r = new Random(System.currentTimeMillis());
        return 10000 + r.nextInt(20000);
    }

    public int fgen() {
        Random r = new Random(System.currentTimeMillis());
        return 30000 + r.nextInt(40000);
    }

    public String deDup(String s) {
        // TODO Auto-generated method stub
        return new LinkedHashSet<String>(Arrays.asList(s.split(",")))
                .toString().replaceAll("(^\\[|\\]$)", "").replace(", ", ",");

    }

    // to call api
    // to get currency
    public String letsCallApi(String url, List<BasicNameValuePair> param) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(param));
            HttpResponse response = client.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public void changelanguage(String languageToLoad, Context context) {

        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());

    }




   /* // to set and get offset value
    public int getOffset(Context ctx) {
        SharedPreferences sh_Pref = ctx.getSharedPreferences(CALL_STATE_PREF_NAME, Context.MODE_MULTI_PROCESS);
        return sh_Pref.getInt(Settings.PREFSIP_OFFSET, Settings.DEFAULTSIP_OFFSET);
    }

    public void setOffset(int offset, Context ctx) {
        SharedPreferences sh_Pref = ctx.getSharedPreferences(CALL_STATE_PREF_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor edit = sh_Pref.edit();
        edit.putInt(Settings.PREFSIP_OFFSET, offset);
        edit.commit();
    }*/

    public void setCurrentRecord(Context ctx, String jObjString) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("current_phone_records", jObjString);
        System.out.println("setCurrentRecords :: " + jObjString);
        editor.commit();
    }

    //Himadri
    // for update profile
    public String getProfileUpdate(Context ctx) {

        SharedPreferences sh_Pref = ctx.getSharedPreferences(CALL_STATE_PREF_NAME, Context.MODE_MULTI_PROCESS);
        System.out.println("get profile update " + sh_Pref.getString(Settings.PREFSIP_PROF_UPDATE,
                Settings.DEFAULTSIP_PROF_UPDATE));
        return sh_Pref.getString(Settings.PREFSIP_PROF_UPDATE, Settings.DEFAULTSIP_PROF_UPDATE);

    }

    // set update profile
    public void setProfileUpdate(String profileUpdate, Context ctx) {

        SharedPreferences sh_Pref = ctx.getSharedPreferences(CALL_STATE_PREF_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor edit = sh_Pref.edit();
        edit.putString(Settings.PREFSIP_PROF_UPDATE, profileUpdate);
        edit.commit();

    }

    public boolean isTablet(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }

    /*public static final String md5Encode(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }*/

    public String checkNetworkType(Context ctx){
        String networkType = "";
        ConnectivityManager connMgr = (ConnectivityManager)
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if( wifi.isAvailable() && wifi.getDetailedState() == NetworkInfo.DetailedState.CONNECTED){
            networkType = "wifi#";
        }
        else if( mobile.isAvailable() && mobile.getDetailedState() == NetworkInfo.DetailedState.CONNECTED ){
            networkType = "";
        }
        return networkType;
    }


    public void setBalanceAPICall (Context ctx, String aut_st) {
        ContentValues values = new ContentValues();
        values.put(BalanceProvider.STATUS, aut_st);
        // to check if database had already data or not
        Uri auto_uri = BalanceProvider.CONTENT_URI;
        Cursor c = ctx.getContentResolver().query(auto_uri, null, null, null, "status");
        if (c != null) {
            System.out.println("Check before insert for cusror count : "+c.getCount());
            if (c.getCount() == 0) {
                Uri uri = ctx.getContentResolver().insert(auto_uri, values);
            }else {
                c.moveToFirst();
                String selection = BalanceProvider._ID + "=?";
                String[] selArgs = {c.getString(c.getColumnIndexOrThrow(BalanceProvider._ID))};
                int up_id = ctx.getContentResolver().update(auto_uri, values, selection, selArgs);
                System.out.println("updated values : "+ up_id);
            }
        } else {
            System.out.println("Cursor is null");
        }
        //Uri uri = ctx.getContentResolver().insert(NumberRewritingProvider.CONTENT_URI, values);
    }

    // get Automatic rewriting value
    public String getBalanceAPICall (Context ctx) {
        Uri auto_uri = BalanceProvider.CONTENT_URI;
        Cursor c = ctx.getContentResolver().query(auto_uri, null, null, null, "status");
        String res = "";
        if (c!=null) {
            if (c.moveToFirst()) {
                int column_index = c.getColumnIndexOrThrow("status");
                res = c.getString(column_index);
            }
            c.close();
        }
        return res;
    }
    // set setBalanceAPICall
    public void setLastCalledNum (Context ctx, String aut_st) {
        ContentValues values = new ContentValues();
        values.put(HistoryStatusProvider.STATUS, aut_st);
        // to check if database had already data or not
        Uri auto_uri = HistoryStatusProvider.CONTENT_URI;
        Cursor c = ctx.getContentResolver().query(auto_uri, null, null, null, "status");
        if (c != null) {
            System.out.println("Check before insert for cusror count : "+c.getCount());
            if (c.getCount() == 0) {
                Uri uri = ctx.getContentResolver().insert(auto_uri, values);
            }else {
                c.moveToFirst();
                String selection = HistoryStatusProvider._ID + "=?";
                String[] selArgs = {c.getString(c.getColumnIndexOrThrow(HistoryStatusProvider._ID))};
                int up_id = ctx.getContentResolver().update(auto_uri, values, selection, selArgs);
                System.out.println("updated values : "+ up_id);
            }
        } else {
            System.out.println("Cursor is null");
        }
        //Uri uri = ctx.getContentResolver().insert(NumberRewritingProvider.CONTENT_URI, values);
    }

    // get balance api value
    public String getLastCalledNum (Context ctx) {
        Uri auto_uri = HistoryStatusProvider.CONTENT_URI;
        Cursor c = ctx.getContentResolver().query(auto_uri, null, null, null, "status");
        String res = "";
        if (c!=null) {
            if (c.moveToFirst()) {

                int column_index = c.getColumnIndexOrThrow("status");
                res = c.getString(column_index);
            }
        }
        c.close();

        return res;
    }


    public boolean isOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        GlobalClass.isOnline = isOnline;
    }

    public static boolean isIsCallRunning() {
        return isCallRunning;
    }
    public static void setIsCallRunning(boolean isCallRunning) {
        GlobalClass.isCallRunning = isCallRunning;
    }

    //For setting Sip connection status
    public String getAccountStatus() {
        return accountStatus;
    }
    //For getting Sip connection status
    public void setAccountStatus(String accountStatus) {
        GlobalClass.accountStatus = accountStatus;
    }



    //For getting Sip connection status code
    public int getAccountStatusCode() {
        return accountStatusCode;
    }
    //For setting Sip connection status code
    public void setAccountStatusCode(int accountStatusCode) {
        GlobalClass.accountStatusCode = accountStatusCode;
    }

    public void set_Contact_sync_flag(Context ctx, Boolean flag){
        ContentValues values = new ContentValues();
        values.put(ContactsProvider.STATUS, flag);
        // to check if database had already data or not
        Uri auto_uri = ContactsProvider.CONTENT_URI;
        Cursor c = ctx.getContentResolver().query(auto_uri, null, null, null, "status");
        if (c != null) {
            System.out.println("Check before insert for cusror count : "+c.getCount());
            if (c.getCount() == 0) {
                Uri uri = ctx.getContentResolver().insert(auto_uri, values);
            }else {
                c.moveToFirst();
                String selection = ContactsProvider._ID + "=?";
                String[] selArgs = {c.getString(c.getColumnIndexOrThrow(ContactsProvider._ID))};
                int up_id = ctx.getContentResolver().update(auto_uri, values, selection, selArgs);
                System.out.println("updated values : "+ up_id);
            }
        } else {
            System.out.println("Cursor is null");
        }
        //Uri uri = ctx.getContentResolver().insert(NumberRewritingProvider.CONTENT_URI, values);
    }

    public String getContact_flag(Context ctx){
        Uri auto_uri = ContactsProvider.CONTENT_URI;
        Cursor c = ctx.getContentResolver().query(auto_uri, null, null, null, "status");
        String res = "";
        if (c!=null) {
            if (c.moveToFirst()) {

                int column_index = c.getColumnIndexOrThrow("status");
                res = c.getString(column_index);
            }
        }
        c.close();

        return res;
    }

    public static boolean isDeviceLocked(Context context) {
        boolean isLocked = false;

        // First we check the locked state
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean inKeyguardRestrictedInputMode = keyguardManager.inKeyguardRestrictedInputMode();

        if (inKeyguardRestrictedInputMode) {
            isLocked = true;

        } else {
            // If password is not set in the settings, the inKeyguardRestrictedInputMode() returns false,
            // so we need to check if screen on for this case

            PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                isLocked = !powerManager.isInteractive();
            } else {
                //noinspection deprecation
                isLocked = !powerManager.isScreenOn();
            }
        }

        // Log.d(String.format("Now device is %s.", isLocked ? "locked" : "unlocked"));
        return isLocked;
    }

    //Added Missed Call number for resolving issue no: TC_011
    public String getLastMissedCall(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        lastMissedCall = prefs.getString("last_missed_call", "");
        return lastMissedCall;
    }
    public void setLastMissedCall(Context ctx, String missedCount) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_missed_call", missedCount);
        System.out.println("last_missed_call :: " + missedCount);
        editor.commit();

        //GlobalClass.rechargeAmount = rechargeAmount;
    }

    //Added Missed Call count for resolving issue no: TC_011
    public void setMissedCallCount (Context ctx, String aut_st) {
        ContentValues values = new ContentValues();
        values.put(MissedCallCountProvider.STATUS, aut_st);
        // to check if database had already data or not
        Uri auto_uri = MissedCallCountProvider.CONTENT_URI;
        Cursor c = ctx.getContentResolver().query(auto_uri, null, null, null, "status");
        if (c != null) {
            System.out.println("Check before insert for cusror count : "+c.getCount());
            if (c.getCount() == 0) {
                Uri uri = ctx.getContentResolver().insert(auto_uri, values);
            }else {
                c.moveToFirst();
                String selection = MissedCallCountProvider._ID + "=?";
                String[] selArgs = {c.getString(c.getColumnIndexOrThrow(MissedCallCountProvider._ID))};
                int up_id = ctx.getContentResolver().update(auto_uri, values, selection, selArgs);
                System.out.println("updated values : "+ up_id);
            }
        } else {
            System.out.println("Cursor is null");
        }
    }
    public String getMissedCallCount (Context ctx) {
        Uri auto_uri = MissedCallCountProvider.CONTENT_URI;
        Cursor c = ctx.getContentResolver().query(auto_uri, null, null, null, "status");
        String res = "";
        if (c!=null) {
            if (c.moveToFirst()) {
                int column_index = c.getColumnIndexOrThrow("status");
                res = c.getString(column_index);
            }
            c.close();
        }
        return res;
    }

}
