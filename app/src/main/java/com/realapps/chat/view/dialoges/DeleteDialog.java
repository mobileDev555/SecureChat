package com.realapps.chat.view.dialoges;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.realapps.chat.BuildConfig;
import com.realapps.chat.R;
import com.realapps.chat.interfaces.DeleteItemsResponse;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Prashant Kumar Sharma on 3/23/2017.
 */

public class DeleteDialog extends Dialog {

    Context mContext;
    DeleteItemsResponse deleteItemsResponse;
    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.txt_message)
    TextView txtMessage;
    @BindView(R.id.btn_delete)
    Button btnDelete;
    @BindView(R.id.btn_cancel)
    Button btnCancel;
    @BindView(R.id.lyr_account_password_child)
    LinearLayout lyrAccountPasswordChild;
    String title = "";
    String message = "";
    private Unbinder unbinder;

    public DeleteDialog(Context mContext, String title, String message, DeleteItemsResponse deleteItemsResponse) {
        super(mContext);
        this.mContext = mContext;
        this.deleteItemsResponse = deleteItemsResponse;
        this.title = title;
        this.message = message;

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.delete_dialog_box);
        Window window = getWindow();
        unbinder = ButterKnife.bind(this);
        WindowManager.LayoutParams wlp = window.getAttributes();
        setCancelable(true);

        wlp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        wlp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        wlp.windowAnimations = R.style.dialog_animation;
        wlp.dimAmount = 0.9f;
        window.setAttributes(wlp);

        txtTitle.setText(title);
        txtMessage.setText(message);

        setOnDismissListener(dialogInterface -> deleteItemsResponse.onClose());

        if (message.contains("On multi")) {
            btnDelete.setText(mContext.getString(R.string.save));
        } else {
            btnDelete.setText(mContext.getString(R.string.delete));
        }


    }

    @OnClick({R.id.btn_delete, R.id.btn_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_delete:
                deleteItemsResponse.onDelete(true);
                dismiss();
                break;
            case R.id.btn_cancel:
                dismiss();
                deleteItemsResponse.onClose();
                break;
        }
    }

}

