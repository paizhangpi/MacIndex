package com.macindex.macindex;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/*
 * MacIndex MachineHelper, Java playground.
 * May 12, 2020.
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

    boolean selfCheck(final Cursor thisCursor) {
        return status;
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

    // Get machine ID by this config number. Return -1 if this config number is invalid.
    int[] findByConfig(final int thisConfig) {
        // Machine ID / Remainder
        int[] config = {0, thisConfig};
        while (config[0] < totalMachine) {
            int thisConfigCount = getThisConfigCount(config[0]);
            if (config[1] > thisConfigCount) {
                config[1] -= thisConfigCount;
                config[0]++;
            } else {
                return config;
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

    String getSound(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("sound"));
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

    byte[] getPicture(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return categoryIndividualCursor[position[0]]
                .getBlob(categoryIndividualCursor[position[0]].getColumnIndex("pic"));
    }
    String getConfig(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("links"));
    }
}
