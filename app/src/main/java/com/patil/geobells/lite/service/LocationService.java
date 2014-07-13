package com.patil.geobells.lite.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.games.internal.constants.NotificationChannel;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.patil.geobells.lite.MainActivity;
import com.patil.geobells.lite.R;
import com.patil.geobells.lite.data.Place;
import com.patil.geobells.lite.data.Reminder;
import com.patil.geobells.lite.utils.Constants;

import java.util.ArrayList;

public class LocationService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    int activity = -2;
    private final IBinder binder = new LocationBinder();
    int currentVolume;
    ArrayList<Reminder> reminders;
    String lastEventFile;
    LocationClient locationClient = null;
    LocationRequest locationRequest;

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

    public void makeUseOfLocation(Location location) {
        for(int reminderIndex = 0; reminderIndex < reminders.size(); reminderIndex++) {
            Reminder reminder  = reminders.get(reminderIndex);
            int distance = reminder.proximity;
            int transition = reminder.transition;
            int type = reminder.type;
            if(type == Constants.TYPE_DYNAMIC) {
                ArrayList<Place> places = reminder.places;
                for(int placeIndex = 0; placeIndex < places.size(); placeIndex++) {
                    Place place = places.get(placeIndex);
                }
            }
        }
    }

    public LocationService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
