package com.rs.smartpoultryfarm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Objects;

/**
 * Auto start service after reboot of device
 **/
public class AutoStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            new NotifyUserReceiver().startNotifyService(context);
        }
    }
}
