package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends AppCompatActivity {

    private final MachineHelper thisMachineHelper = MainActivity.getMachineHelper();

    private final PrefsHelper prefs = MainActivity.getPrefs();

    private SearchView searchText = null;

    private TextView textResult = null;

    private TextView textIllegalInput = null;

    private LinearLayout currentLayout = null;

    private String currentManufacturer = null;

    private String currentOption = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // If SearchActivity Usage is set to not be saved
        if (!(prefs.getBooleanPrefs("isSaveSearchUsage"))) {
            prefs.clearPrefs("searchLastInput");
            prefs.clearPrefs("searchManufacturer");
            prefs.clearPrefs("searchOption");
            prefs.clearPrefs("searchManufacturerSelection");
            prefs.clearPrefs("searchOptionSelection");
        }

        // If EveryMac enabled, a message should append.
        if (prefs.getBooleanPrefs("isOpenEveryMac")) {
            this.setTitle(getString(R.string.menu_search) + getString(R.string.menu_group_everymac));
        } else {
            this.setTitle(R.string.menu_search);
        }

        final LinearLayout mainLayout = findViewById(R.id.mainLayout);
        LayoutTransition layoutTransition = mainLayout.getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);

        initOptions();
        initSearch();

        // Init search from last state
        searchText.setQuery(prefs.getStringPrefs("searchLastInput"), true);
        Log.i("SearchActivity", "Current Query: " + searchText.getQuery()
                + ", Current Manufacturer: " + currentManufacturer + ", Current Option: " + currentOption);
    }

    @Override
    protected void onDestroy() {
        // Remember last state
        prefs.editPrefs("searchLastInput", searchText.getQuery().toString());
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void initOptions() {
        currentManufacturer = prefs.getStringPrefs("searchManufacturer");
        currentOption = prefs.getStringPrefs("searchOption");

        final RadioGroup manufacturerOptions = findViewById(R.id.groupsOptions);
        final RadioGroup searchOptions = findViewById(R.id.searchOptions);
        manufacturerOptions.check(prefs.getIntPrefs("searchManufacturerSelection"));
        searchOptions.check(prefs.getIntPrefs("searchOptionSelection"));

        manufacturerOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final RadioGroup radioGroup, final int i) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.id0Group:
                        currentManufacturer = "all";
                        break;
                    case R.id.id1Group:
                        currentManufacturer = "apple68k";
                        break;
                    case R.id.id2Group:
                        currentManufacturer = "appleppc";
                        break;
                    case R.id.id3Group:
                        currentManufacturer = "appleintel";
                        break;
                    case R.id.id4Group:
                        currentManufacturer = "applearm";
                        break;
                    default:
                        Log.e("getOption", "Not a Valid Manufacturer Selection, This should NOT happen!!");
                        currentManufacturer = "all";
                }
                prefs.editPrefs("searchManufacturer", currentManufacturer);
                prefs.editPrefs("searchManufacturerSelection", radioGroup.getCheckedRadioButtonId());
                startSearch(searchText.getQuery().toString());
            }
        });
        searchOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final RadioGroup radioGroup, final int i) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.nameOption:
                        currentOption = "sindex";
                        break;
                    case R.id.modelOption:
                        currentOption = "model";
                        break;
                    default:
                        Log.e("getOption", "Not a Valid Search Column Selection, This should NOT happen!!");
                        currentOption = "sindex";
                }
                prefs.editPrefs("searchOption", currentOption);
                prefs.editPrefs("searchOptionSelection", radioGroup.getCheckedRadioButtonId());
                startSearch(searchText.getQuery().toString());
            }
        });
    }

    private void initSearch() {
        searchText = findViewById(R.id.searchInput);
        textResult = findViewById(R.id.textResult);
        textResult.setVisibility(View.GONE);
        textIllegalInput = findViewById(R.id.textIllegalInput);
        textIllegalInput.setVisibility(View.GONE);
        currentLayout = findViewById(R.id.resultFullContainer);

        searchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                return startSearch(query);
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                return startSearch(newText);
            }
        });
    }

    private boolean startSearch(final String s) {
        Log.i("startSearch", "Current Input: " + s + ", Current Manufacturer: "
                + currentManufacturer + ", Current Option: " + currentOption);
        textIllegalInput.setVisibility(View.GONE);
        currentLayout.removeAllViews();
        String searchInput = s.trim();
        if (!searchInput.equals("")) {
            if (validate(searchInput, currentOption)) {
                performSearch(s);
                return true;
            } else {
                // Illegal input
                textResult.setVisibility(View.GONE);
                textIllegalInput.setVisibility(View.VISIBLE);
                return false;
            }
        } else {
            // No input
            textResult.setVisibility(View.GONE);
            return true;
        }
    }

    private boolean validate(final String validateInput, final String method) {
        // Name: acceptable search input A~Z, a~z, 0~9, whitespace, /, (), dash, comma, plus.
        final String legalCharactersName = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxzy0123456789 /()-,+";
        // Model Number: acceptable search input Aa, Mm, 0~9.
        final String legalCharactersModel = "AMam1234567890";

        String legalCharacters;
        // update
        switch (method) {
            case "sindex":
                legalCharacters = legalCharactersName;
                break;
            case "model":
                legalCharacters = legalCharactersModel;
                break;
            default:
                legalCharacters = "";
        }
        boolean status = true;
        for (int i = 0; i < validateInput.length(); i++) {
            if (!legalCharacters.contains(String.valueOf(validateInput.charAt(i)))) {
                status = false;
            }
        }
        return status;
    }

    private void performSearch(final String searchInput) {
        try {
            Log.i("performSearch", "Current Input " + searchInput + ", Current Manufacturer: "
                    + currentManufacturer + ", Current Option: " + currentOption);
            final int[] positions = thisMachineHelper.searchHelper(currentOption, searchInput, currentManufacturer);
            final int resultCount = positions.length;
            textResult.setVisibility(View.VISIBLE);
            if (positions.length == 0) {
                textResult.setText(R.string.search_noResult);
            } else {
                textResult.setText(getString(R.string.search_found) + resultCount + getString(R.string.search_results));
            }

            // Largely adapted MainActivity InitCategory. Should update both.
            for (int i = 0; i < resultCount; i++) {
                final View mainChunk = getLayoutInflater().inflate(R.layout.chunk_main, null);
                final TextView machineName = mainChunk.findViewById(R.id.machineName);
                final TextView machineYear = mainChunk.findViewById(R.id.machineYear);

                final int machineID = positions[i];

                // Find information necessary for interface.
                final String thisName = thisMachineHelper.getName(machineID);
                final String thisYear = thisMachineHelper.getYear(machineID);
                final String thisLinks = thisMachineHelper.getConfig(machineID);

                machineName.setText(thisName);
                machineYear.setText(thisYear);

                machineName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View unused) {
                        if (prefs.getBooleanPrefs("isOpenEveryMac")) {
                            loadLinks(thisName, thisLinks);
                        } else {
                            sendIntent(positions, machineID);
                        }
                    }
                });

                machineYear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View unused) {
                        if (prefs.getBooleanPrefs("isOpenEveryMac")) {
                            loadLinks(thisName, thisLinks);
                        } else {
                            sendIntent(positions, machineID);
                        }
                    }
                });
                currentLayout.addView(mainChunk);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    // Keep compatible with MainActivity.
    private void sendIntent(final int[] thisCategory, final int thisMachineID) {
        final Intent intent = new Intent(this, SpecsActivity.class);
        intent.putExtra("thisCategory", thisCategory);
        intent.putExtra("machineID", thisMachineID);
        startActivity(intent);
    }

    // Copied from specsActivity, keep them compatible.
    private void loadLinks(final String thisName, final String thisLinks) {
        try {
            if (thisLinks.equals("N")) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.link_not_available), Toast.LENGTH_LONG).show();
                return;
            }
            final String[] linkGroup = thisLinks.split(";");
            if (linkGroup.length == 1) {
                // Only one option, launch EveryMac directly.
                startBrowser(linkGroup[0].split(",")[0], linkGroup[0].split(",")[1]);
            } else {
                final AlertDialog.Builder linkDialog = new AlertDialog.Builder(this);
                linkDialog.setTitle(thisName);
                linkDialog.setMessage(getResources().getString(R.string.link_message));
                // Setup each option in dialog.
                final View linkChunk = getLayoutInflater().inflate(R.layout.chunk_links, null);
                final RadioGroup linkOptions = linkChunk.findViewById(R.id.option);
                for (int i = 0; i < linkGroup.length; i++) {
                    final RadioButton linkOption = new RadioButton(this);
                    linkOption.setText(linkGroup[i].split(",")[0]);
                    linkOption.setId(i);
                    if (i == 0) {
                        linkOption.setChecked(true);
                    }
                    linkOptions.addView(linkOption);
                }
                linkDialog.setView(linkChunk);

                // When user tapped confirm or cancel...
                linkDialog.setPositiveButton(this.getResources().getString(R.string.link_confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                try {
                                    startBrowser(linkGroup[linkOptions.getCheckedRadioButtonId()]
                                            .split(",")[0], linkGroup[linkOptions.getCheckedRadioButtonId()]
                                            .split(",")[1]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(),
                                            getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                linkDialog.setNegativeButton(this.getResources().getString(R.string.link_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                // Cancelled.
                            }
                        });
                linkDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            Log.e("loadLinks", "Link loading failed!!");
        }
    }

    private void startBrowser(final String thisName, final String url) {
        try {
            final Intent browser = new Intent(Intent.ACTION_VIEW);
            browser.setData(Uri.parse(url));
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.link_opening) + thisName, Toast.LENGTH_LONG).show();
            startActivity(browser);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }
}
