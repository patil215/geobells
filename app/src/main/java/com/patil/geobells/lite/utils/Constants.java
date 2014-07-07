package com.patil.geobells.lite.utils;


public class Constants {
    public static final int TRANSITION_ENTER = 0;
    public static final int TRANSITION_EXIT = 1;
    public static final int[] PROXIMITY_DISTANCES = new int[] {15, 30, 90, 200, 400, 800, 1600, 3200, 8000, 16000};
    public static final int PROXIMITY_DISTANCES_DEFAULT_INDEX = 5;
    public static final String PREFERENCES_DATA = "data";
    public static final String PREFERENCES_DATA_KEY = "reminders";
    public static final long TIME_COMPLETED_DEFAULT = -1;
    public static final int TYPE_FIXED = 0;
    public static final int TYPE_DYNAMIC = 1;
    // red, blue, orange, brown, green, yellow, purple
    public static final String[] COLORS = new String[] {"#b0120a", "#2a36b1", "#e65100", "#3e2723", "#0d5302", "#f57f17", "#4a148c"};
    public static final String AUTOCOMPLETE_TYPE_ADDRESS = "geocode";
    public static final String AUTOCOMPLETE_TYPE_BUSINESS = "establishment";
}
