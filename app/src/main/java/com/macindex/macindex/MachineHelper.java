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

    private int[] configIndividualCount;

    private boolean status = true;

    private int totalMachine = 0;

    MachineHelper(final SQLiteDatabase thisDatabase, final int setCategoryCount) {
        // Set basic parameters.
        database = thisDatabase;
        categoryCount = setCategoryCount;

        // Initialize cursors.
        categoryIndividualCount= new int[categoryCount + 1];
        categoryIndividualCursor = new Cursor[categoryCount + 1];
        for (int i = 0; i <= categoryCount; i++) {
            categoryIndividualCursor[i] = database.query("category" + i, null,
                    null, null, null, null, null);
            categoryIndividualCursor[i].moveToFirst();
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

    int getMachineCount() {
        return totalMachine;
    }

    int getConfigCount() {

    }

    int getCategory(final int thisMachine) {
        return 0;
    }

    int getCursorMoveCount(final int thisCategory, final int thisMachine) {

    }

    int getThisConfigCount(final int thisMachine) {

    }

    int findByConfig (final int thisConfig) {

    }

    String getName(final int thisMachine) {
        return null;
    }

    String getSound(final int thisMachine) {

    }

    String getProcessor(final int thisMachine) {

    }

    String getMaxRam(final int thisMachine) {

    }

    String getYear(final int thisMachine) {

    }

    String getModel(final int thisMachine) {

    }

    byte[] getPicture(final int thisMachine) {

    }
    String getLinks(final int thisMachine) {

    }
}
