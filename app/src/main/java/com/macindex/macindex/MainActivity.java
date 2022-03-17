package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.TextViewCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;

/**
 * MacIndex.
 * University of Illinois, CS125 FA19 Final Project
 * University of Illinois, CS199 Kotlin SP20 Final Project
 * https://MacIndex.paizhang.info/
 * https://github.com/paizhangpi/MacIndex
 *
 * Basic functionality was finished on 16:12 CST, Dec 2, 2019.
 * 3.0 Update May 12, 2020 at Champaign, Illinois, U.S.A.
 * 4.0 Update June 13, 2020 at Shenyang, Liaoning, China.
 * 4.5 Update January 7, 2021 at Jinzhong, Shanxi, China.
 */
public class MainActivity extends AppCompatActivity {

    private static SQLiteDatabase database = null;

    private static MachineHelper machineHelper = null;

    private static Resources resources = null;

    private DrawerLayout mDrawerLayout = null;

    private String thisManufacturer = null;

    private String thisFilter = null;

    private int[][] loadPositions = {};

    private TextView[][] machineLoadedCount = null;

    private long mBackPressed = 0;

    private ProgressDialog waitDialog = null;

    private static boolean isMainRunning = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            isMainRunning = true;

            thisManufacturer = PrefsHelper.getStringPrefs("lastMainManufacturer", this);
            thisFilter = PrefsHelper.getStringPrefs("lastMainFilter", this);
            initMenu();

            waitDialog = new ProgressDialog(MainActivity.this);
            waitDialog.setMessage(getString(R.string.loading_category));
            waitDialog.setCancelable(false);

            if (savedInstanceState == null) {
                // Creating activity due to user
                Log.i("MacIndex", "Welcome to MacIndex.");

                // If MainActivity Usage is set to not be saved
                if (!(PrefsHelper.getBooleanPrefs("isSaveMainUsage", this))) {
                    PrefsHelper.clearPrefs("lastMainManufacturer", this);
                    PrefsHelper.clearPrefs("lastMainFilter", this);
                }

                // Reset Volume Warning
                PrefsHelper.clearPrefs("isEnableVolWarningThisTime", this);

                // Reset EveryMacIndex Warning
                PrefsHelper.clearPrefs("isJustLunched", this);

                resources = getResources();
                if (machineHelper == null || database == null || resources == null || !database.isOpen()) {
                    Log.i("MacIndex", "Initializing database.");
                    initDatabase(this);
                } else {
                    Log.w("MacIndex", "Database already initialized.");
                }

                // Cache clear if new version is registered
                if (PrefsHelper.registerNewVersion(this)) {
                    clearCache();
                }

                initInterface(true);

                // Deep Link Support, Activity Not Present
                Uri deepLink = getIntent().getData();
                if (deepLink != null) {
                    decodeDeepLink(deepLink.toString(), null);
                } else {
                    Log.w("onCreateDeepLinkEntry", "Got null data");
                }
            } else {
                // Creating activity due to system
                Log.i("MacIndex", "Reloading the main activity.");

                validateOperation(this);
                if (savedInstanceState.getBoolean("loadComplete")) {
                    // Restore the saved ID list
                    final int loadPositionsCount = savedInstanceState.getInt("loadPositionsCount");
                    loadPositions = new int[loadPositionsCount][];
                    for (int i = 0; i < loadPositionsCount; i++) {
                        loadPositions[i] = savedInstanceState.getIntArray("loadPositions" + i);
                    }
                    initInterface(false);
                } else {
                    initInterface(true);
                }

                // Finally, restore drawer.
                if (savedInstanceState.getBoolean("drawerOpen")) {
                    resetDrawerTitle();
                    resetDrawerSelection();
                }
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "MainCreation", "Unable to create the main activity.");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Deep Link Support, Activity Present
        // Override this function due to the special lunch mode
        Uri deepLink = intent.getData();
        if (deepLink != null) {
            decodeDeepLink(deepLink.toString(), null);
        } else {
            Log.w("onNewIntentDeepLinkEntry", "Got null data");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try {
            // If reload is needed..
            if (PrefsHelper.getBooleanPrefs("isReloadNeeded", this)) {
                setTitle(getString(translateTitleRes()));
                initInterface(true);
                PrefsHelper.editPrefs("isReloadNeeded", false, this);
            }

            // Reload favourites
            SpecsIntentHelper.refreshFavourites(machineLoadedCount, this);
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "MainOnRestart", "Unable to resume normal activity.");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Is still loading?
        if (!waitDialog.isShowing()) {
            // Save the currently received ID list
            outState.putBoolean("loadComplete", true);
            outState.putInt("loadPositionsCount", loadPositions.length);
            for (int i = 0; i < loadPositions.length; i++) {
                outState.putIntArray("loadPositions" + i, loadPositions[i]);
            }
        } else {
            outState.putBoolean("loadComplete", false);
            MainActivity.reloadDatabase(this);
        }

        // Is drawer opened?
        outState.putBoolean("drawerOpen", mDrawerLayout.isDrawerOpen(GravityCompat.START));
    }

