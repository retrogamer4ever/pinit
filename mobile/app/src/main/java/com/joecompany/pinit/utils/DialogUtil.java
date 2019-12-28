package com.joecompany.pinit.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;


public class DialogUtil {

    public static final String DIALOG_ERROR_TITLE = "Error";

    public static void show(Context context, String title, String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void show(Context context, String title, String message, String yesLabel, String noLabel, DialogInterface.OnClickListener yesClickListener, DialogInterface.OnClickListener noClickListener){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(yesLabel, yesClickListener);
        alertDialogBuilder.setNegativeButton(noLabel, noClickListener);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}