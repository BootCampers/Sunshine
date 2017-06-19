package org.sunlightmap.backgroundlibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;

public class DiePeriodicAlarmReceiver extends WakefulBroadcastReceiver {

    public static final int REQUEST_CODE = 1234;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        boolean isAppOn = sharedPreferences.getBoolean(Constants.APP_ON, false);

        if(isAppOn) {
            Intent serviceIntent = new Intent(context, DieIntentService.class);
            context.startService(serviceIntent);

            if (intent.getAction() != null &&
                    intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                Scheduler scheduler = new Scheduler(context);
                scheduler.scheduleAlarm();
            }
        }
    }
}
