package com.patil.geobells.lite.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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
        edit.putString("pref_notification_sound", chosenRingtone);
        edit.commit();
    }

    public String getNotificationSoundUri() {
        return preferences.getString("pref_notification_sound", "");
    }

    public boolean isVoiceReminderEnabled() {
        boolean voiceEnabled = preferences.getBoolean("pref_voice", true);
        Log.d("Preferencies", String.valueOf(voiceEnabled));
        return preferences.getBoolean("pref_voice", false);
    }

    public boolean isPopupReminderEnabled() {
        return preferences.getBoolean("pref_popup", false);
    }

    public boolean isShowBackgroundNotificationEnabled() {
        return preferences.getBoolean("pref_notification", true);
    }

    public boolean isLowPowerEnabled() {
        return preferences.getBoolean("pref_low_power", false);
    }

    public boolean isMetricEnabled() {
        return preferences.getBoolean("pref_metric", false);
    }

    public boolean isDisabled() {
        return preferences.getBoolean("pref_disable", false);
    }


    public void saveIntervalMultiplier(double interval) {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putFloat("pref_multiplier", (float)interval);
        edit.commit();
    }

    public double getIntervalMultiplier() {
        return preferences.getFloat("pref_multiplier", 1);
    }

    public void saveLastActivityRecognitionTime(long time) {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putLong("activity_time", time);
        edit.commit();
    }

    public long getLastActivityRecognitionTime() {
        return preferences.getLong("activity_time", 0);
    }
}
