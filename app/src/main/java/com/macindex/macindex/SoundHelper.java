package com.macindex.macindex;

import android.util.Log;

public class SoundHelper {

    public static int getSound(final String thisSound) {
        Log.i("SoundHelper", "Get ID " + thisSound);
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
                return R.raw.powermac;
            case "8":
                return R.raw.newmac;
            case "9":
                return R.raw.tam;
            default:
                return 0;
        }
    }
}