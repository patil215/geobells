package com.patil.geobells.lite.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.patil.geobells.lite.CreateReminderActivity;
import com.patil.geobells.lite.R;

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


public class PlacesAPIDialogAsyncTask extends AsyncTask<String, String, String> {
    private CreateReminderActivity activity;
    private ProgressDialog dialog;
    private Context context;

    // Places API stuff
    final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    final String TYPE_AUTOCOMPLETE = "/autocomplete";
    final String TYPE_NEARBYSEARCH = "/nearbysearch";
    final String TYPE_TEXTSEARCH = "/textsearch";
    final String OUT_JSON = "/json";
    final String API_KEY = "AIzaSyCzEMbwj8vbLH8i1_QegjVd6B-3oFUFyp8";


    public PlacesAPIDialogAsyncTask(CreateReminderActivity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... data) {
        return attemptPlaceSearch(data[0], data[1], data[2]);
    }

    public String attemptPlaceSearch(String query, String latitude, String longitude) {

        ArrayList<String> resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_TEXTSEARCH + OUT_JSON);
            sb.append("?key=" + API_KEY);
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
            Log.e("Autocomplete", "Error processing Places API URL", e);
            return "{}";
        } catch (IOException e) {
            Log.e("Autocomplete", "Error connecting to Places API", e);
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
        dialog.setMessage(context.getString(R.string.message_searching));
        dialog.show();
    }

    @Override
    protected void onPostExecute(String response) {
        dialog.dismiss();
        activity.handleResponseJsonForDialog(response);
    }
}
