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
class MachineHelper {

    /*
     * Updating categories (Ver. 4.0)
     * (1) Update the following number.
     * (2) Update the following filter string array.
     * (3) Add a new table to database.
     *
     * Updating filters (Ver. 4.0)
     * (1) Update the navigation code if needed.
     * (2) Update the following filter string array.
     *
     * Updating columns
     * (1) Update MH to adapt the new column.
     * (2) Update any code if needed.
     * (3) Add a new column to every table.
     */

    /* Set to actual quantity - 1.
     * Warning! In a loop should include itself.
     * Array Init should +1 to match actual quantity.
     */
    private static final int CATEGORIES_COUNT = 14;

    private SQLiteDatabase database;

    private Cursor[] categoryIndividualCursor;

    /* Machine ID starts from 0, ends total -1. */
    private int[] categoryIndividualCount;

    private int totalMachine = 0;

    private int totalConfig = 0;

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
     * category12: PowerBook Subnotebook
     * category13: PowerBook G3/G4
     * category14: iBook
     */
    private static final Map<Integer, Integer> CATEGORIES_NAMES;
    static {
        CATEGORIES_NAMES = new HashMap<>();
        CATEGORIES_NAMES.put(0, R.string.category0);
        CATEGORIES_NAMES.put(1, R.string.category1);
        CATEGORIES_NAMES.put(2, R.string.category2);
        CATEGORIES_NAMES.put(3, R.string.category3);
        CATEGORIES_NAMES.put(4, R.string.category4);
        CATEGORIES_NAMES.put(5, R.string.category5);
        CATEGORIES_NAMES.put(6, R.string.category6);
        CATEGORIES_NAMES.put(7, R.string.category7);
        CATEGORIES_NAMES.put(8, R.string.category8);
        CATEGORIES_NAMES.put(9, R.string.category9);
        CATEGORIES_NAMES.put(10, R.string.category10);
        CATEGORIES_NAMES.put(11, R.string.category11);
        CATEGORIES_NAMES.put(12,R.string.category12);
        CATEGORIES_NAMES.put(13, R.string.category13);
        CATEGORIES_NAMES.put(14, R.string.category14);
    }

    MachineHelper(final SQLiteDatabase thisDatabase) {
        database = thisDatabase;
        // Initialize cursors and perform a self check.
        categoryIndividualCount = new int[CATEGORIES_COUNT + 1];
        categoryIndividualCursor = new Cursor[CATEGORIES_COUNT + 1];
        for (int i = 0; i <= CATEGORIES_COUNT; i++) {
            categoryIndividualCursor[i] = database.query("category" + i, null,
                    null, null, null, null, null);
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
    /* SelfCheck was removed since Ver 4.0 */

    void suicide() {
        for (int i = 0; i <= CATEGORIES_COUNT; i++) {
            if (categoryIndividualCursor[i] != null) {
                categoryIndividualCursor[i].close();
                Log.i("MachineHelperSuicide", "Category cursor " + i + " closed successfully.");
            }
        }
    }

    // Get the total count of categories
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
            return CATEGORIES_NAMES.get(thisCategory);
        } catch (Exception e) {
            Log.e("MachineHelperGetCatName", "Failed with " + thisCategory);
            e.printStackTrace();
        }
        return 0;
    }
    /* Category description was removed since Ver. 4.0, June 12, 2020 at Shenyang, China */

    // Get total machines in a category.
    int getCategoryCount(final int thisCategory) {
        return categoryIndividualCount[thisCategory];
    }

    // Get start and end ID of a category. [start, end)
    int[] getCategoryStartEnd(final int thisCategory) {
        int start = 0;
        for (int i = 0; i < thisCategory; i++) {
            start += categoryIndividualCount[i];
        }
        int end = start + getCategoryCount(thisCategory);
        int[] toReturn = {start, end};
        return toReturn;
    }

    // Get specific position of a machine ID.
    int[] getPosition(final int thisMachine) {
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
        int machineID = 0;
        for (int i = 0; i < thisPosition[0]; i++) {
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
        return checkApplicability(categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("name")));
    }

