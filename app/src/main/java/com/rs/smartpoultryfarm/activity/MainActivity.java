package com.rs.smartpoultryfarm.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import com.android.volley.Request;
import com.github.ybq.android.spinkit.style.Circle;
import com.google.android.material.navigation.NavigationView;
import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.adapter.FieldAdapter;
import com.rs.smartpoultryfarm.adapter.LightAdapter;
import com.rs.smartpoultryfarm.api.ApiHandler;
import com.rs.smartpoultryfarm.controller.AppController;
import com.rs.smartpoultryfarm.fragment.AddContactFragment;
import com.rs.smartpoultryfarm.fragment.AddControllerFragment;
import com.rs.smartpoultryfarm.fragment.ContactsFragment;
import com.rs.smartpoultryfarm.fragment.EmergencyContactFragment;
import com.rs.smartpoultryfarm.model.Channel;
import com.rs.smartpoultryfarm.model.Feed;
import com.rs.smartpoultryfarm.model.Field;
import com.rs.smartpoultryfarm.model.PoultryData;
import com.rs.smartpoultryfarm.model.AgroDataModel;
import com.rs.smartpoultryfarm.receiver.NotifyUserReceiver;
import com.rs.smartpoultryfarm.remote.PermissionManager;
import com.rs.smartpoultryfarm.util.AppExtensions;
import com.rs.smartpoultryfarm.util.Constants;
import com.rs.smartpoultryfarm.util.CustomSnackBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.rs.smartpoultryfarm.receiver.NetworkStatusChangeReceiver;
import com.rs.smartpoultryfarm.util.SharedPreference;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private DrawerLayout                drawerLayout;
    private SwipeRefreshLayout          refreshLayout;
    private CardView                    contentView;
    private NavigationView              navigationView;
    private AgroDataModel               dataModel;
    private RecyclerView                rcvLights;
    private LightAdapter                lightsAdapter;
    private RecyclerView                rcvFields;
    private FieldAdapter                fieldsAdapter;
    private List<Field>                 fields;

    /*Drawer*/
    AppCompatTextView                   navAddController;
    AppCompatTextView                   navAddContact;
    AppCompatTextView                   navPhoneContacts;
    AppCompatTextView                   navEmergencyContact;
    AppCompatTextView                   navShare;
    AppCompatTextView                   navLogout;

    /*Other*/
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
            actionBar.setElevation(0);
        }

        init();

        setAdapter();

        setDrawer();

        refreshLayout.setOnRefreshListener(this);

        new NotifyUserReceiver().startNotifyService(this);

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
        drawerLayout = findViewById(R.id.layout_drawer);

        contentView = findViewById(R.id.layout_content);
        refreshLayout = findViewById(R.id.layout_refresh);
        refreshLayout.setColorSchemeResources(R.color.icon_Color_Accent);

        navigationView = findViewById(R.id.view_navigation);
        navAddController = findViewById(R.id.nav_add_controller);
        navAddContact = findViewById(R.id.nav_add_contact);
        navPhoneContacts = findViewById(R.id.nav_phone_contacts);
        navEmergencyContact = findViewById(R.id.nav_emergency_contact);
        navShare = findViewById(R.id.nav_share);
        navLogout = findViewById(R.id.nav_logout);

        dataModel = new AgroDataModel(getApplication());

        rcvLights = findViewById(R.id.lights_rcv);
        rcvFields = findViewById(R.id.fields_rcv);

        loading = findViewById(R.id.loader);
        loading.setIndeterminateDrawable(new Circle());

        sp = new SharedPreference();
        networkStatusChangeReceiver = new NetworkStatusChangeReceiver();
    }

    public void setAdapter() {
        lightsAdapter = new LightAdapter();
        rcvLights.setAdapter(lightsAdapter);
        lightsAdapter.setFeed(sp.feedData(SharedPreference.CONTROLLER_FEED_SP_KEY));

        fields = Constants.fields();
        fieldsAdapter = new FieldAdapter();
        rcvFields.setAdapter(fieldsAdapter);
        fieldsAdapter.setFields(fields);
    }

    public void setDrawer() {
        drawerLayout.setScrimColor(Color.TRANSPARENT);
        drawerLayout.setDrawerElevation(0);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                float width = navigationView.getWidth() * slideOffset;
                contentView.setX(width * -1);
                contentView.setRadius(48 * slideOffset);
                contentView.setElevation(12 * slideOffset);
                contentView.setPivotX(width / 2);
                contentView.setRotation(7 * slideOffset);
                contentView.invalidate();
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {}

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {}

            @Override
            public void onDrawerStateChanged(int newState) {}
        });

        navAddController.setOnClickListener(v ->
                AddControllerFragment.show().setOnAddListener(feed -> lightsAdapter.setFeed(feed))
        );

        navAddContact.setOnClickListener(v -> AddContactFragment.show());

        navPhoneContacts.setOnClickListener(v -> ContactsFragment.show());

        navEmergencyContact.setOnClickListener(v -> EmergencyContactFragment.show());

        navShare.setOnClickListener(v -> {
            if (!new PermissionManager(PermissionManager.Permission.STORAGE, true, response -> AppExtensions.shareApk()).isGranted()) return;
            AppExtensions.shareApk();
        });

        navLogout.setOnClickListener(v -> {
            sp.channelData(SharedPreference.POULTRY_CHANNEL_SP_KEY, null);
            sp.setLoggedIn(false);

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
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
            if (data == null
                    || data.getCode() == 404
                    || AppExtensions.isNullOrEmpty(data.getFeeds())) return;

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
        String curTemp = AppExtensions.formatVal(currentFeed.getField1(), null);
        String prevTemp = AppExtensions.formatVal(lastFeed == null ? null : lastFeed.getField1(), null);
        fields.get(0).setCurValue(curTemp);
        fields.get(0).setPrevValue(prevTemp);

        if (curTemp != null) {
            double temperature = Double.parseDouble(curTemp);
            if (temperature < Constants.TEMPERATURE_MIN_VALUE)
                fields.get(0).setStatus(AppExtensions.string(R.string.low));
            else if (temperature > Constants.TEMPERATURE_MAX_VALUE)
                fields.get(0).setStatus(AppExtensions.string(R.string.high));
            else
                fields.get(0).setStatus(AppExtensions.string(R.string.normal));
        } else fields.get(0).setStatus(null);

        /**
         * Humidity
         **/
        String curHum = AppExtensions.formatVal(currentFeed.getField2(), null);
        String prevHum = AppExtensions.formatVal(lastFeed == null ? null : lastFeed.getField2(), null);
        fields.get(1).setCurValue(curHum);
        fields.get(1).setPrevValue(prevHum);

        if (curHum != null) {
            double humidity = Double.parseDouble(curHum);
            if (humidity < Constants.HUMIDITY_MIN_VALUE)
                fields.get(1).setStatus(AppExtensions.string(R.string.low));
            else if (humidity > Constants.HUMIDITY_MAX_VALUE)
                fields.get(1).setStatus(AppExtensions.string(R.string.high));
            else
                fields.get(1).setStatus(AppExtensions.string(R.string.normal));
        } else fields.get(1).setStatus(null);

        /**
         * Air Quality
         **/
        String curAQ = AppExtensions.formatVal(currentFeed.getField3(), null);
        String prevAQ = AppExtensions.formatVal(lastFeed == null ? null : lastFeed.getField3(), null);
        fields.get(2).setCurValue(curAQ);
        fields.get(2).setPrevValue(prevAQ);

        if (curAQ != null) {
            double airQuality = Double.parseDouble(curAQ);
            if (airQuality >= 0 && airQuality < 51)
                fields.get(2).setStatus(AppExtensions.string(R.string.good));
            else if (airQuality >= 51 && airQuality < 101)
                fields.get(2).setStatus(AppExtensions.string(R.string.moderate));
            else if (airQuality >= 101 && airQuality < 151)
                fields.get(2).setStatus(AppExtensions.string(R.string.unhealthyForSensitiveGroups));
            else if (airQuality >= 151 && airQuality < 201)
                fields.get(2).setStatus(AppExtensions.string(R.string.unhealthy));
            else if (airQuality >= 201 && airQuality < 301)
                fields.get(2).setStatus(AppExtensions.string(R.string.veryUnhealthy));
            else if (airQuality >= 301 && airQuality <= 500)
                fields.get(2).setStatus(AppExtensions.string(R.string.hazardous));
            else
                fields.get(2).setStatus(null);
        } else fields.get(2).setStatus(null);

        /**
         * Water Height
         **/
        String curWH = AppExtensions.formatVal(currentFeed.getField4(), null);
        String prevWH = AppExtensions.formatVal(lastFeed == null ? null : lastFeed.getField4(), null);
        fields.get(3).setCurValue(curWH);
        fields.get(3).setPrevValue(prevWH);
        if (curWH != null) {
            double waterHeight = Double.parseDouble(curWH);
            if (waterHeight > Constants.WATER_HEIGHT_MAX_VALUE)
                fields.get(3).setStatus(AppExtensions.string(R.string.high));
            else
                fields.get(3).setStatus(AppExtensions.string(R.string.normal));
        } else fields.get(3).setStatus(null);

        sp.feedData(SharedPreference.POULTRY_CHANNEL_SP_KEY, currentFeed);
        fieldsAdapter.setFields(fields);
    }

    private void getControllersState() {
        refreshLayout.setRefreshing(false);
        Channel channel = sp.channelData(SharedPreference.CONTROLLER_CHANNEL_SP_KEY + "_" + sp.channelData(SharedPreference.POULTRY_CHANNEL_SP_KEY).getChannelId());
        if (channel.getChannelId() == null) {
            AddControllerFragment.show().setOnAddListener(feed -> lightsAdapter.setFeed(feed));
            return;
        }

        ApiHandler.invoke(this, Feed.class, Request.Method.GET,
                ApiHandler.singleFeedUrl(channel.getChannelId(), channel.getReadKey()),
                new ApiHandler.OnDataListener<Feed>() {
                    @Override
                    public void onData(Feed feed) {
                        lightsAdapter.setFeed(feed);
                    }
                    @Override
                    public void onError() {}
                });
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
        if (item.getItemId() == R.id.drawer_id) {
            if (drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.closeDrawer(GravityCompat.END);
            else drawerLayout.openDrawer(GravityCompat.END);
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
            CustomSnackBar snackBar = new CustomSnackBar(drawerLayout, R.string.network_Error, R.string.retry, CustomSnackBar.Duration.INDEFINITE);
            snackBar.show();
            snackBar.setOnDismissListener(skBar -> {
                networkStatusChangeReceiver.onReceive(MainActivity.this, null);
                skBar.dismiss();
            });
        }
    }
}
