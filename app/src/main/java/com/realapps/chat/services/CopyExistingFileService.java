package com.realapps.chat.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.model.VaultEntity;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DbConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CopyExistingFileService extends IntentService {

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_COPY = "com.realapps.chat.services.action.COPY";
    private static final String ACTION_DELETE = "com.realapps.chat.services.action.DELETE";

    private static final String EXTRA_PARAM1 = "com.realapps.chat.services.extra.FILE_LIST";


    public CopyExistingFileService() {
        super("CopyExistingFileService");
    }

    public static void startActionCopy(Context context, ArrayList<VaultEntity> param1) {
        Intent intent = new Intent(context, CopyExistingFileService.class);
        intent.setAction(ACTION_COPY);
        intent.putExtra(EXTRA_PARAM1, param1);
        context.startService(intent);
    }

    public static void deleteTinyDirectory(Context applicationContext) {
        Intent intent = new Intent(applicationContext, CopyExistingFileService.class);
        intent.setAction(ACTION_DELETE);
        applicationContext.startService(intent);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_COPY.equals(action)) {
                final ArrayList<VaultEntity> extra = (ArrayList<VaultEntity>) intent.getSerializableExtra(EXTRA_PARAM1);
                handleActionCopy(extra);
            } else if (ACTION_DELETE.equals(action)) {
                handleActionDelete();
            }
        }
    }
    private void handleActionDelete() {
        File tinyDir = new File("/storage/emulated/0/Android/data/com.realapps.chat/tiny/");
        if (tinyDir.exists()) {
            final File[] files = tinyDir.listFiles();
            if (files.length > 0) {
                for (File file : files) {
                    file.delete();
                }
            }

            //delete parent
            tinyDir.delete();
        }

    }

    private void handleActionCopy(ArrayList<VaultEntity> extra) {
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        File parent = null;

        for (VaultEntity entity : extra) {
            File src = new File(entity.getImage());
            if (parent == null)
                parent = src.getParentFile();
            File dst = new File(CommonUtils.getImageDirectory(getApplicationContext()), src.getAbsolutePath().substring(src.getAbsolutePath().lastIndexOf("/") + 1));
            try {
                copy(src, dst);
                dbHelper.updateVaultItem(DbConstants.KEY_IMAGE_PATH, dst.getAbsolutePath(), DbConstants.KEY_IMAGE_PATH, src.getAbsolutePath());
                src.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final File[] files = parent.listFiles();
        if (files.length > 0) {
            for (File file : files) {
                file.delete();
            }
        }
        //delete parent
        parent.delete();

    }


    public String copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
        return dst.getAbsolutePath();
    }


}
