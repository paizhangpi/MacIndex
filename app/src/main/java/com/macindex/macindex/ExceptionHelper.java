package com.macindex.macindex;

import android.app.AlertDialog;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;

class ExceptionHelper {
    public static void handleException(final Exception thisException) {
        thisException.printStackTrace();
    }

    public static void handleExceptionWithDialog(final Context thisContext, final Exception thisException) {
        handleException(thisException);

        final AlertDialog.Builder exceptionDialog = new AlertDialog.Builder(thisContext);
        exceptionDialog.setTitle(R.string.error);
        exceptionDialog.setMessage(R.string.error_information);
        exceptionDialog.setCancelable(false);
        exceptionDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
        });

        final View infoChunk = ((LayoutInflater) thisContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chunk_exception_dialog, null);
        final TextView exceptionInfo = infoChunk.findViewById(R.id.exceptionInfo);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        thisException.printStackTrace(printWriter);

        exceptionInfo.setText(stringWriter.toString());
        exceptionInfo.setMovementMethod(new ScrollingMovementMethod());

        exceptionDialog.setView(infoChunk);
        exceptionDialog.show();
    }
}
