/*
 * Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://mindorks.com/license/apache-v2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.realapps.chat.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.didisoft.pgp.CompressionAlgorithm;
import com.didisoft.pgp.CypherAlgorithm;
import com.didisoft.pgp.EcCurve;
import com.didisoft.pgp.HashAlgorithm;
import com.didisoft.pgp.KeyAlgorithm;
import com.didisoft.pgp.KeyPairInformation;
import com.didisoft.pgp.KeyStore;
import com.didisoft.pgp.PGPException;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.data.network.ApiEndPoints;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.view.dialoges.DialogUnlock;
import com.realapps.chat.view.home.activity.LockScreenActivity;
import com.realapps.chat.view.login.activity.LoginActivity;
import com.tapadoo.alerter.Alerter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Prashant Sharma on 27/01/17.
 * Core techies
 * prashant@coretechies.org
 */

public final class CommonUtils {
    public static final String TAG = "====CommonUtils";
   private static final String PASSWORD_REGEX_6CHARACTER = "^.*(?=.{6,15})(?=.*\\d)(?=.*[a-zA-Z]).*$";   //Minimum SIX characters, at least one letter and one number:
     public static DialogUnlock dialogUnlock;

    public CommonUtils() {
        // This utility class is not publicly instantiable
    }

    public static boolean isPassword(Context mContext, EditText editText, boolean required) {
        return isValid(mContext, editText, PASSWORD_REGEX_6CHARACTER, mContext.getString(R.string.password_must_be_of_6_digit_and_must_contain_at_least_one_digit_one_character), required);
    }

