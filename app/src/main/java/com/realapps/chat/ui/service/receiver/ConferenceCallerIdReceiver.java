package com.realapps.chat.ui.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import com.realapps.chat.ui.api.SipManager;

public class ConferenceCallerIdReceiver extends BroadcastReceiver {

	public interface IConferenceCallerID {
		void sendCallerID(ArrayList<Integer> sendCallerIdArray);
	}

	private static IConferenceCallerID iConferenceCallerID;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().equals(SipManager.ACTION_FOR_CONFERENCE_CALLERID)){

			ArrayList<Integer> caller_id_list = intent.getIntegerArrayListExtra("caller_id_array");
			System.out.println("ConferenceCallerIdReceiver action "+ intent.getAction());
			System.out.println("ConferenceCallerIdReceiver caller_id_list "+ caller_id_list.size());

			iConferenceCallerID.sendCallerID(caller_id_list);
		}
	}

    public void registerCallback(IConferenceCallerID iConferenceCallerID) {
        this.iConferenceCallerID = iConferenceCallerID;
    }
}