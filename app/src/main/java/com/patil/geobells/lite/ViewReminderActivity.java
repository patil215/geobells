package com.patil.geobells.lite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.patil.geobells.lite.asynctask.DownloadImageTask;
import com.patil.geobells.lite.data.Place;
import com.patil.geobells.lite.data.Reminder;
import com.patil.geobells.lite.utils.Constants;
import com.patil.geobells.lite.utils.GeobellsDataManager;
import com.patil.geobells.lite.utils.GeobellsPreferenceManager;
import com.patil.geobells.lite.utils.GeobellsUtils;
import com.patil.geobells.lite.views.UpcomingRemindersFragment;

import java.util.ArrayList;

public class ViewReminderActivity extends Activity {

    TextView titleBox;
    TextView locationBox;
    ImageButton viewPlacesButton;
    TextView descriptionBox;
    TextView proximityBox;
    TextView daysBox;
    TextView repeatBox;
    TextView toggleBox;
    ImageView mapImage;

    Reminder reminder;
    int reminderIndex;

    ArrayList<Reminder> reminders;

    GeobellsDataManager dataManager;
    GeobellsPreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reminder);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setupViews();
        reminderIndex = getIntent().getIntExtra(Constants.EXTRA_REMINDER_INDEX, -1);
        dataManager = new GeobellsDataManager(this);
        preferenceManager = new GeobellsPreferenceManager(this);
        if (reminderIndex != -1) {
            reminders = dataManager.getSavedReminders();
            reminder = reminders.get(reminderIndex);
            displayDetails(reminder);
        } else {
            Toast.makeText(this, getString(R.string.toast_error_occurred), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void displayDetails(Reminder reminder) {
        titleBox.setText(reminder.title);
        setVolatileText(descriptionBox, reminder.description);
        if (reminder.type == Constants.TYPE_FIXED) {
            locationBox.setText(reminder.address);
        } else {
            locationBox.setText(reminder.business);
        }
        if (reminder.transition == Constants.TRANSITION_ENTER) {
            setVolatileText(proximityBox, getString(R.string.text_when_closer_than) + proximityToWord(reminder.proximity));
        } else {
            setVolatileText(proximityBox, getString(R.string.text_when_farther_than) + proximityToWord(reminder.proximity));
        }
        String[] displayDays = getResources().getStringArray(R.array.days);
        boolean[] days = reminder.days;
        String daysString = " ";
        for (int i = 0; i < days.length; i++) {
            if (days[i]) {
                daysString += displayDays[i] + ", ";
            }
        }
        daysString = daysString.substring(0, daysString.length() - 2);
        setVolatileText(daysBox, getString(R.string.text_reminds_on) + daysString);
        if (reminder.repeat) {
            setVolatileText(repeatBox, getString(R.string.text_repeats));
        }

        if (reminder.silencePhone && reminder.toggleAirplane) {
            setVolatileText(toggleBox, getString(R.string.text_silence_and_airplane));
        } else if (reminder.silencePhone) {
            setVolatileText(toggleBox, getString(R.string.text_silence));
        } else if (reminder.toggleAirplane) {
            setVolatileText(toggleBox, getString(R.string.text_airplane));
        } else {
            setVolatileText(toggleBox, "");
        }


        ArrayList<LatLng> positions = new ArrayList<LatLng>();
        if (reminder.type == Constants.TYPE_FIXED) {
            positions.add(new LatLng(reminder.latitude, reminder.longitude));
            String url = GeobellsUtils.constructMapImageURL(positions, Color.parseColor(Constants.COLORS[reminderIndex % reminders.size()]), Constants.SIZE_IMAGE_LARGE_HORIZONTAL, Constants.SIZE_IMAGE_LARGE_VERTICAL);
            new DownloadImageTask(this, mapImage).execute(url);
        } else {
            for (Place place : reminder.places) {
                positions.add(new LatLng(place.latitude, place.longitude));
            }
            String url = GeobellsUtils.constructMapImageURL(positions, Color.parseColor(Constants.COLORS[reminderIndex % reminders.size()]), Constants.SIZE_IMAGE_LARGE_HORIZONTAL, Constants.SIZE_IMAGE_LARGE_VERTICAL);
            new DownloadImageTask(this, mapImage).execute(url);
        }
    }


    public String proximityToWord(int proximity) {
        int[] proximities = Constants.PROXIMITY_DISTANCES;
        int index = Constants.PROXIMITY_DISTANCES_DEFAULT_INDEX;
        for (int i = 0; i < proximities.length; i++) {
            if (proximity == proximities[i]) {
                index = i;
                break;
            }
        }
        if (preferenceManager.isMetricEnabled()) {
            String[] metricArray = getResources().getStringArray(R.array.spinner_proximity_metric);
            return metricArray[index];
        } else {
            String[] feetArray = getResources().getStringArray(R.array.spinner_proximity);
            return feetArray[index];
        }
    }

    public void setVolatileText(TextView textBox, String text) {
        if (text.length() > 0) {
            textBox.setText(text);
        } else {
            textBox.setVisibility(View.GONE);
        }
    }

    public void setupViews() {
        titleBox = (TextView) findViewById(R.id.reminder_title);
        locationBox = (TextView) findViewById(R.id.reminder_location);
        viewPlacesButton = (ImageButton) findViewById(R.id.button_business_view_places);
        descriptionBox = (TextView) findViewById(R.id.reminder_description);
        proximityBox = (TextView) findViewById(R.id.reminder_proximity);
        daysBox = (TextView) findViewById(R.id.reminder_days);
        repeatBox = (TextView) findViewById(R.id.reminder_repeat);
        toggleBox = (TextView) findViewById(R.id.reminder_toggle);
        mapImage = (ImageView) findViewById(R.id.view_map);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_reminder, menu);
        return true;
    }

    public void onBusinessViewPlacesClick(View v) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        reminder = dataManager.getSavedReminders().get(reminderIndex);
        displayDetails(reminder);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_edit:
                Intent intent = new Intent(this, CreateReminderActivity.class);
                intent.putExtra(Constants.EXTRA_EDIT_REMINDER, true);
                intent.putExtra(Constants.EXTRA_REMINDER_INDEX, reminderIndex);
                startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_EDIT_REMINDER);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
