package com.realapps.chat.ui.service.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;


import com.realapps.chat.ui.api.GlobalClass;
import com.realapps.chat.ui.api.SipCallSession;
import com.realapps.chat.ui.api.SipManager;
import com.realapps.chat.ui.api.SipProfileState;
import com.realapps.chat.view.home.activity.HomeActivity;

import java.util.ArrayList;


public class SipStatusReceiver extends BroadcastReceiver {


    String ACC_STATUS_YES = "YES";
    String ACC_STATUS_NO = "NO";
    String THIS_FILE = SipStatusReceiver.class.toString();
    GlobalClass gc = GlobalClass.getInstance();
    Activity activity;
    Context contexted;
    HomeActivity main = new HomeActivity();
    HomeActivity main1 = null;
    public SipStatusReceiver() {

    }

    public SipStatusReceiver(Context ctx, Activity maContext) {
        this.contexted = ctx;
        this.activity = maContext;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub


        if (intent.getAction() == SipManager.ACTION_SIP_ACCOUNT_STATUS) {
            int statusCode=0;
            //  gc=GlobalClass.getInstance();
            if (intent.getAction() == SipManager.ACTION_SIP_ACCOUNT_STATUS) {
               String  st = intent.getExtras().getString("status");

                if (st.equals(ACC_STATUS_YES)) {


                    ArrayList<SipProfileState> activeProfilesState = intent.getExtras().getParcelableArrayList("message");
                    if (activeProfilesState.size() > 0) {
                        for (SipProfileState accountInfo : activeProfilesState) {
                            if (accountInfo != null && accountInfo.isActive()) {
                                if (accountInfo.getAddedStatus() >= SipManager.SUCCESS) {


                                    if (TextUtils.isEmpty(accountInfo.getRegUri())) {
                                        // Green
                                        System.out.println(THIS_FILE + "  Account is Registered");

                                    } else if (accountInfo.isAddedToStack()) {
                                        String pjStat = accountInfo.getStatusText();    // Used only on error status message
                                        statusCode = accountInfo.getStatusCode();
                                        statusCode = accountInfo.getStatusCode();
                                        if (statusCode == SipCallSession.StatusCode.OK) {
                                            if (accountInfo.getExpires() > 0) {
                                                // Green
                                                System.out.println(THIS_FILE + "  Account is Registered");
                                            } else {
                                                System.out.println(THIS_FILE + "  Account is unregistered");
                                            }

                                        } else if (statusCode != -1) {
                                            if (statusCode == SipCallSession.StatusCode.PROGRESS || statusCode == SipCallSession.StatusCode.TRYING) {
                                               // System.out.println(THIS_FILE + "  Account is trying to register");

                                            } else {
                                                //TODO : treat 403 with special message
                                                // Red : error
                                             //   System.out.println(THIS_FILE + "  Account error");
                                            }
                                        } else {
                                           // System.out.println(THIS_FILE + context.getResources().getString(R.string.sip_failuremsgconnection));
                                            //main.showSnackbar(main1, R.string.sip_failuremsgconnection, R.string.close, null);
                                        }
                                    }
                                }
                            }
                            /*if (!accountInfo.isActive()) {
                                main.showSnackbar(main1, R.string.sip_failuremsgconnection, R.string.close, null);
                            } else {
                                main.showSnackbar(main1, R.string.sip_successmsgconnection, R.string.open, null);

                            }*/
                        }

                    } else {
                        //main.showSnackbar(main1, R.string.sip_failuremsgconnection, R.string.close, null);
                    }
                    gc.setAccountStatus(st);
                    gc.setAccountStatusCode(statusCode);
                    Intent intent1 = new Intent();
                    intent1.setAction(SipManager.ACTION_SIP_bROADCAST);
                    context.sendBroadcast(intent1);

                }   else{

                    gc.setAccountStatus(st);
                    gc.setAccountStatusCode(statusCode);
                    //main.showSnackbar(main1, R.string.sip_failuremsgconnection, R.string.close, null);
                    Intent intent1 = new Intent();
                    intent1.setAction(SipManager.ACTION_SIP_bROADCAST);
                    context.sendBroadcast(intent1);

            }

            }
        }
    }


    public void setMainActivityHandler(HomeActivity main){
        this.main1=main;
    }

}