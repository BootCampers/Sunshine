package android.sunlightmap.org.backgroundlibrary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Created by baphna on 6/11/2017.
 */

public class Scheduler {

    private static Context mContext;
    private final AlarmManager mAlarmManager;

    public Scheduler(Context mContext) {
        this.mContext = mContext;
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    public void scheduleAlarm() {
        Intent intent = new Intent(mContext, DiePeriodicAlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                DiePeriodicAlarmReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        long now = SystemClock.elapsedRealtime(); //alarm set right now

        mAlarmManager.setInexactRepeating(AlarmManager.RTC, now, AlarmManager.INTERVAL_HOUR,
                pendingIntent);
    }

    public void cancelAlarm() {
        Intent intent = new Intent(mContext, DiePeriodicAlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                DiePeriodicAlarmReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.cancel(pendingIntent);
    }
}
