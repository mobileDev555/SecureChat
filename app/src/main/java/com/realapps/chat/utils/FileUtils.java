package com.realapps.chat.utils;

import android.content.Context;

import com.realapps.chat.model.ContactEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by Prashant Sharma on 3/22/2018.
 */

public class FileUtils {

    public static String createPublicKeysFile(Context mContext, String keyContent, String filename) {
        File myFile = null;
        try {
            String keysBasePath = CommonUtils.getKeyBasePath(mContext);
            File keysDir = new File(keysBasePath + "publicKeys");
            if (!keysDir.exists()) {
                keysDir.mkdir();
            }
            myFile = new File(keysDir, filename);
            if (myFile.exists()) {
                myFile.delete();
                myFile.createNewFile();
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
                outputStreamWriter.write(keyContent);
                outputStreamWriter.close();
                return myFile.getAbsolutePath();
            } else {
                myFile.createNewFile();
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
                outputStreamWriter.write(keyContent);
                outputStreamWriter.close();
                return myFile.getAbsolutePath();
            }
        } catch (IOException e) {
            FileLog.e("Exception", "File write failed: " + e.toString());
            return null;
        }
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

    public static boolean checkAndCreateFolder(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return file.mkdirs();
        }

        return file.exists();
    }

    public static String createContactFile(Context mContext, ContactEntity contactListEntity) {

        File baseDir = new File(CommonUtils.getKeyBasePath(mContext) + "Contacts");
        if (!baseDir.exists())
            baseDir.mkdirs();
        File vcfFile = new File(baseDir + File.separator + contactListEntity.getName().toUpperCase() + ".vcf");
        try {
            FileWriter fw = new FileWriter(vcfFile);
            fw.write("BEGIN:VCARD\r\n");
            fw.write("VERSION:3.0\r\n");
            fw.write("N:" + contactListEntity.getName() + "\r\n");
            fw.write("FN:" + contactListEntity.getEccId() + "\r\n");
            fw.write("ORG:" + contactListEntity.getUserDbId() + "\r\n");
            fw.write("TITLE:" + "" + "\r\n");
            fw.write("TEL;TYPE=WORK,VOICE:" + "" + "\r\n");
            fw.write("ADR;TYPE=WORK:;;" + "" + "\r\n");
            fw.write("EMAIL;TYPE=PREF,INTERNET:" + contactListEntity.getUserType() + "\r\n");
            fw.write("END:VCARD\r\n");
            fw.close();
            return vcfFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

    }

    public static String[] createContactFile(Context mContext, ArrayList<ContactEntity> contactList) {
        String[] contactPathList = new String[contactList.size()];
        try {
            for (int i = 0; i < contactList.size(); i++) {
                File baseDir = new File(CommonUtils.getKeyBasePath(mContext) + "Contacts");
                if (!baseDir.exists())
                    baseDir.mkdirs();
                File vcfFile = new File(baseDir + File.separator + contactList.get(i).getName().toUpperCase() + ".vcf");
                FileWriter fw = new FileWriter(vcfFile);
                fw.write("BEGIN:VCARD\r\n");
                fw.write("VERSION:3.0\r\n");
                fw.write("N:" + contactList.get(i).getName() + "\r\n");
                fw.write("FN:" + contactList.get(i).getEccId() + "\r\n");
                fw.write("ORG:" + contactList.get(i).getUserDbId() + "\r\n");
                fw.write("TITLE:" + "" + "\r\n");
                fw.write("TEL;TYPE=WORK,VOICE:" + "" + "\r\n");
                fw.write("ADR;TYPE=WORK:;;" + "" + "\r\n");
                fw.write("EMAIL;TYPE=PREF,INTERNET:" + contactList.get(i).getUserType() + "\r\n");
                fw.write("END:VCARD\r\n");
                fw.close();
                contactPathList[i] = vcfFile.getAbsolutePath();
            }
            return contactPathList;

        } catch (IOException e) {
            e.printStackTrace();
            return contactPathList;
        }

    }
}
