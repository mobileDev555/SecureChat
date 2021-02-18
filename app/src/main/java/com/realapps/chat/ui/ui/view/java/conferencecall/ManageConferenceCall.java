package com.realapps.chat.ui.ui.view.java.conferencecall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.helper.PrefManager;
import com.realapps.chat.ui.ui.view.java.adapter.ManageConferenceCallAdapter;

import java.util.ArrayList;


/**
 * Created by inextrix on 3/4/18.
 */

public class ManageConferenceCall extends AppCompatActivity {

    public Toolbar tool;
    public TextView tv;
    public RecyclerView recyclerConf;
    private ManageConferenceCallAdapter mAdapter;
    public String conferenceList;
    public ArrayList<String> conferenceListArray;
    int hangupCallId;

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sp = getSharedPreferences("OURINFO", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("manageConferenceActive", true);
        ed.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sp = getSharedPreferences("OURINFO", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("manageConferenceActive", false);
        ed.commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.conference_call_contact_list);

        tool = (Toolbar) findViewById(R.id.toolbar);
        tv = (TextView) findViewById(R.id.toolbar_text);
        setSupportActionBar(tool);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.backarrow);

        tv.setText("Manage Conference Call");
        tv.setTextColor(getResources().getColor(R.color.white));

        tool.setNavigationIcon(getResources().getDrawable(R.drawable.md_nav_back));
        tool.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
                //What to do on back clicked
            }
        });

        IntentFilter iFilter = new IntentFilter(SipManager.ACTION_MANAGE_CONFERENCECALL_STOP);
        registerReceiver(mStopCurrentActivity, iFilter);

        PrefManager pref = new PrefManager(this);
        String UserEccId = pref.getEccId();
        conferenceListArray = new ArrayList<>();
        Bundle b = getIntent().getExtras();
        if (b != null) {
            hangupCallId = b.getInt("callId");
            conferenceList = b.getString("contactArray");
        }

//        System.out.println("InCall Activity : ManageConferenceCall new clicked " + conferenceList);

        /*String num = pref.getCallingNum();
        if(num != null){
            //newNumber = num;
            conferenceListArray.add(num);
        }*/
        if (conferenceList != null) {
            Gson gson = new Gson();
            String[] text = gson.fromJson(conferenceList, String[].class);
            for (int i = 0; i < text.length; i++) {
                String newNumber = text[i];

                conferenceListArray.add(newNumber);
            }
            if (UserEccId != null) {
                conferenceListArray.add(UserEccId);
            }
        }

        recyclerConf = (RecyclerView) findViewById(R.id.recyclerConference);

        //if(conferenceList == null) {
        mAdapter = new ManageConferenceCallAdapter(conferenceListArray, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerConf.setLayoutManager(mLayoutManager);
        recyclerConf.setItemAnimator(new DefaultItemAnimator());
        recyclerConf.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerConf.setAdapter(mAdapter);
        //}
    }

    private BroadcastReceiver mStopCurrentActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(SipManager.ACTION_MANAGE_CONFERENCECALL_STOP)) {
                finish();
            } else if (intent.getAction().equals(SipManager.ACTION_MANAGE_CONFERENCECALL_ADD_CONTACT)) {

                String newConferenceMember = intent.getStringExtra("new_added_contact");

            }

        }
    };
}
