package com.realapps.chat.ui.ui.view.java;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by inextrix on 29/6/18.
 */

public class WrapContentLinearLayoutManager extends LinearLayoutManager {


    /*public WrapContentLinearLayoutManager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WrapContentLinearLayoutManager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }*/

    public WrapContentLinearLayoutManager(Context context, int horizontal, boolean b) {
        super(context);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Log.e("probe", "meet a IOOBE in RecyclerView");
        }
    }
}
