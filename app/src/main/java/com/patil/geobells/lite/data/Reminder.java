package com.patil.geobells.lite.data;


public class Reminder {
    public String name;
    public boolean completed;
    public boolean repeat; // Whether to reset daily
    public boolean[] days; // Length 7 (days of week)
    public int distance; // Distance in meters
    public boolean toggleAirplane;
    public boolean silencePhone;
    public long timeCompleted;
    public long timeCreated;
    public int transition; // Enter or exit
}
