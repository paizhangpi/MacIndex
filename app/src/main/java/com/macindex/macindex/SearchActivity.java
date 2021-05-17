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

    private final MachineHelper thisMachineHelper = MainActivity.getMachineHelper();

    private SearchView searchText = null;

    private TextView textResult = null;

    private LinearLayout currentLayout = null;

    private Spinner filtersSpinner = null;

    private Spinner optionsSpinner = null;

    private TextView[][] loadedResults = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // If SearchActivity Usage is set to not be saved
        if (!(PrefsHelper.getBooleanPrefs("isSaveSearchUsage", this))) {
            PrefsHelper.clearPrefs("searchFiltersSpinner", this);
            PrefsHelper.clearPrefs("searchOptionsSpinner", this);
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

        filtersSpinner = findViewById(R.id.filtersSpinner);
        optionsSpinner = findViewById(R.id.optionsSpinner);

        initSpinners();
        initSearch();

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
                    if ((position == 2 || position == 5) && translateFiltersParam().equals("apple68k")) {
                        return false;
                    } else if (position == 3 && (translateFiltersParam().equals("appleintel") || translateFiltersParam().equals("applearm"))) {
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

            filtersSpinner.setSelection(PrefsHelper.getIntPrefs("searchFiltersSpinner", this));
            optionsSpinner.setSelection(PrefsHelper.getIntPrefs("searchOptionsSpinner", this));

            filtersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    PrefsHelper.editPrefs("searchFiltersSpinner", i, SearchActivity.this);
                    disableCheck();
                    clearResults();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // Nothing to do.
                }
            });

            optionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    PrefsHelper.editPrefs("searchOptionsSpinner", i, SearchActivity.this);
                    searchText.setQuery("", true);
                    searchText.clearFocus();
                    changeTips();
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
        int thisSelection = PrefsHelper.getIntPrefs("searchFiltersSpinner", this);
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
        int thisSelection = PrefsHelper.getIntPrefs("searchOptionsSpinner", this);
        switch (thisSelection) {
            case 0:
                return "sname";
            case 1:
                return "smodel";
            case 2:
                return "sident";
            case 3:
                return "sgestalt";
            case 4:
                return "sorder";
            case 5:
                return "semc";
            default:
                ExceptionHelper.handleException(this, null,
                        "translateOptionsParam",
                        "Not a Valid Search Column Selection, This should NOT happen!!");
                return "sname";
        }
    }

    private boolean translateMatchParam() {
        int thisSelection = PrefsHelper.getIntPrefs("searchOptionsSpinner", this);
        switch (thisSelection) {
            case 0:
            case 4:
                return false;
            case 1:
            case 2:
            case 3:
            case 5:
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
        if ((position == 2 || position == 5) && translateFiltersParam().equals("apple68k")) {
            final AlertDialog.Builder disableDialog = new AlertDialog.Builder(SearchActivity.this);
            disableDialog.setMessage(R.string.search_disable_identification);
            disableDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                // Confirmed
            });
            disableDialog.show();
            optionsSpinner.setSelection(3);
            PrefsHelper.editPrefs("searchOptionsSpinner", 3, this);
        } else if (position == 3 && (translateFiltersParam().equals("appleintel") || translateFiltersParam().equals("applearm"))) {
            final AlertDialog.Builder disableDialog = new AlertDialog.Builder(SearchActivity.this);
            disableDialog.setMessage(R.string.search_disable_gestalt);
            disableDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                // Confirmed
            });
            disableDialog.show();
            optionsSpinner.setSelection(2);
            PrefsHelper.editPrefs("searchOptionsSpinner", 2, this);
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
                String searchInput = newText.trim();
                textResult.setText(R.string.search_prompt);
                textResult.setTextColor(getColor(R.color.colorDefaultText));
                if (!searchInput.equals("")) {
                    if (!validate(searchInput, translateOptionsParam())) {
                        textResult.setText(R.string.search_illegal);
                        textResult.setTextColor(Color.RED);
                    }
                } else {
                    // No input
                    clearResults();
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

    private void clearResults() {
        textResult.setText(R.string.search_prompt);
        textResult.setTextColor(getColor(R.color.colorDefaultText));
        loadedResults = null;
        currentLayout.removeAllViews();
    }

    private void changeTips() {
        try {
            int thisSelection = PrefsHelper.getIntPrefs("searchOptionsSpinner", this);
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
        textResult.setText(R.string.search_prompt);
        textResult.setTextColor(getColor(R.color.colorDefaultText));
        loadedResults = null;
        currentLayout.removeAllViews();
        if (!searchInput.equals("")) {
            if (validate(searchInput, translateOptionsParam())) {
                performSearch(searchInput);
                return true;
            } else {
                // Illegal input
                textResult.setText(R.string.search_illegal);
                textResult.setTextColor(Color.RED);
                return false;
            }
        } else {
            // No input
            textResult.setText(R.string.search_prompt);
            textResult.setTextColor(getColor(R.color.colorDefaultText));
            return false;
        }
    }

    private boolean validate(final String validateInput, final String method) {
        // If the input is too long, it is not valid.
        if (validateInput.length() > 50) {
            Log.i("validate", "Input is too long!");
            return false;
        }
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
                    + translateFiltersParam() + ", Current Option: " + translateOptionsParam());
            ProgressDialog waitDialog = new ProgressDialog(SearchActivity.this);
            waitDialog.setMessage(getString(R.string.loading_search));
            waitDialog.setCancelable(false);
            waitDialog.show();
            new Thread() {
                @Override
                public void run() {
                    final int[] positions = thisMachineHelper.searchHelper(translateOptionsParam(), searchInput, translateFiltersParam(),
                            SearchActivity.this, translateMatchParam());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                waitDialog.dismiss();
                                final int resultCount = positions.length;
                                textResult.setVisibility(View.VISIBLE);
                                if (positions.length == 0) {
                                    textResult.setText(R.string.search_noResult);
                                    textResult.setTextColor(getColor(R.color.colorDefaultText));
                                } else {
                                    textResult.setText(getString(R.string.search_found) + resultCount + getString(R.string.search_results));
                                    textResult.setTextColor(getColor(R.color.colorDefaultText));
                                }
                                loadedResults = new TextView[1][positions.length];
                                loadedResults[0] = SpecsIntentHelper.initCategory(currentLayout, positions,
                                        true, SearchActivity.this);
                                SpecsIntentHelper.refreshFavourites(loadedResults, SearchActivity.this);
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
