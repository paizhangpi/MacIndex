package com.macindex.macindex;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.Locale;

public class SettingsAboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_about);
        this.setTitle(getResources().getString(R.string.menu_about_settings));
        initAbout();
        initSettings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initAbout() {
        // Set Version Text
        TextView versionText = findViewById(R.id.versionText);
        versionText.setText(getResources().getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
        // Set Total Machine Text
        TextView totalMachineText = findViewById(R.id.totalMachinesText);
        totalMachineText.setText(getResources().getString(R.string.total_1) + MainActivity.getMachineHelper().getMachineCount() + " / "
                + MainActivity.getMachineHelper().getConfigCount() + getResources().getString(R.string.total_2));
        // Set Website Logo clickable
        View aboutView = findViewById(R.id.aboutLayout);
        aboutView.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View unused) {
                try {
                    AlertDialog.Builder webSiteLuncher = new AlertDialog.Builder(SettingsAboutActivity.this);
                    webSiteLuncher.setTitle(getResources().getString(R.string.information_about_website_title));
                    webSiteLuncher.setMessage(getResources().getString(R.string.information_about_website));
                    webSiteLuncher.setPositiveButton(getResources().getString(R.string.link_confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent browser = new Intent(Intent.ACTION_VIEW);
                            if (Locale.getDefault().getDisplayLanguage().equals("中文")) {
                                browser.setData(Uri.parse("https://paizhang.info/MacIndexCN"));
                            } else {
                                browser.setData(Uri.parse("https://paizhang.info/MacIndex"));
                            }
                            startActivity(browser);
                        }
                    });
                    webSiteLuncher.setNegativeButton(getResources().getString(R.string.link_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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
    }

    private void initSettings() {
        final Switch swEveryMac = findViewById(R.id.switchEveryMac);
        final Switch swDeathSound = findViewById(R.id.switchDeathSound);
        final Switch swGestures = findViewById(R.id.switchGestures);
        final Switch swNavButtons = findViewById(R.id.switchNavButtons);
        final Button restoreDefaults = findViewById(R.id.button_defaults);

        swEveryMac.setChecked(MainActivity.getPrefs().getBoolean("isOpenEveryMac", false));
        swDeathSound.setChecked(MainActivity.getPrefs().getBoolean("isPlayDeathSound", true));
        swGestures.setChecked(MainActivity.getPrefs().getBoolean("isUseGestures", true));
        swNavButtons.setChecked(MainActivity.getPrefs().getBoolean("isUseNavButtons", false));

        swEveryMac.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                MainActivity.getPrefs().edit().putBoolean("isOpenEveryMac", isChecked).apply();
            }
        });
        swDeathSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                MainActivity.getPrefs().edit().putBoolean("isPlayDeathSound", isChecked).apply();
            }
        });
        swGestures.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                MainActivity.getPrefs().edit().putBoolean("isUseGestures", isChecked).apply();
            }
        });
        swNavButtons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                MainActivity.getPrefs().edit().putBoolean("isUseNavButtons", isChecked).apply();
            }
        });
        restoreDefaults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swEveryMac.setChecked(false);
                MainActivity.getPrefs().edit().putBoolean("isOpenEveryMac", false).apply();
                swDeathSound.setChecked(true);
                MainActivity.getPrefs().edit().putBoolean("isPlayDeathSound", true).apply();
                swGestures.setChecked(true);
                MainActivity.getPrefs().edit().putBoolean("isUseGestures", true).apply();
                swNavButtons.setChecked(false);
                MainActivity.getPrefs().edit().putBoolean("isUseNavButtons", false).apply();
            }
        });
    }
}
