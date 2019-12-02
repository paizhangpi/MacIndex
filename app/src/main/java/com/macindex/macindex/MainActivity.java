package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * MacIndexer Android application and Specs database
 * University of Illinois, CS125 FA19 Final Project
 *
 *
 */
public class MainActivity extends AppCompatActivity {


    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDatabase();
        initInterface();
    }

    private void initDatabase() {
        String appDataPath = this.getApplicationInfo().dataDir;
        File dbFolder = new File(appDataPath + "/databases");
        dbFolder.mkdir();
        File dbFilePath = new File(appDataPath + "/databases/specs.db");
        try {
            InputStream inputStream = this.getAssets().open("specs.db");
            OutputStream outputStream = new FileOutputStream(dbFilePath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0)
            {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e){
            // To catch
        }
        DatabaseOpenHelper dbHelper = new DatabaseOpenHelper(this);
        database = dbHelper.getReadableDatabase();
    }

    private void initInterface() {
        View mainChunk = getLayoutInflater().inflate(R.layout.chunk_main, null);
        TextView machineName = mainChunk.findViewById(R.id.machineName);
        Button viewButton = mainChunk.findViewById(R.id.viewButton);
        for (int i = 0; i < 1; i++) {
            LinearLayout currentLayout = findViewById(R.id.category0Layout);
            Cursor cursor = database.query("category" + String.valueOf(i), new String[]{"name"},
                    null, null, null, null, null);
            while (cursor.moveToNext()) {
                machineName.setText(cursor.getString(cursor.getColumnIndex("name")));
                currentLayout.addView(mainChunk);
            }
        }
    }
}
