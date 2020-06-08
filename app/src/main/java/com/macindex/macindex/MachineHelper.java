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

    /*
     * Updating categories
     * (1) Update the following number.
     * (2) Add string resources.
     * (3) Update the following two hash maps.
     * (4) Add a new table to database.
     *
     * Updating columns
     * (1) Update the following number.
     * (2) Update MH to adapt the new column.
     * (3) Update any code if needed.
     * (4) Add a new column to every table.
     */

    /* Set to actual quantity - 1 */
    private static final int CATEGORIES_COUNT = 14;

    /* Set to actual quantity */
    private static final int COLUMNS_COUNT = 11;

    private SQLiteDatabase database;

    private Cursor[] categoryIndividualCursor;

    private int[] categoryIndividualCount;

    private boolean status = true;

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

    private static final Map<Integer, Integer> CATEGORIES_DESCRIPTIONS;
    static {
        CATEGORIES_DESCRIPTIONS = new HashMap<>();
        CATEGORIES_DESCRIPTIONS.put(0, R.string.category0_description);
        CATEGORIES_DESCRIPTIONS.put(1, R.string.category1_description);
        CATEGORIES_DESCRIPTIONS.put(2, R.string.category2_description);
        CATEGORIES_DESCRIPTIONS.put(3, R.string.category3_description);
        CATEGORIES_DESCRIPTIONS.put(4, R.string.category4_description);
        CATEGORIES_DESCRIPTIONS.put(5, R.string.category5_description);
        CATEGORIES_DESCRIPTIONS.put(6, R.string.category6_description);
        CATEGORIES_DESCRIPTIONS.put(7, R.string.category7_description);
        CATEGORIES_DESCRIPTIONS.put(8, R.string.category8_description);
        CATEGORIES_DESCRIPTIONS.put(9, R.string.category9_description);
        CATEGORIES_DESCRIPTIONS.put(10, R.string.category10_description);
        CATEGORIES_DESCRIPTIONS.put(11, R.string.category11_description);
        CATEGORIES_DESCRIPTIONS.put(12,R.string.category12_description);
        CATEGORIES_DESCRIPTIONS.put(13, R.string.category13_description);
        CATEGORIES_DESCRIPTIONS.put(14, R.string.category14_description);
    }


    MachineHelper(final SQLiteDatabase thisDatabase) {
        database = thisDatabase;
        // Initialize cursors and perform a self check.
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
                Log.i("MachineHelperSuicide", "Category cursor " + i + " closed successfully.");
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
            return CATEGORIES_NAMES.get(thisCategory);
        } catch (Exception e) {
            Log.e("MachineHelperGetCatName", "Failed with " + thisCategory);
            e.printStackTrace();
        }
        return 0;
    }

    // Get the description of a category
    int getCategoryDescription(final int thisCategory) {
        try {
            return CATEGORIES_DESCRIPTIONS.get(thisCategory);
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
        // NullSafe
        if (thisBlob == null) {
            return new File("/");
        }
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
            int[][] toReturn = {{0},{0}};
            return toReturn;
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
    private String checkApplicability(String thisSpec) {
        if (thisSpec == null || thisSpec.equals("N")) {
            return MainActivity.getRes().getString(R.string.not_applicable);
        } else {
            return thisSpec;
        }
    }
}
