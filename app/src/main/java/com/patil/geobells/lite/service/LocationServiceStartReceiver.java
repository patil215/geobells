package com.patil.geobells.lite.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocationServiceStartReceiver extends BroadcastReceiver {
    public LocationServiceStartReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, LocationService.class));
        context.startService(new Intent(context, ActivityRecognitionService.class));
    }
}
