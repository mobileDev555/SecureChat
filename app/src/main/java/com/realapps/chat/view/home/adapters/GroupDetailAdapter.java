package com.realapps.chat.view.home.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.realapps.chat.R;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.GroupMemberEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;

import java.util.ArrayList;

/**
 * Created by Prashant Sharma on 16/11/17.
 * Core techies
 * prashant@coretechies.org
 */

public class GroupDetailAdapter extends RecyclerSwipeAdapter<GroupDetailAdapter.MyViewHolder> {
    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    public ArrayList<GroupMemberEntity> groupMemberList;
    private Context mContext;
    private OnItemListeners listeners;

    public GroupDetailAdapter(Context mContext, ArrayList<GroupMemberEntity> groupMemberList, OnItemListeners listeners) {
        this.mContext = mContext;
        this.groupMemberList = groupMemberList;
        this.listeners = listeners;
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
                        .inflate(R.layout.group_detail_list_item, parent, false);
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
            GroupMemberEntity groupMemberEntity = groupMemberList.get(position);

            holder.txtEccId.setText(groupMemberEntity.getEccId());
            holder.txtName.setText(CommonUtils.getContactName(mContext, groupMemberEntity.getEccId()));

            if (!groupMemberEntity.getName().isEmpty() && groupMemberEntity.getName().length() > 0) {
                if (groupMemberEntity.getName().contains(" ")) {
                    int lastIndex = groupMemberEntity.getName().trim().lastIndexOf(" ");
                    String lastIntial = (String.valueOf(groupMemberEntity.getName().toCharArray()[lastIndex + 1]).toUpperCase());
                    holder.txtTitle.setText(String.valueOf(groupMemberEntity.getName().toCharArray()[0]).toUpperCase() + lastIntial);
                } else
                    holder.txtTitle.setText(String.valueOf(groupMemberEntity.getName().toCharArray()[0]).toUpperCase());
            }
            if (groupMemberEntity.getMemberType() == AppConstants.GROUP_ADMIN) {
                holder.txtIsAdmin.setVisibility(View.VISIBLE);
                holder.img_options.setVisibility(View.GONE);
            } else {
                holder.txtIsAdmin.setVisibility(View.GONE);
                if (isImGroupAdmin())
                    holder.img_options.setVisibility(View.VISIBLE);
                else
                    holder.img_options.setVisibility(View.GONE);
            }

            holder.lyr_parent.setOnLongClickListener(view -> {
                listeners.onLongClickListeners(groupMemberEntity, position);
                return false;
            });

            holder.img_options.setOnClickListener(view -> {
                listeners.onClickMore(holder.img_options, groupMemberEntity, position);
            });
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (groupMemberList != null && groupMemberList.size() > 0) {
            return VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_EMPTY;
        }
    }

    @Override
    public int getItemCount() {
        if (groupMemberList != null && groupMemberList.size() > 0) {
            return groupMemberList.size();
        } else {
            return 1;
        }
    }

    public boolean isImGroupAdmin() {
        boolean isAdmin = false;
        for (GroupMemberEntity memberEntity : groupMemberList) {
            if (memberEntity.getMemberType() == AppConstants.GROUP_ADMIN && memberEntity.getEccId().equalsIgnoreCase(User_settings.getECCID(mContext)))
                isAdmin = true;
        }
        return isAdmin;
    }


    public interface OnItemListeners {

        void onLongClickListeners(GroupMemberEntity memberEntity, int position);

        void onClickMore(View view, GroupMemberEntity memberEntity, int position);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtName;
        TextView txtEccId;
        TextView txtIsAdmin;
        LinearLayout lyr_parent;
        ImageView img_options;

        MyViewHolder(View view) {
            super(view);
            txtTitle = view.findViewById(R.id.txt_title);
            txtName = view.findViewById(R.id.txt_name);
            txtEccId = view.findViewById(R.id.txt_ecc_id);
            txtIsAdmin = view.findViewById(R.id.txt_is_admin);
            lyr_parent = view.findViewById(R.id.lyr_parent);
            img_options = view.findViewById(R.id.img_options);
        }
    }
}
