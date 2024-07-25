package com.toda.todamoon_v2.model;

import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DriverQueueingModel {
    private String name;
    private String tricycleNumber;
    private Timestamp joinTime;

    // Default constructor required for Firestore
    public DriverQueueingModel() {}

    public DriverQueueingModel(String name, String tricycleNumber, Timestamp joinTime) {
        this.name = name;
        this.tricycleNumber = tricycleNumber;
        this.joinTime = joinTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTricycleNumber() {
        return tricycleNumber;
    }

    public void setTricycleNumber(String tricycleNumber) {
        this.tricycleNumber = tricycleNumber;
    }

    public Timestamp getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Timestamp joinTime) {
        this.joinTime = joinTime;
    }

    // Method to format and display the join time
    public String getFormattedJoinTime() {
        if (joinTime != null) {
            Date date = joinTime.toDate();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return dateFormat.format(date);
        } else {
            return "No time available";
        }
    }
}
