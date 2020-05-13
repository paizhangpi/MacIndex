package com.macindex.macindex;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

        // Initialize cursors.
        categoryIndividualCount= new int[categoryCount];
        categoryIndividualCursor = new Cursor[categoryCount];
        for (int i = 0; i <= categoryCount; i++) {
            categoryIndividualCursor[i] = database.query("category" + i, null,
                    null, null, null, null, null);
            if (categoryIndividualCursor[i].getColumnCount() != COLUMNS_COUNT) {
                status = false;
                return;
            }
            int thisCursorCount = categoryIndividualCursor[i].getColumnCount();
            categoryIndividualCount[i] = thisCursorCount;
            totalMachine += thisCursorCount;
        }
    }

    boolean selfCheck(final Cursor thisCursor) {
        return status;
    }

    // Get total Machines. For usage of random access.
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
        }
        return totalConfig;
    }

    // Get specific position of this machine ID.
    private int[] getPosition(final int thisMachine) {
        int[] position = new int[2];
        // Category ID
        position[0] = 0;
        // Remainder
        position[1] = thisMachine;
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

    // Get machine ID by this config number. Return -1 if this config number is invalid.
    int findByConfig (final int thisConfig) {
        int config = thisConfig;
        int machineID = 0;
        while (machineID < totalMachine) {
            int thisConfigCount = getThisConfigCount(machineID);
            if (config > thisConfigCount) {
                config -= thisConfigCount;
                machineID++;
            } else {
                return machineID;
            }
        }
        return -1;
    }

    // Helpful for cursor usage.
    private Object getByColumn(final int thisMachine, final String columnName) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex(columnName));
    }

    String getName(final int thisMachine) {
        return (String) getByColumn(thisMachine, "name");
    }

    String getSound(final int thisMachine) {
        return (String) getByColumn(thisMachine, "sound");
    }

    String getProcessor(final int thisMachine) {
        return (String) getByColumn(thisMachine, "processor");
    }

    String getMaxRam(final int thisMachine) {
        return (String) getByColumn(thisMachine, "maxram");
    }

    String getYear(final int thisMachine) {
        return (String) getByColumn(thisMachine, "year");
    }

    String getModel(final int thisMachine) {
        return (String) getByColumn(thisMachine, "model");
    }

    byte[] getPicture(final int thisMachine) {
        return (byte[]) getByColumn(thisMachine, "pic");
    }
    String getConfig(final int thisMachine) {
        return (String) getByColumn(thisMachine, "links");
    }
}
