package com.patil.geobells.lite.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.patil.geobells.lite.utils.Constants;

public class ActivityRecognitionService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    ActivityRecognitionClient activityRecognitionClient;
    PendingIntent activityRecognitionIntent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BackgroundService", "onStartCommand for ActivityRecognitionService");
        activityRecognitionIntent = PendingIntent.getService(this, 0, new Intent(this, ActivityRecognitionIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        if (activityRecognitionClient != null) {
            activityRecognitionClient.removeActivityUpdates(activityRecognitionIntent);
        }
        startActivityListening();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("BackgroundService", "Activity recognition client connected");
        activityRecognitionIntent = PendingIntent.getService(this, 0, new Intent(this, ActivityRecognitionIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        activityRecognitionClient.requestActivityUpdates(Constants.POLLING_INTERVAL_ACTIVITY_RECOGNITION, activityRecognitionIntent);
        activityRecognitionClient.disconnect();
        stopSelf();
    }

    public void startActivityListening() {
        Log.d("BackgroundService", "Registering activity recognition client");
        activityRecognitionClient = new ActivityRecognitionClient(this, this, this);
        activityRecognitionClient.connect();
    }

    @Override
    public void onDisconnected() {
        Log.d("BackgroundService", "Activity recognition client disconnected");
        activityRecognitionClient = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("BackgroundService", "Activity recognition client connection failed");
    }
}
