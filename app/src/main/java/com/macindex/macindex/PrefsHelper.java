package com.macindex.macindex;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * MacIndex Preference Helper.
 * July 11, 2020
 */
class PrefsHelper {

    public static final String PREFERENCE_FILENAME = "MacIndex_Preference";

    /**
     * Preference Values Reference List.
     * User Settings
     * (Boolean) isOpenEveryMac: false
     * (Boolean) isPlayDeathSound: true
     * (Boolean) isUseGestures: true
     * (Boolean) isUseNavButtons: true
     * (Boolean) isQuickNav: false
     * (Boolean) isRandomAll: false
     * (Boolean) isSaveMainUsage: true
     * (Boolean) isSaveSearchUsage: false
     *
     * User Usage
     * (MainActivity - Int) isFirstLunch
     * (MainActivity - Int) MainTitle: current application title resource: R.string.menu_group0
     * (MainActivity - String) thisManufacturer: current selected category filter MH string: "all"
     * (MainActivity - String) thisFilter: current selected view method MH string: "names"
     * (MainActivity - Int) ManufacturerMenu: current selected category filter menu item resource in menu: R.id.group0MenuItem
     * (MainActivity - Int) FilterMenu: current selected view method menu item resource in menu: R.id.view1MenuItem
     *
     * (SearchActivity - String) searchLastInput: last search string: ""
     * (SearchActivity - String) searchManufacturer: current selected category filter MH string: "all"
     * (SearchActivity - String) searchOption: current selected search filter MH string: "sindex"
     * (SearchActivity - Int) searchManufacturerSelection: current selected category filter radio button resource: R.id.allGroup
     * (SearchActivity - Int) searchOptionSelection: current selected search filter radio button resource: R.id.nameOption
     * (SearchActivity - Int) currentManufacturerResource: current selected category filter string resource: R.string.menu_group0
     * (SearchActivity - Int) currentOptionResource: R.string.search_nameOption
     */
    private static final Map<String, Object> DEFAULT_VALUES;
    static {
        DEFAULT_VALUES = new HashMap<>();
        DEFAULT_VALUES.put("isOpenEveryMac", Boolean.FALSE);
        DEFAULT_VALUES.put("isPlayDeathSound", Boolean.TRUE);
        DEFAULT_VALUES.put("isUseGestures", Boolean.TRUE);
        DEFAULT_VALUES.put("isUseNavButtons", Boolean.TRUE);
        DEFAULT_VALUES.put("isQuickNav", Boolean.FALSE);
        DEFAULT_VALUES.put("isRandomAll", Boolean.FALSE);
        DEFAULT_VALUES.put("isSaveMainUsage", Boolean.TRUE);
        DEFAULT_VALUES.put("isSaveSearchUsage", Boolean.TRUE);

        DEFAULT_VALUES.put("isFirstLunch", Boolean.TRUE);
        DEFAULT_VALUES.put("MainTitle", R.string.menu_group0);
        DEFAULT_VALUES.put("thisManufacturer", "all");
        DEFAULT_VALUES.put("thisFilter", "names");
        DEFAULT_VALUES.put("ManufacturerMenu", R.id.group0MenuItem);
        DEFAULT_VALUES.put("FilterMenu", R.id.view1MenuItem);

        DEFAULT_VALUES.put("searchLastInput", "");
        DEFAULT_VALUES.put("searchManufacturer", "all");
        DEFAULT_VALUES.put("searchOption", "sindex");
        DEFAULT_VALUES.put("searchManufacturerSelection", R.id.id0Group);
        DEFAULT_VALUES.put("searchOptionSelection", R.id.nameOption);
        DEFAULT_VALUES.put("currentManufacturerResource", R.string.menu_group0);
        DEFAULT_VALUES.put("currentOptionResource", R.string.search_nameOption);
    }

    private SharedPreferences prefsFile = null;

