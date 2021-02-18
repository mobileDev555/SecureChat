/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.realapps.chat.ui.ui.incall;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.realapps.chat.R;
import com.realapps.chat.ui.utils.Log;
import com.realapps.chat.ui.widgets.DialpadDtmf;

public class DtmfDialogFragment extends DialogFragment implements DialpadDtmf.OnDialKeyListener {


    private static final String EXTRA_CALL_ID = "call_id";
    private static final String THIS_FILE = "DtmfDialogFragment";
    private TextView dialPadTextView;
    
    public static DtmfDialogFragment newInstance(int callId) {
        DtmfDialogFragment instance = new DtmfDialogFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_CALL_ID, callId);
        instance.setArguments(args);
        return instance;
    }

    

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog dialog;
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        View v=getCustomView(getActivity().getLayoutInflater(), null, savedInstanceState);

        Button btn=(Button) v.findViewById(R.id.btn_done);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        builder.setView(v);
        builder.setCancelable(true);
        dialog=builder.create();

/*
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())

                .setView()
                .setCancelable(true)
                .setNeutralButton(R.string.done, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })

                .create();*/
        return dialog;

    }

    
    public View getCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.in_call_dialpad, container, false);

        DialpadDtmf dialPad = (DialpadDtmf) v.findViewById(R.id.dialPad);
        //dialPad.setForceWidth(true);
        dialPad.setOnDialKeyListener(this);
        dialPadTextView = (TextView) v.findViewById(R.id.digitsText);


        return v;
    }

    public interface OnDtmfListener {
        void OnDtmf(int callId, int keyCode, int dialTone);
    }

    @Override
    public void onTrigger(int keyCode, int dialTone) {
        if(dialPadTextView != null) {
            // Update text view
            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
            char nbr = event.getNumber();
            StringBuilder sb = new StringBuilder(dialPadTextView.getText());
            sb.append(nbr);
            dialPadTextView.setText(sb.toString());
        }
        if(getActivity() instanceof OnDtmfListener) {
            Integer callId = getArguments().getInt(EXTRA_CALL_ID);
            if(callId != null) {
                ((OnDtmfListener) getActivity()).OnDtmf(callId, keyCode, dialTone);
            }else {
                Log.w(THIS_FILE, "Impossible to find the call associated to this view");
            }
        }
        
    }
    
    
}
