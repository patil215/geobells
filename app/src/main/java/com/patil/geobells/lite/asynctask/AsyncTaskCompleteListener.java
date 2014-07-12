package com.patil.geobells.lite.asynctask;

import com.patil.geobells.lite.data.Place;

import java.util.ArrayList;

public interface AsyncTaskCompleteListener<String> {
    public void onPlacesTaskComplete(ArrayList<Place> places, String method);
    public void onGeocodeTaskComplete(Double[] coords, String method);
    public void onReverseGeocodeTaskComplete(String address, String method);
}