    PrefsHelper(final SharedPreferences thisPrefsFile) {
        prefsFile = thisPrefsFile;
    }

    int getIntPrefs(final String thisPrefsName) {
        try {
            if (DEFAULT_VALUES.containsKey(thisPrefsName) || !(DEFAULT_VALUES.get(thisPrefsName) instanceof Integer)) {
                int value = prefsFile.getInt(thisPrefsName, (Integer) DEFAULT_VALUES.get(thisPrefsName));
                Log.i("Preference Helper", "Got Int preference " + thisPrefsName
                        + " with value " + value);
                return value;
            } else {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            Log.e("Preference Helper", "Unable to get Int preference: " + thisPrefsName);
            return 0;
        }
    }

    Boolean getBooleanPrefs(final String thisPrefsName) {
        try {
            if (DEFAULT_VALUES.containsKey(thisPrefsName) || !(DEFAULT_VALUES.get(thisPrefsName) instanceof Boolean)) {
                Boolean value = prefsFile.getBoolean(thisPrefsName, (Boolean) DEFAULT_VALUES.get(thisPrefsName));
                Log.i("Preference Helper", "Got Boolean preference: " + thisPrefsName
                        + " with value " + value);
                return value;
            } else {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            Log.e("Preference Helper", "Unable to get Boolean preference: " + thisPrefsName);
            return false;
        }
    }

    String getStringPrefs(final String thisPrefsName) {
        try {
            if (DEFAULT_VALUES.containsKey(thisPrefsName) || !(DEFAULT_VALUES.get(thisPrefsName) instanceof String)) {
                String value = prefsFile.getString(thisPrefsName, (String) DEFAULT_VALUES.get(thisPrefsName));
                Log.i("Preference Helper", "Got String preference: " + thisPrefsName
                        + " with value " + value);
                return value;
            } else {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            Log.e("Preference Helper", "Unable to get String preference: " + thisPrefsName);
            return null;
        }
    }

    void editPrefs(final String thisPrefsName, final Object thisPrefsValue) {
        try {
            if (DEFAULT_VALUES.containsKey(thisPrefsName)) {
                if (thisPrefsValue instanceof Integer) {
                    if (!(DEFAULT_VALUES.get(thisPrefsName) instanceof Integer)) {
                        throw new IllegalArgumentException();
                    }
                    prefsFile.edit().putInt(thisPrefsName, (Integer) thisPrefsValue).apply();
                    Log.i("Preference Helper", "Edited Int preference "
                            + thisPrefsName + " with value " + thisPrefsValue);
                } else if (thisPrefsValue instanceof Boolean) {
                    if (!(DEFAULT_VALUES.get(thisPrefsName) instanceof Boolean)) {
                        throw new IllegalArgumentException();
                    }
                    prefsFile.edit().putBoolean(thisPrefsName, (Boolean) thisPrefsValue).apply();
                    Log.i("Preference Helper", "Edited Boolean preference "
                            + thisPrefsName + " with value " + thisPrefsValue);
                } else if (thisPrefsValue instanceof String) {
                    if (!(DEFAULT_VALUES.get(thisPrefsName) instanceof String)) {
                        throw new IllegalArgumentException();
                    }
                    prefsFile.edit().putString(thisPrefsName, (String) thisPrefsValue).apply();
                    Log.i("Preference Helper", "Edited String preference "
                            + thisPrefsName + " with value " + thisPrefsValue);
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            Log.e("Preference Helper", "Unable to edit preference "
                    + thisPrefsName + " with value " + thisPrefsValue);
        }
    }

    void clearPrefs(final String thisPrefsName) {
        editPrefs(thisPrefsName, DEFAULT_VALUES.get(thisPrefsName));
    }

    void clearPrefs() {
        try {
            prefsFile.edit().clear().apply();
            Log.w("Preference Helper", "Preference file cleared");
        } catch (Exception e) {
            Log.e("Preference Helper", "Unable to clear preference");
        }
    }
}
