package com.darren.android.bluetoothdemo.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.darren.android.bluetoothdemo.R;

/**
 * Created by Darren on 6/20/2017.
 */

public class Utils {
    public static void showAlertDialog(String message, Context context) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(R.string.alert);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
