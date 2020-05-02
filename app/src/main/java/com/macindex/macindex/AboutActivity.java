package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        this.setTitle(getResources().getString(R.string.about));
        TextView versionText = findViewById(R.id.versionText);
        ImageView websiteImage = findViewById(R.id.websiteLogo);
        versionText.setText(BuildConfig.VERSION_NAME);
        websiteImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View unused) {
                try {
                    Intent browser = new Intent(Intent.ACTION_VIEW);
                    if (Locale.getDefault().getDisplayLanguage().equals("中文")) {
                        browser.setData(Uri.parse("https://paizhang.info/MacIndexCN"));
                    } else {
                        browser.setData(Uri.parse("https://paizhang.info/MacIndex"));
                    }
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.link_opening), Toast.LENGTH_LONG).show();
                    startActivity(browser);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
