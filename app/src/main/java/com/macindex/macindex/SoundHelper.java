package com.macindex.macindex;

public class SoundHelper {

    public static int getSound (String thisSound) {
        if (thisSound.equals("0")) {
            return R.raw.mac128;
        } else if (thisSound.equals("1")) {
            return R.raw.macii;
        } else if (thisSound.equals("2")) {
            return R.raw.maclc;
        } else if (thisSound.equals("3")) {
            return R.raw.quadra;
        } else if (thisSound.equals("4")) {
            return R.raw.quadraav;
        } else if (thisSound.equals("5")) {
            return R.raw.powermac6100;
        } else if (thisSound.equals("6")) {
            return R.raw.powermac5000;
        } else if (thisSound.equals("7")) {
            return R.raw.powermac;
        } else if (thisSound.equals("8")) {
            return R.raw.newmac;
        } else if (thisSound.equals("9")) {
            return R.raw.tam;
        } else {
            return 0;
        }
    }
}
