package com.vanvatcorporation.doubleclips.popups;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vanvatcorporation.doubleclips.AdsHandler;
import com.vanvatcorporation.doubleclips.R;

public class CompressionPopup extends AlertDialog.Builder {
    public ProgressBar previewProgressBar;
    public TextView titleText, descriptionText;
    public TextView processingPercent;

    public AlertDialog dialog;

    public CompressionPopup(Context context, String title, String description) {
        super(context);


        // Inflate your custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.popup_processing_project, null);
        setView(dialogView);
        setCancelable(false);

        // Get references to the EditText and Buttons in your custom layout
        titleText = dialogView.findViewById(R.id.title);
        descriptionText = dialogView.findViewById(R.id.processingDescription);
        previewProgressBar = dialogView.findViewById(R.id.previewProgressBar);
        processingPercent = dialogView.findViewById(R.id.processingPercent);

        // Create the AlertDialog
        dialog = create();

        // Already prevent using the setCancelable above
        //dialog.setCanceledOnTouchOutside(false);
        // Show the dialog
        dialog.show();

        titleText.setText(title);
        descriptionText.setText(description);
    }


}
