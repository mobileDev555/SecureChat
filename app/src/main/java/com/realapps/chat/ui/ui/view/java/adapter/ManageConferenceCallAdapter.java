package com.realapps.chat.ui.ui.view.java.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.realapps.chat.R;

import java.util.ArrayList;
import java.util.List;


import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.ui.helper.PrefManager;
import com.realapps.chat.ui.ui.view.java.model.ConferenceContactPojo;
import com.realapps.chat.utils.CommonUtils;

/**
 * Created by inextrix on 3/4/18.
 */

public class ManageConferenceCallAdapter extends RecyclerView.Adapter<ManageConferenceCallAdapter.MyViewHolder>{

    private List<ConferenceContactPojo> confCalllist;
    ArrayList<String> contactList;
    static List<ContactEntity> conferenceContactList;
    DbHelper db;
    Context ctx;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvname, tvnumber,tvNameDrawable;
        ImageView hangup;

        public MyViewHolder(View view) {
            super(view);
            tvname = (TextView) view.findViewById(R.id.cont_name);
            tvnumber = (TextView) view.findViewById(R.id.cont_num);
            tvNameDrawable = (TextView) view.findViewById(R.id.txt_user_iconc);
            hangup = (ImageView)view.findViewById(R.id.hangup);
        }
    }


    public ManageConferenceCallAdapter(ArrayList<String> contactList, Context ctx) {
        this.contactList = contactList;
        this.ctx = ctx;
        db = new DbHelper(ctx);
        conferenceContactList = new ArrayList<>();

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.conference_contact_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {


        conferenceContactList = db.getContactList();
        String ecc_id = contactList.get(position).toUpperCase();

        String ecc_name;
        ContactEntity entity;

        PrefManager prefManager=new PrefManager(ctx);
        String userScreenName= prefManager.getScreenName();
        String userEccId= prefManager.getEccId();

        for (int i = 0; i < conferenceContactList.size(); i++) {
            entity = conferenceContactList.get(i);

            if(ecc_id.toLowerCase().equals(userEccId.toLowerCase())){
                holder.tvname.setText(userScreenName);
                holder.tvnumber.setText(ecc_id);

            }else if (entity.getEccId().toLowerCase().equals(ecc_id.toLowerCase())) {
                /*System.out.println("ManageConferenceCallAdapter conferenceList "+ ecc_id.toLowerCase());
                System.out.println("ManageConferenceCallAdapter conferenceList 1 "+ entity.getEccId());*/

                ecc_name = CommonUtils.getContactName(ctx,entity.getEccId());


                holder.tvname.setText(ecc_name);
                holder.tvnumber.setText(ecc_id);

            }

        }


        /*if(ecc_id.toLowerCase().equals(userEccId)){
            holder.tvname.setText(userScreenName);
        }else{

        }*/


        Character firstLatterOfName = holder.tvname.getText().toString().toUpperCase().charAt(0);
        holder.tvNameDrawable.setText(firstLatterOfName.toString());
        holder.hangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("ManageConferenceCallAdapter hangup ");
                //dispatchTriggerEvent(IOnCallActionTrigger.TERMINATE_CALL);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }
}
