package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.widget.TextView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import java.text.DateFormat;
import java.util.Date;

public class NewAboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_about);

        this.setTitle(getResources().getString(R.string.menu_about));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MainActivity.validateOperation(this);

        try {
            // Get build time information
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
            Date buildDate = new Date();
            buildDate.setTime(BuildConfig.TIMESTAMP);

            String versionString = getString(R.string.version_information_general) + " " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")\n" +
                    getString(R.string.version_information_releasedate) + " " + dateFormat.format(buildDate) + "\n" +
                    getString(R.string.version_information_models) + " " + MainActivity.getMachineHelper().getMachineCount();
            if (BuildConfig.DEBUG) {
                versionString = versionString.concat("\n\nPRERELEASE. Internal Evaluations Only.");
            }
            ((TextView) findViewById(R.id.versionText)).setText(versionString);

            findViewById(R.id.appNameText).setOnClickListener(v -> {
                LinkLoadingHelper.startBrowser("https://macindex.paizhang.info/v/english/",
                        "https://macindex.paizhang.info/", this);
            });
            findViewById(R.id.appLogo).setOnClickListener(v -> {
                LinkLoadingHelper.startBrowser("https://macindex.paizhang.info/v/english/",
                        "https://macindex.paizhang.info/", this);
            });
            findViewById(R.id.importantButton).setOnClickListener(v -> {
                LinkLoadingHelper.startBrowser("https://macindex.paizhang.info/v/english/important-information",
                        "https://macindex.paizhang.info/important-information", this);
            });
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.about_opensource));
            findViewById(R.id.openSourceButton).setOnClickListener(v -> {
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
            });
            findViewById(R.id.updateButton).setOnClickListener(v -> {
                LinkLoadingHelper.startBrowser(null, "https://macindex.paizhang.info/download-and-update-history", this);
            });
            findViewById(R.id.feedbackButton).setOnClickListener(v -> {
                LinkLoadingHelper.startBrowser("https://macindex.paizhang.info/v/english/feedback",
                        "https://macindex.paizhang.info/feedback-and-evaluation", this);
            });
            findViewById(R.id.githubLogo).setOnClickListener(v -> {
                LinkLoadingHelper.startBrowser("https://github.com/paizhangpi/MacIndex",
                        "https://github.com/paizhangpi/MacIndex", this);
            });
            findViewById(R.id.cs125Logo).setOnClickListener(v -> {
                LinkLoadingHelper.startBrowser(null, "https://macindex.paizhang.info/story-of-macindex", this);
            });
            findViewById(R.id.paizhangLogo).setOnClickListener(v -> {
                LinkLoadingHelper.startBrowser(null, "https://paizhang.info/", this);
            });
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "NewAboutActivity", "Failed to fetch information.");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}