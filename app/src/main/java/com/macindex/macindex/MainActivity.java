package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

/**
 * MacIndex
 * University of Illinois, CS125 FA19 Final Project
 * https://paizhang.info/MacIndex
 */
public class MainActivity extends AppCompatActivity {
    // Set to the ID of last table.
    private static final int CATEGORIES_COUNT = 9;

    private MachineHelper machineHelper;

    private SQLiteDatabase database;

    private SharedPreferences prefs = null;

    private SharedPreferences.Editor prefsEditor = null;

    private int totalMachine = 0;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("MACINDEX_PREFS", Activity.MODE_PRIVATE);
        prefsEditor = prefs.edit();
        setContentView(R.layout.activity_main);
        initDatabase();
        initInterface();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        MenuItem aboutMenu = menu.findItem(R.id.aboutMenu);
        aboutMenu.setTitle(getResources().getString(R.string.about) + BuildConfig.VERSION_NAME);
        MenuItem isEveryMacMenu = menu.findItem(R.id.isEveryMacMenu);
        isEveryMacMenu.setChecked(prefs.getBoolean("isOpenEveryMac", false));
        MenuItem searchMenu = menu.findItem(R.id.searchMenu);
        searchMenu.setEnabled(false);
        searchMenu.setTitle(getResources().getString(R.string.search));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aboutMenu:
                Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            case R.id.searchMenu:
                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(searchIntent);
                return true;
            case R.id.randomMenu:
                openRandom();
                return true;
            case R.id.isEveryMacMenu:
                if (item.isChecked()) {
                    item.setChecked(false);
                    prefsEditor.putBoolean("isOpenEveryMac", false);
                    prefsEditor.commit();
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.menu_everymac_false), Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder isEveryMacDialog = new AlertDialog.Builder(this);
                    isEveryMacDialog.setTitle(getResources().getString(R.string.menu_everymac));
                    isEveryMacDialog.setMessage(getResources().getString(R.string.menu_everymac_description));
                    isEveryMacDialog.setPositiveButton(this.getResources().getString(R.string.link_confirm),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    item.setChecked(true);
                                    prefsEditor.putBoolean("isOpenEveryMac", true);
                                    prefsEditor.commit();
                                    Toast.makeText(getApplicationContext(),
                                            getResources().getString(R.string.menu_everymac_true), Toast.LENGTH_LONG).show();
                                }
                            });
                    isEveryMacDialog.setNegativeButton(this.getResources().getString(R.string.link_cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    // CANCELLED, do not modify.
                                }
                            });
                    isEveryMacDialog.show();
                }
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            Log.e("initDatabase", "Initialize failed!!");
        }
        machineHelper = new MachineHelper(database, CATEGORIES_COUNT);
        totalMachine = machineHelper.getMachineCount();
    }

    private void initInterface() {
        for (int i = 0; i <= CATEGORIES_COUNT; i++) {
            final int layoutID = CategoryHelper.getLayout(i);
            if (layoutID == 0) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                continue;
            }
            final LinearLayout currentLayout = findViewById(layoutID);
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
        Log.i("InitInterface", totalMachine + " loaded");
        TextView totalMachineText = findViewById(R.id.totalMachinesText);
        totalMachineText.setText(getResources().getString(R.string.total_1) + totalMachine + getResources().getString(R.string.total_2));
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

                // Create a String for each data category. Update here.
                final String thisName = machineHelper.getName(machineID);
                final String thisSound = machineHelper.getSound(machineID);
                final String thisProcessor = machineHelper.getProcessor(machineID);
                final String thisMaxRAM = machineHelper.getMaxRam(machineID);
                final String thisYear = machineHelper.getYear(machineID);
                final String thisModel = machineHelper.getModel(machineID);
                final byte[] thisBlob = machineHelper.getPicture(machineID);
                final String thisLinks = machineHelper.getConfig(machineID);

                machineName.setText(thisName);
                machineYear.setText(thisYear);

                machineName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View unused) {
                        if (prefs.getBoolean("isOpenEveryMac", false)) {
                            loadLinks(thisName, thisLinks);
                        } else {
                            sendIntent(thisName, thisSound, thisProcessor,
                                    thisMaxRAM, thisYear, thisModel, thisBlob, thisLinks, machineID);
                        }
                    }
                });

                machineYear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View unused) {
                        if (prefs.getBoolean("isOpenEveryMac", false)) {
                            loadLinks(thisName, thisLinks);
                        } else {
                            sendIntent(thisName, thisSound, thisProcessor,
                                    thisMaxRAM, thisYear, thisModel, thisBlob, thisLinks, machineID);
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

    public void sendIntent(final String thisName, final String thisSound, final String thisProcessor,
                                  final String thisMaxRAM, final String thisYear, final String thisModel,
                                  final byte[] thisBlob, final String thisLinks, final int thisMachineID) {
        Intent intent = new Intent(MainActivity.this, SpecsActivity.class);
        intent.putExtra("name", thisName);
        intent.putExtra("sound", thisSound);
        intent.putExtra("processor", thisProcessor);
        intent.putExtra("maxram", thisMaxRAM);
        intent.putExtra("year", thisYear);
        intent.putExtra("model", thisModel);
        intent.putExtra("links", thisLinks);
        intent.putExtra("id", thisMachineID);

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
            if (totalMachine == 0) {
                throw new IllegalArgumentException();
            }
            if (prefs.getBoolean("isOpenEveryMac", false)) {
                int configID = new Random().nextInt(machineHelper.getConfigCount());
            } else {
                int machineID = new Random().nextInt(totalMachine);
            }
            Log.i("RandomAccess", "Machine No. " + machineID);
            final String thisName = machineHelper.getName(machineID);
            final String thisSound = machineHelper.getSound(machineID);
            final String thisProcessor = machineHelper.getProcessor(machineID);
            final String thisMaxRAM = machineHelper.getMaxRam(machineID);
            final String thisYear = machineHelper.getYear(machineID);
            final String thisModel = machineHelper.getModel(machineID);
            final byte[] thisBlob = machineHelper.getPicture(machineID);
            final String thisLinks = machineHelper.getConfig(machineID);
            if (prefs.getBoolean("isOpenEveryMac", false)) {
                if (thisLinks.equals("N")) {
                    // If this does happen: random machine have no link and open EveryMac checked
                    Log.w("RandomAccess", "No link present! retrying");
                    openRandom();
                    return;
                }
                loadLinks(thisName, thisLinks);
            } else {
                sendIntent(thisName, thisSound, thisProcessor,
                        thisMaxRAM, thisYear, thisModel, thisBlob, thisLinks, machineID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean getIsOpenInEveryMac() {
        return prefs.getBoolean("isOpenEveryMac", false);
    }

}
