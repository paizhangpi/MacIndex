package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
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
        if (!(PrefsHelper.getBooleanPrefs("isSaveSearchUsage", this))) {
            PrefsHelper.clearPrefs("searchLastInput", this);
            PrefsHelper.clearPrefs("searchManufacturer", this);
            PrefsHelper.clearPrefs("searchOption", this);
            PrefsHelper.clearPrefs("searchManufacturerSelection", this);
            PrefsHelper.clearPrefs("searchOptionSelection", this);
        }

        // If EveryMac enabled, a message should append.
        if (PrefsHelper.getBooleanPrefs("isOpenEveryMac", this)) {
            this.setTitle(getString(R.string.menu_search) + getString(R.string.menu_group_everymac));
        } else {
            this.setTitle(R.string.menu_search);
        }

        final LinearLayout mainLayout = findViewById(R.id.mainLayout);
        LayoutTransition layoutTransition = mainLayout.getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);

        currentManufacturer = PrefsHelper.getStringPrefs("searchManufacturer", this);
        currentOption = PrefsHelper.getStringPrefs("searchOption", this);

        optionsButton = findViewById(R.id.buttonShowFilters);
        optionsButton.setText(getString(PrefsHelper.getIntPrefs("currentManufacturerResource", this))
                + " / " + getString(PrefsHelper.getIntPrefs("currentOptionResource", this)));
        optionsButton.setOnClickListener(view -> initOptions());

        initSearch();

        // Init search from last state
        searchText.setQuery(PrefsHelper.getStringPrefs("searchLastInput", this), true);
        Log.i("SearchActivity", "Current Query: " + searchText.getQuery()
                + ", Current Manufacturer: " + currentManufacturer + ", Current Option: " + currentOption);
    }

    @Override
    protected void onDestroy() {
        // Remember last state
        PrefsHelper.editPrefs("searchLastInput", searchText.getQuery().toString(), this);
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
            optionsButton.setText(getString(PrefsHelper.getIntPrefs("currentManufacturerResource", this))
                    + " / " + getString(PrefsHelper.getIntPrefs("currentOptionResource", this)));
            startSearch(searchText.getQuery().toString());
        });

        final View optionChunk = getLayoutInflater().inflate(R.layout.chunk_search_filters, null);
        final RadioGroup manufacturerOptions = optionChunk.findViewById(R.id.groupsOptions);
        final RadioGroup searchOptions = optionChunk.findViewById(R.id.searchOptions);
        manufacturerOptions.check(PrefsHelper.getIntPrefs("searchManufacturerSelection", this));
        searchOptions.check(PrefsHelper.getIntPrefs("searchOptionSelection", this));
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
                    ExceptionHelper.handleException(this, null,
                            "getOption",
                            "Not a Valid Manufacturer Selection, This should NOT happen!!");
                    currentManufacturer = "all";
                    toEditManufacturerResource = R.string.menu_group0;
            }
            PrefsHelper.editPrefs("searchManufacturer", currentManufacturer, this);
            PrefsHelper.editPrefs("searchManufacturerSelection", radioGroup.getCheckedRadioButtonId(), this);
            PrefsHelper.editPrefs("currentManufacturerResource", toEditManufacturerResource, this);
        });
        searchOptions.setOnCheckedChangeListener((radioGroup, i) -> {
            int toEditOptionResource;
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.nameOption:
                    currentOption = "sname";
                    toEditOptionResource = R.string.search_nameOption;
                    break;
                case R.id.modelOption:
                    currentOption = "smodel";
                    toEditOptionResource = R.string.search_modelOption;
                    break;
                case R.id.midOption:
                    currentOption = "sident";
                    toEditOptionResource = R.string.search_idOption;
                    break;
                case R.id.gestaltOption:
                    currentOption = "sgestalt";
                    toEditOptionResource = R.string.search_gestaltOption;
                    break;
                case R.id.orderOption:
                    currentOption = "sorder";
                    toEditOptionResource = R.string.search_orderOption;
                    break;
                case R.id.emcOption:
                    currentOption = "semc";
                    toEditOptionResource = R.string.search_emcOption;
                    break;
                default:
                    ExceptionHelper.handleException(this, null,
                            "getOption",
                            "Not a Valid Search Column Selection, This should NOT happen!!");
                    currentOption = "sindex";
                    toEditOptionResource = R.string.search_nameOption;
            }
            PrefsHelper.editPrefs("searchOption", currentOption, this);
            PrefsHelper.editPrefs("searchOptionSelection", radioGroup.getCheckedRadioButtonId(), this);
            PrefsHelper.editPrefs("currentOptionResource", toEditOptionResource, this);
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
        String searchInput = s.trim();
        Log.i("startSearch", "Current Input: " + searchInput + ", Current Manufacturer: "
                + currentManufacturer + ", Current Option: " + currentOption);
        textIllegalInput.setVisibility(View.GONE);
        currentLayout.removeAllViews();
        if (!searchInput.equals("")) {
            if (validate(searchInput, currentOption)) {
                performSearch(searchInput);
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
        // If the input is too long, it is not valid.
        if (validateInput.length() > 50) {
            Log.i("validate", "Input is too long!");
            return false;
        }
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
                ExceptionHelper.handleException(this, null,
                        "validate",
                        "Not a Valid Search Method, This should NOT happen!!");
                legalCharacters = "";
        }
        boolean status = true;
        for (int i = 0; i < validateInput.length(); i++) {
            if (!legalCharacters.contains(String.valueOf(validateInput.charAt(i)))) {
                Log.i("validate", "Illegal Char Detected!");
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
            Log.i("performSearch", SpecsIntentHelper.initCategory(currentLayout, positions,
                    true, this) + " Results loaded");
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, null, null);
        }
    }
}
