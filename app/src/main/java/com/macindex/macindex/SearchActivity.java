package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends AppCompatActivity {

    private MachineHelper thisMachineHelper = MainActivity.getMachineHelper();

    private SharedPreferences prefs = MainActivity.getPrefs();

    private TextView textResult = null;

    private TextView textIllegalInput = null;

    private RadioGroup searchOptions = null;

    private LinearLayout currentLayout = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        this.setTitle(getResources().getString(R.string.search));

        textResult = findViewById(R.id.textResult);
        textIllegalInput = findViewById(R.id.textIllegalInput);

        textResult.setVisibility(View.GONE);
        textIllegalInput.setVisibility(View.GONE);

        searchOptions = findViewById(R.id.searchOptions);
        currentLayout = findViewById(R.id.resultFullContainer);

        initSearch();
    }

    private String getOption() {
        switch (searchOptions.getCheckedRadioButtonId()) {
            case R.id.nameOption:
                Log.i("getOption", "Option Name");
                return "name";
            case R.id.modelOption:
                Log.i("getOption", "Option Model");
                return "model";
            default:
                Log.e("getOption", "Not a Valid Selection");
                return "";
        }
    }

    private void initSearch() {
        TextView searchText = findViewById(R.id.searchInput);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action
            }

            @Override
            public void afterTextChanged(Editable s) {
                TextView illegalInput = findViewById(R.id.textIllegalInput);
                illegalInput.setVisibility(View.GONE);
                currentLayout.removeAllViews();
                String searchInput = s.toString().trim();
                if (!searchInput.equals("")) {
                    if (validate(searchInput, getOption())) {
                        performSearch(s.toString());
                    } else {
                        illegalInput.setVisibility(View.VISIBLE);
                    }
                } else {
                    textResult.setVisibility(View.GONE);
                }
            }
        });
    }

    // Acceptable search input: A~Z, a~z, 0~9, whitespace, /, (), dash, comma, plus.
    public static boolean validate(final String validateInput, final String method) {
        final String legalCharactersName = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxzy0123456789 /()-,+";
        final String legalCharactersModel = "AM1234567890";
        String legalCharacters;
        switch (method) {
            case "name":
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
            textResult.setVisibility(View.VISIBLE);
            int[][] positions = thisMachineHelper.searchHelper(getOption(), searchInput);
            int resultCount = positions.length;
            if (positions.length == 0) {
                textResult.setText(R.string.search_noResult);
            } else {
                textResult.setText(getString(R.string.search_found) + String.valueOf(resultCount) + getString(R.string.search_results));
            }

            // Largely adapted MainActivity InitCategory. Should update both.
            for (int i = 0; i < resultCount; i++) {
                View mainChunk = getLayoutInflater().inflate(R.layout.chunk_main, null);
                TextView machineName = mainChunk.findViewById(R.id.machineName);
                TextView machineYear = mainChunk.findViewById(R.id.machineYear);

                final int machineID = thisMachineHelper.findByPosition(positions[i]);

                // Find information necessary for interface.
                final String thisName = thisMachineHelper.getName(machineID);
                final String thisYear = thisMachineHelper.getYear(machineID);
                final String thisLinks = thisMachineHelper.getConfig(machineID);

                machineName.setText(thisName);
                machineYear.setText(thisYear);

                machineName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View unused) {
                        if (prefs.getBoolean("isOpenEveryMac", false)) {
                            loadLinks(thisName, thisLinks);
                        } else {
                            sendIntent(machineID);
                        }
                    }
                });

                machineYear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View unused) {
                        if (prefs.getBoolean("isOpenEveryMac", false)) {
                            loadLinks(thisName, thisLinks);
                        } else {
                            sendIntent(machineID);
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
    private void sendIntent(final int thisMachineID) {
        Intent intent = new Intent(this, SpecsActivity.class);
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
                AlertDialog.Builder linkDialog = new AlertDialog.Builder(this);
                linkDialog.setTitle(thisName);
                linkDialog.setMessage(getResources().getString(R.string.link_message));
                // Setup each option in dialog.
                View linkChunk = getLayoutInflater().inflate(R.layout.chunk_links, null);
                final RadioGroup linkOptions = linkChunk.findViewById(R.id.option);
                for (int i = 0; i < linkGroup.length; i++) {
                    RadioButton linkOption = new RadioButton(this);
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
            Intent browser = new Intent(Intent.ACTION_VIEW);
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
