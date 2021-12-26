package com.rs.smartpoultryfarm.api;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.GsonBuilder;
import com.rs.smartpoultryfarm.model.PoultryData;

public class API {
    public static String DATA_CHANNEL_ID = "1562231";
    public static String DATA_READ_API_KEY = "ET9H1SY3HR9ZNLL5";
    public static String CONTROLLER_CHANNEL_ID = "1567504";
    public static String CONTROLLER_READ_API_KEY = "X1QJYT5YGVL6L6G1";
    public static String CONTROLLER_WRITE_API_KEY = "D3ZV4DMFD1FZGGFG";

    public static String getDataFeedURL(String id, String key, int limit) {
        return "https://api.thingspeak.com/channels/" + id + "/feeds.json?api_key=" + key + "&results=" + limit;
    }

    public static String getControllerFeedURL() {
        return "https://api.thingspeak.com/channels/" + CONTROLLER_CHANNEL_ID + "/feeds/last.json?api_key=" + CONTROLLER_READ_API_KEY;
    }

    public static String updateControllerFeedURL(boolean lightOneState, boolean lightTwoState) {
        return "https://api.thingspeak.com/update"
                + "?api_key=" + CONTROLLER_WRITE_API_KEY
                + "&field1=" + (lightOneState ? "1" : "0")
                + "&field2=" + (lightTwoState ? "1" : "0");
    }

    public static void invoke(Context context, int method, String url, OnDataListener listener) {
        StringRequest stringRequest = new StringRequest(method, url,
                response -> listener.onData(new GsonBuilder().create().fromJson(response, PoultryData.class)),
                error -> {
                    listener.onData(null);
                    error.printStackTrace();
                });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public interface OnDataListener {
        void onData(PoultryData data);
    }
}
