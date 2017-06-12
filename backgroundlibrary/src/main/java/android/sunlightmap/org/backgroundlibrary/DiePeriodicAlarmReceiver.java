package android.sunlightmap.org.backgroundlibrary;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class DiePeriodicAlarmReceiver extends WakefulBroadcastReceiver {

    public static final int REQUEST_CODE = 1234;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, DieIntentService.class);
        context.startService(serviceIntent);

        Scheduler scheduler = new Scheduler(context);
        scheduler.scheduleAlarm();
    }
}
