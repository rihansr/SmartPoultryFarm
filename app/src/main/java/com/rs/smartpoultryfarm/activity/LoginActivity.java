package com.rs.smartpoultryfarm.activity;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;

import com.android.volley.Request;
import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.api.API;
import com.rs.smartpoultryfarm.model.AgroDataModel;
import com.rs.smartpoultryfarm.receiver.NetworkStatusChangeReceiver;
import com.rs.smartpoultryfarm.util.AppExtensions;
import com.rs.smartpoultryfarm.util.Constants;
import com.rs.smartpoultryfarm.util.CustomSnackBar;
import com.rs.smartpoultryfarm.util.SharedPreference;

import java.util.Collections;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private RelativeLayout              rootLayout;
    private AppCompatEditText           emailInput;
    private AppCompatEditText           passwordInput;
    private FrameLayout                 passwordIconHolder;
    private AppCompatImageView          passwordIcon;
    private AppCompatButton             login_Btn;
    private ProgressDialog              progressDialog;
    private SharedPreference            sp;
    private NetworkStatusChangeReceiver networkStatusChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initId();

        init();
    }

    /**
     * {@link #onResume()} called when the activity is in the resumed state.
     **/
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkStatusChangeReceiver, new IntentFilter(CONNECTIVITY_ACTION));
    }


    /**
     * {@link #onPause()} called when an activity is about to lose focus.
     **/
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkStatusChangeReceiver);
    }

    private void initId() {
        rootLayout = findViewById(R.id.rootLayout);
        emailInput = findViewById(R.id.id_Input);
        passwordInput = findViewById(R.id.key_Input);
        passwordIconHolder = findViewById(R.id.passwordIconHolder);
        passwordIcon = findViewById(R.id.password_Icon);
        login_Btn = findViewById(R.id.login_Btn);
        progressDialog = new ProgressDialog(LoginActivity.this, R.style.ProgressDialog);
        sp = new SharedPreference(LoginActivity.this);
        networkStatusChangeReceiver = new NetworkStatusChangeReceiver();
    }

    private void init() {
        passwordIconHolder.setOnClickListener(v -> {
            if (passwordInput.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                passwordIcon.setImageResource(R.drawable.ic_password_visible_off);
                passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                passwordIcon.setImageResource(R.drawable.ic_password_visible_on);
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        login_Btn.setOnClickListener(v -> {
            if (!Constants.IS_NETWORK_CONNECTED) {
                new CustomSnackBar(rootLayout, AppExtensions.getString(R.string.network_Error), false, CustomSnackBar.Duration.LONG).show();
                return;
            }

            signIn();
        });
    }

    private void signIn() {
        final String id = Objects.requireNonNull(emailInput.getText()).toString().trim();
        final String key = Objects.requireNonNull(passwordInput.getText()).toString().trim();

        /**
         *  Validation
         **/
        if (TextUtils.isEmpty(Objects.requireNonNull(id))) {
            emailInput.setError(getResources().getString(R.string.channelId_Error));
            AppExtensions.requestFocus(emailInput);
            return;
        } else if (id.length() < 7) {
            emailInput.setError(getResources().getString(R.string.validChannelId_Error));
            AppExtensions.requestFocus(emailInput);
            return;
        } else if (TextUtils.isEmpty(Objects.requireNonNull(key))) {
            passwordInput.setError(getResources().getString(R.string.key_Error));
            AppExtensions.requestFocus(passwordInput);
            return;
        } else if (key.length() < 16) {
            passwordInput.setError(getResources().getString(R.string.validKeyError));
            AppExtensions.requestFocus(passwordInput);
            return;
        }

        progressDialog.setMessage(getResources().getString(R.string.processing));
        progressDialog.setCancelable(false);
        progressDialog.show();

        /**
         * Get data from https://thingspeak.com/
         **/
        API.invoke(LoginActivity.this, Request.Method.GET, API.getDataFeedURL(id, key, 1), data -> {
            progressDialog.dismiss();
            if (data == null || data.getFeeds().size() == 0 || data.getCode() == 404) {
                new CustomSnackBar(rootLayout, R.string.dataNotExist, R.string.retry, CustomSnackBar.Duration.SHORT).show();
                return;
            }
            launchActivity(id, key);
        });
    }

    private void launchActivity(String id, String key) {
        sp.channelData(SharedPreference.CHANNEL_ID_SP_KEY, id);
        sp.channelData(SharedPreference.CHANNEL_KEY_SP_KEY, key);
        sp.setLoggedIn(true);

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * {@link #onStop()} called when the activity is no longer visible to the user
     **/
    @Override
    protected void onStop() {
        super.onStop();
        try {
            progressDialog.dismiss();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Hide soft keyboard when click outside
     **/
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * Monitor Internet Connection
     **/
    public void updateInternetConnectionStatus(boolean isConnected) {
        if (isConnected) {
            if (CustomSnackBar.snackbar != null) CustomSnackBar.snackbar.dismiss();
        } else {
            CustomSnackBar snackBar = new CustomSnackBar(rootLayout, R.string.network_Error, R.string.retry, CustomSnackBar.Duration.INDEFINITE);
            snackBar.show();
            snackBar.setOnDismissListener(skBar -> {
                networkStatusChangeReceiver.onReceive(LoginActivity.this, null);
                skBar.dismiss();
            });
        }
    }
}