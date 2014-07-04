package com.patil.geobells.lite;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class CreateReminderActivity extends Activity {

    EditText titleBox;
    RadioButton specificRadioButton;
    RadioButton dynamicRadioButton;
    EditText businessBox;
    EditText addressBox;
    RadioButton enterRadioButton;
    RadioButton exitRadioButton;
    Spinner proximitySpinner;
    CheckBox repeatCheckBox;
    CheckBox airplaneCheckBox;
    CheckBox silenceCheckBox;
    TextView proximityPrompt;
    RelativeLayout specificLayout;
    RelativeLayout dynamicLayout;
    RelativeLayout advancedLayout;
    Button advancedButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder);
        setupViews();
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
}
