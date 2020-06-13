package com.macindex.macindex;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class SettingsAboutActivity extends AppCompatActivity {

    private boolean setEveryMac;
    private boolean setDeathSound;
    private boolean setGestures;
    private boolean setNavButtons;
    private boolean setQuickNav;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_about);
        this.setTitle(getResources().getString(R.string.menu_about_settings));
        initAbout();
        initSettings();
        checkChange();
    }

    @Override
    protected void onDestroy() {
        if (setEveryMac != MainActivity.getPrefs().getBoolean("isOpenEveryMac", false)
                || setDeathSound != MainActivity.getPrefs().getBoolean("isPlayDeathSound", true)
                || setGestures != MainActivity.getPrefs().getBoolean("isUseGestures", true)
                || setNavButtons != MainActivity.getPrefs().getBoolean("isUseNavButtons", false)
                || setQuickNav != MainActivity.getPrefs().getBoolean("isQuickNav", false)) {
            Log.i("Settings", "Settings changed");
            Toast.makeText(this, R.string.setting_saved, Toast.LENGTH_LONG).show();
        }
        super.onDestroy();
    }

    private void initAbout() {
        try {
            // Set Version Text
            TextView versionText = findViewById(R.id.versionText);
            versionText.setText(getResources().getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
            // Set Website Logo clickable
            View aboutView = findViewById(R.id.aboutLayout);
            aboutView.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View unused) {
                    try {
                        AlertDialog.Builder webSiteLuncher = new AlertDialog.Builder(SettingsAboutActivity.this);
                        webSiteLuncher.setTitle(getResources().getString(R.string.information_about_website_title));
                        webSiteLuncher.setMessage(getResources().getString(R.string.information_about_website));
                        webSiteLuncher.setPositiveButton(getResources().getString(R.string.information_about_open), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                Intent browser = new Intent(Intent.ACTION_VIEW);
                                if (Locale.getDefault().getDisplayLanguage().equals("中文")) {
                                    browser.setData(Uri.parse("https://paizhang.info/MacIndexCN"));
                                } else {
                                    browser.setData(Uri.parse("https://paizhang.info/MacIndex"));
                                }
                                startActivity(browser);
                            }
                        });
                        webSiteLuncher.setNegativeButton(getResources().getString(R.string.information_about_close), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                // CANCELLED.
                            }
                        });
                        webSiteLuncher.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            Log.e("initDatabase", "Initialize failed!!");
        }
    }

    private void initSettings() {
        final Switch swEveryMac = findViewById(R.id.switchEveryMac);
        final Switch swDeathSound = findViewById(R.id.switchDeathSound);
        final Switch swGestures = findViewById(R.id.switchGestures);
        final Switch swNavButtons = findViewById(R.id.switchNavButtons);
        final Switch swQuickNav = findViewById(R.id.switchQuickNav);

        setEveryMac = MainActivity.getPrefs().getBoolean("isOpenEveryMac", false);
        setDeathSound = MainActivity.getPrefs().getBoolean("isPlayDeathSound", true);
        setGestures = MainActivity.getPrefs().getBoolean("isUseGestures", true);
        setNavButtons = MainActivity.getPrefs().getBoolean("isUseNavButtons", false);
        setQuickNav = MainActivity.getPrefs().getBoolean("isQuickNav", false);

        swEveryMac.setChecked(setEveryMac);
        swDeathSound.setChecked(setDeathSound);
        swGestures.setChecked(setGestures);
        swNavButtons.setChecked(setNavButtons);
        swQuickNav.setChecked(setQuickNav);

        swEveryMac.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                MainActivity.getPrefs().edit().putBoolean("isOpenEveryMac", isChecked).apply();
                checkChange();
            }
        });
        swDeathSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                MainActivity.getPrefs().edit().putBoolean("isPlayDeathSound", isChecked).apply();
                checkChange();
            }
        });
        swGestures.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                MainActivity.getPrefs().edit().putBoolean("isUseGestures", isChecked).apply();
                checkChange();
            }
        });
        swNavButtons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                MainActivity.getPrefs().edit().putBoolean("isUseNavButtons", isChecked).apply();
                checkChange();
            }
        });
        swQuickNav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                MainActivity.getPrefs().edit().putBoolean("isQuickNav", isChecked).apply();
                checkChange();
            }
        });
    }

    private void checkChange() {
        final Switch swEveryMac = findViewById(R.id.switchEveryMac);
        final Switch swDeathSound = findViewById(R.id.switchDeathSound);
        final Switch swGestures = findViewById(R.id.switchGestures);
        final Switch swNavButtons = findViewById(R.id.switchNavButtons);
        final Switch swQuickNav = findViewById(R.id.switchQuickNav);

        final TextView restoreDefaults = findViewById(R.id.textDefaults);
        if (MainActivity.getPrefs().getBoolean("isOpenEveryMac", false)
                || !MainActivity.getPrefs().getBoolean("isPlayDeathSound", true)
                || !MainActivity.getPrefs().getBoolean("isUseGestures", true)
                || MainActivity.getPrefs().getBoolean("isUseNavButtons", false)
                || MainActivity.getPrefs().getBoolean("isQuickNav", false)) {
            Log.i("Settings", "Restore default available");
            restoreDefaults.setTextColor(getColor(R.color.colorPrimaryDark));
            restoreDefaults.setPaintFlags(restoreDefaults.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            restoreDefaults.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    swEveryMac.setChecked(false);
                    MainActivity.getPrefs().edit().putBoolean("isOpenEveryMac", false).apply();
                    swDeathSound.setChecked(true);
                    MainActivity.getPrefs().edit().putBoolean("isPlayDeathSound", true).apply();
                    swGestures.setChecked(true);
                    MainActivity.getPrefs().edit().putBoolean("isUseGestures", true).apply();
                    swNavButtons.setChecked(false);
                    MainActivity.getPrefs().edit().putBoolean("isUseNavButtons", false).apply();
                    swQuickNav.setChecked(false);
                    MainActivity.getPrefs().edit().putBoolean("isQuickNav", false).apply();
                }
            });
        } else {
            Log.i("Settings", "Restore default not available");
            restoreDefaults.setTextColor(Color.LTGRAY);
            restoreDefaults.setPaintFlags(restoreDefaults.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            restoreDefaults.setOnClickListener(null);
        }
    }
}
