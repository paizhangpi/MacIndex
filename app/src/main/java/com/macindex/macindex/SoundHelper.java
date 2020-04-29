package com.macindex.macindex;

import android.util.Log;

class SoundHelper {

    static int getSound(final String thisSound) {
        Log.i("getSound", "Get ID " + thisSound);
        switch (thisSound) {
            case "0":
                return R.raw.mac128;
            case "1":
                return R.raw.macii;
            case "2":
                return R.raw.maclc;
            case "3":
                return R.raw.quadra;
            case "4":
                return R.raw.quadraav;
            case "5":
                return R.raw.powermac6100;
            case "6":
                return R.raw.powermac5000;
            case "7":
            case "PB":
                return R.raw.powermac;
            case "8":
                return R.raw.newmac;
            case "9":
                return R.raw.tam;
            default:
                Log.i("getSound", "No startup sound for ID " + thisSound);
                return 0;
        }
    }

    static int getDeathSound(final String thisSound) {
        Log.i("getDeathSound", "Get ID " + thisSound);
        switch (thisSound) {
            case "1":
                return R.raw.macii_death;
            case "2":
            case "3":
            case "PB":
                return R.raw.maclc_death;
            case "4":
                return R.raw.quadraav_death;
            case "5":
                return R.raw.powermac6100_death;
            case "6":
                return R.raw.powermac5000_death;
            case "7":
            case "9":
                return R.raw.powermac_death;
            default:
                Log.i("getDeathSound", "No death sound for ID " + thisSound);
                return 0;
        }
    }
}
