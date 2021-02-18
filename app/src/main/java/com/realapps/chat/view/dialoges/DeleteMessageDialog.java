package com.realapps.chat.view.dialoges;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class DeleteMessageDialog extends Dialog {
    @BindView(R.id.textView2)
    TextView textView2;
    @BindView(R.id.textView3)
    TextView textView3;
    @BindView(R.id.textView5)
    TextView textView5;
    @BindView(R.id.textView6)
    TextView textView6;
    private String message;
    private Context mContext;
    private DeleteMessageListener mListener;
    private Unbinder unbinder;
    private boolean enableDeleteForEveryOne;

    public DeleteMessageDialog(Context mContext, String message, boolean enableDeleteForEveryOne, DeleteMessageListener listner) {
        super(mContext);
        this.mContext = mContext;
        this.mListener = listner;
        this.message = message;
        this.enableDeleteForEveryOne = enableDeleteForEveryOne;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.delete_message_dailog);
        Window window = getWindow();
        unbinder = ButterKnife.bind(this);
        WindowManager.LayoutParams wlp = window.getAttributes();
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        wlp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        wlp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        wlp.windowAnimations = R.style.dialog_animation;
        wlp.dimAmount = 0.9f;
        window.setAttributes(wlp);
        textView2.setText(message);
        setOnDismissListener(dialog -> mListener.onCancel());
        textView6.setVisibility(enableDeleteForEveryOne ? View.VISIBLE : View.GONE);

    }
    @OnClick({R.id.textView3, R.id.textView5, R.id.textView6})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.textView3:
                mListener.deleteForMe();
                dismiss();
                break;
            case R.id.textView5:
                mListener.onCancel();
                dismiss();
                break;
            case R.id.textView6:
                mListener.deleteForEveryOne();
                dismiss();
                break;
        }
    }


    public interface DeleteMessageListener {
        void deleteForMe();

        void onCancel();

        void deleteForEveryOne();
    }
}

