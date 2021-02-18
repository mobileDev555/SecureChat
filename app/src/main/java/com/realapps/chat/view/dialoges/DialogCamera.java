package com.realapps.chat.view.dialoges;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.interfaces.AttachmentDialogResponse;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Prashant Kumar Sharma on 3/23/2017.
 */

public class DialogCamera extends Dialog {


    Context mContext;
    AttachmentDialogResponse dialogResponseListener;
    @BindView(R.id.lyr_picturs)
    LinearLayout lyrPicturs;
    @BindView(R.id.lyr_contacts)
    LinearLayout lyrContacts;
    @BindView(R.id.lyr_notes)
    LinearLayout lyrNotes;
    @BindView(R.id.lyr_camera)
    LinearLayout lyrCamera;


    public DialogCamera(Context mContext, AttachmentDialogResponse dialogResponseListener) {
        super(mContext);
        this.mContext = mContext;
        this.dialogResponseListener = dialogResponseListener;
        dialogResponseListener.onCameraResponse();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.dialog_attechment);
        ButterKnife.bind(this);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(getContext().getResources().getColor(android.R.color.transparent)));
        WindowManager.LayoutParams wlp = window.getAttributes();
        setCancelable(true);
        DisplayMetrics dm = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        wlp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        wlp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        wlp.windowAnimations = R.style.dialog_animation_attechment;
        wlp.gravity = Gravity.BOTTOM;
        wlp.dimAmount = 0.5f;
        window.setAttributes(wlp);
        setOnDismissListener(dialogInterface -> dialogResponseListener.onClose());
    }


    @OnClick({R.id.lyr_picturs, R.id.lyr_contacts, R.id.lyr_notes, R.id.lyr_camera})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.lyr_picturs:
                dialogResponseListener.onImageSelect();
                dismiss();
                break;

            case R.id.lyr_contacts:
                dialogResponseListener.onContactSelect();
                dismiss();
                break;
            case R.id.lyr_notes:
                dialogResponseListener.onPersonalNoteSelect();
                dismiss();
                break;

            case R.id.lyr_camera:
                dialogResponseListener.onCameraResponse();
                dismiss();
                break;
        }
    }
}
