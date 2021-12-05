package com.rs.smartpoultryfarm.api;

public class API {

    public static String DATA_CHANNEL_ID = "1562231";
    public static String CONTROLLER_CHANNEL_ID = "1567504";
    public static String DATA_READ_API_KEY = "ET9H1SY3HR9ZNLL5";
    public static String CONTROLLER_READ_API_KEY = "X1QJYT5YGVL6L6G1";
    public static String CONTROLLER_WRITE_API_KEY = "D3ZV4DMFD1FZGGFG";

    public static String getDataFeedURL(int limit) {
        return "https://api.thingspeak.com/channels/" + DATA_CHANNEL_ID + "/feeds.json?api_key=" + DATA_READ_API_KEY + "&results=" + limit;
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
}
