package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class SpecsActivity extends AppCompatActivity {

    private int machineID = -1;

    private int[] categoryStartEnd = {};

    private int machineIDPosition = -1;

    private boolean startup = true;

    private MediaPlayer startupSound = null;

    private MediaPlayer deathSound = null;

    private Vibrator vibrator = null;

    private String[] allComments = null;

    private int commentID = -1;

    private String thisName = null;

    private String thisType = null;

    private String thisProcessor = null;

    private String thisMaxram = null;

    private String thisYear = null;

    private String thisModel = null;

    private String thisId = null;

    private String thisGraphics = null;

    private String thisExpansion = null;

    private String thisStorage = null;

    private String thisOrder = null;

    private String thisGestalt = null;

    private String thisEmc = null;

    private String thisSoftware = null;

    private String thisDesign = null;

    private String thisSupport = null;

    private String thisComment = null;

    private MenuItem compareItem = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specs);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MainActivity.validateOperation(this);

        try {
            final Intent intent = getIntent();
            categoryStartEnd = intent.getIntArrayExtra("thisCategory");
            machineID = intent.getIntExtra("machineID", -1);

            if (categoryStartEnd == null || machineID == -1) {
                throw new IllegalArgumentException();
            }

            // Is position already inherited?
            if (intent.getBooleanExtra("machineIDPositionInherit", false)) {
                machineIDPosition = intent.getIntExtra("machineIDPosition", -1);
            } else {
                // Find the current position.
                for (int i = 0; i < categoryStartEnd.length; i++) {
                    if (categoryStartEnd[i] == machineID) {
                        machineIDPosition = i;
                        break;
                    }
                }
            }

            if (machineIDPosition == -1) {
                throw new IllegalArgumentException();
            }

            ViewGroup mainView = findViewById(R.id.mainView);
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            LayoutTransition layoutTransition = mainView.getLayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            initialize();

            // Is randomized into a favourite machine?
            if (intent.getBooleanExtra("isRandom", false)
                    && FavouriteActivity.isFavourite(thisName, this)) {
                Log.i("Random", "User wins.");
                final AlertDialog.Builder congratsDialog = new AlertDialog.Builder(SpecsActivity.this);
                congratsDialog.setTitle(R.string.random_lucky_title);
                congratsDialog.setMessage(R.string.ramdom_lucky_message);
                congratsDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                    // Do nothing..
                });
                congratsDialog.show();
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, null, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_specs, menu);
        compareItem = menu.findItem(R.id.addCompareItem);
        initCompareCheckBox();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shareItem:
                shareDialog();
                break;
            case R.id.addFavouriteItem:
                selectFolder();
                break;
            case R.id.addCompareItem:
                addToCompare();
                break;
            case R.id.commentItem:
                initCommentDialog();
                break;
            case R.id.specsHelpItem:
                LinkLoadingHelper.startBrowser(null, "https://macindex.paizhang.info/specs-activity", this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        release();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        // Restart Sound System.
        super.onRestart();
        initImage();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void initialize() {
        Log.i("SpecsInitialize", "Machine ID " + machineID);
        if (PrefsHelper.getBooleanPrefs("isUseNavButtons", this) && categoryStartEnd.length > 1) {
            initButtons();
        }
        getSpecs();
        initSpecs();
        initImage();
        initLinks();
        initComment();
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
            Log.w("SpecsActivity", "Unable to release all sounds.");
        }
    }

    private void getSpecs() {
        thisName = MainActivity.getMachineHelper().getName(machineID);
        thisType = MainActivity.getMachineHelper().getType(machineID);
        thisProcessor = MainActivity.getMachineHelper().getProcessor(machineID);
        thisMaxram = MainActivity.getMachineHelper().getMaxRam(machineID);
        thisYear = MainActivity.getMachineHelper().getYear(machineID);
        thisModel = MainActivity.getMachineHelper().getModel(machineID);
        thisId = MainActivity.getMachineHelper().getMid(machineID);
        thisGraphics = MainActivity.getMachineHelper().getGraphics(machineID);
        thisExpansion = MainActivity.getMachineHelper().getExpansion(machineID);
        thisStorage = MainActivity.getMachineHelper().getStorage(machineID);
        thisOrder = MainActivity.getMachineHelper().getOrder(machineID);
        thisGestalt = MainActivity.getMachineHelper().getGestalt(machineID);
        thisEmc = MainActivity.getMachineHelper().getEMC(machineID);
        thisSoftware = MainActivity.getMachineHelper().getSoftware(machineID);
        thisDesign = MainActivity.getMachineHelper().getDesign(machineID);
        thisSupport = MainActivity.getMachineHelper().getSupport(machineID);
    }

    private void initSpecs() {
        try {
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

            this.setTitle(thisName);
            reloadName();

            type.setText(thisType);
            /* return true - do not attach another click event */
            type.setOnLongClickListener(view -> {
                if (thisType.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("typeInfo", thisType);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
            processor.setText(thisProcessor);
            processor.setOnLongClickListener(view -> {
                if (thisProcessor.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("processorInfo", thisProcessor);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
            maxram.setText(thisMaxram);
            maxram.setOnLongClickListener(view -> {
                if (thisMaxram.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("maxramInfo", thisMaxram);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
            year.setText(thisYear);
            year.setOnLongClickListener(view -> {
                if (thisYear.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("yearInfo", thisYear);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
            model.setText(thisModel);
            model.setOnLongClickListener(view -> {
                if (thisModel.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("modelInfo", thisModel);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
            id.setText(thisId);
            id.setOnLongClickListener(view -> {
                if (thisId.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("idInfo", thisId);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
            graphics.setText(thisGraphics);
            graphics.setOnLongClickListener(view -> {
                if (thisGraphics.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("graphicsInfo", thisGraphics);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
            expansion.setText(thisExpansion);
            expansion.setOnLongClickListener(view -> {
                if (thisExpansion.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("expansionInfo", thisExpansion);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
            storage.setText(thisStorage);
            storage.setOnLongClickListener(view -> {
                if (thisStorage.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("storageInfo", thisStorage);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
            order.setText(thisOrder);
            order.setOnLongClickListener(view -> {
                if (thisOrder.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("orderInfo", thisOrder);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
            gestalt.setText(thisGestalt);
            gestalt.setOnLongClickListener(view -> {
                if (thisGestalt.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("gestaltInfo", thisGestalt);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
            emc.setText(thisEmc);
            emc.setOnLongClickListener(view -> {
                if (thisEmc.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("emcInfo", thisEmc);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
            software.setText(thisSoftware);
            software.setOnLongClickListener(view -> {
                if (thisSoftware.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("softwareInfo", thisSoftware);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
            design.setText(thisDesign);
            design.setOnLongClickListener(view -> {
                if (thisDesign.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("designInfo", thisDesign);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
            support.setText(thisSupport);
            support.setOnLongClickListener(view -> {
                if (thisSupport.equals(getString(R.string.not_applicable))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("supportInfo", thisSupport);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });

            // Set Support Box Text Color.
            if (thisSupport.equals("Obsolete")) {
                support.setTextColor(Color.RED);
            } else if (thisSupport.equals("Vintage")) {
                support.setTextColor(Color.MAGENTA);
            } else if (thisSupport.equals("Supported")) {
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
            final int[][] processorImageRes = MainActivity.getMachineHelper().getProcessorImage(machineID, SpecsActivity.this);

            // Default states are all hidden.
            processorTypeImageLayout.setVisibility(View.GONE);
            processorImageLayoutContainer.setVisibility(View.GONE);

            final int processorTypeImageRes = MainActivity.getMachineHelper().getProcessorTypeImage(machineID, SpecsActivity.this);
            if (processorTypeImageRes != 0) {
                // Got type image. Now loading.
                processorTypeImageLayout.setVisibility(View.VISIBLE);
                processorTypeImage.setImageBitmap(BitmapLoadingHelper.decodeSampledBitmapFromResource(getResources(), processorTypeImageRes, 200, 200));
            }
            if (processorImageRes[0][0] != 0) {
                // Got specific images. Now loading.
                processorImageLayoutContainer.setVisibility(View.VISIBLE);
                // Clear all existing children.
                processorImages.removeAllViews();
                for (int[] processorImageResGroup : processorImageRes) {
                    for (final int thisProcessorImageRes : processorImageResGroup) {
                        @SuppressLint("InflateParams")
                        final View imageChunk = getLayoutInflater().inflate(R.layout.chunk_processor_image, null);
                        final ImageView thisProcessorImage = imageChunk.findViewById(R.id.processorImage);
                        thisProcessorImage.setImageBitmap(BitmapLoadingHelper.decodeSampledBitmapFromResource(getResources(), thisProcessorImageRes, 200, 200));
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

    private void reloadName() {
        final TextView name = findViewById(R.id.nameText);
        name.setVisibility(View.INVISIBLE);

        // Reset the auto-sizing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            name.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);
        } else {
            TextViewCompat.setAutoSizeTextTypeWithDefaults(name, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
        }

        // Reset the Machine Name.
        name.setText(thisName);
        name.setTextSize(20);

        // Check if the star is needed.
        if (FavouriteActivity.isFavourite(thisName, this)) {
            name.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_star_24, 0);
        } else {
            name.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        }

        // Auto-sizing only if the width is insufficient.
        name.post(() -> {
            if (!name.getLayout().getText().toString().equals(thisName)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    name.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                } else {
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(name, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                }
            }
            name.setVisibility(View.VISIBLE);
        });

        // Set copy
        name.setOnLongClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("nameInfo", thisName);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(SpecsActivity.this,
                    getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
            return true;
        });
    }

    private void initImage() {
        try {
            // Init image
            final ImageView image = findViewById(R.id.pic);
            final File imageFile = MainActivity.getMachineHelper().getPicture(machineID, SpecsActivity.this);
            if (imageFile.exists()) {
                Log.i("SpecsAct", "Image exists");
                image.setImageBitmap(BitmapFactory.decodeFile(imageFile.getPath()));
            }
            imageFile.delete();

            // Init startup and death sound
            final int[] sound = MainActivity.getMachineHelper().getSound(machineID, SpecsActivity.this);
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
                // Should set a listener
                image.setOnClickListener(unused -> {
                    // Initialize Sound.
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(50);
                        }
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
                    } catch (Exception e) {
                        ExceptionHelper.handleException(this, e,
                                "initImage", "Unable to initialize sounds.");
                    }
                });
            } else {
                // Exception for PowerBook DuoDock...
                // Fix IllegalStateException
                startupSound = null;
                deathSound = null;
                Log.i("InitSound", "Startup and death sound do not exist");
                image.setOnClickListener(v -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(50);
                    }
                    Toast.makeText(SpecsActivity.this, R.string.information_specs_no_sound,
                            Toast.LENGTH_SHORT).show();
                });
                informationLabel.setText(R.string.information_specs_no_sound);
            }

            // Set a long click listener
            image.setOnLongClickListener(v -> {
                Intent viewImageIntent = new Intent(SpecsActivity.this, ViewImageActivity.class);
                viewImageIntent.putExtra("machineID", machineID);
                startActivity(viewImageIntent);
                return true;
            });
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e,
                    "initSound", "Failed, Machine Name " + thisName);
        }
    }

    private void playSound() {
        try {
            if (startupSound == null) {
                throw new IllegalStateException();
            }
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
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e,
                    "playSound", "Unable to play sound.");
        }
    }

    private void initLinks() {
        final ImageView link = findViewById(R.id.everymac);
        link.setOnClickListener(v -> LinkLoadingHelper.loadLinks(thisName,
                MainActivity.getMachineHelper().getConfig(machineID), SpecsActivity.this));
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
    /* Gestures were removed since Ver. 4.5b3 */

    /* Comments Functions */
    private void initComment() {
        try {
            Log.i("initComment", PrefsHelper.getStringPrefs("userComments", this));
            allComments = PrefsHelper.getStringPrefs("userComments", this).split("││");
            if (allComments.length == 0) {
                commentID = -1;
            }
            for (int i = 0; i < allComments.length; i++) {
                if (allComments[i].split("│")[0].equals(thisName)) {
                    commentID = i;
                    break;
                }
                if (i + 1 == allComments.length) {
                    commentID = -1;
                }
            }
            final TextView comment = findViewById(R.id.commentText);
            if (commentID != -1) {
                thisComment = allComments[commentID].split("│")[1];

            } else {
                thisComment = getString(R.string.comment_null);
            }
            comment.setText(thisComment);
            comment.setOnClickListener(view -> {
                initCommentDialog();
            });
            comment.setOnLongClickListener(view -> {
                if (thisComment.equals(getString(R.string.comment_null))) {
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_not_available), Toast.LENGTH_LONG).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("userComment", thisComment);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this,
                            getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
                }
                return true;
            });
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "initComment", "Illegal comment prefs string. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userComments", this));
        }
    }

    private void initCommentDialog() {
        final View commentChunk = getLayoutInflater().inflate(R.layout.chunk_edit_comment, null);
        final EditText editComment = commentChunk.findViewById(R.id.editComment);
        if (commentID != -1) {
            editComment.setText(allComments[commentID].split("│")[1]);
        }

        final AlertDialog.Builder commentDialog = new AlertDialog.Builder(this);
        commentDialog.setTitle(R.string.submenu_specs_comment);
        commentDialog.setMessage(R.string.comment_tips);
        commentDialog.setView(commentChunk);
        commentDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
            // To be overwritten...
        });
        commentDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
            // Do nothing
        });

        final AlertDialog commentDialogCreated = commentDialog.create();
        commentDialogCreated.show();
        // Overwrite the positive button
        commentDialogCreated.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            try {
                final String inputtedString = editComment.getText().toString().trim();
                // Is "│" included?
                if (inputtedString.contains("│")) {
                    Log.w("commentDialog", "Illegal Character Detected.");
                    Toast.makeText(this, R.string.comment_illegal, Toast.LENGTH_LONG).show();
                } else if (inputtedString.length() > 500) {
                    Log.w("commentDialog", "Input is too long.");
                    Toast.makeText(this, R.string.comment_length, Toast.LENGTH_LONG).show();
                } else {
                    String originalString = PrefsHelper.getStringPrefs("userComments", this);
                    String realOriginalString = PrefsHelper.getStringPrefs("userComments", this);
                    // Is available before?
                    if (commentID != -1) {
                        // Is input legal?
                        if (!inputtedString.isEmpty()) {
                            String[] toConcat = originalString.split(Pattern.quote(thisName + "│" + allComments[commentID].split("│")[1]), -1);
                            if (toConcat.length != 2) {
                                Log.e("commentDialog", "Error length is " + toConcat.length);
                                throw new IllegalStateException();
                            }
                            originalString = toConcat[0] + thisName + "│" + inputtedString + toConcat[1];
                        } else {
                            // Is this one is the first machine?
                            if (commentID == 0) {
                                // Is this one is the only machine?
                                if (allComments.length == 1) {
                                    originalString = "";
                                } else {
                                    String[] toConcat = originalString.split(Pattern.quote(thisName + "│" + allComments[commentID].split("│")[1] + "││"), -1);
                                    if (toConcat.length != 2) {
                                        Log.e("commentDialog", "Error length is " + toConcat.length);
                                        throw new IllegalStateException();
                                    }
                                    originalString = toConcat[1];
                                }
                            } else {
                                String[] toConcat = originalString.split(Pattern.quote("││" + thisName + "│" + allComments[commentID].split("│")[1]), -1);
                                if (toConcat.length != 2) {
                                    Log.e("commentDialog", "Error length is " + toConcat.length);
                                    throw new IllegalStateException();
                                }
                                originalString = toConcat[0] + toConcat[1];
                            }
                        }
                    } else {
                        // Is input legal?
                        if (!inputtedString.isEmpty()) {
                            // Is original string not empty?
                            if (originalString.length() != 0) {
                                originalString = thisName + "│" + inputtedString + "││" + originalString;
                            } else {
                                originalString = originalString.concat(thisName + "│" + inputtedString);
                            }
                        }
                    }
                    PrefsHelper.editPrefs("userComments", originalString, this);
                    if (!originalString.equals(realOriginalString)) {
                        // Changed string, reload needed
                        PrefsHelper.editPrefs("isCommentsReloadNeeded", true, this);
                    }
                    initComment();
                    commentDialogCreated.dismiss();
                }
            } catch (Exception e) {
                ExceptionHelper.handleException(this, e, "commentDialog", "Unable to set positive button. Likely illegal comment prefs string. Please reset the application. String is: "
                        + PrefsHelper.getStringPrefs("userComments", this));
            }
        });
    }

    /* Favourites Functions */
    // Call this when trying to add to favourites.
    private void selectFolder() {
        try {
            // Check if totally empty.
            if (!isEmptyString()) {
                final View selectChunk = this.getLayoutInflater().inflate(R.layout.chunk_favourites_select, null);
                final LinearLayout selectLayout = selectChunk.findViewById(R.id.selectLayout);
                final String[] splitedString = PrefsHelper.getStringPrefs("userFavourites", this).split("││");
                final String[] allFolders = FavouriteActivity.getFolders(this, true);
                final int[] currentSelections = new int[splitedString.length];
                for (int i = 1; i < splitedString.length; i++) {
                    // Is it in this folder?
                    final String[] splitedFolderContent = splitedString[i].split("│");
                    boolean isExistsAtHere = false;
                    for (int j = 1; j < splitedFolderContent.length; j++) {
                        if (splitedFolderContent[j].equals("[" + thisName + "]")) {
                            isExistsAtHere = true;
                            break;
                        }
                    }

                    // Set the checkbox
                    CheckBox thisCheckBox = new CheckBox(this);
                    thisCheckBox.setText(allFolders[i - 1]);
                    thisCheckBox.setChecked(isExistsAtHere);
                    // Fix the init bug
                    currentSelections[i] = isExistsAtHere ? 1 : 0;
                    int finalI = i;
                    thisCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
                        currentSelections[finalI] = thisCheckBox.isChecked() ? 1 : 0;
                    });
                    selectLayout.addView(thisCheckBox);
                }

                // Create the dialog.
                final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
                deleteDialog.setTitle(R.string.submenu_specs_favourite);
                deleteDialog.setMessage(R.string.favourites_tips);
                deleteDialog.setView(selectChunk);
                deleteDialog.setPositiveButton(R.string.link_confirm, (dialog, which) -> {
                    try {
                        String newString = "";
                        for (int j = 1; j < splitedString.length; j++) {
                            // Is it in this folder?
                            final String[] splitedFolderContent = splitedString[j].split("│");
                            boolean isExistsAtHere = false;
                            for (int i = 1; i < splitedFolderContent.length; i++) {
                                if (splitedFolderContent[i].equals("[" + thisName + "]")) {
                                    isExistsAtHere = true;
                                    break;
                                }
                            }

                            // Add or remove
                            if (currentSelections[j] == 0) {
                                Log.w("Selection", String.valueOf(currentSelections[j]));
                                // Is exists at here?
                                if (isExistsAtHere) {
                                    Log.w("selectFolder", "Exists, removing.");
                                    String[] splitedAgain = splitedString[j].split(Pattern.quote("│[" + thisName + "]"), -1);
                                    if (splitedAgain.length != 2) {
                                        Log.e("selectFolder", "Error length is " + splitedAgain.length);
                                        throw new IllegalStateException();
                                    }
                                    if (!splitedAgain[1].isEmpty()) {
                                        // Have something in the trailing.
                                        splitedAgain[0] = splitedAgain[0].concat(splitedAgain[1]);
                                    }
                                    newString = newString.concat("││" + splitedAgain[0]);
                                } else {
                                    Log.w("selectFolder", "Does not exist, keeping.");
                                    newString = newString.concat("││" + splitedString[j]);
                                }
                            } else {
                                Log.w("Selection", String.valueOf(currentSelections[j]));
                                // Is exists at here?
                                if (isExistsAtHere) {
                                    Log.w("selectFolder", "Exists, keeping.");
                                    newString = newString.concat("││" + splitedString[j]);
                                } else {
                                    Log.w("selectFolder", "Does not exist, adding.");
                                    String folderName = splitedFolderContent[0];
                                    String[] splitedAgain = splitedString[j].split(Pattern.quote(folderName), -1);
                                    if (splitedAgain.length != 2) {
                                        Log.e("selectFolder", "Error length is " + splitedAgain.length);
                                        throw new IllegalStateException();
                                    }
                                    if (splitedAgain[1].isEmpty()) {
                                        // Empty folder.
                                        splitedAgain[0] = "[" + thisName + "]";
                                    } else {
                                        // Not Empty folder.
                                        splitedAgain[0] = "[" + thisName + "]│" + splitedAgain[1].substring(1);
                                    }
                                    newString = newString.concat("││" + folderName + "│" + splitedAgain[0]);
                                }
                            }
                        }
                        PrefsHelper.editPrefs("userFavourites", newString, this);
                        PrefsHelper.editPrefs("isFavouritesReloadNeeded", true, this);
                        reloadName();
                    } catch (Exception e) {
                        ExceptionHelper.handleException(this, e, "selectFolder", "Illegal Favourites String. Please reset the application. String is: "
                                + PrefsHelper.getStringPrefs("userFavourites", this));
                    }
                });
                deleteDialog.setNegativeButton(R.string.link_cancel, ((dialog, which) -> {
                    // Cancelled, do nothing
                }));
                deleteDialog.show();
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "deleteFolder", "Illegal Favourites String. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userFavourites", this));
        }
    }

    // Modified from the original one from the FavouriteActivity
    private boolean isEmptyString() {
        if (PrefsHelper.getStringPrefs("userFavourites", this).isEmpty()) {
            final AlertDialog.Builder emptyStringDialog = new AlertDialog.Builder(this);
            emptyStringDialog.setTitle(R.string.submenu_specs_favourite);
            emptyStringDialog.setMessage(R.string.favourites_no_folder);
            emptyStringDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
                // Create new folder
                createFolder();
            });
            emptyStringDialog.setNegativeButton(R.string.link_cancel, ((dialogInterface, i) -> {
                // Cancelled, do nothing
            }));
            emptyStringDialog.show();
            return true;
        } else {
            return false;
        }
    }

    // Modified from the original one from the FavouriteActivity
    private void createFolder() {
        final View newFolderChunk = getLayoutInflater().inflate(R.layout.chunk_favourites_new, null);
        final EditText folderName = newFolderChunk.findViewById(R.id.folderName);
        final AlertDialog.Builder newFolderDialog = new AlertDialog.Builder(this);
        newFolderDialog.setTitle(R.string.submenu_favourite_add);
        newFolderDialog.setMessage(R.string.favourites_new_folder);
        newFolderDialog.setView(newFolderChunk);
        newFolderDialog.setPositiveButton(R.string.link_confirm, (dialogInterface, i) -> {
            // To be overwritten...
        });
        newFolderDialog.setNegativeButton(R.string.link_cancel, (dialogInterface, i) -> {
            // Do nothing
        });

        final AlertDialog newFolderDialogCreated = newFolderDialog.create();
        newFolderDialogCreated.show();
        // Overwrite the positive button
        newFolderDialogCreated.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            try {
                final String inputtedName = folderName.getText().toString().trim();
                // Check if the input is legal
                if (FavouriteActivity.validateFolderName(inputtedName, new String[0], this)) {
                    // Finally create the new folder.
                    PrefsHelper.editPrefs("userFavourites", "││{"
                            + inputtedName + "}" + PrefsHelper.getStringPrefs("userFavourites", this), this);
                    newFolderDialogCreated.dismiss();
                    selectFolder();
                }
            } catch (Exception e) {
                ExceptionHelper.handleException(this, e, "newFolderDialog", "Illegal Favourites String. Please reset the application. String is: "
                        + PrefsHelper.getStringPrefs("userFavourites", this));
            }
        });
    }

    /* Compare Functions */
    private void addToCompare() {
        try {
            final String originalCompare = PrefsHelper.getStringPrefs("userCompares", this);
            final String[] splitedCompare = originalCompare.split("│");
            Log.e("stringis", originalCompare);
            if (splitedCompare.length == 1 && splitedCompare[0].isEmpty()) {
                PrefsHelper.editPrefs("userCompares", "[" + thisName + "]", this);
            } else if (splitedCompare.length >= 1 && splitedCompare.length <= 10) {
                if (originalCompare.contains("[" + thisName + "]")) {
                    if (splitedCompare.length == 1) {
                        PrefsHelper.clearPrefs("userCompares", this);
                    } else {
                        PrefsHelper.editPrefs("userCompares", originalCompare.replace("│[" + thisName + "]", ""), this);
                    }
                } else {
                    PrefsHelper.editPrefs("userCompares", originalCompare.concat("│[" + thisName + "]"), this);
                }
            } else {
                Log.e("initCompare", "Error length is " + splitedCompare.length);
                throw new IllegalStateException();
            }
            initCompareCheckBox();
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "addToCompare", "Illegal Compare String. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userCompares", this));
        }
    }

    private void initCompareCheckBox() {
        try {
            final String originalCompare = PrefsHelper.getStringPrefs("userCompares", this);
            final String[] splitedCompare = originalCompare.split("│");
            Log.e("stringis", originalCompare);
            if (splitedCompare.length == 1 && splitedCompare[0].isEmpty()) {
                compareItem.setChecked(false);
                compareItem.setEnabled(true);
                compareItem.setTitle(getString(R.string.submenu_specs_compare) + " (0)");
            } else if (splitedCompare.length >= 1 && splitedCompare.length < 10) {
                if (originalCompare.contains("[" + thisName + "]")) {
                    compareItem.setChecked(true);
                } else {
                    compareItem.setChecked(false);
                }
                compareItem.setEnabled(true);
                compareItem.setTitle(getString(R.string.submenu_specs_compare) + " (" + splitedCompare.length + ")");
            } else if (splitedCompare.length == 10) {
                if (originalCompare.contains("[" + thisName + "]")) {
                    compareItem.setChecked(true);
                    compareItem.setEnabled(true);
                } else {
                    compareItem.setChecked(false);
                    compareItem.setEnabled(false);
                }
                compareItem.setTitle(getString(R.string.submenu_specs_compare) + " " + getString(R.string.compare_limit));
            } else {
                Log.e("initCompare", "Error length is " + splitedCompare.length);
                throw new IllegalStateException();
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "initCompareCheckBox", "Illegal Compare String. Please reset the application. String is: "
                    + PrefsHelper.getStringPrefs("userCompares", this));
        }
    }

    private void shareDialog() {
        // 2021.11.13 at Jinzhong, Shanxi, China
        AlertDialog.Builder shareDialog = new AlertDialog.Builder(this);
        shareDialog.setTitle(getString(R.string.submenu_specs_share));
        final String[] shareEntries = getResources().getStringArray(R.array.share_menu);
        final String[] shareDescription = getResources().getStringArray(R.array.share_description);
        shareDialog.setItems(shareEntries, (dialog, which) -> {
            try {
                if (which == 0 || which == 1) {
                    // Model no. or all info
                    List<Integer> currentEntries = new ArrayList<>(5);
                    for (int i = 1; i < (which == 0 ? 6 : 16); i++) {
                        currentEntries.add(i);
                    }
                    final String modelInfo = generateShareInfo(currentEntries);
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("MacIndexModelInfo", modelInfo);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this, shareDescription[which], Toast.LENGTH_LONG).show();
                } else if (which == 2) {
                    // User choose info
                    // Construct the dialog view.
                    final View selectChunk = this.getLayoutInflater().inflate(R.layout.chunk_favourites_select, null);
                    final LinearLayout selectLayout = selectChunk.findViewById(R.id.selectLayout);
                    final String[] selectableSpecs = new String[]{
                            getString(R.string.year), getString(R.string.model), getString(R.string.id), getString(R.string.gestalt),
                            getString(R.string.order), getString(R.string.emc), getString(R.string.processor), getString(R.string.graphics),
                            getString(R.string.type), getString(R.string.maxram), getString(R.string.software), getString(R.string.storage),
                            getString(R.string.bus_expansion), getString(R.string.design), getString(R.string.support), getString(R.string.comment)};
                    final boolean[] currentSelections = new boolean[selectableSpecs.length];
                    for (int i = 0; i < selectableSpecs.length; i++) {
                        CheckBox thisCheckBox = new CheckBox(this);
                        thisCheckBox.setText(selectableSpecs[i]);
                        thisCheckBox.setChecked(false);
                        int finalI = i;
                        thisCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
                            currentSelections[finalI] = thisCheckBox.isChecked();
                        });
                        // Use generateShareInfo to find if it is disabled
                        List<Integer> currentEntries = new ArrayList<>(1);
                        currentEntries.add(i);
                        final String modelInfo = generateShareInfo(currentEntries);
                        if (modelInfo.split("\n").length != 2) {
                            // This is not supposed...
                            thisCheckBox.setEnabled(false);
                            thisCheckBox.setText(selectableSpecs[i] + " " + getString(R.string.share_menu_not_applicable));
                        }
                        selectLayout.addView(thisCheckBox);
                    }

                    // Create the dialog.
                    final AlertDialog.Builder selectDialog = new AlertDialog.Builder(this);
                    selectDialog.setTitle(shareEntries[which]);
                    selectDialog.setView(selectChunk);
                    selectDialog.setPositiveButton(R.string.link_confirm, (dialog2, which2) -> {
                        // To be overwritten...
                    });
                    selectDialog.setNegativeButton(R.string.link_cancel, ((dialog2, which2) -> {
                        // Cancelled, do nothing
                    }));
                    final AlertDialog selectDialogCreated = selectDialog.create();
                    selectDialogCreated.show();

                    selectDialogCreated.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                        // Overwrite the positive button
                        try {
                            // Convert currentSelections to Entries Array.
                            List<Integer> currentEntries = new ArrayList<>(0);
                            for (int i = 0; i < currentSelections.length; i++) {
                                if (currentSelections[i]) {
                                    currentEntries.add(i);
                                }
                            }
                            if (currentEntries.size() > 0) {
                                final String modelInfo = generateShareInfo(currentEntries);
                                ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("MacIndexModelInfo", modelInfo);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(SpecsActivity.this, shareDescription[which], Toast.LENGTH_LONG).show();
                                selectDialogCreated.dismiss();
                            } else {
                                Toast.makeText(SpecsActivity.this, R.string.share_menu_null, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            ExceptionHelper.handleException(this, e, "selectDialog", "Error when copying currentSelections.");
                        }
                    });
                } else if (which == 3) {
                    // Generate link
                    final String shareLink = "https://paizhang.info/macindex/share?code=" + thisName.replace(" ", "_") + "_";
                    ClipboardManager clipboard = (ClipboardManager) SpecsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("MacIndexShareLink", shareLink);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SpecsActivity.this, shareDescription[which], Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                ExceptionHelper.handleException(this, e, "shareDialog", "Unable to create the share dialog.");
            }
        });
        shareDialog.show();
    }

    private String generateShareInfo(final List<Integer> entries) {
        String modelInfo = thisName + "\n";
        try {
            for (int i = 0; i < entries.size(); i++) {
                switch (entries.get(i)) {
                    case 0:
                        // Introduction
                        modelInfo = modelInfo.concat(thisYear.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.year) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisYear + "\n");
                        break;
                    case 1:
                        // Model Number
                        modelInfo = modelInfo.concat(thisModel.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.model) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisModel + "\n");
                        break;
                    case 2:
                        // Identification
                        modelInfo = modelInfo.concat(thisId.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.id) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisId + "\n");
                        break;
                    case 3:
                        // Gestalt
                        modelInfo = modelInfo.concat(thisGestalt.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.gestalt) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisGestalt + "\n");
                        break;
                    case 4:
                        // Part Number
                        modelInfo = modelInfo.concat(thisOrder.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.order) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisOrder + "\n");
                        break;
                    case 5:
                        // EMC
                        modelInfo = modelInfo.concat(thisEmc.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.emc) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisEmc + "\n");
                        break;
                    case 6:
                        // Processor
                        modelInfo = modelInfo.concat(thisProcessor.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.processor) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisProcessor + "\n");
                        break;
                    case 7:
                        // Graphics and Display
                        modelInfo = modelInfo.concat(thisGraphics.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.graphics) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisGraphics + "\n");
                        break;
                    case 8:
                        // ROM Information
                        modelInfo = modelInfo.concat(thisType.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.type) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisType + "\n");
                        break;
                    case 9:
                        // RAM Information
                        modelInfo = modelInfo.concat(thisMaxram.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.maxram) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisMaxram + "\n");
                        break;
                    case 10:
                        // Software Support
                        modelInfo = modelInfo.concat(thisSoftware.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.software) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisSoftware + "\n");
                        break;
                    case 11:
                        // Storage
                        modelInfo = modelInfo.concat(thisStorage.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.storage) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisStorage + "\n");
                        break;
                    case 12:
                        // Features and Expansions
                        modelInfo = modelInfo.concat(thisExpansion.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.bus_expansion) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisExpansion + "\n");
                        break;
                    case 13:
                        // Design
                        modelInfo = modelInfo.concat(thisDesign.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.design) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisDesign + "\n");
                        break;
                    case 14:
                        // Support Status
                        modelInfo = modelInfo.concat(thisSupport.equals(getString(R.string.not_applicable)) ? "" :
                                getString(R.string.support) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisSupport + "\n");
                        break;
                    case 15:
                        // Custom Comments
                        modelInfo = modelInfo.concat(thisComment.equals(getString(R.string.comment_null)) ? "" :
                                getString(R.string.comment) + (Locale.getDefault().getDisplayLanguage().equals("中文") ? "：" : ": ") + thisComment + "\n");
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            }
        } catch (Exception e) {
            ExceptionHelper.handleException(this, e, "generateShareInfo", "Illegal Argument. Received arguments:" + entries.toString());
        }
        return modelInfo.trim(); // Get rid of the last new line
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
        final Intent newMachine = new Intent(SpecsActivity.this, SpecsActivity.class);
        newMachine.putExtra("machineID", machineID);
        newMachine.putExtra("machineIDPositionInherit", true);
        newMachine.putExtra("machineIDPosition", machineIDPosition);
        newMachine.putExtra("thisCategory", categoryStartEnd);
        startActivity(newMachine);
        finish();
    }
}
