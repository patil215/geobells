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

    public boolean isVoiceReminderEnabled() {
        return preferences.getBoolean("preference_voice", false);
    }

    public boolean isPopupReminderEnabled() {
        return preferences.getBoolean("preference_popup", false);
    }

    public boolean isShowBackgroundNotificationEnabled() {
        return preferences.getBoolean("preference_notification", true);
    }

    public boolean isLowPowerEnabled() {
        return preferences.getBoolean("preference_low_power", false);
    }

    public boolean isMetricEnabled() {
        return preferences.getBoolean("preference_metric", false);
    }
}
