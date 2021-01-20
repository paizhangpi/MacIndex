package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * MacIndex Compare Activity
 * Jan. 18, 2021
 */
public class CompareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        this.setTitle(getResources().getString(R.string.menu_compare));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Check and build the sufficient dialog.
        if (PrefsHelper.getStringPrefs("userCompares", this).split("│").length < 2) {
            final AlertDialog.Builder insufficientDialog = new AlertDialog.Builder(this);
            insufficientDialog.setTitle(R.string.menu_compare);
            insufficientDialog.setMessage(R.string.compare_insufficient);
            insufficientDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                // Confirmed.
            });
            insufficientDialog.show();
        }
        initCompare();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_compare, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.manageCompareItem:
                manageList();
                break;
            case R.id.clearCompareItem:
                final AlertDialog.Builder clearWarningDialog = new AlertDialog.Builder(this);
                clearWarningDialog.setTitle(R.string.submenu_compare_clear);
                clearWarningDialog.setMessage(R.string.compare_clear_warning);
                clearWarningDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                    PrefsHelper.clearPrefs("userCompares", this);
                    initCompare();
                });
                clearWarningDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
                    // Cancelled, nothing to do.
                });
                clearWarningDialog.show();
                break;
            case R.id.compareHelpItem:
                LinkLoadingHelper.startBrowser(null, "https://macindex.paizhang.info/compare", this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void initCompare() {
        try {
            Log.i("initCompare", PrefsHelper.getStringPrefs("userCompares", CompareActivity.this));

            final LinearLayout emptyLayout = findViewById(R.id.emptyLayout);
            final TextView emptyText = findViewById(R.id.emptyText);

            if (PrefsHelper.getStringPrefs("userCompares", this).split("│").length >= 2) {
                emptyLayout.setVisibility(View.GONE);
                // To be implemented
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    emptyText.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                } else {
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(emptyText, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                }
                emptyLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "initCompare", "Exception occurred. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userCompares", this));
        }
    }

    private void manageList() {
        try {
            // Check for empty
            if (PrefsHelper.getStringPrefs("userCompares", this).isEmpty()) {
                final AlertDialog.Builder insufficientDialog = new AlertDialog.Builder(this);
                insufficientDialog.setTitle(R.string.submenu_compare_manage);
                insufficientDialog.setMessage(R.string.compare_empty);
                insufficientDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                    // Confirmed.
                });
                insufficientDialog.show();
            } else {
                final View selectChunk = this.getLayoutInflater().inflate(R.layout.chunk_favourites_select, null);
                final LinearLayout selectLayout = selectChunk.findViewById(R.id.selectLayout);
                final String[] thisCompareStrings = PrefsHelper.getStringPrefs("userCompares", this).split("│");
                final int[] currentSelections = new int[thisCompareStrings.length];
                for (int i = 0; i < thisCompareStrings.length; i++) {
                    CheckBox thisCheckBox = new CheckBox(this);
                    thisCheckBox.setText(thisCompareStrings[i].substring(1, thisCompareStrings[i].length() - 1));
                    thisCheckBox.setChecked(false);
                    int finalI = i;
                    thisCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
                        currentSelections[finalI] = thisCheckBox.isChecked() ? 1 : 0;
                    });
                    selectLayout.addView(thisCheckBox);
                }

                // Create the dialog.
                final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
                deleteDialog.setTitle(R.string.submenu_compare_manage);
                deleteDialog.setMessage(R.string.compare_manage);
                deleteDialog.setView(selectChunk);
                deleteDialog.setPositiveButton(R.string.link_confirm, (dialog, which) -> {
                    try {
                        // Delete the folders.
                        String newString = "";
                        for (int j = 0; j < thisCompareStrings.length; j++) {
                            if (currentSelections[j] == 0) {
                                newString = newString.concat("│" + thisCompareStrings[j]);
                            }
                        }
                        if (!newString.isEmpty()) {
                            newString = newString.substring(1);
                        }
                        PrefsHelper.editPrefs("userCompares", newString, this);
                        initCompare();
                    } catch (Exception e) {
                        ExceptionHelper.handleException(this, e, "manageListConfirm", "Exception occurred. Please reset the application. String is: "
                                + PrefsHelper.getStringPrefs("userCompares", this));
                    }
                });
                deleteDialog.setNegativeButton(R.string.link_cancel, ((dialog, which) -> {
                    // Cancelled, do nothing
                }));
                deleteDialog.show();
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "manageList", "Exception occurred. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userCompares", this));
        }
    }
}
