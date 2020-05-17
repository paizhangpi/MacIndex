package com.macindex.macindex;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/*
 * MacIndex MachineHelper.
 * Helps with ID-based flexible database query.
 * First built May 12, 2020.
 */
public class MachineHelper {

    /* Set to actual quantity */
    private static final int COLUMNS_COUNT = 9;

    private SQLiteDatabase database;

    private Cursor[] categoryIndividualCursor;

    private int[] categoryIndividualCount;

    private boolean status = true;

    private int totalMachine = 0;

    private int totalConfig = 0;

    /* Set to actual quantity - 1 */
    private static final int CATEGORIES_COUNT = 13;

    /**
     * Categories Reference List since 3.2
     *
     * category0:  Compact Macintosh
     * category1:  Macintosh II
     * category2:  Macintosh LC
     * category3:  Macintosh Centris
     * category4:  Macintosh Quadra
     * category5:  Macintosh Performa
     * category6:  Power Macintosh
     * category7:  Power Mac G3/G4/G5
     * category8:  iMac
     * category9:  eMac
     * category10: Mac mini
     * category11: PowerBook
     * category12: PowerBook G3/G4
     * category13: iBook
     *
     * The following two hash maps need to be updated MANUALLY.
     */
    private static final Map<Integer, Integer> categoryName;
    static {
        categoryName = new HashMap<>();
        categoryName.put(0, R.string.category0);
        categoryName.put(1, R.string.category1);
        categoryName.put(2, R.string.category2);
        categoryName.put(3, R.string.category3);
        categoryName.put(4, R.string.category4);
        categoryName.put(5, R.string.category5);
        categoryName.put(6, R.string.category6);
        categoryName.put(7, R.string.category7);
        categoryName.put(8, R.string.category8);
        categoryName.put(9, R.string.category9);
    }

    private static final Map<Integer, Integer> categoryDescription;
    static {
        categoryDescription = new HashMap<>();
        categoryDescription.put(0, R.string.category0_description);
        categoryDescription.put(1, R.string.category1_description);
        categoryDescription.put(2, R.string.category2_description);
        categoryDescription.put(3, R.string.category3_description);
        categoryDescription.put(4, R.string.category4_description);
        categoryDescription.put(5, R.string.category5_description);
        categoryDescription.put(6, R.string.category6_description);
        categoryDescription.put(7, R.string.category7_description);
        categoryDescription.put(8, R.string.category8_description);
        categoryDescription.put(9, R.string.category9_description);
    }


    MachineHelper(final SQLiteDatabase thisDatabase) {
        database = thisDatabase;

        // Initialize cursors and perform a self check.
        Cursor cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        int categoryCount = 0;
        if (cursor.moveToFirst()) {
            while ( !cursor.isAfterLast() ) {
                if (!cursor.getString(0).equals("category" + categoryCount)) {
                    Log.e("MachineHelperInit", "Wrong table name.");
                    status = false;
                    return;
                }
                categoryCount++;
                cursor.moveToNext();
            }
        }
        if (categoryCount - 1 != CATEGORIES_COUNT) {
            Log.e("MachineHelperInit", "Category total count preset mismatch with actual quantity.");
            status = false;
            return;
        }
        categoryIndividualCount = new int[CATEGORIES_COUNT + 1];
        categoryIndividualCursor = new Cursor[CATEGORIES_COUNT + 1];
        for (int i = 0; i <= CATEGORIES_COUNT; i++) {
            categoryIndividualCursor[i] = database.query("category" + i, null,
                    null, null, null, null, null);
            if (categoryIndividualCursor[i].getColumnCount() != COLUMNS_COUNT) {
                Log.e("MachineHelperInit", "Columns count preset mismatch with actual quantity.");
                status = false;
                return;
            }
            int thisCursorCount = categoryIndividualCursor[i].getCount();
            categoryIndividualCount[i] = thisCursorCount;
            totalMachine += thisCursorCount;
            Log.i("MachineHelperInit", "Category cursor " + i
                    + " loaded with row count " + thisCursorCount
                    + ", accumulated total row count " + totalMachine);
        }

        // Initialize configurations
        for (int i = 0; i < totalMachine; i++) {
            totalConfig += getThisConfigCount(i);
        }
        Log.i("MachineHelperTotCfg", "Initialized with " + totalConfig + " configurations.");
    }

    boolean selfCheck() {
        return status;
    }

    void suicide() {
        if (selfCheck()) {
            for (int i = 0; i <= CATEGORIES_COUNT; i++) {
                categoryIndividualCursor[i].close();
                Log.i("MachineHelperSuicide", "Category cursor " + i + "closed successfully.");
            }
            database.close();
        }
    }

    // Get the total count of a category
    int getCategoryTotalCount() {
        return CATEGORIES_COUNT;
    }

