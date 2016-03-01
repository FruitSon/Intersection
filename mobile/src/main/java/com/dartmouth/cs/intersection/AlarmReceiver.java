package com.dartmouth.cs.intersection;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        startUpdate(context);
    }

    private void startUpdate(Context context) {
        int notificationId = 001;

        // Build intent for notification content
        Intent viewIntent = new Intent(context, GPSService.class);

        Bundle b = new Bundle();
        b.putInt("page", 1); //Your id
        viewIntent.putExtras(b); //Put your id to your next Intent

        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(context, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(context)
//                        .setSmallIcon(R.drawable.ic_launcher)
//                        .setContentTitle("Stress")
//                        .setDefaults(Notification.DEFAULT_ALL)
//                        .setAutoCancel(true)
//                        .setContentText("Start to take measurement")
//                        .setContentIntent(viewPendingIntent);
//
//// Get an instance of the NotificationManager service
//        NotificationManagerCompat notificationManager =
//                NotificationManagerCompat.from(context);
//
//// Build the notification and issues it with notification manager.
//        notificationManager.notify(notificationId, notificationBuilder.build());
//
//        //EMAAlert.getAlertObject().startAlert(context);
//
//        //notificationManager.cancel(notificationId);

    }
}
