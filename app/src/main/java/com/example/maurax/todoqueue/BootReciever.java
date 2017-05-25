package com.example.maurax.todoqueue;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.preference.PreferenceManager;

import static android.R.id.list;
import static android.content.Context.ALARM_SERVICE;

/**
 * Created by marcus on 25/05/2017.
 */

public class BootReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String alarm = prefs.getString("Alarm", null);
        if(alarm != null){
            Calendar c = Calendar.getInstance();
            final int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
            final int minuteOfDay = c.get(Calendar.MINUTE);
            String list = prefs.getString("AlarmList", null);
            String[] time = alarm.split(":");
            c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
            c.set(Calendar.MINUTE, Integer.parseInt(time[1]));
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            if (Integer.parseInt(time[0])<hourOfDay || (Integer.parseInt(time[0])==hourOfDay && Integer.parseInt(time[1])<=minuteOfDay))
                c.add(Calendar.HOUR, 24);

            Intent alarmIntent = new Intent(context, NotificationReciever.class);
            alarmIntent.setAction("Alarm");
            alarmIntent.putExtra(NotificationReciever.NOTIFICATION_LIST, list);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            am.set(AlarmManager.RTC, c.getTimeInMillis(), pi);
        }
    }
}
