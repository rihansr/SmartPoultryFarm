package com.rs.smartpoultryfarm.util;

public class Constants {

    /** Other **/
    public static String        TAG = "SPF";
    public static boolean       IS_NETWORK_CONNECTED = false;
    public static String        DATA_LISTENER_KEY = "dataListenerKey";
    public static String        DATA_INTENT_KEY = "dataIntentKey";
    public static String        CONTACT_BUNDLE_KEY = "contactBundleKey";

    /** Data Min Max Value **/
    public static final double  TEMPERATURE_MIN_VALUE = 65;
    public static final double  TEMPERATURE_MAX_VALUE = 95;
    public static final long    HUMIDITY_MIN_VALUE = 60;
    public static final long    HUMIDITY_MAX_VALUE = 80;
    public static final long    WATER_HEIGHT_MAX_VALUE = 100;

    /** Timer **/
    public static final long    SPLASH_TIME_OUT = 2000;
    public static final long    REFRESH_DELAY = 60000; /*1 min*/
}
