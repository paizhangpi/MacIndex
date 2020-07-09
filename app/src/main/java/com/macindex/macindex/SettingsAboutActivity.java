package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class SettingsAboutActivity extends AppCompatActivity {

    private final SharedPreferences prefs = MainActivity.getPrefs();

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

            textWebsite.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View unused) {
                    Intent browser = new Intent(Intent.ACTION_VIEW);
                    if (Locale.getDefault().getDisplayLanguage().equals("中文")) {
                        browser.setData(Uri.parse("https://paizhang.info/MacIndexCN"));
                    } else {
                        browser.setData(Uri.parse("https://paizhang.info/MacIndex"));
                    }
                    startActivity(browser);
                }
            });

            restoreDefaults.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final AlertDialog.Builder defaultsWarningDialog = new AlertDialog.Builder(SettingsAboutActivity.this);
                    defaultsWarningDialog.setTitle(R.string.setting_defaults_warning_title);
                    defaultsWarningDialog.setMessage(R.string.setting_defaults_warning_content);
                    defaultsWarningDialog.setPositiveButton(R.string.link_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialogInterface, final int i) {
                            prefs.edit().clear().apply();
                            Toast.makeText(SettingsAboutActivity.this, R.string.setting_defaults_cleared, Toast.LENGTH_LONG).show();
                            finishAffinity();
                        }
                    });
                    defaultsWarningDialog.setNegativeButton(R.string.link_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialogInterface, final int i) {
                            // Cancelled, nothing to do.
                        }
                    });
                    defaultsWarningDialog.show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    private void initSettings() {
        final Switch swEveryMac = findViewById(R.id.switchEveryMac);
        final Switch swDeathSound = findViewById(R.id.switchDeathSound);
        final Switch swGestures = findViewById(R.id.switchGestures);
        final Switch swNavButtons = findViewById(R.id.switchNavButtons);
        final Switch swQuickNav = findViewById(R.id.switchQuickNav);
        final Switch swRandomAll = findViewById(R.id.switchRandomAll);

        swEveryMac.setChecked(prefs.getBoolean("isOpenEveryMac", false));
        swDeathSound.setChecked(prefs.getBoolean("isPlayDeathSound", true));
        swGestures.setChecked(prefs.getBoolean("isUseGestures", true));
        swNavButtons.setChecked(prefs.getBoolean("isUseNavButtons", false));
        swQuickNav.setChecked(prefs.getBoolean("isQuickNav", false));
        swRandomAll.setChecked(prefs.getBoolean("isRandomAll", false));

        swEveryMac.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.edit().putBoolean("isOpenEveryMac", isChecked).apply();
            }
        });
        swDeathSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.edit().putBoolean("isPlayDeathSound", isChecked).apply();
            }
        });
        swGestures.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.edit().putBoolean("isUseGestures", isChecked).apply();
            }
        });
        swNavButtons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.edit().putBoolean("isUseNavButtons", isChecked).apply();
            }
        });
        swQuickNav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.edit().putBoolean("isQuickNav", isChecked).apply();
            }
        });
        swRandomAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.edit().putBoolean("isRandomAll", isChecked).apply();
            }
        });
    }
}
