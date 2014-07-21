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

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.maps.model.LatLng;
import com.patil.geobells.lite.asynctask.DownloadImageTask;
import com.patil.geobells.lite.data.Place;
import com.patil.geobells.lite.data.Reminder;
import com.patil.geobells.lite.utils.Constants;
import com.patil.geobells.lite.utils.GeobellsDataManager;
import com.patil.geobells.lite.utils.GeobellsPreferenceManager;
import com.patil.geobells.lite.utils.GeobellsUtils;

import java.util.ArrayList;

public class ViewReminderActivity extends Activity {

    private TextView titleBox;
    private TextView locationBox;
    private ImageButton viewPlacesButton;
    private TextView descriptionBox;
    private TextView proximityBox;
    private TextView daysBox;
    private TextView repeatBox;
    private TextView toggleBox;
    private ImageView mapImage;

    private Reminder reminder;
    private int reminderIndex;
    private MenuItem setUncompletedItem;
    private MenuItem setCompletedItem;

    private ArrayList<Reminder> reminders;

    private GeobellsDataManager dataManager;
    private GeobellsPreferenceManager preferenceManager;

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
        int daysCount = 0;
        for (int i = 0; i < days.length; i++) {
            if (days[i]) {
                daysCount++;
                daysString += displayDays[i] + ", ";
            }
        }
        if(daysCount < 7) {
            daysString = daysString.substring(0, daysString.length() - 2);
        } else {
            daysString = getString(R.string.all_days);
        }
        setVolatileText(daysBox, getString(R.string.text_reminds_on) + daysString);
        if (reminder.repeat) {
            setVolatileText(repeatBox, getString(R.string.text_repeats));
        } else {
            setVolatileText(repeatBox, "");
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
        setUncompletedItem = menu.findItem(R.id.action_set_uncompleted);
        setCompletedItem = menu.findItem(R.id.action_set_completed);

        if(reminder.completed) {
            setCompletedItem.setVisible(false);
            setUncompletedItem.setVisible(true);
        } else {
            setCompletedItem.setVisible(true);
            setUncompletedItem.setVisible(false);
        }
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
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
                break;
            case R.id.action_edit:
                Intent intent = new Intent(this, CreateReminderActivity.class);
                intent.putExtra(Constants.EXTRA_EDIT_REMINDER, true);
                intent.putExtra(Constants.EXTRA_REMINDER_INDEX, reminderIndex);
                startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_EDIT_REMINDER);
                Intent returnIntent1 = new Intent();
                setResult(RESULT_OK, returnIntent1);
                finish();
                break;
            case R.id.action_delete:
                reminders.remove(reminderIndex);
                dataManager.saveReminders(reminders);
                Intent returnIntent2 = new Intent();
                setResult(RESULT_OK, returnIntent2);
                finish();
                Toast.makeText(this, getString(R.string.toast_reminder_deleted), Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_set_completed:
                if(!reminder.completed) {
                    reminder.completed = true;
                }
                dataManager.saveReminders(reminders);
                Toast.makeText(this, getString(R.string.toast_reminder_swipe_completed), Toast.LENGTH_SHORT).show();
                Intent returnIntent3 = new Intent();
                setResult(RESULT_OK, returnIntent3);
                finish();
                break;
            case R.id.action_set_uncompleted:
                if(reminder.completed) {
                    reminder.completed = false;
                }
                dataManager.saveReminders(reminders);
                Toast.makeText(this, getString(R.string.toast_reminder_swipe_uncompleted), Toast.LENGTH_SHORT).show();
                Intent returnIntent4 = new Intent();
                setResult(RESULT_OK, returnIntent4);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Get an Analytics tracker to report app starts and uncaught exceptions etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);

    }
}
