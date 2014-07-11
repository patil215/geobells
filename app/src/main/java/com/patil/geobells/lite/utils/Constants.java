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

    // primary colors: blue, deep orange, purple, amber, brown, green, red
    public static final String[] COLORS = new String[] {"#5677fc", "#ff5722", "#9c27b0", "#ffc107", "#795548", "#259b24", "#e51c23"};
    public static final String COLOR_ACTION_BAR = "#0a7e07";

    public static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    public static final String PLACES_AUTOCOMPLETE_TYPE_ADDRESS = "geocode";
    public static final String PLACES_AUTOCOMPLETE_TYPE_BUSINESS = "establishment";
    public static final String PLACES_TYPE_AUTOCOMPLETE = "/autocomplete";
    public static final String PLACES_TYPE_NEARBYSEARCH = "/nearbysearch";
    public static final String PLACES_TYPE_TEXTSEARCH = "/textsearch";
    public static final String PLACES_OUT_JSON = "/json";
    public static final String PLACES_API_KEY_AUTOCOMPLEE_LITE = "AIzaSyCzEMbwj8vbLH8i1_QegjVd6B-3oFUFyp8";
    public static final String PLACES_API_KEY_AUTOCOMPLETE_PRO = "AIzaSyCTNKrFcWPsBGe-g-hIxhmPTH6i1H3FaAs";
    public static final String PLACES_API_KEY_SEARCH_PRO = "AIzaSyBvvP27YFCUWApMSQGLaaDMWwwTYLW81_U";
    public static final String PLACES_API_KEY_SEARCH_LITE = "AIzaSyBvX3peWY0KFaB0t67KFdeLK7KtprOgPwE";

    public static final String GEOCODE_API_BASE=  "http://maps.googleapis.com/maps/api/geocode";
    public static final String GEOCODE_OUT_JSON =  "/json";


    public static final String METHOD_PLACES_DIALOG_ADDRESS = "address";
    public static final String METHOD_PLACES_CREATE = "create";
    public static final String METHOD_PLACES_DIALOG_VIEW = "view";

    public static final String METHOD_GEOCODE_CREATE = "create";
    public static final String GEOCODE_STATUS_OK = "OK";
    public static final String GEOCODE_STATUS_NORESULTS = "ZERO_RESULTS";
    public static final Double GEOCODE_RESPONSE_NORESULTS = -1.0;
    public static final Double GEOCODE_RESPONSE_ERROR = -2.0;
    public static final String FILE_NAME_TEMP = "temp";

    public static final String EXTRA_REMINDER_INDEX = "index";

    public static final String MAPS_API_KEY_DEBUG = "AIzaSyAtycxCYF1MvdR2MgxTT3W3EQCiMj4DStQ";
}
