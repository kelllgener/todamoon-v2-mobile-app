package com.toda.todamoon_v2.model;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BillingModel {

    private double amount;
    private Timestamp timestamp;
    private String description;

    public BillingModel() {
        // Firestore requires a public no-argument constructor
    }

    public BillingModel(int amount, Timestamp timestamp, String description) {
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedAmount() {
        return String.valueOf(amount);
    }

    public String getFormattedTimestamp() {
        Date date = timestamp.toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy"); // Format: September 1, 2024
        return dateFormat.format(date);
    }

}
