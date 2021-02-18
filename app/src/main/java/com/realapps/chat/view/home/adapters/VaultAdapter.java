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
import com.daimajia.swipe.SwipeLayout;
import com.realapps.chat.R;
import com.realapps.chat.model.VaultEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DateTimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prashant Sharma on 16/11/17.
 * Core techies
 * prashant@coretechies.org
 */

public class VaultAdapter extends RecyclerView.Adapter<VaultAdapter.MyViewHolder>
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


    public static int openLayoutPosition = -1;
    public static RecyclerView recyclerView;

    public VaultAdapter(Context mContext, List<VaultEntity> vaultList, onItemClickListener listener, RecyclerView recyclerView) {
        this.mContext = mContext;
        this.listener = listener;
        this.vaultList = vaultList;
        this.recyclerView = recyclerView;
        vaultFilteredList = vaultList;
        checkLists = new boolean[vaultFilteredList.size()];
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case VIEW_TYPE_CHAT:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.vault_chat_list_item, parent, false);
                break;
            case VIEW_TYPE_NOTES:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.vault_notes_list_item, parent, false);
                break;
            case VIEW_TYPE_PICTURE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.vault_picture_list_item, parent, false);
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

            //check box
            if (checkLists[position]) {
                holder.checkbox.setChecked(true);
            } else {
                holder.checkbox.setChecked(false);
            }

            holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> VaultAdapter.checkLists[position] = isChecked);

            switch (getItemViewType(position)) {
                case VIEW_TYPE_CHAT:
                    if (VaultEntity.getChatType() == 0) { //Single Chat Messages
                        holder.txt_ecc_id.setVisibility(View.VISIBLE);
                        if (!CommonUtils.getContactName(mContext, VaultEntity.getEccId()).isEmpty() && CommonUtils.getContactName(mContext, VaultEntity.getEccId()).length() > 0)
                            if (CommonUtils.getContactName(mContext, VaultEntity.getEccId()).trim().contains(" ")) {
                                int lastIndex = CommonUtils.getContactName(mContext, VaultEntity.getEccId()).lastIndexOf(" ");
                                String lastIntial = (String.valueOf(CommonUtils.getContactName(mContext, VaultEntity.getEccId()).toCharArray()[lastIndex + 1]).toUpperCase());
                                holder.txtTitle.setText(String.valueOf(CommonUtils.getContactName(mContext, VaultEntity.getEccId()).toCharArray()[0]).toUpperCase() + lastIntial);
                            } else
                                holder.txtTitle.setText(String.valueOf(CommonUtils.getContactName(mContext, VaultEntity.getEccId()).toCharArray()[0]).toUpperCase());
                        holder.txtName.setText(CommonUtils.getContactName(mContext, VaultEntity.getEccId()));
                        holder.txt_ecc_id.setText(VaultEntity.getEccId());
                    } else { // Group Chat Messages
                        holder.txtTitle.setText(String.valueOf(VaultEntity.getName().toCharArray()[0]).toUpperCase());
                        holder.txtName.setText(VaultEntity.getName().substring(0, 1).toUpperCase() + VaultEntity.getName().substring(1));
                        holder.txt_ecc_id.setVisibility(View.GONE);
                    }

                    holder.txt_time.setText(DateTimeUtils.getSimplifiedDateTime(VaultEntity.getDateTimeStamp()));
                    break;
                case VIEW_TYPE_NOTES:
                    holder.txt_ecc_id.setVisibility(View.GONE);
                    holder.txtName.setText(VaultEntity.getName());
                    holder.txt_time.setText(DateTimeUtils.getSimplifiedDateTime(VaultEntity.getDateTimeStamp()));
                    break;
                case VIEW_TYPE_PICTURE:
                    if (VaultEntity.getName().contains(".jpg"))
                        holder.txtName.setText(VaultEntity.getName().replace(".jpg", ""));
                    else
                        holder.txtName.setText(VaultEntity.getName());

                    holder.txt_ecc_id.setVisibility(View.GONE);

                    holder.txt_time.setText(DateTimeUtils.getSimplifiedDateTime(VaultEntity.getDateTimeStamp()));

                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
                    Glide.with(mContext).load(new File(VaultEntity.getImage())).apply(requestOptions).into(holder.image);
                    break;

            }
            // on item click Listener
            holder.lyrParent.setOnClickListener(view -> listener.onItemClick(VaultEntity, position, holder.image));

            holder.lyrDelete.setOnClickListener(view -> listener.onDelete(VaultEntity, position));

            holder.lyrRename.setOnClickListener(view -> listener.onRename(VaultEntity, position));

            holder.lyrShare.setOnClickListener(view -> listener.onShare(VaultEntity, position));

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
        void onItemClick(VaultEntity contactEntity, int position, ImageView imageView);

        void onItemLongPress(VaultEntity contactEntity, int position);

        void onDelete(VaultEntity contactEntity, int position);

        void onRename(VaultEntity vaultEntity, int position);

        void onShare(VaultEntity vaultEntity, int position);


    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private static final String TAG = "MyViewHolder";
        TextView txtTitle, txt_ecc_id, txt_time;
        TextView txtName;
        LinearLayout lyrParent;
        LinearLayout lyrDelete;
        LinearLayout lyrRename, lyrShare;
        CheckBox checkbox;
        ImageView image;

        public SwipeLayout mSwipeLayout;

        MyViewHolder(View view) {
            super(view);
            txt_ecc_id = view.findViewById(R.id.txt_ecc_id);
            txt_time = view.findViewById(R.id.txt_time);
            txtTitle = view.findViewById(R.id.txt_title);
            lyrDelete = view.findViewById(R.id.lyr_delete);
            lyrRename = view.findViewById(R.id.lyr_rename);
            txtName = view.findViewById(R.id.txt_name);
            lyrParent = view.findViewById(R.id.lyr_parent);
            checkbox = view.findViewById(R.id.checkbox);
            lyrShare = view.findViewById(R.id.lyr_share);
            image = view.findViewById(R.id.image);
            mSwipeLayout = view.findViewById(R.id.swipe);

            mSwipeLayout.setClickToClose(true);
            mSwipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

            mSwipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    if (openLayoutPosition != -1) {
                        VaultAdapter.MyViewHolder viewHolder = (VaultAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(openLayoutPosition);
                        if (viewHolder != null && viewHolder.mSwipeLayout != null)
                            viewHolder.mSwipeLayout.close(true);
                        openLayoutPosition = getAdapterPosition();
                    } else {
                        openLayoutPosition = getAdapterPosition();
                    }
                }

                @Override
                public void onStartClose(SwipeLayout layout) {

                }

                @Override
                public void onClose(SwipeLayout layout) {
                    if (openLayoutPosition == getAdapterPosition())
                        openLayoutPosition = -1;
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                }
            });

        }
    }
}
