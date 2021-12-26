package com.rs.smartpoultryfarm.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.controller.AppController;
import com.rs.smartpoultryfarm.receiver.NetworkStatusChangeReceiver;
import com.rs.smartpoultryfarm.util.Constants;
import com.rs.smartpoultryfarm.util.SharedPreference;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private boolean                     isSplashDone = false;
    private AppCompatTextView           networkCheckingTv;
    private SharedPreference            sp;
    private NetworkStatusChangeReceiver networkStatusChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        AppController.setActivity(SplashActivity.this);
        init();
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


    private void init() {
        networkCheckingTv = findViewById(R.id.networkCheckingTv);
        sp = new SharedPreference(SplashActivity.this);
        networkStatusChangeReceiver = new NetworkStatusChangeReceiver();
        splashTimer();
    }

    /**
     * Show splash screen for 2 seconds
     **/
    private void splashTimer() {
        new Handler().postDelayed(() -> {
            if (Constants.IS_NETWORK_CONNECTED) {
                launchNewActivity();
            } else {
                networkCheckingTv.setVisibility(View.VISIBLE);
            }

            isSplashDone = true;
        }, Constants.SPLASH_TIME_OUT);
    }

    /**
     * Launch Main Activity
     **/
    private void launchNewActivity() {
        Intent intent = new Intent(SplashActivity.this, sp.isLoggedIn() ? MainActivity.class : LoginActivity.class);
        startActivity(intent);
        finish();
    }


    /**
     * Monitor Internet Connection
     **/
    public void updateInternetConnectionStatus(boolean isConnected) {
        if (isConnected) {
            if (isSplashDone) {
                launchNewActivity();
            }
        }
    }
}
