package com.patil.geobells.lite.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;

public class ActivityRecognitionService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    ActivityRecognitionClient activityRecognitionClient;
    PendingIntent activityRecognitionIntent;

    public ActivityRecognitionService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.activityRecognitionIntent = PendingIntent.getService(this, 0, new Intent(this, ActivityRecognitionIntentService.class), 134217728);
        if (this.activityRecognitionClient != null)
            this.activityRecognitionClient.removeActivityUpdates(this.activityRecognitionIntent);
        startActivityListening();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(Bundle bundle) {
        activityRecognitionIntent = PendingIntent.getService(this, 0, new Intent(this, ActivityRecognitionIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        activityRecognitionClient.requestActivityUpdates(90000L, activityRecognitionIntent);
        activityRecognitionClient.disconnect();
        stopSelf();
    }

    public void startActivityListening() {
        activityRecognitionClient = new ActivityRecognitionClient(this, this, this);
        activityRecognitionClient.connect();
    }

    @Override
    public void onDisconnected() {
        activityRecognitionClient = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
