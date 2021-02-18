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
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.realapps.chat.R;
import com.realapps.chat.model.ContactEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prashant Sharma on 16/11/17.
 * Core techies
 * prashant@coretechies.org
 */

public class CreateGroupAdapter extends RecyclerSwipeAdapter<CreateGroupAdapter.MyViewHolder>
        implements Filterable {
    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    public static boolean DELETE_MODE = false;
    public static boolean checkLists[];
    private static List<ContactEntity> contactFilteredList;
    private Context mContext;
    private List<ContactEntity> conntactlist;
    private onItemClickListner listener;
    public CreateGroupAdapter(Context mContext, List<ContactEntity> conntactlist, onItemClickListner listener) {
        this.mContext = mContext;
        this.listener = listener;
        this.conntactlist = conntactlist;
        contactFilteredList = conntactlist;
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
                        .inflate(R.layout.create_group_list_item, parent, false);
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
            final ContactEntity contactEntity = conntactlist.get(position);

            if (holder.txtTitle != null)
                if (!contactEntity.getName().isEmpty() && contactEntity.getName().length() > 0) {
                    if (contactEntity.getName().contains(" ")) {
                        int lastIndex = contactEntity.getName().trim().lastIndexOf(" ");
                        String lastIntial = (String.valueOf(contactEntity.getName().toCharArray()[lastIndex + 1]).toUpperCase());
                        holder.txtTitle.setText(String.valueOf(contactEntity.getName().toCharArray()[0]).toUpperCase() + lastIntial);
                    } else
                        holder.txtTitle.setText(String.valueOf(contactEntity.getName().toCharArray()[0]).toUpperCase());
                }
            if (holder.txtName != null)
                holder.txtName.setText(contactEntity.getName());


            // on item click Listener
            int pos = position;
            if (holder.lyrParent != null)

                holder.imgRemove.setOnClickListener(view -> listener.onItemRemove(contactEntity, position));

        }

    }

    @Override
    public int getItemViewType(int position) {
        if (conntactlist != null && conntactlist.size() > 0) {
            return VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_EMPTY;
        }
    }

    @Override
    public int getItemCount() {
        if (conntactlist != null && conntactlist.size() > 0) {
            return conntactlist.size();
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
                    contactFilteredList = conntactlist;
                } else {
                    List<ContactEntity> filteredList = new ArrayList<>();
                    for (ContactEntity row : contactFilteredList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getEccId().toLowerCase().contains(charString.toLowerCase()) || row.getName().contains(charSequence)) {
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

        void onItemRemove(ContactEntity contactEntity, int position);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        TextView txtName;
        LinearLayout lyrParent;
        CheckBox checkbox;
        SwipeLayout swipe;
        ImageView imgRemove;

        MyViewHolder(View view) {
            super(view);
            txtTitle = view.findViewById(R.id.txt_title);
            txtName = view.findViewById(R.id.txt_name);
            lyrParent = view.findViewById(R.id.lyr_parent);
            checkbox = view.findViewById(R.id.checkbox);
            swipe = view.findViewById(R.id.swipe);
            imgRemove = view.findViewById(R.id.img_remove);

        }
    }


}