    public static ProgressDialog showLoadingDialog(Context mContext) {
        ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.show();
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

    @SuppressLint("all")
    public static String getDeviceId(Context mContext) {
        return Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getHours(String burnTime) {
        if(burnTime==null || burnTime.length()==0) {
            Log.e("blank", "bb");
            return  "";
        }
         Date endDate = ConvertStringToDate(burnTime, "yyyy-MM-dd HH:mm:ss");
        Date nowDate = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        try {
            nowDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            boolean status = CommonUtils.compareSnoozeDate(endDate, nowDate);
            if (!status) {
                long diff = endDate.getTime() - nowDate.getTime();
                int day = (int) ((diff / (1000 * 60 * 60 * 24)));
                int hours = (int) ((diff / (1000 * 60 * 60)) % 24);
                int minutes = (int) ((diff / (1000 * 60)) % 60);
                int seconds = (int) ((diff / 1000) % 60);
                if (day > 0)
                    return day + " days";
                else if (hours > 0)
                    return hours + " hours";
                else if (minutes > 0)
                    return minutes + " minutes";
                else
                    return seconds + " seconds";
            } else
                return "0 seconds";

        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean compareSnoozeDate(Date endDate, Date nowDate) {
          if (nowDate.compareTo(endDate) == 0) { //Current date is equal to end date
                return true;
            } else if (nowDate.compareTo(endDate) < 0) { //Current date is before end date
                return false;
            } else //Current date is after end date
                return nowDate.compareTo(endDate) > 0;
    }

    public static boolean isEmailValid(String email) {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isEmailAddress(Context mContext, EditText editText, boolean required) {
        if (editText.getText().toString().trim().length() > 0)
            return isValid(mContext, editText, AppConstants.EMAIL_REGEX, mContext.getString(R.string.please_enter_valid_email_id), required);
        else
            return true;
    }

    // return true if the input field is valid, based on the parameter passed
    public static boolean isValid(Context mContext, EditText editText, String regex,
                                  String errMsg, boolean required) {

        String text = editText.getText().toString().trim();
        // clearing the error, if it was previously set by some other values
        editText.setError(null);

        // text required and editText is blank, so return false
        if (required && !hasText(mContext, editText))
            return false;

        // pattern doesn't match so returning false Pattern.matches(regex, text)

        if (required && !Pattern.matches(regex, text)) {
            showInfoMsg(mContext, errMsg);
            editText.requestFocus();
            return false;
        }

        return true;
    }

    public static boolean hasText(Context mContext, EditText editText) {

        String text = editText.getText().toString().trim();
        editText.setError(null);
        if (text.length() == 0) {
            editText.setError(mContext.getResources().getString(R.string.required));
            editText.requestFocus();
            return false;
        }

        return true;
    }

    public static boolean hasText(Context mContext, EditText editText, String msg) {

        String text = editText.getText().toString().trim();
        editText.setError(null);
        if (text.length() == 0) {
            showInfoMsg(mContext, msg);
            editText.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean hasTextdialog(Context mContext, EditText editText, String msg) {
        String text = editText.getText().toString().trim();
        editText.setError(null);
        if (text.length() == 0) {
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            editText.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean hasText(Context mContext, TextView editText, String msg) {

        String text = editText.getText().toString().trim();
        editText.setError(null);
        if (text.length() == 0) {
            showInfoMsg(mContext, msg);
            editText.requestFocus();
            return false;
        }
        return true;
    }


    public static boolean length_check(Context mContext, EditText editText, String msg,
                                       int len_min, int len_max) {

        String text = editText.getText().toString().trim();
        editText.setError(null);

        // length 0 means there is no text
        if (text.length() < len_min || text.length() > len_max) {
            showInfoMsg(mContext, msg);
            editText.requestFocus();
            return false;
        }

        return true;
    }
    public static void showInfoMsg(Context mContext, String msg) {
        Alerter.create((Activity) mContext)
                .setText(msg)
                .setDuration(1500)
                .setBackgroundColorRes(R.color.colorPrimary)
                .enableSwipeToDismiss()
                .show();
    }

    public static void showErrorMsg(Context mContext, String msg) {
        Alerter.create((Activity) mContext)
                .setText(msg)
                .setDuration(1500)
                .setIcon(R.drawable.ic_error_outline_black_24dp)
                .setBackgroundColorRes(R.color.colorPrimary)
                .enableSwipeToDismiss()
                .show();
    }


    public static String loadJSONFromAsset(Context mContext, String jsonFileName)
            throws IOException {
        AssetManager manager = mContext.getAssets();
        InputStream is = manager.open(jsonFileName);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        return new String(buffer, "UTF-8");
    }

    public static boolean compareText(Context mContext, EditText editTextTo, EditText editTextCompaireWith, String msg) {

        String To = editTextTo.getText().toString().trim();
        String from = editTextCompaireWith.getText().toString().trim();
        editTextTo.setError(null);
        editTextCompaireWith.setError(null);
        if (To.equals(from)) {
            return true;
        } else {
            showErrorMsg(mContext, msg);
            editTextTo.requestFocus();
            return false;
        }

    }

    public static boolean compareText(Context mContext, String compareto, String editTextCompaireWith, String msg) {

        String To = compareto;
        String from = editTextCompaireWith;
        if (To.equals(from)) {
            return true;
        } else {
            showErrorMsg(mContext, msg);
            return false;
        }
    }

    public static boolean comparedurText(Context mContext, String compareto, String editTextCompaireWith, String msg) {

        String To = compareto;
        String from = editTextCompaireWith;

        if (To.equals(from)) {
            showErrorMsg(mContext, msg);
            return false;
        } else {
            return true;
        }
    }

    public static boolean sameText(Context mContext, EditText editTextTo, EditText editTextCompaireWith, String msg) {

        String To = editTextTo.getText().toString().trim();
        String from = editTextCompaireWith.getText().toString().trim();
        editTextTo.setError(null);
        editTextCompaireWith.setError(null);
        if (To.equals(from)) {
            showErrorMsg(mContext, msg);
            editTextTo.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    public static String getKeyBasePath(Context mContext) {
        return User_settings.getAttachDir(mContext) + File.separator;
    }

    public static String getImageDirectory(Context mContext) {
        File folder = new File(getKeyBasePath(mContext) + "Images");
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath();
    }
    public static String getVideoDirectory(Context mContext) {
        File folder = new File(getKeyBasePath(mContext) + "Videos");
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath();
    }

    public static String getAudioDirectory(Context mContext) {
        File folder = new File(getKeyBasePath(mContext) + "Audio");
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath();
    }

    public static String getContactDirectory(Context mContext) {
        File folder = new File(getKeyBasePath(mContext) + "Contacts");
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath();
    }

    public static String getTextsDirectory(Context mContext) {
        File folder = new File(getKeyBasePath(mContext) + "Texts");
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath();
    }

    public static String getNotesDirectory(Context mContext) {
        File folder = new File(getKeyBasePath(mContext) + "Texts");
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath();
    }

    public static String getEccPublicKey(Context mContext) {
        String ecc_public_key = FileUtils.readKeyString(CommonUtils.getKeyBasePath(mContext) + AppConstants.pubECCKeyName);
        ecc_public_key = ecc_public_key.replace("\n", "");
        ecc_public_key = ecc_public_key.replaceAll("-----BEGIN PGP PUBLIC KEY BLOCK-----Version: BCPG v@RELEASE_NAME@", "");
        ecc_public_key = ecc_public_key.replaceAll("-----END PGP PUBLIC KEY BLOCK-----", "");
        return ecc_public_key;
    }

    public static String getAppVersion(Context mContext) {
        String version = "";
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static String readKeyString(String path) {
        File file = new File(path);

//Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return text.toString();
    }

    public static String getUserEmail(String id) {
        //new changes 21-09-2018
        String email = id;
        return email;
    }

    public static boolean isSubscriptionValid(Context mContext) {
        return true;
    }

    public static String getBurnTime(Context mContext, int id, int type) {
        List<String> des_time = Arrays.asList(mContext.getResources().getStringArray(R.array.burn_time));

        if (type == AppConstants.TIME_TEXT_TYPE_NORMAL) {
            if (id >= 0 && id <= 12)
                return des_time.get(id) + AppConstants.SECONDS;
            else if (id >= 13 && id <= 27)
                return des_time.get(id) + AppConstants.MINUTES;
            else if (id >= 28 && id <= 38)
                return des_time.get(id) + AppConstants.HOURS;
            else
                return des_time.get(id) + AppConstants.DAYS;
        } else {
            if (id >= 0 && id <= 12)
                return des_time.get(id) + AppConstants.SECONDS_SMALL;
            else if (id >= 13 && id <= 27)
                return des_time.get(id) + AppConstants.MINUTES_SMALL;
            else if (id >= 28 && id <= 38)
                return des_time.get(id) + AppConstants.HOURS_SMALL;
            else
                return des_time.get(id) + AppConstants.DAYS_SMALL;
        }
    }

    public static String getBurnTime(Context mContext, int id) {
        List<String> des_time = Arrays.asList(mContext.getResources().getStringArray(R.array.burn_time));

        if (id >= 0 && id <= 12)
            return des_time.get(id) + AppConstants.SECONDS;
        else if (id >= 13 && id <= 27)
            return des_time.get(id) + AppConstants.MINUTES;
        else if (id >= 28 && id <= 38)
            return des_time.get(id) + AppConstants.HOURS;
        else
            return des_time.get(id) + AppConstants.DAYS;
    }

    public static String getFileExt(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1).trim();
    }

    public static boolean hasPermissions(Context mContext, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mContext != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }
        return true;
    }

    public static boolean duressPass(Context mContext, String duressPassword, EditText editTextCompaireWith) {

        String from = editTextCompaireWith.getText().toString().trim();
        editTextCompaireWith.setError(null);
        return duressPassword.equals(from);

    }


    ////////////////////////////////////Service is running or not ///////////////////////////////////////////////////////
    public static boolean isMyServiceRunning(Context ctx, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }




    public static void sendDeviceToken(Context mContext, int status) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", 14);
            jsonObject.put("userId", Integer.parseInt(User_settings.getUserId(mContext)));
            jsonObject.put("eccId", User_settings.getECCID(mContext));
            jsonObject.put("deviceToken", User_settings.getFirebaseToken(mContext));
            //  jsonObject.put("deviceToken", "khfhegt");
            jsonObject.put("status", status);
            jsonObject.put("dtype", 1);
            if (AppConstants.mWebSocketClient != null && AppConstants.mWebSocketClient.isOpen())
                AppConstants.mWebSocketClient.send(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean compareTextDialog(Context mContext, String compareto, String editTextCompaireWith, String msg) {

        String To = compareto;
        String from = editTextCompaireWith;

        if (To.equals(from)) {
            return true;
        } else {

            return false;
        }

    }

    public static void clearNotification(Context mContext) {
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    public static void lockDialog(Activity mActivity) {

    }


    public static void checkDialog(Activity mContext) {

        try {
            if (dialogUnlock != null) {
                if (dialogUnlock.isShowing())
                    if (mContext instanceof Activity) {
                        if (!mContext.isFinishing() && !mContext.isDestroyed())
                            dialogUnlock.dismiss();
                    } else //if the Context used wasnt an Activity, then dismiss it too
                        dialogUnlock.dismiss();
            }
            dialogUnlock = null;
            AppConstants.lockscreen = false;
            lockDialog(mContext);
        } catch (Exception e) {
            AppConstants.lockscreen = false;
            e.printStackTrace();
        }
    }

    public static String getContactName(Context mContext, String eddId) {
        String userName;
        DbHelper dbHelper = new DbHelper(mContext);
        String name = dbHelper.getContactName(eddId);

        if (name.length() > 0)
            userName = name.substring(0, 1).toUpperCase() + name.substring(1);
        else
            userName = eddId;

        Log.i(TAG, "getContactName: " + userName);
        return userName;
    }

    public static String getGroupName(Context mContext, String eddId) {
        String userName;
        DbHelper dbHelper = new DbHelper(mContext);
        String name = dbHelper.getGroupName(eddId);

        if (name.length() > 0)
            userName = name.substring(0, 1).toUpperCase() + name.substring(1);
        else
            userName = eddId;

        Log.e(TAG, "getContactName: " + userName);
        return userName;
    }

    public static void createTempFile() {

    }

    public static String textCapsString(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static void hideTextSuggestion(EditText editText) {
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
    }

    public static void hideTextSuggestionMultiLine(EditText editText) {
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
    }

    public static void hideTextSuggestionOnly(EditText editText) {
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

    public static void checkAppSignature(Context context) {

        try {

            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature signature : packageInfo.signatures) {
                byte[] signatureBytes = signature.toByteArray();
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                final String currentSignature = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d("REMOVE_ME", "Include this string as a value for SIGNATURE:" + currentSignature);

            }

        } catch (Exception e) {
            e.printStackTrace();
            //assumes an issue in checking signature., but we let the caller decide on what to do.
        }

    }

    public static String getLanguageCode(int lanId) {

        if (lanId == 0) {
            return "en";
        } else if (lanId == 1) {
            return "nl";
        } else if (lanId == 2) {
            return "es";
        } else if (lanId == 3) {
            return "tr";
        } else if (lanId == 4) {
            return "de";
        } else if (lanId == 5) {
            return "fr";
        } else if (lanId == 6) {
            return "it";
        } else if (lanId == 7) {
            return "pl";
        } else if (lanId == 8) {
            return "sv";
        } else if (lanId == 9) {
            return "ru";
        } else if (lanId == 10) {
            return "pt";
        } else if (lanId == 11) {
            return "zh";
        }

        return "en";
    }

    public static String getLanguageStr(String lan) {
        final CharSequence langs[] = new CharSequence[]{"ENGLISH", "NEDERLANDS", "ESPANOL", "TURKCE", "DEUTSCHE", "FRANCAISE", "ITALIANO", "POLSKI", "SVENSKA", "RYSKA", "PORTUGUES", "MANDARIN"};
        if (lan != null && lan.length() > 0) {
            if (lan.equalsIgnoreCase("en")) {
                return (String) langs[0];
            } else if (lan.equalsIgnoreCase("nl")) {
                return (String) langs[1];
            } else if (lan.equalsIgnoreCase("es")) {
                return (String) langs[2];
            } else if (lan.equalsIgnoreCase("tr")) {
                return (String) langs[3];
            } else if (lan.equalsIgnoreCase("de")) {
                return (String) langs[4];
            } else if (lan.equalsIgnoreCase("fr")) {
                return (String) langs[5];
            } else if (lan.equalsIgnoreCase("it")) {
                return (String) langs[6];
            } else if (lan.equalsIgnoreCase("pl")) {
                return (String) langs[7];
            } else if (lan.equalsIgnoreCase("sv")) {
                return (String) langs[8];
            } else if (lan.equalsIgnoreCase("ru")) {
                return (String) langs[9];
            } else if (lan.equalsIgnoreCase("pt")) {
                return (String) langs[10];
            } else if (lan.equalsIgnoreCase("zh")) {
                return (String) langs[11];
            }
        }
        return "Default";
    }

    public static boolean checkPasswordLength(Context context, EditText txtNewPwd, int id) {
        boolean isValid = false;
        if (txtNewPwd.getText().toString().trim().length() > 6 && txtNewPwd.getText().toString().trim().length() <= 20) {
            isValid = true;
        } else {
            isValid = false;
            showInfoMsg(context, context.getString(id));
        }

        return isValid;
    }

    public static boolean length_6(Context mContext, EditText editText, String msg) {

        String text = editText.getText().toString().trim();
        editText.setError(null);
        if (text.length() != 6) {
            showInfoMsg(mContext, msg);
            return false;
        }

        return true;
    }

    public static void showLockScreen(Context context) {
        Intent intent = new Intent(context, LockScreenActivity.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public boolean exportECCKey(Context mContext) throws IOException, PGPException {
        OutputStream keyPairStream = null;
        OutputStream publicKeyStream = null;
        OutputStream privateKeyStream = null;
        try {
            KeyStore keyStore = generateECC(mContext);
            InputStream keystoreStream = mContext.openFileInput(AppConstants.KeyStoreECC);
            keyStore.loadFromStream(keystoreStream);
            KeyPairInformation[] keys = keyStore.getKeys();
            String userId = keys[0].getUserIDs()[0];
            String basePath = getKeyBasePath(mContext); //User_settings.getAttachDir(mContext) + File.separator;
            // specifies will the output be ASCII armored or binary
            boolean asciiArmor = true;

            // export public and private key into a single file
            keyPairStream = new FileOutputStream(basePath + AppConstants.KeyPairECCName); //mContext.openFileOutput(AppConstants.KeyPairPGPName, mContext.MODE_PRIVATE);
            keyStore.exportKeyRing(keyPairStream, userId, asciiArmor);
            FileLog.sout("keypair path " + mContext.getFileStreamPath(AppConstants.KeyPairECCName).getAbsolutePath());

            // export only public key
            publicKeyStream = new FileOutputStream(basePath + AppConstants.pubECCKeyName); //mContext.openFileOutput(AppConstants.pubPGPKeyName, mContext.MODE_PRIVATE);
            keyStore.exportPublicKey(publicKeyStream, userId, asciiArmor);
            FileLog.sout("pubkey path " + mContext.getFileStreamPath(AppConstants.pubECCKeyName).getAbsolutePath());
            // export only private key
            privateKeyStream = new FileOutputStream(basePath + AppConstants.privECCKeyName); //mContext.openFileOutput(AppConstants.privPGPKeyName, mContext.MODE_PRIVATE);
            keyStore.exportPrivateKey(privateKeyStream, userId, asciiArmor);
            FileLog.sout("privkey path " + mContext.getFileStreamPath(AppConstants.privECCKeyName).getAbsolutePath());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {

            if (keyPairStream != null) {
                keyPairStream.close();
            }
            if (publicKeyStream != null) {
                publicKeyStream.close();
            }
            if (privateKeyStream != null) {
                privateKeyStream.close();
            }
        }
    }

    public KeyStore generateECC(Context mContext) throws Exception {
       int keySizeInBits = AppConstants.ECCKeySize;
        String userId = User_settings.getECCID(mContext) + " <" + User_settings.getECCID(mContext) + AppConstants.ECCDomain + ">";
        String keyPassword = AppConstants.tempPassword;//us.getUserPassword(mContext);
        KeyStore keyStore = new KeyStore(AppConstants.KeyStoreECC, keyPassword);
         EcCurve curve = EcCurve.P384;

        try {
            // asymmetric encryption algorithm
            KeyAlgorithm algorithm = KeyAlgorithm.EC;
            // preferred hashing algorithms
            HashAlgorithm[] hashingAlgorithms = new HashAlgorithm[]
                    {HashAlgorithm.SHA512};
            // preferred compression algorithms
            CompressionAlgorithm[] compressions = new CompressionAlgorithm[]
                    {CompressionAlgorithm.ZIP,
                            CompressionAlgorithm.ZLIB,
                            CompressionAlgorithm.UNCOMPRESSED};
            // preferred symmetric key algorithms
            CypherAlgorithm[] cyphers = new CypherAlgorithm[]{
                    CypherAlgorithm.AES_256};
            // load the key store data from a previous session
            File keystoreFile = mContext.getFileStreamPath(AppConstants.KeyStoreECC);
            if (keystoreFile.exists()) {
                keystoreFile.delete();
            }
         KeyPairInformation key = keyStore.generateEccKeyPair(curve,
                    userId,
                    keyPassword,
                    compressions,
                    hashingAlgorithms,
                    cyphers,
                    keySizeInBits);



        } catch (PGPException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            OutputStream keystoreStream = mContext.openFileOutput(AppConstants.KeyStoreECC, Context.MODE_PRIVATE);
            // save the key store data
            keyStore.saveToStream(keystoreStream);

            if (keystoreStream != null) {
                keystoreStream.close();
            }
        }

        return keyStore;
    }

    public void resetAll(Context mContext) {
        AppConstants.lockscreen = false;
        AppConstants.onpermission = true;
        Activity mActivity = (Activity) mContext;
        DbHelper db = new DbHelper(mContext);
        db.resetDB();
        User_settings.logout(mContext);
        sendDeviceToken(mContext, 0);
        // new Utility().showCustomToast(mContext, "Reset done");
        mContext.startActivity(new Intent(mActivity, LoginActivity.class));
        mActivity.finishAffinity();
    }

    public static void checkStatus(Context mContext) {
        AndroidNetworking.get(ApiEndPoints.END_POINT_GET_USER_STATUS)
                .addQueryParameter("user_id", User_settings.getUserId(mContext))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(response.toString());
                            if (rootObject.getString("status").equalsIgnoreCase("1")) {
                                User_settings.setUserActiveStatus(mContext, getUserStatus(rootObject.getString("user_status")));
                                User_settings.setInventrystatus(mContext, !getUserStatus(rootObject.getString("inventry_status")));
                                User_settings.setEndDateLicense(mContext,rootObject.getString("SubValid"));
                                User_settings.setSubscriptionStatus(mContext, checkStatus(rootObject.getString("SubValid"), mContext));

                                if (response.has("SubEmail") && response.getString("SubEmail") != null) {
                                    final String eccId = response.getString("SubEmail").substring(0, response.getString("SubEmail").indexOf("@"));
                                    if (!eccId.equalsIgnoreCase(User_settings.getECCID(mContext))) {
                                        new CommonUtils().resetAll(mContext);
                                    }
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                    }
                });
    }

    public static boolean checkStatus(String endDate, Context mContext) {
        int totalDays = (int) getTotalDays(endDate);
        if (totalDays <= 0) {
            User_settings.setSubscriptionStatus(mContext, false);
            return false;
        } else {
            User_settings.setSubscriptionStatus(mContext, true);
            return true;
        }
    }

    public static boolean getUserStatus(String userStatus) {
        if (userStatus != null && userStatus.length() > 0) {
            if (userStatus.equalsIgnoreCase("Y")) {
                return true;
            } else if (userStatus.equalsIgnoreCase("N")) {
                return false;
            }
        }
        return false;
    }

    public static long getTotalDays(String endDateStr) {
        Date currentDate = getCurrentDateTime(AppConstants.Format_yyyyMMdd);
        Date endDate = ConvertStringToDate(endDateStr, AppConstants.Format_yyyyMMdd);
        FileLog.sout("current_date : " + currentDate.toString());
        FileLog.sout("end_date : " + endDate.toString());
        long diff = endDate.getTime() - currentDate.getTime();
        FileLog.sout("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    public static boolean getTimeDifference(String endDateStr) {
        Date currentDate = getCurrentDateTime("yyyy-MM-dd HH:mm:ss");
        Date endDate = ConvertStringToDate(endDateStr, "yyyy-MM-dd HH:mm:ss");
        long diff = currentDate.getTime() - endDate.getTime();
        if (TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS) < 5)
            return true;
        else
            return false;
    }

    public static String getTime(String endDateStr) {
        String time = "0";
        Date currentDate = getCurrentDateTime(AppConstants.Format_yyyyMMdd);
        Date endDate = ConvertStringToDate(endDateStr, AppConstants.Format_yyyyMMdd);
        FileLog.sout("current_date : " + currentDate.toString());
        FileLog.sout("end_date : " + endDate.toString());
        long diff = endDate.getTime() - currentDate.getTime();
        FileLog.sout("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
        if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) == 0) {
            time = endDateStr.substring(endDateStr.lastIndexOf(" ") + 1);
            time = time.substring(0, 5);
        } else if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) == 1) {
            time = "Yesterday";
        } else {
            time = endDateStr.substring(0, endDateStr.indexOf(" "));
        }
        return time;
    }

    public static Date getCurrentDateTime(String format) {
        String currentDateTimeString = ""; // = DateFormat.getDateTimeInstance().format(new Date());
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault()); //"E MMM dd HH:mm:ss Z yyyy"
        Date date = null;
        try {
            currentDateTimeString = formatter.format(new Date());
            date = (Date) formatter.parse(formatter.format(new Date()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date ConvertStringToDate(String dateStr, String format) {
        DateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        Date date = null;
        try {
            date = formatter.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    public static String getImageName() {
        return "IMG" + "_" + getCurrentTime("dd-M-yyyy hh:mm:ss");
    }

    private static String getCurrentTime(String s) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(s);
        return simpleDateFormat.format(new Date());
    }


    public static void appendLog(String text)
    {
        File logFile = new File("sdcard/log.txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
