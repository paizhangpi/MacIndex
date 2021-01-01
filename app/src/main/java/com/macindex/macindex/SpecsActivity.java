package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class SpecsActivity extends AppCompatActivity {

    private final MachineHelper thisMachineHelper = MainActivity.getMachineHelper();

    private int machineID = -1;

    private int[] categoryStartEnd = {};

    private int machineIDPosition = -1;

    private boolean startup = true;

    private MediaPlayer startupSound = null;

    private MediaPlayer deathSound = null;

    private ViewGroup mainView = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specs);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        try {
            final Intent intent = getIntent();
            machineID = intent.getIntExtra("machineID", -1);
            categoryStartEnd = intent.getIntArrayExtra("thisCategory");
            if (categoryStartEnd == null || machineID == -1) {
                throw new IllegalArgumentException();
            }
            // Find the current position.
            for (int i = 0; i < categoryStartEnd.length; i++) {
                if (categoryStartEnd[i] == machineID) {
                    machineIDPosition = i;
                    break;
                }
            }
            mainView = findViewById(R.id.mainView);
            LayoutTransition layoutTransition = mainView.getLayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            initialize();
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void initialize() {
        try {
            initSpecs();
            initImage();
            initLinks();
            if (PrefsHelper.getBooleanPrefs("isUseNavButtons", this) && categoryStartEnd.length > 1) {
                initButtons();
            }
            if (PrefsHelper.getBooleanPrefs("isUseGestures", this) && categoryStartEnd.length > 1) {
                initGestures();
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e,
                    "SpecsInitialize", "Failed, Machine ID " + machineID);
        }
        Log.i("SpecsInitialize", "Machine ID " + machineID);
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
            ExceptionHelper.handleException(this, e,
                    "SpecsActivity", "Unable to release sounds.");
        }
    }

    private void initSpecs() {
        try {
            final TextView name = findViewById(R.id.nameText);
            final TextView type = findViewById(R.id.typeText);
            final TextView processor = findViewById(R.id.processorText);
            final TextView maxram = findViewById(R.id.maxramText);
            final TextView year = findViewById(R.id.yearText);
            final TextView model = findViewById(R.id.modelText);
            final TextView id = findViewById(R.id.idText);
            final TextView graphics = findViewById(R.id.graphicsText);
            final TextView expansion = findViewById(R.id.expansionText);
            final TextView storage = findViewById(R.id.storageText);
            final TextView order = findViewById(R.id.orderText);
            final TextView gestalt = findViewById(R.id.gestaltText);
            final TextView emc = findViewById(R.id.emcText);
            final TextView software = findViewById(R.id.softwareText);
            final TextView design = findViewById(R.id.designText);
            final TextView support = findViewById(R.id.supportText);

            this.setTitle(thisMachineHelper.getName(machineID));
            name.setText(thisMachineHelper.getName(machineID));
            type.setText(thisMachineHelper.getType(machineID));
            processor.setText(thisMachineHelper.getProcessor(machineID));
            maxram.setText(thisMachineHelper.getMaxRam(machineID));
            year.setText(thisMachineHelper.getYear(machineID));
            model.setText(thisMachineHelper.getModel(machineID));
            id.setText(thisMachineHelper.getMid(machineID));
            graphics.setText(thisMachineHelper.getGraphics(machineID));
            expansion.setText(thisMachineHelper.getExpansion(machineID));
            storage.setText(thisMachineHelper.getStorage(machineID));
            order.setText(thisMachineHelper.getOrder(machineID));
            gestalt.setText(thisMachineHelper.getGestalt(machineID));
            emc.setText(thisMachineHelper.getEMC(machineID));
            software.setText(thisMachineHelper.getSoftware(machineID));
            design.setText(thisMachineHelper.getDesign(machineID));
            support.setText(thisMachineHelper.getSupport(machineID));

            // Set Support Box Text Color.
            if (support.getText().equals("Obsolete")) {
                support.setTextColor(Color.RED);
            } else if (support.getText().equals("Vintage")) {
                support.setTextColor(Color.YELLOW);
            } else if (support.getText().equals("Supported")) {
                support.setTextColor(Color.GREEN);
            }

            /*
                Processor Images dynaLoad.

                (1) Try getting type image. Will load if the type image is present.
                (2) Try getting specific image. Will load if specific image(s) is/are present.
                (3) No action. The case is not applicable for both loading process.
            */
            final LinearLayout processorTypeImageLayout = findViewById(R.id.processorTypeImageLayout);
            final ImageView processorTypeImage = findViewById(R.id.processorTypeImage);
            final LinearLayout processorImageLayoutContainer = findViewById(R.id.processorImageLayoutContainer);
            final LinearLayout processorImages = findViewById(R.id.processorImageLayout);
            final int[][] processorImageRes = thisMachineHelper.getProcessorImage(machineID);

            // Default states are all hidden.
            processorTypeImageLayout.setVisibility(View.GONE);
            processorImageLayoutContainer.setVisibility(View.GONE);

            final int processorTypeImageRes = thisMachineHelper.getProcessorTypeImage(machineID);
            if (processorTypeImageRes != 0) {
                // Got type image. Now loading.
                processorTypeImageLayout.setVisibility(View.VISIBLE);
                processorTypeImage.setImageResource(processorTypeImageRes);
            }
            if (processorImageRes[0][0] != 0) {
                // Got specific images. Now loading.
                processorImageLayoutContainer.setVisibility(View.VISIBLE);
                // Clear all existing children.
                processorImages.removeAllViews();
                for (int[] processorImageResGroup : processorImageRes) {
                    for (final int thisProcessorImageRes : processorImageResGroup) {
                        final View imageChunk = getLayoutInflater().inflate(R.layout.chunk_processor_image, null);
                        final ImageView thisProcessorImage = imageChunk.findViewById(R.id.processorImage);
                        thisProcessorImage.setImageResource(thisProcessorImageRes);
                        processorImages.addView(imageChunk);
                    }
                }
                // Remove the last space.
                ((LinearLayout) processorImages.getChildAt(processorImages.getChildCount() - 1)).removeViewAt(1);
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e,
                    "initSpecs", "Failed, Machine ID " + machineID);
        }
    }

    private void initImage() {
        try {
            // Init image
            final ImageView image = findViewById(R.id.pic);
            final File imageFile = thisMachineHelper.getPicture(machineID);
            if (imageFile.exists()) {
                Log.i("SpecsAct", "Image exists");
                image.setImageBitmap(BitmapFactory.decodeFile(imageFile.getPath()));
            }
            imageFile.delete();

            // Init startup and death sound
            final int[] sound = thisMachineHelper.getSound(machineID);
            final int startupID = sound[0];
            final int deathID = sound[1];
            final TextView informationLabel = findViewById(R.id.information);

            if (startupID != 0 || deathID != 0) {
                // Set Sound accordingly
                if (startupID != 0 && deathID != 0
                        && PrefsHelper.getBooleanPrefs("isPlayDeathSound", this)) {
                    // Startup sound exists, death sound exists, and user prefers both
                    informationLabel.setText(getResources().getString(R.string.information_specs_full));
                    startupSound = MediaPlayer.create(this, startupID);
                    deathSound = MediaPlayer.create(this, deathID);
                    Log.i("InitSound", "Startup and death sound loaded");
                } else {
                    // Startup sound exists, death sound not exist
                    // Fix IllegalStateException
                    informationLabel.setText(getResources().getString(R.string.information_specs_no_death));
                    startupSound = MediaPlayer.create(this, startupID);
                    deathSound = null;
                    Log.i("InitSound", "Startup sound loaded");
                }
                informationLabel.setVisibility(View.VISIBLE);
                // Should set a listener
                image.setOnClickListener(unused -> {
                    if (!startupSound.isPlaying() && (deathSound == null || !deathSound.isPlaying())) {
                        // Not playing any sound
                        if (PrefsHelper.getBooleanPrefs("isEnableVolWarningThisTime", this)
                                && PrefsHelper.getBooleanPrefs("isEnableVolWarning", this)) {
                            // High Volume Warning Enabled
                            boolean currentOutputDevice = false;
                            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                            if (audioManager != null) {
                                for (AudioDeviceInfo deviceInfo : audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
                                    final int thisType = deviceInfo.getType();
                                    Log.i("VolWarning", "Get type " + thisType);
                                    if (thisType == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                                            || thisType == AudioDeviceInfo.TYPE_WIRED_HEADSET
                                            || thisType == AudioDeviceInfo.TYPE_USB_HEADSET
                                            || thisType == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                                            || thisType == AudioDeviceInfo.TYPE_HEARING_AID) {
                                        Log.i("VolWarning", "Earphone detected");
                                        currentOutputDevice = true;
                                        break;
                                    }
                                }
                                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                int currentVolumePercentage = 100 * currentVolume / maxVolume;
                                Log.i("VolWarning", "Enabled, current percentage " + currentVolumePercentage
                                        + " current output device " + currentOutputDevice);
                                if (currentVolumePercentage >= 60 && currentOutputDevice) {
                                    Log.i("VolWarning", "Armed");
                                    final AlertDialog.Builder volWarningDialog = new AlertDialog.Builder(SpecsActivity.this);
                                    volWarningDialog.setMessage(R.string.information_specs_high_vol_warning);
                                    volWarningDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                                        // Enabled, and popup a warning
                                        PrefsHelper.editPrefs("isEnableVolWarningThisTime", false, this);
                                        playSound();
                                    });
                                    volWarningDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
                                        // Do nothing
                                    });
                                    volWarningDialog.show();
                                } else {
                                    // Enabled, but should not popup a warning
                                    Log.i("VolWarning", "Unarmed");
                                    playSound();
                                }
                            } else {
                                // Enabled, but audio service not available
                                ExceptionHelper.handleException(this, null,
                                        "VolWarning",
                                        "Audio Service Not Available.");
                                playSound();
                            }
                        } else {
                            // High Volume Warning Disabled
                            Log.i("VolWarning", "Disabled");
                            playSound();
                        }
                    }
                });
                image.setClickable(true);
            } else {
                // Exception for PowerBook DuoDock...
                // Fix IllegalStateException
                startupSound = null;
                deathSound = null;
                Log.i("InitSound", "Startup and death sound do not exist");
                image.setOnClickListener(null);
                image.setClickable(false);
                informationLabel.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e,
                    "initImage", "Failed, Machine ID " + machineID);
        }
    }

    private void playSound() {
        try {
            if (startupSound != null) {
                // NullSafe
                if (deathSound != null) {
                    if (startup) {
                        startupSound.start();
                        startup = false;
                    } else {
                        deathSound.start();
                        startup = true;
                    }
                } else {
                    startupSound.start();
                }
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e,
                    "playSound", "Unable to play sound.");
        }
    }

    private void initLinks() {
        final ImageView link = findViewById(R.id.everymac);
        link.setOnClickListener(v -> LinkLoadingHelper.loadLinks(thisMachineHelper.getName(machineID),
                thisMachineHelper.getConfig(machineID), SpecsActivity.this));
    }

    private void initButtons() {
        try {
            Log.i("SpecNavButtons", "Loading");
            // Reset the padding
            final LinearLayout basicInfoLayout = findViewById(R.id.basicInfoLayout);
            final float density = getResources().getDisplayMetrics().density;
            basicInfoLayout.setPadding((int) (10 * density), (int) (10 * density), (int) (10 * density), 0);

            final View buttonView = findViewById(R.id.buttonView);
            final Button previous = findViewById(R.id.buttonPrevious);
            final Button next = findViewById(R.id.buttonNext);

            // Reset the listener
            previous.setOnClickListener(null);
            next.setOnClickListener(null);

            // GONE by default, let it show up
            buttonView.setVisibility(View.VISIBLE);

            // Previous button.
            if (machineIDPosition == 0) {
                // First one, disable the prev button
                previous.setEnabled(false);
                previous.setText(getResources().getString(R.string.first_one));
            } else {
                previous.setEnabled(true);
                previous.setText(MainActivity.getMachineHelper().getName(categoryStartEnd[machineIDPosition - 1]));
                previous.setOnClickListener(v -> {
                    previous.setEnabled(false);
                    navPrev();
                });
            }

            // Next button.
            if (machineIDPosition == categoryStartEnd.length - 1) {
                // Last one, disable the next button
                next.setEnabled(false);
                next.setText(getResources().getString(R.string.last_one));
            } else {
                next.setEnabled(true);
                next.setText(MainActivity.getMachineHelper().getName(categoryStartEnd[machineIDPosition + 1]));
                next.setOnClickListener(v -> {
                    next.setEnabled(false);
                    navNext();
                });
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e,
                    "SpecsActivity", "Unable to init buttons.");
        }
    }

    private void initGestures() {
        try {
            Log.i("SpecGestures", "Loading");
            final OnSwipeTouchListener listenerNotAvailable = new OnSwipeTouchListener(SpecsActivity.this) {
                public void onSwipeRight() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.last_one), Toast.LENGTH_LONG).show();
                }

                public void onSwipeLeft() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.first_one), Toast.LENGTH_LONG).show();
                }
            };
            final OnSwipeTouchListener listenerOnlyNext = new OnSwipeTouchListener(SpecsActivity.this) {
                public void onSwipeRight() {
                    releaseGestures();
                    navNext();
                }

                public void onSwipeLeft() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.first_one), Toast.LENGTH_LONG).show();
                }
            };
            final OnSwipeTouchListener listenerOnlyPrev = new OnSwipeTouchListener(SpecsActivity.this) {
                public void onSwipeRight() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.last_one), Toast.LENGTH_LONG).show();
                }

                public void onSwipeLeft() {
                    releaseGestures();
                    navPrev();
                }
            };
            final OnSwipeTouchListener listenerCanBoth = new OnSwipeTouchListener(SpecsActivity.this) {
                public void onSwipeRight() {
                    releaseGestures();
                    navNext();
                }

                public void onSwipeLeft() {
                    releaseGestures();
                    navPrev();
                }
            };

            if (machineIDPosition == 0 && machineIDPosition == categoryStartEnd.length - 1) {
                // Can NOT do BOTH
                setListenerForView(listenerNotAvailable);
            } else if (machineIDPosition == 0) {
                // Can only swipe Right (NEXT)
                setListenerForView(listenerOnlyNext);
            } else if (machineIDPosition == categoryStartEnd.length - 1) {
                // Can only swipe Left (PREV)
                setListenerForView(listenerOnlyPrev);
            } else {
                // Can do BOTH
                setListenerForView(listenerCanBoth);
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e,
                    "SpecsActivity", "Unable to init gestures.");
        }
    }

    private void releaseGestures() {
        setListenerForView(null);
        Log.i("SpecGestures", "Released");
    }

    private void setListenerForView(final OnSwipeTouchListener listenerForView) {
        for (int i = 0; i < mainView.getChildCount(); i++) {
            mainView.getChildAt(i).setOnTouchListener(listenerForView);
        }
    }

    private void navPrev() {
        machineIDPosition--;
        refresh();
    }

    private void navNext() {
        machineIDPosition++;
        refresh();
    }

    private void refresh() {
        machineID = categoryStartEnd[machineIDPosition];
        if (PrefsHelper.getBooleanPrefs("isQuickNav", this)) {
            // Old method - not creating a new Activity
            release();
            startup = true;
            initialize();
        } else {
            // New method
            final Intent newMachine = new Intent(SpecsActivity.this, SpecsActivity.class);
            newMachine.putExtra("machineID", machineID);
            newMachine.putExtra("thisCategory", categoryStartEnd);
            startActivity(newMachine);
            finish();
        }
    }
}
