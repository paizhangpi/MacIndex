package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsAboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_about);
        this.setTitle(getResources().getString(R.string.menu_about_settings));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initAbout();
        initSettings();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void initAbout() {
        try {
            final Button restoreDefaults = findViewById(R.id.buttonDefaults);
            final Button invalidate = findViewById(R.id.buttonInvalidate);

            restoreDefaults.setOnClickListener(v -> {
                final AlertDialog.Builder defaultsWarningDialog = new AlertDialog.Builder(SettingsAboutActivity.this);
                defaultsWarningDialog.setTitle(R.string.setting_defaults_warning_title);
                defaultsWarningDialog.setMessage(R.string.setting_defaults_warning_content);
                defaultsWarningDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                    PrefsHelper.clearPrefs(this);
                    Toast.makeText(SettingsAboutActivity.this, R.string.setting_defaults_cleared, Toast.LENGTH_LONG).show();
                    finishAffinity();
                });
                defaultsWarningDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
                    // Cancelled, nothing to do.
                });
                defaultsWarningDialog.show();
            });

            invalidate.setOnClickListener(v -> {
                final AlertDialog.Builder defaultsWarningDialog = new AlertDialog.Builder(SettingsAboutActivity.this);
                defaultsWarningDialog.setTitle(R.string.setting_defaults_warning_title);
                defaultsWarningDialog.setMessage(R.string.setting_invalidate_warning_content);
                defaultsWarningDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                    PrefsHelper.invalidatePrefs(this);
                    Toast.makeText(SettingsAboutActivity.this, R.string.setting_defaults_cleared, Toast.LENGTH_LONG).show();
                    finishAffinity();
                });
                defaultsWarningDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
                    // Cancelled, nothing to do.
                });
                defaultsWarningDialog.show();
            });
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "AboutInit", "About info initialization failed");
        }
    }

    private void initSettings() {
        final Switch swEveryMac = findViewById(R.id.switchEveryMac);
        final Switch swDeathSound = findViewById(R.id.switchDeathSound);
        final Switch swGestures = findViewById(R.id.switchGestures);
        final Switch swNavButtons = findViewById(R.id.switchNavButtons);
        final Switch swQuickNav = findViewById(R.id.switchQuickNav);
        final Switch swRandomAll = findViewById(R.id.switchRandomAll);
        final Switch swSaveMainUsage = findViewById(R.id.switchSaveMainUsage);
        final Switch swSaveSearchUsage = findViewById(R.id.switchSaveSearchUsage);
        final Switch swVolWarning = findViewById(R.id.switchVolWarning);

        final Boolean everyMacSelection = PrefsHelper.getBooleanPrefs("isOpenEveryMac", this);
        swEveryMac.setChecked(everyMacSelection);
        swDeathSound.setChecked(PrefsHelper.getBooleanPrefs("isPlayDeathSound", this));
        swGestures.setChecked(PrefsHelper.getBooleanPrefs("isUseGestures", this));
        swNavButtons.setChecked(PrefsHelper.getBooleanPrefs("isUseNavButtons", this));
        swQuickNav.setChecked(PrefsHelper.getBooleanPrefs("isQuickNav", this));
        swRandomAll.setChecked(PrefsHelper.getBooleanPrefs("isRandomAll", this));
        swSaveMainUsage.setChecked(PrefsHelper.getBooleanPrefs("isSaveMainUsage", this));
        swSaveSearchUsage.setChecked(PrefsHelper.getBooleanPrefs("isSaveSearchUsage", this));
        swVolWarning.setChecked(PrefsHelper.getBooleanPrefs("isEnableVolWarning", this));

        swDeathSound.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isPlayDeathSound", isChecked, this));
        swGestures.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isUseGestures", isChecked, this));
        swNavButtons.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isUseNavButtons", isChecked, this));
        swQuickNav.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isQuickNav", isChecked, this));
        swRandomAll.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isRandomAll", isChecked, this));
        swSaveMainUsage.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isSaveMainUsage", isChecked, this));
        swSaveSearchUsage.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isSaveSearchUsage", isChecked, this));
        swVolWarning.setOnCheckedChangeListener((buttonView, isChecked) -> PrefsHelper.editPrefs("isEnableVolWarning", isChecked, this));

        // If EveryMac is checked, disable following settings.
        if (everyMacSelection) {
            swDeathSound.setEnabled(false);
            swGestures.setEnabled(false);
            swNavButtons.setEnabled(false);
            swQuickNav.setEnabled(false);
            swRandomAll.setEnabled(false);
            swVolWarning.setEnabled(false);
        } else {
            swDeathSound.setEnabled(true);
            swGestures.setEnabled(true);
            swNavButtons.setEnabled(true);
            swQuickNav.setEnabled(true);
            swRandomAll.setEnabled(true);
            swVolWarning.setEnabled(true);
        }

        swEveryMac.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                final AlertDialog.Builder everyMacWarningDialog = new AlertDialog.Builder(SettingsAboutActivity.this);
                everyMacWarningDialog.setTitle(R.string.setting_defaults_warning_title);
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