    // Get total machines. For usage of random access.
    int getMachineCount() {
        return totalMachine;
    }

    // Get total configurations. For usage of random access.
    int getConfigCount() {
        return totalConfig;
    }

    // Get the name of a category
    int getCategoryName(final int thisCategory) {
        try {
            return categoryName.get(thisCategory);
        } catch (Exception e) {
            Log.e("MachineHelperGetCatName", "Failed with " + thisCategory);
            e.printStackTrace();
        }
        return 0;
    }

    // Get the description of a category
    int getCategoryDescription(final int thisCategory) {
        try {
            return categoryDescription.get(thisCategory);
        } catch (Exception e) {
            Log.e("MachineHelperGetCatDesp", "Failed with " + thisCategory);
            e.printStackTrace();
        }
        return 0;
    }

    // Get total machines in a category.
    int getCategoryCount(final int thisCategory) {
        return categoryIndividualCount[thisCategory];
    }

    // Get specific position of a machine ID.
    private int[] getPosition(final int thisMachine) {
        // Category ID / Remainder
        int[] position = {0, thisMachine};
        while (position[0] <= CATEGORIES_COUNT) {
            if (position[1] >= categoryIndividualCount[position[0]]) {
                position[1] -= categoryIndividualCount[position[0]];
                position[0]++;
            } else {
                break;
            }
        }
        return position;
    }

    private int getThisConfigCount(final int thisMachine) {
        final String[] configGroup = getConfig(thisMachine).split(";");
        return configGroup.length;
    }

    // Get machine ID by a specific position.
    int findByPosition(final int[] thisPosition) {
        int[] position = thisPosition;
        int machineID = 0;
        for (int i = 0; i < position[0]; i++) {
            machineID += categoryIndividualCount[i];
        }
        return machineID + thisPosition[1];
    }

    // Get config position by this config number. Return null if this config number is invalid.
    int[] findByConfig(final int thisConfig) {
        // Machine ID / Remainder
        int[] configPosition = {0, thisConfig};
        while (configPosition[0] < totalMachine) {
            int thisConfigCount = getThisConfigCount(configPosition[0]);
            if (configPosition[1] >= thisConfigCount) {
                configPosition[1] -= thisConfigCount;
                configPosition[0]++;
            } else {
                return configPosition;
            }
        }
        Log.e("MachineHelperFndByCfg", "Can't find such ID, returning null.");
        return null;
    }

    String getName(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("name"));
    }

    // Integrated with SoundHelper
    int[] getSound(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        String thisSound = categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("sound"));
        Log.i("MachineHelperGetSound", "Get ID " + thisSound);
        int[] sound = new int[2];
        switch (thisSound) {
            case "0":
                sound[0] = R.raw.mac128;
                break;
            case "1":
                sound[0] = R.raw.macii;
                break;
            case "2":
                sound[0] = R.raw.maclc;
                break;
            case "3":
                sound[0] = R.raw.quadra;
                break;
            case "4":
                sound[0] = R.raw.quadraav;
                break;
            case "5":
                sound[0] = R.raw.powermac6100;
                break;
            case "6":
                sound[0] = R.raw.powermac5000;
                break;
            case "7":
            case "PB":
                sound[0] = R.raw.powermac;
                break;
            case "8":
                sound[0] = R.raw.newmac;
                break;
            case "9":
                sound[0] = R.raw.tam;
                break;
            default:
                Log.i("MachineHelperGetSound", "No startup sound for ID " + thisSound);
                sound[0] = 0;
        }
        switch (thisSound) {
            case "1":
                sound[1] = R.raw.macii_death;
                break;
            case "2":
            case "3":
            case "PB":
                sound[1] = R.raw.maclc_death;
                break;
            case "4":
                sound[1] = R.raw.quadraav_death;
                break;
            case "5":
                sound[1] = R.raw.powermac6100_death;
                break;
            case "6":
                sound[1] = R.raw.powermac5000_death;
                break;
            case "7":
            case "9":
                sound[1] = R.raw.powermac_death;
                break;
            default:
                Log.i("MachineHelperGetDthSnd", "No death sound for ID " + thisSound);
                sound[1] = 0;
                break;
        }
        return sound;
    }

    String getProcessor(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("processor"));
    }

    String getMaxRam(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("maxram"));
    }

    String getYear(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("year"));
    }

    String getModel(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("model"));
    }

    File getPicture(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        byte[] thisBlob = categoryIndividualCursor[position[0]]
                .getBlob(categoryIndividualCursor[position[0]].getColumnIndex("pic"));
        // Old code from my old friend was not modified.
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
                }
                path = file.getPath();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new File(path);
    }

    String getConfig(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("links"));
    }
}
