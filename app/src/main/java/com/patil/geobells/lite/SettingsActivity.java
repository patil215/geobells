package com.patil.geobells.lite;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.patil.geobells.lite.R;
import com.patil.geobells.lite.utils.GeobellsPreferenceManager;

public class SettingsActivity extends PreferenceActivity {

    Preference notificationSoundPreference;
    GeobellsPreferenceManager preferenceManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        preferenceManager = new GeobellsPreferenceManager(this);

        notificationSoundPreference = (Preference) findPreference("preference_notification_sound");
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
