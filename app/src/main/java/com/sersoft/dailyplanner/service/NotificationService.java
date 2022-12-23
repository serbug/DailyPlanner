package com.sersoft.dailyplanner.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.sersoft.dailyplanner.NotificationBroadcastReceiver;
import com.sersoft.dailyplanner.domain.Data;
import com.sersoft.dailyplanner.domain.Events;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Date;
public class NotificationService extends IntentService {
    public static final String CANCEL = "CANCEL";
    public static final String UPDATE = "UPDATE";
    private Events events;
    public NotificationService() {
        super("MyOrganizerService");
    }

    XStream xstream = new XStream(new DomDriver());

    @Override
    protected void onHandleIntent(Intent intent) {
        if (UPDATE.equals(intent.getAction())) {
            executeUpdate();
        } else if(CANCEL.equals(intent.getAction())) {
            executeCancel(intent.getExtras().getInt("requestCode"));
        }
    }
    private void executeCancel(int requestCode){
        Intent notificationIntent = new Intent(this, NotificationBroadcastReceiver.class);
        notificationIntent.setAction(requestCode+"");
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    private void executeUpdate() {
        xstream.addPermission(NoTypePermission.NONE);
        // allow some basics
        xstream.addPermission(NullPermission.NULL);
        xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
        xstream.allowTypeHierarchy(Collection.class);
// allow any type from the same package
        xstream.allowTypesByWildcard(new String[] {
                "com.sersoft.dailyplanner.**"
        });
        xstream.processAnnotations(Events.class);
        xstream.processAnnotations(Data.class);
        InputStream inputStream;
        try {
            inputStream = Files.newInputStream(new File(this.getFilesDir(), "events.xml").toPath());
            events = (Events) xstream.fromXML(inputStream);
            inputStream.close();
        } catch (IOException ignored) {
        }
        if (events != null) {
            for (Data data : events.data) {
                Intent notificationIntent = new Intent(this,
                        NotificationBroadcastReceiver.class);
                notificationIntent.putExtra(NotificationBroadcastReceiver.NOTIFICATION_ID,
                        data.getId());
                notificationIntent.putExtra("title", data.getTitle());
                notificationIntent.putExtra("location", data.getLocation());
                notificationIntent.putExtra("description", data.getDescription());
                notificationIntent.setAction(data.getId() + "");
                @SuppressLint("UnspecifiedImmutableFlag")
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, data.getId(),
                        notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager)
                        getSystemService(Context.ALARM_SERVICE);
                Date g = new Date();
                g.setTime(data.getCalendarDate());
                if (new Date().before(g)) {
                    if(data.getRepeating() == 0) {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, data.getCalendarDate() -
                                data.getRemind(), pendingIntent);
                    } else
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                                data.getCalendarDate(), data.getRepeating() - data.getRemind(), pendingIntent);
                }
            }
        }
    }
}