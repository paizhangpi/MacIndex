package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
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
        Toast.makeText(getApplicationContext(),
                getResources().getString(R.string.setting_saved), Toast.LENGTH_LONG).show();
    }

    private void initAbout() {
        // Set Version Text
        TextView versionText = findViewById(R.id.versionText);
        versionText.setText(getResources().getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
        // Set Website Logo clickable
        ImageView websiteImage = findViewById(R.id.websiteLogo);
        websiteImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View unused) {
                try {
                    Intent browser = new Intent(Intent.ACTION_VIEW);
                    if (Locale.getDefault().getDisplayLanguage().equals("中文")) {
                        browser.setData(Uri.parse("https://paizhang.info/MacIndexCN"));
                    } else {
                        browser.setData(Uri.parse("https://paizhang.info/MacIndex"));
                    }
                    startActivity(browser);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initSettings() {
        Switch swEveryMac = findViewById(R.id.switchEveryMac);
        Switch swDeathSound = findViewById(R.id.switchDeathSound);
        Switch swGestures = findViewById(R.id.switchGestures);
        Switch swNavButtons = findViewById(R.id.switchNavButtons);

        swEveryMac.setChecked(MainActivity.getPrefs().getBoolean("isOpenEveryMac", false));
        swDeathSound.setChecked(MainActivity.getPrefs().getBoolean("isPlayDeathSound", true));
        swGestures.setChecked(MainActivity.getPrefs().getBoolean("isUseGestures", true));
        swNavButtons.setChecked(MainActivity.getPrefs().getBoolean("isUseNavButtons", false));

        swEveryMac.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                MainActivity.getPrefsEditor().putBoolean("isOpenEveryMac", isChecked);
                MainActivity.getPrefsEditor().commit();
            }
        });
        swDeathSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                MainActivity.getPrefsEditor().putBoolean("isPlayDeathSound", isChecked);
                MainActivity.getPrefsEditor().commit();
            }
        });
        swGestures.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                MainActivity.getPrefsEditor().putBoolean("isUseGestures", isChecked);
                MainActivity.getPrefsEditor().commit();
            }
        });
        swNavButtons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                MainActivity.getPrefsEditor().putBoolean("isUseNavButtons", isChecked);
                MainActivity.getPrefsEditor().commit();
            }
        });
    }
}
