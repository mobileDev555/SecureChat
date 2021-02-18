package com.realapps.chat.ui.ui.view.java.fontstyle;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;


public class MyButtonOpenMavenProRegular extends Button {
    public MyButtonOpenMavenProRegular(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public MyButtonOpenMavenProRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyButtonOpenMavenProRegular(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        Typeface tf = Typeface.createFromAsset(context.getAssets(),
                "fonts/MavenPro-Bold.ttf");
        setTypeface(tf);
    }
}
