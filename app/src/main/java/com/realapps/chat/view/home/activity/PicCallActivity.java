package com.realapps.chat.view.home.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.view.dialoges.DialogUnlock;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PicCallActivity extends AppCompatActivity {


    Context mContext;
    Activity mActivity;
    Bundle bundle;
    @BindView(R.id.txt_name)
    TextView txt_name;
    @BindView(R.id.txt_ecc_id)
    TextView txt_ecc_id;
    @BindView(R.id.txt_title)
    TextView txt_title;
    @BindView(R.id.text_calling)
    TextView text_calling;
    @BindView(R.id.txt_credit)
    TextView txtCredit;
    @BindView(R.id.user)
    ImageView user;
    @BindView(R.id.mic)
    ImageView mic;
    @BindView(R.id.speaker)
    ImageView speaker;
    @BindView(R.id.red_close)
    ImageView redClose;
    int seconds;
    boolean running;
    private ChatListEntity chatListEntity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_pic_call);
        mActivity = this;
        mContext = this;
        ButterKnife.bind(mActivity);

        if (getIntent().getExtras() != null) {
            chatListEntity = (ChatListEntity) getIntent().getSerializableExtra(AppConstants.EXTRA_CHAT_LIST_ITEM);
        }
        initView();

    }

    public void runTimer(){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int sec = seconds % 60;
                String time = String.format("%d:%02d:%02d", hours, minutes, sec);
                text_calling.setText(time);
                if(running) {
                    seconds++;
                }
                handler.postDelayed(this, 1000);
            }
        });

    }

    public void resetTimer(){
        running = true;
        seconds = 0;
        runTimer();
    }

    private void initView() {
        txt_name.setText(CommonUtils.getContactName(mContext, chatListEntity.getEccId()));
        txt_ecc_id.setText(chatListEntity.getEccId());
        txt_title.setText(String.valueOf(CommonUtils.getContactName(mContext, chatListEntity.getEccId()).toUpperCase().toCharArray()[0]));
       resetTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        HomeActivity.lockHandler.removeCallbacks(HomeActivity.runnable);
        if (AppConstants.lockscreen) {
            CommonUtils.checkDialog(PicCallActivity.this);
        }
        AppConstants.isbackground = false;
        if (AppConstants.lockscreen) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    DialogUnlock.onShowKeyboard.showKeyboard();
                }
            }, 500);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppConstants.isbackground = true;
        HomeActivity.runnable = new Runnable() {
            @Override
            public void run() {
                if (AppConstants.isbackground) {
                    Log.e("Tag", "onPause: " + "background-015");
                    CommonUtils.lockDialog(PicCallActivity.this);
                } else {
                    Log.e("Tag", "onPause: " + "forground-015");
                }


            }
        };
        HomeActivity.lockHandler.postDelayed(HomeActivity.runnable, User_settings.getLockTime(PicCallActivity.this));
    }


    @OnClick({R.id.user, R.id.mic, R.id.speaker, R.id.red_close})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.user:
                onUserClick();
                break;
            case R.id.mic:
                onMicClick();
                break;
            case R.id.speaker:
                onSpeakerClick();
                break;
            case R.id.red_close:
                Log.e("=======calling-close-2", "ok");
                PicCallActivity.this.finish();
                break;
        }
    }

    public void onUserClick() {

    }

    public void onMicClick() {
        if (mic.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.mic).getConstantState())
            mic.setImageResource(R.drawable.micon);
        else
            mic.setImageResource(R.drawable.mic);
    }

    public void onSpeakerClick() {
        if (speaker.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.speaker).getConstantState())
            speaker.setImageResource(R.drawable.speakeron);
        else
            speaker.setImageResource(R.drawable.speaker);
    }


}
