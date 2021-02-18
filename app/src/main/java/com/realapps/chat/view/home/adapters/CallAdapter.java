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
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prashant Sharma on 16/11/17.
 * Core techies
 * prashant@coretechies.org
 */

public class CallAdapter extends RecyclerSwipeAdapter<CallAdapter.MyViewHolder>
        implements Filterable {
    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    public static boolean DELETE_MODE = false;
    public static boolean checkLists[];
    private static List<ChatListEntity> chatFilteredList;
    private Context mContext;
    private List<ChatListEntity> chatList;
    private onItemClickListner listener;
    private DbHelper dbHelper;


    public CallAdapter(Context mContext, List<ChatListEntity> chatList, onItemClickListner listener) {
        this.mContext = mContext;
        this.listener = listener;
        this.chatList = chatList;
        dbHelper = new DbHelper(mContext);
        chatFilteredList = chatList;
        checkLists = new boolean[chatFilteredList.size()];
    }

    public void notifyItemRemove(int position) {
        chatFilteredList.remove(position);
        notifyItemRemoved(position);
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
                        .inflate(R.layout.call_list_item, parent, false);
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
            final ChatListEntity chatListEntity = chatFilteredList.get(position);

            if (chatListEntity.getChatType() == AppConstants.SINGLE_CHAT_TYPE) {

                holder.swipe.setShowMode(SwipeLayout.ShowMode.PullOut);

                if (DELETE_MODE) {
                    if (holder.checkbox.getVisibility() == View.GONE)
                        holder.checkbox.setVisibility(View.VISIBLE);
                } else {
                    if (holder.checkbox.getVisibility() == View.VISIBLE)
                        holder.checkbox.setVisibility(View.GONE);
                }

                if (chatListEntity.isSelected()) {
                    holder.checkbox.setChecked(true);
                } else {
                    holder.checkbox.setChecked(false);
                }


                holder.txtName.setText(CommonUtils.getContactName(mContext, chatListEntity.getEccId()));
               holder.txt_ecc_id.setText(mContext.getString(R.string.ecc_id_s, chatListEntity.getEccId().toUpperCase()));
                holder.txt_time.setText(String.valueOf(CommonUtils.getTime(chatListEntity.getMessageTimeStamp())));

                if (position == 0)
                    holder.call_icon.setImageResource(R.drawable.missed);
                else if (position == 1)
                    holder.call_icon.setImageResource(R.drawable.incoming);
                else
                    holder.call_icon.setImageResource(R.drawable.outgoing);


                // on item click Listener
                holder.lyrParent.setOnClickListener(view -> listener.onItemClick(chatListEntity, position));

                //on  delete Item
                holder.lyrDelete.setOnClickListener(view -> listener.onDeleteItem(chatListEntity, position));

                // on Item Long Press

                holder.lyrParent.setOnLongClickListener(view -> {
                    listener.onItemLongPress(chatListEntity, position);
                    return false;
                });

            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (chatFilteredList != null && chatFilteredList.size() > 0) {
            return VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_EMPTY;
        }
    }

    @Override
    public int getItemCount() {
        if (chatFilteredList != null && chatFilteredList.size() > 0) {
            return chatFilteredList.size();
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
                    chatFilteredList = chatList;
                } else {
                    List<ChatListEntity> filteredList = new ArrayList<>();
                    for (ChatListEntity row : chatList) {

                        if (row.getEccId().toLowerCase().contains(charString.toLowerCase()) || row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    chatFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = chatFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                chatFilteredList = (ArrayList<ChatListEntity>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    public interface onItemClickListner {
        void onItemClick(ChatListEntity contactEntity, int position);

        void onItemLongPress(ChatListEntity contactEntity, int position);

        void onDeleteItem(ChatListEntity contactEntity, int position);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtName, txt_ecc_id, txt_time;
        LinearLayout lyrDelete;
        LinearLayout lyrParent;
        CheckBox checkbox;
        SwipeLayout swipe;
        ImageView call_icon;


        MyViewHolder(View view) {
            super(view);
            txtName = view.findViewById(R.id.txt_name);
            txt_ecc_id = view.findViewById(R.id.txt_ecc_id);
            lyrParent = view.findViewById(R.id.lyr_parent);
            lyrDelete = view.findViewById(R.id.lyr_delete);
            checkbox = view.findViewById(R.id.checkbox);
            swipe = view.findViewById(R.id.swipe);
            txt_time = view.findViewById(R.id.txt_time);
            call_icon = view.findViewById(R.id.call_icon);
        }
    }


}
