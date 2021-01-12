package com.macindex.macindex;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import java.util.Locale;

class LinkLoadingHelper {

    public static void loadLinks(final String thisName, final String thisLinks, final Context thisContext) {
        try {
            if (thisLinks.equals("null")) {
                throw new IllegalArgumentException();
            }
            if (thisLinks.equals("N")) {
                Toast.makeText(thisContext,
                        MainActivity.getRes().getString(R.string.link_not_available), Toast.LENGTH_LONG).show();
                return;
            }
            final String[] linkGroup = thisLinks.split("html;");

            if (linkGroup.length == 1) {
                // Only one option, launch EveryMac directly.
                startEveryMac(linkGroup[0].split(",http")[0], "http" + linkGroup[0].split(",http")[1], thisContext);
            } else {
                // Fix ; and , split bug.
                for (int i = 0; i < linkGroup.length; i++) {
                    linkGroup[i] = linkGroup[i] + "html";
                }
                final AlertDialog.Builder linkDialog = new AlertDialog.Builder(thisContext);
                linkDialog.setTitle(thisName);
                linkDialog.setMessage(MainActivity.getRes().getString(R.string.link_message));
                // Setup each option in dialog.
                final View linkChunk = ((LayoutInflater) thisContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chunk_links, null);
                final RadioGroup linkOptions = linkChunk.findViewById(R.id.option);
                for (int i = 0; i < linkGroup.length; i++) {
                    final RadioButton linkOption = new RadioButton(thisContext);
                    linkOption.setText(linkGroup[i].split(",http")[0]);
                    linkOption.setId(i);
                    if (i == 0) {
                        linkOption.setChecked(true);
                    }
                    linkOptions.addView(linkOption);
                }
                linkDialog.setView(linkChunk);

                // When user tapped confirm or cancel...
                linkDialog.setPositiveButton(MainActivity.getRes().getString(R.string.link_confirm),
                        (dialog, which) -> {
                            try {
                                startEveryMac(linkGroup[linkOptions.getCheckedRadioButtonId()]
                                        .split(",http")[0], "http" + linkGroup[linkOptions.getCheckedRadioButtonId()]
                                        .split(",http")[1], thisContext);
                            } catch (Exception e) {
                                ExceptionHelper.handleException(thisContext, e, null, null);
                            }
                        });
                linkDialog.setNegativeButton(MainActivity.getRes().getString(R.string.link_cancel),
                        (dialog, which) -> {
                            // Cancelled.
                        });
                linkDialog.show();
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(thisContext, e,
                    "loadLinks", "Link loading failed for name " + thisName + " and string " + thisLinks);
        }
    }

    public static void startBrowser(final String urlEnglish, final String urlChinese, final Context thisContext) {
        try {
            if (urlEnglish == null && urlChinese == null) {
                throw new IllegalArgumentException();
            }
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(ContextCompat.getColor(thisContext, R.color.colorPrimary));
            CustomTabsIntent customTabsIntent = builder.build();
            if (Locale.getDefault().getDisplayLanguage().equals("中文")) {
                if (urlChinese == null) {
                    final AlertDialog.Builder langWarningDialog = new AlertDialog.Builder(thisContext);
                    langWarningDialog.setMessage(R.string.link_only_available);
                    langWarningDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                        customTabsIntent.launchUrl(thisContext, Uri.parse(urlEnglish));
                    });
                    langWarningDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
                        // Cancelled, nothing to do.
                    });
                    langWarningDialog.show();
                } else {
                    customTabsIntent.launchUrl(thisContext, Uri.parse(urlChinese));
                }
            } else {
                if (urlEnglish == null) {
                    final AlertDialog.Builder langWarningDialog = new AlertDialog.Builder(thisContext);
                    langWarningDialog.setMessage(R.string.link_only_available);
                    langWarningDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                        customTabsIntent.launchUrl(thisContext, Uri.parse(urlChinese));
                    });
                    langWarningDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
                        // Cancelled, nothing to do.
                    });
                    langWarningDialog.show();
                } else {
                    customTabsIntent.launchUrl(thisContext, Uri.parse(urlEnglish));
                }
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(thisContext, e,
                    "startBrowserCustomTabs", "Failed to open " + urlEnglish + ", " + urlChinese);
        }
    }

    public static void startEveryMac(final String thisName, final String url, final Context thisContext) {
        startBrowser(url, url, thisContext);
        Toast.makeText(thisContext,
                MainActivity.getRes().getString(R.string.link_opening) + thisName, Toast.LENGTH_LONG).show();
    }
}
