package com.patil.geobells.lite.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.patil.geobells.lite.R;

public class UpgradeDialog {
    private Context context;
    public UpgradeDialog(Context context) {
        this.context = context;
    }

    public void showUpgradeDialog(String message) {
        AlertDialog.Builder alertbox = new AlertDialog.Builder(context);
        alertbox.setTitle(context.getString(R.string.dialog_title_upgrade));
        alertbox.setMessage(message);
        alertbox.setPositiveButton(context.getString(R.string.dialog_button_upgrade), new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.patil.geobells"));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent);
                    }
                });
        alertbox.setNegativeButton(context.getString(R.string.dialog_button_nothanks), new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });
        alertbox.show();
    }
}
