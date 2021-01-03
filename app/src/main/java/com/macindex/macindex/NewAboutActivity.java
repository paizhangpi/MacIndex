package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class NewAboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_about);
        this.setTitle(getResources().getString(R.string.menu_new_about));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final TextView versionText = findViewById(R.id.versionText);
        final Button websiteButton = findViewById(R.id.buttonWebsite);

        versionText.setText(getResources().getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);

        websiteButton.setOnClickListener(unused -> {
            if (Locale.getDefault().getDisplayLanguage().equals("中文")) {
                LinkLoadingHelper.startBrowser("https://macindex.paizhang.info/", NewAboutActivity.this);
            } else {
                LinkLoadingHelper.startBrowser("https://macindex.paizhang.info/v/english/", NewAboutActivity.this);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
