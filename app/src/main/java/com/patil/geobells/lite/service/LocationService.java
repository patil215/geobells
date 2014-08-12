package com.patil.geobells.lite.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.patil.geobells.lite.MainActivity;
import com.patil.geobells.lite.R;
import com.patil.geobells.lite.ViewReminderActivity;
import com.patil.geobells.lite.data.Place;
import com.patil.geobells.lite.data.Reminder;
import com.patil.geobells.lite.utils.Constants;
import com.patil.geobells.lite.utils.GeobellsDataManager;
import com.patil.geobells.lite.utils.GeobellsPreferenceManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class LocationService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private int activity = -2;
    private final IBinder binder = new LocationBinder();
    private ArrayList<Reminder> reminders;

    private LocationClient locationClient = null;
    private LocationRequest locationRequest;

    private GeobellsDataManager dataManager;
    private GeobellsPreferenceManager preferenceManager;

    private long lastLocationPollingReset;

    public int getActivity() {
        return this.activity;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preferenceManager = new GeobellsPreferenceManager(this);
        dataManager = new GeobellsDataManager(this);
        reminders = dataManager.getSavedReminders();

        boolean disabled = preferenceManager.isDisabled();
        boolean showNotification = preferenceManager.isShowBackgroundNotificationEnabled();

        if (!disabled) {
            if (intent != null) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    activity = bundle.getInt(Constants.EXTRA_ACTIVITY);
                }
            }
            startLocationListening();

            if (showNotification) {
                makeForegroundNotification(getString(R.string.notification_listening_title), getString(R.string.notification_listening_message));
            }
        } else {
            makeForegroundNotification(getString(R.string.notification_disabled_title), getString(R.string.notification_disabled_message));
        }

        return START_STICKY;
    }

    public class LocationBinder extends Binder {
        public LocationBinder() {
        }

        LocationService getService() {
            return LocationService.this;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (locationClient != null && locationRequest != null) {
            locationClient.requestLocationUpdates(locationRequest, this);
        }
    }

    @Override
    public void onDestroy() {
        if (locationClient != null) {
            locationClient.disconnect();
        }
    }

    @Override
    public void onDisconnected() {
        locationClient = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            makeUseOfLocation(location);
        }
        // If we've waited long enough between requests
        if (System.currentTimeMillis() - lastLocationPollingReset > locationRequest.getInterval()) {
            if(preferenceManager != null && preferenceManager.isLowPowerEnabled()) {
                startLocationListening();
            }
        }
    }

    public void makeForegroundNotification(String title, String message) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setContentIntent(pendingIntent);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setOnlyAlertOnce(true);
        startForeground(1, builder.build());
    }

    public void makeUseOfLocation(Location currentLocation) {
        for (int reminderIndex = 0; reminderIndex < reminders.size(); reminderIndex++) {

            Reminder reminder = reminders.get(reminderIndex);

            if (!reminder.completed) {
                // Get the current day. If the current day is one of the days to remind, then proceed, otherwise don't.
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_WEEK) - 1; // -1 to make it zero based indexing: 0 = Sunday, 6 = Saturday, etc

                if (reminder.days[day]) {

                    int triggerDistance = reminder.proximity;
                    int type = reminder.type;

                    if (type == Constants.TYPE_DYNAMIC) {
                        ArrayList<Place> places = reminder.places;
                        for (int placeIndex = 0; placeIndex < reminder.places.size(); placeIndex++) {
                            Place place = places.get(placeIndex);

                            Location placeLocation = new Location("");
                            placeLocation.setLatitude(place.latitude);
                            placeLocation.setLongitude(place.longitude);

                            double placeDistance = currentLocation.distanceTo(placeLocation);
                            if (placeDistance <= triggerDistance) {
                                handleTrigger(currentLocation, reminderIndex, placeIndex);
                                break;
                            }
                        }
                    } else if (type == Constants.TYPE_FIXED) {
                        Location reminderLocation = new Location("");

                        reminderLocation.setLatitude(reminder.latitude);
                        reminderLocation.setLongitude(reminder.longitude);

                        double reminderDistance = currentLocation.distanceTo(reminderLocation);
                        if (reminderDistance <= triggerDistance) {
                            if (reminder.transition == Constants.TRANSITION_ENTER) {
                                handleTrigger(currentLocation, reminderIndex, 0);
                            } else if (reminder.transition == Constants.TRANSITION_EXIT) {
                                reminder.transition = Constants.TRANSITION_ENTER_TO_EXIT;
                                dataManager.saveReminders(reminders);
                                reminders = dataManager.getSavedReminders();
                            }
                        } else if (reminderDistance > triggerDistance) {
                            if (reminder.transition == Constants.TRANSITION_ENTER_TO_EXIT) {
                                handleTrigger(currentLocation, reminderIndex, 0);
                            }
                        }
                    }
                }
            } else {
                // Reminder completed. Check if it's a new day since the time the reminder was completed. If it is and the reminder is set to repeat, reset.
                long timeCompleted = reminder.timeCompleted;
                long currentTime = System.currentTimeMillis();

                Date completedDate = new Date(timeCompleted);
                Date currentDate = new Date(currentTime);

                Calendar completedCalendar = Calendar.getInstance();
                completedCalendar.setTime(completedDate);

                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.setTime(currentDate);

                boolean sameDay = completedCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                        completedCalendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR);

                if (!sameDay && reminder.repeat) {
                    reminder.completed = false;
                    dataManager.saveReminders(reminders);
                }
            }
        }

    }


    public void handleTrigger(Location currentLocation, int reminderIndex, int placeIndex) {
        Reminder reminder = reminders.get(reminderIndex);

        reminder.completed = true;
        reminder.timeCompleted = System.currentTimeMillis();
        dataManager.saveReminders(reminders);
        reminders = dataManager.getSavedReminders();

        String notificationTitle = reminder.title;
        String notificationMessage = "";
        double reminderLatitude = 0;
        double reminderLongitude = 0;
        if (reminder.type == Constants.TYPE_FIXED) {
            notificationMessage = reminder.address;
            reminderLatitude = reminder.latitude;
            reminderLongitude = reminder.longitude;
        } else if (reminder.type == Constants.TYPE_DYNAMIC) {
            Place place = reminder.places.get(placeIndex);
            notificationMessage = place.title;
            reminderLatitude = place.latitude;
            reminderLongitude = place.longitude;
        }

        Uri ringtoneUri = Uri.parse(preferenceManager.getNotificationSoundUri());
        PendingIntent directionsIntent = makeDirectionsIntent(currentLocation, reminderLatitude, reminderLongitude);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(notificationTitle);
        builder.setAutoCancel(true);
        builder.setContentText(notificationMessage);
        builder.setSound(ringtoneUri);
        builder.setDefaults(-1);
        builder.addAction(R.drawable.ic_action_map, getString(R.string.notification_action_navigate), directionsIntent);
        builder.setContentIntent(getViewReminderIntent(reminderIndex));
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(Constants.NOTIFICATION_ID, builder.build());

        if (preferenceManager.isPopupReminderEnabled()) {
            showPopup(reminderIndex);
        }

        if (reminder.silencePhone) {
            silencePhone();
        } else if (preferenceManager.isVoiceReminderEnabled()) {
            String textToSpeak = makeSpeakText(notificationTitle, notificationMessage, reminder.transition);
            Intent intent = new Intent(this, SpeakService.class);
            intent.putExtra(Constants.EXTRA_SPEAKTEXT, textToSpeak);
            startService(intent);
        }

        if (reminder.toggleAirplane) {
            toggleAirplaneMode();
        }
    }

    public String makeSpeakText(String title, String message, int transition) {
        // Make text to speak
        String totalText = "";
        String[] words = message.split(" ");
        String backerText = "";
        if (words.length <= 3) {
            backerText = message;
        } else {
            int count = 0;
            for (String word : words) {
                if (count < 4) {
                    count++;
                    backerText = backerText + word + " ";
                }
            }
            backerText = backerText.substring(0, backerText.length() - 1);
        }
        if (transition == Constants.TRANSITION_ENTER) {
            totalText = getString(R.string.notification_message_approaching) + " " + backerText + ", " + getString(R.string.notification_message_dontforget) + " " + title;
        } else {
            totalText = getString(R.string.notification_message_exiting) + " " + backerText + ", " + getString(R.string.notification_message_dontforget) + " " + title;
        }
        return totalText;
    }

    public PendingIntent getViewReminderIntent(int reminderIndex) {
        Intent viewReminderIntent = new Intent(this, ViewReminderActivity.class);
        viewReminderIntent.putExtra(Constants.EXTRA_REMINDER_INDEX, reminderIndex);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(viewReminderIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public PendingIntent makeDirectionsIntent(Location currentLocation, double reminderLatitude, double reminderLongitude) {
        Intent directionsIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://maps.google.com/maps?saddr=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&daddr=" + reminderLatitude + "," + reminderLongitude));
        directionsIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        return PendingIntent.getActivity(this, 0, directionsIntent, 0);
    }

    public void showPopup(int index) {
        Intent intent = new Intent(this, ViewReminderActivity.class);
        intent.putExtra(Constants.EXTRA_REMINDER_INDEX, index);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void silencePhone() {
        ((AudioManager) getSystemService(AUDIO_SERVICE)).setRingerMode(0);
    }

    public void toggleAirplaneMode() {
        if (Build.VERSION.SDK_INT >= 17) {
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            } else {
                wifiManager.setWifiEnabled(false);
            }
        } else {
            boolean isEnabled = Settings.System.getInt(
                    getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) == 1;
            Settings.System.putInt(
                    getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, isEnabled ? 0 : 1);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", !isEnabled);
            sendBroadcast(intent);
        }
    }

    public int findLargestReminderProximitySetting() {
        ArrayList<Reminder> remindersList = dataManager.getUpcomingReminders();
        int largestProximity = 0;
        for (Reminder reminder : remindersList) {
            if (reminder.proximity > largestProximity) {
                largestProximity = reminder.proximity;
            }
        }
        return largestProximity;
    }

    public double findClosestReminderDistance(Location currentLocation) {
        ArrayList<Reminder> remindersList = dataManager.getUpcomingReminders();
        double closestDistance = Double.MAX_VALUE;
        for (Reminder reminder : remindersList) {
            if (reminder.type == Constants.TYPE_FIXED) {
                Location reminderLocation = new Location("");
                reminderLocation.setLatitude(reminder.latitude);
                reminderLocation.setLongitude(reminder.longitude);
                double distanceTo = currentLocation.distanceTo(reminderLocation);
                if (distanceTo < closestDistance) {
                    closestDistance = distanceTo;
                }
            } else if (reminder.type == Constants.TYPE_DYNAMIC) {
                for (Place place : reminder.places) {
                    Location placeLocation = new Location("");
                    placeLocation.setLatitude(place.latitude);
                    placeLocation.setLongitude(place.longitude);
                    double distanceTo = currentLocation.distanceTo(placeLocation);
                    if (distanceTo < closestDistance) {
                        closestDistance = distanceTo;
                    }
                }
            }
        }
        return closestDistance;
    }

    public void startLocationListening() {
        lastLocationPollingReset = System.currentTimeMillis();
        boolean lowPowerEnabled = preferenceManager.isLowPowerEnabled(); // Is low power enabled?
        double multiplier = preferenceManager.getIntervalMultiplier();   // Multiplier specified in settings

        boolean startLocationListening = true;                           // Whether or not to start location listening.

        int numReminders = dataManager.getUpcomingReminders().size();

        if (numReminders == 0) {
            startLocationListening = false;
        }

        if(lowPowerEnabled) {
            Location currentLocation = null;
            if (locationClient != null && locationClient.isConnected()) {
                currentLocation = locationClient.getLastLocation();
            }
            if (currentLocation != null) {
                double closestReminderDistance = findClosestReminderDistance(currentLocation);
                int largestProximitySetting = findLargestReminderProximitySetting();
                // If we're 10x the distance from the largest reminder proximity setting
                // Ex: Largest proximity setting is 1/2 mi, and we're more than 2.5 miles away
                // Don't bother polling location (we're too far away).
                if (largestProximitySetting * 8 < closestReminderDistance) {
                    startLocationListening = false;
                }
            }
        }

        long interval = Constants.BASE_POLLING_INTERVAL;

        interval *= multiplier;
        if (lowPowerEnabled) {
            interval *= Constants.MULTIPLIER_LOW_POWER;
        }
        // If the activity is actually known
        if (activity >= 0) {
            interval *= Constants.ACTIVITY_MULTIPLIERS[activity];
        } else {
            interval *= Constants.ACTIVITY_MULTIPLIER_DEFAULT;
        }

        // If the interval is negative (can happen because of Constants.ACTIVITY_MULTIPLIERS)
        if (interval <= 0) {
            startLocationListening = false;
        }

        if (startLocationListening) {
            locationRequest = LocationRequest.create();
            if (lowPowerEnabled) {
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            } else {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            }
            locationRequest.setInterval(interval);
            locationRequest.setFastestInterval(interval / 50);
            if (locationClient != null) {
                if (locationClient.isConnected()) {
                    locationClient.removeLocationUpdates(this);
                    locationClient.requestLocationUpdates(locationRequest, LocationService.this);
                } else {
                    locationClient.connect();
                }
            } else {
                locationClient = new LocationClient(this, this, this);
                locationClient.connect();
            }
        } else {
            if (locationClient != null && locationClient.isConnected()) {
                locationClient.removeLocationUpdates(this);
            }
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
