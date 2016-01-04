package com.example.junhong.beachat;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by Junhong on 2016-01-04.
 */
public class CustomDialog extends Dialog implements DialogInterface.OnClickListener {
    public CustomDialog(Context context) {
        super(context);

        setContentView(R.layout.custom_dialog);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }
}
