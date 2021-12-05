package com.rs.smartpoultryfarm.util;

import android.widget.Toast;
import com.rs.smartpoultryfarm.controller.AppController;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateExtensions {

    public long getTime(String date) {
        if(date == null || date.trim().isEmpty()) return 0;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        long totalTime = 0;

        try {
            Date d = format.parse(date.replaceAll("T", " ").replaceAll("Z", ""));
            totalTime = d != null ? d.getTime() : 0;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return totalTime;
    }

    private final int SECOND_MILLIS = 1000;
    private final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private final int DAY_MILLIS = 24 * HOUR_MILLIS;
    public String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            Toast.makeText(AppController.getContext(), "nope", Toast.LENGTH_SHORT).show();
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1m ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + "m ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "1h ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + "h ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + "d ago";
        }
    }
}
