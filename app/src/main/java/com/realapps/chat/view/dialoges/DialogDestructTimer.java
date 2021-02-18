package com.realapps.chat.view.dialoges;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.interfaces.DestructTimeDialogResponse;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Prashant Kumar Sharma on 3/23/2017.
 */

public class DialogDestructTimer extends Dialog {

    Context mContext;
    DestructTimeDialogResponse dialogResponseListener;
    @BindView(R.id.seek_bar_tym)
    SeekBar seekbar;
    List<String> message_burn_time_list = new ArrayList<>();
    ChatListEntity chatListEntity;
    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.txt_destruction_time)
    TextView txtDestructionTime;

    public DialogDestructTimer(Context mContext, ChatListEntity chatListEntity, DestructTimeDialogResponse dialogResponseListener) {
        super(mContext);
        this.mContext = mContext;
        this.dialogResponseListener = dialogResponseListener;
        this.chatListEntity = chatListEntity;
        message_burn_time_list = Arrays.asList(mContext.getResources().getStringArray(R.array.burn_time));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_destruct_time);
        ButterKnife.bind(this);
        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        setCancelable(true);
        wlp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        wlp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        wlp.windowAnimations = R.style.dialog_animation;
        wlp.dimAmount = 0.9f;
        window.setAttributes(wlp);
        int tempBurnTime = message_burn_time_list.size() - 1;
        txtDestructionTime.setText(CommonUtils.getBurnTime(mContext, chatListEntity.getBurnTime()));
        seekbar.setMax(tempBurnTime);
        seekbar.setProgress(chatListEntity.getBurnTime());
        setOnDismissListener(dialogInterface -> dialogResponseListener.onClose());
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setValue(progress);
                dialogResponseListener.onTimeChange(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setValue(int progress) {
        int id = progress;
        txtDestructionTime.setText(CommonUtils.getBurnTime(mContext, id));
    }


}
