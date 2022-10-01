package com.apetta.detext_app.alertDialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.apetta.detext_app.R;

public class ProgressAlertDialog {

    private final AlertDialog alertDialog;
    private final TextView textView;

    /**
     *
     * @param context The context of the application.
     * @param message The message of the alert dialog that will be displayed.
     */
    public ProgressAlertDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.progress_alert_dialog, null);
        textView = view.findViewById(R.id.msgProgress);
        textView.setText(message);
        builder.setView(view);
        builder.setCancelable(false);
        alertDialog = builder.create();
    }

    // Show the alert dialog to the user.
    public void show() {
        alertDialog.show();
    }

    // Dismiss the alert dialog.
    public void dismiss() {
        alertDialog.dismiss();
    }

    protected void setMessage(String message) {
        textView.setText(message);
    }
}
