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
     * Twice if restored from a savedInstanceState
     * They are called outside the onCreate, I don't know what happened
     * Below are patches for the weird system call
     */
    private int filterSpinnerCallingPatch = 1;

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

        // Run a disable check here..
        disableCheck();

        if (savedInstanceState != null) {
            // Patch; see above
            filterSpinnerCallingPatch++;
            optionsSpinnerCallingPatch++;
            searchText.setQuery(savedInstanceState.getCharSequence("searchInput"), false);
            if (savedInstanceState.getBoolean("loadComplete")) {
                positions = savedInstanceState.getIntArray("positions");
                performSearch(null, false);
            } else {
                performSearch(savedInstanceState.getCharSequence("searchInput").toString(), true);
            }
        }

        Log.i("SearchActivity", "Current Query: " + searchText.getQuery()
                + ", Current Manufacturer: " + translateFiltersParam() + ", Current Option: " + translateOptionsParam());
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
            case R.id.everymacItem:
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
                    Arrays.asList(getResources().getStringArray(R.array.search_Options))) {
                @Override
                public boolean isEnabled(int position) {
                    // Disable Identification and EMC if current selection is 68K
                    // Disable Gestalt if current selection is Intel or ARM
                    if ((position == 3 || position == 6) && translateFiltersParam().equals("apple68k")) {
                        return false;
                    } else if (position == 4 && (translateFiltersParam().equals("appleintel") || translateFiltersParam().equals("applearm"))) {
                        return false;
                    } else {
                        return true;
                    }
                }

                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View mView = super.getDropDownView(position, convertView, parent);
                    TextView mText = (TextView) mView;
                    if (isEnabled(position)) {
                        mText.setTextColor(Color.BLACK);
                    } else {
                        mText.setTextColor(Color.GRAY);
                    }
                    return mView;
                }
            };

            filtersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            optionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            filtersSpinner.setAdapter(filtersAdapter);
            optionsSpinner.setAdapter(optionsAdapter);

            filtersSpinner.setSelection(PrefsHelper.getIntPrefs("lastSearchFiltersSpinner", this));
            optionsSpinner.setSelection(PrefsHelper.getIntPrefs("lastSearchOptionsSpinner", this));

            filtersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.w("ReloadSpinnerCallDebug", "Filter Patch " + filterSpinnerCallingPatch);
                    if (filterSpinnerCallingPatch <= 0) {
                        Log.w("ReloadSpinnerCallDebug", "Filter Executed");
                        PrefsHelper.editPrefs("lastSearchFiltersSpinner", i, SearchActivity.this);
                        disableCheck();
                    } else {
                        filterSpinnerCallingPatch--;
                    }
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

    private String translateOptionsParam() {
        int thisSelection = PrefsHelper.getIntPrefs("lastSearchOptionsSpinner", this);
        switch (thisSelection) {
            case 0:
                return "special";
            case 1:
                return "sname";
            case 2:
                return "smodel";
            case 3:
                return "sident";
            case 4:
                return "sgestalt";
            case 5:
                return "sorder";
            case 6:
                return "semc";
            default:
                ExceptionHelper.handleException(this, null,
                        "translateOptionsParam",
                        "Not a Valid Search Column Selection, This should NOT happen!!");
                return "sname";
        }
    }

    private boolean translateMatchParam() {
        int thisSelection = PrefsHelper.getIntPrefs("lastSearchOptionsSpinner", this);
        switch (thisSelection) {
            case 0:
            case 1:
            case 5:
                return false;
            case 2:
            case 3:
            case 4:
            case 6:
                return true;
            default:
                ExceptionHelper.handleException(this, null,
                        "translateMatchParam",
                        "Not a Valid Search Column Selection, This should NOT happen!!");
                return false;
        }
    }

    private void disableCheck() {
        final int position = optionsSpinner.getSelectedItemPosition();
        // Disable Identification and EMC if current selection is 68K
        // Disable Gestalt if current selection is Intel or ARM
        if ((position == 3 || position == 6) && translateFiltersParam().equals("apple68k")) {
            final AlertDialog.Builder disableDialog = new AlertDialog.Builder(SearchActivity.this);
            disableDialog.setMessage(R.string.search_disable_identification);
            disableDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                // Confirmed
            });
            disableDialog.show();
            optionsSpinner.setSelection(4);
            PrefsHelper.editPrefs("lastSearchOptionsSpinner", 4, this);
        } else if (position == 4 && (translateFiltersParam().equals("appleintel") || translateFiltersParam().equals("applearm"))) {
            final AlertDialog.Builder disableDialog = new AlertDialog.Builder(SearchActivity.this);
            disableDialog.setMessage(R.string.search_disable_gestalt);
            disableDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                // Confirmed
            });
            disableDialog.show();
            optionsSpinner.setSelection(3);
            PrefsHelper.editPrefs("lastSearchOptionsSpinner", 3, this);
        }
    }

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
                    if (!characterCheck(searchInput, translateOptionsParam())
                            || !lengthCheck(searchInput, translateOptionsParam(), false)) {
                        setIllegal();
                    }
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

    private void setIllegal() {
        textResult.setText(R.string.search_illegal);
        textResult.setTextColor(Color.RED);
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
                + translateFiltersParam() + ", Current Option: " + translateOptionsParam());
        if (!searchInput.equals("")) {
            if (strictCheck(searchInput, translateOptionsParam())) {
                // For order number: clip country code.
                if (translateOptionsParam().equals("sorder")) {
                    searchInput = searchInput.substring(0, 5);
                    searchInput = searchInput.concat("LL/");
                }
                // Remove Results only before actual search starts.
                resetIllegal();
                clearSearch();
                performSearch(searchInput, true);
                return true;
            } else {
                // Illegal input
                setIllegal();
                return false;
            }
        } else {
            // No input
            resetIllegal();
            return false;
        }
    }

    private boolean characterCheck(final String validateInput, final String method) {
        // Name: acceptable search input A~Z, a~z, 0~9, whitespace, /, (), dash, comma, plus, dot.
        final String legalCharactersName = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxzy0123456789 /()-,+.";
        // Model Number: acceptable search input Aa, Mm, 0~9.
        final String legalCharactersModel = "AMam1234567890";
        // Identification: acceptable search input A~Z, a~z, 0~9, comma.
        final String legalCharactersIdentification = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxzy0123456789,";
        // Gestalt: acceptable search input 0~9.
        final String legalCharactersGestalt = "0123456789";
        // Order Number: acceptable search input A~Z, a~z, 0~9, /.
        final String legalCharactersOrder = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxzy0123456789/";
        // EMC Number: acceptable search input Cc, 0~9, -.
        final String legalCharactersEMC = "Cc0123456789-";

        String legalCharacters;
        // update
        switch (method) {
            case "special":
            case "sname":
                legalCharacters = legalCharactersName;
                break;
            case "smodel":
                legalCharacters = legalCharactersModel;
                break;
            case "sident":
                legalCharacters = legalCharactersIdentification;
                break;
            case "sgestalt":
                legalCharacters = legalCharactersGestalt;
                break;
            case "sorder":
                legalCharacters = legalCharactersOrder;
                break;
            case "semc":
                legalCharacters = legalCharactersEMC;
                break;
            default:
                ExceptionHelper.handleException(this, null,
                        "validate",
                        "Not a Valid Search Method, This should NOT happen!!");
                legalCharacters = "";
        }

        for (int i = 0; i < validateInput.length(); i++) {
            // If it contains illegal character, it is not valid.
            if (!legalCharacters.contains(String.valueOf(validateInput.charAt(i)))) {
                Log.i("validate", "Illegal Char Detected!");
                return false;
            }
        }
        return true;
    }

    // isStrict: only check the upper limit. 2021/8/1
    private boolean lengthCheck(final String validateInput, final String method, final boolean isStrict) {
        switch (method) {
            case "special":
            case "sname":
                return validateInput.length() <= 50;
            case "smodel":
                return (validateInput.length() <= 5) && (validateInput.length() == 5 || !isStrict);
            case "sident":
                return validateInput.length() <= 14 && (validateInput.length() >= 6 || !isStrict);
            case "sgestalt":
                return validateInput.length() <= 3;
            case "sorder":
                return validateInput.length() <= 9 && (validateInput.length() >= 5 || !isStrict);
            case "semc":
                return validateInput.length() <= 6 && (validateInput.length() >= 4 || !isStrict);
            default:
                ExceptionHelper.handleException(this, null,
                        "lengthCheck",
                        "Not a Valid Search Method, This should NOT happen!!");
                return false;
        }
    }

    // New pre-search validation. 2021/8/1
    private boolean strictCheck(final String validateInput, final String method) {
        String errorPrompt = "";
        switch (method) {
            case "special":
            case "sname":
                if (!characterCheck(validateInput, method)) {
                    errorPrompt = errorPrompt.concat(getString(R.string.search_illegal_name) + "\n");
                }
                if (!lengthCheck(validateInput, method, true)) {
                    errorPrompt = errorPrompt.concat(getString(R.string.search_length_name) + "\n");
                }
                break;
            case "smodel":
                if (!characterCheck(validateInput, method)) {
                    errorPrompt = errorPrompt.concat(getString(R.string.search_illegal_model) + "\n");
                }
                if (!lengthCheck(validateInput, method, true)) {
                    errorPrompt = errorPrompt.concat(getString(R.string.search_length_model) + "\n");
                }
                break;
            case "sident":
                if (!characterCheck(validateInput, method)) {
                    errorPrompt = errorPrompt.concat(getString(R.string.search_illegal_ident) + "\n");
                }
                if (!lengthCheck(validateInput, method, true)) {
                    errorPrompt = errorPrompt.concat(getString(R.string.search_length_ident) + "\n");
                }
                break;
            case "sgestalt":
                if (!characterCheck(validateInput, method)) {
                    errorPrompt = errorPrompt.concat(getString(R.string.search_illegal_gestalt) + "\n");
                }
                if (!lengthCheck(validateInput, method, true)) {
                    errorPrompt = errorPrompt.concat(getString(R.string.search_length_gestalt) + "\n");
                }
                break;
            case "sorder":
                if (!characterCheck(validateInput, method)) {
                    errorPrompt = errorPrompt.concat(getString(R.string.search_illegal_order) + "\n");
                }
                if (!lengthCheck(validateInput, method, true)) {
                    errorPrompt = errorPrompt.concat(getString(R.string.search_length_order) + "\n");
                }
                break;
            case "semc":
                if (!characterCheck(validateInput, method)) {
                    errorPrompt = errorPrompt.concat(getString(R.string.search_illegal_emc) + "\n");
                }
                if (!lengthCheck(validateInput, method, true)) {
                    errorPrompt = errorPrompt.concat(getString(R.string.search_length_emc) + "\n");
                }
                break;
            default:
                ExceptionHelper.handleException(this, null,
                        "strictCheck",
                        "Not a Valid Search Method, This should NOT happen!!");
                return false;
        }
        if (errorPrompt.length() > 0) {
            final AlertDialog.Builder inputWarningDialog = new AlertDialog.Builder(SearchActivity.this);
            inputWarningDialog.setTitle(R.string.search_illegal);
            errorPrompt = errorPrompt.concat(getString(R.string.search_checkInput));
            inputWarningDialog.setMessage(errorPrompt);
            inputWarningDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                // Confirmed
            });
            inputWarningDialog.show();
            return false;
        } else {
            return true;
        }
    }

    private void performSearch(final String searchInput, final boolean reloadPositions) {
        try {
            Log.i("performSearch", "Current Input " + searchInput + ", Current Manufacturer: "
                    + translateFiltersParam() + ", Current Option: " + translateOptionsParam() + ", Reload Flag: " + reloadPositions);
            if (reloadPositions) {
                waitDialog.show();
            }
            new Thread() {
                @Override
                public void run() {
                    if (reloadPositions) {
                        positions = MainActivity.getMachineHelper().searchHelper(translateOptionsParam(), searchInput, translateFiltersParam(),
                                SearchActivity.this, translateMatchParam());
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
