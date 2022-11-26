package com.seg2105.termprojectgroup21.Objects;

public class ScheduleItem {
    private String course_id, start_time, end_time;
    private int day; // from 0 to 4, Monday to Friday
    public ScheduleItem(String course_id, int day, String start_time, String end_time) {
        this.course_id = course_id;
        this.day = day;
        this.start_time = start_time;
        this.end_time = end_time;
    }
    public String getId() { return course_id; }
    public int getDay() { return day; }
    public String getStartTime() { return start_time; }
    public String getEndTime() { return end_time; }
}
