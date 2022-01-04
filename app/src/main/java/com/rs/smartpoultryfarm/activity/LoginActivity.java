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
import com.rs.smartpoultryfarm.api.ApiHandler;
import com.rs.smartpoultryfarm.model.Channel;
import com.rs.smartpoultryfarm.model.PoultryData;
import com.rs.smartpoultryfarm.receiver.NetworkStatusChangeReceiver;
import com.rs.smartpoultryfarm.util.AppExtensions;
import com.rs.smartpoultryfarm.util.Constants;
import com.rs.smartpoultryfarm.util.CustomSnackBar;
import com.rs.smartpoultryfarm.util.SharedPreference;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private RelativeLayout              rootLayout;
    private AppCompatEditText           idInput;
    private AppCompatEditText           keyInput;
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
        idInput = findViewById(R.id.id_Input);
        keyInput = findViewById(R.id.key_Input);
        passwordIconHolder = findViewById(R.id.passwordIconHolder);
        passwordIcon = findViewById(R.id.password_Icon);
        login_Btn = findViewById(R.id.login_Btn);
        progressDialog = new ProgressDialog(LoginActivity.this, R.style.ProgressDialog);
        sp = new SharedPreference(LoginActivity.this);
        networkStatusChangeReceiver = new NetworkStatusChangeReceiver();
    }

    private void init() {
        passwordIconHolder.setOnClickListener(v -> {
            if (keyInput.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                passwordIcon.setImageResource(R.drawable.ic_password_visible_off);
                keyInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                passwordIcon.setImageResource(R.drawable.ic_password_visible_on);
                keyInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        login_Btn.setOnClickListener(v -> {
            if (!Constants.IS_NETWORK_CONNECTED) {
                new CustomSnackBar(rootLayout, AppExtensions.string(R.string.network_Error), false, CustomSnackBar.Duration.LONG).show();
                return;
            }

            signIn();
        });
    }

    private void signIn() {
        final String id = Objects.requireNonNull(idInput.getText()).toString().trim();
        final String key = Objects.requireNonNull(keyInput.getText()).toString().trim();

        /**
         *  Validation
         **/
        if (TextUtils.isEmpty(Objects.requireNonNull(id))) {
            idInput.setError(getResources().getString(R.string.channelId_Error));
            AppExtensions.requestFocus(idInput);
            return;
        } else if (id.length() < 7) {
            idInput.setError(getResources().getString(R.string.validChannelId_Error));
            AppExtensions.requestFocus(idInput);
            return;
        } else if (TextUtils.isEmpty(Objects.requireNonNull(key))) {
            keyInput.setError(getResources().getString(R.string.key_Error));
            AppExtensions.requestFocus(keyInput);
            return;
        } else if (key.length() < 16) {
            keyInput.setError(getResources().getString(R.string.validKeyError));
            AppExtensions.requestFocus(keyInput);
            return;
        }

        progressDialog.setMessage(getResources().getString(R.string.processing));
        progressDialog.setCancelable(false);
        progressDialog.show();

        /**
         * Get data from https://thingspeak.com/
         **/
        ApiHandler.invoke(LoginActivity.this, PoultryData.class, Request.Method.GET,
                ApiHandler.feedsUrl(id, key, 1),
                new ApiHandler.OnDataListener<PoultryData>() {
                    @Override
                    public void onData(PoultryData data) {
                        progressDialog.dismiss();
                        if (data == null || data.getCode() == 404) {
                            new CustomSnackBar(rootLayout, R.string.dataNotExist, R.string.retry, CustomSnackBar.Duration.SHORT).show();
                            return;
                        }
                        launchActivity(id, key);
                    }

                    @Override
                    public void onError() {
                        progressDialog.dismiss();
                        new CustomSnackBar(rootLayout, R.string.dataNotExist, R.string.retry, CustomSnackBar.Duration.SHORT).show();
                    }
                });
    }

    private void launchActivity(String id, String key) {
        sp.channelData(SharedPreference.POULTRY_CHANNEL_SP_KEY, new Channel(id, key, null));
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