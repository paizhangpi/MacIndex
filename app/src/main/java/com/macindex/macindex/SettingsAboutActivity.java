package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Switch;

public class SettingsAboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_about);
        this.setTitle(getResources().getString(R.string.menu_about_settings));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_prefs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clearPrefsItem:
                final AlertDialog.Builder defaultsWarningDialog = new AlertDialog.Builder(SettingsAboutActivity.this);
                defaultsWarningDialog.setTitle(R.string.submenu_prefs_clear);
                defaultsWarningDialog.setMessage(R.string.setting_defaults_warning_content);
                defaultsWarningDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                    PrefsHelper.clearPrefs(this);
                });
                defaultsWarningDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
                    // Cancelled, nothing to do.
                });
                defaultsWarningDialog.show();
                break;
            case R.id.prefsHelpItem:
                LinkLoadingHelper.startBrowser(null, "https://macindex.paizhang.info/settings-activity", this);
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

    private void initSettings() {
        final Switch swSort = findViewById(R.id.switchSort);
        final Switch swSortComment = findViewById(R.id.switchSortComment);
        final Switch swEveryMac = findViewById(R.id.switchEveryMac);
        final Switch swDeathSound = findViewById(R.id.switchDeathSound);
        final Switch swNavButtons = findViewById(R.id.switchNavButtons);
        final Switch swQuickNav = findViewById(R.id.switchQuickNav);
        final Switch swRandomAll = findViewById(R.id.switchRandomAll);
        final Switch swSaveMainUsage = findViewById(R.id.switchSaveMainUsage);
        final Switch swSaveSearchUsage = findViewById(R.id.switchSaveSearchUsage);
        final Switch swSaveCompareUsage = findViewById(R.id.switchSaveCompareUsage);
        final Switch swVolWarning = findViewById(R.id.switchVolWarning);

        swSort.setChecked(PrefsHelper.getBooleanPrefs("isSortAgain", this));
        swSortComment.setChecked(PrefsHelper.getBooleanPrefs("isSortComment", this));
        final Boolean everyMacSelection = PrefsHelper.getBooleanPrefs("isOpenEveryMac", this);
        swEveryMac.setChecked(everyMacSelection);
        swDeathSound.setChecked(PrefsHelper.getBooleanPrefs("isPlayDeathSound", this));
        swNavButtons.setChecked(PrefsHelper.getBooleanPrefs("isUseNavButtons", this));
        swQuickNav.setChecked(PrefsHelper.getBooleanPrefs("isFixedNav", this));
        swRandomAll.setChecked(PrefsHelper.getBooleanPrefs("isRandomAll", this));
        swSaveMainUsage.setChecked(PrefsHelper.getBooleanPrefs("isSaveMainUsage", this));
        swSaveSearchUsage.setChecked(PrefsHelper.getBooleanPrefs("isSaveSearchUsage", this));
        swSaveCompareUsage.setChecked(PrefsHelper.getBooleanPrefs("isSaveCompareUsage", this));
        swVolWarning.setChecked(PrefsHelper.getBooleanPrefs("isEnableVolWarning", this));

        swSort.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    PrefsHelper.editPrefs("isSortAgain", isChecked, this);
                    PrefsHelper.editPrefs("isReloadNeeded", true, this);
                });
        swSortComment.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isSortComment", isChecked, this));
        swDeathSound.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isPlayDeathSound", isChecked, this));
        swNavButtons.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isUseNavButtons", isChecked, this));
        swQuickNav.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isFixedNav", isChecked, this));
        swRandomAll.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isRandomAll", isChecked, this));
        swSaveMainUsage.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isSaveMainUsage", isChecked, this));
        swSaveSearchUsage.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isSaveSearchUsage", isChecked, this));
        swSaveCompareUsage.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isSaveCompareUsage", isChecked, this));
        swVolWarning.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isEnableVolWarning", isChecked, this));

        // If EveryMac is checked, disable following settings.
        if (everyMacSelection) {
            swSortComment.setEnabled(false);
            swDeathSound.setEnabled(false);
            swNavButtons.setEnabled(false);
            swQuickNav.setEnabled(false);
            swRandomAll.setEnabled(false);
            swVolWarning.setEnabled(false);
            swSaveCompareUsage.setEnabled(false);
        } else {
            swSortComment.setEnabled(true);
            swDeathSound.setEnabled(true);
            swNavButtons.setEnabled(true);
            swQuickNav.setEnabled(true);
            swRandomAll.setEnabled(true);
            swVolWarning.setEnabled(true);
            swSaveCompareUsage.setEnabled(true);
        }

        swEveryMac.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                final AlertDialog.Builder everyMacWarningDialog = new AlertDialog.Builder(SettingsAboutActivity.this);
                everyMacWarningDialog.setTitle(R.string.setting_everymac);
                everyMacWarningDialog.setMessage(R.string.setting_everymac_warning_content);
                everyMacWarningDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                    PrefsHelper.editPrefs("isOpenEveryMac", true, this);
                    initSettings();
                });
                everyMacWarningDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> swEveryMac.setChecked(false));
                everyMacWarningDialog.show();
            } else {
                PrefsHelper.editPrefs("isOpenEveryMac", false, this);
                initSettings();
            }
        });
    }
}
