package com.rs.smartpoultryfarm.util;

import android.content.Context;

import com.google.gson.Gson;
import com.rs.smartpoultryfarm.controller.AppController;
import com.rs.smartpoultryfarm.model.Contact;

/**
 *  Storing data locally
 **/
public class SharedPreference {

    private final       Context context;

    private final       String  FEED_DATA_SP_NAME = "feedData";
    private final       String  CHANNEL_DATA_SP_NAME = "channelData";
    private final       String  CONTROLLERS_STATE_SP_NAME = "controllersState";
    public static final String  HUMIDITY_VALUE_SP_KEY = "humidityValueKey";
    public static final String  TEMP_VALUE_SP_KEY = "temperatureValue";
    public static final String  AIR_QUALITY_VALUE_SP_KEY = "airQualityValueKey";
    public static final String  LIGHT_ONE_STATE_SP_KEY = "lightOeStateKey";
    public static final String  LIGHT_TWO_STATE_SP_KEY = "lightTwoStateKey";

    private final       String  AUTO_START_SP_NAME = "enableAutoStart";
    public static final String  AUTO_START_SP_KEY = "autoStarKey";

    private final       String EMERGENCY_CONTACT_SP_NAME = "emergencyContact";
    private final       String EMERGENCY_CONTACT_SP_KEY = "emergencyContactKey";

    private final String        LOG_IN_SP_NAME = "loginData";
    public static final String  CHANNEL_ID_SP_KEY = "channelId";
    public static final String  CHANNEL_KEY_SP_KEY = "channelKey";
    public static final String  LOGGED_IN_SP_KEY = "loggedInKey";


    public SharedPreference() {
        this.context = AppController.getContext();
    }

    public SharedPreference(Context context) {
        this.context = context;
    }

    public void feedData(String key, float value){
        key = channelData(CHANNEL_ID_SP_KEY) + "_" + key;
        context.getSharedPreferences(FEED_DATA_SP_NAME, Context.MODE_PRIVATE).edit().putFloat(key, value).apply();
    }

    public Double feedData(String key){
        key = channelData(CHANNEL_ID_SP_KEY) + "_" + key;
        return (double) context.getSharedPreferences(FEED_DATA_SP_NAME, Context.MODE_PRIVATE).getFloat(key,0);
    }

    public void channelData(String key, String value){
        context.getSharedPreferences(CHANNEL_DATA_SP_NAME, Context.MODE_PRIVATE).edit().putString(key, value).apply();
    }

    public String channelData(String key){
        return context.getSharedPreferences(CHANNEL_DATA_SP_NAME, Context.MODE_PRIVATE).getString(key,null);
    }

    public void controllersState(String key, boolean value){
        key = channelData(CHANNEL_ID_SP_KEY) + "_" + key;
        context.getSharedPreferences(CONTROLLERS_STATE_SP_NAME, Context.MODE_PRIVATE).edit().putBoolean(key, value).apply();
    }

    public boolean controllersState(String key){
        key = channelData(CHANNEL_ID_SP_KEY) + "_" + key;
        return context.getSharedPreferences(CONTROLLERS_STATE_SP_NAME, Context.MODE_PRIVATE).getBoolean(key,false);
    }

    public void allowAutoStart(boolean allow){
        context.getSharedPreferences(AUTO_START_SP_NAME, Context.MODE_PRIVATE).edit().putBoolean(SharedPreference.AUTO_START_SP_KEY, allow).apply();
    }

    public boolean isAutoStart(){
        return context.getSharedPreferences(AUTO_START_SP_NAME, Context.MODE_PRIVATE).getBoolean(SharedPreference.AUTO_START_SP_KEY,false);
    }

    public void storeEmergencyContact(Contact contact){
        String data = contact == null ? null : new Gson().toJson(contact);
        String key = channelData(CHANNEL_ID_SP_KEY) + "_" + EMERGENCY_CONTACT_SP_KEY;
        context.getSharedPreferences(EMERGENCY_CONTACT_SP_NAME, Context.MODE_PRIVATE).edit().putString(key, data).apply();
    }

    public Contact getEmergencyContact(){
        String key = channelData(CHANNEL_ID_SP_KEY) + "_" + EMERGENCY_CONTACT_SP_KEY;
        String contact = context.getSharedPreferences(EMERGENCY_CONTACT_SP_NAME, Context.MODE_PRIVATE).getString(key,null);
        return contact == null ? null : new Gson().fromJson(contact, Contact.class);
    }

    public void setLoggedIn(boolean state){
        context.getSharedPreferences(LOG_IN_SP_NAME, Context.MODE_PRIVATE).edit().putBoolean(LOGGED_IN_SP_KEY, state).apply();
    }

    public boolean isLoggedIn(){
        return context.getSharedPreferences(LOG_IN_SP_NAME, Context.MODE_PRIVATE).getBoolean(LOGGED_IN_SP_KEY,false);
    }
}
