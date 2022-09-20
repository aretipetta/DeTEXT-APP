package com.apetta.detext_app.alertDialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.apetta.detext_app.R;

public class TemperatureAlertDialog {
    private final AlertDialog alertDialog;


    public TemperatureAlertDialog(Context context, String temperature, String location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.temperature_alert_dialog, null);
        TextView temperatureValue = view.findViewById(R.id.temperatureValue);
        temperatureValue.setText(temperature);
        TextView locationValue = view.findViewById(R.id.locationValue);
        locationValue.setText(location);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog = builder.create();
    }

    public void show() {
        alertDialog.show();
    }
}
