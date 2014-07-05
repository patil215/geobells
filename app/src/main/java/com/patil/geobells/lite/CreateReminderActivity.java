package com.patil.geobells.lite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.patil.geobells.lite.utils.Constants;
import com.patil.geobells.lite.utils.GeobellsPreferenceManager;

import java.util.ArrayList;

public class CreateReminderActivity extends Activity implements AdapterView.OnItemSelectedListener {

    EditText titleBox;
    RadioButton specificRadioButton;
    RadioButton dynamicRadioButton;
    EditText businessBox;
    EditText addressBox;
    RadioButton enterRadioButton;
    RadioButton exitRadioButton;
    CheckBox repeatCheckBox;
    CheckBox airplaneCheckBox;
    CheckBox silenceCheckBox;
    TextView proximityPrompt;
    RelativeLayout specificLayout;
    RelativeLayout dynamicLayout;
    RelativeLayout advancedLayout;
    Button advancedButton;
    Spinner proximitySpinner;

    GeobellsPreferenceManager preferenceManager;

    boolean[] days = new boolean[7];
    int proximity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder);
        setupViews();
        for(int i = 0; i < 7; i++) {
            days[i] = true;
        }
        preferenceManager = new GeobellsPreferenceManager(this);
        setupSpinner();
    }

    public void setupSpinner() {
        ArrayAdapter<CharSequence> adapter;
        if(preferenceManager.isMetricEnabled()) {
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.spinner_proximity_metric, android.R.layout.simple_spinner_dropdown_item);
        } else {
            adapter = ArrayAdapter.createFromResource(this, R.array.spinner_proximity, android.R.layout.simple_spinner_dropdown_item);
        }
        proximitySpinner.setAdapter(adapter);
        proximitySpinner.setSelection(Constants.PROXIMITY_DISTANCES_DEFAULT_INDEX);
        proximity = Constants.PROXIMITY_DISTANCES[Constants.PROXIMITY_DISTANCES_DEFAULT_INDEX];
    }

    public void setupViews() {
        titleBox = (EditText) findViewById(R.id.reminder_title);
        specificRadioButton = (RadioButton) findViewById(R.id.radiobutton_reminder_type_specific);
        dynamicRadioButton = (RadioButton) findViewById(R.id.radiobutton_reminder_type_dynamic);
        businessBox = (EditText) findViewById(R.id.reminder_business);
        addressBox = (EditText) findViewById(R.id.reminder_address);
        enterRadioButton = (RadioButton) findViewById(R.id.radiobutton_reminder_transition_enter);
        exitRadioButton = (RadioButton) findViewById(R.id.radiobutton_reminder_transition_exit);
        proximitySpinner = (Spinner) findViewById(R.id.spinner_reminder_proximity);
        repeatCheckBox = (CheckBox) findViewById(R.id.checkbox_reminder_repeat);
        airplaneCheckBox = (CheckBox) findViewById(R.id.checkbox_reminder_airplane);
        silenceCheckBox = (CheckBox) findViewById(R.id.checkbox_reminder_silence);
        proximityPrompt = (TextView) findViewById(R.id.prompt_reminder_proximity);
        specificLayout = (RelativeLayout) findViewById(R.id.layout_reminder_specific);
        dynamicLayout = (RelativeLayout) findViewById(R.id.layout_reminder_dynamic);
        advancedLayout = (RelativeLayout) findViewById(R.id.layout_advanced_options);
        advancedButton = (Button) findViewById(R.id.button_advanced_options);
    }

    public void onTypeSpecificClick(View v) {
        dynamicLayout.setVisibility(View.GONE);
        specificLayout.setVisibility(View.VISIBLE);
        advancedButton.setVisibility(View.VISIBLE);
    }

    public void onTypeDynamicClick(View v) {
        specificLayout.setVisibility(View.GONE);
        dynamicLayout.setVisibility(View.VISIBLE);
        advancedButton.setVisibility(View.VISIBLE);
    }

    public void onAdvancedOptionsClick(View v) {
        if(advancedLayout.getVisibility() == View.VISIBLE) {
            advancedLayout.setVisibility(View.GONE);
        } else {
            advancedLayout.setVisibility(View.VISIBLE);
        }
    }

    public void onChooseDaysClick(View v) {

        final CharSequence[] displayDays = {getString(R.string.sunday), getString(R.string.monday), getString(R.string.tuesday), getString(R.string.wednesday), getString(R.string.thursday), getString(R.string.friday), getString(R.string.saturday)};
        // arraylist to keep the selected items
        final boolean[] selectedItems = days;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.prompt_reminder_days));
        builder.setMultiChoiceItems(displayDays, selectedItems,
                new DialogInterface.OnMultiChoiceClickListener() {
                    // indexSelected contains the index of item (of which checkbox checked)
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected,
                                        boolean isChecked) {
                        selectedItems[indexSelected] = isChecked;
                    }
                })
                // Set the action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        CreateReminderActivity.this.days = selectedItems;
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        builder.create().show();//AlertDialog dialog; create like this outside onClick
    }

    public void onBusinessViewPlacesClick(View v) {

    }

    public void onAddressSearchClick(View v) {

    }

    public void onAddressMapClick(View v) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_reminder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        proximity = Constants.PROXIMITY_DISTANCES[pos];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        proximity = Constants.PROXIMITY_DISTANCES[Constants.PROXIMITY_DISTANCES_DEFAULT_INDEX];
    }
}
