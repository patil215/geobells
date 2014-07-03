package com.patil.geobells.lite.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GeobellsPreferenceManager {
    Context context;
    SharedPreferences preferences;
    public GeobellsPreferenceManager(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void saveNotificationSoundURI(String chosenRingtone) {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("notification_sound", chosenRingtone);
        edit.commit();
    }

    public String getNotificationSoundUri() {
        return preferences.getString("notification_sound", "");
    }
}
