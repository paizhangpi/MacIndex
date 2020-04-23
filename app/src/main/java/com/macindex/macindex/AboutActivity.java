package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        this.setTitle(getResources().getString(R.string.about));
        TextView debugText = findViewById(R.id.debugText);
        if (MainActivity.isDebug()) {
            debugText.setText(getResources().getString(R.string.err_debug_mode));
        }
    }
}
