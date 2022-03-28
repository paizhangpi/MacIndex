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

    private boolean isAbleToInitialize = false;

    private boolean isInitialized = false;

    private boolean isAbleToManage = true;

    private MenuItem initialMenuItem = null;

    private MenuItem clearColumnMenuItem = null;

    private MenuItem exchangeColumnMenuItem = null;

    private MenuItem manageListMenuItem = null;

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
        initialMenuItem = menu.findItem(R.id.initCompareItem);
        clearColumnMenuItem = menu.findItem(R.id.clearColumnCompareItem);
        exchangeColumnMenuItem = menu.findItem(R.id.switchCompareItem);
        manageListMenuItem = menu.findItem(R.id.manageCompareItem);
        initialMenuItem.setEnabled(isAbleToInitialize);
        clearColumnMenuItem.setEnabled(isInitialized);
        exchangeColumnMenuItem.setEnabled(isInitialized);
        manageListMenuItem.setEnabled(isAbleToManage);
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
            final LinearLayout initialLayout = findViewById(R.id.initialLayout);
            final TextView emptyText = findViewById(R.id.emptyText);
            final TextView initialText = findViewById(R.id.initialText);

            if (PrefsHelper.getStringPrefs("userCompares", this).split("│").length >= 2) {
                if (!(PrefsHelper.getStringPrefs("userComparesLeft", this).isEmpty())
                        && !(PrefsHelper.getStringPrefs("userComparesRight", this).isEmpty())) {
                    // Load the comparison.
                    initialLayout.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.GONE);
                    // To be implemented
                    setAbleToInitialize(true);
                    setInitialized(true);
                    setAbleToManage(true);
                } else {
                    // Sufficient compare list, but invalid compare parameters.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        initialText.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                    } else {
                        TextViewCompat.setAutoSizeTextTypeWithDefaults(initialText, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                    }
                    initialLayout.setVisibility(View.VISIBLE);
                    emptyLayout.setVisibility(View.GONE);
                    setAbleToInitialize(true);
                    setInitialized(false);
                    setAbleToManage(true);
                }
            } else {
                // Insufficient compare list.
                if (PrefsHelper.getStringPrefs("userCompares", this).isEmpty()) {
                    emptyText.setText(getResources().getStringArray(R.array.compare_insufficient_tips)[0]);
                    setAbleToManage(false);
                } else {
                    emptyText.setText(getResources().getStringArray(R.array.compare_insufficient_tips)[1]);
                    setAbleToManage(true);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    emptyText.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                } else {
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(emptyText, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                }
                initialLayout.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.VISIBLE);
                setAbleToInitialize(false);
                setInitialized(false);
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "initCompare", "Exception occurred. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userCompares", this));
        }
    }

    private void initCompareItem() {
        // Only way to set the left/right parameters.
        try {
            if (PrefsHelper.getStringPrefs("userCompares", this).split("│").length < 2) {
                // Under the new behaviour, this branch should not be taken.
                throw new IllegalAccessException("Should not enter this MenuItem");
            } else {
                // Testing.
                PrefsHelper.editPrefs("userComparesLeft", "Macintosh 128K", this);
                PrefsHelper.editPrefs("userComparesRight", "Macintosh XL", this);
                initCompare();
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "initCompareItem", "Exception occurred. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userCompares", this));
        }
    }

    private void manageList() {
        try {
            if (PrefsHelper.getStringPrefs("userCompares", this).isEmpty()) {
                // Under the new behaviour, this branch should not be taken.
                throw new IllegalAccessException("Should not enter this MenuItem");
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

    private void setAbleToInitialize(final boolean newStatus) {
        Log.i("CompareActivity", "isAbleToInitialize set to " + newStatus);
        isAbleToInitialize = newStatus;
        // Avoid null pointers
        if (initialMenuItem != null) {
            initialMenuItem.setEnabled(newStatus);
        }
    }

    private void setInitialized(final boolean newStatus) {
        Log.i("CompareActivity", "isInitialized set to " + newStatus);
        isInitialized = newStatus;
        // Avoid null pointers
        if (clearColumnMenuItem != null && exchangeColumnMenuItem != null) {
            clearColumnMenuItem.setEnabled(newStatus);
            exchangeColumnMenuItem.setEnabled(newStatus);
        }
    }

    private void setAbleToManage(final boolean newStatus) {
        Log.i("CompareActivity", "isAbleToManage set to " + newStatus);
        isAbleToManage = newStatus;
        // Avoid null pointers
        if (manageListMenuItem != null) {
            manageListMenuItem.setEnabled(newStatus);
        }
    }

    public static void checkIsComparing(final String machineName, final Context thisContext) {
        /* Two comparing parameters should always be valid.
           Thus, when deleting a machine from the list, should perform a checking.
           Note: two comparing parameters do not include brackets.
        */
        Log.i("CompareActivity", "Checking for deletion");
        if (machineName.equals(PrefsHelper.getStringPrefs("userComparesLeft", thisContext))
                || machineName.equals(PrefsHelper.getStringPrefs("userComparesRight", thisContext))
                || PrefsHelper.getStringPrefs("userCompares", thisContext).split("│").length < 2) {
            clearComparing(thisContext);
        }
    }

    private static void clearComparing(final Context thisContext) {
        Log.w("CompareActivity", "Clearing left/right parameters");
        PrefsHelper.clearPrefs("userComparesLeft", thisContext);
        PrefsHelper.clearPrefs("userComparesRight", thisContext);
    }
}
