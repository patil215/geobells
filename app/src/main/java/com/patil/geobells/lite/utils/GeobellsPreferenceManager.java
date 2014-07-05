package com.patil.geobells.lite.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

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

    public void saveDays(ArrayList<Boolean> days) {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("pref_sunday", days.get(0));
        edit.putBoolean("pref_monday", days.get(1));
        edit.putBoolean("pref_tuesday", days.get(2));
        edit.putBoolean("pref_wednesday", days.get(3));
        edit.putBoolean("pref_thursday", days.get(4));
        edit.putBoolean("pref_friday", days.get(5));
        edit.putBoolean("pref_saturday", days.get(6));
        edit.commit();
    }

    public ArrayList<Boolean> getDays() {
        ArrayList<Boolean> days = new ArrayList<Boolean>();
        days.add(preferences.getBoolean("pref_sunday", true));
        days.add(preferences.getBoolean("pref_monday", true));
        days.add(preferences.getBoolean("pref_tuesday", true));
        days.add(preferences.getBoolean("pref_wednesday", true));
        days.add(preferences.getBoolean("pref_thursday", true));
        days.add(preferences.getBoolean("pref_friday", true));
        days.add(preferences.getBoolean("pref_saturday", true));
        return days;
    }
}
