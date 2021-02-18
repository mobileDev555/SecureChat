package com.realapps.chat.view.home.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.realapps.chat.R;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatMessageEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

import cn.iwgang.countdownview.CountdownView;

/**
 * Created by Prashant Sharma on 16/11/17.
 * Core techies
 * prashant@coretechies.org
 */

public class GroupChatWindowAdapter extends RecyclerView.Adapter<GroupChatWindowAdapter.MyViewHolder>
        implements Filterable {
    private static final int VIEW_TYPE_EMPTY = -1;
    private static final int VIEW_TYPE_MY_TEXT_MESSAGE = 0;
    private static final int VIEW_TYPE_MY_AUDIO_MESSAGE = 1;
    private static final int VIEW_TYPE_MY_IMAGE_MESSAGE = 2;
    private static final int VIEW_TYPE_MY_CONTACT_MESSAGE = 3;
    private static final int VIEW_TYPE_MY_FILE_MESSAGE = 4;
    private static final int VIEW_TYPE_MY_VIDEO_MESSAGE = 5;
    private static final int VIEW_TYPE_SENDERS_TEXT_MESSAGE = 6;
    private static final int VIEW_TYPE_SENDERS_AUDIO_MESSAGE = 7;
    private static final int VIEW_TYPE_SENDERS_IMAGE_MESSAGE = 8;
    private static final int VIEW_TYPE_SENDERS_CONTACT_MESSAGE = 9;
    private static final int VIEW_TYPE_SENDERS_FILE_MESSAGE = 10;
    private static final int VIEW_TYPE_SENDERS_VIDEO_MESSAGE = 11;
    private static final int VIEW_TYPE_FROM_DELETE_MESSAGE = 12;
    private static final int VIEW_TYPE_MY_DELETE_MESSAGE = 13;
    public static boolean SELECT_MODE = false;
    public static boolean checkLists[];
    private static List<ChatMessageEntity> chatFilteredList;
    private Context mContext;
    private List<ChatMessageEntity> chatList;
    private onItemClickListner listener;
    boolean isVault = false;
    private ChatMessageEntity lastmessageEntity;


    public GroupChatWindowAdapter(Context mContext, List<ChatMessageEntity> chatList, onItemClickListner listener) {
        this.mContext = mContext;
        this.listener = listener;
        this.chatList = chatList;
        chatFilteredList = chatList;
        checkLists = new boolean[chatFilteredList.size()];
        SELECT_MODE = false;
    }

    public GroupChatWindowAdapter(Context mContext, List<ChatMessageEntity> chatList, onItemClickListner listener, boolean b) {
        this.mContext = mContext;
        this.listener = listener;
        this.chatList = chatList;
        chatFilteredList = chatList;
        checkLists = new boolean[chatFilteredList.size()];
        SELECT_MODE = b;
        isVault = mContext.getClass().getSimpleName().equalsIgnoreCase("VaultMessageWindowActivity");
    }

    public void notifyItemInsert(int position) {
        checkLists = new boolean[chatFilteredList.size()];
        notifyItemInserted(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case VIEW_TYPE_MY_TEXT_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.my_message_list_item, parent, false);
                break;
            case VIEW_TYPE_MY_AUDIO_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.my_audio_list_item, parent, false);
                break;
            case VIEW_TYPE_MY_IMAGE_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.my_image_list_item, parent, false);
                break;
            case VIEW_TYPE_MY_CONTACT_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.my_contact_list_item, parent, false);
                break;
            case VIEW_TYPE_MY_FILE_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.my_personal_note_list_item, parent, false);
                break;
            case VIEW_TYPE_MY_VIDEO_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.my_video_list_item, parent, false);
                break;
            case VIEW_TYPE_SENDERS_TEXT_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.from_message_list_item, parent, false);
                break;
            case VIEW_TYPE_SENDERS_AUDIO_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.from_audio_list_item, parent, false);
                break;
            case VIEW_TYPE_SENDERS_IMAGE_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.from_image_list_item, parent, false);
                break;
            case VIEW_TYPE_SENDERS_CONTACT_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.from_contact_list_item, parent, false);
                break;
            case VIEW_TYPE_SENDERS_FILE_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.from_personal_note_list_item, parent, false);
                break;
            case VIEW_TYPE_SENDERS_VIDEO_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.my_video_list_item, parent, false);
                break;
            case VIEW_TYPE_FROM_DELETE_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.from_delete_list_item, parent, false);
                break;
            case VIEW_TYPE_MY_DELETE_MESSAGE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.my_delete_list_item, parent, false);
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
        ChatMessageEntity messageEntity = null;
        if (getItemViewType(position) != VIEW_TYPE_EMPTY && getItemViewType(position) != VIEW_TYPE_FROM_DELETE_MESSAGE && getItemViewType(position) != VIEW_TYPE_MY_DELETE_MESSAGE) {
            messageEntity = chatFilteredList.get(position);
            Log.e("MsgStatus",""+messageEntity.getMessageStatus());
            if (position > 0 && messageEntity.getChatType() == 1) {
                lastmessageEntity = chatFilteredList.get(position - 1);
            }


            if (position == 0 && !messageEntity.getEddId().equalsIgnoreCase(User_settings.getECCID(mContext))) {
                holder.txt_user_name.setVisibility(View.VISIBLE);
                holder.txt_user_name.setText(CommonUtils.getContactName(mContext, messageEntity.getEddId()));
            } else {
                if (position != 0 && lastmessageEntity != null)
                    if (lastmessageEntity.getEddId().equalsIgnoreCase(messageEntity.getEddId())) {
                        holder.txt_user_name.setVisibility(View.GONE);
                    } else if (!messageEntity.getEddId().equalsIgnoreCase(User_settings.getECCID(mContext))) {
                        holder.txt_user_name.setVisibility(View.VISIBLE);
                        holder.txt_user_name.setText(CommonUtils.getContactName(mContext, messageEntity.getEddId()));
                    }

            }


            holder.checkBox.setEnabled(false);

            if (SELECT_MODE)
                holder.checkBox.setVisibility(View.VISIBLE);
            else
                holder.checkBox.setVisibility(View.GONE);

            if (messageEntity.getIsRevised() == AppConstants.revised)
                holder.img_edited.setVisibility(View.VISIBLE);
            else
                holder.img_edited.setVisibility(View.GONE);


            holder.checkBox.setChecked(messageEntity.isSelected());

            if (messageEntity.getMessageTimeStamp().length() > 0) {
                holder.txt_message_timestamp.setText(DateTimeUtils.getSimplifiedDateTime(messageEntity.getMessageTimeStamp()));
            }
            //holder.txt_message_destruction_time.setText(CommonUtils.getHours(messageEntity.getMessageBurnTimeStamp()));
            holder.mCountdownView.start(DateTimeUtils.getReamingTime(messageEntity.getMessageBurnTimeStamp()));

            if (SELECT_MODE) {
                holder.img_forward.setVisibility(View.GONE);
            } else {
                if (messageEntity.getMessageStatus() == AppConstants.MESSAGE_NOT_SENT_STATUS) {
                   holder.img_forward.setVisibility(View.GONE);
                    ChatMessageEntity finalMessageEntity2 = messageEntity;
                    holder.img_forward.setOnClickListener(view -> listener.onRetryMessage(finalMessageEntity2, position));
                } else {
                    holder.img_forward.setVisibility(View.GONE);
                    holder.img_forward.setImageResource(R.drawable.img_forward);
                    ChatMessageEntity finalMessageEntity3 = messageEntity;
                    holder.img_forward.setOnClickListener(view -> listener.onItemForward(finalMessageEntity3, position));
                }
            }

            switch (getItemViewType(position)) {

                case VIEW_TYPE_SENDERS_TEXT_MESSAGE:
                case VIEW_TYPE_MY_TEXT_MESSAGE:
                    if (User_settings.getfont(mContext) == AppConstants.smallFont) {
                        holder.txtMsg.setTextSize(15);
                    } else if (User_settings.getfont(mContext) == AppConstants.mediumFont) {
                        holder.txtMsg.setTextSize(18);
                    } else {
                        holder.txtMsg.setTextSize(22);
                    }
                    ChatMessageEntity finalMessageEntity = messageEntity;

                    holder.txtMsg.setText(finalMessageEntity.getMessage());


                    holder.lyrParent.setOnClickListener(view -> listener.onItemClick(finalMessageEntity, position));
                    holder.lyrParent.setOnLongClickListener(view -> {
                        listener.onItemLongPress(finalMessageEntity, position);
                        return false;
                    });
                    Log.e("Msg", "" + finalMessageEntity.getMessageStatus());

                    break;

                case VIEW_TYPE_MY_AUDIO_MESSAGE:

                case VIEW_TYPE_MY_IMAGE_MESSAGE:

                case VIEW_TYPE_MY_CONTACT_MESSAGE:

                case VIEW_TYPE_MY_FILE_MESSAGE:

                case VIEW_TYPE_MY_VIDEO_MESSAGE:

                case VIEW_TYPE_SENDERS_AUDIO_MESSAGE:

                case VIEW_TYPE_SENDERS_IMAGE_MESSAGE:

                case VIEW_TYPE_SENDERS_CONTACT_MESSAGE:

                case VIEW_TYPE_SENDERS_FILE_MESSAGE:

                case VIEW_TYPE_SENDERS_VIDEO_MESSAGE:
                    ChatMessageEntity finalMessageEntity1 = messageEntity;
                    holder.lyrParent.setOnClickListener(view -> listener.onItemClick(finalMessageEntity1, position));
                    holder.lyrParent.setOnLongClickListener(view -> {
                        listener.onItemLongPress(finalMessageEntity1, position);
                        return false;
                    });
                    break;
                case VIEW_TYPE_EMPTY:
                default:

                    break;
            }
            switch (getItemViewType(position)) {


                case VIEW_TYPE_MY_TEXT_MESSAGE:

                case VIEW_TYPE_MY_AUDIO_MESSAGE:

                case VIEW_TYPE_MY_IMAGE_MESSAGE:

                case VIEW_TYPE_MY_CONTACT_MESSAGE:

                case VIEW_TYPE_MY_FILE_MESSAGE:

                case VIEW_TYPE_MY_VIDEO_MESSAGE:
                    ChatMessageEntity finalMessageEntity = messageEntity;
                    if (isVault) {
                        holder.img_msg_status.setVisibility(View.GONE);
                    } else {
                        holder.img_msg_status.setVisibility(View.VISIBLE);
                    }

                    if (finalMessageEntity.getMessageStatus() == AppConstants.MESSAGE_SENT_STATUS)
                        holder.img_msg_status.setImageResource(R.drawable.ic_s1);
                    else if (finalMessageEntity.getMessageStatus() == AppConstants.MESSAGE_STATUS_DELIVERED)
                        holder.img_msg_status.setImageResource(R.drawable.ic_s1);
                    else if (finalMessageEntity.getMessageStatus() == AppConstants.MESSAGE_READ_STATUS)
                        holder.img_msg_status.setImageResource(R.drawable.ic_s1);
                    else
                        holder.img_msg_status.setVisibility(View.GONE);
                    break;

                case VIEW_TYPE_SENDERS_TEXT_MESSAGE:
                case VIEW_TYPE_SENDERS_AUDIO_MESSAGE:
                case VIEW_TYPE_SENDERS_IMAGE_MESSAGE:
                case VIEW_TYPE_SENDERS_CONTACT_MESSAGE:
                case VIEW_TYPE_SENDERS_FILE_MESSAGE:
                case VIEW_TYPE_SENDERS_VIDEO_MESSAGE:

                    holder.img_msg_status.setVisibility(View.GONE);
                    break;
                case VIEW_TYPE_EMPTY:
                default:
                    break;
            }
        } else if (getItemViewType(position) == VIEW_TYPE_FROM_DELETE_MESSAGE || getItemViewType(position) == VIEW_TYPE_MY_DELETE_MESSAGE) {
            messageEntity = chatFilteredList.get(position);

            String name;
            if (messageEntity.getSenderId() == Integer.parseInt(User_settings.getUserId(mContext))) {
                name = "You";
            } else {
                name = CommonUtils.getContactName(mContext, messageEntity.getEddId());
            }
            holder.messageText.setText(name + " deleted this message");
        }
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = -1;
        if (chatFilteredList != null && chatFilteredList.size() > 0) {

            if (chatFilteredList.get(position).getSenderId() == Integer.valueOf(User_settings.getUserId(mContext))) {
                if (chatFilteredList.get(position).getMessageMimeType() == AppConstants.MIME_TYPE_TEXT)
                    viewType = VIEW_TYPE_MY_TEXT_MESSAGE;
                else if (chatFilteredList.get(position).getMessageMimeType() == AppConstants.MIME_TYPE_AUDIO)
                    viewType = VIEW_TYPE_MY_AUDIO_MESSAGE;
                else if (chatFilteredList.get(position).getMessageMimeType() == AppConstants.MIME_TYPE_IMAGE)
                    viewType = VIEW_TYPE_MY_IMAGE_MESSAGE;
                else if (chatFilteredList.get(position).getMessageMimeType() == AppConstants.MIME_TYPE_CONTACT)
                    viewType = VIEW_TYPE_MY_CONTACT_MESSAGE;
                else if (chatFilteredList.get(position).getMessageMimeType() == AppConstants.MIME_TYPE_NOTE)
                    viewType = VIEW_TYPE_MY_FILE_MESSAGE;
                else if (chatFilteredList.get(position).getMessageMimeType() == AppConstants.MIME_TYPE_VIDEO)
                    viewType = VIEW_TYPE_MY_VIDEO_MESSAGE;
                else if (chatFilteredList.get(position).getMessageMimeType() == AppConstants.MIME_TYPE_DELETE)
                    viewType = VIEW_TYPE_MY_DELETE_MESSAGE;
            } else {
                if (chatFilteredList.get(position).getMessageMimeType() == AppConstants.MIME_TYPE_TEXT)
                    viewType = VIEW_TYPE_SENDERS_TEXT_MESSAGE;
                else if (chatFilteredList.get(position).getMessageMimeType() == AppConstants.MIME_TYPE_AUDIO)
                    viewType = VIEW_TYPE_SENDERS_AUDIO_MESSAGE;
                else if (chatFilteredList.get(position).getMessageMimeType() == AppConstants.MIME_TYPE_IMAGE)
                    viewType = VIEW_TYPE_SENDERS_IMAGE_MESSAGE;
                else if (chatFilteredList.get(position).getMessageMimeType() == AppConstants.MIME_TYPE_CONTACT)
                    viewType = VIEW_TYPE_SENDERS_CONTACT_MESSAGE;
                else if (chatFilteredList.get(position).getMessageMimeType() == AppConstants.MIME_TYPE_NOTE)
                    viewType = VIEW_TYPE_SENDERS_FILE_MESSAGE;
                else if (chatFilteredList.get(position).getMessageMimeType() == AppConstants.MIME_TYPE_VIDEO)
                    viewType = VIEW_TYPE_SENDERS_VIDEO_MESSAGE;
                else if (chatFilteredList.get(position).getMessageMimeType() == AppConstants.MIME_TYPE_DELETE)
                    viewType = VIEW_TYPE_FROM_DELETE_MESSAGE;
            }


        } else {
            viewType = VIEW_TYPE_EMPTY;
        }
        return viewType;
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
                    List<ChatMessageEntity> filteredList = new ArrayList<>();
                    for (ChatMessageEntity row : chatList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getMessage().toLowerCase().contains(charString.toLowerCase())) {
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
                chatFilteredList = (ArrayList<ChatMessageEntity>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    public interface onItemClickListner {
        void onItemClick(ChatMessageEntity chatMessageEntity, int position);

        void onItemLongPress(ChatMessageEntity chatMessageEntity, int position);

        void onItemForward(ChatMessageEntity chatMessageEntity, int position);

        void onRetryMessage(ChatMessageEntity chatMessageEntity, int position);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView txtMsg;
        public CheckBox checkBox;
        public TextView txt_message_destruction_time, txt_user_name, messageText;
        public TextView txt_message_timestamp;
        public ImageView img_forward, img_edited,img_msg_status;
        public LinearLayout lyrParent;
        CountdownView mCountdownView;


        MyViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.checkbox);
            mCountdownView = view.findViewById(R.id.countdownView);
            messageText = view.findViewById(R.id.messageText);
            txtMsg = view.findViewById(R.id.txt_msg);
            lyrParent = view.findViewById(R.id.lyr_parent);
            txt_message_destruction_time = view.findViewById(R.id.txt_message_destruction_time);
            txt_message_timestamp = view.findViewById(R.id.txt_message_timestamp);
            img_forward = view.findViewById(R.id.img_forward);
            txt_user_name = view.findViewById(R.id.txt_user_name);
            img_edited = view.findViewById(R.id.img_edited);
            img_msg_status = view.findViewById(R.id.img_msg_status);
        }
    }


}
