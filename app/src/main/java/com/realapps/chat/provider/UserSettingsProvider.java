package com.realapps.chat.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Saif Ahmed
 */

public class UserSettingsProvider extends ContentProvider {

    static final String PROVIDER_NAME = "com.realapps.chat.UserSettingsProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/details";
    static final Uri CONTENT_URI = Uri.parse(URL);

    // fields for the database
    static final String APP_PASSWORD = "appPassword";
    static final String MAX_PASSWORD_ATTEMPTS = "max_pwd_attempts";
    static final String TOKEN = "Token";
    static final String SUPPORT_ECC_ID = "support_ecc_id";
    static final String SCREEN_NAME = "screenName";
    static final String NORMAL_USER = "normal_user";
    static final String PGP_PASSWORD = "pgp_password";

    // integer values used in content URI
    static final int FRIENDS = 1;
    static final int FRIENDS_ID = 2;

    /*DBHelper dbHelper;*/

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
