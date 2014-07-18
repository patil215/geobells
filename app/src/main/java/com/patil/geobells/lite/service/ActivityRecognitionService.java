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
    boolean inProgress;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BackgroundService", "onStartCommand for ActivityRecognitionService");
        inProgress = false;
        if (activityRecognitionClient != null) {
            activityRecognitionClient.removeActivityUpdates(activityRecognitionIntent);
        }
        Log.d("BackgroundService", "Registering activity recognition client");
        activityRecognitionClient = new ActivityRecognitionClient(this, this, this);
        Intent activityIntent = new Intent(this, ActivityRecognitionIntentService.class);
        activityRecognitionIntent = PendingIntent.getService(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if(!inProgress) {
            inProgress = true;
            activityRecognitionClient.connect();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("BackgroundService", "Activity recognition client connected");
        activityRecognitionClient.requestActivityUpdates(Constants.POLLING_INTERVAL_ACTIVITY_RECOGNITION, activityRecognitionIntent);
        inProgress = false;
        activityRecognitionClient.disconnect();
        stopSelf();
    }

    @Override
    public void onDisconnected() {
        Log.d("BackgroundService", "Activity recognition client disconnected");
        inProgress = false;
        activityRecognitionClient = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("BackgroundService", "Activity recognition client connection failed");
    }
}
