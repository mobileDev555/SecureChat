package com.realapps.chat.view.home.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.databinding.ActivityCallBinding;
import com.realapps.chat.interfaces.OnClickHandlerInterface;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.view.dialoges.DialogUnlock;


public class CallActivity extends AppCompatActivity implements OnClickHandlerInterface {


    Context mContext;
    Activity mActivity;
    private ChatListEntity chatListEntity;
    private ActivityCallBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mActivity = this;
        mContext = this;
        if (getIntent().getExtras() != null) {
            chatListEntity = (ChatListEntity) getIntent().getSerializableExtra(AppConstants.EXTRA_CHAT_LIST_ITEM);
        }
        initView();
        binding.setChat(chatListEntity);
        binding.setClickHandler(this);
    }

    private void initView() {
        binding.txtName.setText(CommonUtils.getContactName(mContext, chatListEntity.getEccId()));
        binding.txtTitle.setText(String.valueOf(CommonUtils.getContactName(mContext, chatListEntity.getEccId()).toUpperCase().toCharArray()[0]));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("=======CallActivity", "resume");
        HomeActivity.lockHandler.removeCallbacks(HomeActivity.runnable);
        if (AppConstants.lockscreen) {
            CommonUtils.checkDialog(CallActivity.this);
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
                    Log.e("Tag", "onPause: " + "background-004");
                    CommonUtils.lockDialog(CallActivity.this);
                } else {
                    Log.e("Tag", "onPause: " + "forground-004");
                }


            }
        };
        HomeActivity.lockHandler.postDelayed(HomeActivity.runnable, User_settings.getLockTime(CallActivity.this));
    }

    public void onUserClick() {

    }

    public void onMicClick() {
        if (binding.mic.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.mic).getConstantState())
            binding.mic.setImageResource(R.drawable.micon);
        else
            binding.mic.setImageResource(R.drawable.mic);
    }

    public void onSpeakerClick() {
        if (binding.speaker.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.speaker).getConstantState())
            binding.speaker.setImageResource(R.drawable.speakeron);
        else
            binding.speaker.setImageResource(R.drawable.speaker);
    }


    @Override
    public void onClick(View view) {
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
                Log.e("=======calling-close-1", "ok");
                binding.avi.hide();
                startActivity(new Intent(CallActivity.this, HomeActivity.class));
//                onBackPressed();
                break;
        }
    }
}
