package com.macindex.macindex;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/*
 * MacIndex MachineHelper.
 * Helps with ID-based flexible database query.
 * First built May 12, 2020.
 */
public class MachineHelper {
    // Set to actual quantity.
    private static final int COLUMNS_COUNT = 9;

    private SQLiteDatabase database;

    private int categoryCount;

    private Cursor[] categoryIndividualCursor;

    private int[] categoryIndividualCount;

    private boolean status = true;

    private int totalMachine = 0;

    private int totalConfig = -1;

    MachineHelper(final SQLiteDatabase thisDatabase, final int setCategoryCount) {
        // Set basic parameters.
        database = thisDatabase;
        categoryCount = setCategoryCount;

        // Initialize cursors and perform a self check.
        categoryIndividualCount = new int[categoryCount + 1];
        categoryIndividualCursor = new Cursor[categoryCount + 1];
        for (int i = 0; i <= categoryCount; i++) {
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
    }

    boolean selfCheck() {
        return status;
    }

    void suicide() {
        if (selfCheck()) {
            for (int i = 0; i <= categoryCount; i++) {
                categoryIndividualCursor[i].close();
                Log.i("MachineHelperSuicide", "Category cursor " + i + "closed successfully.");
            }
            database.close();
        }
    }

    // Get total machines. For usage of random access.
    int getMachineCount() {
        return totalMachine;
    }

    // Get total configurations. For usage of random access.
    int getConfigCount() {
        if (totalConfig == -1) {
            // See if this is uninitialized..
            for (int i = 0; i < totalMachine; i++) {
                totalConfig += getThisConfigCount(i);
            }
            Log.i("MachineHelperTotCfg", "Initialized with " + totalConfig + " configurations.");
        }
        return totalConfig;
    }

    // Get total machines in a category.
    int getCategoryCount(final int thisCategory) {
        return categoryIndividualCount[thisCategory];
    }

    // Get specific position of a machine ID.
    private int[] getPosition(final int thisMachine) {
        // Category ID / Remainder
        int[] position = {0, thisMachine};
        while (position[0] <= categoryCount) {
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
