package com.patil.geobells.lite.asynctask;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.patil.geobells.lite.R;
import com.patil.geobells.lite.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class ReverseGeocoderAPIAsyncTask extends AsyncTask<Double, String, String> {
    private AsyncTaskCompleteListener<String> callback;
    private ProgressDialog dialog;
    private Context context;
    private String method;

    public ReverseGeocoderAPIAsyncTask(AsyncTaskCompleteListener<String> callback, Context context, String method) {
        this.callback = callback;
        this.context = context;
        this.method = method;
    }

    @Override
    protected String doInBackground(Double... coords) {
        String json = getGeocodeJson(coords[0], coords[1]);
        String address = parseJsonForAddress(json);
        return address;
    }

    public String parseJsonForAddress(String json) {
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(json);
            String status = jsonObj.getString("status");
            if(status.equals(Constants.GEOCODE_STATUS_NORESULTS)) {
                return Constants.GEOCODE_REVERSE_RESPONSE_NORESULTS;
            }
            String address = jsonObj.getJSONArray("results").getJSONObject(0).getString("formatted_address");
            return address;
        } catch (JSONException e) {
            Log.e("PlacesAPIAsyncTask", "Cannot process JSON results", e);
        }
        return Constants.GEOCODE_REVERSE_RESPONSE_ERROR;
    }


    public String getGeocodeJson(Double latitude, Double longitude) {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(Constants.GEOCODE_API_BASE + Constants.GEOCODE_OUT_JSON);
            sb.append("?latlng=" + URLEncoder.encode(latitude + "," + longitude, "utf8"));

            Log.d("GeobellsReverseGeocodeTask", sb.toString());

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
        Log.d("GeobellsReverseGeocodeTask", jsonResults.toString());
        return jsonResults.toString();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        if(method.equals(Constants.METHOD_GEOCODE_CREATE)) {
            dialog.setMessage(context.getString(R.string.message_creating));
            dialog.show();
        }
    }

    @Override
    protected void onPostExecute(String address) {
        dialog.dismiss();
        callback.onReverseGeocodeTaskComplete(address, method);
    }
}
