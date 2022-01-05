package com.rs.smartpoultryfarm.util;

import static com.rs.smartpoultryfarm.util.AppExtensions.string;

import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.model.Field;

import java.util.ArrayList;
import java.util.List;

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

    public static List<Field> fields() {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("field1", R.raw.temperature, string(R.string.temperature), string(R.string.temperatureUnit)));
        fields.add(new Field("field2", R.raw.humidity, string(R.string.humidity), string(R.string.humidityUnit)));
        fields.add(new Field("field3", R.raw.air_quality, string(R.string.airQuality), string(R.string.airQualityUnit)));
        fields.add(new Field("field4", R.raw.water_height, string(R.string.waterHeight), string(R.string.waterHeightUnit)));
        return fields;
    }
}
