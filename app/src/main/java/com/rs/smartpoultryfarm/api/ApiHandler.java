package com.rs.smartpoultryfarm.api;

import android.content.Context;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.GsonBuilder;
import com.rs.smartpoultryfarm.model.Feed;

public class ApiHandler {

    /** Default Credentials **/
    public static String DATA_CHANNEL_ID = "1562231";
    public static String DATA_READ_API_KEY = "ET9H1SY3HR9ZNLL5";
    public static String CONTROLLER_CHANNEL_ID = "1567504";
    public static String CONTROLLER_READ_API_KEY = "X1QJYT5YGVL6L6G1";
    public static String CONTROLLER_WRITE_API_KEY = "D3ZV4DMFD1FZGGFG";

    public static String feedsUrl(String id, String key, int limit) {
        return "https://api.thingspeak.com/channels/" + id + "/feeds.json?api_key=" + key + "&results=" + limit;
    }

    public static String singleFeedUrl(String id, String key) {
        return "https://api.thingspeak.com/channels/" + id + "/feeds/last.json?api_key=" + key;
    }

    public static String updateControllerFeedURL(String key, Feed feed) {
        return "https://api.thingspeak.com/update.json"
                + "?api_key=" + key
                + "&field1=" + feed.getField1()
                + "&field2=" + feed.getField2()
                + "&field3=" + feed.getField3()
                + "&field4=" + feed.getField4();
    }

    public static <T> void invoke(Context context, Class<T> type, int method, String url, OnDataListener<T> listener) {
        StringRequest stringRequest = new StringRequest(method, url,
                response -> {
                    try {
                        listener.onData(new GsonBuilder().create().fromJson(response, type));
                    }
                    catch (Exception exp){
                        listener.onError();
                        exp.printStackTrace();
                    }
                },
                error -> {
                    listener.onError();
                    error.printStackTrace();
                });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public interface OnDataListener<T> {
        void onData(T data);
        void onError();
    }
}
