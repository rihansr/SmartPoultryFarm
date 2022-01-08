package com.rs.smartpoultryfarm.receiver;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.android.volley.Request;
import com.rs.smartpoultryfarm.BuildConfig;
import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.activity.MainActivity;
import com.rs.smartpoultryfarm.api.ApiHandler;
import com.rs.smartpoultryfarm.model.Channel;
import com.rs.smartpoultryfarm.model.Contact;
import com.rs.smartpoultryfarm.model.Feed;
import com.rs.smartpoultryfarm.model.PoultryData;
import com.rs.smartpoultryfarm.remote.PermissionManager;
import com.rs.smartpoultryfarm.util.AppExtensions;
import com.rs.smartpoultryfarm.util.Constants;
import com.rs.smartpoultryfarm.util.SharedPreference;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Alert Notification Service
 **/
public class NotifyUserReceiver extends BroadcastReceiver {

    public static int NOTIFICATION_ID = 1;
    private LocalBroadcastManager dataBroadcaster;

    @Override
    public void onReceive(final Context context, Intent intent) {
        dataBroadcaster = LocalBroadcastManager.getInstance(context);

        SharedPreference sp = new SharedPreference(context);
        if (!sp.isLoggedIn()) return;

        Channel channel = sp.channelData(SharedPreference.POULTRY_CHANNEL_SP_KEY);
        ApiHandler.invoke(context, PoultryData.class, Request.Method.GET,
                ApiHandler.feedsUrl(channel.getChannelId(), channel.getReadKey(), 2),
                new ApiHandler.OnDataListener<PoultryData>() {
                    @Override
                    public void onData(PoultryData data) {
                        if (data == null || data.getCode() == 404 || AppExtensions.isNullOrEmpty(data.getFeeds())) return;
                        Collections.reverse(data.getFeeds());

                        Feed curFeed = data.getFeeds().get(0);
                        Feed lastFeed = sp.feedData(SharedPreference.POULTRY_FEED_SP_KEY);
                        if (curFeed == null) return;

                        String temperatureStatus = "";
                        String humidityStatus = "";
                        String airQualityStatus = "";
                        String waterHeightStatus = "";

                        Intent tokenIntent = new Intent(Constants.DATA_LISTENER_KEY);
                        tokenIntent.putExtra(Constants.DATA_INTENT_KEY, data);
                        dataBroadcaster.sendBroadcast(tokenIntent);

                        /**
                         *  Checking Temperature
                         **/
                        String getNewTemperature = AppExtensions.formatVal(curFeed.getField1(), null);
                        String getOldTemperature = AppExtensions.formatVal(lastFeed.getField1(), null);
                        if (getNewTemperature != null) {
                            double newTemperature = Double.parseDouble(getNewTemperature);
                            if (getOldTemperature == null || newTemperature != Double.parseDouble(getOldTemperature)) {
                                if (newTemperature < Constants.TEMPERATURE_MIN_VALUE) {
                                    temperatureStatus = AppExtensions.string(R.string.low) + " temperature, its about " + getNewTemperature + " " + AppExtensions.string(R.string.temperatureUnit);
                                } else if (newTemperature > Constants.TEMPERATURE_MAX_VALUE) {
                                    temperatureStatus = AppExtensions.string(R.string.high) + " temperature, its about " + getNewTemperature + " " + AppExtensions.string(R.string.temperatureUnit);
                                }
                            }
                        }

                        /**
                         *  Checking Humidity
                         **/
                        String getNewHumidity = AppExtensions.formatVal(curFeed.getField2(), null);
                        String getOldHumidity = AppExtensions.formatVal(lastFeed.getField2(), null);
                        if (getNewHumidity != null) {
                            double newHumidity = Double.parseDouble(getNewHumidity);
                            if (getOldHumidity == null || newHumidity != Double.parseDouble(getOldHumidity)) {
                                if (newHumidity < Constants.HUMIDITY_MIN_VALUE) {
                                    humidityStatus = AppExtensions.string(R.string.low) + " humidity, its about " + getNewHumidity + " " + AppExtensions.string(R.string.humidityUnit);
                                } else if (newHumidity > Constants.HUMIDITY_MAX_VALUE) {
                                    humidityStatus = AppExtensions.string(R.string.high) + " humidity, its about " + getNewHumidity + " " + AppExtensions.string(R.string.humidityUnit);
                                }
                            }
                        }

                        /**
                         *  Checking Air Quality
                         **/
                        String getNewAirQuality = AppExtensions.formatVal(curFeed.getField3(), null);
                        String getOldAirQuality = AppExtensions.formatVal(lastFeed.getField3(), null);
                        if (getNewAirQuality != null) {
                            double newAirQuality = Double.parseDouble(getNewAirQuality);
                            if (getOldAirQuality == null || newAirQuality != Double.parseDouble(getOldAirQuality)) {
                                if (newAirQuality >= 51 && newAirQuality < 101) {
                                    airQualityStatus = "Air Quality (" + AppExtensions.string(R.string.moderate) + "), its about " + getNewAirQuality + " " + AppExtensions.string(R.string.airQualityUnit);
                                } else if (newAirQuality >= 101 && newAirQuality < 151) {
                                    airQualityStatus = "Air Quality (" + AppExtensions.string(R.string.unhealthyForSensitiveGroups) + "), its about " + getNewAirQuality + " " + AppExtensions.string(R.string.airQualityUnit);
                                } else if (newAirQuality >= 151 && newAirQuality < 201) {
                                    airQualityStatus = "Air Quality (" + AppExtensions.string(R.string.unhealthy) + "), its about " + getNewAirQuality + " " + AppExtensions.string(R.string.airQualityUnit);
                                } else if (newAirQuality >= 201 && newAirQuality < 301) {
                                    airQualityStatus = "Air Quality (" + AppExtensions.string(R.string.veryUnhealthy) + "), its about " + getNewAirQuality + " " + AppExtensions.string(R.string.airQualityUnit);
                                } else if (newAirQuality >= 301 && newAirQuality <= 500) {
                                    airQualityStatus = "Air Quality (" + AppExtensions.string(R.string.hazardous) + "), its about " + getNewAirQuality + " " + AppExtensions.string(R.string.airQualityUnit);
                                }
                            }
                        }

                        /**
                         *  Checking Water Height
                         **/
                        String getNewWaterHeight = AppExtensions.formatVal(curFeed.getField4(), null);
                        String getOldWaterHeight = AppExtensions.formatVal(lastFeed.getField4(), null);
                        if (getNewWaterHeight != null) {
                            double newWaterHeight = Double.parseDouble(getNewWaterHeight);
                            if (getOldWaterHeight == null || newWaterHeight != Double.parseDouble(getOldWaterHeight)) {
                                if (newWaterHeight > Constants.WATER_HEIGHT_MAX_VALUE) {
                                    waterHeightStatus = "Water height " + AppExtensions.string(R.string.high).toLowerCase()
                                            + ", its about " + getNewWaterHeight + " " + AppExtensions.string(R.string.waterHeightUnit);
                                }
                            }
                        }

                        sp.feedData(SharedPreference.POULTRY_FEED_SP_KEY, curFeed);

                        /**
                         * Send sms to emergency contact
                         **/
                        if (!temperatureStatus.isEmpty() || !humidityStatus.isEmpty() || !airQualityStatus.isEmpty()) {
                            StringBuilder message = new StringBuilder()
                                    .append(temperatureStatus)
                                    .append(temperatureStatus.isEmpty() || humidityStatus.isEmpty() ? humidityStatus : "\n" + humidityStatus)
                                    .append(temperatureStatus.isEmpty() && humidityStatus.isEmpty() || airQualityStatus.isEmpty() ? airQualityStatus : "\n" + airQualityStatus)
                                    .append(waterHeightStatus.isEmpty() ? waterHeightStatus : "\n" + waterHeightStatus);

                            sendSMS(context, "Emergency Alert !!\n\n" + message);
                            showNotification(context, "Emergency Alert !!", message.toString());
                        }
                    }

                    @Override
                    public void onError() {}
                });
    }

