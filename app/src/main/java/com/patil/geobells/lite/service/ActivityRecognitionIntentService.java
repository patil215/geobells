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
            ActivityRecognitionIntentService.this.locationService = ((LocationService.LocationBinder) iBinder).getService();
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
        if (ActivityRecognitionResult.hasResult(intent)) {
            bindService(new Intent(this, LocationService.class), connection, BIND_AUTO_CREATE);
            int currentActivity = -1;
            if(locationService != null) {
                Log.d("BackgroundService", "LocationService successfully bound to ActivityRecognitionIntentService");
                currentActivity = locationService.getActivity();
            }
            DetectedActivity detectedActivity = ActivityRecognitionResult.extractResult(intent).getMostProbableActivity();
            int type = detectedActivity.getType();
            if(currentActivity == -1) {
                Log.d("BackgroundService", "current activity not gotten, restarting LocationService");
                Intent locationIntent = new Intent(this, LocationService.class);
                locationIntent.putExtra(Constants.EXTRA_ACTIVITY, type);
                unbindService(connection);
                stopService(new Intent(this, LocationService.class));
                startService(locationIntent);
            } else if(currentActivity != type) {
                Log.d("BackgroundService", "current activity different, restarting LocationService");
                Intent locationIntent = new Intent(this, LocationService.class);
                locationIntent.putExtra(Constants.EXTRA_ACTIVITY, type);
                unbindService(connection);
                stopService(new Intent(this, LocationService.class));
                startService(locationIntent);
            } else {
                Log.d("BackgroundService", "No need to restart LocationService");
            }
        }
    }


}
