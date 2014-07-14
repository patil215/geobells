package com.patil.geobells.lite.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.patil.geobells.lite.utils.Constants;

public class ActivityRecognitionIntentService extends IntentService {
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ActivityRecognitionIntentService.this.locationService = ((LocationService.LocationBinder)iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            ActivityRecognitionIntentService.this.locationService = null;
        }
    };

    LocationService locationService;

    public ActivityRecognitionIntentService() {
        super("ActivityRecognitionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            Log.d("BackgroundService", "Got activity");
            bindService(new Intent(this, LocationService.class), this.connection, BIND_AUTO_CREATE);
            DetectedActivity detectedActivity = ActivityRecognitionResult.extractResult(intent).getMostProbableActivity();
            int activityType = detectedActivity.getType();
            Log.d("BackgroundService", "Activity type is " + String.valueOf(activityType));
            Intent startIntent = new Intent(this, LocationService.class);
            startIntent.putExtra(Constants.EXTRA_ACTIVITY, activityType);
            unbindService(connection);
            Log.d("BackgroundService", "Restarting LocationService");
            stopService(new Intent(this, LocationService.class));
            startService(startIntent);
        }
    }
}
