package com.realapps.chat.view.home.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
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

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.realapps.chat.R;
import com.realapps.chat.data.database.DbHelper;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Prashant Sharma on 16/11/17.
 * Core techies
 * prashant@coretechies.org
 */

public class ChatAdapter extends RecyclerSwipeAdapter<ChatAdapter.MyViewHolder>
        implements Filterable {
    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private static final String TAG = "ChatAdapter";
    public static boolean DELETE_MODE = false;
    public static boolean checkLists[];
    private static List<ChatListEntity> chatFilteredList;
    private Context mContext;
    private List<ChatListEntity> chatList;
    private onItemClickListner listener;
    private DbHelper dbHelper;
    public static int openLayoutPosition = -1;
    public static RecyclerView recyclerView;

    public ChatAdapter(Context mContext, List<ChatListEntity> chatList, onItemClickListner listener, RecyclerView recyclerView) {
        this.mContext = mContext;
        this.listener = listener;
        this.chatList = chatList;
        this.recyclerView = recyclerView;
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
                        .inflate(R.layout.chat_list_item, parent, false);
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
            final ChatListEntity chatListEntity = chatFilteredList.get(holder.getAdapterPosition());

            //========================  chat detail in the chat fragment
            ArrayList<ChatMessageEntity> messageList = dbHelper.getMessageList(chatListEntity.getId(), chatListEntity.getChatType());

            if(messageList.size() == 0) return;

            int index = messageList.size() - 1;
            ChatMessageEntity entity = messageList.get(index);
            int msg_deliver_status = entity.getMessageStatus();//UNREAD_STATUS = 0;READ_STATUS = 1;ENCRYPTED_STATUS = 2;LOCK_STATUS = 3;IN_PROGRESS_STATUS = 4;NOT_SENT_STATUS = 5;
            //SENT_STATUS = 6;SENT_IN_PROGRESS_STATUS = 7;STATUS_DELIVERED = 2;STATUS_READ_BUT_UN_ACK = 8;
            int msg_mime_type = entity.getMessageMimeType();//TEXT = 1;IMAGE = 2;AUDIO = 3;VIDEO = 4;CONTACT = 5;NOTE = 6;DELETE = 7;MISSED = 8;
            int msg_send_recive = entity.getMessageType();// sended msg=0 or received msg=1
            String msg_arrived_time = entity.getMessageTimeStamp();//== message arrived time : 2020-06-23 02:06:56.933
            String msg_edited_time = entity.getEditedMessageTimeStamp();
            String msg_content = entity.getMessage();//======message content

            if(msg_mime_type == AppConstants.MIME_TYPE_TEXT) {
                holder.img_encrypted_type.setVisibility(View.GONE);
                holder.txt_encrypted.setText(mContext.getResources().getString(R.string.encrypted_text));

            } else if(msg_mime_type == AppConstants.MIME_TYPE_IMAGE) {
                holder.img_encrypted_type.setVisibility(View.VISIBLE);
                holder.img_encrypted_type.setImageResource(R.drawable.ic_camera_new);
                holder.txt_encrypted.setText(mContext.getResources().getString(R.string.encrypted_photo));

            } else if(msg_mime_type == AppConstants.MIME_TYPE_VIDEO) {
                holder.img_encrypted_type.setVisibility(View.VISIBLE);
                holder.img_encrypted_type.setImageResource(R.drawable.ic_video);
                holder.txt_encrypted.setText(mContext.getResources().getString(R.string.encrypted_video));

            } else if(msg_mime_type == AppConstants.MIME_TYPE_AUDIO) {
                holder.img_encrypted_type.setVisibility(View.VISIBLE);
                holder.img_encrypted_type.setImageResource(R.drawable.ic_mic_white);
                holder.txt_encrypted.setText(mContext.getResources().getString(R.string.encrypted_audio));

            } else if(msg_mime_type == AppConstants.MIME_TYPE_CONTACT) {
                holder.img_encrypted_type.setVisibility(View.VISIBLE);
                holder.img_encrypted_type.setImageResource(R.drawable.ic_contacts);
                holder.txt_encrypted.setText(mContext.getResources().getString(R.string.encrypted_contact));

            } else if(msg_mime_type == AppConstants.MIME_TYPE_NOTE) {
                holder.img_encrypted_type.setVisibility(View.VISIBLE);
                holder.img_encrypted_type.setImageResource(R.drawable.icon_file_unknown);
                holder.txt_encrypted.setText(mContext.getResources().getString(R.string.encrypted_note));

            }

            String msg_last_time = "";
            if(msg_edited_time.equals("")) msg_last_time = msg_arrived_time;
            else msg_last_time = msg_edited_time;

            String display_time = getDisplayTime(msg_last_time);
            holder.txt_missed_time.setText(display_time);

            if (msg_deliver_status == AppConstants.MESSAGE_SENT_STATUS) {
                holder.img_accept_status.setVisibility(View.VISIBLE);
                holder.img_accept_status.setImageResource(R.drawable.img_send); //message sent
            } else if (msg_deliver_status == AppConstants.MESSAGE_STATUS_DELIVERED) {
                holder.img_accept_status.setVisibility(View.VISIBLE);
                holder.img_accept_status.setImageResource(R.drawable.ic_delivered); // message delivered with confirmation so this only works on messages not on photos
            } else if (msg_deliver_status == AppConstants.MESSAGE_READ_STATUS) {
                holder.img_accept_status.setVisibility(View.VISIBLE);
                holder.img_accept_status.setImageResource(R.drawable.ic_read_sky);  //message read by the recipient
            } else {
                holder.img_accept_status.setVisibility(View.GONE);
            }
            //======================================================================







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
                holder.lyrParent.setBackgroundResource(R.color.black_effective);
            } else {
                holder.checkbox.setChecked(false);
                holder.lyrParent.setBackgroundResource(R.color.color_chat_list_item);;
            }

            if (chatListEntity.getChatType() == AppConstants.GROUP_CHAT_TYPE) {
                holder.group_bagde.setVisibility(View.VISIBLE);
                holder.Single_bagde.setVisibility(View.GONE);
                holder.txtName.setText(chatListEntity.getName().substring(0, 1).toUpperCase() + chatListEntity.getName().substring(1));
                holder.img_group.setVisibility(View.VISIBLE);
                holder.txt_ecc_id.setVisibility(View.GONE);

            } else if (chatListEntity.getChatType() == AppConstants.SINGLE_CHAT_TYPE) {
                holder.group_bagde.setVisibility(View.GONE);
                holder.Single_bagde.setVisibility(View.VISIBLE);
                holder.txtTitle.setBackgroundResource(R.drawable.badge_text_bg_black);
                if (CommonUtils.getContactName(mContext, chatListEntity.getEccId()).trim().contains(" ")) {
                    int lastIndex = CommonUtils.getContactName(mContext, chatListEntity.getEccId()).trim().lastIndexOf(" ");
                    String lastIntial = (String.valueOf(CommonUtils.getContactName(mContext, chatListEntity.getEccId()).toCharArray()[lastIndex + 1]).toUpperCase());
                    holder.txtTitle.setText(String.valueOf(CommonUtils.getContactName(mContext, chatListEntity.getEccId()).toCharArray()[0]).toUpperCase() + lastIntial);
                } else
                    holder.txtTitle.setText(String.valueOf(CommonUtils.getContactName(mContext, chatListEntity.getEccId()).toUpperCase().toCharArray()[0]));
                holder.txtTitle.setTextColor(Color.parseColor("#FFFFFF"));
                holder.txtName.setText(CommonUtils.getContactName(mContext, chatListEntity.getEccId()));
                holder.img_group.setVisibility(View.GONE);
                holder.txt_ecc_id.setVisibility(View.GONE);
            }


            int total_unread_message_count = dbHelper.getTotalUnreadMessagesCount(chatListEntity.getId());
            if (total_unread_message_count > 0) {
                holder.total_unread_msg.setVisibility(View.VISIBLE);
                holder.img_accept_status.setVisibility(View.GONE);
                if (total_unread_message_count > 99) {
                    holder.total_unread_msg.setTextSize(8);
                    holder.total_unread_msg.setText(Html.fromHtml("99<sup>+</sup>"));
                } else {
                    holder.total_unread_msg.setTextSize(12);
                    holder.total_unread_msg.setText(String.valueOf(total_unread_message_count));
                }
            } else {
                holder.total_unread_msg.setVisibility(View.GONE);
                holder.img_accept_status.setVisibility(View.VISIBLE);
            }


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

    private String getDisplayTime(String _t) {
        String val = "";
        String received_time = DateTimeUtils.getSimplifiedDateTime(_t);
        String received_date = DateTimeUtils.getCurrentDateOnly(_t, "MM/dd/yyyy");

        String current_date_time = DateTimeUtils.getCurrentDateTimeString(new Date());
        String current_date = DateTimeUtils.getCurrentDateOnly(current_date_time, "MM/dd/yyyy");
        String display_date = "Today";
        String display_time = "";

        if(current_date.equals(received_date)) {
//            display_date = "Today";
            display_time = received_time;

            val = display_time;

        } else {
            display_date = received_date;
//            display_time = received_time.split(" ")[1].trim() +" "+ received_time.split(" ")[2].trim();

            val = display_date;
        }
        return val;
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
        TextView txtName, total_unread_msg, txt_ecc_id, total_unread_msg1, txt_encrypted, txt_missed_time;
        LinearLayout lyrDelete;
        LinearLayout lyrParent;
        RelativeLayout Single_bagde, group_bagde;
        CheckBox checkbox;
        SwipeLayout swipe;
        ImageView img_group, txt_title1, img_encrypted_type, img_accept_status;
        SwipeLayout mSwipeLayout;


        MyViewHolder(View view) {
            super(view);
            txtTitle = view.findViewById(R.id.txt_title);
            total_unread_msg1 = view.findViewById(R.id.total_unread_msg1);
            txt_title1 = view.findViewById(R.id.txt_title1);
            txt_ecc_id = view.findViewById(R.id.txt_ecc_id);
            txtName = view.findViewById(R.id.txt_name);
            Single_bagde = view.findViewById(R.id.Single_bagde);
            group_bagde = view.findViewById(R.id.group_bagde);
            lyrParent = view.findViewById(R.id.lyr_parent);
            lyrDelete = view.findViewById(R.id.lyr_delete);
            checkbox = view.findViewById(R.id.checkbox);
            swipe = view.findViewById(R.id.swipe);
            img_group = view.findViewById(R.id.img_group);

            //=== new adding
            txt_encrypted = view.findViewById(R.id.txt_encrypted);
            img_encrypted_type = view.findViewById(R.id.img_encrypted_type);
            img_accept_status = view.findViewById(R.id.img_accept_status);
            txt_missed_time = view.findViewById(R.id.txt_missed_time);
            total_unread_msg = view.findViewById(R.id.total_unread_msg);

            mSwipeLayout = view.findViewById(R.id.swipe);
            mSwipeLayout.setClickToClose(true);
            mSwipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
            mSwipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    if (openLayoutPosition != -1 && openLayoutPosition != getAdapterPosition()) {
                        ChatAdapter.MyViewHolder viewHolder = (ChatAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(openLayoutPosition);
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
