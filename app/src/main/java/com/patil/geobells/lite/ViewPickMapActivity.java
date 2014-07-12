package com.patil.geobells.lite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.patil.geobells.lite.R;
import com.patil.geobells.lite.asynctask.AsyncTaskCompleteListener;
import com.patil.geobells.lite.asynctask.GeocoderAPIAsyncTask;
import com.patil.geobells.lite.asynctask.ReverseGeocoderAPIAsyncTask;
import com.patil.geobells.lite.data.Place;
import com.patil.geobells.lite.utils.Constants;

import java.util.ArrayList;

public class ViewPickMapActivity extends Activity implements AsyncTaskCompleteListener<String> {

    GoogleMap mapView;
    LatLng lastTouchedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState)

    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pick_map);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mapView = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mapView.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                lastTouchedPosition = latLng;
                new ReverseGeocoderAPIAsyncTask(ViewPickMapActivity.this, ViewPickMapActivity.this, Constants.METHOD_REVERSE_GEOCODE_VIEW_MAP).execute(latLng.latitude, latLng.longitude);
            }
        });
        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra(Constants.EXTRA_REMINDER_LATITUDE, Constants.NO_REMINDER_LATITUDE);
        double longitude = intent.getDoubleExtra(Constants.EXTRA_REMINDER_LONGITUDE, Constants.NO_REMINDER_LONGITUDE);
        String address = intent.getStringExtra(Constants.EXTRA_REMINDER_ADDRESS);
        if(latitude == Constants.NO_REMINDER_LATITUDE) {

        } else if(latitude == Constants.INVALID_REMINDER_LATITUDE) {
            Toast.makeText(this, getString(R.string.toast_no_map_geocode_results_begin) + address + getString(R.string.toast_no_map_geocode_results_end), Toast.LENGTH_SHORT).show();
        } else {
            LatLng markerPosition = new LatLng(latitude, longitude);
            Marker marker = mapView.addMarker(new MarkerOptions().title(address).position(markerPosition));
            mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 11));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_pick_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPlacesTaskComplete(ArrayList<Place> places, String method) {

    }

    @Override
    public void onGeocodeTaskComplete(Double[] coords, String method) {

    }

    @Override
    public void onReverseGeocodeTaskComplete(String address, String method) {
        if(method.equals(Constants.METHOD_REVERSE_GEOCODE_VIEW_MAP)) {;
            Marker marker = mapView.addMarker(new MarkerOptions().title(address).position(lastTouchedPosition));
            mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(lastTouchedPosition, 11));
        }
    }
}