    @Override
    protected void onDestroy() {
        isMainRunning = false;
        closeDatabase();
        if (waitDialog.isShowing()) {
            waitDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        // Debug items visibility
        if (!BuildConfig.DEBUG) {
            Log.i("DebugMode", "Disabling debug menu items.");
            menu.findItem(R.id.mainDebugReloadItem).setVisible(false);
            menu.findItem(R.id.mainDebugTriggerErrorItem).setVisible(false);
            menu.findItem(R.id.mainDebugRunnerItem).setVisible(false);
            menu.findItem(R.id.mainDebugClearCacheItem).setVisible(false);
            menu.findItem(R.id.mainDebugVersionRegistration).setVisible(false);
        }
        return true;
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
            case R.id.mainDebugReloadItem:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                reloadDatabase(this);
                initInterface(true);
                break;
            case R.id.mainDebugTriggerErrorItem:
                // Debug use only. Should not visible to users
                ExceptionHelper.handleException(this, null, "Debug", "User triggered.");
                break;
            case R.id.mainDebugClearCacheItem:
                clearCache();
                break;
            case R.id.mainDebugVersionRegistration:
                PrefsHelper.editPrefs("lastKnownVersion", BuildConfig.VERSION_CODE - 1, this);
                PrefsHelper.triggerRebirth(this);
                break;
            case R.id.mainDebugRunnerItem:
                /* For function testing */
                Toast.makeText(this, "Complete", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mainReadCodeItem:
                decodeSharedInfo();
                break;
            case R.id.mainResetItem:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
                if (!(thisManufacturer.equals("all") && thisFilter.equals("names"))) {
                    thisManufacturer = "all";
                    thisFilter = "names";
                    PrefsHelper.editPrefs("lastMainManufacturer", "all", this);
                    PrefsHelper.editPrefs("lastMainFilter", "names", this);
                    initInterface(true);
                }
                break;
            case R.id.mainHelpItem:
                LinkLoadingHelper.startBrowser(null, "https://macindex.paizhang.info/main-activity", this);
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
            if (mBackPressed + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                Toast.makeText(this, R.string.information_double_press, Toast.LENGTH_SHORT).show();
            }
            mBackPressed = System.currentTimeMillis();
        }
    }

    private static void initDatabase(final Context context) {
        try {
            Log.w("Database", "Initializing.");
            File dbFilePath = new File(context.getApplicationInfo().dataDir + "/databases/specs.db");
            File dbFolder = new File(context.getApplicationInfo().dataDir + "/databases");
            dbFilePath.delete();
            dbFolder.delete();
            dbFolder.mkdir();
            InputStream inputStream = context.getAssets().open("specs.db");
            OutputStream outputStream = new FileOutputStream(dbFilePath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            DatabaseOpenHelper dbHelper = new DatabaseOpenHelper(context);
            database = dbHelper.getReadableDatabase();

            // Open MachineHelper
            machineHelper = new MachineHelper(database);

        } catch (Exception e) {
            ExceptionHelper.handleException(context, e,
                    "initDatabaseSafe", "Initialize failed!!");
        }
    }

    private static void closeDatabase() {
        if (machineHelper != null) {
            machineHelper.setStopQuery();
        }
        if (database != null) {
            Log.w("Database", "Current database close.");
            database.close();
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
                mDrawerLayout.closeDrawers();
                thisManufacturer = "all";
                PrefsHelper.editPrefs("lastMainManufacturer", "all", this);
                initInterface(true);
            });
            // Manufacturer 1: apple68k
            findViewById(R.id.group1MenuItem).setOnClickListener(view -> {
                mDrawerLayout.closeDrawers();
                thisManufacturer = "apple68k";
                PrefsHelper.editPrefs("lastMainManufacturer", "apple68k", this);
                initInterface(true);
            });
            // Manufacturer 2: appleppc
            findViewById(R.id.group2MenuItem).setOnClickListener(view -> {
                mDrawerLayout.closeDrawers();
                thisManufacturer = "appleppc";
                PrefsHelper.editPrefs("lastMainManufacturer", "appleppc", this);
                initInterface(true);
            });
            // Manufacturer 3: appleintel
            findViewById(R.id.group3MenuItem).setOnClickListener(view -> {
                mDrawerLayout.closeDrawers();
                thisManufacturer = "appleintel";
                PrefsHelper.editPrefs("lastMainManufacturer", "appleintel", this);
                initInterface(true);
            });
            // Manufacturer 4: applearm
            findViewById(R.id.group4MenuItem).setOnClickListener(view -> {
                mDrawerLayout.closeDrawers();
                thisManufacturer = "applearm";
                PrefsHelper.editPrefs("lastMainManufacturer", "applearm", this);
                initInterface(true);
            });

            // Filter Menu
            // Filter 1: names (Default)
            findViewById(R.id.view1MenuItem).setOnClickListener(view -> {
                mDrawerLayout.closeDrawers();
                thisFilter = "names";
                PrefsHelper.editPrefs("lastMainFilter", "names", this);
                initInterface(true);
            });
            // Filter 2: processors
            findViewById(R.id.view2MenuItem).setOnClickListener(view -> {
                mDrawerLayout.closeDrawers();
                thisFilter = "processors";
                PrefsHelper.editPrefs("lastMainFilter", "processors", this);
                initInterface(true);
            });
            // Filter 3: years
            findViewById(R.id.view3MenuItem).setOnClickListener(view -> {
                mDrawerLayout.closeDrawers();
                thisFilter = "years";
                PrefsHelper.editPrefs("lastMainFilter", "years", this);
                initInterface(true);
            });

            // Main Menu
            // SearchActivity Entrance
            findViewById(R.id.searchMenuItem).setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                mDrawerLayout.closeDrawers();
            });
            // Random Access
            findViewById(R.id.randomMenuItem).setOnClickListener(view -> {
                openRandom();
                mDrawerLayout.closeDrawers();
            });
            // FavouriteActivity Entrance
            findViewById(R.id.favouriteMenuItem).setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, FavouriteActivity.class));
                mDrawerLayout.closeDrawers();
            });
            // CompareActivity Entrance
            findViewById(R.id.compareMenuItem).setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, CompareActivity.class));
                mDrawerLayout.closeDrawers();
            });
            // CommentActivity Entrance
            findViewById(R.id.commentMenuItem).setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, CommentActivity.class));
                mDrawerLayout.closeDrawers();
            });
            // SettingsAboutActivity Entrance
            findViewById(R.id.aboutMenuItem).setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, SettingsAboutActivity.class));
                mDrawerLayout.closeDrawers();
            });
            // AboutActivity Entrance
            findViewById(R.id.newAboutMenuItem).setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, NewAboutActivity.class));
                mDrawerLayout.closeDrawers();
            });

            // Set a drawer listener to change title and color.
            mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(@NonNull final View drawerView, final float slideOffset) {
                    // No action
                }

                @Override
                public void onDrawerOpened(@NonNull final View drawerView) {
                    resetDrawerTitle();
                }

                @Override
                public void onDrawerClosed(@NonNull final View drawerView) {
                    setTitle(getString(translateTitleRes()));
                }

                @Override
                public void onDrawerStateChanged(final int newState) {
                    resetDrawerSelection();
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

    private void resetDrawerTitle() {
        // Set if it is in EveryMac mode.
        if (PrefsHelper.getBooleanPrefs("isOpenEveryMac", MainActivity.this)) {
            setTitle(getString(R.string.app_name_everymac));
        } else {
            setTitle(R.string.app_name);
        }
    }

    private void resetDrawerSelection() {
        // Manufacturer Menu
        final LinearLayout manufacturerLayout = findViewById(R.id.groupLayout);
        for (int i = 1; i < manufacturerLayout.getChildCount(); i++) {
            if (manufacturerLayout.getChildAt(i) instanceof TextView) {
                final TextView currentChild = (TextView) manufacturerLayout.getChildAt(i);
                if (currentChild == findViewById(translateManufacturerMenuRes())) {
                    currentChild.setEnabled(false);
                    currentChild.setTextColor(Color.WHITE);
                    currentChild.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    currentChild.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_24_white, 0);

                } else {
                    currentChild.setEnabled(true);
                    currentChild.setTextColor(getResources().getColor(R.color.colorDefaultText));
                    currentChild.setBackgroundColor(Color.WHITE);
                    currentChild.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
        }

        // Filter Menu
        final LinearLayout filterLayout = findViewById(R.id.viewLayout);
        for (int i = 1; i < filterLayout.getChildCount(); i++) {
            if (filterLayout.getChildAt(i) instanceof TextView) {
                final TextView currentChild = (TextView) filterLayout.getChildAt(i);
                if (currentChild == findViewById(translateFilterMenuRes())) {
                    currentChild.setEnabled(false);
                    currentChild.setTextColor(Color.WHITE);
                    currentChild.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    currentChild.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_24_white, 0);
                } else {
                    currentChild.setEnabled(true);
                    currentChild.setTextColor(getResources().getColor(R.color.colorDefaultText));
                    currentChild.setBackgroundColor(Color.WHITE);
                    currentChild.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
        }

        // If EveryMac enabled, random should be disabled
        if (PrefsHelper.getBooleanPrefs("isOpenEveryMac", MainActivity.this)) {
            findViewById(R.id.randomMenuItem).setEnabled(false);
            findViewById(R.id.favouriteMenuItem).setEnabled(false);
            findViewById(R.id.compareMenuItem).setEnabled(false);
            findViewById(R.id.commentMenuItem).setEnabled(false);
        } else {
            findViewById(R.id.randomMenuItem).setEnabled(true);
            findViewById(R.id.favouriteMenuItem).setEnabled(true);
            findViewById(R.id.compareMenuItem).setEnabled(true);
            findViewById(R.id.commentMenuItem).setEnabled(true);
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

    private void initInterface(final boolean reloadPositions) {
        try {
            boolean internalReloadFlag = reloadPositions;
            // Set Activity title.
            setTitle(getString(translateTitleRes()));
            // Parent layout of all categories.
            final LinearLayout categoryContainer = findViewById(R.id.categoryContainer);
            // Fix an animation bug here
            LayoutTransition layoutTransition = categoryContainer.getLayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            categoryContainer.removeAllViews();
            // Get filter string and positions.
            final String[][] thisFilterString = machineHelper.getFilterString(thisFilter);

            // Query cache.
            if (internalReloadFlag) {
                internalReloadFlag = !(operateCache(false));
            }

            if (internalReloadFlag) {
                waitDialog.show();
            }
            final boolean finalInternalReloadFlag = internalReloadFlag;
            new Thread() {
                @Override
                public void run() {
                    if (finalInternalReloadFlag) {
                        loadPositions = machineHelper.filterSearchHelper(thisFilterString, thisManufacturer,
                                PrefsHelper.getBooleanPrefsSafe("isSortAgain", MainActivity.this));
                        // Write cache.
                        operateCache(true);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (finalInternalReloadFlag) {
                                    waitDialog.dismiss();
                                }
                                // Set up each category.
                                machineLoadedCount = new TextView[loadPositions.length][];
                                for (int i = 0; i < loadPositions.length; i++) {
                                    final View categoryChunk = getLayoutInflater().inflate(R.layout.chunk_category, null);
                                    final LinearLayout categoryChunkLayout = categoryChunk.findViewById(R.id.categoryInfoLayout);
                                    final TextView categoryName = categoryChunk.findViewById(R.id.category);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        categoryName.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                                    } else {
                                        TextViewCompat.setAutoSizeTextTypeWithDefaults(categoryName, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                                    }

                                    if (loadPositions[i].length != 0) {
                                        categoryName.setText(thisFilterString[2][i]);

                                        /* Remake my teammate's code */
                                        categoryName.setOnClickListener(new View.OnClickListener() {
                                            private boolean thisVisibility = false;

                                            @Override
                                            public void onClick(final View view) {
                                                try {
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
                                                } catch (Exception e) {
                                                    ExceptionHelper.handleException(MainActivity.this, e, null, null);
                                                }
                                            }
                                        });
                                        Log.i("initCategory", "Loading category " + i);
                                        machineLoadedCount[i] = SpecsIntentHelper
                                                .initCategory(categoryChunkLayout, loadPositions[i], false, MainActivity.this);
                                        categoryContainer.addView(categoryChunk);
                                    }
                                }
                                // Remove the last divider.
                                if (categoryContainer.getChildCount() != 0) {
                                    ((LinearLayout) categoryContainer.getChildAt(categoryContainer.getChildCount() - 1)).removeViewAt(1);
                                }

                                // Load the favourites star.
                                SpecsIntentHelper.refreshFavourites(machineLoadedCount, MainActivity.this);
                            } catch (Exception e) {
                                ExceptionHelper.handleException(MainActivity.this, e, null, null);
                            }

                            // If user lunched MacIndex for the first time, a message should show.
                            if (PrefsHelper.getBooleanPrefs("isFirstLunch", MainActivity.this)) {
                                final AlertDialog.Builder firstLunchGreet = new AlertDialog.Builder(MainActivity.this);
                                firstLunchGreet.setTitle(R.string.information_first_lunch_title);
                                firstLunchGreet.setMessage(R.string.information_first_lunch);
                                firstLunchGreet.setPositiveButton(R.string.get_started, (dialogInterface, i) -> mDrawerLayout.openDrawer(GravityCompat.START));
                                firstLunchGreet.show();
                                PrefsHelper.editPrefs("isFirstLunch", false, MainActivity.this);
                            }

                            // EveryMacIndex reminder
                            if (PrefsHelper.getBooleanPrefs("isOpenEveryMac", MainActivity.this)
                                    && PrefsHelper.getBooleanPrefs("isJustLunched", MainActivity.this)) {
                                final AlertDialog.Builder everyMacIndexReminder = new AlertDialog.Builder(MainActivity.this);
                                everyMacIndexReminder.setTitle(R.string.app_name_everymac);
                                everyMacIndexReminder.setMessage(R.string.information_set_everymac);
                                everyMacIndexReminder.setPositiveButton(R.string.menu_about_settings, (dialogInterface, i) -> {
                                    startActivity(new Intent(MainActivity.this, SettingsAboutActivity.class));
                                });
                                everyMacIndexReminder.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
                                    // Do nothing
                                });
                                everyMacIndexReminder.show();
                                PrefsHelper.editPrefs("isJustLunched", false, MainActivity.this);
                            }
                        }
                    });
                }
            }.start();
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e,
                    "initInterface", "Initialize failed!!");
        }
    }

    private void openRandom() {
        try {
            if (machineHelper.getMachineCount() == 0) {
                throw new IllegalStateException();
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
                    if (totalLoadad == 0) {
                        throw new IllegalStateException();
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
                SpecsIntentHelper.sendIntent(new int[]{machineID}, machineID, this, true);
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, null, null);
        }
    }

    private int translateTitleRes() {
        switch (thisManufacturer) {
            case "all":
                return R.string.menu_group0;
            case "apple68k":
                return R.string.menu_group1;
            case "appleppc":
                return R.string.menu_group2;
            case "appleintel":
                return R.string.menu_group3;
            case "applearm":
                return R.string.menu_group4;
            default:
                ExceptionHelper.handleException(this, null,
                        "translateTitleRes",
                        "Not a Valid Manufacturer Selection, This should NOT happen!!");
                return R.string.menu_group0;
        }
    }

    private int translateManufacturerMenuRes() {
        switch (thisManufacturer) {
            case "all":
                return R.id.group0MenuItem;
            case "apple68k":
                return R.id.group1MenuItem;
            case "appleppc":
                return R.id.group2MenuItem;
            case "appleintel":
                return R.id.group3MenuItem;
            case "applearm":
                return R.id.group4MenuItem;
            default:
                ExceptionHelper.handleException(this, null,
                        "translateManufacturerMenuRes",
                        "Not a Valid Manufacturer Selection, This should NOT happen!!");
                return R.id.group0MenuItem;
        }
    }

    private int translateFilterMenuRes() {
        switch (thisFilter) {
            case "names":
                return R.id.view1MenuItem;
            case "processors":
                return R.id.view2MenuItem;
            case "years":
                return R.id.view3MenuItem;
            default:
                ExceptionHelper.handleException(this, null,
                        "translateFilterMenuRes",
                        "Not a Valid Search Column Selection, This should NOT happen!!");
                return R.id.view1MenuItem;
        }
    }

    private boolean operateCache(final boolean isWrite) {
        try {
            String toWrite = "";
            if (isWrite) {
                for (int i = 0; i < loadPositions.length; i++) {
                    for (int j = 0; j < loadPositions[i].length; j++) {
                        toWrite = toWrite.concat(String.valueOf(loadPositions[i][j]));
                        if (!(j + 1 == loadPositions[i].length)) {
                            toWrite = toWrite.concat(",");
                        }
                    }
                    if (!(i + 1 == loadPositions.length)) {
                        toWrite = toWrite.concat(";");
                    }
                }
                Log.w("operateCache", "String to write: " + toWrite);
            }
            switch (thisManufacturer) {
                case "all":
                    switch (thisFilter) {
                        case "names":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM0F0", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM0F0", this);
                                break;
                            }
                        case "processors":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM0F1", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM0F1", this);
                                break;
                            }
                        case "years":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM0F2", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM0F2", this);
                                break;
                            }
                        default:
                            throw new IllegalArgumentException();
                    }
                    break;
                case "apple68k":
                    switch (thisFilter) {
                        case "names":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM1F0", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM1F0", this);
                                break;
                            }
                        case "processors":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM1F1", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM1F1", this);
                                break;
                            }
                        case "years":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM1F2", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM1F2", this);
                                break;
                            }
                        default:
                            throw new IllegalArgumentException();
                    }
                    break;
                case "appleppc":
                    switch (thisFilter) {
                        case "names":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM2F0", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM2F0", this);
                                break;
                            }
                        case "processors":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM2F1", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM2F1", this);
                                break;
                            }
                        case "years":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM2F2", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM2F2", this);
                                break;
                            }
                        default:
                            throw new IllegalArgumentException();
                    }
                    break;
                case "appleintel":
                    switch (thisFilter) {
                        case "names":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM3F0", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM3F0", this);
                                break;
                            }
                        case "processors":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM3F1", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM3F1", this);
                                break;
                            }
                        case "years":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM3F2", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM3F2", this);
                                break;
                            }
                        default:
                            throw new IllegalArgumentException();
                    }
                    break;
                case "applearm":
                    switch (thisFilter) {
                        case "names":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM4F0", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM4F0", this);
                                break;
                            }
                        case "processors":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM4F1", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM4F1", this);
                                break;
                            }
                        case "years":
                            if (isWrite) {
                                PrefsHelper.editPrefs("lastCachedM4F2", toWrite, this);
                                return true;
                            } else {
                                toWrite = PrefsHelper.getStringPrefs("lastCachedM4F2", this);
                                break;
                            }
                        default:
                            throw new IllegalArgumentException();
                    }
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            if (!isWrite) {
                if (toWrite.isEmpty()) {
                    Log.i("MainCache", "Cache is empty.");
                    return false;
                } else {
                    Log.i("MainCache", "Hit cache string: " + toWrite);
                    String[] splitedCategories = toWrite.split(";");
                    loadPositions = new int[splitedCategories.length][];
                    for (int i = 0; i < splitedCategories.length; i++) {
                        // Check if empty:
                        if (splitedCategories[i].isEmpty()) {
                            loadPositions[i] = new int[0];
                            continue;
                        }
                        String[] splitedMachineIDs = splitedCategories[i].split(",");
                        loadPositions[i] = new int[splitedMachineIDs.length];
                        for (int j = 0; j < splitedMachineIDs.length; j++) {
                            loadPositions[i][j] = Integer.parseInt(splitedMachineIDs[j]);
                        }
                    }
                    return true;
                }
            } else {
                throw new IllegalStateException();
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e,
                    "MainCache",
                    "Unable to operate the cache.");
            return false;
        }
    }

    private void clearCache() {
        Log.w("MainCache", "Clearing cache.");
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "Cache clear requested.", Toast.LENGTH_SHORT).show();
        }
        PrefsHelper.clearPrefs("lastCachedM0F0", this);
        PrefsHelper.clearPrefs("lastCachedM0F1", this);
        PrefsHelper.clearPrefs("lastCachedM0F2", this);
        PrefsHelper.clearPrefs("lastCachedM1F0", this);
        PrefsHelper.clearPrefs("lastCachedM1F1", this);
        PrefsHelper.clearPrefs("lastCachedM1F2", this);
        PrefsHelper.clearPrefs("lastCachedM2F0", this);
        PrefsHelper.clearPrefs("lastCachedM2F1", this);
        PrefsHelper.clearPrefs("lastCachedM2F2", this);
        PrefsHelper.clearPrefs("lastCachedM3F0", this);
        PrefsHelper.clearPrefs("lastCachedM3F1", this);
        PrefsHelper.clearPrefs("lastCachedM3F2", this);
        PrefsHelper.clearPrefs("lastCachedM4F0", this);
        PrefsHelper.clearPrefs("lastCachedM4F1", this);
        PrefsHelper.clearPrefs("lastCachedM4F2", this);
    }

    private void decodeSharedInfo() {
        final View decodeChunk = getLayoutInflater().inflate(R.layout.chunk_edit_comment, null);
        final EditText inputtedInfo = decodeChunk.findViewById(R.id.editComment);
        final AlertDialog.Builder infoDecodeDialog = new AlertDialog.Builder(this);
        infoDecodeDialog.setTitle(R.string.submenu_main_share);
        infoDecodeDialog.setMessage(R.string.share_main_decode);
        infoDecodeDialog.setView(decodeChunk);
        infoDecodeDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
            // To be overwritten...
        });
        infoDecodeDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
            // Do nothing
        });

        final AlertDialog infoDecodeDialogCreated = infoDecodeDialog.create();
        infoDecodeDialogCreated.show();
        // Overwrite the positive button
        infoDecodeDialogCreated.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            try {
                final String inputtedString = inputtedInfo.getText().toString().trim();
                if (!inputtedString.isEmpty()) {
                    if (inputtedString.contains("paizhang.info/macindex/share")) {
                        // Call deeplink handling
                        decodeDeepLink(inputtedString, infoDecodeDialogCreated);
                    } else {
                        final int[] thisID = decodeStartedParam(inputtedString.split("\n")[0].trim());
                        if (thisID.length != 1) {
                            Log.w("infoDecodeDialog", "Unable to decode the requested information.");
                            Toast.makeText(this, R.string.share_main_decode_failed, Toast.LENGTH_LONG).show();
                        } else {
                            // Decoded successfully, call intent parser
                            infoDecodeDialogCreated.dismiss();
                            SpecsIntentHelper.sendIntent(thisID, thisID[0], this, false);
                        }
                    }
                }
            } catch (Exception e) {
                ExceptionHelper.handleException(this, e, "infoDecodeDialog", "Unable to set positive button. Likely illegal info string. Please reset the application. String is: "
                        + inputtedInfo.getText().toString().trim());
            }
        });
    }

    private void decodeDeepLink(final String deepLink, final AlertDialog parentDialog) {
        String[] machineParam = deepLink.split("\\?code=");
        Log.i("DeepLinkDecode", "Got data " + Arrays.toString(machineParam));
        if (machineParam.length == 2) {
            int[] decodedID = decodeStartedParam(machineParam[1].replace("_", " ").trim());
            if (decodedID.length != 1) {
                Log.w("DeepLinkDecode", "Unable to decode the requested link.");
                Toast.makeText(this, R.string.share_main_decode_failed, Toast.LENGTH_LONG).show();
            } else {
                // Decoded successfully, call intent parser
                if (parentDialog != null) {
                    // Dismiss parent dialog if present...
                    parentDialog.dismiss();
                }
                SpecsIntentHelper.sendIntent(decodedID, decodedID[0], this, false);
            }
        } else {
            Log.w("DeepLinkDecode", "Unable to process the link due to illegal parameter.");
            Toast.makeText(this, R.string.share_main_decode_failed, Toast.LENGTH_LONG).show();
        }
    }

    // Return an ID array with matched name. Input: suspected machine name.
    private int[] decodeStartedParam(final String param) {
        try {
            // Param must not be an empty string.
            if (param == null || param.isEmpty()) {
                throw new IllegalArgumentException();
            }
            return MainActivity.getMachineHelper().searchHelper("name", param.trim(),
                    "all", true, false);
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "MachineParamDecoder", "Unable to decode the parameter: " + param.trim());
            return new int[0];
        }
    }

    public static MachineHelper getMachineHelper() {
        return machineHelper;
    }

    public static Resources getRes() {
        return resources;
    }

    // Verify if the application was killed due to system's process termination.
    public static void validateOperation(final Context context) {
        if (machineHelper == null || database == null || resources == null || !database.isOpen()) {
            Log.w("MainValidate", "Process was killed. Reloading resources.");
            resources = context.getResources();
            reloadDatabase(context);
        }
    }

    // When there is an incomplete database query, reload the database.
    public static void reloadDatabase(final Context context) {
        Log.w("Database", "Reload requested.");
        if (BuildConfig.DEBUG) {
            Toast.makeText(context, "Database reload requested", Toast.LENGTH_SHORT).show();
        }
        closeDatabase();
        initDatabase(context);
    }

    public static boolean getMainState() {
        return isMainRunning;
    }
}
