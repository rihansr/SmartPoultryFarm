package com.rs.smartpoultryfarm.util;

import android.content.Context;
import com.google.gson.Gson;
import com.rs.smartpoultryfarm.controller.AppController;
import com.rs.smartpoultryfarm.model.Channel;
import com.rs.smartpoultryfarm.model.Contact;
import com.rs.smartpoultryfarm.model.Feed;

/**
 *  Storing data locally
 **/
public class SharedPreference {

    private final       Context context;

    private final       String  FEED_SP_NAME = "feedData";
    private final       String  CHANNEL_SP_NAME = "channelData";
    public static final String  POULTRY_FEED_SP_KEY = "poultryFeedKey";
    public static final String  CONTROLLER_FEED_SP_KEY = "controllerFeedKey";

    private final       String  AUTO_START_SP_NAME = "enableAutoStart";
    public static final String  AUTO_START_SP_KEY = "autoStartKey";

    private final       String  EMERGENCY_CONTACT_SP_NAME = "emergencyContact";
    private final       String  EMERGENCY_CONTACT_SP_KEY = "emergencyContactKey";

    private final       String  LOG_IN_SP_NAME = "loginData";
    public static final String  POULTRY_CHANNEL_SP_KEY = "poultryChannelKey";
    public static final String  CONTROLLER_CHANNEL_SP_KEY = "controllerChannelKey";
    public static final String  LOGGED_IN_SP_KEY = "loggedInKey";

    public SharedPreference() {
        this.context = AppController.getContext();
    }

    public SharedPreference(Context context) {
        this.context = context;
    }

    public void feedData(String key, Feed feed){
        key = channelData(POULTRY_CHANNEL_SP_KEY).getChannelId() + "_" + key;
        String data = feed == null ? null : new Gson().toJson(feed);
        context.getSharedPreferences(FEED_SP_NAME, Context.MODE_PRIVATE).edit().putString(key, data).apply();
    }

    public Feed feedData(String key){
        key = channelData(POULTRY_CHANNEL_SP_KEY).getChannelId() + "_" + key;
        String data = context.getSharedPreferences(FEED_SP_NAME, Context.MODE_PRIVATE).getString(key,null);
        return data == null ? new Feed() : new Gson().fromJson(data, Feed.class);
    }

    public void channelData(String key, Channel channel){
        String data = channel == null ? null : new Gson().toJson(channel);
        context.getSharedPreferences(CHANNEL_SP_NAME, Context.MODE_PRIVATE).edit().putString(key, data).apply();
    }

    public Channel channelData(String key){
        String data = context.getSharedPreferences(CHANNEL_SP_NAME, Context.MODE_PRIVATE).getString(key,null);
        return data == null ? new Channel() : new Gson().fromJson(data, Channel.class);
    }

    public void allowAutoStart(boolean allow){
        context.getSharedPreferences(AUTO_START_SP_NAME, Context.MODE_PRIVATE).edit().putBoolean(SharedPreference.AUTO_START_SP_KEY, allow).apply();
    }

    public boolean isAutoStart(){
        return context.getSharedPreferences(AUTO_START_SP_NAME, Context.MODE_PRIVATE).getBoolean(SharedPreference.AUTO_START_SP_KEY,false);
    }

    public void storeEmergencyContact(Contact contact){
        String data = contact == null ? null : new Gson().toJson(contact);
        String key = channelData(POULTRY_CHANNEL_SP_KEY).getChannelId() + "_" + EMERGENCY_CONTACT_SP_KEY;
        context.getSharedPreferences(EMERGENCY_CONTACT_SP_NAME, Context.MODE_PRIVATE).edit().putString(key, data).apply();
    }

    public Contact getEmergencyContact(){
        String key = channelData(POULTRY_CHANNEL_SP_KEY).getChannelId() + "_" + EMERGENCY_CONTACT_SP_KEY;
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
