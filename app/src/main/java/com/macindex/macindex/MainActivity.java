package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
 * https://paizhang.info/MacIndexCN
 * https://paizhang.info/MacIndex
 * https://github.com/paizhangpi/MacIndex
 *
 * 1st Major Update May 12, 2020 at Champaign, Illinois, U.S.A.
 * 2nd Major Update June 13, 2020 at Shenyang, Liaoning, P.R.C.
 */
public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database;

    private static MachineHelper machineHelper;

    private static SharedPreferences prefs = null;

    private static Resources resources = null;

    private DrawerLayout mDrawerLayout = null;

    private String thisManufacturer = null;

    private String thisFilter = null;

    private String[][] thisFilterString = {};

    private int[][] loadPositions = {};

    private int machineLoadedCount = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("MACINDEX_PREFS", Activity.MODE_PRIVATE);
        resources = getResources();
        thisManufacturer = prefs.getString("thisManufacturer", "all");
        thisFilter = prefs.getString("thisFilter", "names");

        initDatabase();
        initMenu();
        initInterface();
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            Log.e("initDatabase", "Initialize failed!!");
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
            /* Set the groups here */
            String[] groupContent = {getString(R.string.menu_group0),
                        getString(R.string.menu_group1), getString(R.string.menu_group2)};
            /* Set the filters here*/
            String[] viewContent = {getString(R.string.menu_view1),
                    getString(R.string.menu_view2), getString(R.string.menu_view3)};
            /* Main menu */
            String[] menuContent = {getString(R.string.menu_search),
                    getString(R.string.menu_random), getString(R.string.menu_about_settings)};

            final ListView groupList = findViewById(R.id.group_list);
            final ListView viewList = findViewById(R.id.view_list);
            final ListView menuList = findViewById(R.id.menu_list);

            groupList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupContent));
            viewList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, viewContent));
            menuList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuContent));

            // Set listView listeners accordingly.
            // Manufacturer menu item listeners.
            groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                    switch (position) {
                        case 0:
                            thisManufacturer = "all";
                            prefs.edit().putString("thisManufacturer", "all").apply();
                            prefs.edit().putInt("groupPosition", position).apply();
                            prefs.edit().putString("MainTitleII", getString(R.string.menu_group0)).apply();
                            break;
                        case 1:
                            thisManufacturer = "appledesktop";
                            prefs.edit().putString("thisManufacturer", "appledesktop").apply();
                            prefs.edit().putInt("groupPosition", position).apply();
                            prefs.edit().putString("MainTitleII", getString(R.string.menu_group1)).apply();
                            break;
                        case 2:
                            thisManufacturer = "applelaptop";
                            prefs.edit().putString("thisManufacturer", "applelaptop").apply();
                            prefs.edit().putInt("groupPosition", position).apply();
                            prefs.edit().putString("MainTitleII", getString(R.string.menu_group2)).apply();
                            break;
                        default:
                            Log.e("MainDrawerGroup", "This should not happen.");
                    }
                    mDrawerLayout.closeDrawers();
                    refresh();
                }
            });

            // Filter menu item listeners.
            viewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                    switch (position) {
                        case 0:
                            thisFilter = "names";
                            prefs.edit().putInt("viewPosition", position).apply();
                            prefs.edit().putString("thisFilter", "names").apply();
                            break;
                        case 1:
                            thisFilter = "processors";
                            prefs.edit().putInt("viewPosition", position).apply();
                            prefs.edit().putString("thisFilter", "processors").apply();
                            break;
                        case 2:
                            thisFilter = "years";
                            prefs.edit().putInt("viewPosition", position).apply();
                            prefs.edit().putString("thisFilter", "years").apply();
                            break;
                        default:
                            Log.e("MainDrawerFilter", "This should not happen.");
                    }
                    mDrawerLayout.closeDrawers();
                    refresh();
                }
            });

            // Main menu item listeners.
            menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                    switch (position) {
                        case 0:
                            startActivity(new Intent(MainActivity.this, SearchActivity.class));
                            break;
                        case 1:
                            openRandom();
                            break;
                        case 2:
                            startActivity(new Intent(MainActivity.this, SettingsAboutActivity.class));
                            break;
                        default:
                            Log.e("MainDrawerMenu", "This should not happen.");
                    }
                    mDrawerLayout.closeDrawers();
                }
            });

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
                    setTitle(prefs.getString("MainTitleII", getString(R.string.menu_group0)));
                }

                @Override
                public void onDrawerStateChanged(final int newState) {
                    for (int i = 0; i < groupList.getChildCount(); i++) {
                        if (prefs.getInt("groupPosition", 0) == i) {
                            final View v = groupList.getChildAt(i);
                            v.setClickable(true);
                            v.setBackgroundColor(getColor(R.color.colorPrimary));
                            final TextView tv = v.findViewById(android.R.id.text1);
                            tv.setTextColor(Color.WHITE);
                        } else {
                            final View v = groupList.getChildAt(i);
                            v.setClickable(false);
                            v.setBackgroundColor(Color.TRANSPARENT);
                            final TextView tv = v.findViewById(android.R.id.text1);
                            tv.setTextColor(Color.BLACK);
                        }
                    }
                    for (int i = 0; i < viewList.getChildCount(); i++) {
                        if (prefs.getInt("viewPosition", 0) == i) {
                            final View v = viewList.getChildAt(i);
                            v.setClickable(true);
                            v.setBackgroundColor(getColor(R.color.colorPrimary));
                            final TextView tv = v.findViewById(android.R.id.text1);
                            tv.setTextColor(Color.WHITE);
                        } else {
                            final View v = viewList.getChildAt(i);
                            v.setClickable(false);
                            v.setBackgroundColor(Color.TRANSPARENT);
                            final TextView tv = v.findViewById(android.R.id.text1);
                            tv.setTextColor(Color.BLACK);
                        }
                    }
                    // If EveryMac enabled, random should be disabled
                    if (prefs.getBoolean("isOpenEveryMac", false)) {
                        menuList.getChildAt(1).setEnabled(false);
                        menuList.getChildAt(1).setClickable(true);
                    } else {
                        menuList.getChildAt(1).setEnabled(true);
                        menuList.getChildAt(1).setClickable(false);
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
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
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
            setTitle(prefs.getString("MainTitleII", getString(R.string.menu_group0)));
            // Parent layout of all categories.
            final LinearLayout categoryContainer = findViewById(R.id.categoryContainer);
            categoryContainer.removeAllViews();
            // Get filter string and positions.
            thisFilterString = machineHelper.getFilterString(thisFilter);
            loadPositions = machineHelper.filterSearchHelper(thisFilter, thisManufacturer);
            // Set up each category.
            for (int i = 0; i < loadPositions.length; i++) {
                final View categoryChunk = getLayoutInflater().inflate(R.layout.chunk_category, null);
                final View dividerChunk = getLayoutInflater().inflate(R.layout.chunk_divider, null);
                final LinearLayout categoryChunkLayout = categoryChunk.findViewById(R.id.categoryInfoLayout);
                final TextView categoryName = categoryChunk.findViewById(R.id.category);
                if (loadPositions[i].length == 0) {
                    // No result to display.
                    categoryChunkLayout.removeAllViews();
                } else {
                    categoryName.setText(thisFilterString[2][i]);

                    /*
                     *  Here's some old code from my teammate.
                     *  We worked together in UGL in the midnight; I won't forget you.
                     *  Hopefully we will meet again...
                     */
                    for (int j = 0; j < categoryChunkLayout.getChildCount(); j++) {
                        final View v = categoryChunkLayout.getChildAt(j);
                        v.setClickable(true);
                        v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                int visa = 0;

                                for (int j = 0; j < categoryChunkLayout.getChildCount(); j++) {
                                    final View vi = categoryChunkLayout.getChildAt(j);
                                    if (vi.getVisibility() == View.VISIBLE) {
                                        visa++;
                                    }
                                }

                                if (visa > 1) {
                                    for (int j = 1; j < categoryChunkLayout.getChildCount(); j++) {
                                        final View vi = categoryChunkLayout.getChildAt(j);
                                        vi.setVisibility(View.GONE);
                                    }
                                } else {
                                    for (int j = 1; j < categoryChunkLayout.getChildCount(); j++) {
                                        final View vi = categoryChunkLayout.getChildAt(j);
                                        vi.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        });
                    }
                    initCategory(categoryChunkLayout, i);
                    categoryContainer.addView(categoryChunk);
                    categoryContainer.addView(dividerChunk);
                }
            }
            // Remove the last divider.
            categoryContainer.removeViewAt(categoryContainer.getChildCount() - 1);
            // Basic functionality was finished on 16:12 CST, Dec 2, 2019.
            Log.w("MainActivity", "Initialized with " + machineLoadedCount + " machines.");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            Log.e("initDatabase", "Initialize failed!!");
        }
    }

    private void initCategory(final LinearLayout currentLayout, final int category) {
        try {
            for (int i = 0; i < loadPositions[category].length; i++) {
                final View mainChunk = getLayoutInflater().inflate(R.layout.chunk_main, null);
                mainChunk.setVisibility(View.GONE);
                final TextView machineName = mainChunk.findViewById(R.id.machineName);
                final TextView machineYear = mainChunk.findViewById(R.id.machineYear);

                // Adapt MachineHelper.
                final int machineID = loadPositions[category][i];

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
                            sendIntent(loadPositions[category], machineID);
                        }
                    }
                });

                machineYear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View unused) {
                        if (prefs.getBoolean("isOpenEveryMac", false)) {
                            loadLinks(thisName, thisLinks);
                        } else {
                            sendIntent(loadPositions[category], machineID);
                        }
                    }
                });
                currentLayout.addView(mainChunk);
                machineLoadedCount++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            Log.e("initCategory", "Initialize Category " + category + " failed!!");
        }
    }

    // Keep compatible with SearchActivity.
    private void sendIntent(final int[] thisCategory, final int thisMachineID) {
        Intent intent = new Intent(this, SpecsActivity.class);
        intent.putExtra("thisCategory", thisCategory);
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
                // Only one option, launch EveryMac directly.
                startBrowser(linkGroup[0].split(",")[0], linkGroup[0].split(",")[1]);
            } else {
                final AlertDialog.Builder linkDialog = new AlertDialog.Builder(this);
                linkDialog.setTitle(thisName);
                linkDialog.setMessage(getResources().getString(R.string.link_message));
                // Setup each option in dialog.
                final View linkChunk = getLayoutInflater().inflate(R.layout.chunk_links, null);
                final RadioGroup linkOptions = linkChunk.findViewById(R.id.option);
                for (int i = 0; i < linkGroup.length; i++) {
                    final RadioButton linkOption = new RadioButton(this);
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
            final Intent browser = new Intent(Intent.ACTION_VIEW);
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
                // This should not happen.
                throw new IllegalStateException();
            } else {
                int machineID = 0;
                if (!prefs.getBoolean("isRandomAll", false)) {
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
                    for (int i = 0; i < loadPositions.length; i++) {
                        if (randomCode >= loadPositions[i].length) {
                            randomCode -= loadPositions[i].length;
                        } else {
                            machineID = loadPositions[i][randomCode];
                            break;
                        }
                    }
                }
                Log.i("RandomAccess", "Machine ID " + machineID);
                sendIntent(new int[]{machineID}, machineID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
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

    public static SharedPreferences getPrefs() {
        return prefs;
    }

    public static Resources getRes() {
        return resources;
    }
}
