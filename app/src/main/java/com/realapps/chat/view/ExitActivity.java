package com.realapps.chat.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Hari Choudhary on 3/26/2019 at 4:21 PM .
 * Core techies
 * hari@coretechies.org
 */
public class ExitActivity extends AppCompatActivity {

    public static void exitApplication(Context context) {
        Intent intent = new Intent(context, ExitActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        finishAffinity();
    }

}
