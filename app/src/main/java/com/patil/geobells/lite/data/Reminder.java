package com.patil.geobells.lite.data;


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
}
