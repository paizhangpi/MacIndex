package com.macindex.macindex;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Vector;

/*
 * MacIndex MachineHelper.
 * Helps with ID-based flexible database query.
 * First built May 12, 2020.
 *
 * Category name and description was removed since Ver. 4.0
 * Category start and end was removed since Ver. 4.0
 * Based on searching since Ver. 4.0
 * Total configuration removed since Ver. 4.5
 * Config count was removed since Ver. 4.5
 * convertToDatabaseCategoryID was removed since Ver. 4.5
 * Find by Config was removed since Ver. 4.5
 * Category Individual Cursor was removed since Ver. 4.5
 * Changed Cursor behavior since Ver. 4.5
 */
class MachineHelper {

    /*
     * Updating categories
     * (1) Update the following array by order.
     * (2) Update the MH manufacturer method.
     * (3) Update the MH filter method.
     * (4) Update String resources.
     * (5) Update the MainActivity.
     * (6) Update the SearchActivity.
     * (7) Make change to the database.
     * (8) Update the following information.
     *
     * Updating filters
     * (1) Update the MH filter method.
     * (2) Update String resources.
     * (3) Update the MainActivity.
     * (4) Update the SearchActivity.
     * (5) Update the following information.
     *
     * Updating columns
     * (1) Update MH to adapt the new column.
     * (2) Update String resources.
     * (3) Update SpecActivity to get the data.
     * (4) Add a new column to every table.
     */

    private static final String[] CATEGORIES_LIST = {"compact_mac", "mac_ii", "mac_lc", "mac_quadra",
            "mac_performa_68k", "mac_centris", "mac_server_68k", "powerbook_68k", "powerbook_duo_68k",
            "power_mac_classic", "mac_performa_ppc", "mac_server_ppc_classic", "powerbook_ppc_classic",
            "powerbook_duo_ppc", "power_mac", "imac_ppc", "emac", "mac_mini_ppc", "mac_server_ppc",
            "xserve_ppc", "powerbook_ppc", "ibook", "mac_pro_intel", "imac_intel", "imac_pro_intel",
            "mac_mini_intel", "xserve_intel", "macbook_pro_intel", "macbook_intel", "macbook_air_intel",
            "mac_pro_arm", "imac_arm", "imac_pro_arm", "mac_mini_arm", "macbook_pro_arm", "macbook_air_arm"};
    private static final int COLUMNS_COUNT = 30;

    /*
     * getSound
     * Available Parameters: 0 Macintosh 128k, mac128, no death sound
     *                       1 Macintosh II, macii, macii_death
     *                       2 Macintosh LC, maclc, maclc_death
     *                       3 Macintosh Quadra w/o AV, 68k PowerBook, quadra, maclc_death
     *                       4 Macintosh Quadra w/ AV, quadraav, quadraav_death
     *                       5 First gen Power Macintosh, powermac6100, powermac6100_death
     *                       6 NuBus Power Macintosh, powermac5000, powermac5000_death
     *                       7 PCI Power Macintosh, powermac, powermac_death
     *                       8 New World Macintosh, newmac, no death sound
     *                       9 TAM, tam, powermac_death
     *                       PB Old World PowerPC PowerBook, powermac(s), maclc_death(s)
     *                       T2 Big Sur startup sound, bigsur, no death sound
     *                       N no startup sound, no death sound
     *
     * getCategoryRange
     * Available Manufacturer(Group) Strings: all, apple68k, appleppc, appleintel, applearm
     * Available Manufacturer(Group) Resources: R.string.menu_group0, R.string.menu_group1,
     *                                          R.string.menu_group2, R.string.menu_group3,
     *                                          R.string.menu_group4
     *
     * getFilterString
     * Available Filter Strings: names, processors, years
     * Available Filter Resources: R.string.view1, R.string.view2, R.string.view3
     */

    private final SQLiteDatabase database;

    /* Machine ID starts from 0, ends total -1. */
    private final int[] categoryIndividualCount;

    /* starts from 0, actual total -1. */
    private int totalMachine = 0;

    // Stop flooding the Logcat!
    private boolean stopQuery = false;

