package com.patil.geobells.lite.utils;

import com.google.android.gms.maps.model.LatLng;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;

public class GeobellsUtils {
    public static String getRelativeTime(long timeMillis) {
        PrettyTime p = new PrettyTime();
        return p.format(new Date(timeMillis));
    }

    public static String constructMapImageURL(ArrayList<LatLng> markerPositions, int markerColor) {
        StringBuilder sb = new StringBuilder(Constants.STATIC_MAP_API_BASE);
        sb.append("?size=200x200");
        sb.append("&format=roadmap");
        sb.append("&scale=2");
        String strColor = String.format("#%06X", 0xFFFFCC & markerColor);
        String color = "0x" + strColor.substring(1, strColor.length());
        sb.append("&markers=color:" + color);
        for(LatLng markerPosition : markerPositions) {
            sb.append("|" + markerPosition.latitude + "," + markerPosition.longitude);
        }
        String url = sb.toString();
        return url;
    }
}
