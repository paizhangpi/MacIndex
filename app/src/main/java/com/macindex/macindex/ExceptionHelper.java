package com.macindex.macindex;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;

class ExceptionHelper {

    public static void handleExceptionWithDialog(final Context thisContext, final Exception thisException) {
        final String exceptionInfo = "MacIndex Version: " + BuildConfig.VERSION_NAME + "\n"
                + "Android Version: " + Build.VERSION.RELEASE + "\n"
                + "Hardware Brand: " + Build.BRAND + "\n"
                + "Hardware Model: " + Build.MODEL + "\n"
                + "Logging Not Applicable" + "\n"
                + "\n" +"Exception Details:" + "\n" + getStackTrace(thisException);
        thisException.printStackTrace();
        handleExceptionDialog(thisContext, exceptionInfo);
    }

    public static void handleExceptionWithDialog(final Context thisContext, final Exception thisException,
                                                 final String exceptionModule, final String exceptionMessage) {
        final String exceptionInfo = "MacIndex Version: " + BuildConfig.VERSION_NAME + "\n"
                + "Android Version: " + Build.VERSION.RELEASE + "\n"
                + "Hardware Brand: " + Build.BRAND + "\n"
                + "Hardware Model: " + Build.MODEL + "\n"
                + "Log Tag: " + exceptionModule + "\n"
                + "Log Message: " + exceptionMessage +"\n"
                + "\n" +"Exception Details:" + "\n" + getStackTrace(thisException);
        thisException.printStackTrace();
        Log.e(exceptionModule, exceptionMessage);
        handleExceptionDialog(thisContext, exceptionInfo);
    }

    private static void handleExceptionDialog(final Context thisContext, final String exceptionInfo) {
        final AlertDialog.Builder exceptionDialog = new AlertDialog.Builder(thisContext);
        exceptionDialog.setTitle(R.string.error);
        exceptionDialog.setMessage(R.string.error_information);
        exceptionDialog.setCancelable(false);
        exceptionDialog.setPositiveButton(R.string.error_dismiss, (dialogInterface, i) -> {
        });
        exceptionDialog.setNegativeButton(R.string.error_copy_button, (dialogInterface, i) -> {
        });

        final View infoChunk = ((LayoutInflater) thisContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chunk_exception_dialog, null);
        final TextView exceptionInfoBox = infoChunk.findViewById(R.id.exceptionInfo);

        exceptionInfoBox.setText(exceptionInfo);
        exceptionDialog.setView(infoChunk);

        final AlertDialog exceptionDialogCreated = exceptionDialog.create();
        exceptionDialogCreated.show();

        // Override the negative button
        exceptionDialogCreated.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) thisContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ExceptionInfo", exceptionInfo);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(thisContext,
                    MainActivity.getRes().getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
        });
    }

    private static String getStackTrace(final Exception thisException) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        thisException.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
