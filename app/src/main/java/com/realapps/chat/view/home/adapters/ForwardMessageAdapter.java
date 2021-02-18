package com.realapps.chat.view.home.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.realapps.chat.R;
import com.realapps.chat.model.ContactEntity;
import com.realapps.chat.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prashant Sharma on 16/11/17.
 * Core techies
 * prashant@coretechies.org
 */

public class ForwardMessageAdapter extends RecyclerSwipeAdapter<ForwardMessageAdapter.MyViewHolder>
        implements Filterable {
    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    public static boolean checkLists[];
    private static List<ContactEntity> contactFilteredList;
    private Context mContext;
    private onItemClickListner listener;
    private List<ContactEntity> contactlist;

    public ForwardMessageAdapter(Context mContext, List<ContactEntity> contactlist, onItemClickListner listener) {
        this.mContext = mContext;
        this.contactlist = contactlist;
        contactFilteredList = contactlist;
        this.listener = listener;
        checkLists = new boolean[contactFilteredList.size()];
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.forward_contact_list_item, parent, false);
                break;
            case VIEW_TYPE_EMPTY:
            default:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.empty_chat_list_layout, parent, false);
                break;
        }

        return new MyViewHolder(itemView);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        if (getItemViewType(position) == VIEW_TYPE_NORMAL) {
            final ContactEntity contactEntity = contactFilteredList.get(position);

            if (contactEntity.getIsGroup() == AppConstants.GROUP_CHAT_TYPE) {
                holder.imgGroup.setVisibility(View.VISIBLE);
                holder.group_bagde.setVisibility(View.VISIBLE);
                holder.single_bagde.setVisibility(View.GONE);
            } else {
                holder.imgGroup.setVisibility(View.GONE);
                holder.group_bagde.setVisibility(View.GONE);
                holder.single_bagde.setVisibility(View.VISIBLE);
                if (!contactEntity.getName().isEmpty() && contactEntity.getName().length() > 0) {
                    if (contactEntity.getName().contains(" ")) {
                        int lastIndex = contactEntity.getName().trim().lastIndexOf(" ");
                        String lastIntial = (String.valueOf(contactEntity.getName().toCharArray()[lastIndex + 1]).toUpperCase());
                        holder.txtTitle.setText(String.valueOf(contactEntity.getName().toCharArray()[0]).toUpperCase() + lastIntial);
                    } else
                        holder.txtTitle.setText(String.valueOf(contactEntity.getName().toCharArray()[0]).toUpperCase());
                }
            }
          holder.txtName.setText(contactEntity.getName());
            holder.checkbox.setEnabled(false);

            if (checkLists[position]) {
                holder.checkbox.setChecked(true);

            } else {
                holder.checkbox.setChecked(false);
            }
            // on item click Listener
            holder.lyrParent.setOnClickListener(view -> listener.onItemClick(contactEntity, position));


        }

    }

    @Override
    public int getItemViewType(int position) {
        if (contactFilteredList != null && contactFilteredList.size() > 0) {
            return VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_EMPTY;
        }
    }

    @Override
    public int getItemCount() {
        if (contactFilteredList != null && contactFilteredList.size() > 0) {
            return contactFilteredList.size();
        } else {
            return 1;
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    contactFilteredList = contactlist;
                } else {
                    List<ContactEntity> filteredList = new ArrayList<>();
                    for (ContactEntity row : contactlist) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getEccId().toLowerCase().contains(charString.toLowerCase()) || row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    contactFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = contactFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contactFilteredList = (ArrayList<ContactEntity>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface onItemClickListner {
        void onItemClick(ContactEntity contactEntity, int position);

        void onItemLongPress(ContactEntity contactEntity, int position);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;
        TextView txtTitle;
        TextView txtName;
        LinearLayout lyrParent;
        ImageView imgGroup;
        RelativeLayout single_bagde,group_bagde;

        MyViewHolder(View view) {
            super(view);
            txtTitle = view.findViewById(R.id.txt_title);
            checkbox = view.findViewById(R.id.checkbox);
            txtName = view.findViewById(R.id.txt_name);
            lyrParent = view.findViewById(R.id.lyr_parent);
            imgGroup = view.findViewById(R.id.img_group);
            group_bagde = view.findViewById(R.id.group_bagde);
            single_bagde = view.findViewById(R.id.single_bagde);

        }
    }


}