    MachineHelper(final SQLiteDatabase thisDatabase, final Context thisContext) {
        database = thisDatabase;

        categoryIndividualCount = new int[CATEGORIES_LIST.length];
        for (int i = 0; i < CATEGORIES_LIST.length; i++) {
            Cursor tempCursor = database.query(CATEGORIES_LIST[i],
                    null, null, null, null, null,
                    null);
            // SelfCheck
            if (tempCursor.getColumnCount() != COLUMNS_COUNT) {
                ExceptionHelper.handleException(thisContext, null,
                        "MachineHelperInit", "Error found on category " + CATEGORIES_LIST[i]);
            }

            final int thisCursorCount = tempCursor.getCount();
            categoryIndividualCount[i] = thisCursorCount;
            totalMachine += thisCursorCount;
            Log.i("MachineHelperInit", "Category cursor " + CATEGORIES_LIST[i]
                    + " loaded with row count " + thisCursorCount
                    + ", accumulated total row count " + totalMachine);

            tempCursor.close();
        }
        Log.w("MachineHelper", "Initialized with " + totalMachine + " machines.");

    }

    public void setStopQuery() {
        stopQuery = true;
    }

    // Get the total count of categories
    public int getCategoryTotalCount() {
        return CATEGORIES_LIST.length;
    }

    // Get total machines. For usage of random access.
    public int getMachineCount() {
        return totalMachine;
    }



    // Get total machines in a category.
    public int getCategoryCount(final int thisCategory) {
        return categoryIndividualCount[thisCategory];
    }

    // Get category range for fixed navigation
    public int[] getCategoryRangeIDs(final int thisMachine, final Context thisContext) {
        return searchHelper("stype", getUndefined(thisMachine, "stype"), "all", thisContext, false);
    }


    // Get specific position of a machine ID.
    private int[] getPosition(final int thisMachine) {
        // Category ID / Remainder
        int[] position = {0, thisMachine};
        while (position[0] < CATEGORIES_LIST.length) {
            if (position[1] >= categoryIndividualCount[position[0]]) {
                position[1] -= categoryIndividualCount[position[0]];
                position[0]++;
            } else {
                break;
            }
        }
        return position;
    }

    // Convert Internal Database Category ID to MH Category ID
    private int convertToMHCategoryID(final String toConvert) {
        // Array out bound bug fix
        int toReturn = 0;
        for (String thisDBCategoryID : CATEGORIES_LIST) {
            if (toConvert.equals(thisDBCategoryID)) {
                break;
            }
            toReturn++;
        }
        return toReturn;
    }

    // Get machine ID by a specific position. Updated to adapt String type.
    public int findByPosition(final Pair<String, Integer> thisPosition) {
        int machineID = 0;
        for (int i = 0; i < convertToMHCategoryID(thisPosition.first); i++) {
            machineID += categoryIndividualCount[i];
        }
        return machineID + thisPosition.second;
    }

