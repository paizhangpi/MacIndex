package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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

import org.w3c.dom.Text;

import java.util.Locale;

public class SettingsAboutActivity extends AppCompatActivity {

    private SharedPreferences prefs = MainActivity.getPrefs();

    private boolean setEveryMac;

    private boolean setDeathSound;

    private boolean setGestures;

    private boolean setNavButtons;

    private boolean setQuickNav;

    private boolean setRandomAll;

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
        if (setEveryMac != prefs.getBoolean("isOpenEveryMac", false)
                || setDeathSound != prefs.getBoolean("isPlayDeathSound", true)
                || setGestures != prefs.getBoolean("isUseGestures", true)
                || setNavButtons != prefs.getBoolean("isUseNavButtons", false)
                || setQuickNav != prefs.getBoolean("isQuickNav", false)
                || setRandomAll != prefs.getBoolean("isRandomAll", false)) {
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
            TextView textWebsite = findViewById(R.id.textWebsite);
            textWebsite.setTextColor(getColor(R.color.colorPrimaryDark));
            textWebsite.setPaintFlags(textWebsite.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
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

        setEveryMac = prefs.getBoolean("isOpenEveryMac", false);
        setDeathSound = prefs.getBoolean("isPlayDeathSound", true);
        setGestures = prefs.getBoolean("isUseGestures", true);
        setNavButtons = prefs.getBoolean("isUseNavButtons", false);
        setQuickNav = prefs.getBoolean("isQuickNav", false);
        setRandomAll = prefs.getBoolean("isRandomAll", false);

        swEveryMac.setChecked(setEveryMac);
        swDeathSound.setChecked(setDeathSound);
        swGestures.setChecked(setGestures);
        swNavButtons.setChecked(setNavButtons);
        swQuickNav.setChecked(setQuickNav);
        swRandomAll.setChecked(setRandomAll);

        swEveryMac.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.edit().putBoolean("isOpenEveryMac", isChecked).apply();
                checkChange();
            }
        });
        swDeathSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.edit().putBoolean("isPlayDeathSound", isChecked).apply();
                checkChange();
            }
        });
        swGestures.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.edit().putBoolean("isUseGestures", isChecked).apply();
                checkChange();
            }
        });
        swNavButtons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.edit().putBoolean("isUseNavButtons", isChecked).apply();
                checkChange();
            }
        });
        swQuickNav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.edit().putBoolean("isQuickNav", isChecked).apply();
                checkChange();
            }
        });
        swRandomAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                prefs.edit().putBoolean("isRandomAll", isChecked).apply();
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
        final Switch swRandomAll = findViewById(R.id.switchRandomAll);

        final TextView restoreDefaults = findViewById(R.id.textDefaults);
        if (prefs.getBoolean("isOpenEveryMac", false)
                || !prefs.getBoolean("isPlayDeathSound", true)
                || !prefs.getBoolean("isUseGestures", true)
                || prefs.getBoolean("isUseNavButtons", false)
                || prefs.getBoolean("isQuickNav", false)
                || prefs.getBoolean("isRandomAll", false)) {
            Log.i("Settings", "Restore default available");
            restoreDefaults.setTextColor(getColor(R.color.colorPrimaryDark));
            restoreDefaults.setPaintFlags(restoreDefaults.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            restoreDefaults.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    swEveryMac.setChecked(false);
                    prefs.edit().putBoolean("isOpenEveryMac", false).apply();
                    swDeathSound.setChecked(true);
                    prefs.edit().putBoolean("isPlayDeathSound", true).apply();
                    swGestures.setChecked(true);
                    prefs.edit().putBoolean("isUseGestures", true).apply();
                    swNavButtons.setChecked(false);
                    prefs.edit().putBoolean("isUseNavButtons", false).apply();
                    swQuickNav.setChecked(false);
                    prefs.edit().putBoolean("isQuickNav", false).apply();
                    swRandomAll.setChecked(false);
                    prefs.edit().putBoolean("isRandomAll", false).apply();
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
