package com.patil.geobells.lite.data;


import java.util.ArrayList;

public class Reminder {
    public String title;
    public boolean completed;
    public boolean repeat; // Whether to reset daily
    public boolean[] days; // Length 7 (days of week)
    public int proximity; // Distance in meters
    public boolean toggleAirplane;
    public boolean silencePhone;
    public long timeCreated;
    public long timeCompleted; // -1 if not completed yet
    public int transition; // Enter or exit, defined in Constants
    public String description;

     /* Why include both FixedReminder and DynamicReminder elements instead of using polymorphism?, you ask.
        It's because Gson doesn't have an easy way of using polymorphism.
       That's okay because we'll just have a variable containing the type of the reminder. */
    public int type; // Fixed or dynamic, defined in Constants

    // Fixed reminder data


    // Dynamic reminder data
    public String business;
    public ArrayList<Place> places;

    public String address;
    public double latitude;
    public double longitude;
}
