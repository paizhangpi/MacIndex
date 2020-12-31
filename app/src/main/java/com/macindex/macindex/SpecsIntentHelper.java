package com.macindex.macindex;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;

class SpecsIntentHelper {

    public static int initCategory(final LinearLayout currentLayout, final int[] machineIDs,
                                   final boolean isVisible, final Context thisContext) {
        int machineLoadedCount = 0;
        try {
            for (int thisMachineID : machineIDs) {
                final View mainChunk = ((LayoutInflater) thisContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.chunk_main, null);
                final TextView machineName = mainChunk.findViewById(R.id.machineName);
                final TextView machineYear = mainChunk.findViewById(R.id.machineYear);
                final LinearLayout mainChunkToClick = mainChunk.findViewById(R.id.main_chunk_clickable);

                // Find information necessary for interface.
                final String thisName = MainActivity.getMachineHelper().getName(thisMachineID);
                final String thisYear = MainActivity.getMachineHelper().getSYear(thisMachineID);
                final String thisLinks = MainActivity.getMachineHelper().getConfig(thisMachineID);

                machineName.setText(thisName);
                machineYear.setText(thisYear);

                mainChunkToClick.setOnClickListener(unused -> {
                    if (PrefsHelper.getBooleanPrefs("isOpenEveryMac", thisContext)) {
                        LinkLoadingHelper.loadLinks(thisName, thisLinks, thisContext);
                    } else {
                        SpecsIntentHelper.sendIntent(machineIDs, thisMachineID, thisContext);
                    }
                });

                if (!isVisible) {
                    mainChunk.setVisibility(View.GONE);
                }

                currentLayout.addView(mainChunk);
                machineLoadedCount++;
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(thisContext, e,
                    "initCategory", "Category initialization failed.");
        }
        return machineLoadedCount;
    }

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
