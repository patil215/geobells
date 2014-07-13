package com.patil.geobells.lite.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.patil.geobells.lite.MainActivity;
import com.patil.geobells.lite.R;
import com.patil.geobells.lite.data.Place;
import com.patil.geobells.lite.data.Reminder;
import com.patil.geobells.lite.utils.Constants;
import com.patil.geobells.lite.utils.GeobellsDataManager;
import com.patil.geobells.lite.utils.GeobellsPreferenceManager;

import java.util.ArrayList;

public class LocationService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    int activity = -2;
    private final IBinder binder = new LocationBinder();
    int currentVolume;
    ArrayList<Reminder> reminders;
    String lastEventFile;
    LocationClient locationClient = null;
    LocationRequest locationRequest;
    GeobellsDataManager dataManager;
    GeobellsPreferenceManager preferenceManager;

    public int getActivity() {
        return this.activity;
    }

    public void makeForeground() {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentText(getString(R.string.notification_listening_title));
        builder.setContentTitle(getString(R.string.notification_listening_message));
        builder.setContentIntent(pendingIntent);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setOnlyAlertOnce(true);
        startForeground(Constants.NOTIFICATION_LISTENING_ID, builder.build());
    }

    public void makeUseOfLocation(Location currentLocation) {
        for (int reminderIndex = 0; reminderIndex < reminders.size(); reminderIndex++) {
            Reminder reminder = reminders.get(reminderIndex);
            if (!reminder.completed) {
                int triggerDistance = reminder.proximity;
                int type = reminder.type;
                if (type == Constants.TYPE_DYNAMIC) {
                    ArrayList<Place> places = reminder.places;
                    for (int placeIndex = 0; placeIndex < places.size(); placeIndex++) {
                        Place place = places.get(placeIndex);
                        Location placeLocation = new Location("");
                        placeLocation.setLatitude(place.latitude);
                        placeLocation.setLongitude(place.longitude);
                        double placeDistance = currentLocation.distanceTo(placeLocation);
                        if (placeDistance <= triggerDistance) {
                            reminder.completed = true;
                            reminder.timeCompleted = System.currentTimeMillis();
                            dataManager.saveReminders(reminders);
                            reminders = dataManager.getSavedReminders();
                            sendNotification(reminder.title, place.title, Constants.TRANSITION_ENTER, currentLocation, place.latitude, place.longitude, reminder.silencePhone);
                            if (reminder.toggleAirplane) {
                                toggleAirplaneMode();
                            }
                            if (reminder.silencePhone) {
                                silencePhone();
                            }
                        }
                    }
                } else if (type == Constants.TYPE_FIXED) {
                    Location reminderLocation = new Location("");
                    reminderLocation.setLatitude(reminder.latitude);
                    reminderLocation.setLongitude(reminder.longitude);
                    double reminderDistance = currentLocation.distanceTo(reminderLocation);
                    if (reminderDistance <= triggerDistance) {
                        if (reminder.transition == Constants.TRANSITION_ENTER) {
                            reminder.completed = true;
                            reminder.timeCompleted = System.currentTimeMillis();
                            dataManager.saveReminders(reminders);
                            reminders = dataManager.getSavedReminders();
                            sendNotification(reminder.title, reminder.address, Constants.TRANSITION_ENTER, currentLocation, reminder.latitude, reminder.longitude, reminder.silencePhone);
                            if (reminder.toggleAirplane) {
                                toggleAirplaneMode();
                            }
                            if (reminder.silencePhone) {
                                silencePhone();
                            }
                        } else if (reminder.transition == Constants.TRANSITION_EXIT) {
                            reminder.transition = Constants.TRANSITION_ENTER_TO_EXIT;
                            dataManager.saveReminders(reminders);
                            reminders = dataManager.getSavedReminders();
                        }
                    } else if (reminderDistance > triggerDistance) {
                        if (reminder.transition == Constants.TRANSITION_ENTER_TO_EXIT) {
                            reminder.completed = true;
                            reminder.timeCompleted = System.currentTimeMillis();
                            reminder.transition = Constants.TRANSITION_EXIT;
                            dataManager.saveReminders(reminders);
                            reminders = dataManager.getSavedReminders();
                            sendNotification(reminder.title, reminder.address, Constants.TRANSITION_EXIT, currentLocation, reminder.latitude, reminder.longitude, reminder.silencePhone);
                            if (reminder.toggleAirplane) {
                                toggleAirplaneMode();
                            }
                            if (reminder.silencePhone) {
                                silencePhone();
                            }
                        }
                    }
                }
            }
        }
    }

    public LocationService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (locationClient != null && locationRequest != null) {
            locationClient.requestLocationUpdates(locationRequest, LocationService.this);
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
        makeUseOfLocation(location);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (locationClient != null && locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
            preferenceManager = new GeobellsPreferenceManager(this);
            dataManager = new GeobellsDataManager(this);
            reminders = dataManager.getSavedReminders();
            boolean showNotification = preferenceManager.isShowBackgroundNotificationEnabled();
            if (reminders.size() > 0) {
                if (intent == null) {
                    startLocationListening(Constants.POLLING_INTERVAL_DEFAULT);
                } else {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        int intentActivity = bundle.getInt(Constants.EXTRA_ACTIVITY);
                        activity = intentActivity;
                        switch (intentActivity) {
                            case Constants.ACTIVITY_STANDING:
                                startLocationListening(Constants.POLLING_INTERVAL_STANDING);
                                break;
                            case Constants.ACTIVITY_BIKING:
                                startLocationListening(Constants.POLLING_INTERVAL_BIKING);
                                break;
                            case Constants.ACTIVITY_WALKING:
                                startLocationListening(Constants.POLLING_INTERVAL_WALKING);
                                break;
                            case Constants.ACTIVITY_DRIVING:
                                startLocationListening(Constants.POLLING_INTERVAL_DRIVING);
                                break;
                            case Constants.ACTIVITY_UNKNOWN:
                                startLocationListening(Constants.POLLING_INTERVAL_UNKNOWN);
                                break;
                            case Constants.ACTIVITY_TILTING:
                                startLocationListening(Constants.POLLING_INTERVAL_TILTING);
                                break;
                        }
                    }
                }
            }
            if (showNotification) {
                makeForeground();
            }
        }
        return START_STICKY;
    }

    public void sendNotification(String title, String message, int transition, Location currentLocation, double latitude, double longitude, boolean silencePhone) {
        String ringtone = PreferenceManager.getDefaultSharedPreferences(this).getString("preference_notification_sound", "default ringtone");
        Intent mapsIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://maps.google.com/maps?saddr=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&daddr=" + latitude + "," + longitude));
        mapsIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mapsIntent, 0);
        Uri ringtoneUri = Uri.parse(ringtone);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher).setContentTitle(title).setContentText(message).setSound(ringtoneUri).setDefaults(-1).addAction(R.drawable.ic_action_map, getString(R.string.notification_action_navigate), pendingIntent).setPriority(2);
        Intent classIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(classIntent);
        builder.setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(7, builder.build());

        // Make text to speak
        String totalText = "";
        String[] words = message.split(" ");
        String backerText = "";
        if (words.length <= 3) {
            backerText = message;
        } else {
            for (int i = 0; i < words.length; i++) {
                backerText = backerText + words[i] + " ";
            }
            backerText = backerText.substring(0, backerText.length() - 1);
        }

        if (transition == Constants.TRANSITION_ENTER) {
            totalText = getString(R.string.notification_message_approaching) + " " + backerText + ", " + getString(R.string.notification_message_dontforget) + " " + title;
        } else {
            totalText = getString(R.string.notification_message_exiting) + " " + backerText + ", " + getString(R.string.notification_message_dontforget) + " " + title;
        }
        if (preferenceManager.isPopupReminderEnabled()) {
            // TODO: showPopup();
        }
        if (silencePhone) {
            silencePhone();
        } else if (preferenceManager.isVoiceReminderEnabled()) {
            Intent intent = new Intent(this, SpeakService.class);
            intent.putExtra(Constants.EXTRA_SPEAKTEXT, totalText);
            startService(intent);
        }
    }

    public void silencePhone() {
        ((AudioManager) getSystemService(AUDIO_SERVICE)).setRingerMode(0);
    }

    public void startLocationListening(int interval) {
        boolean lowPowerEnabled = preferenceManager.isLowPowerEnabled();
        locationRequest = LocationRequest.create();
        if (lowPowerEnabled) {
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            locationRequest.setInterval(interval * 2);
            locationRequest.setFastestInterval(interval);
        } else {
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(interval);
            locationRequest.setFastestInterval(interval / 2);
        }
        locationClient = new LocationClient(this, this, this);
        locationClient.connect();
    }

    public void toggleAirplaneMode() {
        if (Build.VERSION.SDK_INT >= 17) {
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled())
                wifiManager.setWifiEnabled(true);
            else
                wifiManager.setWifiEnabled(false);
        } else {
            int j;
            if (Settings.System.getInt(getContentResolver(), "airplane_mode_on", 0) != 1)
                j = 0;
            else
                j = 1;
            ContentResolver localContentResolver = getContentResolver();
            int k;
            if (j == 0)
                k = 1;
            else
                k = 0;
            Settings.System.putInt(localContentResolver, "airplane_mode_on", k);
            Intent localIntent = new Intent("android.intent.action.AIRPLANE_MODE");
            boolean bool = false;
            if (j == 0)
                bool = true;
            localIntent.putExtra("state", bool);
            sendBroadcast(localIntent);
        }
    }

    public class LocationBinder extends Binder {
        public LocationBinder() {
        }

        LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
