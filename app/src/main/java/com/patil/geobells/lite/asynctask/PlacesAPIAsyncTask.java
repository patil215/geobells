package com.patil.geobells.lite.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.patil.geobells.lite.R;
import com.patil.geobells.lite.data.Photo;
import com.patil.geobells.lite.data.Place;
import com.patil.geobells.lite.utils.Config;
import com.patil.geobells.lite.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


public class PlacesAPIAsyncTask extends AsyncTask<String, String, ArrayList<Place>> {
    private AsyncTaskCompleteListener<String> callback;
    private ProgressDialog dialog;
    private Context context;
    private String method;

    public PlacesAPIAsyncTask(AsyncTaskCompleteListener<String> callback, Context context, String method) {
        this.callback = callback;
        this.context = context;
        this.method = method;
    }

    @Override
    protected ArrayList<Place> doInBackground(String... data) {
        String json = getPlaceJson(data[0], data[1], data[2]);
        ArrayList<Place> places = parseJsonForPlaces(json);
        return places;
    }

    public ArrayList<Place> parseJsonForPlaces(String json) {
        ArrayList<Place> places = new ArrayList<Place>();
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(json);
            JSONArray resultsJsonArray = jsonObj.getJSONArray("results");

            // Extract the Place descriptions from the results
            for (int i = 0; i < resultsJsonArray.length(); i++) {
                JSONObject jsonResult = resultsJsonArray.getJSONObject(i);
                String title = jsonResult.getString("name");
                String address = jsonResult.getString("formatted_address");
                double latitude = jsonResult.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                double longitude = jsonResult.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                String iconURL = jsonResult.getString("icon");
                String id = jsonResult.getString("id");
                String placeID = jsonResult.getString("place_id");
                String reference = jsonResult.getString("reference");
                Photo[] photos = new Photo[0];
                if(jsonResult.has("photos")) {
                    photos = new Photo[jsonResult.getJSONArray("photos").length()];
                    JSONArray photosJsonArray = jsonResult.getJSONArray("photos");
                    for(int d = 0; d < photosJsonArray.length(); d++) {
                        JSONObject photoJson = photosJsonArray.getJSONObject(d);
                        Photo photo = new Photo();
                        photo.height = photoJson.getInt("height");
                        photo.width = photoJson.getInt("width");
                        photo.photoReference = photoJson.getString("photo_reference");
                        photos[d] = photo;
                    }
                }
                Place place = new Place();
                place.title = title;
                place.address = address;
                place.latitude = latitude;
                place.longitude = longitude;
                place.iconURL = iconURL;
                place.id = id;
                place.placeID = placeID;
                place.reference = reference;
                place.photos = photos;
                places.add(place);
            }
        } catch (JSONException e) {
            Log.e("PlacesAPIAsyncTask", "Cannot process JSON results", e);
        }
        return places;
    }


    public String getPlaceJson(String query, String latitude, String longitude) {
        ArrayList<String> resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(Constants.PLACES_API_BASE + Constants.PLACES_TYPE_TEXTSEARCH + Constants.PLACES_OUT_JSON);
            if(Config.IS_LITE_VERSION) {
                sb.append("?key=" + Constants.PLACES_API_KEY_SEARCH_LITE);
            } else {
                sb.append("?key=" + Constants.PLACES_API_KEY_SEARCH_PRO);
            }
            sb.append("&location=" + latitude + "," + longitude);
            sb.append("&radius=50000");
            sb.append("&query=" + URLEncoder.encode(query, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e("PlacesAPIAsyncTask", "Error processing Places API URL", e);
            return "{}";
        } catch (IOException e) {
            Log.e("PlacesAPIAsyncTask", "Error connecting to Places API", e);
            return "{}";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return jsonResults.toString();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        if(method.equals(Constants.METHOD_PLACES_DIALOG_ADDRESS)) {
            dialog.setMessage(context.getString(R.string.message_searching));
        } else if(method.equals(Constants.METHOD_PLACES_CREATE)) {
            dialog.setMessage(context.getString(R.string.message_creating));
        } else if (method.equals(Constants.METHOD_PLACES_DIALOG_VIEW)) {
            dialog.setMessage(context.getString(R.string.message_viewing));
        }
        dialog.show();
    }

    @Override
    protected void onPostExecute(ArrayList<Place> places) {
        dialog.dismiss();
        callback.onPlacesTaskComplete(places, method);
    }
}
