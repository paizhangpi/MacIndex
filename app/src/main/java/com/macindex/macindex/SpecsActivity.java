package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;

public class SpecsActivity extends AppCompatActivity {

    private Intent intent;

    private boolean startup = true;

    private boolean isOpenEveryMac = false;

    private MediaPlayer startupSound = null;

    private MediaPlayer deathSound = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specs);
        try {
            intent = getIntent();
            isOpenEveryMac = intent.getBooleanExtra("isOpenEveryMac", false);
            this.setTitle(intent.getStringExtra("name"));
            initSpecs();
            initImage();
            initLinks();
            if (isOpenEveryMac) {
                Log.i("SpecsOnCreate", "isOpenEveryMac Checked!");
                loadLinks();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (startupSound != null && startupSound.isPlaying()) {
            startupSound.stop();
            startupSound.release();
        }
        if (deathSound != null && deathSound.isPlaying()) {
            deathSound.stop();
            deathSound.release();
        }
        super.onDestroy();
    }

    private void initSpecs() {
        TextView name = findViewById(R.id.nameText);
        TextView processor = findViewById(R.id.processorText);
        TextView maxram = findViewById(R.id.maxramText);
        TextView year = findViewById(R.id.yearText);
        TextView model = findViewById(R.id.modelText);

        name.setText(intent.getStringExtra("name"));
        processor.setText(intent.getStringExtra("processor"));
        maxram.setText(intent.getStringExtra("maxram"));
        year.setText(intent.getStringExtra("year"));
        model.setText(intent.getStringExtra("model"));
    }

    private void initImage() {
        ImageView image = findViewById(R.id.pic);
        String path = intent.getStringExtra("path");
        File file = new File(path);
        if (file.exists()) {
            Log.i("SpecsAct", "Image exists");
            image.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
        }
        file.delete();
        String soundID = intent.getStringExtra("sound");
        int startupID = SoundHelper.getSound(soundID);
        int deathID = SoundHelper.getDeathSound(soundID);
        TextView informationLabel = findViewById(R.id.information);
        if (startupID != 0 && deathID != 0) {
            // Startup sound exists, death sound exists
            informationLabel.setText(getResources().getString(R.string.information_specs_full));
            startupSound = MediaPlayer.create(this, startupID);
            deathSound = MediaPlayer.create(this, deathID);
            image.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View unused) {
                    if (!startupSound.isPlaying() && !deathSound.isPlaying()) {
                        if (startup) {
                            startupSound.start();
                            startup = false;
                        } else {
                            deathSound.start();
                            startup = true;
                        }
                    }
                }
            });
        } else if (startupID != 0) {
            // Startup sound exists, death sound not exist
            informationLabel.setText(getResources().getString(R.string.information_specs_no_death));
            startupSound = MediaPlayer.create(this, startupID);
            image.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View unused) {
                    startupSound.start();
                }
            });
        } else {
            // Exception for PowerBook DuoDock...
            informationLabel.setVisibility(View.GONE);
        }
    }

    private void initLinks() {
        ImageView link = findViewById(R.id.everymac);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                loadLinks();
            }
        });
    }

    private void loadLinks() {
        try {
            final String[] linkGroup = intent.getStringExtra("links").split(";");
            if (linkGroup.length == 1) {
                startBrowser(linkGroup[0].split(",")[1]);
            } else {
                AlertDialog.Builder linkDialog = new AlertDialog.Builder(this);
                linkDialog.setMessage(getResources().getString(R.string.link_message));
                // Setup each option in dialog.
                View linkChunk = getLayoutInflater().inflate(R.layout.chunk_links, null);
                final RadioGroup linkOptions = linkChunk.findViewById(R.id.option);
                for (int i = 0; i < linkGroup.length; i++) {
                    RadioButton linkOption = new RadioButton(this);
                    linkOption.setText(linkGroup[i].split(",")[0]);
                    linkOption.setId(i);
                    if (i == 0) {
                        linkOption.setChecked(true);
                    }
                    linkOptions.addView(linkOption);
                }
                linkDialog.setView(linkChunk);

                // When user tapped confirm or cancel...
                linkDialog.setPositiveButton(this.getResources().getString(R.string.link_confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                startBrowser(linkGroup[linkOptions.getCheckedRadioButtonId()]
                                        .split(",")[1]);
                            }
                        });
                linkDialog.setNegativeButton(this.getResources().getString(R.string.link_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                if (isOpenEveryMac) {
                                    finish();
                                }
                            }
                        });
                linkDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            Log.e("loadLinks", "Link loading failed!!");
        }
    }

    private void startBrowser(final String url) {
        try {
            Intent browser = new Intent(Intent.ACTION_VIEW);
            browser.setData(Uri.parse(url));
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.link_opening), Toast.LENGTH_LONG).show();
            startActivity(browser);
            if (isOpenEveryMac) {
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }
}
