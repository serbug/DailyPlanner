package com.sersoft.dailyplanner.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class Data{
    @XStreamAlias("ID")
    private int id;
    @XStreamAlias("Title")
    private String title;
    @XStreamAlias("Description")
    private String description;
    @XStreamAlias("Location")
    private String location;
    @XStreamAlias("CalendarDate")
    private long calendarDate;
    @XStreamAlias("Repeating")
    private long repeating;
    @XStreamAlias("Color")
    private int color;
    @XStreamAlias("Remind")
    private int remind;
    public int getRemind() {
        return remind;
    }
    public Data setRemind(int remind) {
        this.remind = remind;
        return this;
    }
    public int getColor() {
        return color;
    }
    public Data setColor(int color) {
        this.color = color;
        return this;
    }
    public long getRepeating() {
        return repeating;
    }
    public Data setRepeating(long repeating) {
        this.repeating = repeating;
        return this;
    }
    public long getCalendarDate() {
        return calendarDate;
    }
    public Data setCalendarDate(long calendarDate) {
        this.calendarDate = calendarDate;
        return this;
    }
    public int getId() {
        return id;
    }
    public Data setId(int id) {
        this.id = id;
        return this;
    }
    public String getTitle() {
        return title;
    }
    public Data setTitle(String title) {
        this.title = title;
        return this;
    }
    public String getDescription() {
        return description;
    }
    public Data setDescription(String description) {
        this.description = description;
        return this;
    }
    public String getLocation() {
        return location;
    }
    public Data setLocation(String location) {
        this.location = location;
        return this;
    }
}