    public String getName(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "name"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("name"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getProcessor(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "processor"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("processor"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getMaxRam(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "ram"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("ram"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getYear(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "year"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("year"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getModel(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "model"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("model"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getType(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "rom"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("rom"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getMid(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "ident"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("ident"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getGraphics(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "graphics"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("graphics"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getExpansion(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "expansion"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("expansion"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getStorage(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "storage"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("storage"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getGestalt(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "gestalt"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("gestalt"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getOrder(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        // KEYWORD COLLISION! Temporary fix.
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                null, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("order"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getEMC(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "emc"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("emc"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getSoftware(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "software"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("software"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getDesign(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "design"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("design"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getSupport(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "support"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("support"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    public String getSYear(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "syear"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("syear"));
        tempCursor.close();
        return checkApplicability(tempResult);
    }

    // NullSafe
    private static String checkApplicability(final String thisSpec) {
        if (thisSpec == null) {
            return MainActivity.getRes().getString(R.string.not_applicable);
        } else {
            return thisSpec;
        }
    }

    private String getUndefined(final int thisMachine, final String thisColumn) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", thisColumn}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex(thisColumn));
        tempCursor.close();
        return tempResult;
    }

    // Integrated with SoundHelper
    public int[] getSound(final int thisMachine, final Context thisContext) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "sound"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String thisSound = tempCursor.getString(tempCursor.getColumnIndex("sound"));
        tempCursor.close();
        int[] sound = {0, 0};
        // NullSafe
        if (thisSound == null) {
            return sound;
        }
        Log.i("MachineHelperGetSound", "Get parameter " + thisSound);
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
            case "T2":
                sound[0] = R.raw.bigsur;
                break;
            default:
                ExceptionHelper.handleException(thisContext, null,
                        "MachineHelperGetSound", "Illegal parameter " + thisSound);
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
                Log.w("MachineHelperGetDthSnd", "No death sound for parameter " + thisSound);
        }
        return sound;
    }

    public File getPicture(final int thisMachine, final Context thisContext) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "pic"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        byte[] thisBlob = tempCursor.getBlob(tempCursor.getColumnIndex("pic"));
        tempCursor.close();
        if (thisBlob == null) {
            return getPicture(thisMachine - 1, thisContext);
        } else {
            String path = "/";
            try {
                Bitmap pic = BitmapFactory.decodeByteArray(thisBlob, 0, thisBlob.length);
                File file = File.createTempFile("tempF", ".tmp");
                FileOutputStream out = new FileOutputStream(file, false);
                pic.compress(Bitmap.CompressFormat.PNG, 100, out);
                path = file.getPath();
            } catch (Exception e) {
                ExceptionHelper.handleException(thisContext, e, null, null);
            }
            return new File(path);
        }
    }

    // Should return "N" if EveryMac link is not available.
    public String getConfig(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "links"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String tempResult = tempCursor.getString(tempCursor.getColumnIndex("links"));
        tempCursor.close();
        // NullSafe
        if (tempResult == null) {
            return "null";
        } else {
            return tempResult;
        }
    }

    // Refer to SpecsActivity for a documentation.
    public int getProcessorTypeImage(final int thisMachine, final Context thisContext) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "sprocessor"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String thisProcessorImage = tempCursor.getString(tempCursor.getColumnIndex("sprocessor"));
        tempCursor.close();
        Log.i("MHGetProcessorImageType", "Get ID " + thisProcessorImage);
        // NullSafe
        if (thisProcessorImage == null) {
            return 0;
        }
        String[] thisImages = thisProcessorImage.split("~");
        switch (thisImages[0]) {
            case "68000":
            case "68020":
            case "68030":
            case "68040":
                return R.drawable.motorola;
            case "601":
            case "603":
            case "604":
            case "g3":
            case "g4":
            case "g5":
                return R.drawable.powerpc;
            case "netburst":
            case "p6":
            case "core":
            case "penryn":
            case "nehalem":
            case "westmere":
            case "snb":
            case "ivb":
            case "haswell":
            case "broadwell":
            case "skylake":
            case "kabylake":
            case "coffeelake":
            case "amberlake":
            case "cascadelake":
            case "cometlake":
            case "icelake":
            case "tigerlake":
                return R.drawable.intel;
            case "A12Z":
            case "m1":
                return R.drawable.arm;
            default:
                ExceptionHelper.handleException(thisContext, null,
                        "MHGetProcessorImageType", "Illegal parameter " + thisProcessorImage);
        }
        return 0;
    }

    public int[][] getProcessorImage(final int thisMachine, final Context thisContext) {
        int[] position = getPosition(thisMachine);
        Cursor tempCursor = database.query(CATEGORIES_LIST[position[0]],
                new String[]{"id", "processorid"}, "id = " + position[1], null, null, null,
                null);
        tempCursor.moveToFirst();
        String thisProcessorImage = tempCursor.getString(tempCursor.getColumnIndex("processorid"));
        tempCursor.close();
        Log.i("MHGetProcessorImage", "Get ID " + thisProcessorImage);
        // NullSafe
        if (thisProcessorImage == null) {
            return new int[][] {{0}};
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
                case "p4ht":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.intelp4ht;
                    break;
                case "coresolo":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.coresolo;
                    break;
                case "coreduo":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.intelcoreduo;
                    break;
                case "core2duo":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.core2duo;
                    break;
                case "core2quad":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.core2quad;
                    break;
                case "core2ex":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.core2ex;
                    break;
                case "corei5":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei5;
                    break;
                case "corei7":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei7;
                    break;
                case "corei3_1":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei3_1;
                    break;
                case "corei5_1":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei5_1;
                    break;
                case "corei7_1":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei7_1;
                    break;
                case "corei3_2":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei3_2;
                    break;
                case "corei5_2":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei5_2;
                    break;
                case "corei7_2":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei7_2;
                    break;
                case "corei3_4":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei3_4;
                    break;
                case "corei5_4":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei5_4;
                    break;
                case "corei7_4":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei7_4;
                    break;
                case "corei3_5":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei3_5;
                    break;
                case "corei5_5":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei5_5;
                    break;
                case "corei7_5":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei7_5;
                    break;
                case "corei3_6":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei3_6;
                    break;
                case "corei5_6":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei5_6;
                    break;
                case "corei7_6":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei7_6;
                    break;
                case "corei3_7":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei3_7;
                    break;
                case "corei5_7":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei5_7;
                    break;
                case "corei7_7":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei7_7;
                    break;
                case "corei3_8":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei3_8;
                    break;
                case "corei5_8":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei5_8;
                    break;
                case "corei7_8":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei7_8;
                    break;
                case "corei9_8":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei9_8;
                    break;
                case "corei3_9":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei3_9;
                    break;
                case "corei5_9":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei5_9;
                    break;
                case "corei7_9":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei7_9;
                    break;
                case "corei9_9":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei9_9;
                    break;
                case "corei3_10":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei3_10;
                    break;
                case "corei5_10":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei5_10;
                    break;
                case "corei7_10":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei7_10;
                    break;
                case "corei9_10":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei9_10;
                    break;
                case "corei3_11":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei3_11;
                    break;
                case "corei5_11":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei5_11;
                    break;
                case "corei7_11":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei7_11;
                    break;
                case "corei9_11":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corei9_11;
                    break;
                case "corem":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corem;
                    break;
                case "corem3_6":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corem3_6;
                    break;
                case "corem5_6":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corem5_6;
                    break;
                case "corem7_6":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corem7_6;
                    break;
                case "corem3_7":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.corem3_7;
                    break;
                case "xeon_a":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeon_a;
                    break;
                case "xeon_b":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeon_b;
                    break;
                case "xeon_1":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeon_1;
                    break;
                case "xeon_2":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeon_2;
                    break;
                case "xeon_4":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeon_4;
                    break;
                case "xeon_5":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeon_5;
                    break;
                case "xeon_6":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeon_6;
                    break;
                case "xeon_11":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeon_11;
                    break;
                case "xeonbronze_6":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeonbronze_6;
                    break;
                case "xeonsilver_6":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeonsilver_6;
                    break;
                case "xeongold_6":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeongold_6;
                    break;
                case "xeonplatinum_6":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeonplatinum_6;
                    break;
                case "xeonbronze_11":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeonbronze_11;
                    break;
                case "xeonsilver_11":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeonsilver_11;
                    break;
                case "xeongold_11":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeongold_11;
                    break;
                case "xeonplatinum_11":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.xeonplatinum_11;
                    break;
                case "t1":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.applet1;
                    break;
                case "t2":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.applet2;
                    break;
                case "A12Z":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.applea12z;
                    break;
                case "m1":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.applem1;
                    break;
                default:
                    ExceptionHelper.handleException(thisContext, null,
                            "MHGetProcessorImage", "Illegal parameter " + thisProcessorImage);
                    toReturn[i] = new int[1];
                    toReturn[i][0] = 0;
                    break;
            }
        }
        return toReturn;
    }

    // Get category range by manufacturer. Should be updated accordingly.
    private String[] getCategoryRange(final String thisManufacturer) {
        Log.i("MHRange", "Get parameter " + thisManufacturer);
        final String[] apple68k = {"compact_mac", "mac_ii", "mac_lc", "mac_quadra",
                "mac_performa_68k", "mac_centris", "mac_server_68k", "powerbook_68k", "powerbook_duo_68k"};
        final String[] appleppc = {"power_mac_classic", "mac_performa_ppc", "mac_server_ppc_classic",
                "powerbook_ppc_classic", "powerbook_duo_ppc", "power_mac", "imac_ppc", "emac",
                "mac_mini_ppc", "mac_server_ppc", "xserve_ppc", "powerbook_ppc", "ibook"};
        final String[] appleintel = {"mac_pro_intel", "imac_intel", "imac_pro_intel",
                "mac_mini_intel", "xserve_intel", "macbook_pro_intel", "macbook_intel", "macbook_air_intel"};
        final String[] applearm = {"mac_pro_arm", "imac_arm", "imac_pro_arm", "mac_mini_arm",
                "macbook_pro_arm", "macbook_air_arm"};
        switch (thisManufacturer) {
            case "all":
                return CATEGORIES_LIST;
            case "apple68k":
                return apple68k;
            case "appleppc":
                return appleppc;
            case "appleintel":
                return appleintel;
            case "applearm":
                return applearm;
            default:
                Log.e("MHRange", "Invalid parameter");
                return CATEGORIES_LIST;
        }
    }

    // Get filter string[type(Search column/Search keywords/Display string), ID]. Should be updated accordingly.
    public String[][] getFilterString(final String thisFilter) {
        final String[][] names = {{"stype"},
                {"compact_mac", "mac_ii", "mac_lc", "mac_quadra", "mac_performa", "mac_centris",
                 "mac_server", "power_mac", "imac_normal", "emac", "xserve", "mac_mini", "nmac_pro", "imac_pro",
                 "powerbook_normal", "powerbook_duo", "ibook", "macbook_pro", "macbook_normal", "macbook_air"},
                {"Compact Macintosh", "Macintosh II", "Macintosh LC", "Macintosh Quadra",
                 "Macintosh Performa", "Macintosh Centris", "Macintosh Server", "Power Macintosh",
                 "iMac", "eMac", "Xserve", "Mac mini", "Mac Pro", "iMac Pro", "Macintosh PowerBook",
                 "Macintosh PowerBook Duo", "iBook", "MacBook Pro", "MacBook", "MacBook Air"}};
        final String[][] processors = {{"sprocessor"},
                {"68000", "68020", "68030", "68040", "601", "603", "604", "g3", "g4", "g5",
                 "netburst", "p6", "core", "penryn", "nehalem", "westmere", "snb", "ivb", "haswell",
                 "broadwell", "skylake", "kabylake", "coffeelake", "amberlake", "cascadelake", "cometlake", "icelake",
                 "tigerlake", "a12", "m1"},
                {"Motorola 68000", "Motorola 68020", "Motorola 68030", "Motorola 68040",
                 "PowerPC 601", "PowerPC 603", "PowerPC 604", "PowerPC G3", "PowerPC G4",
                 "PowerPC G5", "Intel NetBurst", "Intel P6 (Yonah)", "Intel Core", "Intel Penryn",
                 "Intel Nehalem", "Intel Westmere", "Intel Sandy Bridge (2nd Gen)", "Intel Ivy Bridge (3rd Gen)",
                 "Intel Haswell (4th Gen)", "Intel Broadwell (5th Gen)", "Intel Skylake (6th Gen)", "Intel Kaby Lake (7th Gen)",
                 "Intel Coffee Lake (8th/9th Gen)", "Intel Amber Lake (8th Gen)", "Intel Cascade Lake", "Intel Comet Lake (10th Gen)",
                 "Intel Ice Lake (10th Gen)", "Intel Tiger Lake (11th Gen)", "Apple A12", "Apple M1"}};
        final String[][] years = {{"syear"},
                {"1984", "1985", "1986", "1987", "1988", "1989", "1990", "1991", "1992", "1993",
                 "1994", "1995", "1996", "1997", "1998", "1999", "2000", "2001", "2002", "2003",
                 "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013",
                 "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021"},
                {"1984", "1985", "1986", "1987", "1988", "1989", "1990", "1991", "1992", "1993",
                 "1994", "1995", "1996", "1997", "1998", "1999", "2000", "2001", "2002", "2003",
                 "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013",
                 "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021"}};
        Log.i("MHGetFilter", "Get parameters " + thisFilter);
        switch (thisFilter) {
            case "names":
                return names;
            case "processors":
                return processors;
            case "years":
                return years;
            default:
                break;
        }
        Log.e("MHGetFilter", "Invalid parameters");
        return names;
    }

    // For search use. Return machine IDs. Adapted with category range.
    public int[] searchHelper(final String columnName, final String searchInput, final String thisManufacturer, final Context thisContext, final boolean isExactMatch) {
        try {
            Log.i("MHSearchHelper", "Get parameter: column " + columnName + ", input " + searchInput);
            // Raw results (categoryID/remainders)
            final String[] thisCategoryRange = getCategoryRange(thisManufacturer);
            final int thisCategoryCount = thisCategoryRange.length;
            int[][] rawResults = new int[thisCategoryCount][];

            // Setup temp cursor of each category for a query.
            for (int i = 0; i < thisCategoryCount; i++) {
                // Terminate immediately.
                if (stopQuery) {
                    throw new IllegalAccessException();
                }
                Cursor thisSearchIndividualCursor = database.query(thisCategoryRange[i], new String[]{"id", columnName}, columnName + " LIKE ? ",
                        new String[]{"%" + searchInput + "%"}, null, null, null);;
                rawResults[i] = new int[thisSearchIndividualCursor.getCount()];
                Log.i("MHSearchHelper", "Category " + thisCategoryRange[i] + " got "
                        + thisSearchIndividualCursor.getCount() + " result(s).");
                // Write raw query results.
                int previousCount = 0;
                while (thisSearchIndividualCursor.moveToNext()) {
                    // Terminate immediately.
                    if (stopQuery) {
                        throw new IllegalAccessException();
                    }
                    rawResults[i][previousCount] = thisSearchIndividualCursor.getInt(thisSearchIndividualCursor.getColumnIndex("id"));
                    previousCount++;
                }
                thisSearchIndividualCursor.close();
            }

            // Convert raw results to positions.
            int resultTotalCount = 0;
            for (int[] thisRawResult : rawResults) {
                if (thisRawResult != null) {
                    resultTotalCount += thisRawResult.length;
                }
            }
            int[] finalPositions = new int[resultTotalCount];
            int previousCount = 0;
            for (int j = 0; j < thisCategoryCount; j++) {
                for (int k = 0; k < rawResults[j].length; k++) {
                    finalPositions[previousCount] = findByPosition(new Pair<>(thisCategoryRange[j], rawResults[j][k]));
                    previousCount++;
                }
            }
            Log.i("MHSearchHelper", "Raw Matched: " + finalPositions.length + " result(s).");

            // Verify Exact Match if required.
            if (isExactMatch) {
                List<Integer> verifiedPositions = new Vector<>(0);
                for (int machineToVerify : finalPositions) {
                    String[] rawUndefinedQuery = getUndefined(machineToVerify, columnName).split("~");
                    for (String resultToVerify : rawUndefinedQuery) {
                        if (resultToVerify.equalsIgnoreCase(searchInput)) {
                            verifiedPositions.add(machineToVerify);
                            break;
                        }
                    }
                }
                resultTotalCount = verifiedPositions.size();

                // Not in Java 8: go over the vector.
                finalPositions = new int[resultTotalCount];
                for (int i = 0; i < resultTotalCount; i++) {
                    finalPositions[i] = verifiedPositions.get(i);
                }
            }
            Log.i("MHSearchHelper", "Exact Match is " + isExactMatch + ".");
            Log.i("MHSearchHelper", "Exact Matched: " + finalPositions.length + " result(s).");

            // Sort if required.
            if (PrefsHelper.getBooleanPrefsSafe("isSortAgain", thisContext) && resultTotalCount > 1) {
                // Insertion sort for best runtime
                for (int i = 0; i < resultTotalCount; i++) {
                    for (int j = i; j > 0; j--) {
                        // Terminate immediately.
                        if (stopQuery) {
                            throw new IllegalAccessException();
                        }
                        if (getYearForSorting(columnName, searchInput, finalPositions[j])
                                < getYearForSorting(columnName, searchInput, finalPositions[j - 1])) {
                            int shiftTemp = finalPositions[j];
                            finalPositions[j] = finalPositions[j - 1];
                            finalPositions[j - 1] = shiftTemp;
                        }
                    }
                }
            }
            Log.i("MHSearchHelper", "Sorting is " + PrefsHelper.getBooleanPrefsSafe("isSortAgain", thisContext) + ".");
            Log.i("MHSearchHelper", "Returning " + finalPositions.length + " result(s).");
            return finalPositions;
        } catch (Exception e) {
            Log.e("MHSearchHelper", "Exception Occurred, returning empty array");
            setStopQuery();
            e.printStackTrace();
            return new int[0];
        }
    }

    // Get year parameter for sorting. Y = Y, M = M/10. Returns double float number.
    private double getYearForSorting(final String columnName, final String searchInput, final int thisMachine) {
        try {
            String[] rawYear = getSYear(thisMachine).split(", ");
            // Terminate immediately.
            if (stopQuery) {
                throw new IllegalAccessException();
            }
            int targetIndex = 0;
            if (columnName.equals("syear") && rawYear.length > 1) {
                for (int i = 0; i < rawYear.length; i++) {
                    String[] rawYearSplited = rawYear[i].split("\\.");
                    if (rawYearSplited.length != 2) {
                        Log.e("getYearForSorting", "Error, columnName " + columnName
                                + ", searchInput " + searchInput + ", Machine Name " + getName(thisMachine) + ", Raw Year " + getSYear(thisMachine));
                        throw new IllegalArgumentException();
                    }
                    if (rawYearSplited[0].equals(searchInput)) {
                        targetIndex = i;
                        break;
                    }
                }
            }
            String[] targetYearSplited = rawYear[targetIndex].split("\\.");
            if (targetYearSplited.length != 2) {
                Log.e("getYearForSorting", "Error, columnName " + columnName
                        + ", searchInput " + searchInput + ", Machine Name " + getName(thisMachine) + ", Raw Year " + getSYear(thisMachine));
                throw new IllegalArgumentException();
            }
            double targetYearSplitedA = Integer.parseInt(targetYearSplited[0]);
            double targetYearSplitedB = Integer.parseInt(targetYearSplited[1]);
            targetYearSplitedB = targetYearSplitedB / 10;
            return targetYearSplitedA + targetYearSplitedB;
        } catch (Exception e) {
            setStopQuery();
            e.printStackTrace();
            return 0.0;
        }
    }

    // For filter-based fixed search use. Return (filterIDs/machineIDs).
    public int[][] filterSearchHelper(final String[][] filterString, final String thisManufacturer, final Context thisContext) {
        try {
            int[][] finalPositions = new int[filterString[1].length][];
            for (int i = 0; i < filterString[1].length; i++) {
                // Terminate immediately.
                if (stopQuery) {
                    throw new IllegalAccessException();
                }
                finalPositions[i] = searchHelper(filterString[0][0], filterString[1][i], thisManufacturer, thisContext, false);
            }
            return finalPositions;
        } catch (Exception e) {
            Log.e("MHFilterSearchHelper", "Exception Occurred, returning empty array");
            setStopQuery();
            e.printStackTrace();
            return new int[0][0];
        }
    }

    // Sorting used by ver. 4.9
    public int[] directSortByYear(final int[] input) {
        try {
            Log.i("MHDirectSort", "Starting Direct Sorting.");
            for (int i = 0; i < input.length; i++) {
                for (int j = i; j > 0; j--) {
                    // Terminate immediately.
                    if (stopQuery) {
                        throw new IllegalAccessException();
                    }
                    if (getYearForSorting("", "", input[j])
                            < getYearForSorting("", "", input[j - 1])) {
                        int shiftTemp = input[j];
                        input[j] = input[j - 1];
                        input[j - 1] = shiftTemp;
                    }
                }
            }
            return input;
        } catch (Exception e) {
            e.printStackTrace();
            setStopQuery();
            return input;
        }
    }
}
