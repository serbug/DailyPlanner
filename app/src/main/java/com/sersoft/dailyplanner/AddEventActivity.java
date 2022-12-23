package com.sersoft.dailyplanner;

import android.annotation.SuppressLint;

import android.app.DatePickerDialog;
import android.app.Dialog;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AddEventActivity extends AppCompatActivity {
    static Date parsedDate;
    private TextView descriptionLabel,eventTitleLabel, locationLabel;

    static final Calendar calendar = Calendar.getInstance();
    @SuppressLint("ConstantLocale")
    static final SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Create / Edit | * Events *");

        TextView startTimeLabel = (TextView) findViewById(R.id.time_label);
        TextView beginDateLabel = (TextView) findViewById(R.id.date_label);
        eventTitleLabel = (EditText) findViewById(R.id.event_title_label);
        descriptionLabel = (EditText) findViewById(R.id.description_label);
        locationLabel = (EditText) findViewById(R.id.location_label);
        Date date = new Date();
        if(getIntent().getAction().equals("modify")) {
            date.setTime(getIntent().getExtras().getLong("time"));
            calendar.setTime(date);
            beginDateLabel.setText(dateFormatForMonth.format(date));
            startTimeLabel.setText(timeFormat.format(date));
            eventTitleLabel.setText(getIntent().getExtras().getString("title"));
            locationLabel.setText(getIntent().getExtras().getString("location"));
            descriptionLabel.setText(getIntent().getExtras().getString("description"));
        } else if(getIntent().getAction().equals("add")){
            date.setTime(getIntent().getLongExtra("beginDate",-1));
            calendar.setTime(date);
            beginDateLabel.setText(dateFormatForMonth.format(date));
        }
        startTimeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });
        beginDateLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }
        @SuppressLint("SetTextI18n")
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            TextView viewById = (TextView) requireActivity().findViewById(R.id.time_label);
            if(minute < 10) {
                viewById.setText(hourOfDay + ":0" + minute);
            }
            else {
                viewById.setText(hourOfDay + ":" + minute);
            }
            calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
            calendar.set(Calendar.MINUTE,minute);
            calendar.set(Calendar.SECOND,0);
            calendar.set(Calendar.MILLISECOND,0);
            parsedDate = calendar.getTime();
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }
        public void onDateSet(DatePicker view, int year, int month, int day) {
            TextView beginDateLabel = (TextView) requireActivity().findViewById(R.id.date_label);
            calendar.set(Calendar.YEAR,year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,day);
            parsedDate = calendar.getTime();
            beginDateLabel.setText(dateFormatForMonth.format(parsedDate));
        }
    }
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.add, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.cancel_button) {
            Intent intent = new Intent();
            if(getIntent().getAction().equals("modify")){
                intent.putExtra("title", getIntent().getExtras().getString("title"));
                intent.putExtra("location", getIntent().getExtras().getString("location"));
                intent.putExtra("description",getIntent().getExtras().getString("description"));
                intent.putExtra("time", getIntent().getExtras().getLong("time"));
            }
            setResult(RESULT_CANCELED, intent);
            finish();
            return true;
        }else if(id == R.id.save_button){
            if(parsedDate != null && !TextUtils.isEmpty(eventTitleLabel.getText().toString())
                    && !TextUtils.isEmpty(locationLabel.getText().toString())
                    && !TextUtils.isEmpty(descriptionLabel.getText().toString())) {
                Intent intent = new Intent();
                intent.putExtra("title", eventTitleLabel.getText().toString());
                intent.putExtra("location", locationLabel.getText().toString());
                intent.putExtra("description", descriptionLabel.getText().toString());
                intent.putExtra("time", parsedDate.getTime());
                setResult(RESULT_OK, intent);
                finish();
            } else Toast.makeText(AddEventActivity.this, "Enter Dates", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        if(getIntent().getAction().equals("modify")){
            intent.putExtra("title", getIntent().getExtras().getString("title"));
            intent.putExtra("location", getIntent().getExtras().getString("location"));
            intent.putExtra("description",getIntent().getExtras().getString("description"));
            intent.putExtra("time", getIntent().getExtras().getLong("time"));
        }
        setResult(RESULT_CANCELED, intent);
        finish();
        super.onBackPressed();
    }
}
