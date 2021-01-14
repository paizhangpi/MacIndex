package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * MacIndex Comment Activity
 * Jan. 13, 2021
 */
public class CommentActivity extends AppCompatActivity {

    private int[] machineIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        this.setTitle(getResources().getString(R.string.menu_comment));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Reset reload parameter
        PrefsHelper.editPrefs("isCommentsReloadNeeded", false, this);

        // Check whether if the string is empty on creation.
        if (PrefsHelper.getStringPrefs("userComments", this).length() == 0) {
            final AlertDialog.Builder nullWarningDialog = new AlertDialog.Builder(this);
            nullWarningDialog.setTitle(R.string.menu_comment);
            nullWarningDialog.setMessage(R.string.comments_not_available);
            nullWarningDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                // Confirmed.
            });
            nullWarningDialog.show();
        }
        initComments();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // If reload is needed..
        if (PrefsHelper.getBooleanPrefs("isCommentsReloadNeeded", this)) {
            initComments();
            PrefsHelper.editPrefs("isCommentsReloadNeeded", false, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_comment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clearCommentsItem:
                final AlertDialog.Builder clearWarningDialog = new AlertDialog.Builder(this);
                clearWarningDialog.setTitle(R.string.submenu_comments_clear);
                clearWarningDialog.setMessage(R.string.comments_clear_warning);
                clearWarningDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                    PrefsHelper.clearPrefs("userComments", this);
                    initComments();
                });
                clearWarningDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
                    // Cancelled, nothing to do.
                });
                clearWarningDialog.show();
                break;
            case R.id.commentHelpItem:
                LinkLoadingHelper.startBrowser(null, "https://macindex.paizhang.info/comments", this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void initComments() {
        try {
            // Init Container...
            final LinearLayout commentContainer = findViewById(R.id.commentContainer);
            LayoutTransition layoutTransition = commentContainer.getLayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            commentContainer.removeAllViews();

            if (PrefsHelper.getStringPrefs("userComments", this).length() != 0) {
                String[] thisCommentsStrings = PrefsHelper.getStringPrefs("userComments", this).split("││");
                machineIDs = new int[thisCommentsStrings.length];
                ProgressDialog waitDialog = new ProgressDialog(this);
                waitDialog.setMessage(getString(R.string.loading));
                waitDialog.setCancelable(false);
                waitDialog.show();
                new Thread() {
                    @Override
                    public void run() {
                        // Run searches on the separate thread.
                        for (int i = 0; i < thisCommentsStrings.length; i++) {
                            String[] splitedThisString = thisCommentsStrings[i].split("│");
                            int[] thisID = MainActivity.getMachineHelper().searchHelper("name", splitedThisString[0],
                                    "all", CommentActivity.this, true);
                            if (thisID.length != 1) {
                                Log.e("CommentSearchThread", "Error occurred on search string " + splitedThisString[0]);
                            }
                            machineIDs[i] = thisID[0];
                        }

                        // Is sorting needed?
                        if (PrefsHelper.getBooleanPrefs("isSortComment", CommentActivity.this)) {
                            machineIDs = MainActivity.getMachineHelper().directSortByYear(machineIDs);
                        }
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    waitDialog.dismiss();
                                    // Update the UI after the thread done.
                                    for (int i = 0; i < machineIDs.length; i++) {
                                        final View commentsChunk = getLayoutInflater().inflate(R.layout.chunk_comments, null);
                                        final TextView machineName = commentsChunk.findViewById(R.id.machineName);
                                        final TextView machineComment = commentsChunk.findViewById(R.id.machineComment);
                                        final LinearLayout commentChunk = commentsChunk.findViewById(R.id.comment_chunk);

                                        // Set Machine Info Accordingly
                                        if (PrefsHelper.getBooleanPrefs("isSortComment", CommentActivity.this)) {
                                            // Something complex here
                                            final String thisName = MainActivity.getMachineHelper().getName(machineIDs[i]);
                                            machineName.setText(thisName);
                                            for (String thisString : thisCommentsStrings) {
                                                if (thisString.split("│")[0].equals(thisName)) {
                                                    machineComment.setText(thisString.split("│")[1]);
                                                    break;
                                                }
                                            }
                                        } else {
                                            String[] splitedThisString = thisCommentsStrings[i].split("│");
                                            if (splitedThisString.length != 2) {
                                                throw new IllegalStateException();
                                            }
                                            machineName.setText(splitedThisString[0]);
                                            machineComment.setText(splitedThisString[1]);
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            machineName.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                                        } else {
                                            TextViewCompat.setAutoSizeTextTypeWithDefaults(machineName, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                                        }

                                        int finalI = i;
                                        commentChunk.setOnClickListener(view -> {
                                            SpecsIntentHelper.sendIntent(machineIDs, machineIDs[finalI], CommentActivity.this);
                                        });
                                        commentChunk.setOnLongClickListener(view -> {
                                            ClipboardManager clipboard = (ClipboardManager) CommentActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clip = ClipData.newPlainText("userComment", machineComment.getText());
                                            clipboard.setPrimaryClip(clip);
                                            Toast.makeText(CommentActivity.this, MainActivity.getRes().getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                                            return true;
                                        });

                                        commentContainer.addView(commentsChunk);
                                    }
                                    Log.i("CommentSearchThread", thisCommentsStrings.length + " Machines loaded in the container.");
                                }
                            });
                        } catch (final Exception e) {
                            ExceptionHelper.handleException(CommentActivity.this, e, "CommentSearchThread", "Cannot add children to container. Likely illegal comment prefs string. Please reset the application. String is: "
                                    + PrefsHelper.getStringPrefs("userComments", CommentActivity.this));
                        }
                    }
                }.start();
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, null, null);
        }
    }
}
