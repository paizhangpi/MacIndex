package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchText = null;

    private TextView textResult = null;

    private LinearLayout currentLayout = null;

    private Spinner filtersSpinner = null;

    private Spinner optionsSpinner = null;

    private TextView[][] loadedResults = null;

    private int[] positions = null;

    private ProgressDialog waitDialog = null;

    /**
     * setOnItemSelectedListener() was called by system weirdly
     * Once if no savedInstanceState was saved
     * ? if restored from a savedInstanceState
     * They are called outside the onCreate, I don't know what happened
     * Below are patches for the weird system call
     */
    private int optionsSpinnerCallingPatch = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        MainActivity.validateOperation(this);

        // Set the dialog AT HERE; its structure is different from other activities
        waitDialog = new ProgressDialog(SearchActivity.this);
        waitDialog.setMessage(getString(R.string.loading_search));
        waitDialog.setCancelable(false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        this.setTitle(R.string.menu_search);

        final LinearLayout mainLayout = findViewById(R.id.mainLayout);
        LayoutTransition layoutTransition = mainLayout.getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);

        filtersSpinner = findViewById(R.id.filtersSpinner);
        optionsSpinner = findViewById(R.id.optionsSpinner);

        // If SearchActivity Usage is set to not be saved
        if (!(PrefsHelper.getBooleanPrefs("isSaveSearchUsage", this))) {
            PrefsHelper.clearPrefs("lastSearchFiltersSpinner", this);
            PrefsHelper.clearPrefs("lastSearchOptionsSpinner", this);
        }

        initSpinners();
        initSearch();

        // Init Search Prompt at Here!!
        resetIllegal();

        if (savedInstanceState != null) {
            /* Patch Increment Placeholder */
            searchText.setQuery(savedInstanceState.getCharSequence("searchInput"), false);
            if (savedInstanceState.getBoolean("loadComplete")) {
                positions = savedInstanceState.getIntArray("positions");
                performSearch(null, false);
            } else {
                performSearch(savedInstanceState.getCharSequence("searchInput").toString(), true);
            }
        }

        Log.i("SearchActivity", "Current Query: " + searchText.getQuery()
                + ", Current Manufacturer: " + translateFiltersParam() + ", Current Option: " + (translateMatchParam()? "MODEL NO." : "NAME"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.searchClearItem:
                resetIllegal();
                clearSearch();
                break;
            case R.id.searchResetItem:
                PrefsHelper.editPrefs("lastSearchFiltersSpinner", 0, SearchActivity.this);
                PrefsHelper.editPrefs("lastSearchOptionsSpinner", 0, SearchActivity.this);
                filtersSpinner.setSelection(0);
                optionsSpinner.setSelection(0);
                searchText.setQuery("", true);
                searchText.clearFocus();
                changeTips();
                break;
            case R.id.searchAppleSNItem:
                LinkLoadingHelper.startBrowser("https://checkcoverage.apple.com/", "https://checkcoverage.apple.com/", this);
                break;
            case R.id.searchEveryMacItem:
                LinkLoadingHelper.startBrowser("https://everymac.com/ultimate-mac-lookup/", "https://everymac.com/ultimate-mac-lookup/", this);
                break;
            case R.id.searchHelpItem:
                LinkLoadingHelper.startBrowser(null, "https://macindex.paizhang.info/search", this);
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SpecsIntentHelper.refreshFavourites(loadedResults, this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!waitDialog.isShowing()) {
            outState.putBoolean("loadComplete", true);
            outState.putIntArray("positions", positions);
            outState.putCharSequence("searchInput", searchText.getQuery());
        } else {
            outState.putBoolean("loadComplete", false);
            outState.putCharSequence("searchInput", searchText.getQuery());
            MainActivity.reloadDatabase(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (waitDialog.isShowing()) {
            waitDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void initSpinners() {
        try {
            ArrayAdapter<CharSequence> filtersAdapter = ArrayAdapter.createFromResource(this,
                    R.array.search_Filters, android.R.layout.simple_spinner_item);
            ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                    Arrays.asList(getResources().getStringArray(R.array.search_Options)));

            filtersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            optionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            filtersSpinner.setAdapter(filtersAdapter);
            optionsSpinner.setAdapter(optionsAdapter);

            filtersSpinner.setSelection(PrefsHelper.getIntPrefs("lastSearchFiltersSpinner", this));
            optionsSpinner.setSelection(PrefsHelper.getIntPrefs("lastSearchOptionsSpinner", this));

            filtersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    PrefsHelper.editPrefs("lastSearchFiltersSpinner", i, SearchActivity.this);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // Nothing to do.
                }
            });

            optionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.w("ReloadSpinnerCallDebug", "Options Patch " + optionsSpinnerCallingPatch);
                    if (optionsSpinnerCallingPatch <= 0) {
                        Log.w("ReloadSpinnerCallDebug", "Options Executed");
                        PrefsHelper.editPrefs("lastSearchOptionsSpinner", i, SearchActivity.this);
                        searchText.setQuery("", true);
                        searchText.clearFocus();
                        changeTips();
                    } else {
                        if (searchText.getQuery().toString().equals("")) {
                            changeTips();
                        }
                        optionsSpinnerCallingPatch--;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // Nothing to do.
                }
            });
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "initSpinners", "Unable to initialize spinners.");
        }
    }

    private String translateFiltersParam() {
        int thisSelection = PrefsHelper.getIntPrefs("lastSearchFiltersSpinner", this);
        switch (thisSelection) {
            case 0:
                return "all";
            case 1:
                return "apple68k";
            case 2:
                return "appleppc";
            case 3:
                return "appleintel";
            case 4:
                return "applearm";
            default:
                ExceptionHelper.handleException(this, null,
                        "translateFilterParam",
                        "Not a Valid Manufacturer Selection, This should NOT happen!!");
                return "all";
        }
    }

    private String[] translateOptionsParam() {
        int thisSelection = PrefsHelper.getIntPrefs("lastSearchOptionsSpinner", this);
        switch (thisSelection) {
            case 0:
                return new String[]{"sname"};
            case 1:
                return new String[]{"smodel", "sident", "sgestalt", "sorder", "semc"};
            default:
                ExceptionHelper.handleException(this, null,
                        "translateOptionsParam",
                        "Not a Valid Search Column Selection, This should NOT happen!!");
                return new String[]{"sname"};
        }
    }

    private boolean translateMatchParam() {
        int thisSelection = PrefsHelper.getIntPrefs("lastSearchOptionsSpinner", this);
        switch (thisSelection) {
            case 0:
                return false;
            case 1:
                return true;
            default:
                ExceptionHelper.handleException(this, null,
                        "translateMatchParam",
                        "Not a Valid Search Column Selection, This should NOT happen!!");
                return false;
        }
    }

    /* Logic was improved since 4.8.2. disableCheck, lengthCheck, and strictCheck were removed. 8/30/2021 */

    private void initSearch() {
        searchText = findViewById(R.id.searchInput);
        textResult = findViewById(R.id.textResult);
        currentLayout = findViewById(R.id.resultFullContainer);

        searchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                searchText.clearFocus();
                return startSearch(query);
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                // TRIM to get the correct validation result.
                String searchInput = newText.trim();
                // Initialize on-the-fly validation.
                resetIllegal();
                if (!searchInput.equals("")) {
                    characterCheck(searchInput, translateMatchParam());
                } else {
                    // No input
                    resetIllegal();
                }
                return false;
            }
        });

        // Set auto-sizing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textResult.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        } else {
            TextViewCompat.setAutoSizeTextTypeWithDefaults(textResult, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        }
    }

    private void clearSearch() {
        loadedResults = null;
        currentLayout.removeAllViews();
    }

    private void resetIllegal() {
        textResult.setText(R.string.search_prompt);
        textResult.setTextColor(getColor(R.color.colorDefaultText));
    }

    private void changeTips() {
        try {
            int thisSelection = PrefsHelper.getIntPrefs("lastSearchOptionsSpinner", this);
            String[] searchTips = getResources().getStringArray(R.array.search_Tips);
            if (thisSelection >= searchTips.length) {
                throw new IllegalStateException();
            }
            searchText.setQueryHint(searchTips[thisSelection]);
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "changeTips", "Invalid Search Tips Configuration.");
        }
    }

    private boolean startSearch(final String s) {
        String searchInput = s.trim();
        Log.i("startSearch", "Current Input: " + searchInput + ", Current Manufacturer: "
                + translateFiltersParam() + ", Current Option: " + (translateMatchParam()? "MODEL NO." : "NAME"));
        if (!searchInput.equals("")) {
            if (characterCheck(searchInput, translateMatchParam())) {
                // Remove Results only before actual search starts.
                resetIllegal();
                clearSearch();
                performSearch(searchInput, true);
                return true;
            } else {
                return false;
            }
        } else {
            // No input
            resetIllegal();
            return false;
        }
    }

    private boolean characterCheck(final String validateInput, final boolean method) {
        // Check the length first
        if ((method && validateInput.length() > 20) || (!method && validateInput.length() > 50)) {
            Log.i("validate", "Overlength Detected!");
            // Set the overlength prompt here..
            textResult.setText(R.string.search_overlength);
            textResult.setTextColor(Color.RED);
            return false;
        }

        String legalCharacters;
        if (method) {
            // Model Numbers: acceptable search input A~Z, a~z, 0~9, comma, -, /.
            legalCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxzy0123456789,-/";
        } else {
            // Name: acceptable search input A~Z, a~z, 0~9, whitespace, /, (), dash, comma, plus, dot.
            legalCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxzy0123456789 /()-,+.";
        }

        for (int i = 0; i < validateInput.length(); i++) {
            // If it contains illegal character, it is not valid.
            if (!legalCharacters.contains(String.valueOf(validateInput.charAt(i)))) {
                Log.i("validate", "Illegal Char Detected!");
                // Set the illegal prompt here..
                textResult.setText(R.string.search_illegal);
                textResult.setTextColor(Color.RED);
                return false;
            }
        }
        return true;
    }

    private void performSearch(final String searchInput, final boolean reloadPositions) {
        try {
            Log.i("performSearch", "Reload Flag: " + reloadPositions);
            if (reloadPositions) {
                waitDialog.show();
            }
            new Thread() {
                @Override
                public void run() {
                    if (reloadPositions) {
                        final String[] searchColumns = translateOptionsParam();
                        int[][] subPositions = new int[searchColumns.length][];
                        String rawSearchInput;
                        boolean rawMatchParam;
                        int resultCount = 0;

                        // Search by translated columns
                        for (int i = 0; i < searchColumns.length; i++) {
                            // For order number: clip country code.
                            if (searchColumns[i].equals("sorder")) {
                                if (searchInput.length() < 5) {
                                    // omit this
                                    subPositions[i] = new int[0];
                                    continue;
                                }
                                // Overwrite input
                                rawSearchInput = searchInput.substring(0, 5);
                                rawSearchInput = rawSearchInput.concat("LL/");
                                // Overwrite match param.
                                rawMatchParam = false;
                            } else {
                                rawSearchInput = searchInput;
                                rawMatchParam = translateMatchParam();
                            }
                            Log.i("rawSearchInput", "Raw Input " + rawSearchInput + ", Current Manufacturer: "
                                    + translateFiltersParam() + ", Raw Option: " + searchColumns[i] + ", Match Parameter: " + rawMatchParam);
                            subPositions[i] = MainActivity.getMachineHelper().searchHelper(searchColumns[i], rawSearchInput, translateFiltersParam(),
                                    rawMatchParam, PrefsHelper.getBooleanPrefsSafe("isSortAgain", SearchActivity.this));
                            resultCount += subPositions[i].length;
                        }

                        // Add raw results
                        positions = new int[resultCount];
                        int previousCount = 0;
                        for (int i = 0; i < searchColumns.length; i++) {
                            for (int j = 0; j < subPositions[i].length; j++) {
                                positions[previousCount] = subPositions[i][j];
                                previousCount++;
                            }
                        }
                        // Check duplicate although IDK the necessarily
                        // positions = MainActivity.getMachineHelper().checkDuplicate(positions);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (reloadPositions) {
                                    waitDialog.dismiss();
                                }
                                // NullSafe
                                if (positions != null) {
                                    Log.i("performSearchLoad", "Position Length: "
                                            + positions.length + ", Reload Flag: " + reloadPositions);
                                    if (positions.length == 0) {
                                        textResult.setText(R.string.search_noResult);
                                        textResult.setTextColor(getColor(R.color.colorDefaultText));
                                    } else {
                                        textResult.setText(getString(R.string.search_found) + positions.length + getString(R.string.search_results));
                                        textResult.setTextColor(getColor(R.color.colorDefaultText));
                                    }
                                    loadedResults = new TextView[1][positions.length];
                                    loadedResults[0] = SpecsIntentHelper.initCategory(currentLayout, positions,
                                            true, SearchActivity.this);
                                    SpecsIntentHelper.refreshFavourites(loadedResults, SearchActivity.this);
                                }
                            } catch (final Exception e) {
                                ExceptionHelper.handleException(SearchActivity.this, e, null, null);
                            }
                        }
                    });
                }
            }.start();
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, null, null);
        }
    }
}
