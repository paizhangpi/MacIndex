package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;

public class SearchActivity extends AppCompatActivity {

    private final MachineHelper thisMachineHelper = MainActivity.getMachineHelper();

    private final PrefsHelper prefs = MainActivity.getPrefs();

    private SearchView searchText = null;

    private TextView textResult = null;

    private TextView textIllegalInput = null;

    private LinearLayout currentLayout = null;

    private String currentManufacturer = null;

    private String currentOption = null;

    private Button optionsButton;

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

        currentManufacturer = prefs.getStringPrefs("searchManufacturer");
        currentOption = prefs.getStringPrefs("searchOption");

        optionsButton = findViewById(R.id.buttonShowFilters);
        optionsButton.setText(getString(prefs.getIntPrefs("currentManufacturerResource"))
                + " / " + getString(prefs.getIntPrefs("currentOptionResource")));
        optionsButton.setOnClickListener(view -> initOptions());

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
        final AlertDialog.Builder optionsDialog = new AlertDialog.Builder(SearchActivity.this);
        //optionsDialog.setTitle(R.string.search_filters);
        optionsDialog.setCancelable(false);
        optionsDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
            optionsButton.setText(getString(prefs.getIntPrefs("currentManufacturerResource"))
                    + "/" + getString(prefs.getIntPrefs("currentOptionResource")));
            startSearch(searchText.getQuery().toString());
        });

        final View optionChunk = getLayoutInflater().inflate(R.layout.chunk_search_filters, null);
        final RadioGroup manufacturerOptions = optionChunk.findViewById(R.id.groupsOptions);
        final RadioGroup searchOptions = optionChunk.findViewById(R.id.searchOptions);
        manufacturerOptions.check(prefs.getIntPrefs("searchManufacturerSelection"));
        searchOptions.check(prefs.getIntPrefs("searchOptionSelection"));
        manufacturerOptions.setOnCheckedChangeListener((radioGroup, i) -> {
            int toEditManufacturerResource;
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.id0Group:
                    currentManufacturer = "all";
                    toEditManufacturerResource = R.string.menu_group0;
                    break;
                case R.id.id1Group:
                    currentManufacturer = "apple68k";
                    toEditManufacturerResource = R.string.menu_group1;
                    break;
                case R.id.id2Group:
                    currentManufacturer = "appleppc";
                    toEditManufacturerResource = R.string.menu_group2;
                    break;
                case R.id.id3Group:
                    currentManufacturer = "appleintel";
                    toEditManufacturerResource = R.string.menu_group3;
                    break;
                case R.id.id4Group:
                    currentManufacturer = "applearm";
                    toEditManufacturerResource = R.string.menu_group4;
                    break;
                default:
                    Log.e("getOption", "Not a Valid Manufacturer Selection, This should NOT happen!!");
                    currentManufacturer = "all";
                    toEditManufacturerResource = R.string.menu_group0;
            }
            prefs.editPrefs("searchManufacturer", currentManufacturer);
            prefs.editPrefs("searchManufacturerSelection", radioGroup.getCheckedRadioButtonId());
            prefs.editPrefs("currentManufacturerResource", toEditManufacturerResource);
        });
        searchOptions.setOnCheckedChangeListener((radioGroup, i) -> {
            int toEditOptionResource;
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.nameOption:
                    currentOption = "sindex";
                    toEditOptionResource = R.string.search_nameOption;
                    break;
                case R.id.modelOption:
                    currentOption = "model";
                    toEditOptionResource = R.string.search_modelOption;
                    break;
                case R.id.midOption:
                    currentOption = "mid";
                    toEditOptionResource = R.string.search_idOption;
                    break;
                default:
                    Log.e("getOption", "Not a Valid Search Column Selection, This should NOT happen!!");
                    currentOption = "sindex";
                    toEditOptionResource = R.string.search_nameOption;
            }
            prefs.editPrefs("searchOption", currentOption);
            prefs.editPrefs("searchOptionSelection", radioGroup.getCheckedRadioButtonId());
            prefs.editPrefs("currentOptionResource", toEditOptionResource);
        });

        optionsDialog.setView(optionChunk);
        optionsDialog.show();
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
        // Identification: acceptable search input A~Z, a~z, 0~9, comma.
        final String legalCharactersIdentification = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxzy0123456789,";

        String legalCharacters;
        // update
        switch (method) {
            case "sindex":
                legalCharacters = legalCharactersName;
                break;
            case "model":
                legalCharacters = legalCharactersModel;
                break;
            case "mid":
                legalCharacters = legalCharactersIdentification;
                break;
            default:
                Log.e("validate", "Not a Valid Search Method, This should NOT happen!!");
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
            for (int position : positions) {
                final View mainChunk = getLayoutInflater().inflate(R.layout.chunk_main, null);
                final TextView machineName = mainChunk.findViewById(R.id.machineName);
                final TextView machineYear = mainChunk.findViewById(R.id.machineYear);
                final LinearLayout mainChunkToClick = mainChunk.findViewById(R.id.main_chunk_clickable);

                final int machineID = position;

                // Find information necessary for interface.
                final String thisName = thisMachineHelper.getName(machineID);
                final String thisYear = thisMachineHelper.getYear(machineID);
                final String thisLinks = thisMachineHelper.getConfig(machineID);

                machineName.setText(thisName);
                machineYear.setText(thisYear);

                mainChunkToClick.setOnClickListener(unused -> {
                    if (prefs.getBooleanPrefs("isOpenEveryMac")) {
                        LinkLoadingHelper.loadLinks(thisName, thisLinks, SearchActivity.this);
                    } else {
                        sendIntent(positions, machineID);
                    }
                });
                currentLayout.addView(mainChunk);
            }
        } catch (Exception e) {
            ExceptionHelper.handleExceptionWithDialog(this, e);
        }
    }

    // Keep compatible with MainActivity.
    private void sendIntent(final int[] thisCategory, final int thisMachineID) {
        final Intent intent = new Intent(this, SpecsActivity.class);
        intent.putExtra("thisCategory", thisCategory);
        intent.putExtra("machineID", thisMachineID);
        startActivity(intent);
    }
}
