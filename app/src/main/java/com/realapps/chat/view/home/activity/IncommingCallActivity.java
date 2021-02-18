package com.realapps.chat.view.home.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.data.prefs.User_settings;
import com.realapps.chat.model.ChatListEntity;
import com.realapps.chat.utils.AppConstants;
import com.realapps.chat.utils.CommonUtils;
import com.realapps.chat.view.dialoges.DialogUnlock;
import com.wang.avi.AVLoadingIndicatorView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IncommingCallActivity extends AppCompatActivity {


    Context mContext;
    Activity mActivity;
    Bundle bundle;
    @BindView(R.id.txt_name)
    TextView txt_name;
    @BindView(R.id.txt_ecc_id)
    TextView txt_ecc_id;
    @BindView(R.id.txt_title)
    TextView txt_title;
    @BindView(R.id.txt_credit)
    TextView txtCredit;
    @BindView(R.id.message)
    ImageView message;
    @BindView(R.id.callanswer)
    ImageView callanswer;
    @BindView(R.id.callcut)
    ImageView callcut;
    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;
    @BindView(R.id.avicut)
    AVLoadingIndicatorView avicut;
    private ChatListEntity chatListEntity;
    private int xDelta;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_incommingcall);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mActivity = this;
        mContext = this;
        ButterKnife.bind(mActivity);
        if (getIntent().getExtras() != null) {
            chatListEntity = (ChatListEntity) getIntent().getSerializableExtra(AppConstants.EXTRA_CHAT_LIST_ITEM);
        }
        initView();
    }

    private void initView() {
        txt_name.setText(CommonUtils.getContactName(mContext, chatListEntity.getEccId()));
        txt_ecc_id.setText(chatListEntity.getEccId());
        txt_title.setText(String.valueOf(CommonUtils.getContactName(mContext, chatListEntity.getEccId()).toUpperCase().toCharArray()[0]));
        slide();
        animationShowHide(true);

    }




    @Override
    protected void onResume() {
        super.onResume();
        HomeActivity.lockHandler.removeCallbacks(HomeActivity.runnable);
        if (AppConstants.lockscreen) {
            CommonUtils.checkDialog(IncommingCallActivity.this);
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
                    Log.e("Tag", "onPause: " + "background-006");
//                    CommonUtils.lockDialog(IncommingCallActivity.this);
                } else {
                    Log.e("Tag", "onPause: " + "forground-006");
                }


            }
        };
        HomeActivity.lockHandler.postDelayed(HomeActivity.runnable, User_settings.getLockTime(IncommingCallActivity.this));
    }


    @OnClick(R.id.message)
    public void onViewClicked() {
        this.finish();
        Intent i = new Intent(mContext, ChatWindowActivity.class);
        i.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
        startActivity(i);
    }


    public void hideShowCallCut(boolean hide) {
        if (hide)
            callcut.setVisibility(View.GONE);
        else
            callcut.setVisibility(View.VISIBLE);
    }

    public void hideShowCallAns(boolean hide) {
        if (hide)
            callanswer.setVisibility(View.GONE);
        else
            callanswer.setVisibility(View.VISIBLE);
    }



    public void animationShowHide(boolean show){
        avi.hide();
        avicut.hide();
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        if(show){
            callanswer.startAnimation(shake);
            callcut.startAnimation(shake);
           /* avi.show();
            avicut.show();*/
        }else{
            avi.hide();
            avicut.hide();
            callanswer.clearAnimation();
            callcut.clearAnimation();
        }

    }

    public  void slide(){

        callanswer.setOnTouchListener((v, event) -> {
            final int x = (int) event.getRawX();
            final int y = (int) event.getRawY();
            int[] location = new int[2];

            callcut.getLocationOnScreen(location);

            int devicewidth = Resources.getSystem().getDisplayMetrics().widthPixels;

            int cutX = location[0];
            int x2 = devicewidth - cutX;


            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    animationShowHide(false);
                    FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams)
                            v.getLayoutParams();


                    xDelta = x - lParams.leftMargin;
                    break;


                case MotionEvent.ACTION_UP:
                    FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) v
                            .getLayoutParams();
                    if (x > cutX) {

                        Intent intent = new Intent(mContext, PicCallActivity.class);
                        intent.putExtra(AppConstants.EXTRA_CHAT_LIST_ITEM, chatListEntity);
                        IncommingCallActivity.this.finish();
                        startActivity(intent);
                    } else {
                        hideShowCallCut(false);
                        layoutParams1.leftMargin = 0;
                        layoutParams1.topMargin = 0;
                        layoutParams1.rightMargin = 0;
                        layoutParams1.bottomMargin = 0;
                        animationShowHide(true);
                    }

                    v.setLayoutParams(layoutParams1);

                    break;

                case MotionEvent.ACTION_MOVE:

                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) v
                            .getLayoutParams();

                    if (x > cutX) {

                    }

                    if (layoutParams.leftMargin >= 0 && x < cutX + callcut.getWidth()) {


                        hideShowCallCut(true);
                        if (x - xDelta >= 0) {
                            layoutParams.leftMargin = x - xDelta;
                        } else {
                            layoutParams.leftMargin = 0;
                        }
                        layoutParams.topMargin = 0;
                        layoutParams.rightMargin = 0;
                        layoutParams.bottomMargin = 0;
                        v.setLayoutParams(layoutParams);
                    }


                    break;

            }

            return true;
        });

        callcut.setOnTouchListener((v, event) -> {
            final int x = (int) event.getRawX();

            final int y = (int) event.getRawY();
            int[] location = new int[2];
            callanswer.getLocationOnScreen(location);
            int x1 = location[0] + callanswer.getWidth();
            callcut.getLocationOnScreen(location);
            int devicewidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            int x2 = devicewidth - x;


            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    animationShowHide(false);
                    FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams)
                            v.getLayoutParams();

                    xDelta = x2 - lParams.rightMargin;
                    break;


                case MotionEvent.ACTION_UP:

                    FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) v
                            .getLayoutParams();

                    if (x < x1) {
                        IncommingCallActivity.this.finish();
                    } else {

                        hideShowCallAns(false);
                        layoutParams1.leftMargin = 0;
                        layoutParams1.topMargin = 0;
                        layoutParams1.rightMargin = 0;
                        layoutParams1.bottomMargin = 0;

                        v.setLayoutParams(layoutParams1);
                        animationShowHide(true);
                    }

                    break;

                case MotionEvent.ACTION_MOVE:

                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) v
                            .getLayoutParams();

                    if (layoutParams.rightMargin >= 0 && x - xDelta > 0) {
                        hideShowCallAns(true);
                        layoutParams.leftMargin = 0;
                        layoutParams.topMargin = 0;
                        if (x2 - xDelta >= 0) {
                            layoutParams.rightMargin = x2 - xDelta;
                        } else {
                            layoutParams.rightMargin = 0;
                        }
                        layoutParams.bottomMargin = 0;
                        v.setLayoutParams(layoutParams);
                    }

                    break;

            }

            return true;
        });

    }




}
