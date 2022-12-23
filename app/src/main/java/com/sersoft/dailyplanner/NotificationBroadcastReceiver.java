package com.sersoft.dailyplanner;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import com.sersoft.dailyplanner.domain.Data;

import java.util.Calendar;


public class NotificationBroadcastReceiver extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "notification-id";
    public static long INTERVAL_MINUTE = AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15;
    public static long INTERVAL_HOUR = AlarmManager.INTERVAL_FIFTEEN_MINUTES * 4;
    public static long INTERVAL_DAY = AlarmManager.INTERVAL_DAY;
    public static long INTERVAL_WEEK = INTERVAL_DAY * 7;
    public static long INTERVAL_MONTH =
            Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH) * INTERVAL_DAY;
    public static long INTERVAL_YEAR =
            Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR) * INTERVAL_DAY;
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Data info = new Data();
        info
                .setLocation(intent.getExtras().getString("location"))
                .setDescription(intent.getExtras().getString("description"))
                .setTitle(intent.getExtras().getString("title"));
        Notification notification = getNotification(info,context);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(id, notification);
    }
    private Notification getNotification(Data data,Context context) {
        Notification.Builder builder = new Notification.Builder(context);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder
                .setContentTitle(data.getTitle())
                .setShowWhen(false)
                .setContentText(data.getDescription()+ "\n" + "at " + data.getLocation())
                .setSmallIcon(R.drawable.ic_black_event)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_black_event))
                .setSound(alarmSound)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setLights(0xff00ff00, 300, 100);
        return builder.build();
    }
}
