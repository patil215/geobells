package com.patil.geobells.lite.asynctask;

import com.patil.geobells.lite.data.Place;

import java.util.ArrayList;

public interface PlacesAsyncTaskCompleteListener<String> {
    public void onTaskComplete(ArrayList<Place> places, String method);
}
