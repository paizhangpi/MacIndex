package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import android.app.AlertDialog;
import android.content.Context;
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
 * Mar. 29, 2022
 */
public class CompareActivity extends AppCompatActivity {

    private boolean isInitialized = false;

    private MenuItem clearColumnMenuItem = null;

    private MenuItem exchangeColumnMenuItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        this.setTitle(getResources().getString(R.string.menu_compare));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MainActivity.validateOperation(this);

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

        // If CompareActivity Usage is set to not be saved
        if (!(PrefsHelper.getBooleanPrefs("isSaveCompareUsage", this))) {
            clearComparing(this);
        }

        initCompare();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // If reload is needed..
        if (PrefsHelper.getBooleanPrefs("isCompareReloadNeeded", this)) {
            initCompare();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_compare, menu);
        clearColumnMenuItem = menu.getItem(1);
        exchangeColumnMenuItem = menu.getItem(2);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.initCompareItem:
                initCompareItem();
                break;
            case R.id.clearColumnCompareItem:
                clearComparing(this);
                initCompare();
                break;
            case R.id.switchCompareItem:
                final String exchangeTemp = PrefsHelper.getStringPrefs("userComparesLeft", this);
                PrefsHelper.editPrefs("userComparesLeft", PrefsHelper.getStringPrefs("userComparesRight", this), this);
                PrefsHelper.editPrefs("userComparesRight", exchangeTemp, this);
                initCompare();
                break;
            case R.id.manageCompareItem:
                manageList();
                break;
            case R.id.clearCompareItem:
                final AlertDialog.Builder clearWarningDialog = new AlertDialog.Builder(this);
                clearWarningDialog.setTitle(R.string.submenu_compare_clear);
                clearWarningDialog.setMessage(R.string.compare_clear_warning);
                clearWarningDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                    // Deleted, obviously
                    PrefsHelper.clearPrefs("userCompares", this);
                    clearComparing(this);
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
            PrefsHelper.editPrefs("isCompareReloadNeeded", false, this);
            Log.i("initCompare", PrefsHelper.getStringPrefs("userCompares", CompareActivity.this));

            final LinearLayout emptyLayout = findViewById(R.id.emptyLayout);
            final TextView emptyText = findViewById(R.id.emptyText);

            if (PrefsHelper.getStringPrefs("userCompares", this).split("│").length >= 2) {
                if (!(PrefsHelper.getStringPrefs("userComparesLeft", this).isEmpty())
                        && !(PrefsHelper.getStringPrefs("userComparesRight", this).isEmpty())) {
                    emptyLayout.setVisibility(View.GONE);
                    // To be implemented
                    setInitialized(true);
                } else {
                    // Sufficient compare list, but invalid compare parameters.
                    emptyText.setMaxLines(1);
                    emptyText.setText(R.string.compare_not_initialized);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        emptyText.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                    } else {
                        TextViewCompat.setAutoSizeTextTypeWithDefaults(emptyText, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                    }
                    emptyLayout.setVisibility(View.VISIBLE);
                    setInitialized(false);
                }
            } else {
                // Insufficient compare list.
                emptyText.setMaxLines(2);
                if (PrefsHelper.getStringPrefs("userCompares", this).isEmpty()) {
                    emptyText.setText(getResources().getStringArray(R.array.compare_insufficient_tips)[0]);
                } else {
                    emptyText.setText(getResources().getStringArray(R.array.compare_insufficient_tips)[1]);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    emptyText.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                } else {
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(emptyText, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                }
                emptyLayout.setVisibility(View.VISIBLE);
                setInitialized(false);
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "initCompare", "Exception occurred. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userCompares", this));
        }
    }

    private void initCompareItem() {
        // Only way to set the left/right parameters.
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
                            } else {
                                // Deletion
                                checkIsComparing(thisCompareStrings[j], this);
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

    private void setInitialized(final boolean newStatus) {
        Log.i("CompareActivity", "Menuitem availability set to " + newStatus);
        isInitialized = newStatus;
        clearColumnMenuItem.setEnabled(newStatus);
        exchangeColumnMenuItem.setEnabled(newStatus);
    }

    public static void checkIsComparing(final String machineName, final Context thisContext) {
        /* Two comparing parameters should always be valid.
           Thus, when deleting a machine from the list, should perform a checking.
           Note: two comparing parameters do not include brackets.
        */
        Log.i("CompareActivity", "Checking for deletion");
        if (machineName.equals(PrefsHelper.getStringPrefs("userComparesLeft", thisContext))
                || machineName.equals(PrefsHelper.getStringPrefs("userComparesRight", thisContext))) {
            clearComparing(thisContext);
        }
    }

    private static void clearComparing(final Context thisContext) {
        Log.w("CompareActivity", "Clearing left/right parameters");
        PrefsHelper.clearPrefs("userComparesLeft", thisContext);
        PrefsHelper.clearPrefs("userComparesRight", thisContext);
    }
}
