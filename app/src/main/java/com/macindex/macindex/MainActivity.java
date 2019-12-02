package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDatabase();
        database.query("category0", null, null, null, null, null, null, null);
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
}
