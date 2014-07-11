package com.patil.geobells.lite;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.patil.geobells.lite.R;
import com.patil.geobells.lite.data.Place;
import com.patil.geobells.lite.data.Reminder;
import com.patil.geobells.lite.utils.Constants;
import com.patil.geobells.lite.utils.GeobellsDataManager;

import java.util.ArrayList;

public class ViewRemindersMapActivity extends Activity {

    GeobellsDataManager dataManager;
    GoogleMap mapView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reminders_map);
        dataManager = new GeobellsDataManager(this);
        ArrayList<Reminder> reminders = dataManager.getSavedReminders();
        mapView = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        ArrayList<Marker> markers = new ArrayList<Marker>();
        for(int i = 0; i < reminders.size(); i++) {
            Reminder reminder = reminders.get(i);
            if(!reminder.completed) {
                if(reminder.type == Constants.TYPE_FIXED) {
                    LatLng markerPosition = new LatLng(reminder.latitude, reminder.longitude);
                    Marker marker = mapView.addMarker(new MarkerOptions().title(reminder.title).snippet(reminder.address).position(markerPosition).icon(BitmapDescriptorFactory.defaultMarker(indexToHue(i, reminders.size()))));
                    markers.add(marker);
                } else {
                    for(Place place : reminder.places) {
                        LatLng markerPosition = new LatLng(place.latitude, place.longitude);
                        Marker marker = mapView.addMarker(new MarkerOptions().title(reminder.title).snippet(reminder.business).position(markerPosition).icon(BitmapDescriptorFactory.defaultMarker(indexToHue(i, reminders.size()))));
                        markers.add(marker);
                    }
                }
            }
        }
        final ArrayList<Marker> finalMarkers = markers;
        mapView.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if(finalMarkers.size() == 0) {

                } else if(finalMarkers.size() == 1) {
                    mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(finalMarkers.get(0).getPosition().latitude, finalMarkers.get(0).getPosition().longitude), 11));
                } else {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Marker marker : finalMarkers) {
                        builder.include(marker.getPosition());
                    }
                    LatLngBounds bounds = builder.build();
                    int padding = 30; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mapView.moveCamera(cu);
                }
            }
        });
    }

    public float indexToHue(int index, int total) {
        double ratio = (double)index / total;
        return (float) ratio * 360;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_reminders_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
