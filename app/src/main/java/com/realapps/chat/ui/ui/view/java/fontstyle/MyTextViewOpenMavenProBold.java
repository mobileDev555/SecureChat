package com.realapps.chat.ui.ui.view.java.fontstyle;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by inextrix on 23/8/17.
 */

public class MyTextViewOpenMavenProBold extends TextView {

    public MyTextViewOpenMavenProBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public MyTextViewOpenMavenProBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyTextViewOpenMavenProBold(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        Typeface tf = Typeface.createFromAsset(context.getAssets(),
                "fonts/MavenPro-Bold.ttf");
        setTypeface(tf);
    }

}
