package com.rs.smartpoultryfarm.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.github.ybq.android.spinkit.style.Circle;
import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.api.ApiHandler;
import com.rs.smartpoultryfarm.controller.AppController;
import com.rs.smartpoultryfarm.fragment.AddContactFragment;
import com.rs.smartpoultryfarm.fragment.AddControllerFragment;
import com.rs.smartpoultryfarm.fragment.ContactsFragment;
import com.rs.smartpoultryfarm.fragment.EmergencyContactFragment;
import com.rs.smartpoultryfarm.model.Channel;
import com.rs.smartpoultryfarm.model.Feed;
import com.rs.smartpoultryfarm.model.PoultryData;
import com.rs.smartpoultryfarm.model.AgroDataModel;
import com.rs.smartpoultryfarm.receiver.NotifyUserReceiver;
import com.rs.smartpoultryfarm.remote.PermissionManager;
import com.rs.smartpoultryfarm.util.AppExtensions;
import com.rs.smartpoultryfarm.util.Constants;
import com.rs.smartpoultryfarm.util.CustomSnackBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.rs.smartpoultryfarm.receiver.NetworkStatusChangeReceiver;
import com.rs.smartpoultryfarm.util.SharedPreference;
import java.util.Collections;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout          refreshLayout;
    private AgroDataModel               dataModel;
    private AppCompatTextView           humidityValue;
    private AppCompatTextView           lastHumidityValue;
    private AppCompatTextView           humidityStatus;
    private AppCompatTextView           temperatureValue;
    private AppCompatTextView           lastTemperatureValue;
    private AppCompatTextView           temperatureStatus;
    private AppCompatTextView           airQualityValue;
    private AppCompatTextView           lastAirQualityValue;
    private AppCompatTextView           airQualityStatus;
    private AppCompatTextView           waterHeightValue;
    private AppCompatTextView           lastWaterHeightValue;
    private AppCompatTextView           waterHeightStatus;
    private LinearLayoutCompat          controllersLayout;
    private LottieAnimationView         lightOneSwitch;
    private LottieAnimationView         lightTwoSwitch;
    private LottieAnimationView         lightThreeSwitch;
    private LottieAnimationView         lightFourSwitch;
    private ProgressBar                 loading;
    private AlertDialog                 notificationAlertDialog;
    private AlertDialog                 autoStartAlertDialog;
    private SharedPreference            sp;
    private NetworkStatusChangeReceiver networkStatusChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppController.setActivity(MainActivity.this);

        new PermissionManager().showPermissionDialogs();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.app_logo_mini);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        init();

        refreshLayout.setOnRefreshListener(this);

        new NotifyUserReceiver().startNotifyService(this);

        lightOneSwitch.setOnClickListener(v -> updateControllersState(v));
        lightTwoSwitch.setOnClickListener(this::updateControllersState);
        lightThreeSwitch.setOnClickListener(this::updateControllersState);
        lightFourSwitch.setOnClickListener(this::updateControllersState);

        getFeedData();

        getControllersState();
    }

    /**
     * {@link #onStart()} called whenever the application becomes visible
     **/
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(mDataReceiver, new IntentFilter(Constants.DATA_LISTENER_KEY));

        /**
         *  Allow Auto Startup ->
         *  Check if my app is allowed to show notification
         **/
        if(!new SharedPreference(MainActivity.this).isAutoStart()){
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    showAutoStartDialog(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + this.getPackageName())));
                }
                catch (ActivityNotFoundException e) {
                    try {
                        showAutoStartDialog(new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        }

        /**
         *  Allow Notification ->
         *  Check if my app is allowed to run in background
         **/
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled())
            showNotificationDialog();
    }

    /**
     * {@link #onResume()} called when the activity is in the resumed state
     **/
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkStatusChangeReceiver, new IntentFilter(CONNECTIVITY_ACTION));
    }


    /**
     * {@link #onPause()} called when an activity is about to lose focus
     **/
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkStatusChangeReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(mDataReceiver);
    }

    private void init(){
        refreshLayout = findViewById(R.id.swipeRefreshLayout);
        refreshLayout.setColorSchemeResources(R.color.icon_Color_Accent);

        dataModel = new AgroDataModel(getApplication());

        humidityValue = findViewById(R.id.humidity_Value);
        lastHumidityValue = findViewById(R.id.humidity_Last);
        humidityStatus = findViewById(R.id.humidity_Status);

        temperatureValue = findViewById(R.id.temperature_Value);
        lastTemperatureValue = findViewById(R.id.temperature_Last);
        temperatureStatus = findViewById(R.id.temperature_Status);

        airQualityValue = findViewById(R.id.airQuality_Value);
        lastAirQualityValue = findViewById(R.id.airQuality_Last);
        airQualityStatus = findViewById(R.id.airQuality_Status);

        waterHeightValue = findViewById(R.id.waterHeight_Value);
        lastWaterHeightValue = findViewById(R.id.waterHeight_Last);
        waterHeightStatus = findViewById(R.id.waterHeight_Status);

        controllersLayout = findViewById(R.id.controllers_Layout);
        lightOneSwitch = findViewById(R.id.lightOne_Switch);
        lightTwoSwitch = findViewById(R.id.lightTwo_Switch);
        lightThreeSwitch = findViewById(R.id.lightThree_Switch);
        lightFourSwitch = findViewById(R.id.lightFour_Switch);

        loading = findViewById(R.id.loader);
        loading.setIndeterminateDrawable(new Circle());

        sp = new SharedPreference();
        networkStatusChangeReceiver = new NetworkStatusChangeReceiver();
    }

    /**
     * Get data from https://thingspeak.com/
     **/
    private void getFeedData() {
        Feed lastFeed = sp.feedData(SharedPreference.POULTRY_FEED_SP_KEY);
        PoultryData poultryData = new PoultryData();
        poultryData.setFeeds(Collections.singletonList(lastFeed));
        dataSetup(poultryData);

        dataModel.getRefresh().observe(MainActivity.this, o -> dataModel.getHealthData().observe(MainActivity.this, data -> {
            refreshLayout.setRefreshing(false);
            loading.setVisibility(View.GONE);
            if(data == null || AppExtensions.isNullOrEmpty(data.getFeeds())) return;
            Collections.reverse(data.getFeeds());
            dataSetup(data);
        }));
    }

    /**
     * Update data from {@link NotifyUserReceiver}
     **/
    private final BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PoultryData poultryData = (PoultryData) intent.getSerializableExtra(Constants.DATA_INTENT_KEY);
            assert poultryData != null;
            dataSetup(poultryData);
        }
    };

    private void dataSetup(PoultryData poultryData){
        Feed lastFeed = poultryData.getFeeds().size() == 2 && poultryData.getFeeds().get(1) != null ? poultryData.getFeeds().get(1) : null;

        Feed currentFeed = poultryData.getFeeds().get(0);
        if(currentFeed == null) return;

        /**
         * Temperature
         **/
        String getLastTemp = AppExtensions.formatVal(lastFeed == null ? null : lastFeed.getField1(), null);
        if(getLastTemp != null){
            lastTemperatureValue.setVisibility(View.VISIBLE);
            lastTemperatureValue.setText(String.format("%s %s", getLastTemp, AppExtensions.string(R.string.temperatureUnit)));
        }
        else lastTemperatureValue.setVisibility(View.GONE);

        temperatureValue.setText(AppExtensions.formatVal(currentFeed.getField1(), getResources().getString(R.string.nullSymbol)));
        String getCurTemp = AppExtensions.formatVal(currentFeed.getField1(), null);

        if(getCurTemp != null){
            double temp = Double.parseDouble(getCurTemp);
            if(temp < Constants.TEMPERATURE_MIN_VALUE){
                temperatureStatus.setText(AppExtensions.string(R.string.low));
            }
            else if(temp > Constants.TEMPERATURE_MAX_VALUE){
                temperatureStatus.setText(AppExtensions.string(R.string.high));
            }
            else {
                temperatureStatus.setText(AppExtensions.string(R.string.normal));
            }
        }

        /**
         * Humidity
         **/
        String getLastHumidity = AppExtensions.formatVal(lastFeed == null ? null : lastFeed.getField2(), null);
        if(getLastHumidity != null){
            lastHumidityValue.setVisibility(View.VISIBLE);
            lastHumidityValue.setText(String.format("%s %s", getLastHumidity, AppExtensions.string(R.string.humidityUnit)));
        }
        else lastHumidityValue.setVisibility(View.GONE);

        humidityValue.setText(AppExtensions.formatVal(currentFeed.getField2(), getResources().getString(R.string.nullSymbol)));
        String getCurHumidity = AppExtensions.formatVal(currentFeed.getField2(), null);
        if(getCurHumidity != null) {
            double humidity = Double.parseDouble(getCurHumidity);
            if(humidity < Constants.HUMIDITY_MIN_VALUE){
                humidityStatus.setText(AppExtensions.string(R.string.low));
            }
            else if(humidity > Constants.HUMIDITY_MAX_VALUE){
                humidityStatus.setText(AppExtensions.string(R.string.high));
            }
            else {
                humidityStatus.setText(AppExtensions.string(R.string.normal));
            }
        }

        /**
         * Air Quality
         **/
        String getLastMoisture = AppExtensions.formatVal(lastFeed == null ? null : lastFeed.getField3(), null);
        if(getLastMoisture != null){
            lastAirQualityValue.setVisibility(View.VISIBLE);
            lastAirQualityValue.setText(String.format("%s %s", getLastMoisture, AppExtensions.string(R.string.airQualityUnit)));
        }
        else lastAirQualityValue.setVisibility(View.GONE);

        airQualityValue.setText(AppExtensions.formatVal(currentFeed.getField3(), getResources().getString(R.string.nullSymbol)));
        String getCurAirQuality = AppExtensions.formatVal(currentFeed.getField3(), null);

        if(getCurAirQuality != null){
            double airQuality = Double.parseDouble(getCurAirQuality);
            if(airQuality >= 0 && airQuality < 51){
                airQualityStatus.setText(AppExtensions.string(R.string.good));
            }
            else if(airQuality >= 51 && airQuality < 101){
                airQualityStatus.setText(AppExtensions.string(R.string.moderate));
            }
            else if(airQuality >= 101 && airQuality < 151){
                airQualityStatus.setText(AppExtensions.string(R.string.unhealthyForSensitiveGroups));
            }
            else if(airQuality >= 151 && airQuality < 201){
                airQualityStatus.setText(AppExtensions.string(R.string.unhealthy));
            }
            else if(airQuality >= 201 && airQuality < 301){
                airQualityStatus.setText(AppExtensions.string(R.string.veryUnhealthy));
            }
            else if(airQuality >= 301 && airQuality <= 500){
                airQualityStatus.setText(AppExtensions.string(R.string.hazardous));
            }
            else {
                airQualityStatus.setText(null);
            }
        }

        /**
         * Water Height
         **/
        String getWaterHeight = AppExtensions.formatVal(lastFeed == null ? null : lastFeed.getField4(), null);
        if(getWaterHeight != null){
            lastWaterHeightValue.setVisibility(View.VISIBLE);
            lastWaterHeightValue.setText(String.format("%s %s", getWaterHeight, AppExtensions.string(R.string.waterHeightUnit)));
        }
        else lastWaterHeightValue.setVisibility(View.GONE);

        waterHeightValue.setText(AppExtensions.formatVal(currentFeed.getField4(), getResources().getString(R.string.nullSymbol)));
        String getCurWaterHeight = AppExtensions.formatVal(currentFeed.getField4(), null);
        if(getCurWaterHeight != null) {
            double waterHeight = Double.parseDouble(getCurWaterHeight);
            if(waterHeight > Constants.WATER_HEIGHT_MAX_VALUE){
                waterHeightStatus.setText(AppExtensions.string(R.string.high));
            }
            else {
                waterHeightStatus.setText(AppExtensions.string(R.string.normal));
            }
        }
    }

    private void getControllersState() {
        Channel channel = sp.channelData(SharedPreference.CONTROLLER_CHANNEL_SP_KEY + "_" + sp.channelData(SharedPreference.POULTRY_CHANNEL_SP_KEY).getChannelId());
        if (channel.getChannelId() == null) {
            AddControllerFragment.show().setOnAddListener(this::updateControllersUi);
            return;
        }
        Feed feed = sp.feedData(SharedPreference.CONTROLLER_FEED_SP_KEY);
        updateControllersUi(feed);

        ApiHandler.invoke(this, Feed.class, Request.Method.GET,
                ApiHandler.singleFeedUrl(channel.getChannelId(), channel.getReadKey()),
                new ApiHandler.OnDataListener<Feed>() {
                    @Override
                    public void onData(Feed data) {
                        updateControllersUi(data);
                    }
                    @Override
                    public void onError() {}
                });
    }

    private void updateControllersState(View light) {
        loading.setVisibility(View.VISIBLE);
        handleLight((LottieAnimationView) light,  ((LottieAnimationView) light).getProgress() == 0 ? "1" : "0");

        Channel channel = sp.channelData(SharedPreference.CONTROLLER_CHANNEL_SP_KEY + "_" + sp.channelData(SharedPreference.POULTRY_CHANNEL_SP_KEY).getChannelId());
        if (channel.getChannelId() == null) return;

        ApiHandler.invoke(this, Feed.class, Request.Method.POST, ApiHandler.updateControllerFeedURL(
                channel.getWriteKey(),
                lightOneSwitch.getProgress() != 0,
                lightTwoSwitch .getProgress() != 0,
                lightThreeSwitch.getProgress() != 0,
                lightFourSwitch.getProgress() != 0
        ), new ApiHandler.OnDataListener<Feed>() {
            @Override
            public void onData(Feed feed) {
                loading.setVisibility(View.GONE);
                updateControllersUi(feed);
            }
            @Override
            public void onError() {
                loading.setVisibility(View.GONE);
                handleLight((LottieAnimationView) light,  ((LottieAnimationView) light).getProgress() == 0 ? "1" : "0");
            }
        });
    }

    void updateControllersUi(Feed feed){
        if (feed == null) return;

        Channel channel = sp.channelData(SharedPreference.CONTROLLER_CHANNEL_SP_KEY + "_" + sp.channelData(SharedPreference.POULTRY_CHANNEL_SP_KEY).getChannelId());

        controllersLayout.setVisibility(channel.getChannelId() != null
                ? View.VISIBLE
                : View.GONE
        );

        handleLight(lightOneSwitch, feed.getField1());
        handleLight(lightTwoSwitch, feed.getField2());
        handleLight(lightThreeSwitch, feed.getField3());
        handleLight(lightFourSwitch, feed.getField4());

        sp.feedData(SharedPreference.CONTROLLER_FEED_SP_KEY, feed);
    }

    void handleLight(LottieAnimationView light, String status){
        if(isSwitchOn(status))
            light.playAnimation();
        else light.setProgress(0);
    }

    boolean isSwitchOn(String status){
        return AppExtensions.formatVal(status, "0").equals("1");
    }

    /**
     *  Emergency Contact
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.addContact:
                AddContactFragment.show(null);
                break;
            case R.id.addController:
                AddControllerFragment.show().setOnAddListener(this::updateControllersUi);
                break;
            case R.id.phoneContacts:
                ContactsFragment.show();
                break;
            case R.id.emergencyContact:
                EmergencyContactFragment.show();
                break;
            case R.id.logout:
                sp.channelData(SharedPreference.POULTRY_CHANNEL_SP_KEY, null);
                sp.setLoggedIn(false);

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Swipe to reload data
     **/
    @Override
    public void onRefresh() {
        dataModel.RefreshData();
        getControllersState();
    }

    /**
     *  prompt permission dialog for notification
     **/
    public void showNotificationDialog(){
        if(notificationAlertDialog != null && notificationAlertDialog.isShowing() ) return;

        notificationAlertDialog = new AlertDialog.Builder(MainActivity.this, R.style.NotificationDialog).create();
        notificationAlertDialog.setCancelable(false);
        notificationAlertDialog.setMessage(getResources().getString(R.string.allowNotificationMessage));

        notificationAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, (getResources().getString(R.string.allow)+"          "),
                (dialog, which) -> {
                    dialog.dismiss();
                    new PermissionManager().goToNotificationPermissionSetting();
                });

        notificationAlertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, ("          "+getResources().getString(R.string.forbid)),
                (dialog, which) -> dialog.dismiss());

        notificationAlertDialog.show();

        AppCompatTextView messageText = notificationAlertDialog.findViewById(android.R.id.message);
        if (messageText != null) {
            messageText.setGravity(Gravity.CENTER_HORIZONTAL);
            messageText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorSmokeWhite));
        }
    }

    /**
     *  prompt permission dialog for autoStart
     **/
    public void showAutoStartDialog(final Intent intent){
        if(autoStartAlertDialog != null && autoStartAlertDialog.isShowing() ) return;

        autoStartAlertDialog = new AlertDialog.Builder(MainActivity.this, R.style.NotificationDialog).create();
        autoStartAlertDialog.setCancelable(false);
        autoStartAlertDialog.setMessage(getResources().getString(R.string.allowAutoStartupMessage));

        autoStartAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, (getResources().getString(R.string.allow)+"          "),
                (dialog, which) -> {
                    dialog.dismiss();
                    new SharedPreference(MainActivity.this).allowAutoStart(true);
                    startActivity(intent);
                });

        autoStartAlertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, ("            "+getResources().getString(R.string.forbid)),
                (dialog, which) -> dialog.dismiss());

        autoStartAlertDialog.show();

        AppCompatTextView messageText = autoStartAlertDialog.findViewById(android.R.id.message);
        if (messageText != null) {
            messageText.setGravity(Gravity.CENTER_HORIZONTAL);
            messageText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorSmokeWhite));
        }
    }

    /**
     *  Monitor Internet Connection
     **/
    public void updateInternetConnectionStatus(boolean isConnected) {
        if (isConnected) {
            if(CustomSnackBar.snackbar != null) CustomSnackBar.snackbar.dismiss();
        }
        else {
            CustomSnackBar snackBar = new CustomSnackBar(refreshLayout, R.string.network_Error, R.string.retry, CustomSnackBar.Duration.INDEFINITE);
            snackBar.show();
            snackBar.setOnDismissListener(skBar -> {
                networkStatusChangeReceiver.onReceive(MainActivity.this, null);
                skBar.dismiss();
            });
        }
    }
}
