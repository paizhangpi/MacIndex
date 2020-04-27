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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;

public class SpecsActivity extends AppCompatActivity {

    private Intent intent;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specs);
        try {
            intent = getIntent();
            this.setTitle(intent.getStringExtra("name"));
            initSpecs();
            initImage();
            initLinks();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
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
        int soundID = SoundHelper.getSound(intent.getStringExtra("sound"));
        if (soundID != 0) {
            final MediaPlayer sound = MediaPlayer.create(this, soundID);
            image.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View unused) {
                    sound.start();
                }
            });
        }
    }

    private void initLinks() {
        Button link = findViewById(R.id.linkButton);
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
                                // CANCELLED
                            }
                        });
                linkDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    private void startBrowser(final String url) {
        Intent browser = new Intent(Intent.ACTION_VIEW);
        browser.setData(Uri.parse(url));
        Toast.makeText(getApplicationContext(),
                getResources().getString(R.string.link_opening), Toast.LENGTH_LONG).show();
        startActivity(browser);
    }
}
