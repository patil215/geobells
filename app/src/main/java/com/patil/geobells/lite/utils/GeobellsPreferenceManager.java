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
}
