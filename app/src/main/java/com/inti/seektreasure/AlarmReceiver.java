package com.inti.seektreasure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Create by OCF on 2019/10/24
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, NotificationService.class);
        context.startService(i);
    }
}

