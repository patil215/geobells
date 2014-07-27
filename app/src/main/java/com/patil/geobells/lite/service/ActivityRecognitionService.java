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
import com.patil.geobells.lite.utils.GeobellsPreferenceManager;

public class ActivityRecognitionService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    private ActivityRecognitionClient activityRecognitionClient;
    private PendingIntent activityRecognitionIntent;
    private GeobellsPreferenceManager preferenceManager = null;
    private boolean inProgress;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        inProgress = false;
        if (activityRecognitionClient != null) {
            activityRecognitionClient.removeActivityUpdates(activityRecognitionIntent);
        }
        activityRecognitionClient = new ActivityRecognitionClient(this, this, this);
        Intent activityIntent = new Intent(this, ActivityRecognitionIntentService.class);
        activityRecognitionIntent = PendingIntent.getService(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (!inProgress) {
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
        if (preferenceManager == null) {
            preferenceManager = new GeobellsPreferenceManager(this);
        }
        long interval = Constants.POLLING_INTERVAL_ACTIVITY_RECOGNITION;
        double multiplier = preferenceManager.getIntervalMultiplier();
        boolean lowPowerEnabled = preferenceManager.isLowPowerEnabled();
        if (lowPowerEnabled) {
            long time = (long) (interval * multiplier * Constants.MULTIPLIER_LOW_POWER);
            activityRecognitionClient.requestActivityUpdates(time, activityRecognitionIntent);
        } else {
            long time = (long) (interval * multiplier);
            activityRecognitionClient.requestActivityUpdates(time, activityRecognitionIntent);

        }
        inProgress = false;
        activityRecognitionClient.disconnect();
        stopSelf();
    }

    @Override
    public void onDisconnected() {
        inProgress = false;
        activityRecognitionClient = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onDestroy() {
        if (activityRecognitionClient != null) {
            activityRecognitionClient.disconnect();
        }
        super.onDestroy();
    }
}
