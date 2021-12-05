package com.rs.smartpoultryfarm.remote;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.SEND_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.controller.AppController;
import com.rs.smartpoultryfarm.fragment.AlertDialogFragment;
import com.rs.smartpoultryfarm.util.AppExtensions;

import java.util.List;

public class PermissionManager {

    public enum Permission {
        CONTACT, SMS
    }

    private Context     context;
    private Activity    activity;
    private Permission  permission;
    private boolean     isGranted;
    private boolean     showDialog;
    private OnPermissionListener permissionListener;

    public PermissionManager() {
        this.activity = AppController.getActivity();
    }

    public PermissionManager(Permission permission) {
        this.activity = AppController.getActivity();
        this.permission = permission;
    }

    public PermissionManager(Permission permission, boolean showDialog) {
        this.context = AppController.getContext();
        this.activity = AppController.getActivity();
        this.permission = permission;
        this.isGranted = false;
        this.showDialog = showDialog;
    }

    public PermissionManager(Permission permission, boolean showDialog, OnPermissionListener permissionListener) {
        this.context = AppController.getContext();
        this.activity = AppController.getActivity();
        this.permission = permission;
        this.isGranted = false;
        this.showDialog = showDialog;
        this.permissionListener = permissionListener;
    }

    public boolean isGranted(){
        switch (permission){
            case CONTACT:
                isGranted = checkSelfPermission(context, READ_CONTACTS) == PERMISSION_GRANTED;
                if(!isGranted && showDialog) showPermissionDialog(READ_CONTACTS);
                break;

            case SMS:
                isGranted = checkSelfPermission(context, SEND_SMS) == PERMISSION_GRANTED;
                if(!isGranted && showDialog) showPermissionDialog(SEND_SMS);
                break;
        }

        return isGranted;
    }

    public void showPermissionDialogs(){
        Dexter.withActivity(activity)
                .withPermissions(READ_CONTACTS, SEND_SMS)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    @SuppressLint("MissingPermission")
                    public void onPermissionsChecked(MultiplePermissionsReport report) {}

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {}
                })
                .check();
    }

    public void showPermissionDialog(String manifestPermission){
        Dexter.withActivity(activity).withPermission(manifestPermission).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                if(permissionListener != null) permissionListener.onPermissionGranted(response);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                showPermissionSettingDialog();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    public interface OnPermissionListener{
        void onPermissionGranted(PermissionGrantedResponse response);
    }

    private void showPermissionSettingDialog(){
        String title = null;    String message = null;

        switch (permission){
            case CONTACT:
                title = AppExtensions.getString(R.string.contactPermission);   message = AppExtensions.getString(R.string.contactPermissionMessage);
                break;

            case SMS:
                title = AppExtensions.getString(R.string.smsPermission);   message = AppExtensions.getString(R.string.smsPermissionMessage);
                break;
        }

        AlertDialogFragment.show(title, message, R.string.cancel, R.string.openSettings)
                .setOnDialogListener(new AlertDialogFragment.OnDialogListener() {
                    @Override public void onLeftButtonClick() {}
                    @Override public void onRightButtonClick() { goToPermissionSetting(); }
                });
    }

    public void goToPermissionSetting(){
        Activity activity = AppController.getActivity();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    public void goToNotificationPermissionSetting(){
        Activity activity = AppController.getActivity();
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

        /**
         * for Android 5-7
         **/
        intent.putExtra("app_package", activity.getPackageName());
        intent.putExtra("app_uid", activity.getApplicationInfo().uid);

        /**
         * for Android 8 and above
         **/
        intent.putExtra("android.provider.extra.APP_PACKAGE", activity.getPackageName());

        activity.startActivity(intent);
    }
}
