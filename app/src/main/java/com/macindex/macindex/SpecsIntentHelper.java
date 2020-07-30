package com.macindex.macindex;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Arrays;

class SpecsIntentHelper {

    public static void sendIntent(final int[] thisCategory, final int thisMachineID,
                            final Context parentContext) {
        Intent intent = new Intent(parentContext, SpecsActivity.class);
        intent.putExtra("thisCategory", thisCategory);
        intent.putExtra("machineID", thisMachineID);
        Log.i("sendIntent", "Category IDs " + Arrays.toString(thisCategory)
                + ", thisMachineID " + thisMachineID);
        parentContext.startActivity(intent);
    }
}
