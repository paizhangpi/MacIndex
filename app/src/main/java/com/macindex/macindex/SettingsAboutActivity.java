package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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

            textWebsite.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View unused) {
                    Intent browser = new Intent(Intent.ACTION_VIEW);
                    browser.setData(Uri.parse("https://paizhang.info/MacIndex"));
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
                            prefs.clearPrefs();
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

        final Boolean everyMacSelection = prefs.getBooleanPrefs("isOpenEveryMac");
        swEveryMac.setChecked(everyMacSelection);
        swDeathSound.setChecked(prefs.getBooleanPrefs("isPlayDeathSound"));
        swGestures.setChecked(prefs.getBooleanPrefs("isUseGestures"));
        swNavButtons.setChecked(prefs.getBooleanPrefs("isUseNavButtons"));
        swQuickNav.setChecked(prefs.getBooleanPrefs("isQuickNav"));
        swRandomAll.setChecked(prefs.getBooleanPrefs("isRandomAll"));

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

        swEveryMac.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    final AlertDialog.Builder everyMacWarningDialog = new AlertDialog.Builder(SettingsAboutActivity.this);
                    everyMacWarningDialog.setTitle(R.string.setting_defaults_warning_title);
                    everyMacWarningDialog.setMessage(R.string.setting_everymac_warning_content);
                    everyMacWarningDialog.setPositiveButton(R.string.link_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialogInterface, final int i) {
                            prefs.editPrefs("isOpenEveryMac", isChecked);
                            initSettings();
                        }
                    });
                    everyMacWarningDialog.setNegativeButton(R.string.link_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialogInterface, final int i) {
                            swEveryMac.setChecked(false);
                        }
                    });
                    everyMacWarningDialog.show();
                } else {
                    prefs.editPrefs("isOpenEveryMac", isChecked);
                    initSettings();
                }
            }
        });
        swDeathSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.editPrefs("isPlayDeathSound", isChecked);
            }
        });
        swGestures.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.editPrefs("isUseGestures", isChecked);
            }
        });
        swNavButtons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.editPrefs("isUseNavButtons", isChecked);
            }
        });
        swQuickNav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.editPrefs("isQuickNav", isChecked);
            }
        });
        swRandomAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.editPrefs("isRandomAll", isChecked);
            }
        });
    }
}
