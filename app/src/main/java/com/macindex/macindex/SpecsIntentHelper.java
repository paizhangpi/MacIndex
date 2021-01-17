package com.macindex.macindex;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.TextViewCompat;

import java.util.Arrays;

class SpecsIntentHelper {

    public static TextView[] initCategory(final LinearLayout currentLayout, final int[] machineIDs,
                                   final boolean isVisible, final Context thisContext) {
        try {
            TextView[] machineLoaded = new TextView[machineIDs.length];
            for (int i = 0; i < machineIDs.length; i++) {
                final int thisMachineID = machineIDs[i];
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    machineName.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                } else {
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(machineName, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                }

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
                machineLoaded[i] = machineName;
            }
            return machineLoaded;
        } catch (Exception e) {
            ExceptionHelper.handleException(thisContext, e,
                    "initCategory", "Category initialization failed.");
            return null;
        }
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

    public static void refreshFavourites(final TextView[][] textViewGroup, final Context thisContext) {
        for (TextView[] thisViewGroup : textViewGroup) {
            // NullSafe
            if (thisViewGroup != null) {
                for (TextView thisView : thisViewGroup) {
                    if (FavouriteActivity.isFavourite(thisView.getText().toString(), thisContext)) {
                        thisView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_star_24, 0);
                    } else {
                        thisView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                    }
                }
            }
        }
    }
}
