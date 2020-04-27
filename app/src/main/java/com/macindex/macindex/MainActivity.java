package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * MacIndex
 * University of Illinois, CS125 FA19 Final Project
 * https://paizhang.info/MacIndex
 */
public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDatabase();
        initInterface();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == R.id.aboutMenu) {
            Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        } else if (item.getItemId() == R.id.searchMenu) {
            Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(searchIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initDatabase() {
        try {
            File dbFilePath = new File(this.getApplicationInfo().dataDir + "/databases/specs.db");
            File dbFolder = new File(this.getApplicationInfo().dataDir + "/databases");
            dbFilePath.delete();
            dbFolder.delete();
            dbFolder.mkdir();
            InputStream inputStream = this.getAssets().open("specs.db");
            OutputStream outputStream = new FileOutputStream(dbFilePath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            DatabaseOpenHelper dbHelper = new DatabaseOpenHelper(this);
            database = dbHelper.getReadableDatabase();
            Log.i("initDatabase","Initialized successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    private void initInterface() {
        for (int i = 0; i <= 9; i++) {
            final LinearLayout currentLayout = findViewById(CategoryHelper.getLayout(i));
            for (int j = 0; j < currentLayout.getChildCount(); j++) {
                View v = currentLayout.getChildAt(j);
                v.setClickable(true);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        int visa = 0;

                        for (int j = 0; j < currentLayout.getChildCount(); j++) {
                            View vi = currentLayout.getChildAt(j);
                            if (vi.getVisibility() == View.VISIBLE) {
                                visa++;
                            }
                        }

                        if (visa > 2) {
                            for (int j = 2; j < currentLayout.getChildCount(); j++) {
                                View vi = currentLayout.getChildAt(j);
                                vi.setVisibility(View.GONE);
                            }
                        } else {
                            for (int j = 2; j < currentLayout.getChildCount(); j++) {
                                View vi = currentLayout.getChildAt(j);
                                vi.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
            initCategory(currentLayout, i);
        }
        // Basic functionality was finished on 16:12 CST, Dec 2, 2019.
    }

    private void initCategory(final LinearLayout currentLayout, final int category) {
        try {
            Log.i("initCategory", "Initializing Category " + category);
            Cursor cursor = database.query("category" + category, null,
                    null, null, null, null, null);
            while (cursor.moveToNext()) {

                View mainChunk = getLayoutInflater().inflate(R.layout.chunk_main, null);
                mainChunk.setVisibility(View.GONE);
                TextView machineName = mainChunk.findViewById(R.id.machineName);
                TextView machineYear = mainChunk.findViewById(R.id.machineYear);

                // Create a String for each data category. Update here.
                final String thisName = cursor.getString(cursor.getColumnIndex("name"));
                final String thisSound = cursor.getString(cursor.getColumnIndex("sound"));
                final String thisProcessor = cursor.getString(cursor.getColumnIndex("processor"));
                final String thisMaxRAM = cursor.getString(cursor.getColumnIndex("maxram"));
                final String thisYear = cursor.getString(cursor.getColumnIndex("year"));
                final String thisModel = cursor.getString(cursor.getColumnIndex("model"));
                final byte[] thisBlob = cursor.getBlob(cursor.getColumnIndex("pic"));
                final String thisLinks = cursor.getString(cursor.getColumnIndex("links"));

                machineName.setText(thisName);
                machineYear.setText(thisYear);

                machineName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View unused) {
                        sendIntent(thisName, thisSound, thisProcessor,
                                thisMaxRAM, thisYear, thisModel, thisBlob, thisLinks);
                    }
                });

                machineYear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View unused) {
                        sendIntent(thisName, thisSound, thisProcessor,
                                thisMaxRAM, thisYear, thisModel, thisBlob, thisLinks);
                    }
                });
                currentLayout.addView(mainChunk);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    public void sendIntent(final String thisName, final String thisSound, final String thisProcessor,
                                  final String thisMaxRAM, final String thisYear, final String thisModel,
                                  final byte[] thisBlob, final String thisLinks) {
        Intent intent = new Intent(MainActivity.this, SpecsActivity.class);
        intent.putExtra("name", thisName);
        intent.putExtra("sound", thisSound);
        intent.putExtra("processor", thisProcessor);
        intent.putExtra("maxram", thisMaxRAM);
        intent.putExtra("year", thisYear);
        intent.putExtra("model", thisModel);
        intent.putExtra("links", thisLinks);

        String path = null;
        if (thisBlob != null) {
            Bitmap pic = BitmapFactory.decodeByteArray(thisBlob, 0, thisBlob.length);
            Log.i("sendIntent", "Converted blob to bitmap");
            try {
                File file = File.createTempFile("tempF", ".tmp");
                try (FileOutputStream out = new FileOutputStream(file, false)) {
                    pic.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
                path = file.getPath();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        }
        intent.putExtra("path", path);
        startActivity(intent);
    }
}
