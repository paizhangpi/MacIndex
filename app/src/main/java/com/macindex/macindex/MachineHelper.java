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
class MachineHelper {

    /*
     * Updating categories (Ver. 4.0)
     * (1) Update the following number.
     * (2) Update the MH manufacturer and filter method. Update String resources.
     * (3) Update the MainActivity and SearchActivity.
     * (4) Make change to the database / make other changes.
     * (5) Update the following information.
     *
     * Updating filters (Ver. 4.0)
     * (1) Update the MH filter method. Update String resources.
     * (2) Update the MainActivity and SearchActivity.
     * (3) Update the following information.
     *
     * Updating columns
     * (1) Update MH to adapt the new column.
     * (2) Update SpecActivity code and string to get the data.
     * (3) Add a new column to every table.
     */

    private static final int[] CATEGORIES_LIST = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 90, 91, 92, 93, 94,
            95, 96, 97, 98, 99, 990, 991, 992, 993, 994, 995, 996, 997, 998, 999};
    //private static final int[] CATEGORIES_LIST = {0, 1, 2, 4, 5, 3, 6, 95, 7, 8, 9, 90, 993, 994,
    //        995, 999, 91, 92, 96, 97, 93, 94, 996, 997, 998, 98, 99, 990, 991, 992};
    /*
     * Category List
     * Category 0: Compact Mac
     * Category 1: Mac II
     * Category 2: Mac LC
     * Category 3: Mac Centris
     * Category 4: Mac Quadra
     * Category 5: Mac Performa 68K
     * Category 6: Power Mac
     * Category 7: Power Mac G3/G4/G5
     * Category 8: iMac PPC
     * Category 9: eMac
     * Category 90: Mac mini PPC
     * Category 91: PowerBook 68K
     * Category 92: PowerBook Duo 68K
     * Category 93: PowerBook G3/G4/G5
     * Category 94: iBook
     * Category 95: Mac Performa PPC
     * Category 96: PowerBook PPC
     * Category 97: PowerBook Duo PPC
     * Category 98: Macintosh Server 68K
     * Category 99: Macintosh Server PPC
     * Category 990: Macintosh Server G3/G4/G5
     * Category 991: Xserve G4/G5
     * Category 992: Xserve Intel
     * Category 993: Mac Pro
     * Category 994: iMac Intel
     * Category 995: Mac mini Intel
     * Category 996: MacBook Pro Intel
     * Category 997: MacBook Intel
     * Category 998: MacBook Air Intel
     * Category 999: Mac mini ARM
     *
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
     *                       N no startup sound, no death sound
     *
     * getProcessorTypeImage
     * Available Parameters: 68k, ppc
     *                       G3 740, 750, 750cx, 750cxe, 755, 750fx (ppc)
     *                       G4 7400, 7410, 7440, 7445, 7450, 7455, 7447 (ppc)
     *                       G5 970, 970fx, 970mp (ppc)
     *                       Apple Silicon A12Z (arm)
     *                       Intel Pentium p4ht (intel)
     *
     * getProcessorImage
     * Available Parameters: G3 740, 750, 750cx, 750cxe, 755, 750fx
     *                       G4 7400, 7410, 7440, 7445, 7450, 7455, 7447
     *                       G5 970, 970fx, 970mp
     *                       Apple Silicon A12Z
     *                       Intel Pentium p4ht
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
     *
     *
     * Methods above should be manually updated.
     *
     * ------------ Don't change class variables below this line ------------
     */

    private SQLiteDatabase database;

    private Cursor[] categoryIndividualCursor;

    /* Machine ID starts from 0, ends total -1. */
    private int[] categoryIndividualCount;

    /* starts from 0, actual total -1. */
    private int totalMachine = 0;

    private int totalConfig = 0;

    MachineHelper(final SQLiteDatabase thisDatabase) {
        database = thisDatabase;

        categoryIndividualCount = new int[CATEGORIES_LIST.length];
        categoryIndividualCursor = new Cursor[CATEGORIES_LIST.length];
        for (int i = 0; i < CATEGORIES_LIST.length; i++) {
            categoryIndividualCursor[i] = database.query("category" + CATEGORIES_LIST[i],
                    null, null, null, null, null,
                    null);
            final int thisCursorCount = categoryIndividualCursor[i].getCount();
            categoryIndividualCount[i] = thisCursorCount;
            totalMachine += thisCursorCount;
            Log.i("MachineHelperInit", "Category cursor " + CATEGORIES_LIST[i]
                    + " loaded with row count " + thisCursorCount
                    + ", accumulated total row count " + totalMachine);
        }

        // Initialize configurations
        for (int i = 0; i < totalMachine; i++) {
            totalConfig += getThisConfigCount(i);
        }
        Log.w("MachineHelper", "Initialized with " + totalMachine + " machines.");
        Log.w("MachineHelper", "Initialized with " + totalConfig + " configurations.");
    }
    /* SelfCheck was removed since Ver 4.0 */

    void suicide() {
        for (int i = 0; i < CATEGORIES_LIST.length; i++) {
            if (categoryIndividualCursor[i] != null) {
                categoryIndividualCursor[i].close();
                Log.i("MachineHelperSuicide", "Category cursor " + CATEGORIES_LIST[i]
                        + " closed successfully.");
            }
        }
    }

    // Get the total count of categories
    int getCategoryTotalCount() {
        return CATEGORIES_LIST.length;
    }

    // Get total machines. For usage of random access.
    int getMachineCount() {
        return totalMachine;
    }

    // Get total configurations. For usage of random access.
    int getConfigCount() {
        return totalConfig;
    }
    /* Category name and description was removed since Ver. 4.0 */

    // Get total machines in a category.
    int getCategoryCount(final int thisCategory) {
        return categoryIndividualCount[thisCategory];
    }
    /* Category start and end was removed since Ver. 4.0 */

    // Get specific position of a machine ID.
    int[] getPosition(final int thisMachine) {
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

    private int getThisConfigCount(final int thisMachine) {
        final String[] configGroup = getConfig(thisMachine).split(";");
        return configGroup.length;
    }

    // Convert Internal Database Category ID to MH Category ID
    private int convertToMHCategoryID(final int toConvert) {
        int toReturn = 0;
        toReturn += (String.valueOf(toConvert).length() - 1) * 10;
        toReturn += toConvert % 10;
        return toReturn;
    }

    // Convert MH Category ID to Internal Database Category ID
    private int convertToDatabaseCategoryID(final int toConvert) {
        String toReturn = "";
        for (int i = 0; i < toConvert / 10; i++) {
            toReturn = toReturn.concat("9");
        }
        toReturn = toReturn.concat(String.valueOf(toConvert % 10));
        return Integer.parseInt(toReturn);
    }

    // Get machine ID by a specific position.
    int findByPosition(final int[] thisPosition) {
        int machineID = 0;
        for (int i = 0; i < convertToMHCategoryID(thisPosition[0]); i++) {
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
        Log.e("MachineHelperFndByCfg", "Can't find such ID, returning empty position.");
        return new int[] {};
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
            case "740":
            case "750":
            case "750cx":
            case "750cxe":
            case "755":
            case "750fx":
            case "7400":
            case "7410":
            case "7440":
            case "7445":
            case "7450":
            case "7455":
            case "7447":
            case "970":
            case "970fx":
            case "970mp":
                return R.drawable.powerpc;
            case "p4ht":
                return R.drawable.intel;
            case "A12Z":
                return R.drawable.arm;
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
                case "p4ht":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.intelp4ht;
                    break;
                case "A12Z":
                    toReturn[i] = new int[1];
                    toReturn[i][0] = R.drawable.applea12z;
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

    String getMid(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return checkApplicability(categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("mid")));
    }

    String getBus(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return checkApplicability(categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("bus")));
    }

    String getGraphics(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return checkApplicability(categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("graphics")));
    }

    String getDisplay(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return checkApplicability(categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("display")));
    }

    String getExpansion(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return checkApplicability(categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("expansion")));
    }

    String getStorage(final int thisMachine) {
        int[] position = getPosition(thisMachine);
        categoryIndividualCursor[position[0]].moveToFirst();
        categoryIndividualCursor[position[0]].move(position[1]);
        return checkApplicability(categoryIndividualCursor[position[0]]
                .getString(categoryIndividualCursor[position[0]].getColumnIndex("storage")));
    }

    // NullSafe
    private static String checkApplicability(final String thisSpec) {
        if (thisSpec == null || thisSpec.equals("N")) {
            return MainActivity.getRes().getString(R.string.not_applicable);
        } else {
            return thisSpec;
        }
    }

    // Get category range by manufacturer. Should be updated accordingly.
    private int[] getCategoryRange(final String thisManufacturer) {
        Log.i("MHRange", "Get parameter " + thisManufacturer);
        final int[] apple68k = {0, 1, 2, 3, 4, 5, 91, 92, 98};
        final int[] appleppc = {6, 7, 8, 9, 90, 93, 94, 95, 96, 97, 99, 990, 991};
        final int[] appleintel = {992, 993, 994, 995, 996, 997, 998};
        final int[] applearm = {999};
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
                Log.w("MHRange", "Invalid parameter");
                return CATEGORIES_LIST;
        }
    }

    // Get filter string[type(Search column/Search keywords/Display string), ID]. Should be updated accordingly.
    String[][] getFilterString(final String thisFilter) {
        final String[][] names = {{"sindex"},
                {/* 68K Desktop */
                "*Macintosh", "Macintosh II", "Macintosh LC", "Macintosh Quadra",
                "<Macintosh Performa", "Macintosh Centris",
                 /* PPC Desktop */
                "^", ">Macintosh Performa", "&Power Mac",
                ">iMac", "eMac", ">Mac mini",
                 /* x86 Desktop */
                "@", "[iMac", "[Mac mini",
                 /* ARM Desktop */
                "#",
                 /* 68K Laptop */
                "<Macintosh Po", "{Macintosh PowerBook Duo",
                 /* PPC Laptop */
                ">Macintosh Po", "}Macintosh PowerBook Duo",
                "PowerBook G", "iBook",
                 /* x86 Laptop */
                "MacBook Pro", "[MacBook", "MacBook Air",
                 /* ARM Laptop */
                 /* 68K Server */
                "<Workgroup Server",
                 /* PPC Server */
                "`", "Macintosh Server G", ">Xserve",
                 /* x86 Server */
                "[Xserve"
                 /* ARM Server */},
                {/* 68K Desktop */
                "Compact Macintosh", "Macintosh II", "Macintosh LC", "Macintosh Quadra",
                "Macintosh Performa (68K)", "Macintosh Centris",
                 /* PPC Desktop */
                "Power Macintosh", "Macintosh Performa (PPC)", "Power Mac G3/G4/G5",
                "iMac (PPC)", "eMac", "Mac mini (PPC)",
                 /* x86 Desktop */
                "Mac Pro", "iMac (x86)", "Mac mini (x86)",
                 /* ARM Desktop */
                "Mac mini (ARM)",
                 /* 68K Laptop */
                "Macintosh PowerBook (68K)", "Macintosh PowerBook Duo (68K)",
                 /* PPC Laptop */
                "Macintosh PowerBook (PPC)", "Macintosh PowerBook Duo (PPC)",
                "PowerBook G3/G4", "iBook",
                 /* x86 Laptop */
                "MacBook Pro", "MacBook", "MacBook Air",
                 /* ARM Laptop */
                 /* 68K Server */
                "Macintosh Server (68K)",
                 /* PPC Server */
                 "Macintosh Server (PPC)", "Macintosh Server G3/G4/G5", "Xserve G4/G5",
                 /* x86 Server */
                 "Xserve (x86)"
                 /* ARM Server */}};
        final String[][] processors = {{"processor"},
                {"68000", "68020", "68030", "040", "601", "603", "604", "G3", "G4", "G5",
                "Pentium", "(Original)", "Duo", "i3", "i5", "i7", "Xeon", "Apple A"},
                {"Motorola 68000", "Motorola 68020", "Motorola 68030", "Motorola 68040",
                "PowerPC 601", "PowerPC 603", "PowerPC 604", "PowerPC G3", "PowerPC G4",
                "PowerPC G5", "Intel Pentium", "Intel Core", "Intel Core 2", "Intel Core i3",
                "Intel Core i5", "Intel Core i7", "Intel Xeon", "Apple Silicon"}};
        final String[][] years = {{"year"},
                {"1984", "1985", "1986", "1987", "1988", "1989", "1990", "1991", "1992", "1993",
                "1994", "1995", "1996", "1997", "1998", "1999", "2000", "2001", "2002", "2003",
                "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013",
                "2014", "2015", "2016", "2017", "2018", "2019", "2020"},
                {"1984", "1985", "1986", "1987", "1988", "1989", "1990", "1991", "1992", "1993",
                "1994", "1995", "1996", "1997", "1998", "1999", "2000", "2001", "2002", "2003",
                "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013",
                "2014", "2015", "2016", "2017", "2018", "2019", "2020"}};
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
        Log.w("MHGetFilter", "Invalid parameters");
        return names;
    }

    // For search use. Return machine IDs. Adapted with category range.
    int[] searchHelper(final String columnName, final String searchInput, final String thisManufacturer) {
        Log.i("MHSearchHelper", "Get parameter: column " + columnName + ", input " + searchInput);
        // Raw results (categoryID/remainders)
        final int[] thisCategoryRange = getCategoryRange(thisManufacturer);
        final int thisCategoryCount = thisCategoryRange.length;
        int[][] rawResults = new int[thisCategoryCount][];

        // Setup temp cursor of each category for a query.
        try {
            for (int i = 0; i < thisCategoryCount; i++) {
                Cursor thisSearchIndividualCursor = database.query("category" + thisCategoryRange[i],
                        null, columnName + " LIKE ? ",
                        new String[]{"%" + searchInput + "%"},
                        null, null, null);
                rawResults[i] = new int[thisSearchIndividualCursor.getCount()];
                Log.i("MHSearchHelper", "Category " + thisCategoryRange[i] + " got "
                        + thisSearchIndividualCursor.getCount() + " result(s).");
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
            }
            Log.i("MHSearchHelper", "Get " + resultTotalCount + " result(s).");
        }
        int[] finalPositions = new int[resultTotalCount];
        int previousCount = 0;
        for (int j = 0; j < thisCategoryCount; j++) {
            for (int k = 0; k < rawResults[j].length; k++) {
                finalPositions[previousCount] = findByPosition(new int[] {
                        getCategoryRange(thisManufacturer)[j], rawResults[j][k]});
                previousCount++;
            }
        }
        return finalPositions;
    }

    // For filter-based fixed search use. Return (filterIDs/machineIDs).
    int[][] filterSearchHelper(final String thisFilter, final String thisManufacturer) {
        final String[][] filterString = getFilterString(thisFilter);
        int[][] finalPositions = new int[filterString[1].length][];
        for (int i = 0; i < filterString[1].length; i++) {
            finalPositions[i] = searchHelper(filterString[0][0], filterString[1][i], thisManufacturer);
        }
        return finalPositions;
    }
}
