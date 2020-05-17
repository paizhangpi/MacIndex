package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

/**
 * MacIndex/2.
 * University of Illinois, CS125 FA19 Final Project
 * University of Illinois, CS199 Kotlin SP20 Final Project
 * https://paizhang.info/MacIndexCN
 * https://paizhang.info/MacIndex
 * https://github.com/paizhangpi/MacIndex
 */
public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database;

    private static MachineHelper machineHelper;

    private static SharedPreferences prefs = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("MACINDEX_PREFS", Activity.MODE_PRIVATE);
        initDatabase();
        initInterface();
    }

    @Override
    protected void onDestroy() {
        machineHelper.suicide();
        database.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        MenuItem aboutMenu = menu.findItem(R.id.aboutMenu);
        aboutMenu.setTitle(getResources().getString(R.string.menu_about_settings));
        MenuItem searchMenu = menu.findItem(R.id.searchMenu);
        searchMenu.setEnabled(false);
        searchMenu.setTitle(getResources().getString(R.string.search));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aboutMenu:
                Intent aboutIntent = new Intent(MainActivity.this, SettingsAboutActivity.class);
                startActivity(aboutIntent);
                return true;
            case R.id.searchMenu:
                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(searchIntent);
                return true;
            case R.id.randomMenu:
                openRandom();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

            // Open MachineHelper
            machineHelper = new MachineHelper(database);
            if (!machineHelper.selfCheck()) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            Log.e("initDatabase", "Initialize failed!!");
        }
    }

    private void initInterface() {
        // Parent layout of all categories.
        final LinearLayout categoryContainer = findViewById(R.id.categoryContainer);
        for (int i = 0; i <= machineHelper.getCategoryTotalCount(); i++) {
            final View categoryChunk = getLayoutInflater().inflate(R.layout.chunk_category, null);
            final LinearLayout categoryChunkLayout = categoryChunk.findViewById(R.id.categoryInfoLayout);
            final TextView categoryName = categoryChunk.findViewById(R.id.category);
            final TextView categoryDescription = categoryChunk.findViewById(R.id.description);
            categoryName.setText(getResources().getString(machineHelper.getCategoryName(i)));
            categoryDescription.setText(getResources().getString(machineHelper.getCategoryDescription(i)));

            for (int j = 0; j < categoryChunkLayout.getChildCount(); j++) {
                View v = categoryChunkLayout.getChildAt(j);
                v.setClickable(true);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        int visa = 0;

                        for (int j = 0; j < categoryChunkLayout.getChildCount(); j++) {
                            View vi = categoryChunkLayout.getChildAt(j);
                            if (vi.getVisibility() == View.VISIBLE) {
                                visa++;
                            }
                        }

                        if (visa > 2) {
                            for (int j = 2; j < categoryChunkLayout.getChildCount(); j++) {
                                View vi = categoryChunkLayout.getChildAt(j);
                                vi.setVisibility(View.GONE);
                            }
                        } else {
                            for (int j = 2; j < categoryChunkLayout.getChildCount(); j++) {
                                View vi = categoryChunkLayout.getChildAt(j);
                                vi.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
            initCategory(categoryChunkLayout, i);
            categoryContainer.addView(categoryChunk);
        }
        Log.i("InitInterface", machineHelper.getMachineCount() + " loaded");
        TextView totalMachineText = findViewById(R.id.totalMachinesText);
        totalMachineText.setText(getResources().getString(R.string.total_1) + machineHelper.getMachineCount() + " / "
                + machineHelper.getConfigCount() + getResources().getString(R.string.total_2));
        // Basic functionality was finished on 16:12 CST, Dec 2, 2019.
    }

    private void initCategory(final LinearLayout currentLayout, final int category) {
        try {
            for (int i = 0; i < machineHelper.getCategoryCount(category); i++) {
                View mainChunk = getLayoutInflater().inflate(R.layout.chunk_main, null);
                mainChunk.setVisibility(View.GONE);
                TextView machineName = mainChunk.findViewById(R.id.machineName);
                TextView machineYear = mainChunk.findViewById(R.id.machineYear);

                // Adapt MachineHelper.
                final int[] position = {category, i};
                final int machineID = machineHelper.findByPosition(position);

                // Find information necessary for interface.
                final String thisName = machineHelper.getName(machineID);
                final String thisYear = machineHelper.getYear(machineID);
                final String thisLinks = machineHelper.getConfig(machineID);

                machineName.setText(thisName);
                machineYear.setText(thisYear);

                machineName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View unused) {
                        if (prefs.getBoolean("isOpenEveryMac", false)) {
                            loadLinks(thisName, thisLinks);
                        } else {
                            sendIntent(machineID);
                        }
                    }
                });

                machineYear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View unused) {
                        if (prefs.getBoolean("isOpenEveryMac", false)) {
                            loadLinks(thisName, thisLinks);
                        } else {
                            sendIntent(machineID);
                        }
                    }
                });
                currentLayout.addView(mainChunk);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            Log.e("initCategory", "Initialize Category " + category + " failed!!");
        }
    }

    public void sendIntent(final int thisMachineID) {
        Intent intent = new Intent(MainActivity.this, SpecsActivity.class);
        intent.putExtra("machineID", thisMachineID);
        startActivity(intent);
    }

    // Copied from specsActivity, keep them compatible.
    private void loadLinks(final String thisName, final String thisLinks) {
        try {
            if (thisLinks.equals("N")) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.link_not_available), Toast.LENGTH_LONG).show();
                return;
            }
            final String[] linkGroup = thisLinks.split(";");
            if (linkGroup.length == 1) {
                startBrowser(linkGroup[0].split(",")[0], linkGroup[0].split(",")[1]);
            } else {
                AlertDialog.Builder linkDialog = new AlertDialog.Builder(this);
                linkDialog.setTitle(thisName);
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
                                try {
                                    startBrowser(linkGroup[linkOptions.getCheckedRadioButtonId()]
                                            .split(",")[0], linkGroup[linkOptions.getCheckedRadioButtonId()]
                                            .split(",")[1]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(),
                                            getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                linkDialog.setNegativeButton(this.getResources().getString(R.string.link_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                // Cancelled.
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

    private void startBrowser(final String thisName, final String url) {
        try {
            Intent browser = new Intent(Intent.ACTION_VIEW);
            browser.setData(Uri.parse(url));
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.link_opening) + thisName, Toast.LENGTH_LONG).show();
            startActivity(browser);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    private void openRandom() {
        try {
            if (machineHelper.getMachineCount() == 0) {
                throw new IllegalArgumentException();
            }
            if (prefs.getBoolean("isOpenEveryMac", false)) {
                final int configID = new Random().nextInt(machineHelper.getConfigCount());
                final int[] configPosition = machineHelper.findByConfig(configID);
                final String configString = machineHelper.getConfig(configPosition[0]);
                if (configString.equals("N")) {
                    // If this does happen: random machine have no link and open EveryMac checked
                    Log.w("RandomAccess", "No link present! retrying");
                    openRandom();
                    return;
                }
                Log.i("RandomAccess", "Link direct, Config ID " + configID
                        + ", Machine ID " + configPosition[0] + ", Link No. " + configPosition[1]);
                final String[] linkGroup = configString.split(";");
                startBrowser(linkGroup[configPosition[1]].split(",")[0],
                        linkGroup[configPosition[1]].split(",")[1]);
            } else {
                int machineID = new Random().nextInt(machineHelper.getMachineCount());
                Log.i("RandomAccess", "Machine ID " + machineID);
                sendIntent(machineID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    public static MachineHelper getMachineHelper() {
        return machineHelper;
    }

    public static SharedPreferences getPrefs() {
        return prefs;
    }
}
