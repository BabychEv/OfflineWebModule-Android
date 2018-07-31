package com.webmodule.offlinemodule;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

public class Utils {

    public static Dialog getProgressDialog(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_progress_bar, null);
        AlertDialog alertDialog = new AlertDialog
                .Builder(context)
                .setCancelable(false)
                .setView(view)
                .create();

        ProgressBar pb = (ProgressBar) view.findViewById(R.id.progressBar);
        pb.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

        alertDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        return alertDialog;
    }

}
