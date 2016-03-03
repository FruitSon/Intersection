package com.dartmouth.cs.intersection;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by RZ on 3/2/16.
 */


public class Scheduler {

    public static void setSchedule(Context context) {

        // the request code distinguish different stress meter schedule instances

//        Intent intent = new Intent(context, AlarmReceiver.class);
        System.out.println("gpsgpsgps");
        Intent intent = new Intent(context, GPSService.class);
        PendingIntent pi = PendingIntent.getService(context, 10, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //RTC_WAKEUP, wake up the device when it goes off.
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 30*1000, pi);
    }

    public static void setSchedule(Context context, int duration) {

        Intent intent = new Intent(context, PollingService.class);
        PendingIntent pi = PendingIntent.getService(context, 10, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //RTC_WAKEUP, wake up the device when it goes off.
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), duration, pi);
    }

}
