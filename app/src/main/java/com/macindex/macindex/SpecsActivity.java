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
            new AlertDialog.Builder(this)
                    .setMessage(this.getResources().getString(R.string.err_intent_invalid))
                    .setNegativeButton(this.getResources().getString(R.string.quit), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            finishAffinity();
                        } })
                    .setTitle(this.getResources().getString(R.string.error)).setCancelable(false).show();
        }
    }

    private void initSpecs() {
        // Initialize TextView for each data category. Update necessary.
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
            public void onClick(View v) {
                loadLinks();
            }
        });
    }

    private void loadLinks() {
        AlertDialog.Builder linkDialog = new AlertDialog.Builder(this);

        // Setup each option in dialog.
        View linkChunk = getLayoutInflater().inflate(R.layout.chunk_links, null);
        final RadioGroup linkOptions = linkChunk.findViewById(R.id.option);
        // GET links AT HERE
        final String[] linkGroup = intent.getStringExtra("links").split(";");
        for (int i = 0; i < linkGroup.length; i++) {
            RadioButton linkOption = new RadioButton(this);
            linkOption.setText(linkGroup[i].split(",")[0]);
            linkOption.setId(i);
            linkOptions.addView(linkOption);
        }

        // When user tapped confirm or cancel...
        linkDialog.setPositiveButton("@string/processor", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int checkedOption = linkOptions.getCheckedRadioButtonId();
                if (checkedOption != -1) {
                    Intent browser = new Intent(Intent.ACTION_VIEW);
                    browser.setData(Uri.parse(linkGroup[checkedOption].split(",")[1]));
                    startActivity(browser);
                }
            }
        });
        linkDialog.setNegativeButton("@string/cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // CANCELLED
            }
        });
    }
}
