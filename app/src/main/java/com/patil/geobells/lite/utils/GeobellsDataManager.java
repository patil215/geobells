package com.patil.geobells.lite.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.patil.geobells.lite.data.Reminder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class GeobellsDataManager {
    Context context;
    SharedPreferences preferences;
    public GeobellsDataManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(Constants.PREFERENCES_DATA, Context.MODE_PRIVATE);
    }

    public ArrayList<Reminder> getSavedReminders() {
        String dataString = preferences.getString(Constants.PREFERENCES_DATA_KEY, "");
        if(dataString.length() == 0) {
            return new ArrayList<Reminder>();
        }
        return new Gson().fromJson(dataString, new TypeToken<List<Reminder>>() {
        }.getType());
    }

    public ArrayList<Reminder> getUpcomingReminders() {
        String dataString = preferences.getString(Constants.PREFERENCES_DATA_KEY, "");
        if(dataString.length() == 0) {
            return new ArrayList<Reminder>();
        }
        ArrayList<Reminder> reminders = new Gson().fromJson(dataString, new TypeToken<List<Reminder>>() {
        }.getType());
        ArrayList<Reminder> upcomingReminders = new ArrayList<Reminder>();
        for(Reminder reminder : reminders) {
            if(!reminder.completed) {
                upcomingReminders.add(reminder);
            }
        }
        return upcomingReminders;
    }


    public ArrayList<Reminder> getCompletedReminders() {
        String dataString = preferences.getString(Constants.PREFERENCES_DATA_KEY, "");
        if(dataString.length() == 0) {
            return new ArrayList<Reminder>();
        }
        ArrayList<Reminder> reminders = new Gson().fromJson(dataString, new TypeToken<List<Reminder>>() {
        }.getType());
        ArrayList<Reminder> completedReminders = new ArrayList<Reminder>();
        for(Reminder reminder : reminders) {
            if(reminder.completed) {
                completedReminders.add(reminder);
            }
        }
        return completedReminders;
    }

    public void saveReminders(ArrayList<Reminder> reminders) {
        SharedPreferences.Editor editor = preferences.edit();
        String dataString = new Gson().toJson(reminders);
        editor.putString(Constants.PREFERENCES_DATA_KEY, dataString);
        editor.commit();
    }
}
