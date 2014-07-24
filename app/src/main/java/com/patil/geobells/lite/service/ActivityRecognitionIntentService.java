package com.patil.geobells.lite.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.patil.geobells.lite.utils.Constants;
import com.patil.geobells.lite.utils.GeobellsPreferenceManager;

public class ActivityRecognitionIntentService extends IntentService {
    private GeobellsPreferenceManager preferenceManager = null;
    private boolean attemptingToBind = false;
    private boolean bound = false;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ActivityRecognitionIntentService.this.locationService = ((LocationService.LocationBinder) iBinder).getService();
            attemptingToBind = false;
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            ActivityRecognitionIntentService.this.locationService = null;
            bound = false;
        }
    };

    public void bindToService() {
        if(!attemptingToBind) {
            attemptingToBind = true;
            bindService(new Intent(this, LocationService.class), connection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unbindFromService() {
        attemptingToBind = false;
        if(bound) {
            unbindService(connection);
            bound = false;
        }
    }

    private LocationService locationService;

    public ActivityRecognitionIntentService() {
        super("ActivityRecognitionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            if(preferenceManager == null) {
                preferenceManager = new GeobellsPreferenceManager(this);
            }
            long timeSinceLastActivity = System.currentTimeMillis() - preferenceManager.getLastActivityRecognitionTime();
            long minTimeSinceLastActivity = Constants.POLLING_INTERVAL_ACTIVITY_RECOGNITION_MINIMUM;
            double multiplier = preferenceManager.getIntervalMultiplier();
            boolean lowPowerEnabled = preferenceManager.isLowPowerEnabled();
            if(lowPowerEnabled) {
                minTimeSinceLastActivity = (long)(minTimeSinceLastActivity * multiplier * Constants.MULTIPLIER_LOW_POWER);
            } else {
                minTimeSinceLastActivity = (long)(minTimeSinceLastActivity * multiplier);
            }
            if(timeSinceLastActivity > minTimeSinceLastActivity) {
                preferenceManager.saveLastActivityRecognitionTime(System.currentTimeMillis());
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                DetectedActivity mostProbableActivity = result.getMostProbableActivity();
                int activityType = mostProbableActivity.getType();

                bindToService();
                int currentActivity = -1;
                if (locationService != null) {
                    Log.d("BackgroundService", "LocationService successfully bound to ActivityRecognitionIntentService");
                    currentActivity = locationService.getActivity();
                }
                if (currentActivity == -1) {
                    Log.d("BackgroundService", "current activity not gotten, restarting LocationService");
                    Intent locationIntent = new Intent(this, LocationService.class);
                    locationIntent.putExtra(Constants.EXTRA_ACTIVITY, activityType);
                    unbindFromService();
                    stopService(new Intent(this, LocationService.class));
                    startService(locationIntent);
                } else if (currentActivity != activityType) {
                    Log.d("BackgroundService", "current activity different, restarting LocationService");
                    Intent locationIntent = new Intent(this, LocationService.class);
                    locationIntent.putExtra(Constants.EXTRA_ACTIVITY, activityType);
                    unbindFromService();
                    stopService(new Intent(this, LocationService.class));
                    startService(locationIntent);
                } else {
                    Log.d("BackgroundService", "No need to restart LocationService");
                }
                unbindFromService();
            } else {
                Log.d("BackgroundService", "Not enough time elapsed since last activity recognition time");
            }
        }
    }


}
