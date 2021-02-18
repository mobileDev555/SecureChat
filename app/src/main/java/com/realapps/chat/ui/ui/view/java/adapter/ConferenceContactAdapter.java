/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * This file contains relicensed code from som Apache copyright of
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 */

package com.realapps.chat.ui.ui.view.java.adapter;

import com.realapps.chat.R;
import com.realapps.chat.model.EccContactConferenceList;
import com.realapps.chat.ui.api.SipProfile;
import com.realapps.chat.ui.helper.PrefManager;
import com.realapps.chat.ui.ui.view.java.AddContactForConference;
import com.realapps.chat.ui.ui.view.kotlin.model.URLCollection;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * This adapter is used to filter contacts on both name and number.
 */
public class ConferenceContactAdapter extends ArrayAdapter implements SectionIndexer {

    private final Context mContext;
    private long currentAccId = SipProfile.INVALID_ID;
    AlphabetIndexer alphaIndexer;
    private String currentFilter = "";
    private CharacterStyle boldStyle = new StyleSpan(android.graphics.Typeface.BOLD);
    private CharacterStyle highlightStyle = new ForegroundColorSpan(0xFF33B5E5);
    private List<EccContactConferenceList> contactList;
    EccContactConferenceList entity;
    private ValueFilter valueFilter;
    boolean clickStatus = true;
    View.OnClickListener onClickListener;
    Activity activity;
    PrefManager pref;
    private SparseBooleanArray mSelectedItemsIds;
    ArrayList<String> selected_data;
    ArrayList<String> selected_data1;
    ArrayList<String> arrayList1;
    boolean layerChecked = false;


    public ConferenceContactAdapter(Context context, List<EccContactConferenceList> contactList, int resource, AddContactForConference onClickListener, Activity activity) {
        super(context, resource);
        mContext=context;
        this.contactList = contactList;
        this.onClickListener = onClickListener;
        this.activity = activity;
        mSelectedItemsIds = new  SparseBooleanArray();
        selected_data = new ArrayList<String>();
        selected_data1 = new ArrayList<String>();
        arrayList1 = new ArrayList<String>();
    }


    public final void setSelectedAccount(long accId) {
        currentAccId = accId;
    }
    public final void setSelectedText(String txt) {
        if(!TextUtils.isEmpty(txt)) {
            currentFilter  = txt.toLowerCase();
        }else {
            currentFilter = "";
        }
        if (TextUtils.isEmpty(txt) || txt.length() >= 2) {
            getFilter().filter(txt);
        }else {
            currentFilter = "";
        }
    }

    public void  toggleSelection(int position) {

        selectView(position, !mSelectedItemsIds.get(position));

    }

    // Item checked on selection

    public void selectView(int position, boolean value) {

        if (value)

            mSelectedItemsIds.put(position,  value);

        else

            mSelectedItemsIds.delete(position);

        notifyDataSetChanged();

    }

   /* @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.search_contact_list_item, parent, false);
    }*/