    public void startNotifyService(Context context) {
        try {
            final int _id = (int) System.currentTimeMillis();

            Intent myIntent = new Intent(context, NotifyUserReceiver.class);
            @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getBroadcast(context, _id, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            assert alarmManager != null;
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), Constants.REFRESH_DELAY, pendingIntent);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(Constants.TAG, "Sorry, there is some problem in starting TeleKiT, please! reinstall it");
        }
    }

    public void sendSMS(Context context, String message) {
        if (!new PermissionManager(PermissionManager.Permission.SMS, false).isGranted()) return;
        SharedPreference sp = new SharedPreference(context);
        Contact contact = sp.getEmergencyContact();
        if (contact == null || contact.getNumber() == null) return;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(contact.getNumber(), null, parts, null, null);
            Log.e(Constants.TAG, "Message sent successfully");
        } catch (Exception ex) {
            Log.e(Constants.TAG, "Message not sent, reason: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showNotification(Context context, String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        String CHANNEL_ID = BuildConfig.APPLICATION_ID;
        String CHANNEL_NAME = Constants.TAG;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setAutoCancel(true)
                .setSmallIcon(R.drawable.app_logo)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{100, 100, 100, 100, 100})
                .setContentTitle(title)
                .setContentText(message)
                .setWhen(System.currentTimeMillis())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setDefaults(Notification.DEFAULT_LIGHTS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(message);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(ContextCompat.getColor(context, R.color.colorAccent));
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            assert notificationManager != null;
            builder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(channel);
        }

        if (NOTIFICATION_ID > 1073741824) NOTIFICATION_ID = 0;

        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_ID++, builder.build());
    }
}
