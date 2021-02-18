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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.realapps.chat.R;
import com.realapps.chat.model.VaultEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.DateTimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prashant Sharma on 16/11/17.
 * Core techies
 * prashant@coretechies.org
 */

public class ShareFromVaultAdapter extends RecyclerView.Adapter<ShareFromVaultAdapter.MyViewHolder>
        implements Filterable {
    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_NOTES = 1;
    private static final int VIEW_TYPE_PICTURE = 2;
    private static final int VIEW_TYPE_CHAT = 3;
    public static boolean DELETE_MODE = false;
    public static boolean checkLists[];
    private static List<VaultEntity> vaultFilteredList;
    private Context mContext;
    private List<VaultEntity> vaultList;
    private onItemClickListener listener;


    public ShareFromVaultAdapter(Context mContext, List<VaultEntity> vaultList, onItemClickListener listener) {
        this.mContext = mContext;
        this.listener = listener;
        this.vaultList = vaultList;
        vaultFilteredList = vaultList;
        checkLists = new boolean[vaultFilteredList.size()];
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case VIEW_TYPE_CHAT:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.share_vault_chat_list_item, parent, false);
                break;
            case VIEW_TYPE_NOTES:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.share_vault_notes_list_item, parent, false);
                break;
            case VIEW_TYPE_PICTURE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.share_vault_picture_list_item, parent, false);
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


        if (getItemViewType(position) != VIEW_TYPE_EMPTY) {

            final VaultEntity VaultEntity = vaultFilteredList.get(position);


            if (DELETE_MODE) {
                if (holder.checkbox.getVisibility() == View.GONE)
                    holder.checkbox.setVisibility(View.VISIBLE);
            } else {
                if (holder.checkbox.getVisibility() == View.VISIBLE)
                    holder.checkbox.setVisibility(View.GONE);
            }
            if (VaultEntity.getSelected()) {
                holder.checkbox.setChecked(true);
            } else {
                holder.checkbox.setChecked(false);
            }

            switch (getItemViewType(position)) {
                case VIEW_TYPE_CHAT:
                    if (!VaultEntity.getName().isEmpty() && VaultEntity.getName().length() > 0) {
                        if (VaultEntity.getName().contains(" ")) {
                            int lastIndex = VaultEntity.getName().trim().lastIndexOf(" ");
                            String lastIntial = (String.valueOf(VaultEntity.getName().toCharArray()[lastIndex + 1]).toUpperCase());
                            holder.txtTitle.setText(String.valueOf(VaultEntity.getName().toCharArray()[0]).toUpperCase() + lastIntial);
                        } else
                            holder.txtTitle.setText(String.valueOf(VaultEntity.getName().toCharArray()[0]).toUpperCase());
                    }
                    holder.txtName.setText(VaultEntity.getName());
                    holder.txt_ecc_id.setText(VaultEntity.getEccId());
                    holder.txt_time.setText(DateTimeUtils.getSimplifiedDateTime(VaultEntity.getDateTimeStamp()));
                    break;
                case VIEW_TYPE_NOTES:
                    holder.txt_ecc_id.setVisibility(View.GONE);
                    holder.txtName.setText(VaultEntity.getName());
                    holder.txt_time.setText(DateTimeUtils.getSimplifiedDateTime(VaultEntity.getDateTimeStamp()));
                    break;
                case VIEW_TYPE_PICTURE:
                    holder.txt_ecc_id.setVisibility(View.GONE);
                    holder.txtName.setText(VaultEntity.getName());
                    holder.txt_time.setText(DateTimeUtils.getSimplifiedDateTime(VaultEntity.getDateTimeStamp()));

                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
                    Glide.with(mContext).load(new File(VaultEntity.getImage())).apply(requestOptions).into(holder.mImageView);
                    break;


            }
            // on item click Listener
            holder.lyrParent.setOnClickListener(view -> listener.onItemClick(VaultEntity, position));


            // on Item Long Press

            holder.lyrParent.setOnLongClickListener(view -> {
                listener.onItemLongPress(VaultEntity, position);
                return false;
            });

        }


    }

    @Override
    public int getItemViewType(int position) {
        int viewType = 0;
        if (vaultFilteredList != null && vaultFilteredList.size() > 0) {
            if (vaultFilteredList.get(position).getMimeType() == AppConstants.ITEM_TYPE_NOTES)
                viewType = VIEW_TYPE_NOTES;
            else if (vaultFilteredList.get(position).getMimeType() == AppConstants.ITEM_TYPE_CHATS)
                viewType = VIEW_TYPE_CHAT;
            else if (vaultFilteredList.get(position).getMimeType() == AppConstants.ITEM_TYPE_PICTURE)
                viewType = VIEW_TYPE_PICTURE;
        } else {
            viewType = VIEW_TYPE_EMPTY;
        }
        return viewType;
    }

    @Override
    public int getItemCount() {
        if (vaultFilteredList != null && vaultFilteredList.size() > 0) {
            return vaultFilteredList.size();
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
                    vaultFilteredList = vaultList;
                } else {
                    List<VaultEntity> filteredList = new ArrayList<>();
                    for (VaultEntity row : vaultList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    vaultFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = vaultFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                vaultFilteredList = (ArrayList<VaultEntity>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    public interface onItemClickListener {
        void onItemClick(VaultEntity contactEntity, int position);

        void onItemLongPress(VaultEntity contactEntity, int position);

        void onDelete(VaultEntity contactEntity, int position);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle, txt_ecc_id, txt_time;
        TextView txtName;
        LinearLayout lyrParent;
        CheckBox checkbox;
        ImageView mImageView;


        MyViewHolder(View view) {
            super(view);
            txt_ecc_id = view.findViewById(R.id.txt_ecc_id);
            txt_time = view.findViewById(R.id.txt_time);
            txtTitle = view.findViewById(R.id.txt_title);
            txtName = view.findViewById(R.id.txt_name);
            lyrParent = view.findViewById(R.id.lyr_parent);
            checkbox = view.findViewById(R.id.checkbox);
            mImageView = view.findViewById(R.id.image);

        }
    }
}
