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
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.ybq.android.spinkit.style.Circle;
import com.google.gson.GsonBuilder;
import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.api.API;
import com.rs.smartpoultryfarm.controller.AppController;
import com.rs.smartpoultryfarm.fragment.AddContactFragment;
import com.rs.smartpoultryfarm.fragment.ContactsFragment;
import com.rs.smartpoultryfarm.fragment.EmergencyContactFragment;
import com.rs.smartpoultryfarm.model.Feed;
import com.rs.smartpoultryfarm.model.PoultryData;
import com.rs.smartpoultryfarm.model.AgroDataModel;
import com.rs.smartpoultryfarm.receiver.NotifyUserReceiver;
import com.rs.smartpoultryfarm.remote.PermissionManager;
import com.rs.smartpoultryfarm.util.AppExtensions;
import com.rs.smartpoultryfarm.util.Constants;
import com.rs.smartpoultryfarm.util.CustomSnackBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
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
    private AppCompatButton             lightOneSwitch;
    private AppCompatButton             lightTwoSwitch;
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

        lightOneSwitch.setText(AppExtensions.getString(sp.controllersState(SharedPreference.LIGHT_ONE_STATE_SP_KEY)
                ? R.string.on
                : R.string.off)
        );
        lightOneSwitch.setOnClickListener(view -> updateControlState(lightOneSwitch));

        lightTwoSwitch.setText(AppExtensions.getString(sp.controllersState(SharedPreference.LIGHT_TWO_STATE_SP_KEY)
                ? R.string.on
                : R.string.off)
        );
        lightTwoSwitch.setOnClickListener(view -> updateControlState(lightTwoSwitch));

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

        lightOneSwitch = findViewById(R.id.lightOne_Switch);
        lightTwoSwitch = findViewById(R.id.lightTwo_Switch);

        loading = findViewById(R.id.loader);
        loading.setIndeterminateDrawable(new Circle());

        sp = new SharedPreference();
        networkStatusChangeReceiver = new NetworkStatusChangeReceiver();
    }

    /**
     * Get data from https://thingspeak.com/
     **/
    private void getFeedData() {
        double humidity = sp.feedData(SharedPreference.HUMIDITY_VALUE_SP_KEY);
        double temperature = sp.feedData(SharedPreference.TEMP_VALUE_SP_KEY);
        double airQuality = sp.feedData(SharedPreference.AIR_QUALITY_VALUE_SP_KEY);

        PoultryData poultryData = new PoultryData();
        poultryData.setFeeds(Collections.singletonList(new Feed(temperature, humidity, airQuality)));
        dataSetup(poultryData);

        dataModel.getRefresh().observe(MainActivity.this, o -> dataModel.getHealthData().observe(MainActivity.this, data -> {
            refreshLayout.setRefreshing(false);
            loading.setVisibility(View.GONE);
            if(data == null || data.getFeeds().size() == 0) return;
            Collections.reverse(data.getFeeds());
            dataSetup(data);
        }));
    }

    private void getControllersState() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, API.getControllerFeedURL(),
                response -> {
                    Feed feed = new GsonBuilder().create().fromJson(response, Feed.class);
                    if (feed == null) return;

                    int controlOneState = Integer.parseInt(AppExtensions.formatValue(feed.getFieldOne(), "0"));
                    lightOneSwitch.setText(AppExtensions.getString(controlOneState == 1 ? R.string.on : R.string.off));
                    sp.controllersState(SharedPreference.LIGHT_ONE_STATE_SP_KEY, controlOneState == 1);

                    int controlTwoState = Integer.parseInt(AppExtensions.formatValue(feed.getFieldTwo(), "0"));
                    lightTwoSwitch.setText(AppExtensions.getString(controlTwoState == 1 ? R.string.on : R.string.off));
                    sp.controllersState(SharedPreference.LIGHT_TWO_STATE_SP_KEY, controlTwoState == 1);
                },
                Throwable::printStackTrace);

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
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
        String getLastTemp = AppExtensions.formatValue(lastFeed == null ? null : lastFeed.getFieldOne(), null);
        if(getLastTemp != null){
            lastTemperatureValue.setVisibility(View.VISIBLE);
            lastTemperatureValue.setText(String.format("%s %s", getLastTemp, AppExtensions.getString(R.string.temperatureUnit)));
        }
        else lastTemperatureValue.setVisibility(View.GONE);

        temperatureValue.setText(AppExtensions.formatValue(currentFeed.getFieldOne(), getResources().getString(R.string.nullSymbol)));
        String getCurTemp = AppExtensions.formatValue(currentFeed.getFieldOne(), null);

        if(getCurTemp != null){
            double temp = Double.parseDouble(getCurTemp);
            if(temp < Constants.TEMPERATURE_MIN_VALUE){
                temperatureStatus.setText(AppExtensions.getString(R.string.low));
            }
            else if(temp > Constants.TEMPERATURE_MAX_VALUE){
                temperatureStatus.setText(AppExtensions.getString(R.string.high));
            }
            else {
                temperatureStatus.setText(AppExtensions.getString(R.string.normal));
            }
        }

        /**
         * Humidity
         **/
        String getLastHumidity = AppExtensions.formatValue(lastFeed == null ? null : lastFeed.getFieldTwo(), null);
        if(getLastHumidity != null){
            lastHumidityValue.setVisibility(View.VISIBLE);
            lastHumidityValue.setText(String.format("%s %s", getLastHumidity, AppExtensions.getString(R.string.humidityUnit)));
        }
        else lastHumidityValue.setVisibility(View.GONE);

        humidityValue.setText(AppExtensions.formatValue(currentFeed.getFieldTwo(), getResources().getString(R.string.nullSymbol)));
        String getCurHumidity = AppExtensions.formatValue(currentFeed.getFieldTwo(), null);
        if(getCurHumidity != null) {
            double humidity = Double.parseDouble(getCurHumidity);
            if(humidity < Constants.HUMIDITY_MIN_VALUE){
                humidityStatus.setText(AppExtensions.getString(R.string.low));
            }
            else if(humidity > Constants.HUMIDITY_MAX_VALUE){
                humidityStatus.setText(AppExtensions.getString(R.string.high));
            }
            else {
                humidityStatus.setText(AppExtensions.getString(R.string.normal));
            }
        }

        /**
         * Air Quality
         **/
        String getLastMoisture = AppExtensions.formatValue(lastFeed == null ? null : lastFeed.getFieldThree(), null);
        if(getLastMoisture != null){
            lastAirQualityValue.setVisibility(View.VISIBLE);
            lastAirQualityValue.setText(String.format("%s %s", getLastMoisture, AppExtensions.getString(R.string.airQualityUnit)));
        }
        else lastAirQualityValue.setVisibility(View.GONE);

        airQualityValue.setText(AppExtensions.formatValue(currentFeed.getFieldThree(), getResources().getString(R.string.nullSymbol)));
        String getCurAirQuality = AppExtensions.formatValue(currentFeed.getFieldThree(), null);

        if(getCurAirQuality != null){
            double airQuality = Double.parseDouble(getCurAirQuality);
            if(airQuality >= 0 && airQuality < 51){
                airQualityStatus.setText(AppExtensions.getString(R.string.good));
            }
            else if(airQuality >= 51 && airQuality < 101){
                airQualityStatus.setText(AppExtensions.getString(R.string.moderate));
            }
            else if(airQuality >= 101 && airQuality < 151){
                airQualityStatus.setText(AppExtensions.getString(R.string.unhealthyForSensitiveGroups));
            }
            else if(airQuality >= 151 && airQuality < 201){
                airQualityStatus.setText(AppExtensions.getString(R.string.unhealthy));
            }
            else if(airQuality >= 201 && airQuality < 301){
                airQualityStatus.setText(AppExtensions.getString(R.string.veryUnhealthy));
            }
            else if(airQuality >= 301 && airQuality <= 500){
                airQualityStatus.setText(AppExtensions.getString(R.string.hazardous));
            }
            else {
                airQualityStatus.setText(null);
            }
        }
    }

    private void updateControlState(AppCompatButton toggleButton) {
        loading.setVisibility(View.VISIBLE);
        boolean lightOneState = (toggleButton == lightOneSwitch) != lightOneSwitch.getText().equals(AppExtensions.getString(R.string.on));
        boolean lightTwoState = (toggleButton == lightTwoSwitch) != lightTwoSwitch.getText().equals(AppExtensions.getString(R.string.on));
        StringRequest stringRequest = new StringRequest(Request.Method.POST, API.updateControllerFeedURL(
                lightOneState,
                lightTwoState
        ), response ->
        {
            loading.setVisibility(View.GONE);
            if (response == null || response.equals("0")) return;
            if (toggleButton == lightOneSwitch)
                toggleButton.setText(AppExtensions.getString(lightOneState ? R.string.on : R.string.off));
            else if (toggleButton == lightTwoSwitch)
                toggleButton.setText(AppExtensions.getString(lightTwoState ? R.string.on : R.string.off));
            sp.controllersState(SharedPreference.LIGHT_ONE_STATE_SP_KEY, lightOneState);
            sp.controllersState(SharedPreference.LIGHT_TWO_STATE_SP_KEY, lightTwoState);
        }
                , error -> {
            loading.setVisibility(View.GONE);
            error.printStackTrace();
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
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
            case R.id.phoneContacts:
                ContactsFragment.show();
                break;
            case R.id.emergencyContact:
                EmergencyContactFragment.show();
                break;
            case R.id.logout:
                sp.setLoggedIn(false);
                sp.channelData(SharedPreference.CHANNEL_ID_SP_KEY, null);
                sp.channelData(SharedPreference.CHANNEL_KEY_SP_KEY, null);
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

                    Intent intent = new Intent();
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

                    /**
                     * for Android 5-7
                     **/
                    intent.putExtra("app_package", getPackageName());
                    intent.putExtra("app_uid", getApplicationInfo().uid);

                    /**
                     * for Android 8 and above
                     **/
                    intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());

                    startActivity(intent);
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
