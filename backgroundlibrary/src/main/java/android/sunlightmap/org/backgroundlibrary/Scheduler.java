package android.sunlightmap.org.backgroundlibrary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by baphna on 6/11/2017.
 */

public class Scheduler {

    private static Context mContext;

    public Scheduler(Context mContext) {
        this.mContext = mContext;
    }

    public void scheduleAlarm() {
        Intent intent = new Intent(mContext, DiePeriodicAlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                DiePeriodicAlarmReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        long now = System.currentTimeMillis(); //alarm set right now
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now, /*30000*/AlarmManager.INTERVAL_HOUR,
                pendingIntent);
    }

    public void cancelAlarm() {
        Intent intent = new Intent(mContext, DiePeriodicAlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                DiePeriodicAlarmReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
