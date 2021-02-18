package com.realapps.chat.utils;

import android.content.Context;

import com.didisoft.pgp.PGPException;
import com.didisoft.pgp.PGPLib;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.model.GroupMemberEntity;
import com.realapps.chat.model.PublicKeyEntity;
import com.realapps.chat.ui.utils.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Prashant Sharma on 3/20/2018.
 */

public class Cryptography {

    public static String decryptText(Context mContext, String messageText) {
        PGPLib pgp = new PGPLib();
        String basePath = CommonUtils.getKeyBasePath(mContext);
        String privateKey = basePath + AppConstants.privECCKeyName;
        String decryptString = "";
        try {
            decryptString = pgp.decryptString(messageText, privateKey, AppConstants.tempPassword);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PGPException e) {
            e.printStackTrace();
        }
        return decryptString;
    }

    public static String encryptText(Context mContext, int userDbId, String msg) {
        DbHelper db = new DbHelper(mContext);
        PGPLib pgp = new PGPLib();
        pgp.setAsciiVersionHeader(AppConstants.asciiHeader);
        PublicKeyEntity publicKeyEntity = db.getPublicKeys(userDbId);
        InputStream publicKeyStream = new ByteArrayInputStream(publicKeyEntity.getEccPublicKey().getBytes());
        String eMsg = null;
        try {
            eMsg = pgp.encryptString(msg, publicKeyStream);
        } catch (PGPException e) {
            e.printStackTrace();
            Log.e("========pgp", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("========pgp", e.getMessage());
        }
        Log.e("=========encrypted msg", eMsg);
        return eMsg;
    }

    public static String encryptGroupText(Context mContext, ArrayList<GroupMemberEntity> groupMemberList, String msg) {
        GroupChatUtils groupEncrypt = new GroupChatUtils();
        groupEncrypt.setAsciiVersionHeader(AppConstants.asciiHeader);
        DbHelper db = new DbHelper(mContext);
        String encryptedMsg = "";
        int size = groupMemberList.size();
        String[] publicKeyPath = new String[size];
        for (int i = 0; i < groupMemberList.size(); i++) {
            publicKeyPath[i] = FileUtils.createPublicKeysFile(mContext, db.getPublicKeys(groupMemberList.get(i).getUserDbId()).getEccPublicKey(), (groupMemberList.get(i).getEccId().toUpperCase() + ".asc"));
        }
        try {
            encryptedMsg = groupEncrypt.encryptStrings(msg, publicKeyPath, "UTF-8");
        } catch (org.spongycastle.openpgp.PGPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encryptedMsg;
    }

    public static String encryptFile(Context mContext, String filePath, int userDbId, String eccId, int fileType) throws PGPException, IOException {
        DbHelper dbHelper = new DbHelper(mContext);
        String basePath = "";
        File basePathDir;
        if (fileType == AppConstants.MIME_TYPE_IMAGE) {
            basePath = CommonUtils.getKeyBasePath(mContext) + "Images";
        } else if (fileType == AppConstants.MIME_TYPE_AUDIO) {
            basePath = CommonUtils.getKeyBasePath(mContext) + "Audio";
        } else if (fileType == AppConstants.MIME_TYPE_VIDEO) {
            basePath = CommonUtils.getKeyBasePath(mContext) + "Videos";
        } else if (fileType == AppConstants.MIME_TYPE_CONTACT) {
            basePath = CommonUtils.getKeyBasePath(mContext) + "Contacts";
        } else if (fileType == AppConstants.MIME_TYPE_NOTE) {
            basePath = CommonUtils.getKeyBasePath(mContext) + "Texts";
        }
        basePathDir = new File(basePath);
        if (!basePathDir.exists()) {
            basePathDir.mkdirs();
        }

        File tempF = new File(filePath);
        File encFile = new File(basePath + File.separator + "E" + tempF.getName().replaceAll("\n", " "));

        try {
            PGPLib pgp = new PGPLib();
            String[] publicKey = {FileUtils.createPublicKeysFile(mContext, dbHelper.getPublicKeys(userDbId).getEccPublicKey(), (eccId.toUpperCase() + ".asc")), CommonUtils.getKeyBasePath(mContext) + AppConstants.pubECCKeyName};
            pgp.encryptFile(filePath, publicKey, encFile.getAbsolutePath(), false, false);
        } catch (PGPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return encFile.getAbsolutePath();
    }

    public static String encryptFileGroup(Context mContext, String filePath, ArrayList<GroupMemberEntity> groupMemberList, int fileType) throws PGPException, IOException {
        DbHelper dbHelper = new DbHelper(mContext);
        String basePath = "";
        File basePathDir;
        if (fileType == AppConstants.MIME_TYPE_IMAGE) {
            basePath = CommonUtils.getKeyBasePath(mContext) + "Images";
        } else if (fileType == AppConstants.MIME_TYPE_AUDIO) {
            basePath = CommonUtils.getKeyBasePath(mContext) + "Audio";
        } else if (fileType == AppConstants.MIME_TYPE_VIDEO) {
            basePath = CommonUtils.getKeyBasePath(mContext) + "Videos";
        } else if (fileType == AppConstants.MIME_TYPE_CONTACT) {
            basePath = CommonUtils.getKeyBasePath(mContext) + "Contacts";
        } else if (fileType == AppConstants.MIME_TYPE_NOTE) {
            basePath = CommonUtils.getKeyBasePath(mContext) + "Texts";
        }
        basePathDir = new File(basePath);
        if (!basePathDir.exists()) {
            basePathDir.mkdirs();
        }
        int size = groupMemberList.size();
        String[] publicKeyPath = new String[size];
        for (int i = 0; i < groupMemberList.size(); i++) {
            publicKeyPath[i] = FileUtils.createPublicKeysFile(mContext, dbHelper.getPublicKeys(groupMemberList.get(i).getUserDbId()).getEccPublicKey(), (groupMemberList.get(i).getEccId().toUpperCase() + ".asc"));
        }

        File tempF = new File(filePath);
        File encFile = new File(basePath + File.separator + "EG" + tempF.getName().replaceAll("\n", " "));

        try {
            PGPLib pgp = new PGPLib();
            pgp.encryptFile(filePath, publicKeyPath, encFile.getAbsolutePath(), false, false);
        } catch (PGPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encFile.getAbsolutePath();
    }

    public static String decryptFile(Context mContext, String encryptedFilePath) {
        PGPLib pgp = new PGPLib();
        String decryptPath = CommonUtils.getKeyBasePath(mContext) + AppConstants.TEMP_FILE_NAME + CommonUtils.getFileExt(encryptedFilePath);
        File newFile = new File(decryptPath);
        String privateKey = CommonUtils.getKeyBasePath(mContext) + AppConstants.privECCKeyName;
        if (newFile.exists()) {
            newFile.delete();
        }
        try {
            pgp.decryptFile(encryptedFilePath, privateKey, AppConstants.tempPassword, decryptPath);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (PGPException e) {
            e.printStackTrace();
            return "";
        }
        return decryptPath;

    }

}
