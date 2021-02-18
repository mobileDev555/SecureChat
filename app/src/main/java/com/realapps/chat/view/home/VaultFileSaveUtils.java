package com.realapps.chat.view.home;

import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.utils.AppConstants;

/**
 * Created by Hari Choudhary on 4/25/2019 at 1:08 PM .
 * Core techies
 * hari@coretechies.org
 */
public class VaultFileSaveUtils {

    private DbHelper db;
    private int mimeType;

    private int count = 2;
    String name = "";

    public VaultFileSaveUtils(DbHelper db, int mimeType) {
        this.db = db;
        this.mimeType = mimeType;
    }
    public String getFileName(String trim) {
        trim = removeExtension(trim);
        if (isFileExist(trim)) {
            if (trim.endsWith(")")) {
                getFileName(addSufToName(trim));
            } else {
                getFileName(trim + "(" + count + ")");
            }
            count = count + 1;
        } else {
            name = trim;
            count = 2;
        }
        return name;
    }

    private String removeExtension(String fileName) {
        if (fileName.indexOf(".") > 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName;
        }
    }
    private String addSufToName(String trim) {
        int lastIndexOfOpenBre = trim.lastIndexOf("(");
        int lastIndexOfCloseBre = trim.lastIndexOf(")");
        CharSequence charSequence = trim.subSequence(lastIndexOfOpenBre + 1, lastIndexOfCloseBre);
        int lastCount = Integer.parseInt(charSequence.toString());
        return trim.replace(charSequence, String.valueOf(lastCount + 1));
    }

    private boolean isFileExist(String name) {
        if (mimeType == AppConstants.ITEM_TYPE_NOTES && db.checkPersonalNoteName(name)) {
            return true;
        } else if (mimeType == AppConstants.ITEM_TYPE_PICTURE && db.checkImageName(name)) {
            return true;
        } else {
            return false;
        }
    }
}
