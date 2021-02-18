package com.realapps.chat.view.dialoges;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.interfaces.DialogResponseListener;
import com.realapps.chat.utils.AudioWife;


/**
 * Created by Prashant Kumar Sharma on 3/23/2017.
 */

public class DialogMediaPlayer extends Dialog implements View.OnClickListener {

    private DialogResponseListener dialogResponse = null;
    private Context ctx;
    private Activity activity;
    private ImageView ic_media_play, ic_media_pause;
    private TextView start_tym, total_tym;
    private SeekBar seek_bar;
    private String path = "";
    private AudioWife audioWife;
    public DialogMediaPlayer(Context mContext, String path, DialogResponseListener dialogResponseListener) {
        super(mContext);
        activity = (Activity) mContext;
        this.dialogResponse = dialogResponseListener;
        this.ctx = mContext;
        this.path = path;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.dialog_media_player_chat);
        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        setCancelable(false);
        wlp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        wlp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        wlp.windowAnimations = R.style.dialog_animation_from_center;
        wlp.dimAmount = 0.9f;
        window.setAttributes(wlp);
        initView();
        setOnDismissListener(dialog -> {
            audioWife.pause();
            audioWife.release();
        });
    }


    private void initView() {
        seek_bar = findViewById(R.id.seek_bar);
        ic_media_play = findViewById(R.id.ic_media_play);
        ic_media_pause = findViewById(R.id.ic_media_pause);
        start_tym = findViewById(R.id.start_tym);
        total_tym = findViewById(R.id.total_tym);
        Uri uri = Uri.parse(path);
        audioWife = AudioWife.getInstance()
                .init(ctx, uri)
                .setPlayView(ic_media_play)
                .setPauseView(ic_media_pause)
                .setSeekBar(seek_bar)
                .setRuntimeView(start_tym)
                .setTotalTimeView(total_tym);
        audioWife.play();
        ic_media_pause.setVisibility(View.VISIBLE);
        ic_media_play.setVisibility(View.GONE);
        audioWife.addOnCompletionListener(mp -> {
            audioWife.pause();
            audioWife.release();
            dismiss();
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_media_play:
                audioWife.play();
                ic_media_pause.setVisibility(View.VISIBLE);
                ic_media_play.setVisibility(View.GONE);
                break;
            case R.id.ic_media_pause:
                audioWife.pause();
                ic_media_pause.setVisibility(View.GONE);
                ic_media_play.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        audioWife.pause();
        audioWife.release();
        dismiss();
    }

}
