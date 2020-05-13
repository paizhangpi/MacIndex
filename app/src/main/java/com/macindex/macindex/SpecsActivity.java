package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;

public class SpecsActivity extends AppCompatActivity {

    private Intent intent;

    private int machineID = -1;

    private boolean startup = true;

    private MediaPlayer startupSound = null;

    private MediaPlayer deathSound = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specs);
        try {
            intent = getIntent();
            machineID = intent.getIntExtra("machineID", -1);
            initialize();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }

    private void initialize() {
        try {
            if (machineID == -1) {
                throw new IllegalArgumentException();
            }
            initSpecs();
            initImage();
            initLinks();
            initButtons();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    private void release() {
        try {
            if (startupSound != null && startupSound.isPlaying()) {
                startupSound.stop();
                Log.i("releaseSound", "Startup sound stopped");
            }
            if (deathSound != null && deathSound.isPlaying()) {
                deathSound.stop();
                Log.i("releaseSound", "Death sound stopped");
            }
            if (startupSound != null) {
                startupSound.release();
                Log.i("releaseSound", "Startup sound released");
            }
            if (deathSound != null) {
                deathSound.release();
                Log.i("releaseSound", "Death sound released");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    private void initSpecs() {
        TextView name = findViewById(R.id.nameText);
        TextView processor = findViewById(R.id.processorText);
        TextView maxram = findViewById(R.id.maxramText);
        TextView year = findViewById(R.id.yearText);
        TextView model = findViewById(R.id.modelText);

        this.setTitle(MainActivity.getMachineHelper().getName(machineID));
        name.setText(MainActivity.getMachineHelper().getName(machineID));
        processor.setText(MainActivity.getMachineHelper().getProcessor(machineID));
        maxram.setText(MainActivity.getMachineHelper().getMaxRam(machineID));
        year.setText(MainActivity.getMachineHelper().getYear(machineID));
        model.setText(MainActivity.getMachineHelper().getModel(machineID));
    }

    private void initImage() {
        // Init image
        ImageView image = findViewById(R.id.pic);
        File imageFile = MainActivity.getMachineHelper().getPicture(machineID);
        if (imageFile.exists()) {
            Log.i("SpecsAct", "Image exists");
            image.setImageBitmap(BitmapFactory.decodeFile(imageFile.getPath()));
        }
        imageFile.delete();

        // Init startup and death sound
        int[] sound = MainActivity.getMachineHelper().getSound(machineID);
        int startupID = sound[0];
        int deathID = sound[1];
        TextView informationLabel = findViewById(R.id.information);
        if (startupID != 0 && deathID != 0) {
            // Startup sound exists, death sound exists
            informationLabel.setText(getResources().getString(R.string.information_specs_full));
            startupSound = MediaPlayer.create(this, startupID);
            deathSound = MediaPlayer.create(this, deathID);
            image.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View unused) {
                    if (!startupSound.isPlaying() && !deathSound.isPlaying()) {
                        if (startup) {
                            startupSound.start();
                            startup = false;
                        } else {
                            deathSound.start();
                            startup = true;
                        }
                    }
                }
            });
            image.setClickable(true);
            informationLabel.setVisibility(View.VISIBLE);
            Log.i("InitSound", "Startup and death sound loaded");
        } else if (startupID != 0) {
            // Startup sound exists, death sound not exist
            // Fix IllegalStateException
            deathSound = null;
            informationLabel.setText(getResources().getString(R.string.information_specs_no_death));
            startupSound = MediaPlayer.create(this, startupID);
            image.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View unused) {
                    startupSound.start();
                }
            });
            image.setClickable(true);
            informationLabel.setVisibility(View.VISIBLE);
            Log.i("InitSound", "Startup sound loaded");
        } else {
            // Exception for PowerBook DuoDock...
            // Fix IllegalStateException
            startupSound = null;
            deathSound = null;
            image.setOnClickListener(null);
            image.setClickable(false);
            informationLabel.setVisibility(View.GONE);
            Log.i("InitSound", "Startup and death sound do not exist");
        }
    }

    private void initLinks() {
        ImageView link = findViewById(R.id.everymac);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                loadLinks(MainActivity.getMachineHelper().getName(machineID), MainActivity.getMachineHelper().getConfig(machineID));
            }
        });
    }

    private void loadLinks(final String thisName, final String thisLinks) {
        try {
            if (thisLinks.equals("N")) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.link_not_available), Toast.LENGTH_LONG).show();
                return;
            }
            final String[] linkGroup = thisLinks.split(";");
            if (linkGroup.length == 1) {
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

    private void initButtons() {
        Log.i("SpecsInitButton", "Machine ID " + machineID
                + ", Previous ID " + (machineID - 1) + ", Next ID " + (machineID + 1));
        try {
            // Previous button.
            final Button previous = findViewById(R.id.buttonPrevious);
            if (machineID - 1 < 0) {
                previous.setVisibility(View.GONE);
            } else {
                previous.setVisibility(View.VISIBLE);
                previous.setText(getResources().getString(R.string.previous) + MainActivity.getMachineHelper().getName(machineID - 1));
                previous.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        machineID--;
                        refresh();
                    }
                });
            }

            // Next button.
            final Button next = findViewById(R.id.buttonNext);
            if (machineID + 1 >= MainActivity.getMachineHelper().getMachineCount()) {
                next.setVisibility(View.GONE);
            } else {
                next.setVisibility(View.VISIBLE);
                next.setText(getResources().getString(R.string.next) + MainActivity.getMachineHelper().getName(machineID + 1));
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        machineID++;
                        refresh();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    private void refresh() {
        release();
        initialize();
        View mainView = findViewById(R.id.mainView);
        mainView.invalidate();
    }
}
