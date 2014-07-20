package com.patil.geobells.lite;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class GeobellsApplication extends Application {

    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "UA-42514029-5";

    //Logging TAG
    private static final String TAG = "GeobellsAnalytics";

    public static int GENERAL_TRACKER = 0;

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER,
         ECOMMERCE_TRACKER,// Tracker used by all the apps from a company. eg: roll-up tracking.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public GeobellsApplication() {
        super();
    }

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.app_tracker)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(PROPERTY_ID): analytics.newTracker(R.xml.ecommerce_tracker);

            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }
}