    // Integrated with SoundHelper
    int[] getSound(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        String thisSound = categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("sound"));
        int[] sound = {0, 0};
        // NullSafe
        if (thisSound == null) {
            return sound;
        }
        Log.i("MachineHelperGetSound", "Get ID " + thisSound);
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
        }
        return sound;
    }

    String getProcessor(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return checkApplicability(categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("processor")));
    }

    String getMaxRam(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return checkApplicability(categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("maxram")));
    }

    String getYear(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return checkApplicability(categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("year")));
    }

    String getModel(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return checkApplicability(categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("model")));
    }

    File getPicture(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        byte[] thisBlob = categoryIndividualCursor[position[0]]
                .getBlob(categoryIndividualCursor[position[0]].getColumnIndex("pic"));
        // Old code from my old friend was not modified.
        String path = "/";
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

    // Should return "N" if EveryMac link is not available.
    String getConfig(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        String toReturn = categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("links"));
        // NullSafe
        if (toReturn == null) {
            return "N";
        } else {
            return toReturn;
        }
    }

    String getType(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return checkApplicability(categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("type")));
    }

    // Refer to SpecsActivity for a documentation.
    int getProcessorTypeImage(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        String thisProcessorImage = categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("processorid"));
        Log.i("MHGetProcessorImageType", "Get ID " + thisProcessorImage);
        // NullSafe
        if (thisProcessorImage == null) {
            return 0;
        }
        String[] thisImages = thisProcessorImage.split(",");
        switch (thisImages[0]) {
            case "68k":
                return R.drawable.motorola;
            case "ppc":
                return R.drawable.powerpc;
            default:
                Log.i("MHGetProcessorImageType", "No processor image for ID " + thisProcessorImage);
        }
        return 0;
    }

    int[][] getProcessorImage(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        String thisProcessorImage = categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("processorid"));
        Log.i("MHGetProcessorImage", "Get ID " + thisProcessorImage);
        // NullSafe
        if (thisProcessorImage == null) {
            return new int[][] {{0}, {0}};
        }
        String[] thisImages = thisProcessorImage.split(",");
        int[][] toReturn = new int[thisImages.length][];
        for (int i = 0; i < thisImages.length; i++) {
            switch (thisImages[i]) {
                case "740":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.ppc740;
                    break;
                case "750":
                    toReturn[i] = new int[2];
                    toReturn[i][0] = R.drawable.mpc750;
                    toReturn[i][1] = R.drawable.ppc750l;
                    break;
                case "750cx":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.ppc750cx;
                    break;
                case "750cxe":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.ppc750cxe;
                    break;
                case "755":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.mpc755;
                    break;
                case "750fx":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.ppc750fx;
                    break;
                case "7400":
                    toReturn[i] = new int[2];
                    toReturn[i][0] = R.drawable.mpc7400;
                    toReturn[i][1] = R.drawable.ppc7400;
                    break;
                case "7410":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.mpc7410;
                    break;
                case "7440":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.mpc7440;
                    break;
                case "7445":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.mpc7445;
                    break;
                case "7450":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.mpc7450;
                    break;
                case "7455":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.mpc7455;
                    break;
                case "7447":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.mpc7447a;
                    break;
                case "970":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.ppc970;
                    break;
                case "970fx":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.ppc970fx;
                    break;
                case "970mp":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.ppc970mp;
                    break;
                default:
                    Log.i("MHGetProcessorImage", "No processor image for ID " + thisProcessorImage);
                    toReturn[i] = new int[1];
                    toReturn[i][0] = 0;
                    break;
            }
        }
        return toReturn;
    }

    // NullSafe
    private static String checkApplicability(final String thisSpec) {
        if (thisSpec == null || thisSpec.equals("N")) {
            return MainActivity.getRes().getString(R.string.not_applicable);
        } else {
            return thisSpec;
        }
    }

    // For search use. Return sets of positions (positionCount/category ID/remainder).
    int[][] searchHelper(final String columnName, final String searchInput) {
        Log.i("MHSearchHelper", "Get parameter " + columnName + ", " + searchInput);
        // Raw results (categoryID/remainders)
        int[][] rawResults = new int[CATEGORIES_COUNT + 1][];

        // Setup temp cursor of each category for a query.
        try {
            for (int i = 0; i <= CATEGORIES_COUNT; i++) {
                Cursor thisSearchIndividualCursor = database.query("category" + i,
                        null, columnName +" LIKE ? ",
                        new String[]{"%" + searchInput + "%"},
                        null, null, null);
                rawResults[i] = new int[thisSearchIndividualCursor.getCount()];
                Log.i("MHSearchHelper", "Category " + i + "get " + thisSearchIndividualCursor.getCount() + " result(s).");
                // Write raw query results.
                int previousCount = 0;
                while (thisSearchIndividualCursor.moveToNext()) {
                    rawResults[i][previousCount] = thisSearchIndividualCursor.getInt(thisSearchIndividualCursor.getColumnIndex("id"));
                    previousCount++;
                }
                thisSearchIndividualCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convert raw results to positions.
        int resultTotalCount = 0;
        for (int[] thisRawResult : rawResults) {
            if (thisRawResult != null) {
                resultTotalCount += thisRawResult.length;
                for (int j = 0; j < thisRawResult.length; j++) {
                }
            }
            Log.i("MHSearchHelper", "Get " + resultTotalCount + " result(s).");
        }

        // Sets of positions (positionCount/category ID/remainder)
        int[][] finalPositions = new int[resultTotalCount][2];
        int previousCount = 0;
        for (int j = 0; j <= CATEGORIES_COUNT; j++) {
            for (int k = 0; k < rawResults[j].length; k++) {
                finalPositions[previousCount][0] = j;
                finalPositions[previousCount][1] = rawResults[j][k];
                previousCount++;
            }
        }
        return finalPositions;
    }
}
