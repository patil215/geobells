package com.patil.geobells.lite.utils;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

public class GeobellsUtils {
    public static String getRelativeTime(long timeMillis) {
        PrettyTime p = new PrettyTime();
        return p.format(new Date(timeMillis));
    }
}
