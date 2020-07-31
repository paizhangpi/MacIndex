package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Random;

/**
 * MacIndex.
 * University of Illinois, CS125 FA19 Final Project
 * University of Illinois, CS199 Kotlin SP20 Final Project
 * https://paizhang.info/MacIndex
 * https://github.com/paizhangpi/MacIndex
 *
 * 3.0 Update May 12, 2020 at Champaign, Illinois, U.S.A.
 * 4.0 Update June 13, 2020 at Shenyang, Liaoning, China.
 */
public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database;

    private static MachineHelper machineHelper;

    private static Resources resources = null;

    private DrawerLayout mDrawerLayout = null;

    private String thisManufacturer = null;

    private String thisFilter = null;

    private int[][] loadPositions = {};

    private int machineLoadedCount = 0;

    private String everyMacAppend = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("MacIndex", "Welcome to MacIndex.");

        resources = getResources();

        // If MainActivity Usage is set to not be saved
        if (!(PrefsHelper.getBooleanPrefs("isSaveMainUsage", this))) {
            PrefsHelper.clearPrefs("MainTitle", this);
            PrefsHelper.clearPrefs("thisManufacturer", this);
            PrefsHelper.clearPrefs("thisFilter", this);
            PrefsHelper.clearPrefs("ManufacturerMenu", this);
            PrefsHelper.clearPrefs("FilterMenu", this);
        }

        // Reset Volume Warning
        PrefsHelper.clearPrefs("isEnableVolWarningThisTime", this);

        // If user lunched MacIndex for the first time, a message should show.
        if (PrefsHelper.getBooleanPrefs("isFirstLunch", this)) {
            final AlertDialog.Builder firstLunchGreet = new AlertDialog.Builder(this);
            firstLunchGreet.setTitle(R.string.information_first_lunch_title);
            firstLunchGreet.setMessage(R.string.information_first_lunch);
            firstLunchGreet.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> startActivity(new Intent(MainActivity.this, SettingsAboutActivity.class)));
            firstLunchGreet.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
                // Cancelled, no action needed.
            });
            firstLunchGreet.show();
            PrefsHelper.editPrefs("isFirstLunch", false, this);
        }

        thisManufacturer = PrefsHelper.getStringPrefs("thisManufacturer", this);
        thisFilter = PrefsHelper.getStringPrefs("thisFilter", this);

        // If EveryMac enabled, a message should append.
        if (PrefsHelper.getBooleanPrefs("isOpenEveryMac", this)) {
            everyMacAppend = getString(R.string.menu_group_everymac);
        } else {
            everyMacAppend = "";
        }

        initDatabase();
        initMenu();
        initInterface();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If EveryMac enabled, a message should append.
        if (PrefsHelper.getBooleanPrefs("isOpenEveryMac", this)) {
            everyMacAppend = getString(R.string.menu_group_everymac);
        } else {
            everyMacAppend = "";
        }
        setTitle(getString(PrefsHelper.getIntPrefs("MainTitle", this)) + everyMacAppend);
    }

    @Override
    protected void onDestroy() {
        if (machineHelper != null) {
            machineHelper.suicide();
        }
        if (database != null) {
            database.close();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
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
                ExceptionHelper.handleException(this, null,
                        "MachineHelperInit",
                        "Columns count preset mismatch with actual quantity.");
            }

        } catch (Exception e) {
            ExceptionHelper.handleException(this, e,
                    "initDatabase", "Initialize failed!!");
        }
    }

    private void initMenu() {
        try {
            Log.i("initMenu", "Initializing");
            // Set the slide menu.
            // Set the edge size of drawer.
            mDrawerLayout = findViewById(R.id.mainContainer);
            Field mDragger = mDrawerLayout.getClass().getDeclaredField(
                    "mLeftDragger");
            mDragger.setAccessible(true);
            ViewDragHelper draggerObj = (ViewDragHelper) mDragger
                    .get(mDrawerLayout);
            Field mEdgeSize = draggerObj.getClass().getDeclaredField(
                    "mEdgeSize");
            mEdgeSize.setAccessible(true);
            int edge = mEdgeSize.getInt(draggerObj);
            mEdgeSize.setInt(draggerObj, edge * 10);

            // Initialize the navigation bar

            // Manufacturer Menu
            // Manufacturer 0: all (Default)
            findViewById(R.id.group0MenuItem).setOnClickListener(view -> {
                thisManufacturer = "all";
                PrefsHelper.editPrefs("thisManufacturer", "all", this);
                PrefsHelper.editPrefs("ManufacturerMenu", R.id.group0MenuItem, this);
                PrefsHelper.editPrefs("MainTitle", R.string.menu_group0, this);
                refresh();
                mDrawerLayout.closeDrawers();
            });
            // Manufacturer 1: apple68k
            findViewById(R.id.group1MenuItem).setOnClickListener(view -> {
                thisManufacturer = "apple68k";
                PrefsHelper.editPrefs("thisManufacturer", "apple68k", this);
                PrefsHelper.editPrefs("ManufacturerMenu", R.id.group1MenuItem, this);
                PrefsHelper.editPrefs("MainTitle", R.string.menu_group1, this);
                refresh();
                mDrawerLayout.closeDrawers();
            });
            // Manufacturer 2: appleppc
            findViewById(R.id.group2MenuItem).setOnClickListener(view -> {
                thisManufacturer = "appleppc";
                PrefsHelper.editPrefs("thisManufacturer", "appleppc", this);
                PrefsHelper.editPrefs("ManufacturerMenu", R.id.group2MenuItem, this);
                PrefsHelper.editPrefs("MainTitle", R.string.menu_group2, this);
                refresh();
                mDrawerLayout.closeDrawers();
            });
            // Manufacturer 3: appleintel
            findViewById(R.id.group3MenuItem).setOnClickListener(view -> {
                thisManufacturer = "appleintel";
                PrefsHelper.editPrefs("thisManufacturer", "appleintel", this);
                PrefsHelper.editPrefs("ManufacturerMenu", R.id.group3MenuItem, this);
                PrefsHelper.editPrefs("MainTitle", R.string.menu_group3, this);
                refresh();
                mDrawerLayout.closeDrawers();
            });
            // Manufacturer 4: applearm
            findViewById(R.id.group4MenuItem).setOnClickListener(view -> {
                thisManufacturer = "applearm";
                PrefsHelper.editPrefs("thisManufacturer", "applearm", this);
                PrefsHelper.editPrefs("ManufacturerMenu", R.id.group4MenuItem, this);
                PrefsHelper.editPrefs("MainTitle", R.string.menu_group4, this);
                refresh();
                mDrawerLayout.closeDrawers();
            });

            // Filter Menu
            // Filter 1: names (Default)
            findViewById(R.id.view1MenuItem).setOnClickListener(view -> {
                thisFilter = "names";
                PrefsHelper.editPrefs("FilterMenu", R.id.view1MenuItem, this);
                PrefsHelper.editPrefs("thisFilter", "names", this);
                refresh();
                mDrawerLayout.closeDrawers();
            });
            // Filter 2: processors
            findViewById(R.id.view2MenuItem).setOnClickListener(view -> {
                thisFilter = "processors";
                PrefsHelper.editPrefs("FilterMenu", R.id.view2MenuItem, this);
                PrefsHelper.editPrefs("thisFilter", "processors", this);
                refresh();
                mDrawerLayout.closeDrawers();
            });
            // Filter 3: years
            findViewById(R.id.view3MenuItem).setOnClickListener(view -> {
                thisFilter = "years";
                PrefsHelper.editPrefs("FilterMenu", R.id.view3MenuItem, this);
                PrefsHelper.editPrefs("thisFilter", "years", this);
                refresh();
                mDrawerLayout.closeDrawers();
            });

            // Main Menu
            // SearchActivity Entrance
            findViewById(R.id.searchMenuItem).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));
            // Random Access
            findViewById(R.id.randomMenuItem).setOnClickListener(view -> openRandom());
            // SettingsAboutActivity Entrance
            findViewById(R.id.aboutMenuItem).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SettingsAboutActivity.class)));

            // Set a drawer listener to change title and color.
            mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(@NonNull final View drawerView, final float slideOffset) {
                    // No action
                }

                @Override
                public void onDrawerOpened(@NonNull final View drawerView) {
                    setTitle(R.string.app_name);
                }

                @Override
                public void onDrawerClosed(@NonNull final View drawerView) {
                    setTitle(getString(PrefsHelper.getIntPrefs("MainTitle", MainActivity.this)) + everyMacAppend);
                }

                @Override
                public void onDrawerStateChanged(final int newState) {
                    // Manufacturer Menu
                    final LinearLayout manufacturerLayout = findViewById(R.id.groupLayout);
                    for (int i = 1; i < manufacturerLayout.getChildCount(); i++) {
                        if (manufacturerLayout.getChildAt(i) instanceof TextView) {
                            final TextView currentChild = (TextView) manufacturerLayout.getChildAt(i);
                            if (currentChild == findViewById(PrefsHelper.getIntPrefs("ManufacturerMenu", MainActivity.this))) {
                                currentChild.setEnabled(false);
                                currentChild.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_24, 0);
                            } else {
                                currentChild.setEnabled(true);
                                currentChild.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                            }
                        }
                    }

                    // Filter Menu
                    final LinearLayout filterLayout = findViewById(R.id.viewLayout);
                    for (int i = 1; i < filterLayout.getChildCount(); i++) {
                        if (filterLayout.getChildAt(i) instanceof TextView) {
                            final TextView currentChild = (TextView) filterLayout.getChildAt(i);
                            if (currentChild == findViewById(PrefsHelper.getIntPrefs("FilterMenu", MainActivity.this))) {
                                currentChild.setEnabled(false);
                                currentChild.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_24, 0);
                            } else {
                                currentChild.setEnabled(true);
                                currentChild.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                            }
                        }
                    }

                    // If EveryMac enabled, random should be disabled
                    if (PrefsHelper.getBooleanPrefs("isOpenEveryMac", MainActivity.this)) {
                        findViewById(R.id.randomMenuItem).setEnabled(false);
                    } else {
                        findViewById(R.id.randomMenuItem).setEnabled(true);
                    }

                    // If limit range enabled, a message should append
                    if (PrefsHelper.getBooleanPrefs("isRandomAll", MainActivity.this)) {
                        ((TextView) findViewById(R.id.randomMenuItem))
                                .setText(getString(R.string.menu_random) + getString(R.string.menu_random_limited));
                    } else {
                        ((TextView) findViewById(R.id.randomMenuItem))
                                .setText(getString(R.string.menu_random));
                    }
                }
            });

            // Set the toolbar.
            final Toolbar mainToolbar = findViewById(R.id.mainToolbar);
            final ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mainToolbar, 0, 0);
            mDrawerLayout.addDrawerListener(drawerToggle);
            drawerToggle.syncState();
            setSupportActionBar(mainToolbar);
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e,
                    "initMenu", "Initialize failed!!");
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    private void initInterface() {
        try {
            // Set Activity title.
            setTitle(getString(PrefsHelper.getIntPrefs("MainTitle", this)) + everyMacAppend);
            // Parent layout of all categories.
            final LinearLayout categoryContainer = findViewById(R.id.categoryContainer);
            // Fix an animation bug here
            LayoutTransition layoutTransition = categoryContainer.getLayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            categoryContainer.removeAllViews();
            // Get filter string and positions.
            final String[][] thisFilterString = machineHelper.getFilterString(thisFilter);
            loadPositions = machineHelper.filterSearchHelper(thisFilter, thisManufacturer);
            // Set up each category.
            for (int i = 0; i < loadPositions.length; i++) {
                final View categoryChunk = getLayoutInflater().inflate(R.layout.chunk_category, null);
                final LinearLayout categoryChunkLayout = categoryChunk.findViewById(R.id.categoryInfoLayout);
                final TextView categoryName = categoryChunk.findViewById(R.id.category);
                if (loadPositions[i].length != 0) {
                    categoryName.setText(thisFilterString[2][i]);

                    /* Remake my teammate's code */
                    categoryName.setOnClickListener(new View.OnClickListener() {
                        private boolean thisVisibility = false;
                        @Override
                        public void onClick(final View view) {
                            final View firstChild = categoryChunkLayout.getChildAt(1);
                            if (thisVisibility) {
                                // Make machines invisible.
                                if (!(firstChild instanceof LinearLayout)) {
                                    // Have the divider
                                    for (int j = 2; j < categoryChunkLayout.getChildCount(); j++) {
                                        categoryChunkLayout.getChildAt(j).setVisibility(View.GONE);
                                        thisVisibility = false;
                                    }
                                    firstChild.setVisibility(View.VISIBLE);
                                } else {
                                    // Does not have the divider
                                    for (int j = 1; j < categoryChunkLayout.getChildCount(); j++) {
                                        categoryChunkLayout.getChildAt(j).setVisibility(View.GONE);
                                        thisVisibility = false;
                                    }
                                }
                            } else {
                                // Make machines visible.
                                if (!(firstChild instanceof LinearLayout)) {
                                    // Have the divider
                                    for (int j = 2; j < categoryChunkLayout.getChildCount(); j++) {
                                        categoryChunkLayout.getChildAt(j).setVisibility(View.VISIBLE);
                                        thisVisibility = true;
                                    }
                                    firstChild.setVisibility(View.GONE);
                                } else {
                                    // Does not have the divider
                                    for (int j = 1; j < categoryChunkLayout.getChildCount(); j++) {
                                        categoryChunkLayout.getChildAt(j).setVisibility(View.VISIBLE);
                                        thisVisibility = true;
                                    }
                                }
                            }
                        }
                    });
                    Log.i("initCategory", "Loading category " + i);
                    machineLoadedCount += SpecsIntentHelper
                            .initCategory(categoryChunkLayout, loadPositions[i], false, this);
                    categoryContainer.addView(categoryChunk);
                }
            }
            // Remove the last divider.
            if (categoryContainer.getChildCount() != 0) {
                ((LinearLayout) categoryContainer.getChildAt(categoryContainer.getChildCount() - 1)).removeViewAt(1);
            }
            // Basic functionality was finished on 16:12 CST, Dec 2, 2019.
            Log.w("MainActivity", "Initialized with " + machineLoadedCount + " machines loaded.");
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e,
                    "initInterface", "Initialize failed!!");
        }
    }

    private void openRandom() {
        try {
            if (machineHelper.getMachineCount() == 0) {
                throw new IllegalArgumentException();
            }
            if (PrefsHelper.getBooleanPrefs("isOpenEveryMac", this)) {
                // This should not happen.
                throw new IllegalStateException();
            } else {
                int machineID = 0;
                if (!PrefsHelper.getBooleanPrefs("isRandomAll", this)) {
                    // Random All mode.
                    machineID = new Random().nextInt(machineHelper.getMachineCount());
                    Log.i("RandomAccess", "Random All mode, get total " + machineHelper.getMachineCount() + " , ID " + machineID);
                } else {
                    // Limited Random mode.
                    int totalLoadad = 0;
                    for (int[] i : loadPositions) {
                        totalLoadad += i.length;
                    }
                    int randomCode = new Random().nextInt(totalLoadad + 1);
                    Log.i("RandomAccess", "Limit Random mode, get total " + totalLoadad + " , ID " + randomCode);
                    for (int[] loadPosition : loadPositions) {
                        if (randomCode >= loadPosition.length) {
                            randomCode -= loadPosition.length;
                        } else {
                            machineID = loadPosition[randomCode];
                            break;
                        }
                    }
                }
                Log.i("RandomAccess", "Machine ID " + machineID);
                SpecsIntentHelper.sendIntent(new int[]{machineID}, machineID, this);
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, null, null);
        }
    }

    private void refresh() {
        Log.i("MainActivity", "Reloading");
        machineLoadedCount = 0;
        initInterface();
    }

    public static MachineHelper getMachineHelper() {
        return machineHelper;
    }

    public static Resources getRes() {
        return resources;
    }
}
