package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FavouriteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        this.setTitle(getResources().getString(R.string.menu_favourite));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (!isEmptyString(R.string.menu_favourite)) {
            initFavourites();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // If reload is needed..
        if (PrefsHelper.getBooleanPrefs("isFavouritesReloadNeeded", this)) {
            initFavourites();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_favourite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addFolderItem:
                createFolder();
                break;
            case R.id.deleteFolderItem:
                deleteFolder();
                break;
            case R.id.renameFolderItem:
                renameFolder();
                break;
            case R.id.favouriteHelpItem:
                LinkLoadingHelper.startBrowser(null, "https://macindex.paizhang.info/favourites", this);
                break;
            case R.id.clearFolderItem:
                final AlertDialog.Builder clearFoldersDialog = new AlertDialog.Builder(this);
                clearFoldersDialog.setTitle(R.string.submenu_favourite_clear);
                clearFoldersDialog.setMessage(R.string.favourites_clear_warning);
                clearFoldersDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                    PrefsHelper.clearPrefs("userFavourites", this);
                    initFavourites();
                });
                clearFoldersDialog.setNegativeButton(R.string.link_cancel, ((dialogInterface, i) -> {
                    // Cancelled.
                }));
                clearFoldersDialog.show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void initFavourites() {
        // To be implemented
        // Reset reload parameter
        PrefsHelper.editPrefs("isFavouritesReloadNeeded", false, this);
        Log.e("initFavourites", PrefsHelper.getStringPrefs("userFavourites", FavouriteActivity.this));
    }

    // Called when to check the favourites is empty.
    private boolean isEmptyString(final int titleRes) {
        if (PrefsHelper.getStringPrefs("userFavourites", this).isEmpty()) {
            final AlertDialog.Builder emptyStringDialog = new AlertDialog.Builder(this);
            emptyStringDialog.setTitle(titleRes);
            emptyStringDialog.setMessage(R.string.favourites_no_folder);
            emptyStringDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                // Create new folder
                createFolder();
            });
            emptyStringDialog.setNegativeButton(R.string.link_cancel, ((dialogInterface, i) -> {
                // Cancelled, do nothing
            }));
            emptyStringDialog.show();
            return true;
        } else {
            return false;
        }
    }

    public static String[] getFolders(final Context thisContext) {
        try {
            String[] splitedString = PrefsHelper.getStringPrefs("userFavourites", thisContext).split("││");
            String[] toReturn = new String[splitedString.length - 1];
            for (int i = 1; i < splitedString.length; i++) {
                if (splitedString[i].isEmpty()) {
                    Log.e("getFolders", "Invalid non-trailing empty string");
                    throw new IllegalStateException();
                }
                String[] tempSplit = splitedString[i].split("│");
                toReturn[i - 1] = tempSplit[0].substring(1, tempSplit[0].length() - 1);
            }
            return toReturn;
        } catch (Exception e) {
            ExceptionHelper.handleException(thisContext, e, "getFolders", "Illegal Favourites String. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userFavourites", thisContext));
            return new String[0];
        }
    }

    public static boolean validateFolderName(final String inputtedName, final String[] currentStrings, final Context thisContext) {
        if (inputtedName.isEmpty()) {
            Log.w("validateFolderName", "Empty input.");
            Toast.makeText(thisContext, R.string.favourites_error_empty, Toast.LENGTH_LONG).show();
            return false;
        } else if (inputtedName.contains("│")) {
            Log.w("validateFolderName", "Illegal Character Detected.");
            Toast.makeText(thisContext, R.string.favourites_error_illegal, Toast.LENGTH_LONG).show();
            return false;
        } else if (inputtedName.length() > 30 || inputtedName.contains("\n")) {
            Log.w("validateFolderName", "Input is too long.");
            Toast.makeText(thisContext, R.string.favourites_error_length, Toast.LENGTH_LONG).show();
            return false;
        } else {
            // Check if specified
            for (String toCheck : currentStrings) {
                if (toCheck.equals(inputtedName)) {
                    Log.w("validateFolderName", "Conflict - Specified.");
                    Toast.makeText(thisContext, R.string.favourites_error_conflict, Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        return true;
    }

    private void createFolder() {
        // Check for folder count
        final String[] currentStrings = getFolders(this);
        if (currentStrings.length >= 10) {
            final AlertDialog.Builder folderLimitDialog = new AlertDialog.Builder(this);
            folderLimitDialog.setTitle(R.string.submenu_favourite_add);
            folderLimitDialog.setMessage(R.string.favourites_error_limit);
            folderLimitDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                // Confirmed
            });
            folderLimitDialog.show();
        } else {
            final View newFolderChunk = getLayoutInflater().inflate(R.layout.chunk_favourites_new, null);
            final EditText folderName = newFolderChunk.findViewById(R.id.folderName);
            final AlertDialog.Builder newFolderDialog = new AlertDialog.Builder(this);
            newFolderDialog.setTitle(R.string.submenu_favourite_add);
            newFolderDialog.setMessage(R.string.favourites_new_folder);
            newFolderDialog.setView(newFolderChunk);
            newFolderDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                // To be overwritten...
            });
            newFolderDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
                // Do nothing
            });

            final AlertDialog newFolderDialogCreated = newFolderDialog.create();
            newFolderDialogCreated.show();
            // Overwrite the positive button
            newFolderDialogCreated.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                try {
                    final String inputtedName = folderName.getText().toString().trim();
                    // Check if the input is legal
                    if (validateFolderName(inputtedName, currentStrings, this)) {
                        // Finally create the new folder.
                        PrefsHelper.editPrefs("userFavourites", "││{"
                                + inputtedName + "}" + PrefsHelper.getStringPrefs("userFavourites", this), this);
                        initFavourites();
                        newFolderDialogCreated.dismiss();
                    }
                } catch (Exception e) {
                    ExceptionHelper.handleException(FavouriteActivity.this, e, "newFolderDialog", "Illegal Favourites String. Please reset the application. String is: "
                            + PrefsHelper.getStringPrefs("userFavourites", FavouriteActivity.this));
                }
            });
        }
    }

    private void deleteFolder() {
        try {
            // Check if totally empty.
            if (!isEmptyString(R.string.submenu_favourite_delete)) {
                final View selectChunk = this.getLayoutInflater().inflate(R.layout.chunk_favourites_select, null);
                final LinearLayout selectLayout = selectChunk.findViewById(R.id.selectLayout);
                final String[] currentStrings = getFolders(this);
                final int[] currentSelections = new int[currentStrings.length];
                for (int i = 0; i < currentStrings.length; i++) {
                    CheckBox thisCheckBox = new CheckBox(this);
                    thisCheckBox.setText(currentStrings[i]);
                    thisCheckBox.setChecked(false);
                    int finalI = i;
                    thisCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
                        currentSelections[finalI] = thisCheckBox.isChecked() ? 1 : 0;
                    });
                    selectLayout.addView(thisCheckBox);
                }

                // Create the dialog.
                final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
                deleteDialog.setTitle(R.string.submenu_favourite_delete);
                deleteDialog.setMessage(R.string.favourites_delete);
                deleteDialog.setView(selectChunk);
                deleteDialog.setPositiveButton(R.string.link_confirm, (dialog, which) -> {
                    try {
                        // Delete the folders.
                        String[] splitedString = PrefsHelper.getStringPrefs("userFavourites", this).split("││");
                        String newString = "";
                        for (int j = 1; j < splitedString.length; j++) {
                            if (currentSelections[j - 1] == 0) {
                                newString = newString.concat("││" + splitedString[j]);
                            }
                        }
                        PrefsHelper.editPrefs("userFavourites", newString, this);
                        initFavourites();
                    } catch (Exception e) {
                        ExceptionHelper.handleException(this, e, "deleteFolderConfirm", "Illegal Favourites String. Please reset the application. String is: "
                                + PrefsHelper.getStringPrefs("userFavourites", this));
                    }
                });
                deleteDialog.setNegativeButton(R.string.link_cancel, ((dialog, which) -> {
                    // Cancelled, do nothing
                }));
                deleteDialog.show();
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "deleteFolder", "Illegal Favourites String. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userFavourites", this));
        }
    }

    private void renameFolder() {
        // To be implemented
    }
}
