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
import com.rs.smartpoultryfarm.model.Contact;
import com.rs.smartpoultryfarm.model.Feed;
import com.rs.smartpoultryfarm.model.PoultryData;
import com.rs.smartpoultryfarm.remote.PermissionManager;
import com.rs.smartpoultryfarm.util.AppExtensions;
import com.rs.smartpoultryfarm.util.Constants;
import com.rs.smartpoultryfarm.util.SharedPreference;
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
        ApiHandler.invoke(context, PoultryData.class, Request.Method.GET,
                ApiHandler.getDataFeedURL(sp.channelData(SharedPreference.CHANNEL_ID_SP_KEY), sp.channelData(SharedPreference.CHANNEL_KEY_SP_KEY), 2),
                data -> {
                    if (data == null) return;
                    if (data.getCode() == 404) return;
                    if (data.getFeeds() == null || data.getFeeds().isEmpty()) return;
                    Collections.reverse(data.getFeeds());

                    Feed curFeed = data.getFeeds().get(0);
                    Feed lastFeed = sp.feedData(SharedPreference.POULTRY_DATA_SP_KEY);
                    if (curFeed == null) return;

                    String temperatureStatus = "";
                    String humidityStatus = "";
                    String airQualityStatus = "";

                    Intent tokenIntent = new Intent(Constants.DATA_LISTENER_KEY);
                    tokenIntent.putExtra(Constants.DATA_INTENT_KEY, data);
                    dataBroadcaster.sendBroadcast(tokenIntent);

                    /**
                     *  Checking Temperature
                     **/
                    String getNewTemperature = AppExtensions.formatValue(curFeed.getField1(), null);
                    String getOldTemperature = AppExtensions.formatValue(lastFeed.getField1(), null);
                    if (getNewTemperature != null) {
                        double newTemperature = Double.parseDouble(getNewTemperature);
                        if (getOldTemperature == null || newTemperature != Double.parseDouble(getOldTemperature)) {
                            if (newTemperature < Constants.TEMPERATURE_MIN_VALUE) {
                                temperatureStatus = AppExtensions.getString(R.string.low) + " temperature, its about " + getNewTemperature + " " + AppExtensions.getString(R.string.temperatureUnit);
                            } else if (newTemperature > Constants.TEMPERATURE_MAX_VALUE) {
                                temperatureStatus = AppExtensions.getString(R.string.high) + " temperature, its about " + getNewTemperature + " " + AppExtensions.getString(R.string.temperatureUnit);
                            }
                        }
                    }

                    /**
                     *  Checking Humidity
                     **/
                    String getNewHumidity = AppExtensions.formatValue(curFeed.getField2(), null);
                    String getOldHumidity = AppExtensions.formatValue(lastFeed.getField2(), null);
                    if (getNewHumidity != null) {
                        double newHumidity = Double.parseDouble(getNewHumidity);
                        if (getOldHumidity == null || newHumidity != Double.parseDouble(getOldHumidity)) {
                            if (newHumidity < Constants.HUMIDITY_MIN_VALUE) {
                                humidityStatus = AppExtensions.getString(R.string.low) + " humidity, its about " + getNewHumidity + " " + AppExtensions.getString(R.string.humidityUnit);
                            } else if (newHumidity > Constants.HUMIDITY_MAX_VALUE) {
                                humidityStatus = AppExtensions.getString(R.string.high) + " humidity, its about " + getNewHumidity + " " + AppExtensions.getString(R.string.humidityUnit);
                            }
                        }
                    }

                    /**
                     *  Checking Air Quality
                     **/
                    String getNewAirQuality = AppExtensions.formatValue(curFeed.getField3(), null);
                    String getOldAirQuality = AppExtensions.formatValue(lastFeed.getField3(), null);
                    if (getNewAirQuality != null) {
                        double newAirQuality = Double.parseDouble(getNewAirQuality);
                        if (getOldAirQuality == null || newAirQuality != Double.parseDouble(getOldAirQuality)) {
                            if (newAirQuality >= 51 && newAirQuality < 101) {
                                airQualityStatus = "Air Quality (" + AppExtensions.getString(R.string.moderate) + "), its about " + getNewAirQuality + " " + AppExtensions.getString(R.string.airQualityUnit);
                            } else if (newAirQuality >= 101 && newAirQuality < 151) {
                                airQualityStatus = "Air Quality (" + AppExtensions.getString(R.string.unhealthyForSensitiveGroups) + "), its about " + getNewAirQuality + " " + AppExtensions.getString(R.string.airQualityUnit);
                            } else if (newAirQuality >= 151 && newAirQuality < 201) {
                                airQualityStatus = "Air Quality (" + AppExtensions.getString(R.string.unhealthy) + "), its about " + getNewAirQuality + " " + AppExtensions.getString(R.string.airQualityUnit);
                            } else if (newAirQuality >= 201 && newAirQuality < 301) {
                                airQualityStatus = "Air Quality (" + AppExtensions.getString(R.string.veryUnhealthy) + "), its about " + getNewAirQuality + " " + AppExtensions.getString(R.string.airQualityUnit);
                            } else if (newAirQuality >= 301 && newAirQuality <= 500) {
                                airQualityStatus = "Air Quality (" + AppExtensions.getString(R.string.hazardous) + "), its about " + getNewAirQuality + " " + AppExtensions.getString(R.string.airQualityUnit);
                            }
                        }
                    }

                    sp.feedData(SharedPreference.POULTRY_DATA_SP_KEY, curFeed);

                    /**
                     * Send sms to emergency contact
                     **/
                    if (!temperatureStatus.isEmpty() || !humidityStatus.isEmpty() || !airQualityStatus.isEmpty()) {
                        StringBuilder message = new StringBuilder()
                                .append(temperatureStatus)
                                .append(temperatureStatus.isEmpty() || humidityStatus.isEmpty() ? humidityStatus : "\n" + humidityStatus)
                                .append(temperatureStatus.isEmpty() && humidityStatus.isEmpty() || airQualityStatus.isEmpty() ? airQualityStatus : "\n" + airQualityStatus);

                        sendSMS(context, "Emergency Alert !!\n\n" + message);
                        showNotification(context, "Emergency Alert !!", message.toString());
                    }
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

    protected void sendSMS(Context context, String message) {
        if (!new PermissionManager(PermissionManager.Permission.SMS, false).isGranted()) return;
        SharedPreference sp = new SharedPreference(context);
        Contact contact = sp.getEmergencyContact();
        if (contact == null || contact.getNumber() == null) return;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(contact.getNumber(), null, message, null, null);
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
        String CHANNEL_NAME = "TeleKiT";

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
