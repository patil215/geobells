package com.patil.geobells.lite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.RingtoneManager;
import android.media.audiofx.BassBoost;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.patil.geobells.lite.R;
import com.patil.geobells.lite.data.Reminder;
import com.patil.geobells.lite.service.LocationService;
import com.patil.geobells.lite.utils.Config;
import com.patil.geobells.lite.utils.GeobellsDataManager;
import com.patil.geobells.lite.utils.GeobellsPreferenceManager;
import com.patil.geobells.lite.views.UpgradeDialog;

import java.util.ArrayList;

public class SettingsActivity extends PreferenceActivity {

    Preference notificationSoundPreference;
    Preference clearRemindersPreference;
    Preference intervalPreference;
    CheckBoxPreference popupRemindersPreference;
    CheckBoxPreference disableGeobellsPreference;
    GeobellsPreferenceManager preferenceManager;
    GeobellsDataManager dataManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        preferenceManager = new GeobellsPreferenceManager(this);
        dataManager = new GeobellsDataManager(this);

        notificationSoundPreference = (Preference) findPreference("pref_notification_sound");
        notificationSoundPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.title_ringtone_dialog));
                // intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                startActivityForResult(intent, 5);
                return false;
            }
        });

        clearRemindersPreference = (Preference) findPreference("pref_clear_reminders");
        clearRemindersPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this).setMessage(getString(R.string.dialog_message_clear_all_reminders)).setTitle(getString(R.string.dialog_title_clear_all_reminders)).setPositiveButton(getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dataManager.saveReminders(new ArrayList<Reminder>());
                        Toast.makeText(SettingsActivity.this, getString(R.string.toast_reminders_cleared), Toast.LENGTH_SHORT).show();
                        Intent serviceIntent = new Intent(SettingsActivity.this, LocationService.class);
                        startService(serviceIntent);
                    }
                }).setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();
                dialog.show();
                return false;
            }
        });

        disableGeobellsPreference = (CheckBoxPreference) findPreference("pref_disable");
        disableGeobellsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent serviceIntent = new Intent(SettingsActivity.this, LocationService.class);
                startService(serviceIntent);
                return false;
            }
        });

        intervalPreference = (Preference) findPreference("pref_interval");
        intervalPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.dialog_preference_interval, null);
                final EditText input = (EditText)dialogLayout.findViewById(R.id.text_multiplier);
                input.setText(String.valueOf(preferenceManager.getIntervalMultiplier()));

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this).setMessage(getString(R.string.dialog_message_interval)).setView(dialogLayout).setTitle(getString(R.string.dialog_title_interval)).setPositiveButton(getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(input.getText().toString().length() > 0) {
                            preferenceManager.saveIntervalMultiplier(Double.valueOf(input.getText().toString()));
                        } else {
                            Toast.makeText(SettingsActivity.this, getString(R.string.toast_multiplier_length), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
        });

        popupRemindersPreference = (CheckBoxPreference) findPreference("pref_popup");
        if(Config.IS_LITE_VERSION) {
            popupRemindersPreference.setEnabled(false);
            popupRemindersPreference.setSummary(getString(R.string.preference_popup_summary_lite));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return false;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
    {
        if (resultCode == Activity.RESULT_OK && requestCode == 5)
        {
            String chosenRingtone;
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null)
            {
                chosenRingtone = uri.toString();
            }
            else
            {
                chosenRingtone = null;
            }
            if(chosenRingtone != null) {
                preferenceManager.saveNotificationSoundURI(chosenRingtone);
            } else {
                preferenceManager.saveNotificationSoundURI("");
            }
        }
    }
}
