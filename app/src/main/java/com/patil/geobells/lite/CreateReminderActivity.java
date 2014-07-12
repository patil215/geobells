package com.patil.geobells.lite;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.patil.geobells.lite.asynctask.AsyncTaskCompleteListener;
import com.patil.geobells.lite.asynctask.GeocoderAPIAsyncTask;
import com.patil.geobells.lite.asynctask.PlacesAPIAsyncTask;
import com.patil.geobells.lite.data.Place;
import com.patil.geobells.lite.data.Reminder;
import com.patil.geobells.lite.utils.Config;
import com.patil.geobells.lite.utils.Constants;
import com.patil.geobells.lite.utils.GeobellsDataManager;
import com.patil.geobells.lite.utils.GeobellsPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class CreateReminderActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, AsyncTaskCompleteListener<String> {

    EditText titleBox;
    EditText descriptionBox;
    RadioButton specificRadioButton;
    RadioButton dynamicRadioButton;
    AutoCompleteTextView businessBox;
    AutoCompleteTextView addressBox;
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
    GeobellsDataManager dataManager;

    // Generic Reminder data
    String title;
    boolean completed;
    boolean repeat;
    boolean[] days = new boolean[7];
    int proximity;
    boolean toggleAirplane;
    boolean silencePhone;
    long timeCreated;
    long timeCompleted;
    int transition;
    String description;

    // Fixed reminder
    String address;
    double latitude;
    double longitude;

    // Dynamic reminder
    String business;
    // Places omitted because it is generated after asynctask

    LocationClient locationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder);
        setupViews();
        for (int i = 0; i < 7; i++) {
            days[i] = true;
        }
        locationClient = new LocationClient(this, this, this);
        preferenceManager = new GeobellsPreferenceManager(this);
        dataManager = new GeobellsDataManager(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setupSpinner();
        setupAutocomplete();
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationClient.connect();
    }

    @Override
    protected void onStop() {
        locationClient.disconnect();
        super.onStop();
    }

    public double[] getLatLong() {
        Location lastLocation = locationClient.getLastLocation();
        return new double[]{lastLocation.getLatitude(), lastLocation.getLongitude()};
    }

    public void setupSpinner() {
        ArrayAdapter<CharSequence> adapter;
        if (preferenceManager.isMetricEnabled()) {
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.spinner_proximity_metric, android.R.layout.simple_spinner_dropdown_item);
        } else {
            adapter = ArrayAdapter.createFromResource(this, R.array.spinner_proximity, android.R.layout.simple_spinner_dropdown_item);
        }
        proximitySpinner.setAdapter(adapter);
        proximitySpinner.setSelection(Constants.PROXIMITY_DISTANCES_DEFAULT_INDEX);
        proximitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                proximity = Constants.PROXIMITY_DISTANCES[pos];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                proximity = Constants.PROXIMITY_DISTANCES[Constants.PROXIMITY_DISTANCES_DEFAULT_INDEX];
            }
        });
        proximity = Constants.PROXIMITY_DISTANCES[Constants.PROXIMITY_DISTANCES_DEFAULT_INDEX];
    }

    public void setupAutocomplete() {
        addressBox.setAdapter(new PlacesAutoCompleteAddressAdapter(this, R.layout.list_item_autocomplete));
        addressBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String str = (String) adapterView.getItemAtPosition(position);
            }
        });
        if (Config.AUTOCOMPLETE_BUSINESSES) {
            businessBox.setAdapter(new PlacesAutoCompleteBusinessAdapter(this, R.layout.list_item_autocomplete));
            businessBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String str = (String) adapterView.getItemAtPosition(position);
                }
            });
        }
    }

    public void setupViews() {
        titleBox = (EditText) findViewById(R.id.reminder_title);
        descriptionBox = (EditText) findViewById(R.id.reminder_description);
        specificRadioButton = (RadioButton) findViewById(R.id.radiobutton_reminder_type_specific);
        dynamicRadioButton = (RadioButton) findViewById(R.id.radiobutton_reminder_type_dynamic);
        businessBox = (AutoCompleteTextView) findViewById(R.id.reminder_business);
        addressBox = (AutoCompleteTextView) findViewById(R.id.reminder_address);
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
        if (advancedLayout.getVisibility() == View.VISIBLE) {
            advancedLayout.setVisibility(View.GONE);
        } else {
            advancedLayout.setVisibility(View.VISIBLE);
        }
    }

    public void onChooseDaysClick(View v) {
        final CharSequence[] displayDays = {getString(R.string.day_sunday), getString(R.string.day_monday), getString(R.string.day_tuesday), getString(R.string.day_wednesday), getString(R.string.day_thursday), getString(R.string.day_friday), getString(R.string.day_saturday)};
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
                }
        )
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

        builder.create().show();
    }

    public void onBusinessViewPlacesClick(View v) {
        if (businessBox.getText() != null && businessBox.getText().length() > 0) {
            double[] latLong = getLatLong();
            new PlacesAPIAsyncTask(this, this, Constants.METHOD_PLACES_DIALOG_VIEW).execute(businessBox.getText().toString(), String.valueOf(latLong[0]), String.valueOf(latLong[1]));
        } else {
            Toast.makeText(this, getString(R.string.toast_fill_business), Toast.LENGTH_SHORT).show();
        }
    }


    public void onAddressSearchClick(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.dialog_title_address_search));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setHint(getString(R.string.hint_reminder_address_search));
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.dialog_button_search), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                double[] latLong = getLatLong();
                new PlacesAPIAsyncTask(CreateReminderActivity.this, CreateReminderActivity.this, Constants.METHOD_PLACES_DIALOG_ADDRESS).execute(value, String.valueOf(latLong[0]), String.valueOf(latLong[1]));
            }
        });
        alert.setCancelable(false);
        alert.setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    public void onAddressMapClick(View v) {
        if (addressBox.getText() != null && addressBox.getText().toString().length() != 0) {
            address = addressBox.getText().toString();
            new GeocoderAPIAsyncTask(this, this, Constants.METHOD_GEOCODE_START_MAP).execute(address);
        } else {
            Intent intent = new Intent(this, ViewPickMapActivity.class);
            intent.putExtra(Constants.EXTRA_REMINDER_LATITUDE, Constants.NO_REMINDER_LATITUDE);
            intent.putExtra(Constants.EXTRA_REMINDER_LONGITUDE, Constants.NO_REMINDER_LONGITUDE);
            intent.putExtra(Constants.EXTRA_REMINDER_ADDRESS, Constants.NO_REMINDER_ADDRESS);
            startActivityForResult(intent, Constants.ACTIVITY_RESULT_CODE_PICK_MAP);
        }
    }

    public boolean isNecessaryFieldsCompleted() {
        if (titleBox.getText().toString() != null && titleBox.getText().toString().length() > 0) {
            if (enterRadioButton.isChecked() || exitRadioButton.isChecked()) {
                if (specificRadioButton.isChecked()) {
                    if (addressBox.getText().toString() != null && addressBox.getText().toString().length() > 0) {
                        return true;
                    }
                } else if (dynamicRadioButton.isChecked()) {
                    if (businessBox.getText().toString() != null && businessBox.getText().toString().length() > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public void createSpecificReminder() {
        address = addressBox.getText().toString();
        new GeocoderAPIAsyncTask(this, this, Constants.METHOD_GEOCODE_CREATE).execute(address);
    }

    public void finishCreatingFixedReminder(Double[] coords) {
        ArrayList<Reminder> reminders = dataManager.getSavedReminders();
        Reminder reminder = new Reminder();
        reminder.title = title;
        reminder.description = description;
        reminder.completed = completed;
        reminder.repeat = repeat;
        reminder.days = days;
        reminder.proximity = proximity;
        reminder.toggleAirplane = toggleAirplane;
        reminder.silencePhone = silencePhone;
        reminder.timeCreated = timeCreated;
        reminder.timeCompleted = timeCompleted;
        reminder.transition = transition;
        address = addressBox.getText().toString();
        latitude = coords[0];
        longitude = coords[1];
        reminder.type = Constants.TYPE_FIXED;
        reminder.address = address;
        reminder.latitude = latitude;
        reminder.longitude = longitude;
        reminders.add(reminder);
        dataManager.saveReminders(reminders);
        Toast.makeText(this, getString(R.string.toast_reminder_created), Toast.LENGTH_SHORT).show();
        finish();
    }

    public void createDynamicReminder() {
        double[] latLng = getLatLong();
        business = businessBox.getText().toString();
        new PlacesAPIAsyncTask(this, this, Constants.METHOD_PLACES_CREATE).execute(business, String.valueOf(latLng[0]), String.valueOf(latLng[1]));
    }

    public void finishCreatingDynamicReminder(ArrayList<Place> places) {
        ArrayList<Reminder> reminders = dataManager.getSavedReminders();
        Reminder reminder = new Reminder();
        reminder.title = title;
        reminder.description = description;
        reminder.completed = completed;
        reminder.repeat = repeat;
        reminder.days = days;
        reminder.proximity = proximity;
        reminder.toggleAirplane = toggleAirplane;
        reminder.silencePhone = silencePhone;
        reminder.timeCreated = timeCreated;
        reminder.timeCompleted = timeCompleted;
        reminder.transition = transition;
        business = businessBox.getText().toString();
        reminder.type = Constants.TYPE_DYNAMIC;
        reminder.business = business;
        reminder.places = places;
        reminders.add(reminder);
        dataManager.saveReminders(reminders);
        Toast.makeText(this, getString(R.string.toast_reminder_created), Toast.LENGTH_SHORT).show();
        finish();
    }

    public void createReminder() {
        if (isNecessaryFieldsCompleted()) {
            title = titleBox.getText().toString();
            completed = false;
            repeat = repeatCheckBox.isChecked();
            if (descriptionBox.getText().toString() != null && descriptionBox.getText().toString().length() > 0) {
                description = descriptionBox.getText().toString();
            } else {
                description = "";
            }
            // days is already set through dialog
            // proximity is already set through dialog
            toggleAirplane = airplaneCheckBox.isChecked();
            silencePhone = silenceCheckBox.isChecked();
            timeCreated = System.currentTimeMillis();
            timeCompleted = Constants.TIME_COMPLETED_DEFAULT;
            if (enterRadioButton.isChecked()) {
                transition = Constants.TRANSITION_ENTER;
            } else {
                transition = Constants.TRANSITION_EXIT;
            }
            if (specificRadioButton.isChecked()) {
                createSpecificReminder();
            } else if (dynamicRadioButton.isChecked()) {
                createDynamicReminder();
            }
        } else {
            Toast.makeText(this, getString(R.string.toast_fill_info), Toast.LENGTH_SHORT).show();
        }
    }

    public void showSimpleDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(message).setTitle(title).setPositiveButton(getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    public ArrayList<String> autocomplete(String input, String type) {
        ArrayList<String> resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(Constants.PLACES_API_BASE + Constants.PLACES_TYPE_AUTOCOMPLETE + Constants.PLACES_OUT_JSON);
            if (Config.IS_LITE_VERSION) {
                sb.append("?key=" + Constants.PLACES_API_KEY_AUTOCOMPLEE_LITE);
            } else {
                sb.append("?key=" + Constants.PLACES_API_KEY_AUTOCOMPLETE_PRO);
            }
            sb.append("&location=" + getLatLong()[0] + "," + getLatLong()[1]);
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
            sb.append("&type=" + type);

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e("Autocomplete", "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e("Autocomplete", "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e("Autocomplete", "Cannot process JSON results", e);
        }

        return resultList;
    }

    @Override
    public void onPlacesTaskComplete(ArrayList<Place> places, String method) {
        if (method.equals(Constants.METHOD_PLACES_DIALOG_ADDRESS)) {
            if (places.size() > 0) {
                createAddressPlaceSearchDialog(places);
            } else {
                showSimpleDialog(getString(R.string.dialog_title_search_no_results), getString(R.string.dialog_message_search_no_results));
            }
        } else if (method.equals(Constants.METHOD_PLACES_CREATE)) {
            finishCreatingDynamicReminder(places);
        } else if (method.equals(Constants.METHOD_PLACES_DIALOG_VIEW)) {
            if (places.size() > 0) {
                createViewPlaceSearchDialog(places);
            } else {
                showSimpleDialog(getString(R.string.dialog_title_view_no_results), getString(R.string.dialog_message_view_no_results));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK) {
            addressBox.setText(intent.getStringExtra(Constants.EXTRA_MARKER_ADDRESS));
        }
    }

    @Override
    public void onGeocodeTaskComplete(Double[] coords, String method) {
        if (method.equals(Constants.METHOD_GEOCODE_CREATE)) {
            if (coords[0] == Constants.GEOCODE_RESPONSE_NORESULTS) {
                Toast.makeText(this, getString(R.string.toast_no_geocode_results), Toast.LENGTH_SHORT).show();
            } else if (coords[0] == Constants.GEOCODE_RESPONSE_ERROR) {
                Toast.makeText(this, getString(R.string.toast_no_geocode_results), Toast.LENGTH_SHORT).show();
            } else {
                finishCreatingFixedReminder(coords);
            }
        } else if (method.equals(Constants.METHOD_GEOCODE_START_MAP)) {
            if (coords[0] == Constants.GEOCODE_RESPONSE_NORESULTS) {
                Intent intent = new Intent(this, ViewPickMapActivity.class);
                intent.putExtra(Constants.EXTRA_REMINDER_LATITUDE, Constants.INVALID_REMINDER_LATITUDE);
                intent.putExtra(Constants.EXTRA_REMINDER_LONGITUDE, Constants.INVALID_REMINDER_LONGITUDE);
                intent.putExtra(Constants.EXTRA_REMINDER_ADDRESS, addressBox.getText().toString());
                startActivityForResult(intent, Constants.ACTIVITY_RESULT_CODE_PICK_MAP);
            } else {
                Intent intent = new Intent(this, ViewPickMapActivity.class);
                intent.putExtra(Constants.EXTRA_REMINDER_LATITUDE, coords[0]);
                intent.putExtra(Constants.EXTRA_REMINDER_LONGITUDE, coords[1]);
                intent.putExtra(Constants.EXTRA_REMINDER_ADDRESS, addressBox.getText().toString());
                startActivityForResult(intent, Constants.ACTIVITY_RESULT_CODE_PICK_MAP);
            }
        }
    }

    @Override
    public void onReverseGeocodeTaskComplete(String address, String method) {

    }

    private class PlacesAutoCompleteAddressAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public PlacesAutoCompleteAddressAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString(), Constants.PLACES_AUTOCOMPLETE_TYPE_ADDRESS);

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

    private class PlacesAutoCompleteBusinessAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public PlacesAutoCompleteBusinessAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString(), Constants.PLACES_AUTOCOMPLETE_TYPE_BUSINESS);

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }


    public void createAddressPlaceSearchDialog(ArrayList<Place> places) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_results));

        ListView modeList = new ListView(this);

        final String[] stringArray = new String[places.size()];
        for (int i = 0; i < places.size(); i++) {
            stringArray[i] = places.get(i).title + " - " + places.get(i).address;
        }
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
        modeList.setAdapter(modeAdapter);

        builder.setView(modeList);
        final Dialog dialog = builder.create();

        modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                addressBox.setText(stringArray[position]);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void createViewPlaceSearchDialog(ArrayList<Place> places) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_view_places));

        ListView modeList = new ListView(this);

        final String[] stringArray = new String[places.size()];
        for (int i = 0; i < places.size(); i++) {
            stringArray[i] = places.get(i).title + " - " + places.get(i).address;
        }
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
        modeList.setAdapter(modeAdapter);

        builder.setView(modeList);
        final Dialog dialog = builder.create();

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_reminder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                createReminder();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnected(Bundle dataBundle) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
