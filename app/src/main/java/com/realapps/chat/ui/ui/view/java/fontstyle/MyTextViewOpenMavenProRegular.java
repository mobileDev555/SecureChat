package com.realapps.chat.ui.ui.view.java.fontstyle;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by telafric on 13/7/17.
 */

public class MyTextViewOpenMavenProRegular extends TextView {
    public MyTextViewOpenMavenProRegular(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public MyTextViewOpenMavenProRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyTextViewOpenMavenProRegular(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        Typeface tf = Typeface.createFromAsset(context.getAssets(),
                "fonts/MavenPro-Regular.ttf");
        setTypeface(tf);
    }
}
