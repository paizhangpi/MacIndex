package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class SpecsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("thing", "called new spec");
        setContentView(R.layout.activity_specs);
        try {
            Intent intent = getIntent();
            this.setTitle(intent.getStringExtra("name"));
            // Initialize TextView for each data category. Update necessary.
            TextView name = findViewById(R.id.nameText);
            TextView processor = findViewById(R.id.processorText);
            TextView maxram = findViewById(R.id.maxramText);
            TextView year = findViewById(R.id.yearText);
            TextView model = findViewById(R.id.modelText);
            ImageView image = findViewById(R.id.pic);

            name.setText(intent.getStringExtra("name"));
            processor.setText(intent.getStringExtra("processor"));
            maxram.setText(intent.getStringExtra("maxram"));
            year.setText(intent.getStringExtra("year"));
            model.setText(intent.getStringExtra("model"));

            String path = intent.getStringExtra("path");
            File file = new File(path);
            if (file.exists()) {
                Log.i("thing", "file exists");
                image.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
            }
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialog.Builder(this).setMessage("One or more intent argument is illegal.\n\n" +
                    "For additional information, please refer to GitHub readme.")
                    .setNegativeButton("DISMISS", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                        }}).setTitle("Warning").setCancelable(false).show();
        }
    }
}
