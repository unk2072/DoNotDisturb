package com.unk2072.donotdisturb;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

public class MyService extends IntentService {
    private static final String TAG = "MyService";

    public MyService(){
        super(TAG);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final int mode = intent.getIntExtra(Const.RUN_MODE, Const.MODE_START);
        Log.i(TAG, "onHandleIntent mode=" + mode);

        switch (mode) {
            case Const.MODE_START:
                doRefreshAlarm();
                break;
            case Const.MODE_ON:
                doSetRingerMode(true);
                break;
            case Const.MODE_OFF:
                doSetRingerMode(false);
                break;
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    private boolean doRefreshAlarm() {
        Log.i(TAG, "doRefreshAlarm");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean run_flag = pref.getBoolean(Const.RUN_FLAG, false);
        if (run_flag) {
            setAlarm(true);
            setAlarm(false);
            doSetRingerMode(isDoNotDisturb());
        } else {
            cancelAlarm(true);
            cancelAlarm(false);
            doSetRingerMode(false);
        }
        return true;
    }

    private boolean doSetRingerMode(final boolean flag) {
        Log.i(TAG, "doSetRingerMode flag=" + flag);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = pref.edit();
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (flag) {
            edit.putInt(Const.RINGER_MODE, am.getRingerMode());
            edit.apply();
            am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        } else {
            am.setRingerMode(pref.getInt(Const.RINGER_MODE, am.getRingerMode()));
            edit.remove(Const.RINGER_MODE);
            edit.apply();
        }
        return true;
    }

    private boolean isDoNotDisturb() {
        long time_now, time_on, time_off;
        time_now = System.currentTimeMillis();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time_now);
        cal.setTimeZone(TimeZone.getDefault());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, pref.getInt(Const.ON_HOUR, 22));
        cal.set(Calendar.MINUTE, pref.getInt(Const.ON_MINUTE, 0));
        time_on = cal.getTimeInMillis();
        if (time_now < time_on) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            time_on = cal.getTimeInMillis();
        }

        cal.set(Calendar.HOUR_OF_DAY, pref.getInt(Const.OFF_HOUR, 6));
        cal.set(Calendar.MINUTE, pref.getInt(Const.OFF_MINUTE, 0));
        time_off = cal.getTimeInMillis();
        if (time_off < time_on) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            time_off = cal.getTimeInMillis();
        }

        return time_now < time_off;
    }

    private boolean setAlarm(final boolean flag) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int hour, minute;
        if (flag) {
            hour = pref.getInt(Const.ON_HOUR, 22);
            minute = pref.getInt(Const.ON_MINUTE, 0);
        } else {
            hour = pref.getInt(Const.OFF_HOUR, 6);
            minute = pref.getInt(Const.OFF_MINUTE, 0);
        }

        Intent i = new Intent(this, MyService.class);
        i.putExtra(Const.RUN_MODE, flag ? Const.MODE_ON : Const.MODE_OFF);
        PendingIntent pi = PendingIntent.getService(this, flag ? 0 : 1, i, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.setTimeZone(TimeZone.getDefault());
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);

        if (System.currentTimeMillis() > cal.getTimeInMillis()) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        return true;
    }

    private boolean cancelAlarm(final boolean flag) {
        Intent i = new Intent(this, MyService.class);
        PendingIntent pi = PendingIntent.getService(this, flag ? 0 : 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(pi);
        return true;
    }
}
