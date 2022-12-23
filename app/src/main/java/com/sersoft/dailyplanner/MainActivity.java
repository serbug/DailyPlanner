package com.sersoft.dailyplanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.sersoft.dailyplanner.domain.Data;
import com.sersoft.dailyplanner.domain.Events;
import com.sersoft.dailyplanner.service.NotificationService;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class MainActivity extends AppCompatActivity {
    private final static int ADD_EVENT = 43;
    private final static String ADD_EVENT_ACTION = "add";
    private final static int MODIFY_EVENT = 37;
    private final static String MODIFY_EVENT_ACTION = "modify";
    private final SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMMM yyyy",Locale.getDefault());
    private Date currentDate;
    private CompactCalendarView compactCalendarView;
    private Events events;
    private  XStream xstream = new XStream(new DomDriver());
    private Button delete;
    private Button show;
    private EditText searchField;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        delete = (Button) findViewById(R.id.button_delete);
        show = (Button) findViewById(R.id.button_show);
        Button search = (Button) findViewById(R.id.button_search);
        Button modify = (Button) findViewById(R.id.button_modify);
        searchField = (EditText) findViewById(R.id.editText);
        Button addBtn = (Button) findViewById(R.id.button_add);
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("");
        compactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);


        actionBar.setTitle(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()))
        ;
        currentDate = new Date();
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener()
        {
            @Override
            public void onDayClick(Date dateClicked) {
                currentDate = dateClicked;
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteData();
                    }
                });
                show.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showData();
                    }
                });
            }
            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                actionBar.setTitle(dateFormatForMonth.format(firstDayOfNewMonth));
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchData(searchField);
            }
        });
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyData();
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddEventActivity.class);
                intent.putExtra("beginDate", currentDate.getTime());
                intent.setAction(ADD_EVENT_ACTION);
                startActivityForResult(intent, ADD_EVENT);
            }
        });
        refresh();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK || (resultCode == RESULT_CANCELED && requestCode == MODIFY_EVENT)) {
            Data event = new Data();
            event.setLocation(data.getExtras().getString("location").toUpperCase())
                    .setDescription(data.getExtras().getString("description"))
                    .setTitle(data.getExtras().getString("title").toUpperCase())
                    .setId(new Random().nextInt(543254))
                    .setCalendarDate(data.getExtras().getLong("time"));
            if (events == null){
                events = new Events();
            }
            ArrayList<Data> datas = new ArrayList<>();
            if (events.getData() == null) {
                events.data = datas;
            }
            events.data.add(event);
            saveToXML();
            refresh();
        }
    }
    private void modifyData() {
        final List<Event> ev = compactCalendarView.getEvents(currentDate);
        if (!ev.isEmpty()) {
            List<String> stockList = new ArrayList<>();
            for (Event event : ev) {
                Data data = (Data) event.getData();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat(" HH:mm EEE, dd MMM yyyy ");
                Date date = new Date();
                date.setTime(data.getCalendarDate());
                stockList.add("-> Title: " + data.getTitle() + "\nDescription: " +
                        data.getDescription() + "\nLocation: " + data.getLocation() +
                        " at " + format.format(date));
            }
            String[] value = new String[stockList.size()];
            value = stockList.toArray(value);
            AlertDialog.Builder alertDialogBuilder = new
                    AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle("* Events *");
            alertDialogBuilder.setItems(value, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Data data = (Data) ev.get(which).getData();
                    Intent intent = new Intent(getApplicationContext(),
                            NotificationService.class);
                    intent.setAction(NotificationService.CANCEL);
                    assert data != null;
                    intent.putExtra("requestCode", data.getId());
                    startService(intent);
                    events.data.remove(data);
                    compactCalendarView.removeEvent(ev.get(which), true);
                    Intent modifyIntent = new Intent(getApplicationContext(),
                            AddEventActivity.class);
                    modifyIntent.putExtra("title", data.getTitle());
                    modifyIntent.putExtra("location", data.getLocation());
                    modifyIntent.putExtra("description", data.getDescription());
                    modifyIntent.putExtra("time", data.getCalendarDate());
                    modifyIntent.setAction(MODIFY_EVENT_ACTION);
                    startActivityForResult(modifyIntent, MODIFY_EVENT);

                }
            });
            AlertDialog dialog = alertDialogBuilder.create();
            dialog.show();

        } else Toast.makeText(MainActivity.this, "No events on this day!",
                Toast.LENGTH_SHORT).show();
    }
    private void searchData(EditText searchField) {
        if(!(events == null)) {
            final List<Data> ev = events.data;
            List<String> stockList = new ArrayList<>();
            for (Data data : ev) {
                if (data.getTitle().contains(searchField.getText().toString().toUpperCase())) {
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat(" HH:mm EEE, dd MMM yyyy ");
                    Date date = new Date();
                    date.setTime(data.getCalendarDate());
                    stockList.add("-> Title: " + data.getTitle() + " \nDescription: " +
                            data.getDescription() + " \nLocation: " + data.getLocation() +
                            " at " + format.format(date));
                }
            }
            if (!stockList.isEmpty()) {
                String[] value = new String[stockList.size()];
                value = stockList.toArray(value);
                AlertDialog.Builder alertDialogBuilder = new
                        AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle("* Events *");
                alertDialogBuilder.setItems(value, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = alertDialogBuilder.create();
                dialog.show();
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                searchField.setText("");
            } else {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                searchField.setText("");
                Toast.makeText(MainActivity.this, "No events found!",
                        Toast.LENGTH_SHORT).show();
            }
        }else Toast.makeText(MainActivity.this, "No events in calendar!",
                Toast.LENGTH_SHORT).show();
    }
    private void showData() {
        final List<Event> ev = compactCalendarView.getEvents(currentDate);
        if (!ev.isEmpty()) {
            List<String> stockList = new ArrayList<>();
            for (Event event : ev) {
                Data data = (Data) event.getData();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat(" HH:mm EEE, dd MMM yyyy ");
                Date date = new Date();
                date.setTime(data.getCalendarDate());
                stockList.add("-> Title: " + data.getTitle() + " \nDescription: " +
                        data.getDescription() + " \nLocation: " + data.getLocation() +
                        " at " + format.format(date));
            }
            String[] value = new String[stockList.size()];
            value = stockList.toArray(value);
            AlertDialog.Builder alertDialogBuilder = new
                    AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle("* Events *");
            alertDialogBuilder.setItems(value, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog dialog = alertDialogBuilder.create();
            dialog.show();
        } else Toast.makeText(MainActivity.this, "No events on this day!",
                Toast.LENGTH_SHORT).show();
    }
    private void deleteData() {
        final List<Event> ev = compactCalendarView.getEvents(currentDate);
        if (!ev.isEmpty()) {
            List<String> stockList = new ArrayList<>();
            for (Event event : ev) {
                Data data = (Data) event.getData();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat(" HH:mm EEE, dd MMM yyyy ");
                Date date = new Date();
                date.setTime(data.getCalendarDate());
                stockList.add("-> Title: " + data.getTitle() + " \nDescription: " +
                        data.getDescription() + " \nLocation: " + data.getLocation() +
                        " at " + format.format(date));
            }
            String[] value = new String[stockList.size()];
            value = stockList.toArray(value);
            AlertDialog.Builder alertDialogBuilder = new
                    AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle("* Events *");
            alertDialogBuilder.setItems(value, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Data data = (Data) ev.get(which).getData();
                    Intent intent = new Intent(getApplicationContext(),
                            NotificationService.class);
                    intent.setAction(NotificationService.CANCEL);
                    intent.putExtra("requestCode", data.getId());
                    startService(intent);
                    events.data.remove(data);
                    compactCalendarView.removeEvent(ev.get(which), true);
                    if (events.data.isEmpty()) {
                        getApplicationContext().deleteFile("events.xml");
                        events = null;
                        refresh();
                    } else {
                        saveToXML();
                        refresh();
                    }
                }
            });
            AlertDialog dialog = alertDialogBuilder.create();
            dialog.show();
        } else Toast.makeText(MainActivity.this, "No events on this day!",
                Toast.LENGTH_SHORT).show();
    }
    private void saveToXML() {
        xstream.processAnnotations(Events.class);
        xstream.processAnnotations(Data.class);

        FileOutputStream outputStream=null;
        try {

            outputStream = new FileOutputStream(new File(getApplicationContext().getFilesDir(), "events.xml"));

            outputStream.write(xstream.toXML(events).getBytes());

           // outputStream.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
           // e.printStackTrace();
        }finally {
            if(outputStream!=null) {
                try{
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace(); // this obviously needs to be refined.
                }
            }
        }
    }
    private void refresh() {
        compactCalendarView.removeAllEvents();
        compactCalendarView.invalidate();
        Intent intent = new Intent(getApplicationContext(), NotificationService.class);
        intent.setAction(NotificationService.UPDATE);
        startService(intent);

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
            inputStream = new FileInputStream(new File(this.getFilesDir(), "events.xml"));
            events = (Events) xstream.fromXML(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (events != null) {
            compactCalendarView.removeAllEvents();
            compactCalendarView.setUseThreeLetterAbbreviation(true);
            compactCalendarView.setEventIndicatorStyle(Color.GREEN);
            compactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            Calendar currentDate = Calendar.getInstance();
            Date time = new Date();
            for (Data data : events.data) {
                time.setTime(data.getCalendarDate());
                currentDate.setTimeInMillis(data.getCalendarDate());
                Event event = new Event(data.getColor(), data.getCalendarDate(), data);
                int interval = calendar.getMaximum(Calendar.DAY_OF_YEAR) -
                        currentDate.get(Calendar.DAY_OF_YEAR);
                if(data.getRepeating() == 0) {

                    compactCalendarView.addEvent(event, true);
                } else if(data.getRepeating() == NotificationBroadcastReceiver.INTERVAL_DAY) {
                    currentDate.setTimeInMillis(data.getCalendarDate());
                    for (int i = 0; i <= interval; i++) {
                        event = new Event(data.getColor(),time.getTime(),data);
                        compactCalendarView.addEvent(event,true);
                        time.setTime(time.getTime() +
                                NotificationBroadcastReceiver.INTERVAL_DAY);
                    }
                } else if(data.getRepeating() == NotificationBroadcastReceiver.INTERVAL_WEEK)
                {
                    currentDate.setTimeInMillis(data.getCalendarDate());
                    for (int i = 0; i <= interval/7; i++) {
                        event = new Event(data.getColor(),time.getTime(),data);
                        compactCalendarView.addEvent(event,true);
                        time.setTime(time.getTime() +
                                NotificationBroadcastReceiver.INTERVAL_WEEK);
                    }
                } else if(data.getRepeating() == NotificationBroadcastReceiver.INTERVAL_MONTH)
                {
                    currentDate.setTimeInMillis(data.getCalendarDate());
                    for (int i = 0; i <= interval/30; i++) {
                        event = new Event(data.getColor(),time.getTime(),data);
                        compactCalendarView.addEvent(event,true);
                        time.setTime(time.getTime() +
                                NotificationBroadcastReceiver.INTERVAL_MONTH);
                    }
                } else if(data.getRepeating() == NotificationBroadcastReceiver.INTERVAL_YEAR)
                {
                    currentDate.setTimeInMillis(data.getCalendarDate());
                    for (int i = 0; i <= 10; i++) {
                        event = new Event(data.getColor(),time.getTime(),data);
                        compactCalendarView.addEvent(event,true);
                        time.setTime(time.getTime() +
                                NotificationBroadcastReceiver.INTERVAL_YEAR);
                    }
                }
            }
        }
    }
    private void updateData(Data info) {
        if (events == null) events = new Events();
        ArrayList<Data> data = new ArrayList<>();
        if (events.getData() == null) {
            events.data = data;
        }
        events.data.add(info);
        saveToXML();
        refresh();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        } else if (id == R.id.current_day) {

            compactCalendarView.setCurrentDate(Calendar.getInstance(Locale.getDefault()).getTime());

            actionBar.setTitle(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()))
            ;
        }
        return false;
    }
}