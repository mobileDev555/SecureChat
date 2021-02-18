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
import com.realapps.chat.ui.utils.Log;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.utils.SocketUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prashant Sharma on 16/11/17.
 * Core techies
 * prashant@coretechies.org
 */

public class ContactAdapter extends RecyclerSwipeAdapter<ContactAdapter.MyViewHolder>
        implements Filterable {
    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    public static boolean DELETE_MODE = false;
    public static boolean checkLists[];
    private static List<ContactEntity> contactFilteredList;
    int expandeItemPosition = -1;
    private Context mContext;
    private List<ContactEntity> contactlist;
    private onItemClickListner listener;

    public static int openLayoutPosition = -1;
    public static RecyclerView recyclerView;

    public ContactAdapter(Context mContext, List<ContactEntity> contactlist, onItemClickListner listener, RecyclerView recyclerView) {
        this.mContext = mContext;
        this.listener = listener;
        this.contactlist = contactlist;
        ContactAdapter.recyclerView = recyclerView;
        contactFilteredList = contactlist;
        checkLists = new boolean[contactFilteredList.size()];
    }

    public void notifyItemRemove(int position) {
        contactFilteredList.remove(position);
        notifyItemRemoved(position);
    }

    public void updateCheckList() {
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
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, parent, false);
                break;
            case VIEW_TYPE_EMPTY:
            default:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_chat_list_layout, parent, false);
                break;
        }

        return new MyViewHolder(itemView);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        if (getItemViewType(position) == VIEW_TYPE_NORMAL) {
            final ContactEntity contactEntity = contactFilteredList.get(position);

            //expande child
            if (expandeItemPosition == position) {
                holder.lyrChild.setVisibility(View.VISIBLE);
                holder.imgExpand.setImageResource(R.drawable.ic_expand_more_white);
                holder.swipe.setSwipeEnabled(false);

            } else {
                holder.lyrChild.setVisibility(View.GONE);
                holder.imgExpand.setImageResource(R.drawable.img_chevron_right_white);
                holder.swipe.setSwipeEnabled(true);
            }

            if (DELETE_MODE) {
                if (holder.checkbox.getVisibility() == View.GONE) {
                    holder.checkbox.setVisibility(View.VISIBLE);
                    holder.swipe.setSwipeEnabled(false);
                }
            } else {
                if (holder.checkbox.getVisibility() == View.VISIBLE) {
                    holder.checkbox.setVisibility(View.GONE);
                    holder.swipe.setSwipeEnabled(true);
                }
            }
            //check box
            if (contactEntity.isSelected()) {
                holder.checkbox.setChecked(true);
                holder.lyrParent.setBackgroundResource(R.color.black_effective);
            } else {
                holder.checkbox.setChecked(false);
                holder.lyrParent.setBackgroundResource(R.color.color_chat_list_item);;
            }

            if (contactEntity.getBlockStatus().equalsIgnoreCase(String.valueOf(SocketUtils.request))) {
                holder.imgAccept.setVisibility(View.VISIBLE);
                holder.imgExpand.setVisibility(View.VISIBLE);
                holder.text_pending.setVisibility(View.GONE);
                holder.imgExpand.setImageResource(R.drawable.ic_clear_black_24dp);
                expandeItemPosition = -1;
                holder.imgAccept.setOnClickListener(view -> listener.onAcceptRequest(contactEntity, position));

                holder.imgExpand.setOnClickListener(view -> listener.onRejectRequest(contactEntity, position));

            } else if (contactEntity.getBlockStatus().equalsIgnoreCase(String.valueOf(SocketUtils.pending))) {
                holder.imgAccept.setVisibility(View.GONE);
                holder.imgExpand.setVisibility(View.GONE);
                holder.text_pending.setVisibility(View.VISIBLE);

            } else if (contactEntity.getBlockStatus().equalsIgnoreCase(String.valueOf(SocketUtils.accepted))) {
                holder.imgAccept.setVisibility(View.GONE);
                holder.imgExpand.setVisibility(View.VISIBLE);
                holder.text_pending.setVisibility(View.GONE);
                holder.imgExpand.setOnClickListener(view -> {
                    if (expandeItemPosition == position) {
                        expandeItemPosition = -1;
                    } else {
                        expandeItemPosition = position;
                    }
                    notifyDataSetChanged();
                });
            } else {
                holder.text_pending.setVisibility(View.GONE);
            }

            if (!contactEntity.getName().isEmpty() && contactEntity.getName().length() > 0) {
                if (contactEntity.getName().contains(" ")) {
                    int lastIndex = contactEntity.getName().trim().lastIndexOf(" ");
                    String lastIntial = (String.valueOf(contactEntity.getName().toCharArray()[lastIndex + 1]).toUpperCase());
                    holder.txtTitle.setText(String.valueOf(contactEntity.getName().toCharArray()[0]).toUpperCase() + lastIntial);
                    } else {
                    holder.txtTitle.setText(String.valueOf(contactEntity.getName().toCharArray()[0]).toUpperCase());
                    }
            }
            if(contactEntity.getName().length()>1)
            holder.txtName.setText(contactEntity.getName().substring(0, 1).toUpperCase() + contactEntity.getName().substring(1));
            else
                holder.txtName.setText(contactEntity.getName().toUpperCase());
            holder.txtECCId.setText(mContext.getString(R.string.ecc_id_s, contactEntity.getEccId().toUpperCase()));


            //============= send message
            holder.lyrParent.setOnLongClickListener(view -> {
                listener.onItemLongPress(contactEntity, position);
                return false;
            });
            holder.lyrParent.setOnClickListener(view -> {
                if (DELETE_MODE) {
                    listener.onItemClick(contactEntity, position);
                } else {
                    listener.onOpeningChat(contactEntity, position, contactEntity.getBlockStatus());
                }
            });
            holder.lyrSendMessage.setOnClickListener(view -> listener.onItemSendMessage(contactEntity, position, contactEntity.getBlockStatus()));
            holder.lyrRename.setOnClickListener(view -> listener.onChangeScreenName(contactEntity, position));
            holder.lyrCall.setOnClickListener(view -> listener.onCall(contactEntity, position));
            holder.lyrChat.setOnClickListener(view -> listener.onChat(contactEntity, position));
            holder.lyr_slide_delete_contact.setOnClickListener(view -> listener.onSlideItemDelete(contactEntity, position));

            holder.swipe.setShowMode(SwipeLayout.ShowMode.PullOut);

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

        void onItemSendMessage(ContactEntity contactEntity, int position, String accept_state);

        void onCall(ContactEntity contactEntity, int position);

        void onSlideItemDelete(ContactEntity contactEntity, int position);

        void onChangeScreenName(ContactEntity contactEntity, int position);

        void onRejectRequest(ContactEntity contactEntity, int position);

        void onAcceptRequest(ContactEntity contactEntity, int position);

        void onChat(ContactEntity contactEntity, int position);

        void onOpeningChat(ContactEntity contactEntity, int position, String accept_state);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkbox;
        TextView txtTitle, text_pending;
        TextView txtName;
        ImageView imgExpand;
        ImageView imgAccept;
        SwipeLayout swipe;
        TextView txtECCId;
        LinearLayout lyrCall;
        LinearLayout lyrChat;
        LinearLayout lyrSendMessage;
        LinearLayout lyrChild;
        LinearLayout lyrParent;
        LinearLayout lyrRename;
        LinearLayout lyr_slide_delete_contact;

        MyViewHolder(View view) {
            super(view);
            checkbox = view.findViewById(R.id.checkbox);
            txtTitle = view.findViewById(R.id.txt_title);
            text_pending = view.findViewById(R.id.text_pending);
            txtName = view.findViewById(R.id.txt_name);
            swipe = view.findViewById(R.id.swipe);
            imgExpand = view.findViewById(R.id.img_expand);
            imgAccept = view.findViewById(R.id.img_accept);
            txtECCId = view.findViewById(R.id.txt_ecc_id);
            lyrCall = view.findViewById(R.id.lyr_call);
            lyrChat = view.findViewById(R.id.lyr_chat);
            lyrSendMessage = view.findViewById(R.id.lyr_send_message);
            lyrChild = view.findViewById(R.id.lyr_child);
            lyrParent = view.findViewById(R.id.lyr_parent);
            lyrRename = view.findViewById(R.id.lyr_rename);
            lyr_slide_delete_contact = view.findViewById(R.id.lyr_slide_delete_contact);

            swipe.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {


                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    if (openLayoutPosition != -1 && openLayoutPosition != getAdapterPosition()) {
                        ContactAdapter.MyViewHolder viewHolder = (ContactAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(openLayoutPosition);
                        if (viewHolder != null && viewHolder.swipe != null)
                            viewHolder.swipe.close(true);
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