    private boolean highlightTextViewSearch(TextView tv) {
        if(currentFilter.length() > 0) {
            String value = tv.getText().toString();
            int foundIdx = value.toLowerCase().indexOf(currentFilter);
            if(foundIdx >= 0) {
                SpannableString spn = new SpannableString(value);
                spn.setSpan(boldStyle, foundIdx, foundIdx + currentFilter.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spn.setSpan(highlightStyle, foundIdx, foundIdx + currentFilter.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.setText(spn);
                return true;
            }
        }
        return false;
    }


    @Override
    public int getPositionForSection(int section) {
        if(alphaIndexer != null) {
            try {
                return alphaIndexer.getPositionForSection(section);
            }catch(CursorIndexOutOfBoundsException e) {
                // Not a problem we are just not yet init
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        if(alphaIndexer != null) {
            try {
                return alphaIndexer.getSectionForPosition(position);
            }catch(CursorIndexOutOfBoundsException e) {
                // Not a problem we are just not yet init
            }
        }
        return 0;
    }

    @Override
    public Object[] getSections() {
        if(alphaIndexer != null) {
            return alphaIndexer.getSections();
        }
        return null;
    }


    @Override
    public int getCount() {
        return this.contactList.size();
    }


    @Override
    public Object getItem(int position) {
        return this.contactList.get(position);
    }


    @Override
    public int getPosition(Object item) {
        return super.getPosition(item);
    }

    static class ImgHolder {
        public TextView name, number,currency1, cost,currency2 ,image_path,rate;
        public ImageView countryflag;

        public LinearLayout row_head,lyr_contact_view_2,lyr_check ;
        String number_str;
        CheckBox check;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row;
        row = convertView;
        final ImgHolder holder;

        final EccContactConferenceList la = (EccContactConferenceList) getItem(position);
        //  System.out.println("Data List:::::"+ Arrays.toString(la));
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.contact_list_for_conference, parent, false);
            holder = new ImgHolder();

            holder.name = (TextView) row.findViewById(R.id.name);
            holder.number = (TextView) row.findViewById(R.id.number);
            holder.row_head = (LinearLayout)row.findViewById(R.id.lyr_head);
            holder.check = (CheckBox)row.findViewById(R.id.chk_contact);
            holder.lyr_check = (LinearLayout)row.findViewById(R.id.lyr_check);
            holder.lyr_contact_view_2 = (LinearLayout)row.findViewById(R.id.lyr_contact_view);
            //zvz76,bom93,edq01

            row.setTag(holder);
        } else {
            holder = (ImgHolder) row.getTag();
        }

        if (la.getScreenName() != null) {
            ((TextView) row.findViewById(R.id.txt_user_iconc)).setText(String.valueOf(la.getScreenName().charAt(0)).toUpperCase());
        } else {
            ((TextView) row.findViewById(R.id.txt_user_iconc)).setText(String.valueOf(la.getEccId().charAt(0)).toUpperCase());
        }




        holder.name.setText(la.getScreenName());
        holder.number.setText(la.getEccId().toUpperCase().toString());
        holder.number_str= holder.number.getText().toString();
        holder.check.setClickable(true);
        pref = new PrefManager(activity);
        /*String conferenceNum = pref.getAddedContacts(activity);

        if(conferenceNum != null) {
                Gson gson = new Gson();
                String[] text = gson.fromJson(conferenceNum, String[].class);

                for (int j = 0; j < text.length; j++) {
                    final String num = text[j];

                    holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                selected_data.add(la.getEccId());
                                selected_data1.add(la.getEccId());
                                selected_data1.add(num);

                                pref.setConferenceContact(getContext(),selected_data);
                                pref.addContact(activity,selected_data1);

                            } else {
                                if (selected_data.contains(la.getEccId())) {
                                    selected_data.remove(la.getEccId());
                                    selected_data.remove(num);

                                    selected_data1.remove(la.getEccId());
                                    selected_data1.remove(num);

                                }

                                pref.setConferenceContact(getContext(),selected_data);
                                pref.addContact(activity,selected_data);
                            }

                        }
                    });

                }
        }else{
            holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selected_data.add(la.getEccId());

                        pref.setConferenceContact(getContext(),selected_data);
                        //Collections.reverse(arrayList1);
                        pref.addContact(activity,selected_data);

                    } else {
                        if (selected_data.contains(la.getEccId())) {
                            selected_data.remove(la.getEccId());
                        }

                        pref.setConferenceContact(getContext(),selected_data);
                        pref.addContact(activity,selected_data);
                    }

                }
            });
        }*/



        holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selected_data.add(la.getEccId());

                    pref.setConferenceContact(getContext(),selected_data);
                    //Collections.reverse(arrayList1);
                    pref.addContact(activity,selected_data);
                    layerChecked = true;


                } else {
                    if (selected_data.contains(la.getEccId())) {
                        selected_data.remove(la.getEccId());
                    }

                    pref.setConferenceContact(getContext(),selected_data);
                    pref.addContact(activity,selected_data);
                    layerChecked = false;

                }


            }
        });



        holder.row_head.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                holder.lyr_check.setVisibility(View.VISIBLE);
                holder.check.setChecked(true);
              //  holder.lyr_contact_view_2.setBackgroundResource(R.color.row_selected_bg);//setBackgroundColor(mContext.getResources().getColor(R.color.in_call_bg_transparent_dark));

                return true;
            }
        });

        holder.row_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.lyr_check.setVisibility(View.VISIBLE);

                if(holder.check.isChecked()) {
                    holder.check.setChecked(false);
                }else{
                    holder.check.setChecked(true);
                }

              /*  if(holder.check.isChecked()){
                    holder.lyr_contact_view_2.setBackgroundResource(R.color.row_selected_bg);
                }else{
                    holder.lyr_contact_view_2.setBackgroundResource(R.color.user_detail_bg);
                }*/

            }
        });

        return row;



    }

    public Filter getFilter() {

        if (valueFilter == null) {

            valueFilter = new ValueFilter(contactList, this);
        }

        return valueFilter;
    }

    public class ValueFilter extends Filter {

        ConferenceContactAdapter adapter;
        private List<EccContactConferenceList> mfilterList;

        public ValueFilter(List<EccContactConferenceList> filterList, ConferenceContactAdapter adapter) {
            this.adapter = adapter;
            this.mfilterList = filterList;
        }

        //Invoked in a worker thread to filter the data according to the constraint.
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {


                int count = mfilterList.size();
                final ArrayList<EccContactConferenceList> nlist = new ArrayList<EccContactConferenceList>();


                for (int i = 0; i < mfilterList.size(); i++) {

                    String name = (String) mfilterList.get(i).getScreenName();
                    String ecc_id = (String) mfilterList.get(i).getEccId();

                    name = name.toLowerCase();
                    constraint = (CharSequence) constraint.toString().toLowerCase();
                    if (name.contains(constraint)) {
                        nlist.add(mfilterList.get(i));

                    }
                    results.count = nlist.size();
                    results.values = nlist;

                }

            } else {

                results.count = mfilterList.size();
                results.values = mfilterList;

            }

            return results;


        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.contactList = (ArrayList<EccContactConferenceList>) results.values;
            notifyDataSetChanged();
        }

    }


    private void sendPositiveResult(String number) {
       // Intent resultValue = new Intent();


        ToCall result = get_value(number);

        if(result != null) {
            // Restore existing extras.
            Intent it = activity.getIntent();
            Intent resultIntent = new Intent();

            if(it != null) {
                Bundle b = it.getExtras();
                if(b != null) {
                    resultIntent.putExtras(b);
                }
            }
            resultIntent.putExtra(Intent.EXTRA_PHONE_NUMBER,
                    result.getCallee());
            resultIntent.putExtra(SipProfile.FIELD_ID,
                    result.getAccountId());
            activity.setResult(Activity.RESULT_OK, resultIntent);
        }else {
            activity.setResult(Activity.RESULT_CANCELED);
        }
        activity.finish();
    }

    public ToCall get_value(String number){
        String toCall="";
        Long accountToUse = null;
        Cursor c = mContext.getContentResolver().query(SipProfile.ACCOUNT_URI, null, null, null, null);
        c.moveToFirst();

        SipProfile account= new SipProfile(c);

        if (account == null) {
            return null;
        }
        if (account != null) {
            accountToUse = account.id;
            if (accountToUse > SipProfile.INVALID_ID) {
                if (Pattern.matches(".*@.*", number)) {
                    toCall = "sip:" + number + "";
                } else if (!TextUtils.isEmpty(URLCollection.obj.SERVER_SIP_IP)) {
                    toCall = "sip:" + number + "@" + URLCollection.obj.SERVER_SIP_IP;
                } else {
                    toCall = "sip:" + number;
                }
            }else {
                toCall = number;
            }
        }else {
            toCall = number;
        }
        return new ToCall(accountToUse, toCall);

    }

    public class ToCall {
        private Long accountId;
        private String callee;

        public ToCall(Long acc, String uri) {
            accountId = acc;
            callee = uri;
        }

        /**
         * @return the pjsipAccountId
         */
        public Long getAccountId() {
            return accountId;
        }

        /**
         * @return the callee
         */
        public String getCallee() {
            return callee;
        }
    };
}

