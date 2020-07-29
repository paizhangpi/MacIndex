package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class SettingsAboutActivity extends AppCompatActivity {

    private final PrefsHelper prefs = MainActivity.getPrefs();

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
            final TextView versionText = findViewById(R.id.versionText);
            final Button textWebsite = findViewById(R.id.buttonWebsite);
            final Button restoreDefaults = findViewById(R.id.buttonDefaults);

            versionText.setText(getResources().getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);

            textWebsite.setOnClickListener(unused -> {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(ContextCompat.getColor(SettingsAboutActivity.this, R.color.colorPrimary));
                CustomTabsIntent customTabsIntent = builder.build();
                if (Locale.getDefault().getDisplayLanguage().equals("中文")) {
                    customTabsIntent.launchUrl(SettingsAboutActivity.this, Uri.parse("https://paizhang.info/MacIndex"));
                } else {
                    customTabsIntent.launchUrl(SettingsAboutActivity.this, Uri.parse("https://paizhang.info/MacIndex/2"));
                }
            });

            restoreDefaults.setOnClickListener(v -> {
                final AlertDialog.Builder defaultsWarningDialog = new AlertDialog.Builder(SettingsAboutActivity.this);
                defaultsWarningDialog.setTitle(R.string.setting_defaults_warning_title);
                defaultsWarningDialog.setMessage(R.string.setting_defaults_warning_content);
                defaultsWarningDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                    prefs.clearPrefs();
                    Toast.makeText(SettingsAboutActivity.this, R.string.setting_defaults_cleared, Toast.LENGTH_LONG).show();
                    finishAffinity();
                });
                defaultsWarningDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
                    // Cancelled, nothing to do.
                });
                defaultsWarningDialog.show();
            });
        } catch (Exception e) {
            ExceptionHelper.handleExceptionWithDialog(this, e);
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

        final Boolean everyMacSelection = prefs.getBooleanPrefs("isOpenEveryMac");
        swEveryMac.setChecked(everyMacSelection);
        swDeathSound.setChecked(prefs.getBooleanPrefs("isPlayDeathSound"));
        swGestures.setChecked(prefs.getBooleanPrefs("isUseGestures"));
        swNavButtons.setChecked(prefs.getBooleanPrefs("isUseNavButtons"));
        swQuickNav.setChecked(prefs.getBooleanPrefs("isQuickNav"));
        swRandomAll.setChecked(prefs.getBooleanPrefs("isRandomAll"));
        swSaveMainUsage.setChecked(prefs.getBooleanPrefs("isSaveMainUsage"));
        swSaveSearchUsage.setChecked(prefs.getBooleanPrefs("isSaveSearchUsage"));

        // If EveryMac is checked, disable following settings.
        if (everyMacSelection) {
            swDeathSound.setEnabled(false);
            swGestures.setEnabled(false);
            swNavButtons.setEnabled(false);
            swQuickNav.setEnabled(false);
            swRandomAll.setEnabled(false);
        } else {
            swDeathSound.setEnabled(true);
            swGestures.setEnabled(true);
            swNavButtons.setEnabled(true);
            swQuickNav.setEnabled(true);
            swRandomAll.setEnabled(true);
        }

        swEveryMac.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                final AlertDialog.Builder everyMacWarningDialog = new AlertDialog.Builder(SettingsAboutActivity.this);
                everyMacWarningDialog.setTitle(R.string.setting_defaults_warning_title);
                everyMacWarningDialog.setMessage(R.string.setting_everymac_warning_content);
                everyMacWarningDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                    prefs.editPrefs("isOpenEveryMac", true);
                    initSettings();
                });
                everyMacWarningDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> swEveryMac.setChecked(false));
                everyMacWarningDialog.show();
            } else {
                prefs.editPrefs("isOpenEveryMac", false);
                initSettings();
            }
        });
        swDeathSound.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.editPrefs("isPlayDeathSound", isChecked));
        swGestures.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.editPrefs("isUseGestures", isChecked));
        swNavButtons.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.editPrefs("isUseNavButtons", isChecked));
        swQuickNav.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.editPrefs("isQuickNav", isChecked));
        swRandomAll.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.editPrefs("isRandomAll", isChecked));
        swSaveMainUsage.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.editPrefs("isSaveMainUsage", isChecked));
        swSaveSearchUsage.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.editPrefs("isSaveSearchUsage", isChecked));
    }
}
